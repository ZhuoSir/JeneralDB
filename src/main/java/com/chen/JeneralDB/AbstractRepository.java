package com.chen.JeneralDB;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sunny on 2017/1/15.
 */
public abstract class AbstractRepository<T> implements Repository<T> {

    private Repository repository;

    public AbstractRepository(String name) {

    }

    @Override
    public String add(T t) {
        return null;
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
}
