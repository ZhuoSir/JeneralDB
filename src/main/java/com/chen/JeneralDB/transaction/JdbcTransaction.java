package com.chen.JeneralDB.transaction;

import com.chen.JeneralDB.jdbc.Connections;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by sunny-chen on 17/1/18.
 */
public class JdbcTransaction implements Transaction {

    private Connection connection;

    private boolean isActive;

    public JdbcTransaction() throws Exception {
        connection = Connections.getConnection();
        connection.setAutoCommit(false);
        isActive = true;
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public void commit() {
        boolean ifSuccess = false;

        try {
            connection.commit();
            ifSuccess = true;
        } catch (SQLException e) {
            throw new RuntimeException("commit mistake", e);
        }

        if (ifSuccess) {
            dispose();
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("rollback mistake", e);
        } finally {
            dispose();
        }
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    public void dispose() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("close connection", e);
        } finally {
            isActive = false;
            connection = null;
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
