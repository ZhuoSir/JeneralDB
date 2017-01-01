package com.chen.JeneralDB;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSetMetaData;
import java.util.Properties;

/**
 * 负责根据数据库创建持久层代码的工厂类
 *
 * @author 陈卓
 * @version 1.0.0
 */
public class DBFactory {

    private static Properties properties;

    private static DBFactory dbFactory;

    private static String allTableNameSql = "select table_name from information_schema.tables " +
            "where table_schema='%s' and table_type='%s';";

    private static String allColumnNameSql = "select column_name from information_schema.columns where table_name='%s' " +
            "order by table_schema,table_name";

    private boolean utilPack = false;

    private boolean sqlPack = false;

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
            parseToJava(allTableNames[i], directory.getAbsolutePath());
        }
    }

    /**
     * 生成Java文件的主体类
     */
    private void parseToJava(String allTableName, String directory)
            throws Exception {
        if (null == allTableName || allTableName.isEmpty() || null == directory) {
            throw new NullPointerException();
        }

        StringBuilder sqlBuilder = new StringBuilder("select * from ");
        sqlBuilder.append(allTableName);

        ResultSetMetaData resultSet = DBUtil.getInstance().queryResultSetMetaData(sqlBuilder.toString());

        int size = resultSet.getColumnCount();
        String[] columnNames = new String[size];
        String[] columnType = new String[size];
        int[] columnSize = new int[size];

        for (int i = 0; i < size; i++) {
            columnNames[i] = resultSet.getColumnName(i + 1);
            columnType[i] = resultSet.getColumnTypeName(i + 1);
            columnSize[i] = resultSet.getColumnDisplaySize(i + 1);

            if (columnType[i].equalsIgnoreCase("datetime")
                    || columnType[i].equalsIgnoreCase("timestamp")) {
                utilPack = true;
            }

            if (columnType[i].equalsIgnoreCase("image")
                    || columnType[i].equalsIgnoreCase("text")) {
                sqlPack = true;
            }
        }

        String content = parse(allTableName, columnNames, columnType, columnSize);

        StringBuffer javaFilePath = new StringBuffer(getProperties().getProperty("packageOutPath"));
        javaFilePath.append("\\");
        javaFilePath.append(initCap(allTableName));
        javaFilePath.append(".java");

        writeToJavaFile(content, javaFilePath.toString());
    }

    private String parse(String allTableName, String[] columnNames, String[] columnType, int[] columnSize)
            throws IOException {
        StringBuffer buffer = new StringBuffer();

        buffer.append("package " + getProperties().getProperty("packageSimplepath") + ";");
        buffer.append("\r\n");
        buffer.append("\r\n");

        if (utilPack) {
            buffer.append("import java.util.Date;\r\n");
        }

        if (sqlPack) {
            buffer.append("import java.sql.*;\r\n");
        }

        buffer.append("\r\n\r\npublic class " + initCap(allTableName) + " {\r\n");
        buffer.append("\r\n");
        processAllAttrs(buffer, columnNames, columnType);
        processAllMethod(buffer, columnNames, columnType);
        buffer.append("}\r\n");

        return buffer.toString();
    }

    private void processAllMethod(StringBuffer buffer, String[] colnames, String[] colTypes) {
        for (int i = 0; i < colnames.length; i++) {
            buffer.append("\tpublic void set" + initCap(colnames[i]) + "(" + sqlType2JavaType(colTypes[i]) + " " +
                    colnames[i] + "){\r\n");
            buffer.append("\tthis." + colnames[i] + " = " + colnames[i] + ";\r\n");
            buffer.append("\t}\r\n");
            buffer.append("\r\n");
            buffer.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + initCap(colnames[i]) + "(){\r\n");
            buffer.append("\t\treturn " + colnames[i] + ";\r\n");
            buffer.append("\t}\r\n");
            buffer.append("\r\n");
        }
    }

    private void processAllAttrs(StringBuffer buffer, String[] columnNames, String[] columnTypes) {
        for (int i = 0; i < columnNames.length; i++) {
            buffer.append("\tprivate ");
            buffer.append(sqlType2JavaType(columnTypes[i]));
            buffer.append(" ");
            buffer.append(columnNames[i]);
            buffer.append(";\r\n");
            buffer.append("\r\n");
        }
    }

    private String sqlType2JavaType(String sqlType) {
        if (sqlType.equalsIgnoreCase("bit")) {
            return "boolean";
        } else if (sqlType.equalsIgnoreCase("tinyint")) {
            return "byte";
        } else if (sqlType.equalsIgnoreCase("smallint")) {
            return "short";
        } else if (sqlType.equalsIgnoreCase("int") || sqlType.equalsIgnoreCase("integer")) {
            return "int";
        } else if (sqlType.equalsIgnoreCase("bigint")) {
            return "long";
        } else if (sqlType.equalsIgnoreCase("float")) {
            return "float";
        } else if (sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric")
                || sqlType.equalsIgnoreCase("real") || sqlType.equalsIgnoreCase("money")
                || sqlType.equalsIgnoreCase("smallmoney")) {
            return "double";
        } else if (sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char")
                || sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar")
                || sqlType.equalsIgnoreCase("text")) {
            return "String";
        } else if (sqlType.equalsIgnoreCase("datetime") || sqlType.equalsIgnoreCase("timestamp")) {
            return "Date";
        } else if (sqlType.equalsIgnoreCase("image")) {
            return "Blod";
        }
        return null;
    }

    private String initCap(String allTableName) {
        if (null == allTableName || allTableName.isEmpty()) {
            return allTableName;
        }

        char[] ch = allTableName.toCharArray();

        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }

        return new String(ch);
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

    /**
     * 将字符串写进java文件，如果没有文件创建之
     */
    private void writeToJavaFile(String content, String directory)
            throws IOException {
        if (null == content || content.isEmpty()
                || null == directory || directory.isEmpty()) {
            throw new NullPointerException();
        }

        File javaFile = new File(directory);
        if (!javaFile.exists()) {
            javaFile.createNewFile();
        }

        PrintWriter printWriter = new PrintWriter(javaFile);
        printWriter.write(content);
        printWriter.flush();
        printWriter.close();
    }

    public static void main(String[] args) {
        try {
            DBFactory.getInstance().GenEntityFromDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
