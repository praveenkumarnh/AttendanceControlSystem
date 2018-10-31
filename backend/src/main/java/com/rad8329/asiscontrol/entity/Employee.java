package com.rad8329.asiscontrol.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Employee implements Entity {

    private final int code;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String avatar;

    public Employee(int code, String firstName, String lastName, String email, String avatar) {
        this.code = code;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.avatar = avatar;
    }

    public int getCode() {
        return code;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();

        if (code > 0)
            json.put("code", code);
        else
            json.putNull("code");

        if (firstName != null)
            json.put("firstName", firstName);
        else
            json.putNull("firstName");

        if (lastName != null)
            json.put("lastName", lastName);
        else
            json.putNull("lastName");

        if (email != null)
            json.put("email", email);
        else
            json.putNull("email");

        if (avatar != null)
            json.put("avatar", avatar);
        else
            json.putNull("avatar");

        return json;
    }

    @Override
    public JsonArray toJsonOArray() {
        JsonArray json = new JsonArray();

        if (code > 0)
            json.add(code);
        else
            json.addNull();

        if (firstName != null)
            json.add(firstName);
        else
            json.addNull();

        if (lastName != null)
            json.add(lastName);
        else
            json.addNull();

        if (email != null)
            json.add(email);
        else
            json.addNull();

        if (avatar != null)
            json.add(avatar);
        else
            json.addNull();

        return json;
    }

    @Override
    public String toString() {
        return "Employee{" + "code=" + code + ", firstName='" + firstName
                + "', lastName='" + lastName + "', email='" + email
                + "', avatar='" + avatar + '}';
    }
}
