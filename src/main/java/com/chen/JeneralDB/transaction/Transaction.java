package com.chen.JeneralDB.transaction;

import java.sql.SQLException;

/**
 * Created by sunny-chen on 17/1/18.
 */
public interface Transaction {

    String getID();

    void commit() throws SQLException;

    void rollback() throws SQLException;

    boolean isActive();
}
