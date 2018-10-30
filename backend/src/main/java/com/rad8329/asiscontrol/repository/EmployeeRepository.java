package com.rad8329.asiscontrol.repository;

import com.rad8329.asiscontrol.entity.Employee;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.ext.sql.SQLConnection;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.SingleHelper;

import java.util.List;

public class EmployeeRepository extends Repository {

    public EmployeeRepository(JDBCClient client) {
        super(client);
    }

    private Single<SQLConnection> rxGetConnection() {
        return getClient().rxGetConnection().flatMap(conn -> {
            Single<SQLConnection> connectionSingle = Single.just(conn);

            return connectionSingle.doFinally(conn::close);
        });
    }
    
    public EmployeeRepository rxFetchAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        getClient().rxQuery("SELECT * FROM employees").flatMapPublisher(result -> {
            List<JsonArray> results = result.getResults();

            return Flowable.fromIterable(results);
        }).map(json -> json.getString(0)).sorted().collect(JsonArray::new, JsonArray::add)
                .subscribe(SingleHelper.toObserver(resultHandler));

        return this;
    }

    public EmployeeRepository rxFetchByCode(int code, Handler<AsyncResult<JsonObject>> resultHandler) {
        getClient().rxQueryWithParams("SELECT * FROM employees WHERE code = ?", new JsonArray().add(code))
                .map(result -> {
                    if (result.getNumRows() > 0) {
                        JsonArray row = result.getResults().get(0);

                        return new JsonObject()
                                .put("found", true)
                                .put("code", row.getInteger(0))
                                .put("firstName", row.getString(1))
                                .put("lastName", row.getString(2))
                                .put("email", row.getString(3))
                                .put("avatar", row.getString(4));
                    } else {
                        return new JsonObject().put("found", false);
                    }
                }).subscribe(SingleHelper.toObserver(resultHandler));

        return this;
    }

    public EmployeeRepository rxCreate(Employee employee, Handler<AsyncResult<Void>> resultHandler) {
        getClient().rxUpdateWithParams("INSERT INTO employees VALUES(?,?,?,?,?)", employee.toJsonOArray())
                .toCompletable()
                .subscribe(CompletableHelper.toObserver(resultHandler));

        return this;
    }

    public EmployeeRepository rxUpdate(int code, Employee employee, Handler<AsyncResult<Void>> resultHandler) {

        String sql = "UPDATE employees SET code = ?, firstName = ?, lastName = ?,"
                + "email = ?, avatar = ? WHERE code = ?";

        getClient().rxUpdateWithParams(sql, employee.toJsonOArray().add(code))
                .toCompletable().subscribe(CompletableHelper.toObserver(resultHandler));

        return this;
    }

    public EmployeeRepository rxDelete(int code, Handler<AsyncResult<Void>> resultHandler) {
        getClient().rxUpdateWithParams("DELETE FROM employees WHERE code = ?", new JsonArray().add(code))
                .toCompletable()
                .subscribe(CompletableHelper.toObserver(resultHandler));

        return this;
    }

    public EmployeeRepository rxFetchAllData(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        getClient().rxQuery("SELECT * FROM employees").map(ResultSet::getRows)
                .subscribe(SingleHelper.toObserver(resultHandler));

        return this;
    }
}
