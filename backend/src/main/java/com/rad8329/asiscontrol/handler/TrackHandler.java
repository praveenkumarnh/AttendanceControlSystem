package com.rad8329.asiscontrol.handler;

import com.rad8329.asiscontrol.entity.Track;
import com.rad8329.asiscontrol.repository.TrackRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import java.util.stream.Collectors;

public final class TrackHandler extends RequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackHandler.class);
    private final TrackRepository repository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public TrackHandler(Vertx vertx, Router router, TrackRepository repository) {
        super(vertx);
        this.repository = repository;

        handle(router);
    }

    @Override
    public void handle(Router router) {
        router.get("/api/tracks").handler(this::root).failureHandler(this::handleFail);
        router.get("/api/tracks/:id").handler(this::get).failureHandler(this::handleFail);

        router.post().handler(BodyHandler.create());
        router.post("/api/tracks").handler(this::create).failureHandler(this::handleFail);
    }

    private void create(RoutingContext context) {

        JsonObject payload = context.getBodyAsJson();

        Future<Track> futureTrack = loadJsonPayloadIntoTrack(
                context, payload, "employeeCode"
        );

        if (futureTrack != null) {
            futureTrack.setHandler(
                    ar -> {
                        if (ar.succeeded()) {
                            Track track = ar.result();

                            repository.rxCreate(track, reply -> {
                                if (reply.succeeded()) {

                                    //Send a message to the consumers
                                    repository.rxFetchByid(track.getId(), arf -> {
                                        if (arf.succeeded()) {
                                            getVertx().eventBus().publish("asiscontrol.client", arf.result());
                                        }
                                    });

                                    handleSimpleSuccess(context, 201);
                                } else {
                                    LOGGER.error("DB Error result: " + track.toString() + " and " + reply.cause().getMessage() + " from "
                                            + context.request().remoteAddress());

                                    fail(context, 500, reply.cause().getMessage());
                                }
                            });
                        } else {
                            futureTrack.fail(ar.cause());
                        }
                    }
            );
        } else {
            fail(context);
        }
    }

    private void get(RoutingContext context) {

        String id = context.request().getParam("id");

        repository.rxFetchByid(id, reply -> {
            if (reply.succeeded()) {
                JsonObject object = reply.result();

                if (object.getBoolean("found")) {

                    JsonObject response = new JsonObject()
                            .put("success", true)
                            .put(
                                    "track",
                                    new JsonObject()
                                            .put("id", object.getString("id"))
                                            .put("createdAt", formatter.format(object.getInstant("createdAt")))
                                            .put("action", object.getString("action"))
                            )
                            .put(
                                    "employee",
                                    new JsonObject()
                                            .put("code", object.getInteger("code"))
                                            .put("firstName", object.getString("firstName"))
                                            .put("lastName", object.getString("lastName"))
                                            .put("email", object.getString("email"))
                                            .put("avatar", object.getString("avatar"))
                            );

                    context.response().putHeader("Content-Type", "application/json");
                    context.response().setStatusCode(200).end(response.encode());
                } else {
                    fail(context, 404, "There is no track with that id");
                }
            } else {
                LOGGER.error("DB Error result: " + reply.cause().getMessage() + " from "
                        + context.request().remoteAddress());

                fail(context, 500, reply.cause().getMessage());
            }
        });
    }

    private void root(RoutingContext context) {
        repository.rxFetchAllData(reply -> {
            JsonObject response = new JsonObject();
            if (reply.succeeded()) {

                List<JsonObject> tracks = reply.result().stream()
                        .map(object -> new JsonObject()
                                .put(
                                        "track",
                                        new JsonObject()
                                                .put("id", object.getString("id"))
                                                .put("createdAt", formatter.format(object.getInstant("createdAt")))
                                                .put("action", object.getString("action"))
                                )
                                .put(
                                        "employee",
                                        new JsonObject()
                                                .put("code", object.getInteger("code"))
                                                .put("firstName", object.getString("firstName"))
                                                .put("lastName", object.getString("lastName"))
                                                .put("email", object.getString("email"))
                                                .put("avatar", object.getString("avatar"))
                                )
                        )
                        .collect(Collectors.toList());

                response.put("success", true).put("tracks", tracks);

                context.response().setStatusCode(200);
                context.response().putHeader("Content-Type", "application/json");
                context.response().end(response.encode());
            } else {
                LOGGER.error("DB Error result: " + reply.cause().getMessage() + " from "
                        + context.request().remoteAddress());

                fail(context, 500, reply.cause().getMessage());
            }
        });
    }

    private Future<Track> loadJsonPayloadIntoTrack(RoutingContext context, JsonObject payload, String... expectedKeys) {
        if (validateJsonPayload(context, payload, expectedKeys)) {
            try {
                int code = payload.getInteger("employeeCode");

                final Future<Track> futureTrack = Future.future();

                repository.rxFetchLastActionFromEmployee(code, reply -> {
                    if (reply.succeeded()) {
                        String action = reply.result();

                        if (action.isEmpty() || action.equals("exit")) {
                            action = "entrance";
                        } else {
                            action = "exit";
                        }

                        futureTrack.complete(new Track(code, new Date(), action));
                    } else {
                        LOGGER.error("DB Error result: " + reply.cause().getMessage() + " from "
                                + context.request().remoteAddress());

                        fail(context, 500, reply.cause().getMessage());
                    }
                });

                return futureTrack;
            } catch (NullPointerException ne) {
                fail(context, 400, "Bad request payload");

                return null;
            }
        }

        return null;
    }
}
