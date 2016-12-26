package com.chen.JeneralDB;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by sunny-chen on 16/12/26.
 */
public class DBFactory {

    private static Properties properties;

    private static DBFactory dbFactory;

    private static String allTableNameSql = "select table_name from information_schema.tables " +
            "where table_schema='%s' and table_type='%s';";

    private static String allColumnNameSql = "select column_name from information_schema.columns where table_name='%s' " +
            "order by table_schema,table_name";

    private DBFactory() {
    }

    public static synchronized DBFactory getInstance() {
        if (null == dbFactory) {
            dbFactory = new DBFactory();
        }
        return dbFactory;
    }

    /**
     * 根据数据库生成实体类
     */
    public void GenEntityFromDataBase()
            throws Exception {
        File directory = new File(getProperties().getProperty("packageOutPath"));
        if (!directory.exists()
                && Boolean.valueOf(getProperties().getProperty("autoCreateDir"))) {
            directory.mkdirs();
        }
        String[] allTableNames = getAllTableNamesOfDataBase();
        for (int i = 0; i < allTableNames.length; i++) {
            parseToJava(allTableNames[i], directory);
        }
    }

    /**
     * 生成Java文件的主体类
     */
    private void parseToJava(String allTableName, File directory) {

    }

    /**
     * 获取所有数据库中的所有表名
     */
    private String[] getAllTableNamesOfDataBase()
            throws Exception {
        Properties p = getProperties();
        String sql = String.format(allTableNameSql, p.getProperty("db_name"), p.getProperty("db_type"));
        Object[] allTableNames = DBUtil.getInstance().queryDataTable(sql).getObjectsByColumnName("TABLE_NAME");
        String[] result = new String[allTableNames.length];
        for (int i = 0; i < allTableNames.length; i++) {
            result[i] = allTableNames[i].toString();
        }
        return result;
    }

    /**
     * 获取表中的所有列名
     */
    private String[] getAllColumnNamesOfTable(String tableName)
            throws Exception {
        String sql = String.format(allColumnNameSql, tableName);
        Object[] allColumnNames = DBUtil.getInstance().queryDataTable(sql).getObjectsByColumnName("COLUMN_NAME");
        String[] result = new String[allColumnNames.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = allColumnNames[i].toString();
        }
        return result;
    }

    /**
     * 获取相应的配置文件信息
     */
    private Properties getProperties()
            throws IOException {
        if (null == properties) {
            properties = new Properties();
            properties.load(DBFactory.class.getResourceAsStream("/JeneralDB-config.properties"));
        }
        return properties;
    }

    public static void main(String[] args) {
        try {
            DBFactory.getInstance().GenEntityFromDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
