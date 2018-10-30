package com.rad8329.asiscontrol.handler;

import com.rad8329.asiscontrol.exception.RequestArgsException;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.Router;

import java.util.Arrays;

abstract class RequestHandler implements Handler<Router> {
    private FailureMessage failureMessage;
    private final Vertx vertx;

    RequestHandler(Vertx vertx) {
        this.vertx = vertx;
    }

    public Vertx getVertx() {
        return vertx;
    }

    private boolean hasFailure() {
        return failureMessage != null;
    }

    void handleFail(RoutingContext context) {
        int code = hasFailure() ? failureMessage.getCode() : 500;
        String message = hasFailure() ? failureMessage.getMessage() : "Unknown server error";

        context.response().setStatusCode(code);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(new JsonObject().put("success", false).put("error", message).encode());

        failureMessage = new FailureMessage();
    }

    void handleSimpleSuccess(RoutingContext context, int code) {
        context.response().setStatusCode(code);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(new JsonObject().put("success", true).encode());
    }

    void fail(RoutingContext context) {
        failureMessage = new FailureMessage();
        context.fail(failureMessage.getCode());
    }

    void fail(RoutingContext context, int code, String message) {
        failureMessage = new FailureMessage(code, message);
        context.fail(code);
    }

    boolean validateJsonPayload(RoutingContext context, JsonObject payload, String... expectedKeys) {
        if (!Arrays.stream(expectedKeys).allMatch(payload::containsKey)) {

            fail(context, 400, "Bad request payload");

            return false;
        }

        return true;
    }

    int getIntegerParam(RoutingContext context, String param) throws RequestArgsException {
        try {
            return Integer.valueOf(context.request().getParam(param));
        } catch (java.lang.NumberFormatException ex) {
            throw new RequestArgsException(400, "Bad get param");
        }
    }
}

class FailureMessage {

    private final String message;
    private final int code;

    FailureMessage(int code, String message) {
        this.message = message;
        this.code = code;
    }

    FailureMessage() {
        message = "Unknown server error";
        code = 500;
    }

    String getMessage() {
        return message;
    }

    int getCode() {
        return code;
    }
}

