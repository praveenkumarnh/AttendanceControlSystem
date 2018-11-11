package com.rad8329.asiscontrol;

import com.rad8329.asiscontrol.handler.EmployeeHandler;
import com.rad8329.asiscontrol.handler.TrackHandler;
import com.rad8329.asiscontrol.repository.EmployeeRepository;
import com.rad8329.asiscontrol.repository.TrackRepository;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.core.Future;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;

import java.sql.Timestamp;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    private JDBCClient databaseClient;

    @Override
    public void start(Future<Void> startFuture) {
        LOGGER.info("Starting verticle...");

        prepareDatabase().compose(_void -> startHttpServer()).setHandler(startFuture.completer());
    }

    private Future<Void> prepareDatabase() {
        Future<Void> future = Future.future();

        JsonObject JDBCClientConfig = new JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", config().getString("db.url"))                
                .put("maximumPoolSize", config().getInteger("db.maximumPoolSize", 8))
                .put("username", config().getString("db.username"))
                .put("password", config().getString("db.password"));
        
        try {
            databaseClient = JDBCClient.createShared(vertx, JDBCClientConfig);
            future.complete();
        } catch (Exception e) {
            LOGGER.error("Database connection refused");
            vertx.close();
        }

        return future;
    }

    private Future<Void> startHttpServer() {
        Future<Void> future = Future.future();

        int httpPort = config().getInteger("http.port", 8080);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/api").handler(this::greetingsHandler);

        //tag::sockjs-handler-setup[]
        BridgeOptions opts = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("tracked.employee"));

        SockJSHandler sockeJsHandler = SockJSHandler.create(vertx).bridge(opts);

        router.route("/eventbus/*").handler(sockeJsHandler);
        router.route().handler(StaticHandler.create());
        // end::sockjs-handler-setup[]
        
        //because wen dont need proxy services
        new EmployeeHandler(vertx, router, new EmployeeRepository(databaseClient));
        new TrackHandler(vertx, router, new TrackRepository(databaseClient));

        //noinspection ResultOfMethodCallIgnored
        server.requestHandler(router::accept).rxListen(httpPort).subscribe(success -> {
            LOGGER.info("HTTP server running on port " + httpPort);
            future.complete();
        }, error -> {
            LOGGER.error("Could not start a HTTP server", error);

            future.fail(error);
        });

        return future;
    }

    private void greetingsHandler(RoutingContext context) {
        context.response().setStatusCode(200);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(new JsonObject().put("success", true).put("message", "Services ready").encode());
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping verticle...");
    }
}
