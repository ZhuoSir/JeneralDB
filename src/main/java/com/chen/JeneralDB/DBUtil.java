package com.chen.JeneralDB;


import com.chen.JeneralDB.jdbc.Query;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

import static com.chen.JeneralDB.SqlBuilder.*;

/**
 * 数据库操作工具类
 *
 * @author 陈卓
 * @version 1.0.0
 */
public class DBUtil {

    private Connection conn = null;

    private static DBUtil dbUtil;

    private static boolean AutoCommit = true;

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
        Properties properties = new Properties();
        properties.load(DBUtil.class.getResourceAsStream("/JeneralDB-config.properties"));
        Class.forName(properties.getProperty("db_driver"));

        conn = DriverManager.getConnection(
                properties.getProperty("db_url"),
                properties.getProperty("db_username"),
                properties.getProperty("db_password"));
        conn.setAutoCommit(AutoCommit);

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
        checkConnect();

        List<Map<String, Object>> lists = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            genDataFromResultSet(rs, lists);
            print("执行sql: " + sql);
        } finally {
            if (null != rs)
                rs.close();
            if (null != stmt)
                stmt.close();
            if (null != conn && AutoCommit)
                conn.close();
        }

        return lists;
    }


    public List<Map<String, Object>> queryMapList(String sql, Object... params)
            throws Exception {
        checkConnect();

        List<Map<String, Object>> lists = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            statement = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            rs = statement.getResultSet();
            genDataFromResultSet(rs, lists);
            print("执行sql: " + sql);
        } finally {
            if (null != rs)
                rs.close();
            if (null != statement)
                statement.close();
            if (null != conn && AutoCommit)
                conn.close();
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
                String name = metaData.getColumnLabel(i + 1);
//                String name = metaData.getColumnClassName(i + 1);
                Object value = rs.getObject(name);
                map.put(name, value);
            }
            lists.add(map);
        }
    }


    public <T> List<T> queryBeanList(String sql, Class<T> beanClass)
            throws Exception {
        checkConnect();

        List<T> lists = new ArrayList<T>();
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            print("执行sql: " + sql);
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
            if (null != conn && AutoCommit)
                conn.close();
        }

        return lists;
    }


    public <T> List<T> queryBeanList(String sql, Class<T> beanClass, Object... params)
            throws Exception {
        checkConnect();

        List<T> lists = new ArrayList<T>();
        PreparedStatement preStmt = null;
        ResultSet rs = null;

        try {
            preStmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                preStmt.setObject(i + 1, params[i]);
            }

            rs = preStmt.executeQuery();
            print("执行sql: " + sql);
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
            if (null != conn && AutoCommit)
                conn.close();
        }

        return lists;
    }


    public <T> T queryBean(String sql, Class<T> beanClass)
            throws Exception {
        List<T> lists = queryBeanList(sql, beanClass);
        return (null != lists && !lists.isEmpty()) ? lists.get(0) : null;
    }


    public <T> T queryBean(String sql, Class<T> beanClass, Object... params)
            throws Exception {
        List<T> lists = queryBeanList(sql, beanClass, params);
        return (null != lists && !lists.isEmpty()) ? lists.get(0) : null;
    }


    public DataTable queryDataTable(String sql)
            throws Exception {
        List<Map<String, Object>> list = queryMapList(sql);
        DataTable dataTable = null;

        if (null != list && !list.isEmpty()) {
            dataTable = new DataTable(list);
        }

        return dataTable;
    }


    public <T> List<T> queryBeanListByQuery(Query query, Class<T> beanClass)
            throws Exception {
        String tableName = beanClass.getSimpleName();
        query.setTableName(tableName);
        return queryByQuery(query).toBeanList(beanClass);
    }


    public DataTable queryByQuery(Query query)
            throws Exception {
        String sql = buildSelectSqlByQuery(query);
        if (null != sql && !"".equals(sql)) {
            return queryDataTable(sql);
        }

        return null;
    }


    public ResultSetMetaData queryResultSetMetaData(String sql)
            throws Exception {
        checkConnect();

        PreparedStatement preStmt = conn.prepareStatement(sql);
        return preStmt.getMetaData();
    }


    public int execute(String sql)
            throws Exception {
        checkConnect();

        int result;

        Statement statement = conn.createStatement();
        print("执行sql: " + sql);
        result = statement.executeUpdate(sql);
        statement.close();

        if (AutoCommit) {
            conn.close();
        }

        return result;
    }


    public int execute(String sql, Connection conn) throws SQLException {
        if (null == conn || conn.isClosed()) {
            throw new SQLException();
        }

        int result;

        Statement statement = conn.createStatement();
        print("执行sql: " + sql);
        result = statement.executeUpdate(sql);

        statement.close();

        return result;
    }


    public int execute(String sql, Object... params)
            throws Exception {
        checkConnect();

        int result;

        PreparedStatement preStmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            print("执行sql: " + sql);
            preStmt.setObject(i + 1, params[i]);// 下标从1开始
        }
        result = preStmt.executeUpdate();

        preStmt.close();
        if (AutoCommit) {
            conn.close();
        }

        return result;
    }


    public int[] executeAsBatch(List<String> sqlList)
            throws Exception {
        return executeAsBatch(conn, sqlList.toArray(new String[]{}));
    }


    public int[] executeAsBatch(Connection conn, String[] sqlArray) throws Exception {
        checkConnect();

        int[] result;

        Statement stmt = conn.createStatement();

        for (String sql : sqlArray) {
            stmt.addBatch(sql);
            print("执行sql: " + sql);
        }

        result = stmt.executeBatch();

        stmt.close();
        if (AutoCommit) {
            conn.close();
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
                print("执行sql: " + sql);
            }

            return preStmt.executeBatch();
        } finally {
            if (null != preStmt) {
                preStmt.close();
            }
            conn.close();
        }
    }


    public int save(Object obj) throws Exception {
        if (null == obj) {
            throw new NullPointerException("保存对象不能为Null");
        }

        return this.execute(buildInsertSql(obj));
    }


    public int save(Object obj, String tableName) throws Exception {
        if (null == obj) {
            throw new NullPointerException("保存对象不能为Null");
        }

        return this.execute(buildInsertSql(obj, tableName));
    }


    public int save(Connection conn, Object obj) throws Exception {
        if (null == obj) {
            throw new NullPointerException("保存对象不能为Null");
        }

        return this.execute(buildInsertSql(obj), conn);
    }


    public int save(Connection conn, Object obj, String tableName) throws Exception {
        if (null == obj) {
            throw new NullPointerException("保存对象不能为Null");
        }

        return this.execute(buildInsertSql(obj, tableName), conn);
    }


    public int update(Object obj) throws Exception {
        if (null == obj) {
            throw new NullPointerException("更新对象不能为Null");
        }

        return this.execute(buildUpdateSql(obj));
    }


    public int update(Object obj, String tableName) throws Exception {
        if (null == obj) {
            throw new NullPointerException("更新对象不能为Null");
        }

        return this.execute(buildUpdateSql(obj, tableName));
    }


    public int update(Connection conn, Object obj, String tableName)
            throws Exception {
        if (null == obj) {
            throw new NullPointerException("更新对象不能为Null");
        }

        return this.execute(buildUpdateSql(obj, tableName), conn);
    }


    public int delete(Object obj) throws Exception {
        if (null == obj) {
            throw new NullPointerException("删除对象不能为Null");
        }

        return this.execute(buildDeleteSql(obj));
    }


    public int delete(Object obj, String tableName) throws Exception {
        if (null == obj) {
            throw new NullPointerException("删除对象不能为Null");
        }

        return this.execute(buildDeleteSql(obj, tableName));
    }


    public int delete(Connection conn, Object obj, String tableName)
            throws Exception {
        if (null == obj) {
            throw new NullPointerException("删除的对象不能为Null");
        }

        return this.execute(buildDeleteSql(obj, tableName), conn);
    }


    protected <T> void setValue(T t, Field f, Object value)
            throws Exception {
        if (null == value) {
            return;
        }

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
        } else if ("java.util.Date".equals(n)) {
            f.set(t, (Date) value);
        } else if ("java.lang.Timer".equals(n)) {
            f.set(t, new Time(((Time) value).getTime()));
        } else if ("java.sql.Timestamp".equals(n)) {
            f.set(t, (Timestamp) value);
        } else {
            throw new Exception("SqlError：暂时不支持此数据类型，请使用其他类型代替此类型！");
        }
    }


    /**
     * 格式打印输出
     */
    public static void print(String content) {
        if (null == content) {
            return;
        }

        StringBuilder builder = new StringBuilder("JeneralDB：");
        builder.append(content);
        System.out.println(builder.toString());
    }


    /**
     * 检查连接是否开启，若数据库连接尚未开启，开启之。
     *
     * @throws Exception
     */
    public void checkConnect() throws Exception {
        if (null == conn || conn.isClosed()) {
            conn = openConnection();
        }
    }


    /**
     * 开启事务。若数据库连接尚未开启，开启之。
     *
     * @throws Exception
     */
    public void transBegin() throws Exception {
        checkConnect();

        conn.setAutoCommit(false);
        this.AutoCommit = false;
    }


    /**
     * 开启事物。若数据库连接尚未开启，开启之.
     *
     * @param isolationLevel 隔离等级
     * */
    public void transBegin(int isolationLevel) throws Exception {
        checkConnect();

        conn.setTransactionIsolation(isolationLevel);
        conn.setAutoCommit(false);
        this.AutoCommit = false;
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
