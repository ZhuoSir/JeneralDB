package com.chen.JeneralDB.repository;

import com.chen.JeneralDB.jdbc.Connections;
import com.chen.JeneralDB.jdbc.Query;
import com.chen.JeneralDB.transaction.JdbcTransaction;
import com.chen.JeneralDB.transaction.Transaction;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sunny on 2017/1/15.
 */
public final class JdbcRepository<T> implements Repository<T> {

    private boolean writeAble = true;

    public void setWriteAble(boolean writeAble) {
        this.writeAble = writeAble;
    }

    private Transaction transaction;

    @Override
    public int add(T t) throws Exception {
        transaction = beginTransaction();

        final Connection connection = Connections.getConnection();

        return 0;
    }

    @Override
    public void update(String id, T t) {

    }

    @Override
    public void delete(String id) {

    }

    @Override
    public T get(String id) {
        return null;
    }

    @Override
    public Map<String, T> get(Iterator<String> ids) {
        return null;
    }

    @Override
    public boolean has(String id) {
        return false;
    }

    @Override
    public T get(Query query) {
        return null;
    }

    @Override
    public List<T> select(String statemnt, Object... params) {
        return null;
    }

    @Override
    public List<T> getRandomly(int fetchSize) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public long count(Query query) {
        return 0;
    }

    @Override
    public boolean isWriteAble() {
        return writeAble;
    }

    @Override
    public Transaction beginTransaction() {
        try {
            transaction = new JdbcTransaction();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JDBC transaction", e);
        }

        return transaction;
    }

    @Override
    public boolean hasTransactionBegun() {
        return null != transaction;
    }
}
