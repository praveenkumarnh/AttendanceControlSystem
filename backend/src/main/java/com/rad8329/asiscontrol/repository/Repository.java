package com.rad8329.asiscontrol.repository;

import io.vertx.reactivex.ext.jdbc.JDBCClient;

abstract class Repository {
    private final JDBCClient client;

    Repository(JDBCClient client) {
        this.client = client;
    }

    JDBCClient getClient() {
        return client;
    }
}
