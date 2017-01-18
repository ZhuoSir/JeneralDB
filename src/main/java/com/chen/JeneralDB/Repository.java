package com.chen.JeneralDB;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sunny on 2017/1/15.
 */
public interface Repository<T> {

    String add(final T t);

    void update(final String id, T t);

    void delete(final String id);

    T get(final String id);

    Map<String, T> get(final Iterator<String> ids);

    boolean has(final String id);

    T get(Query query);

    List<T> select(final String statemnt, final Object... params);

    List<T> getRandomly(final int fetchSize);

    long count();

    long count(Query query);


}
