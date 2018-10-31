package com.rad8329.asiscontrol.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Date;

public class Track implements Entity {

    private final DateTimeFormatter formatter = DateTimeFormatter.
            ofPattern("YYYY-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final String id;
    private final int employeeCode;
    private final Date createdAt;
    private final String action; // possible values [entrance|exit]

    public Track(int employeeCode, Date createdAt, String action) {
        this.id = UUID.randomUUID().toString();
        this.employeeCode = employeeCode;
        this.createdAt = createdAt;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public int getEmployeeCode() {
        return employeeCode;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getAction() {
        return action;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();

        if (id != null)
            json.put("id", id);
        else
            json.putNull("id");

        if (employeeCode > 0)
            json.put("employeeCode", employeeCode);
        else
            json.putNull("employeeCode");

        if (createdAt != null)
            json.put("ceratedAt", createdAt.toInstant());
        else
            json.putNull("ceratedAt");

        if (action != null)
            json.put("action", action);
        else
            json.putNull("action");

        return json;
    }

    @Override
    public JsonArray toJsonOArray() {
        JsonArray json = new JsonArray();

        if (id != null)
            json.add(id);
        else
            json.addNull();

        if (employeeCode > 0)
            json.add(employeeCode);
        else
            json.addNull();

        if (createdAt != null)
            json.add(createdAt.toInstant());
        else
            json.addNull();

        if (action != null)
            json.add(action);
        else
            json.addNull();

        return json;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id='" + id + '\'' +
                ", employeeCode=" + employeeCode +
                ", createdAt=" +  formatter.format(createdAt.toInstant()) +
                ", action='" + action + '\'' +
                '}';
    }
}
