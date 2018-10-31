package com.rad8329.asiscontrol.handler;

import com.rad8329.asiscontrol.entity.Employee;
import com.rad8329.asiscontrol.exception.RequestArgsException;
import com.rad8329.asiscontrol.repository.EmployeeRepository;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

public final class EmployeeHandler extends RequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeHandler.class);
    private final EmployeeRepository repository;

    public EmployeeHandler(Vertx vertx, Router router, EmployeeRepository repository) {
        super(vertx);
        this.repository = repository;

        handle(router);
    }

    @Override
    public void handle(Router router) {
        router.get("/api/employees").handler(this::root).failureHandler(this::handleFail);
        router.get("/api/employees/:code").handler(this::get).failureHandler(this::handleFail);

        router.post().handler(BodyHandler.create());
        router.post("/api/employees").handler(this::create).failureHandler(this::handleFail);

        router.put().handler(BodyHandler.create());
        router.put("/api/employees/:code").handler(this::update).failureHandler(this::handleFail);
        router.delete("/api/employees/:code").handler(this::delete).failureHandler(this::handleFail);
    }

    private void delete(RoutingContext context) {
        try {
            int code = getIntegerParam(context, "code");

            repository.rxFetchByCode(code, reply -> {
                if (reply.succeeded()) {
                    JsonObject dbObject = reply.result();

                    if (dbObject.getBoolean("found")) {

                        repository.rxDelete(code, deleteReply -> {
                            if (deleteReply.succeeded()) {
                                handleSimpleSuccess(context, 200);
                            } else {
                                LOGGER.error("DB Error result: code = " + code + " and " +
                                        deleteReply.cause().getMessage() + " from "
                                        + context.request().remoteAddress());

                                fail(context, 500, deleteReply.cause().getMessage());
                            }
                        });
                    } else {
                        fail(context, 404, "There is no employee with that code");
                    }
                } else {
                    LOGGER.error("DB Error result: code = " + code + " and " + reply.cause().getMessage() + " from "
                            + context.request().remoteAddress());

                    fail(context, 500, reply.cause().getMessage());
                }
            });
        } catch (RequestArgsException re) {
            fail(context, re.getCode(), re.getMessage());
        }
    }

    private void update(RoutingContext context) {
        JsonObject payload = context.getBodyAsJson();

        Employee newdataForEmployee = loadJsonPayloadIntoEmployee(
                context, payload, "code", "firstName", "lastName", "email", "avatar"
        );

        if (newdataForEmployee != null) {

            try {
                int code = getIntegerParam(context, "code");

                repository.rxFetchByCode(code, reply -> {
                    if (reply.succeeded()) {
                        JsonObject dbObject = reply.result();

                        if (dbObject.getBoolean("found")) {

                            repository.rxUpdate(code, newdataForEmployee, updateReply -> {
                                if (updateReply.succeeded()) {
                                    handleSimpleSuccess(context, 200);
                                } else {
                                    LOGGER.error("DB Error result: code = " + code + " and "
                                            + newdataForEmployee.toString() + " and "
                                            + updateReply.cause().getMessage() + " from "
                                            + context.request().remoteAddress());

                                    fail(context, 500, updateReply.cause().getMessage());
                                }
                            });
                        } else {
                            fail(context, 404, "There is no employee with that code");
                        }
                    } else {
                        LOGGER.error("DB Error result: code = " + code + " and "
                                + newdataForEmployee.toString() + " and " + reply.cause().getMessage() + " from "
                                + context.request().remoteAddress());

                        fail(context, 500, reply.cause().getMessage());
                    }
                });
            } catch (RequestArgsException re) {
                fail(context, re.getCode(), re.getMessage());
            }
        }
    }

    private void create(RoutingContext context) {

        JsonObject payload = context.getBodyAsJson();

        Employee employee = loadJsonPayloadIntoEmployee(
                context, payload, "code", "firstName", "lastName", "email", "avatar"
        );

        if (employee != null) {
            repository.rxCreate(employee, reply -> {
                if (reply.succeeded()) {
                    handleSimpleSuccess(context, 201);
                } else {
                    LOGGER.error("DB Error result: " + employee.toString() + " and "
                            + reply.cause().getMessage() + " from "
                            + context.request().remoteAddress());

                    fail(context, 500, reply.cause().getMessage());
                }
            });
        }
    }

    private void get(RoutingContext context) {

        try {
            int code = getIntegerParam(context, "code");

            repository.rxFetchByCode(code, reply -> {
                if (reply.succeeded()) {
                    JsonObject object = reply.result();

                    if (object.getBoolean("found")) {
                        JsonObject employee = new JsonObject()
                                .put("code", object.getInteger("code"))
                                .put("firstName", object.getString("firstName"))
                                .put("lastName", object.getString("lastName"))
                                .put("email", object.getString("email"))
                                .put("avatar", object.getString("avatar"));

                        JsonObject response = new JsonObject();
                        response.put("success", true).put("employee", employee);

                        context.response().putHeader("Content-Type", "application/json");
                        context.response().setStatusCode(200).end(response.encode());
                    } else {
                        fail(context, 404, "There is no employee with that code");
                    }
                } else {
                    LOGGER.error("DB Error result: " + reply.cause().getMessage() + " from "
                            + context.request().remoteAddress());

                    fail(context, 500, reply.cause().getMessage());
                }
            });
        } catch (RequestArgsException re) {
            fail(context, re.getCode(), re.getMessage());
        }
    }

    private void root(RoutingContext context) {
        repository.rxFetchAllData(reply -> {
            JsonObject response = new JsonObject();
            if (reply.succeeded()) {
                List<JsonObject> employees = reply.result().stream()
                        .map(object -> new JsonObject()
                                .put("code", object.getInteger("code"))
                                .put("firstName", object.getString("firstName"))
                                .put("lastName", object.getString("lastName"))
                                .put("email", object.getString("email"))
                                .put("avatar", object.getString("avatar")))
                        .collect(Collectors.toList());

                response.put("success", true).put("employees", employees);

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

    private Employee loadJsonPayloadIntoEmployee(RoutingContext context, JsonObject payload, String... expectedKeys) {
        if (validateJsonPayload(context, payload, expectedKeys)) {
            try {
                return new Employee(
                        payload.getInteger("code"),
                        payload.getString("firstName"),
                        payload.getString("lastName"),
                        payload.getString("email"),
                        payload.getString("avatar")
                );
            } catch (NullPointerException ne) {
                fail(context, 400, "Bad request payload");

                return null;
            }
        }

        LOGGER.error("Bad employee JSON payload: " + payload.encodePrettily() + " from "
                + context.request().remoteAddress());

        return null;
    }
}
