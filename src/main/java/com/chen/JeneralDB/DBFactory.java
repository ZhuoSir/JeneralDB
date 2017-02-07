package com.chen.JeneralDB;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    private static final String allTableNameSql = "select TABLE_NAME from information_schema.tables " +
            "where table_schema='%s' and table_type='%s';";

    private static final String allColumnNameSql = "select column_name from information_schema.columns where table_name='%s' " +
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
    public void createEntityFromDataBase()
            throws Exception {
        List<String> allTableNames = Arrays.asList(getAllTableNamesOfDataBase());

        createEntitysByTableNames(allTableNames);
    }


    /**
     * 指定表生成实体类
     *
     * @param tableNames 表名
     */
    public void createEntitysByTableNames(List<String> tableNames)
            throws Exception {
        createEntitysByTableNames(tableNames, null, null);
    }


    /**
     * 指定表生成实体类到指定文件夹
     *
     * @param tableNames    表名
     * @param directoryPath 文件夹名
     */
    public void createEntitysByTableNames(List<String> tableNames, String directoryPath, String packageName)
            throws Exception {
        if (null == directoryPath) {
            directoryPath = getProperties().getProperty("packageOutPath");
        }

        if (null == packageName) {
            packageName = getProperties().getProperty("packageSimplepath");
        }

        File directory = new File(directoryPath);
        if (!directory.exists()
                && Boolean.valueOf(getProperties().getProperty("autoCreateDir"))) {
            directory.mkdirs();
        }

        for (String tableName : tableNames) {
            parseToJava(tableName, directory.getAbsolutePath(), packageName);
        }
    }


    /**
     * 生成Java文件的主体类
     */
    private void parseToJava(String allTableName, String directory, String packageName)
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

        String content = parse(allTableName, packageName, columnNames, columnType);

        StringBuffer javaFilePath = new StringBuffer(directory);
        javaFilePath.append(File.separator);
        javaFilePath.append(formatClassName(allTableName));
        javaFilePath.append(".java");

        writeToJavaFile(content, javaFilePath.toString());
    }


    private String parse(String allTableName, String packageName, String[] columnNames, String[] columnType)
            throws IOException {
        StringBuffer buffer = new StringBuffer();

        buffer.append("package " + packageName + ";");
        buffer.append("\r\n");
        buffer.append("\r\n");

        if (utilPack) {
            buffer.append("import java.util.Date;\r\n\r\n");
        }

        if (sqlPack) {
            buffer.append("import java.sql.*;\r\n\r\n");
        }

        buffer.append("/**\r\n");
        buffer.append(" * created by JeneralDB at ");
        buffer.append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        buffer.append("\r\n");
        buffer.append(" */");
        buffer.append("\r\n");

        buffer.append("public class " + formatClassName(allTableName) + " {\r\n");
        buffer.append("\r\n");

        processAllAttrs(buffer, columnNames, columnType);

        processAllMethod(buffer, columnNames, columnType);

        processToString(buffer, columnNames, columnType);

        buffer.append("}\r\n");

        return buffer.toString();
    }


    private void processToString(StringBuffer buffer, String[] columnNames, String[] columnType) {
        buffer.append("\tpublic String toString() {\n");
        buffer.append("\t\tStringBuffer string = new StringBuffer();\n");

        for (int i = 0; i < columnNames.length; i++) {
            buffer.append("\t\tstring.append(\"");
            buffer.append(columnNames[i]);
            buffer.append(" = \");\n");
            buffer.append("\t\tstring.append(");
            buffer.append("this.");
            buffer.append(columnNames[i]);
            buffer.append(");\n");
            buffer.append("\t\tstring.append(");
            buffer.append("\";\"");
            buffer.append(");\n");
        }

        buffer.append("\t\treturn string.toString();\n");
        buffer.append("\t}\n");
    }


    private void processAllMethod(StringBuffer buffer, String[] colnames, String[] colTypes) {
        for (int i = 0; i < colnames.length; i++) {
            buffer.append("\tpublic void set" + initCap(colnames[i]) + "(" + sqlType2JavaType(colTypes[i]) + " " +
                    colnames[i] + ") {\r\n");
            buffer.append("\t\tthis." + colnames[i] + " = " + colnames[i] + ";\r\n");
            buffer.append("\t}\r\n");
            buffer.append("\r\n");
            buffer.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + initCap(colnames[i]) + "() {\r\n");
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
            return "Boolean";
        } else if (sqlType.equalsIgnoreCase("tinyint")) {
            return "byte";
        } else if (sqlType.equalsIgnoreCase("smallint")) {
            return "short";
        } else if (sqlType.equalsIgnoreCase("int") || sqlType.equalsIgnoreCase("integer")) {
            return "int";
        } else if (sqlType.equalsIgnoreCase("bigint") || sqlType.equalsIgnoreCase("int unsigned")) {
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
        } else if (sqlType.equalsIgnoreCase("double")) {
            return "double";
        } else if (sqlType.equalsIgnoreCase("longblob")) {
            return "byte[]";
        }
        return null;
    }


    private String formatClassName(String allTableName) {
        if (null == allTableName || allTableName.isEmpty()) {
            return allTableName;
        }

        allTableName = initCap(allTableName);
        if (allTableName.contains("_")) {
            char[] array = allTableName.toCharArray();

            for (int i = 0; i < array.length; i++) {
                if (array[i] == '_') {
                    array[i + 1] = Character.toUpperCase(array[i + 1]);
                }
            }

            allTableName = new String(array);
            allTableName = allTableName.replaceAll("[_]", "");
        }

        return allTableName;
    }




    private String initCap(String allTableName) {
        if (null == allTableName || allTableName.isEmpty()) {
            return allTableName;
        }

        char[] ch = allTableName.toCharArray();
        if (Character.isLowerCase(ch[0])) {
            ch[0] = Character.toUpperCase(ch[0]);
        }

        return new String(ch);
    }


    /**
     * 获取所有数据库中的所有表名
     */
    public String[] getAllTableNamesOfDataBase()
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


    public DataTable getAllPKOfTable(String tableName)
            throws Exception {
        Properties p = getProperties();
        String sql = " SELECT\n" +
                "  t.TABLE_NAME,\n" +
                "  t.CONSTRAINT_TYPE,\n" +
                "  c.COLUMN_NAME,\n" +
                "  c.ORDINAL_POSITION\n" +
                " FROM\n" +
                "  INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS t,\n" +
                "  INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS c\n" +
                " WHERE\n" +
                "  t.TABLE_NAME = c.TABLE_NAME\n" +
                "  AND t.TABLE_SCHEMA = '" + p.getProperty("db_name") + "'\n" +
                "  AND t.CONSTRAINT_TYPE = 'PRIMARY KEY'" +
                "  AND t.TABLE_NAME = '" + tableName + "'";

        return DBUtil.getInstance().queryDataTable(sql);
    }


    public String[] getAllPkNamesOfTable(String tableName)
            throws Exception {
        DataTable dt = getAllPKOfTable(tableName);
        Object[] objs = dt.getObjectsByColumnName("COLUMN_NAME");

        String[] ret = new String[objs.length];
        for (int i = 0; i < objs.length; i++) {
            ret[i] = objs[i].toString();
        }

        return ret;
    }


    /**
     * 获取相应的配置文件信息
     */
    public static Properties getProperties()
            throws IOException {
        if (null == properties) {
            properties = new Properties();
            properties.load(DBFactory.class.getResourceAsStream("/JeneralDB-config.properties"));
        }

        return properties;
    }


    /**
     * 获取配置文件中的基本属性
     */
    public static String getProperty(String key)
            throws IOException {
        return getProperties().getProperty(key);
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
        DBUtil.print("成功创建实体类" + javaFile.getPath());
    }
}
