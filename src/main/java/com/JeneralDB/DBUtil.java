package com.JeneralDB;

import bean.Person;

import java.io.IOException;
import java.lang.reflect.Constructor;
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
            throws SQLException, IOException, ClassNotFoundException {
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

    public static List<Map<String, Object>> queryMapList(Connection connection, String sql)
            throws SQLException {
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
        }
        return lists;
    }

    public static List<Map<String, Object>> queryMapList(Connection connection, String sql, Object... params)
            throws SQLException {
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

    public static <T> List<T> queryBeanList(Connection con, String sql, Class<T> beanClass)
            throws SQLException, IllegalAccessException, InstantiationException {
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
        }
        return lists;
    }

    public static <T> List<T> queryBeanList(Connection con, String sql, Class<T> beanClass, Object... params)
            throws SQLException, InstantiationException, IllegalAccessException {
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
        }
        return lists;
    }

    public static <T> T queryBean(Connection con, String sql, Class<T> beanClass) throws SQLException,
            InstantiationException, IllegalAccessException {
        List<T> lists = queryBeanList(con, sql, beanClass);
        if (lists.size() != 1)
            throw new SQLException("SqlError：期待一行返回值，却返回了太多行！");
        return lists.get(0);
    }

    public static <T> T queryBean(Connection con, String sql, Class<T> beanClass, Object... params)
            throws SQLException, InstantiationException, IllegalAccessException {
        List<T> lists = queryBeanList(con, sql, beanClass, params);
        if (lists.size() != 1)
            throw new SQLException("SqlError：期待一行返回值，却返回了太多行！");
        return lists.get(0);
    }

    public static int execute(Connection con, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = con.createStatement();
            return statement.executeUpdate(sql);
        } finally {
            if (null != statement)
                statement.close();
        }
    }

    public static int execute(Connection con, String sql, Object... params) throws SQLException {
        PreparedStatement preStmt = null;
        try {
            preStmt = con.prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                preStmt.setObject(i + 1, params[i]);// 下标从1开始
            return preStmt.executeUpdate();
        } finally {
            if (null != preStmt)
                preStmt.close();
        }
    }

    public static int[] executeAsBatch(Connection con, List<String> sqlList) throws SQLException {
        return executeAsBatch(con, sqlList.toArray(new String[] {}));
    }

    public static int[] executeAsBatch(Connection con, String[] sqlArray) throws SQLException {
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
        }
    }

    public static int[] executeAsBatch(Connection con, String sql, Object[][] params) throws SQLException {
        PreparedStatement preStmt = null;
        try {
            preStmt = con.prepareStatement(sql);
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
        }
    }

//    public static <T> List<T> queryObjectList(Connection con, String sql, Class<T> objClass) throws SQLException {
//        List<T> lists = new ArrayList<T>();
//        Statement statement = null;
//        ResultSet resultSet = null;
//        try {
//            statement = con.createStatement();
//            resultSet = statement.executeQuery(sql);
//            label : while (null != resultSet && resultSet.next()) {
//                Constructor<?>[] constor = objClass.getConstructors();
//                for (Constructor<?> c : constor) {
//                    Object value = resultSet.getObject(1);
//                    try {
//                        lists.add((T) c.newInstance(value));
//                        continue  label;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } finally {
//            if (null != resultSet)
//                resultSet.close();
//            if (null != statement)
//                statement.close();
//        }
//        return lists;
//    }

    private static <T> void setValue(T t, Field f, Object value) throws IllegalAccessException {
        // TODO 以数据库类型为准绳，还是以java数据类型为准绳？还是混合两种方式？
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
            System.out.println("SqlError：暂时不支持此数据类型，请使用其他类型代替此类型！");
        }
    }

    public static void main(String[] args) {
        try {
            Connection conn = DBUtil.openConnection();
//            List<Map<String, Object>> maps = DBUtil.queryMapList(conn, "select * from Person");
            List<Person> person = DBUtil.queryBeanList(conn, "select idCard,name,sex from Person", Person.class);
            System.out.println(person.get(0).toString());
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
