package no.hiof.gruppe28;

import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.http.staticfiles.Location;
import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.config.EpostConfig;
import no.hiof.gruppe28.controller.InnloggingController;
import no.hiof.gruppe28.controller.KursController;
import no.hiof.gruppe28.controller.NyhetController;
import no.hiof.gruppe28.controller.PameldingController;
import no.hiof.gruppe28.epost.EpostTjeneste;
import no.hiof.gruppe28.epost.KonsollEpostTjeneste;
import no.hiof.gruppe28.epost.SmtpEpostTjeneste;
import no.hiof.gruppe28.repository.DatabaseException;
import no.hiof.gruppe28.repository.DemoKursRepository;
import no.hiof.gruppe28.repository.DemoNyhetRepository;
import no.hiof.gruppe28.repository.JdbcKursRepository;
import no.hiof.gruppe28.repository.JdbcPameldingRepository;
import no.hiof.gruppe28.service.ApiException;
import no.hiof.gruppe28.service.InnloggingService;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.service.NyhetService;
import no.hiof.gruppe28.service.PameldingService;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;

public class Application {

    public static void main(String[] args) throws IOException {
        Javalin app = create(args);

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

        app.start(8080);
    }

    public static Javalin create(String... args) throws IOException {
        Clock clock = Clock.system(ZoneId.of("Europe/Oslo"));

        boolean demo = Arrays.asList(args).contains("--demo");

        if (demo) {
            System.getLogger(Application.class.getName())
                .log(
                    System.Logger.Level.INFO,
                    "Starter med eksempeldata. Påmelding krever MySQL."
                );

            var repository = new DemoKursRepository(LocalDate.now(clock));

            return create(new KursService(repository, clock));
        }

        var database = DatabaseConfig.load();

        var kursService = new KursService(
            new JdbcKursRepository(database),
            clock
        );

        var pameldingService = new PameldingService(
            new JdbcPameldingRepository(database),
            clock,
            lagEpostTjeneste()
        );

        // Byttes til JdbcNyhetRepository når tabellen nyhet finnes i MySQL.
        var nyhetService = new NyhetService(new DemoNyhetRepository());

        // Byttes til new InnloggingService(new JdbcAdministratorRepository(database))
        // når Taulant har laget JdbcAdministratorRepository.
        InnloggingService innloggingService = null;

        return create(kursService, pameldingService, nyhetService, innloggingService);
    }

    private static EpostTjeneste lagEpostTjeneste() throws IOException {
        var logger = System.getLogger(Application.class.getName());

        if (EpostConfig.finnes()) {
            logger.log(System.Logger.Level.INFO, "Sender ekte e-post med SMTP.");
            return new SmtpEpostTjeneste(EpostConfig.load());
        }

        logger.log(System.Logger.Level.INFO,
            "Fant ikke epostConfig.properties. E-post skrives i terminalen.");
        return new KonsollEpostTjeneste();
    }

    public static Javalin create(KursService service) {
        return create(service, null, new NyhetService(new DemoNyhetRepository()), null);
    }

    public static Javalin create(
        KursService service,
        PameldingService pameldingService,
        NyhetService nyhetService,
        InnloggingService innloggingService
    ) {
        var kursController = new KursController(service);
        var nyhetController = new NyhetController(nyhetService);

        return Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.staticFiles.enableWebjars();

            config.routes.get(
                "/api/health",
                ctx -> ctx.json(Map.of("status", "OK"))
            );

            config.routes.get(
                "/api/kurs",
                kursController::alle
            );

            config.routes.get(
                "/api/kurs/kommende",
                kursController::kommende
            );

            config.routes.get(
                "/api/kurs/{id}",
                kursController::detalj
            );

            config.routes.get(
                "/api/nyheter",
                nyhetController::alle
            );

            config.routes.get(
                "/api/nyheter/{id}",
                nyhetController::detalj
            );

            if (pameldingService != null) {
                var pameldingController =
                    new PameldingController(pameldingService);

                config.routes.post(
                    "/api/kurs/{id}/pameldinger",
                    pameldingController::registrer
                );
            }

            if (innloggingService != null) {
                var innloggingController =
                    new InnloggingController(innloggingService);

                config.routes.post(
                    "/api/login",
                    innloggingController::loggInn
                );

                config.routes.post(
                    "/api/logout",
                    innloggingController::loggUt
                );

                config.routes.get(
                    "/api/meg",
                    innloggingController::meg
                );

                config.routes.before(
                    "/api/admin/*",
                    innloggingController::krevInnlogging
                );
            }

            config.routes.exception(
                ApiException.class,
                (exception, ctx) -> {
                    ctx.header("Cache-Control", "no-store");

                    ctx.status(exception.status()).json(
                        Map.of("melding", exception.getMessage())
                    );
                }
            );

            config.routes.exception(
                DatabaseException.class,
                (exception, ctx) -> {
                    System.getLogger(Application.class.getName())
                        .log(
                            System.Logger.Level.ERROR,
                            "Databaseoperasjonen feilet.",
                            exception
                        );

                    ctx.header("Cache-Control", "no-store");

                    ctx.status(503).json(
                        Map.of(
                            "melding",
                            "Databasen er utilgjengelig. "
                                + "Prøv igjen senere."
                        )
                    );
                }
            );

            config.routes.exception(
                Exception.class,
                (exception, ctx) -> {
                    if (exception instanceof HttpResponseException javalinFeil) {
                        ctx.status(javalinFeil.getStatus()).json(
                            Map.of("melding", javalinFeil.getMessage())
                        );
                        return;
                    }

                    System.getLogger(Application.class.getName())
                        .log(
                            System.Logger.Level.ERROR,
                            "Uventet feil.",
                            exception
                        );

                    ctx.header("Cache-Control", "no-store");

                    ctx.status(500).json(
                        Map.of(
                            "melding",
                            "Noe gikk galt. Prøv igjen senere."
                        )
                    );
                }
            );
        });
    }
}
