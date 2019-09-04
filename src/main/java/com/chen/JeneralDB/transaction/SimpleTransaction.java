package com.chen.JeneralDB.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class SimpleTransaction implements Transaction {

    private Connection connection;

    public SimpleTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public String getID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    @Override
    public boolean isActive() {
        return false;
    }

    public Connection getConnection() {
        return connection;
    }
}
