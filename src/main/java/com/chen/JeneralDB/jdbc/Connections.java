package com.chen.JeneralDB.jdbc;

import com.chen.JeneralDB.DBFactory;
import com.chen.JeneralDB.DBUtil;
import com.chen.JeneralDB.common.RuntimeDatabase;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by sunny-chen on 17/1/18.
 */
public final class Connections {

    private static String poolType;

    private static final int CONN_TIMEOUT = 5000;

    private static String url;

    private static String userName;

    private static String password;

    private static int minConnCnt;

    private static int maxConnCnt;

    private static String transactionIsolation;

    private static int transactionIsolationInt;

    static {
        try {
            RuntimeDatabase runtimeDatabase = RuntimeDatabase.valueOf(DBFactory.getProperty("runtimeDatabase"));

            if (RuntimeDatabase.NONE != runtimeDatabase) {
                final String driver = DBFactory.getProperty("db_driver");

                Class.forName(driver);

                poolType = DBFactory.getProperty("db_poolType");

                url = DBFactory.getProperty("db_url");
                userName = DBFactory.getProperty("db_username");
                password = DBFactory.getProperty("db_password");
                minConnCnt = Integer.valueOf(DBFactory.getProperty("db_minConnCnt"));
                maxConnCnt = Integer.valueOf(DBFactory.getProperty("db_maxConnCnt"));

                transactionIsolation = DBFactory.getProperty("db_transactionIsolation");
                if ("NONE".equals(transactionIsolation)) {
                    transactionIsolationInt = Connection.TRANSACTION_NONE;
                } else if ("READ_COMMITTED".equals(transactionIsolation)) {
                    transactionIsolationInt = Connection.TRANSACTION_READ_COMMITTED;
                } else if ("READ_UNCOMMITTED".equals(transactionIsolation)) {
                    transactionIsolationInt = Connection.TRANSACTION_READ_UNCOMMITTED;
                } else if ("REPEATABLE_READ".equals(transactionIsolation)) {
                    transactionIsolationInt = Connection.TRANSACTION_REPEATABLE_READ;
                } else if ("SERIALIZABLE".equals(transactionIsolation)) {
                    transactionIsolationInt = Connection.TRANSACTION_SERIALIZABLE;
                } else {
                    throw new IllegalStateException("Undefined transaction isolation [" + transactionIsolation + "]");
                }

                if ("h2".equals(poolType)) {

                } else if ("druid".equals(poolType)) {

                } else if ("none".equals(poolType)) {
                    DBUtil.print("Do not use database connection pool");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            DBUtil.print("Can not initialize database connection");
        }
    }

    public static Connection getConnection() throws Exception {
        if ("h2".equals(poolType)) {

        } else if ("druid".equals(poolType)) {

        } else if ("none".equals(poolType)) {
            final Connection ret = DriverManager.getConnection(url, userName, password);

            ret.setTransactionIsolation(transactionIsolationInt);
            ret.setAutoCommit(false);

            return ret;
        } else if (RuntimeDatabase.NONE == RuntimeDatabase.valueOf(DBFactory.getProperty("runtimeDatabase"))) {
            return null;
        }

        throw new IllegalStateException("Not found database connection pool [" + poolType + "]");
    }

    private Connections() {
    }
}
