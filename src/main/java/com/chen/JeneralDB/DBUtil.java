package com.chen.JeneralDB;


import com.chen.JeneralDB.jdbc.Query;
import com.chen.JeneralDB.transaction.SimpleTransaction;
import com.chen.JeneralDB.transaction.Transaction;
import com.chen.JeneralDB.utils.ReflectUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

import static com.chen.JeneralDB.SqlBuilder.*;

/**
 * 数据库操作工具类
 *
 * @author 陈卓
 * @version 1.1.0
 */
public class DBUtil {

    private DataSource dataSource;

    private static volatile DBUtil dbUtil;

    private static boolean AutoCommit = true;

    public DBUtil() {
    }

    @Deprecated
    public static DBUtil getInstance() {
        if (null == dbUtil) {
            synchronized (DBUtil.class) {
                if (null == dbUtil) {
                    dbUtil = new DBUtil();
                }
            }
        }

        return dbUtil;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 打开数据连接方法<br>
     * <P>连接数据在JeneralDB-config.properties中</P>
     *
     * */
    public Connection openConnection()
            throws Exception {
        if (dataSource != null) {
            Connection conn = dataSource.getConnection();
            return conn;
        } else {
            throw new NullPointerException("DataSource is null...");
        }
    }


    /**
     * Map对象查询方法
     *
     * @param sql 查询语句
     * @return 查询结果Map
     * */
    @Deprecated
    public List<Map<String, Object>> queryMapList(String sql)
            throws Exception {

        List<Map<String, Object>> lists = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        
        Connection conn = openConnection();
        try {
            stmt = openConnection().createStatement();
            rs = stmt.executeQuery(sql);
            genDataFromResultSet(rs, lists);
            
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


    /**
     * Map对象查询方法
     *
     * @param sql 查询语句
     * @param params sql参数
     *
     * @return 查询结果Map
     * */
    @Deprecated
    public List<Map<String, Object>> queryMapList(String sql, Object... params)
            throws Exception {
        Connection conn = openConnection();

        List<Map<String, Object>> lists = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            statement = conn.prepareStatement(sql);

            if (null != params) {
                for (int i = 0, j = 1; i < params.length; i++) {
                    if (null == params[i]) continue;
                    statement.setObject(j++, params[i]);
                }
            }

            rs = statement.executeQuery();
            genDataFromResultSet(rs, lists);
            
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


    /**
     * 从结果集ResultSet中获取数据
     *
     * @param rs 结果集
     * @param lists 返回Map数组
     * */
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


    /**
     * 查询JavaBean数组方法
     *
     * @param sql 查询sql
     * @param beanClass 转换Javabean对象Class
     *
     * @return 结果数组
     * */
    public <T> List<T> queryBeanList(String sql, Class<T> beanClass)
            throws Exception {
        return queryBeanList(sql, beanClass, null);
    }


    /**
     * 查询JavaBean数组方法
     *
     * @param sql 查询sql
     * @param beanClass 转换Javabean对象Class
     * @param params sql 查询参数
     *
     * @return 结果数组
     * */
    public <T> List<T> queryBeanList(String sql, Class<T> beanClass, Object... params)
            throws Exception {
        Connection conn = openConnection();

        List<T> lists = new ArrayList<>();
        PreparedStatement preStmt = null;
        ResultSet rs = null;

        try {
            preStmt = conn.prepareStatement(sql);

            if (null != params) {
                for (int i = 0; i < params.length; i++) {
                    preStmt.setObject(i + 1, params[i]);
                }
            }

            rs = preStmt.executeQuery();
            
            Field[] fields = ReflectUtil.getAllDeclaredFields(beanClass);

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
                    } catch (SQLException e) {

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
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


    /**
     * 查询JavaBean数组方法(新方法)
     *
     * @param sql 查询sql
     * @param beanClass 转换Javabean对象Class
     *
     * @return 结果数组
     * */
    public <T> List<T> queryBeanListNew(Class<T> beanClass, String sql, Object... params)
            throws Exception {
        Connection conn = openConnection();

        List<T>           lists     = new ArrayList<T>();
        PreparedStatement preStmt   = null;
        ResultSet         resultSet = null;
        try {
            preStmt = conn.prepareStatement(sql); {
                if (null != params) {
                    for (int i = 0; i < params.length; i++) {
                        preStmt.setObject(i + 1, params[i]);
                    }
                }
            }

            resultSet = preStmt.executeQuery();
            ResultSetMetaData data = resultSet.getMetaData();

            String[] columnArr = new String[data.getColumnCount()];
            for (int i = 1; i <= data.getColumnCount(); i++) {
                columnArr[i-1] = data.getColumnLabel(i);
            }

            while (resultSet.next()) {
                T t = beanClass.newInstance();
                for (int i = 0; i < columnArr.length; i++) {
                    try {
                        Field field = beanClass.getDeclaredField(columnArr[i]);
                        field.setAccessible(true);
                        Object value = resultSet.getObject(columnArr[i]);
                        setValue(t, field, value);
                    } catch (NoSuchFieldException e) {
                        throw e;
                    }
                }

                lists.add(t);
            }
        } finally {
            if (null != preStmt)
                preStmt.close();
            if (null != resultSet)
                resultSet.close();
            if (null != conn && AutoCommit)
                conn.close();
        }

        return lists;
    }


    /**
     * 查询单独JavaBean方法
     *
     * @param sql 查询sql
     * @param beanClass 转换Javabean对象Class
     *
     * @return 结果对象
     * */
    public <T> T queryBean(String sql, Class<T> beanClass)
            throws Exception {
        List<T> lists = queryBeanList(sql, beanClass);
        return (null != lists && !lists.isEmpty()) ? lists.get(0) : null;
    }


    /**
     * 查询单独JavaBean方法
     *
     * @param sql 查询sql
     * @param beanClass 转换Javabean对象Class
     * @param params 查询参数
     *
     * @return 结果对象
     * */
    public <T> T queryBean(String sql, Class<T> beanClass, Object... params)
            throws Exception {
        List<T> lists = queryBeanList(sql, beanClass, params);
        return (null != lists && !lists.isEmpty()) ? lists.get(0) : null;
    }


    /**
     * 查询DataTable方法
     *
     * @param sql 查询sql
     *
     * @return 结果DataTable
     * */
    public DataTable queryDataTable(String sql)
            throws Exception {
        return queryDataTable(sql, null);
    }


    /**
     * 查询DataTable方法
     *
     * @param sql 查询sql
     * @param params 参数
     *
     * @return 结果DataTable
     * */
    public DataTable queryDataTable(String sql, Object... params) throws Exception {
        List<Map<String, Object>> list = queryMapList(sql, params);
        DataTable dataTable = new DataTable();

        if (null != list && !list.isEmpty()) {
            dataTable = new DataTable(list);
        }

        return dataTable;
    }


    /**
     * 查询DataTable方法,Query方式
     *
     * @param query Query对象
     *
     * @return 结果DataTable
     * */
    public DataTable queryDataTable(Query query)
            throws Exception {
        return queryDataTable(buildSelectSqlByQuery(query));
    }


    /**
     * 根据Query查询bean集合
     *
     * @param query 查询query
     * @param beanClass 查询类class
     *
     * @return 返回集合
     * */
    public <T> List<T> queryBeanListByQuery(Query query, Class<T> beanClass)
            throws Exception {
        DataTable dt = queryByQuery(query);
        return null != dt ? dt.toBeanList(beanClass) : null;
    }


    public DataTable queryByQuery(Query query)
            throws Exception {
        String sql = buildSelectSqlByQuery(query);
        if (null != sql && !"".equals(sql)) {
            return queryDataTable(sql);
        }

        return null;
    }


    public Object querySingleOne(String sql)
            throws Exception {
        return querySingleOne(sql, null);
    }


    public Object querySingleOne(String sql, Object... params)
            throws Exception {
        DataTable dt;

        if (null != params)
            dt = queryDataTable(sql, params);
        else
            dt = queryDataTable(sql);

        if (null == dt)
            return null;
        else
            return dt.getObjectAtCoordinate(0,0);
    }


    public Object querySingleOne(Query query)
            throws Exception {
        String sql = buildSelectSqlByQuery(query);

        return querySingleOne(sql);
    }

    public ResultSetMetaData queryResultSetMetaData(String sql)
            throws Exception {
        Connection conn = openConnection();

        PreparedStatement preStmt = conn.prepareStatement(sql);
        return preStmt.getMetaData();
    }


    public int execute(String sql)
            throws Exception {
        Connection conn = openConnection();

        int result;

        Statement statement = conn.createStatement();
        
        result = statement.executeUpdate(sql);
        statement.close();

        if (AutoCommit) {
            conn.close();
        }

        return result;
    }


    public int execute(String sql, Connection conn) throws Exception {
        if (null == conn || conn.isClosed()) {
            conn = openConnection();
        }

        int result;

        Statement statement = conn.createStatement();
        
        result = statement.executeUpdate(sql);

        statement.close();

        return result;
    }


    public int execute(String sql, Object... params)
            throws Exception {
        Connection conn = openConnection();

        int result;

        PreparedStatement preStmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            
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
        return executeAsBatch(openConnection(), sqlList.toArray(new String[]{}));
    }


    public int[] executeAsBatch(Connection conn, String[] sqlArray) throws Exception {

        int[] result;

        Statement stmt = conn.createStatement();

        for (String sql : sqlArray) {
            stmt.addBatch(sql);
            
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

        Connection conn = openConnection();
        try {
            preStmt = conn.prepareStatement(sql);

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
            conn.close();
        }
    }


    public int save(Object obj) throws Exception {
        return save(openConnection(), obj);
    }


    public int save(Connection conn, Object obj) throws Exception {
        if (null == obj) {
            throw new IllegalArgumentException("保存对象不能为Null");
        }

        return this.execute(buildInsertSql(obj), conn);
    }


    public int update(Object obj) throws Exception {
        return update(openConnection(), obj);
    }


    public int update(Connection conn, Object obj)
            throws Exception {
        if (null == obj) {
            throw new IllegalArgumentException("更新对象不能为Null");
        }

        return this.execute(buildUpdateSql(obj), conn);
    }


    public int saveOrUpdate(Object obj) throws Exception {
        return saveOrUpdate(openConnection(), obj);
    }


    public int saveOrUpdate(Connection conn, Object obj) throws Exception {
        if (Objects.isNull(obj)) {
            throw new IllegalArgumentException("操作对象不能为Null");
        }

        int count = this.execute(buildUpdateSql(obj), conn);
        if (count == 0) {
            count = this.execute(buildInsertSql(obj), conn);
        }

        return count;
    }


    public int delete(Object obj) throws Exception {
        return delete(openConnection(), obj);
    }


    public int delete(Connection conn, Object obj)
            throws Exception {
        if (null == obj) {
            throw new IllegalArgumentException("删除的对象不能为Null");
        }

        return this.execute(buildDeleteSql(obj), conn);
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
        } else if ("java.lang.Boolean".equals(n) || "boolean".equals(n)) {
            f.set(t, (Boolean)value);
        } else {
            throw new Exception("SqlError：暂时不支持此数据类型，请使用其他类型代替此类型！");
        }
    }
    
    /**
     * 开启事务。若数据库连接尚未开启，开启之。
     *
     * @throws Exception
     */
    public Transaction transBegin() throws Exception {
        Connection conn = openConnection();

        conn.setAutoCommit(false);
        this.AutoCommit = false;
        Transaction transaction = new SimpleTransaction(conn);
        return transaction;
    }


    /**
     * 开启事物。若数据库连接尚未开启，开启之.
     *
     * @param isolationLevel 隔离等级
     * */
    public Transaction transBegin(int isolationLevel) throws Exception {
        Connection conn = openConnection();

        conn.setTransactionIsolation(isolationLevel);
        conn.setAutoCommit(false);
        this.AutoCommit = false;

        Transaction transaction = new SimpleTransaction(conn);
        return transaction;
    }


    /**
     * 提交事务。提交事务后，自动关闭连接。
     *
     * @throws Exception
     */
    public void transCommit(Transaction transaction) throws Exception {
        transaction.commit();
    }


    /**
     * 回滚事务。提交事务后，自动关闭连接。
     *
     * @throws Exception
     */
    public void transRollBack(Transaction transaction) throws Exception {
        transaction.rollback();
    }
}
