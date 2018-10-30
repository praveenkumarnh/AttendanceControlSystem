package com.rad8329.asiscontrol.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface Entity {
    JsonObject toJsonObject();

    JsonArray toJsonOArray();
}
