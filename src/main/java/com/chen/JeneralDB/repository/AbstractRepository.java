package com.chen.JeneralDB.repository;

import com.chen.JeneralDB.*;
import com.chen.JeneralDB.common.RuntimeDatabase;
import com.chen.JeneralDB.exception.RespositoryException;
import com.chen.JeneralDB.jdbc.Query;
import com.chen.JeneralDB.transaction.Transaction;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sunny on 2017/1/15.
 */
public abstract class AbstractRepository<T> implements Repository<T> {

    protected String name;

    public String getName() {
        return name;
    }

    private Repository repository;

    public Repository getRepository() {
        return repository;
    }

    public AbstractRepository(String name) {
        this.name = name;

        try {
            final RuntimeDatabase runtimeDatabase = RuntimeDatabase
                    .valueOf(DBFactory.getProperties().getProperty("runtimeDatabase"));

            switch (runtimeDatabase) {
                case MYSQL:
                case H2:
                case MSSQL:
                    repository = new JdbcRepository();

                    break;
                default:
                    throw new RespositoryException("The runtime database[" + runtimeDatabase + "] is not support NOW!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int add(T t) throws Exception {
        if (!isWriteAble()) {
            throw new RespositoryException("The repository[name = " + getName() + "] is not writeable at present");
        }
        return repository.add(t);
    }

    @Override
    public void update(String id, T t) throws RespositoryException {
        if (!isWriteAble()) {
            throw new RespositoryException("The repository[name = " + getName() + "] is not writeable at present");
        }

        repository.update(id, t);
    }

    @Override
    public void delete(String id) throws RespositoryException {
        if (!isWriteAble()) {
            throw new RespositoryException("The repository[name = " + getName() + "] is not writeable at present");
        }

        repository.delete(id);
    }

    @Override
    public T get(String id) throws RespositoryException {
        return (T) repository.get(id);
    }

    @Override
    public Map<String, T> get(Iterator<String> ids) throws RespositoryException {
        return repository.get(ids);
    }

    @Override
    public boolean has(String id) throws RespositoryException {
        return repository.has(id);
    }

    @Override
    public T get(Query query) throws RespositoryException {
        return (T) repository.get(query);
    }

    @Override
    public List<T> select(String statemnt, Object... params) throws RespositoryException {
        return repository.select(statemnt, params);
    }

    @Override
    public List<T> getRandomly(int fetchSize) throws RespositoryException {
        return repository.getRandomly(fetchSize);
    }

    @Override
    public long count() throws RespositoryException {
        return repository.count();
    }

    @Override
    public long count(Query query) throws RespositoryException {
        return repository.count(query);
    }

    @Override
    public Transaction beginTransaction() {
        return repository.beginTransaction();
    }

    @Override
    public boolean hasTransactionBegun() {
        return repository.hasTransactionBegun();
    }
}
