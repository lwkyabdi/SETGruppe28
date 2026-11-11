package no.hiof.gruppe28;

import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.http.staticfiles.Location;
import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.config.EpostConfig;
import no.hiof.gruppe28.controller.AdminKursController;
import no.hiof.gruppe28.controller.AdminNyhetController;
import no.hiof.gruppe28.controller.AdminPameldingController;
import no.hiof.gruppe28.controller.BildeController;
import no.hiof.gruppe28.controller.InnloggingController;
import no.hiof.gruppe28.controller.KontaktpersonController;
import no.hiof.gruppe28.controller.KursController;
import no.hiof.gruppe28.controller.LokallagController;
import no.hiof.gruppe28.controller.NyhetController;
import no.hiof.gruppe28.controller.PameldingController;
import no.hiof.gruppe28.epost.EpostTjeneste;
import no.hiof.gruppe28.epost.KonsollEpostTjeneste;
import no.hiof.gruppe28.epost.SmtpEpostTjeneste;
import no.hiof.gruppe28.repository.DatabaseException;
import no.hiof.gruppe28.repository.DemoArtikkelRepository;
import no.hiof.gruppe28.repository.DemoKontaktpersonRepository;
import no.hiof.gruppe28.repository.DemoKursRepository;
import no.hiof.gruppe28.repository.DemoLokallagRepository;
import no.hiof.gruppe28.repository.DemoPameldingRepository;
import no.hiof.gruppe28.repository.JdbcAdminKursRepository;
import no.hiof.gruppe28.repository.JdbcAdminPameldingRepository;
import no.hiof.gruppe28.repository.JdbcAdministratorRepository;
import no.hiof.gruppe28.repository.JdbcArtikkelRepository;
import no.hiof.gruppe28.repository.JdbcKontaktpersonRepository;
import no.hiof.gruppe28.repository.JdbcKursRepository;
import no.hiof.gruppe28.repository.JdbcLokallagRepository;
import no.hiof.gruppe28.repository.JdbcPameldingRepository;
import no.hiof.gruppe28.service.AdminKursService;
import no.hiof.gruppe28.service.AdminNyhetService;
import no.hiof.gruppe28.service.AdminPameldingService;
import no.hiof.gruppe28.service.ApiException;
import no.hiof.gruppe28.service.BildeService;
import no.hiof.gruppe28.service.InnloggingService;
import no.hiof.gruppe28.service.KontaktpersonService;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.service.LokallagService;
import no.hiof.gruppe28.service.NyhetService;
import no.hiof.gruppe28.service.PameldingService;

import java.io.IOException;
import java.nio.file.Path;
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
                    "Starter med eksempeldata. Påmeldinger lagres bare i minnet."
                );

            var repository = new DemoKursRepository(LocalDate.now(clock));

            var demoPamelding = new PameldingService(
                new DemoPameldingRepository(repository),
                clock,
                new KonsollEpostTjeneste()
            );

            return create(
                new KursService(repository, clock),
                demoPamelding,
                new NyhetService(new DemoArtikkelRepository()),
                null,
                new LokallagService(new DemoLokallagRepository()),
                new KontaktpersonService(new DemoKontaktpersonRepository())
            );
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

        var nyhetService = new NyhetService(new JdbcArtikkelRepository(database));

        var innloggingService = new InnloggingService(new JdbcAdministratorRepository(database));

        var lokallagService = new LokallagService(new JdbcLokallagRepository(database));

        var kontaktpersonService = new KontaktpersonService(new JdbcKontaktpersonRepository(database));

        var adminPameldingService = new AdminPameldingService(new JdbcAdminPameldingRepository(database));

        var adminKursService = new AdminKursService(new JdbcAdminKursRepository(database));

        // Byttes til new AdminNyhetService(new JdbcAdminArtikkelRepository(database), clock)
        // når Taulant har laget JdbcAdminArtikkelRepository.
        AdminNyhetService adminNyhetService = null;

        // Opplastede bilder lagres i mappen opplastinger/ og vises på /opplastinger/...
        var bildeService = new BildeService(Path.of("opplastinger"), "/opplastinger");

        return create(
            kursService,
            pameldingService,
            nyhetService,
            innloggingService,
            lokallagService,
            kontaktpersonService,
            adminPameldingService,
            adminKursService,
            adminNyhetService,
            bildeService
        );
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
        return create(
            service,
            null,
            new NyhetService(new DemoArtikkelRepository()),
            null,
            new LokallagService(new DemoLokallagRepository()),
            new KontaktpersonService(new DemoKontaktpersonRepository())
        );
    }

    // Uten admin. Brukes av testene som ikke trenger den.
    public static Javalin create(
        KursService service,
        PameldingService pameldingService,
        NyhetService nyhetService,
        InnloggingService innloggingService,
        LokallagService lokallagService,
        KontaktpersonService kontaktpersonService
    ) {
        return create(service, pameldingService, nyhetService, innloggingService,
            lokallagService, kontaktpersonService, null);
    }

    // Med admin for påmeldinger.
    public static Javalin create(
        KursService service,
        PameldingService pameldingService,
        NyhetService nyhetService,
        InnloggingService innloggingService,
        LokallagService lokallagService,
        KontaktpersonService kontaktpersonService,
        AdminPameldingService adminPameldingService
    ) {
        return create(service, pameldingService, nyhetService, innloggingService,
            lokallagService, kontaktpersonService, adminPameldingService, null);
    }

    // Med admin for påmeldinger og kurs.
    public static Javalin create(
        KursService service,
        PameldingService pameldingService,
        NyhetService nyhetService,
        InnloggingService innloggingService,
        LokallagService lokallagService,
        KontaktpersonService kontaktpersonService,
        AdminPameldingService adminPameldingService,
        AdminKursService adminKursService
    ) {
        return create(service, pameldingService, nyhetService, innloggingService,
            lokallagService, kontaktpersonService, adminPameldingService, adminKursService,
            null, null);
    }

    public static Javalin create(
        KursService service,
        PameldingService pameldingService,
        NyhetService nyhetService,
        InnloggingService innloggingService,
        LokallagService lokallagService,
        KontaktpersonService kontaktpersonService,
        AdminPameldingService adminPameldingService,
        AdminKursService adminKursService,
        AdminNyhetService adminNyhetService,
        BildeService bildeService
    ) {
        var kursController = new KursController(service);
        var nyhetController = new NyhetController(nyhetService);
        var lokallagController = new LokallagController(lokallagService);
        var kontaktpersonController = new KontaktpersonController(kontaktpersonService);

        return Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.staticFiles.enableWebjars();

            // Opplastede bilder vises fra en egen mappe utenfor koden
            if (bildeService != null) {
                config.staticFiles.add(filer -> {
                    filer.hostedPath = bildeService.adresse();
                    filer.directory = bildeService.mappe().toAbsolutePath().toString();
                    filer.location = Location.EXTERNAL;
                });
            }

            config.routes.get(
                "/api/health",
                ctx -> ctx.json(Map.of("status", "OK"))
            );

            config.routes.get(
                "/api/lokallag",
                lokallagController::alle
            );

            config.routes.get(
                "/api/kontaktpersoner",
                kontaktpersonController::forKontaktside
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

                // Admin for påmeldinger: bare når innloggingen er på, så vakten alltid beskytter dem
                if (adminPameldingService != null && pameldingService != null) {
                    var adminPameldingController = new AdminPameldingController(
                        adminPameldingService,
                        new PameldingController(pameldingService)
                    );

                    config.routes.get(
                        "/api/admin/oversikt",
                        adminPameldingController::oversikt
                    );

                    config.routes.get(
                        "/api/admin/pameldinger",
                        adminPameldingController::pameldinger
                    );

                    config.routes.post(
                        "/api/admin/pameldinger/{referanse}/gi-plass",
                        adminPameldingController::giPlass
                    );

                    config.routes.delete(
                        "/api/admin/pameldinger/{referanse}",
                        adminPameldingController::slett
                    );

                    config.routes.post(
                        "/api/admin/kurs/{id}/pameldinger",
                        adminPameldingController::meldPa
                    );
                }

                // Admin for kurs: også bare bak vakten
                if (adminKursService != null) {
                    var adminKursController = new AdminKursController(adminKursService);

                    config.routes.get(
                        "/api/admin/kurs",
                        adminKursController::alle
                    );

                    config.routes.get(
                        "/api/admin/kurs/{id}",
                        adminKursController::finn
                    );

                    config.routes.post(
                        "/api/admin/kurs",
                        adminKursController::opprett
                    );

                    config.routes.put(
                        "/api/admin/kurs/{id}",
                        adminKursController::oppdater
                    );

                    config.routes.delete(
                        "/api/admin/kurs/{id}",
                        adminKursController::slett
                    );
                }

                // Admin for nyheter: også bare bak vakten
                if (adminNyhetService != null) {
                    var adminNyhetController = new AdminNyhetController(adminNyhetService);

                    config.routes.get(
                        "/api/admin/nyheter",
                        adminNyhetController::alle
                    );

                    config.routes.get(
                        "/api/admin/nyheter/{id}",
                        adminNyhetController::finn
                    );

                    config.routes.post(
                        "/api/admin/nyheter",
                        adminNyhetController::opprett
                    );

                    config.routes.put(
                        "/api/admin/nyheter/{id}",
                        adminNyhetController::oppdater
                    );

                    config.routes.delete(
                        "/api/admin/nyheter/{id}",
                        adminNyhetController::slett
                    );
                }

                // Bildeopplasting: også bare bak vakten
                if (bildeService != null) {
                    var bildeController = new BildeController(bildeService);

                    config.routes.post(
                        "/api/admin/bilder",
                        bildeController::lastOpp
                    );
                }
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
