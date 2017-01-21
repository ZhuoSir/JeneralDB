package com.chen.JeneralDB.repository;

import com.chen.JeneralDB.jdbc.Query;
import com.chen.JeneralDB.exception.RespositoryException;
import com.chen.JeneralDB.transaction.Transaction;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sunny on 2017/1/15.
 */
public interface Repository<T> {

    int add(final T t) throws Exception;

    void update(final String id, T t) throws RespositoryException;

    void delete(final String id) throws RespositoryException;

    T get(final String id) throws RespositoryException;

    Map<String, T> get(final Iterator<String> ids) throws RespositoryException;

    boolean has(final String id) throws RespositoryException;

    T get(Query query) throws RespositoryException;

    List<T> select(final String statemnt, final Object... params) throws RespositoryException;

    List<T> getRandomly(final int fetchSize) throws RespositoryException;

    long count() throws RespositoryException;

    long count(Query query) throws RespositoryException;

    boolean isWriteAble();

    Transaction beginTransaction();

    boolean hasTransactionBegun();
}
