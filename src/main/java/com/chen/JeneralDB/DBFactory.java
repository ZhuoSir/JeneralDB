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
 * @version 1.1.0
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

    private boolean decimalPack = false;

    private String database;

    private DBFactory() {
        try {
            this.database = getProperty("db_name");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (null == directoryPath)
            directoryPath = getProperties().getProperty("packageOutPath");

        if (null == packageName)
            packageName = getProperties().getProperty("packageSimplepath");

        File directory = new File(directoryPath);
        if (!directory.exists()
                && Boolean.valueOf(getProperties().getProperty("autoCreateDir")))
            directory.mkdirs();

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
        String[] columnType  = new String[size];

        utilPack = false; sqlPack = false; decimalPack = false;
        for (int i = 0; i < size; i++) {
            columnNames[i] = resultSet.getColumnName(i + 1);
            columnType[i]  = resultSet.getColumnTypeName(i + 1);

            if ("datetime,timestamp,date,year".contains(columnType[i].toLowerCase()))
                utilPack = true;

            if ("image,text".contains(columnType[i].toLowerCase()))
                sqlPack = true;

            if ("decimal,real,numeric,money,smallmoney".contains(columnType[i].toLowerCase()))
                decimalPack = true;
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

        buffer.append("import com.chen.JeneralDB.annotation.Column;\r\n");
        buffer.append("import com.chen.JeneralDB.annotation.Table;\r\n");

        if (utilPack)
            buffer.append("import java.util.Date;\r\n\r\n");

        if (sqlPack)
            buffer.append("import java.sql.*;\r\n\r\n");

        if (decimalPack)
            buffer.append("import java.math.BigDecimal;\r\n\r\n");

        buffer.append("/**\r\n");
        buffer.append(" * created by JeneralDB at ");
        buffer.append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        buffer.append("\r\n");
        buffer.append(" */");
        buffer.append("\r\n");

        buffer.append("@Table(\"").append(allTableName).append("\")\r\n");
        buffer.append("public class " + formatClassName(allTableName) + " {\r\n");
        buffer.append("\r\n");

        processAllAttrs(buffer, allTableName, columnNames, columnType);

        processAllMethod(buffer, columnNames, columnType);

        processToString(buffer, columnNames, columnType);

        buffer.append("}\r\n");

        return buffer.toString();
    }


    /**
     * 生成toString()方法
     *
     * */
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


    /**
     * 生成所有get set 方法
     *
     * */
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


    /**
     * 生成所有属性
     *
     * */
    private void processAllAttrs(StringBuffer buffer, String tableName, String[] columnNames, String[] columnTypes) {
        for (int i = 0; i < columnNames.length; i++) {
            String indexType = getIndexOfcolumn(columnNames[i], tableName);
            if (null != indexType) {
                buffer.append("\t@Column(value = \"").append(columnNames[i]).append("\"");
                if ("PRIMARY".equals(indexType)) {
                    buffer.append(", index = Column.index.PRIMARYKEY");
                } else if ("UNIQUE".equals(indexType)) {
                    buffer.append(", index = Column.index.UNIQUE");
                } else if ("FULLTEXT".equals(indexType)) {
                    buffer.append(", index = Column.index.FULLTEXT");
                }
            } else {
                buffer.append("\t@Column(\"").append(columnNames[i]).append("\"");
            }

            buffer.append(")\r\n");
            buffer.append("\tprivate ");
            buffer.append(sqlType2JavaType(columnTypes[i]));
            buffer.append(" ");
            buffer.append(columnNames[i]);
            buffer.append(";\r\n");
            buffer.append("\r\n");
        }
    }


    private String getIndexOfcolumn(String columnName, String tableName) {
        String sql = "show index from " + tableName + " where Column_name = ?;";
        try {
            DataTable dt = DBUtil.getInstance().queryDataTable(sql, columnName);
            return !dt.isEmpty() ? dt.getObjectByColumnNameInRow("Key_name", 0).toString() : null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private String sqlType2JavaType(String sqlType) {
        String ret = null;

        switch (sqlType.toLowerCase()) {
            case "bit":
                ret = "Boolean";
                break;
            case "tinyint":
                ret = "byte";
                break;
            case "smallint":
                ret = "short";
                break;
            case "float":
                ret = "float";
                break;
            case "int":
            case "integer":
                ret = "int";
                break;
            case "bigint":
            case "int unsigned":
                ret = "long";
                break;
            case "decimal":
            case "numeric":
            case "real":
            case "money":
            case "smallmoney":
                ret = "BigDecimal";
                break;
            case "varchar":
            case "char":
            case "nvarchar":
            case "nchar":
            case "text":
                ret = "String";
                break;
            case "datetime":
            case "timestamp":
            case "date":
            case "year":
                ret = "Date";
                break;
            case "image":
                ret = "Blod";
                break;
            case "double":
                ret = "double";
                break;
            case "longblob":
                ret = "byte[]";
            default:
                break;
        }

        return ret;
    }


    /**
     * 根据表名格式化生成类名
     *
     * */
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


    /**
     * 将表中名的第一个字符大写
     *
     * */
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
     *
     */
    public String[] getAllTableNamesOfDataBase()
            throws Exception {
        String sql = String.format(allTableNameSql, getProperty("db_name"), getProperty("db_type"));
        Object[] allTableNames = DBUtil.getInstance().queryDataTable(sql).getObjectsByColumnName("TABLE_NAME");
        String[] result = new String[allTableNames.length];

        for (int i = 0; i < allTableNames.length; i++) {
            result[i] = allTableNames[i].toString();
        }

        return result;
    }


    /**　
     * 获取表的所有主键
     *
     * */
    private DataTable getAllPKOfTable(String tableName)
            throws Exception {
        String sql = "show index from " + tableName + " where key_name = 'PRIMARY';";
        return DBUtil.getInstance().queryDataTable(sql);
    }


    /**
     * 获取表中所有主键的名称
     *
     * */
    public String[] getAllPkNamesOfTable(String tableName)
            throws Exception {
        DataTable dt = getAllPKOfTable(tableName);
        Object[] objs = dt.getObjectsByColumnName("Column_name");

        String[] ret = new String[objs.length];
        for (int i = 0; i < objs.length; i++) {
            ret[i] = objs[i].toString();
        }

        return ret;
    }


    /**
     * 获取相应的配置文件信息
     *
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
     *
     */
    public static String getProperty(String key)
            throws IOException {
        return getProperties().getProperty(key);
    }


    /**
     * 将字符串写进java文件，如果没有文件创建之
     *
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
