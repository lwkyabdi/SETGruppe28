package no.hiof.gruppe28;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.controller.KursController;
import no.hiof.gruppe28.repository.DatabaseException;
import no.hiof.gruppe28.repository.DemoKursRepository;
import no.hiof.gruppe28.repository.JdbcKursRepository;
import no.hiof.gruppe28.repository.KursRepository;
import no.hiof.gruppe28.service.KursService;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;

public class Application {
    public static void main(String[] args) throws Exception {
        Javalin app = create(args);
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        app.start(8080);
    }

    public static Javalin create(String... args) throws IOException {
        Clock clock = Clock.system(ZoneId.of("Europe/Oslo"));
        boolean demo = Arrays.asList(args).contains("--demo");
        KursRepository repository = demo
                ? new DemoKursRepository(LocalDate.now(clock))
                : new JdbcKursRepository(DatabaseConfig.load());
        if (demo) {
            System.getLogger(Application.class.getName())
                    .log(System.Logger.Level.INFO, "Starter med eksempeldata uten database.");
        }
        return create(new KursService(repository, clock));
    }

    public static Javalin create(KursService service) {
        KursController controller = new KursController(service);
        return Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.staticFiles.enableWebjars();
            config.routes.get("/api/health", ctx -> ctx.json(Map.of("status", "OK")));
            config.routes.get("/api/kurs", controller::alle);
            config.routes.get("/api/kurs/kommende", controller::kommende);
            config.routes.get("/api/kurs/{id}", controller::detalj);
            config.routes.exception(DatabaseException.class, (exception, ctx) -> {
                System.getLogger(Application.class.getName())
                        .log(System.Logger.Level.ERROR, "Feil ved lesing av kurs.", exception);
                ctx.status(503).json(Map.of("melding", "Kurs kan ikke hentes akkurat nå. Prøv igjen senere."));
            });
        });
    }
}
