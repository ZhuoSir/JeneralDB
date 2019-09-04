package com.chen.JeneralDB;

public class DBProperty {

    private String runtimeDatabase = "MYSQL";

    private String dbUrl;

    private String dbDriver;

    private String dbUserName;

    private String dbPassWord;

    private String dbName;

    private String dbType = "base table";

    private int dbTransactionLevel = 8;

    private String dbPoolType = "none";

    private int dbMinConnCount = 3;

    private int dbMaxConnCount = 8;

    private String packageOutPath;

    private String packageSimplepath;

    private boolean autoCreateDir;

    public String getRuntimeDatabase() {
        return runtimeDatabase;
    }

    public void setRuntimeDatabase(String runtimeDatabase) {
        this.runtimeDatabase = runtimeDatabase;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public void setDbUserName(String dbUserName) {
        this.dbUserName = dbUserName;
    }

    public String getDbPassWord() {
        return dbPassWord;
    }

    public void setDbPassWord(String dbPassWord) {
        this.dbPassWord = dbPassWord;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public int getDbTransactionLevel() {
        return dbTransactionLevel;
    }

    public void setDbTransactionLevel(int dbTransactionLevel) {
        this.dbTransactionLevel = dbTransactionLevel;
    }

    public String getDbPoolType() {
        return dbPoolType;
    }

    public void setDbPoolType(String dbPoolType) {
        this.dbPoolType = dbPoolType;
    }

    public int getDbMinConnCount() {
        return dbMinConnCount;
    }

    public void setDbMinConnCount(int dbMinConnCount) {
        this.dbMinConnCount = dbMinConnCount;
    }

    public int getDbMaxConnCount() {
        return dbMaxConnCount;
    }

    public void setDbMaxConnCount(int dbMaxConnCount) {
        this.dbMaxConnCount = dbMaxConnCount;
    }

    public String getPackageOutPath() {
        return packageOutPath;
    }

    public void setPackageOutPath(String packageOutPath) {
        this.packageOutPath = packageOutPath;
    }

    public String getPackageSimplepath() {
        return packageSimplepath;
    }

    public void setPackageSimplepath(String packageSimplepath) {
        this.packageSimplepath = packageSimplepath;
    }

    public boolean isAutoCreateDir() {
        return autoCreateDir;
    }

    public void setAutoCreateDir(boolean autoCreateDir) {
        this.autoCreateDir = autoCreateDir;
    }
}
