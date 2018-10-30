package com.rad8329.asiscontrol.repository;

import com.rad8329.asiscontrol.entity.Track;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

import java.util.List;

@SuppressWarnings("unused")
public class TrackRepository extends Repository {

    public TrackRepository(JDBCClient client) {
        super(client);
    }

    private Single<SQLConnection> rxGetConnection() {
        return getClient().rxGetConnection().flatMap(conn -> {
            Single<SQLConnection> connectionSingle = Single.just(conn);
            return connectionSingle.doFinally(conn::close);
        });
    }

    @SuppressWarnings("UnusedReturnValue")
    public TrackRepository rxFetchAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        getClient().rxQuery("SELECT * FROM tracks t JOIN employees e ON e.code = t.employeeCode").flatMapPublisher(result -> {
            List<JsonArray> results = result.getResults();

            return Flowable.fromIterable(results);
        }).map(json -> json.getString(0)).sorted().collect(JsonArray::new, JsonArray::add)
                .subscribe(SingleHelper.toObserver(resultHandler));

        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TrackRepository rxFetchByid(String id, Handler<AsyncResult<JsonObject>> resultHandler) {
        String sql = "SELECT * FROM tracks t JOIN employees e ON e.code = t.employeeCode WHERE id = ?";

        getClient().rxQueryWithParams(sql, new JsonArray().add(id))
                .map(result -> {
                    if (result.getNumRows() > 0) {
                        JsonArray row = result.getResults().get(0);

                        return new JsonObject()
                                .put("found", true)
                                .put("id", row.getString(0))
                                .put("employeeCode", row.getInteger(1))
                                .put("createdAt", row.getString(2))
                                .put("action", row.getString(3))
                                .put("code", row.getInteger(4))
                                .put("firstName", row.getString(5))
                                .put("lastName", row.getString(6))
                                .put("email", row.getString(7))
                                .put("avatar", row.getString(8));
                    } else {
                        return new JsonObject().put("found", false);
                    }
                }).subscribe(SingleHelper.toObserver(resultHandler));

        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TrackRepository rxFetchLastActionFromEmployee(int code, Handler<AsyncResult<String>> resultHandler) {
        String sql = "SELECT action FROM tracks WHERE employeeCode = ? ORDER BY createdAt DESC LIMIT 1";

        getClient().rxQueryWithParams(sql, new JsonArray().add(code))
                .map(result -> {
                    if (result.getNumRows() > 0) {
                        return result.getResults().get(0).getString(0);
                    } else {
                        return "";
                    }
                }).subscribe(SingleHelper.toObserver(resultHandler));

        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TrackRepository rxCreate(Track track, Handler<AsyncResult<Void>> resultHandler) {
        getClient().rxUpdateWithParams("INSERT INTO tracks VALUES(?,?,?,?)", track.toJsonOArray()).toCompletable()
                .subscribe(CompletableHelper.toObserver(resultHandler));

        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TrackRepository rxFetchAllData(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "SELECT * FROM tracks t JOIN employees e ON e.code = t.employeeCode ORDER BY createdAt DESC";

        getClient().rxQuery(sql).map(ResultSet::getRows).subscribe(SingleHelper.toObserver(resultHandler));

        return this;
    }
}
