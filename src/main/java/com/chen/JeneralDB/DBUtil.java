package com.chen.JeneralDB;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by sunny-chen on 16/11/27.
 */
public class DBUtil {

    private static Connection conn = null;

    public static Connection openConnection()
            throws Exception {
        if (null == conn || conn.isClosed()) {
            Properties p = new Properties();
            p.load(DBUtil.class.getResourceAsStream("/JeneralDB-config.properties"));
            Class.forName(p.getProperty("db_driver"));
            conn = DriverManager.getConnection(p.getProperty("db_url"), p.getProperty("db_username"),
                    p.getProperty("db_password"));
        }
        return conn;
    }

    public static void closeConnection() throws SQLException {
        try {
            if (null != conn) {
                conn.close();
            }
        } finally {
            conn = null;
            System.gc();
        }
    }

    public static List<Map<String, Object>> queryMapList(String sql)
            throws Exception {
        return queryMapList(null, sql);
    }

    public static List<Map<String, Object>> queryMapList(Connection connection, String sql)
            throws Exception {
        if (null == connection) {
            connection = openConnection();
        }
        List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            genDataFromResultSet(rs, lists);
        } finally {
            if (null != rs)
                rs.close();
            if (null != stmt)
                stmt.close();
            if (null != connection)
                connection.close();
        }
        return lists;
    }

    public static List<Map<String, Object>> queryMapList(String sql, Object... params)
            throws Exception {
        return queryMapList(null, sql, params);
    }

    public static List<Map<String, Object>> queryMapList(Connection connection, String sql, Object... params)
            throws Exception {
        if (null == connection) {
            connection = openConnection();
        }
        List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            rs = statement.getResultSet();
            genDataFromResultSet(rs, lists);
        } finally {
            if (null != rs)
                rs.close();
            if (null != statement)
                statement.close();
            if (null != connection)
                connection.close();
        }
        return lists;
    }

    private static void genDataFromResultSet(ResultSet rs, List<Map<String, Object>> lists)
            throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (null != rs && rs.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < columnCount; i++) {
                String name = metaData.getColumnName(i + 1);
                Object value = rs.getObject(name);
                map.put(name, value);
            }
            lists.add(map);
        }
    }

    public static <T> List<T> queryBeanList(String sql, Class<T> beanClass)
            throws Exception {
        return queryBeanList(null, sql, beanClass);
    }

    public static <T> List<T> queryBeanList(Connection con, String sql, Class<T> beanClass)
            throws Exception {
        if (null == con) {
            con = openConnection();
        }
        List<T> lists = new ArrayList<T>();
        Statement stmt = null;
        ResultSet resultSet = null;
        Field[] fields = null;
        try {
            stmt = con.createStatement();
            resultSet = stmt.executeQuery(sql);
            fields = beanClass.getDeclaredFields();
            for (Field field : fields)
                field.setAccessible(true);
            while (null != resultSet && resultSet.next()) {
                T t = beanClass.newInstance();
                for (Field field : fields) {
                    String name = field.getName();
                    try {
                        Object value = resultSet.getObject(name);
                        setValue(t, field, value);
                    } finally {
                        continue;
                    }
                }
                lists.add(t);
            }
        } finally {
            if (null != resultSet)
                resultSet.close();
            if (null != stmt)
                stmt.close();
            if (null != con)
                con.close();
        }
        return lists;
    }

    public static <T> List<T> queryBeanList(String sql, Class<T> beanClass, Object... params)
            throws Exception {
        return queryBeanList(null, sql, beanClass, params);
    }

    public static <T> List<T> queryBeanList(Connection con, String sql, Class<T> beanClass, Object... params)
            throws Exception {
        if (null == con) {
            con = openConnection();
        }
        List<T> lists = new ArrayList<T>();
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        Field[] fields = null;
        try {
            preStmt = con.prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                preStmt.setObject(i + 1, params[i]);// 下标从1开始
            rs = preStmt.executeQuery();
            fields = beanClass.getDeclaredFields();
            for (Field f : fields)
                f.setAccessible(true);
            while (null != rs && rs.next()) {
                T t = beanClass.newInstance();
                for (Field f : fields) {
                    String name = f.getName();
                    try {
                        Object value = rs.getObject(name);
                        setValue(t, f, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        continue;
                    }
                }
                lists.add(t);
            }
        } finally {
            if (null != rs)
                rs.close();
            if (null != preStmt)
                preStmt.close();
            if (null != con)
                con.close();
        }
        return lists;
    }

    public static <T> T queryBean(String sql, Class<T> beanClass)
            throws Exception {
        List<T> lists = queryBeanList(null, sql, beanClass);
        if (lists.size() != 1)
            throw new SQLException("SqlError：期待一行返回值，却返回了太多行！");
        return lists.get(0);
    }

    public static <T> T queryBean(String sql, Class<T> beanClass, Object... params)
            throws Exception {
        List<T> lists = queryBeanList(null, sql, beanClass, params);
        if (lists.size() != 1)
            throw new SQLException("SqlError：期待一行返回值，却返回了太多行！");
        return lists.get(0);
    }

    public static <T> DataTable queryDataTable(String sql, Class<T> beanClass)
            throws Exception {
        List<T> list = queryBeanList(sql, beanClass);
        DataTable dataTable = null;
        if (list != null && list.size() != 0) {
            dataTable = new DataTable(list);
        }
        return dataTable;
    }

    public static int execute(String sql)
            throws Exception {
        Statement statement = null;
        try {
            statement = openConnection().createStatement();
            return statement.executeUpdate(sql);
        } finally {
            if (null != statement)
                statement.close();
            closeConnection();
        }
    }

    public static int execute(String sql, Object... params)
            throws Exception {
        PreparedStatement preStmt = null;
        try {
            preStmt = openConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                preStmt.setObject(i + 1, params[i]);// 下标从1开始
            return preStmt.executeUpdate();
        } finally {
            if (null != preStmt)
                preStmt.close();
            closeConnection();
        }
    }

    public static int[] executeAsBatch(List<String> sqlList)
            throws Exception {
        return executeAsBatch(null, sqlList.toArray(new String[]{}));
    }

    public static int[] executeAsBatch(Connection con, String[] sqlArray) throws Exception {
        if (null == con) {
            con = openConnection();
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            for (String sql : sqlArray) {
                stmt.addBatch(sql);
            }
            return stmt.executeBatch();
        } finally {
            if (null != stmt) {
                stmt.close();
            }
            if (null != con) {
                con.close();
            }
        }
    }

    public static int[] executeAsBatch(String sql, Object[][] params)
            throws Exception {
        PreparedStatement preStmt = null;
        try {
            preStmt = openConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                Object[] rowParams = params[i];
                for (int k = 0; k < rowParams.length; k++) {
                    Object obj = rowParams[k];
                    preStmt.setObject(k + 1, obj);
                }
                preStmt.addBatch();
            }
            return preStmt.executeBatch();
        } finally {
            if (null != preStmt) {
                preStmt.close();
            }
            closeConnection();
        }
    }

    public static int save(Object obj) throws Exception {
        if (obj == null) {
            throw new NullPointerException("保存对象不能为Null");
        }
        StringBuilder columns = new StringBuilder(" insert into ");
        StringBuilder values = new StringBuilder(" ) values (");
        Class<?> t = obj.getClass();
        String tableName = getTableName(t);
        Field[] fields = t.getDeclaredFields();
        columns.append(tableName + " ( ");
        int size = fields.length, columnNum = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            String columnName = field.getName();
            Object value = field.get(obj);
            columns.append(columnName);
            values.append("'" + value.toString() + "'");
            if (columnNum++ < size - 1) {
                columns.append(" , ");
                values.append(" , ");
            }
        }
        values.append(" ) ");
        columns.append(values);
        return DBUtil.execute(columns.toString());
    }

    private static String getTableName(Class<?> t) {
        String name = t.getName();
        int index = name.lastIndexOf(".");
        return name.substring(index + 1);
    }

    private static <T> void setValue(T t, Field f, Object value)
            throws Exception {
        if (null == value)
            return;
        String v = value.toString();
        String n = f.getType().getName();
        if ("java.lang.Byte".equals(n) || "byte".equals(n)) {
            f.set(t, Byte.parseByte(v));
        } else if ("java.lang.Short".equals(n) || "short".equals(n)) {
            f.set(t, Short.parseShort(v));
        } else if ("java.lang.Integer".equals(n) || "int".equals(n)) {
            f.set(t, Integer.parseInt(v));
        } else if ("java.lang.Long".equals(n) || "long".equals(n)) {
            f.set(t, Long.parseLong(v));
        } else if ("java.lang.Float".equals(n) || "float".equals(n)) {
            f.set(t, Float.parseFloat(v));
        } else if ("java.lang.Double".equals(n) || "double".equals(n)) {
            f.set(t, Double.parseDouble(v));
        } else if ("java.lang.String".equals(n)) {
            f.set(t, value.toString());
        } else if ("java.lang.Character".equals(n) || "char".equals(n)) {
            f.set(t, (Character) value);
        } else if ("java.lang.Date".equals(n)) {
            f.set(t, new Date(((java.sql.Date) value).getTime()));
        } else if ("java.lang.Timer".equals(n)) {
            f.set(t, new Time(((java.sql.Time) value).getTime()));
        } else if ("java.sql.Timestamp".equals(n)) {
            f.set(t, (java.sql.Timestamp) value);
        } else {
            throw new Exception("SqlError：暂时不支持此数据类型，请使用其他类型代替此类型！");
        }
    }
}
