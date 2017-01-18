package com.chen.JeneralDB.transaction;

/**
 * Created by sunny-chen on 17/1/18.
 */
public interface Transaction {

    String getID();

    void commit();

    void rollback();

    boolean isActive();
}
