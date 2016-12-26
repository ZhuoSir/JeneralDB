package com.chen.JeneralDB;


import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by sunny-chen on 16/11/27.
 */
public class DBUtil {

    private Connection conn = null;

    private static DBUtil dbUtil;

    private DBUtil() {
    }

    public static synchronized DBUtil getInstance() {
        if (null == dbUtil) {
            dbUtil = new DBUtil();
        }
        return dbUtil;
    }

    public Connection openConnection()
            throws Exception {
        if (null == conn || conn.isClosed()) {
            Properties properties = new Properties();
            properties.load(DBUtil.class.getResourceAsStream("/JeneralDB-config.properties"));
            Class.forName(properties.getProperty("db_driver"));
            conn = DriverManager.getConnection(properties.getProperty("db_url"), properties.getProperty("db_username"),
                    properties.getProperty("db_password"));
        }
        return conn;
    }

    public void closeConnection() throws SQLException {
        try {
            if (null != conn) {
                conn.close();
            }
        } finally {
            conn = null;
            System.gc();
        }
    }

    public List<Map<String, Object>> queryMapList(String sql)
            throws Exception {
        return queryMapList(conn, sql);
    }

    public List<Map<String, Object>> queryMapList(Connection connection, String sql)
            throws Exception {
        if (null == connection) {
            connection = this.openConnection();
        }
        List<Map<String, Object>> lists = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            genDataFromResultSet(rs, lists);
            System.out.println("JeneralDB：执行sql: " + sql);
        } finally {
            if (null != rs)
                rs.close();
            if (null != stmt)
                stmt.close();
            if (null != connection)
                closeConnection();
        }
        return lists;
    }

    public List<Map<String, Object>> queryMapList(String sql, Object... params)
            throws Exception {
        return queryMapList(conn, sql, params);
    }

    public List<Map<String, Object>> queryMapList(Connection connection, String sql, Object... params)
            throws Exception {
        if (null == connection) {
            connection = openConnection();
        }
        List<Map<String, Object>> lists = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            rs = statement.getResultSet();
            genDataFromResultSet(rs, lists);
            System.out.println("JeneralDB：执行sql: " + sql);
        } finally {
            if (null != rs)
                rs.close();
            if (null != statement)
                statement.close();
            if (null != connection)
                closeConnection();
        }
        return lists;
    }

    private void genDataFromResultSet(ResultSet rs, List<Map<String, Object>> lists)
            throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < columnCount; i++) {
                String name = metaData.getColumnName(i + 1);
                Object value = rs.getObject(name);
                map.put(name, value);
            }
            lists.add(map);
        }
    }

    public <T> List<T> queryBeanList(String sql, Class<T> beanClass)
            throws Exception {
        return queryBeanList(conn, sql, beanClass);
    }

    public <T> List<T> queryBeanList(Connection con, String sql, Class<T> beanClass)
            throws Exception {
        if (null == con) {
            con = openConnection();
        }
        List<T> lists = new ArrayList<T>();
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = con.createStatement();
            resultSet = stmt.executeQuery(sql);
            System.out.println("JeneralDB：执行sql: " + sql);
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }
            while (null != resultSet && resultSet.next()) {
                T t = beanClass.newInstance();
                for (Field field : fields) {
                    String name = field.getName();
                    try {
                        Object value = resultSet.getObject(name);
                        setValue(t, field, value);
                    } catch (Exception e) {
                        e.printStackTrace();
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
                closeConnection();
        }
        return lists;
    }

    public <T> List<T> queryBeanList(String sql, Class<T> beanClass, Object... params)
            throws Exception {
        return queryBeanList(conn, sql, beanClass, params);
    }

    public <T> List<T> queryBeanList(Connection con, String sql, Class<T> beanClass, Object... params)
            throws Exception {
        if (null == con) {
            con = openConnection();
        }
        List<T> lists = new ArrayList<T>();
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        try {
            preStmt = con.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                preStmt.setObject(i + 1, params[i]);
            }
            rs = preStmt.executeQuery();
            System.out.println("JeneralDB：执行sql: " + sql);
            Field[] fields = beanClass.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
            }
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
                closeConnection();
        }
        return lists;
    }

    public <T> T queryBean(String sql, Class<T> beanClass)
            throws Exception {
        List<T> lists = queryBeanList(conn, sql, beanClass);
        return (null != lists && !lists.isEmpty()) ? lists.get(0) : null;
    }

    public <T> T queryBean(String sql, Class<T> beanClass, Object... params)
            throws Exception {
        List<T> lists = queryBeanList(conn, sql, beanClass, params);
        return (null != lists && !lists.isEmpty()) ? lists.get(0) : null;
    }

    public <T> DataTable queryDataTable(String sql, Class<T> beanClass)
            throws Exception {
        List<T> list = queryBeanList(sql, beanClass);
        DataTable dataTable = null;
        if (list != null && !list.isEmpty()) {
            dataTable = new DataTable(list, beanClass);
        }
        return dataTable;
    }

    public DataTable queryDataTable(String sql)
            throws Exception {
        List<Map<String, Object>> list = queryMapList(sql);
        DataTable dataTable = null;
        if (list != null && !list.isEmpty()) {
            dataTable = new DataTable(list);
        }
        return dataTable;
    }

    public int execute(String sql)
            throws Exception {
        boolean openConnHere = false;
        int result;
        if (null == conn) {
            conn = openConnection();
            openConnHere = true;
        }
        Statement statement = conn.createStatement();
        System.out.println("JeneralDB：执行sql: " + sql);
        result = statement.executeUpdate(sql);
        statement.close();
        if (openConnHere) {
            closeConnection();
        }
        return result;
    }

    public int execute(String sql, Object... params)
            throws Exception {
        boolean openConnHere = false;
        int result;
        if (null == conn) {
            conn = openConnection();
            openConnHere = true;
        }
        PreparedStatement preStmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            System.out.println("JeneralDB：执行sql: " + sql);
            preStmt.setObject(i + 1, params[i]);// 下标从1开始
        }

        result = preStmt.executeUpdate();
        preStmt.close();
        if (openConnHere) {
            closeConnection();
        }
        return result;
    }

    public int[] executeAsBatch(List<String> sqlList)
            throws Exception {
        return executeAsBatch(conn, sqlList.toArray(new String[]{}));
    }

    public int[] executeAsBatch(Connection con, String[] sqlArray) throws Exception {
        boolean openConnHere = false;
        int[] result;
        if (null == con) {
            con = openConnection();
            openConnHere = true;
        }
        Statement stmt = con.createStatement();
        for (String sql : sqlArray) {
            stmt.addBatch(sql);
            System.out.println("JeneralDB：执行sql: " + sql);
        }
        result = stmt.executeBatch();
        stmt.close();
        if (openConnHere) {
            closeConnection();
        }
        return result;
    }

    public int[] executeAsBatch(String sql, Object[][] params)
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
                System.out.println("JeneralDB：执行sql: " + sql);
            }
            return preStmt.executeBatch();
        } finally {
            if (null != preStmt) {
                preStmt.close();
            }
            closeConnection();
        }
    }

    public int save(Object obj) throws Exception {
        if (null == obj) {
            throw new NullPointerException("保存对象不能为Null");
        }
        StringBuilder columns = new StringBuilder(" insert into ");
        StringBuilder values = new StringBuilder(" ) values (");
        Class<?> t = obj.getClass();
        String tableName = t.getSimpleName();
        Field[] fields = t.getDeclaredFields();
        columns.append(tableName + " ( ");
        int size = fields.length, columnNum = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            String columnName = field.getName();
            Object value = field.get(obj);
            if (null == value) continue;
            columns.append(columnName);
            values.append("'" + value.toString() + "'");
            if (columnNum++ < size - 1) {
                columns.append(" ,");
                values.append(" ,");
            }
        }
        if (columns.charAt(columns.length() - 1) == ',') {
            columns = columns.deleteCharAt(columns.length() - 1);
        }
        if (values.charAt(values.length() - 1) == ',') {
            values = values.deleteCharAt(values.length() - 1);
        }
        values.append(" ) ");
        columns.append(values);
        return this.execute(columns.toString());
    }

    protected <T> void setValue(T t, Field f, Object value)
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

    /**
     * 开启事务。若数据库连接尚未开启，开启之。
     *
     * @throws Exception
     */
    public void transBegin() throws Exception {
        if (null == conn) openConnection();
        conn.setAutoCommit(false);
    }

    /**
     * 提交事务。提交事务后，自动关闭连接。
     *
     * @throws Exception
     */
    public void transCommit() throws Exception {
        conn.commit();
        closeConnection();
    }

    /**
     * 回滚事务。提交事务后，自动关闭连接。
     *
     * @throws Exception
     */
    public void transRollBack() throws Exception {
        conn.rollback();
        closeConnection();
    }
}
