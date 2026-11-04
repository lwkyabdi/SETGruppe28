package no.hiof.gruppe28;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.controller.KursController;
import no.hiof.gruppe28.controller.PameldingController;
import no.hiof.gruppe28.repository.DatabaseException;
import no.hiof.gruppe28.repository.DemoKursRepository;
import no.hiof.gruppe28.repository.JdbcKursRepository;
import no.hiof.gruppe28.repository.JdbcPameldingRepository;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.service.PameldingException;
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
                clock
        );

        return create(kursService, pameldingService);
    }

    public static Javalin create(KursService service) {
        return create(service, null);
    }

    public static Javalin create(
            KursService service,
            PameldingService pameldingService
    ) {
        var kursController = new KursController(service);

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

            if (pameldingService != null) {
                var pameldingController =
                        new PameldingController(pameldingService);

                config.routes.post(
                        "/api/kurs/{id}/pameldinger",
                        pameldingController::registrer
                );
            }

            config.routes.exception(
                    PameldingException.class,
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
        });
    }
}
