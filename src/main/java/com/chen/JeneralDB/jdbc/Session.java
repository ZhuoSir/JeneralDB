package com.chen.JeneralDB.jdbc;

import com.chen.JeneralDB.dialect.Dialect;
import com.chen.JeneralDB.dialect.DialectFactory;
import com.chen.JeneralDB.repository.Repository;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 *
 * Created by sunny on 2017/4/25.
 */
public class Session implements Closeable {

    private Connection conn;

    private Dialect dialect;

    private Boolean isSupportTransaction;

    public void setSupportTransaction(Boolean supportTransaction) {isSupportTransaction = supportTransaction;}

    public Session(DataSource dataSource) {
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.dialect = DialectFactory.newDialect(conn);
    }

    public Session(Connection connection) {
        this.conn = connection;
        this.dialect = DialectFactory.newDialect(conn);
    }


    /**
     * 开始事务
     *
     * @throws SQLException
     * */
    public void beginTransaction() throws SQLException {
        if (null == isSupportTransaction) {
            isSupportTransaction = conn.getMetaData().supportsTransactions();
        } else if (!isSupportTransaction){
            throw new SQLException("Transaction not supported for current database!");
        }

        conn.setAutoCommit(isSupportTransaction);
    }


    /**
     * 提交事务
     *
     * @throws SQLException
     * */
    public void commit() throws SQLException {
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn.setAutoCommit(true);
        }
    }


    /**
     * 回滚事务
     *
     * @throws SQLException
     * */
    public void rollback() throws SQLException {
        try {
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn.setAutoCommit(true);
        }
    }


    /**
     * 回滚事务到某个保存点
     *
     * @param savepoint 保存点
     * @throws SQLException
     * */
    public void rollback(Savepoint savepoint) throws SQLException {
        try {
            conn.rollback(savepoint);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn.setAutoCommit(true);
        }
    }


    /**
     * 设置保存点
     *
     * @param name 保存点名称
     * @throws SQLException
     * */
    public Savepoint setSavePoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }


    /**
     * 设置事务的隔离级别<br>
     *
     * Connection.TRANSACTION_NONE               驱动不支持事务<br>
     * Connection.TRANSACTION_READ_UNCOMMITTED   允许脏读、不可重复读和幻读<br>
     * Connection.TRANSACTION_READ_COMMITTED     禁止脏读，但允许不可重复读和幻读<br>
     * Connection.TRANSACTION_REPEATABLE_READ    禁止脏读和不可重复读，单运行幻读<br>
     * Connection.TRANSACTION_SERIALIZABLE       禁止脏读、不可重复读和幻读<br>
     *
     * @param level 隔离级别
     * @throws SQLException
     */
    public void setTransactionIsolation(int level) throws SQLException {
        if (conn.getMetaData().supportsTransactionIsolationLevel(level)) {
            throw new SQLException(String.format("Transaction isolation %d not support!", level));
        }

        conn.setTransactionIsolation(level);
    }


    /**
     * 关闭连接
     *
     * @throws IOException
     * */
    @Override
    public void close() throws IOException {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
