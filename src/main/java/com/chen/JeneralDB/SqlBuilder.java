package com.chen.JeneralDB;

import com.chen.JeneralDB.jdbc.Query;
import com.chen.JeneralDB.jdbc.SortDirection;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 常用sql语句生成器
 *
 * Created by admin on 2017/2/6.
 */
public final class SqlBuilder {


    public static String buildInsertSql(Object obj)
            throws IllegalAccessException {
        return buildInsertSql(obj, null);
    }


    public static String buildInsertSql(Object obj, String tableName)
            throws IllegalAccessException {
        StringBuilder columns = new StringBuilder(" insert into ");
        StringBuilder values = new StringBuilder(" ) values (");

        Class<?> t = obj.getClass();
        Field[] fields = t.getDeclaredFields();
        columns.append(null == tableName ? t.getSimpleName() : tableName);
        columns.append(" ( ");

        int size = fields.length, columnNum = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            String columnName = field.getName();
            String columnType = field.getType().getTypeName();
            Object value = field.get(obj);

            if (null == value) {
                continue;
            }

            columns.append(columnName);

            addValueToValues(values, columnName, columnType, value);

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

        return columns.toString();
    }


    private static void addValueToValues(StringBuilder values, String columnName, String columnType, Object value) {
        if (null == value) {
            values.append("null");
        } else {
            if ("java.lang.Boolean".equals(columnType)) {
                values.append(value.toString());
            } else if ("java.util.Date".equals(columnType)) {
                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                values.append("'");
                values.append(ft.format(value));
                values.append("'");
            } else {
                values.append("'");
                values.append(value.toString());
                values.append("'");
            }
        }
    }


    public static String buildUpdateSql(Object obj) throws Exception {
        return buildUpdateSql(obj, null);
    }


    public static String buildUpdateSql(Object obj, String tableName)
            throws Exception {
        Class<?> t = obj.getClass();
        Field[] fields = t.getDeclaredFields();
        String tName = null != tableName ? tableName : t.getSimpleName();
        StringBuilder update = new StringBuilder(" update " + tName);

        for (int i = 0, size = fields.length; i < size; i++) {
            Field field = fields[i];
            String columnName = field.getName();
            String columnType = field.getType().getTypeName();
            field.setAccessible(true);
            Object value = field.get(obj);

            if (i == 0) {
                update.append(" set ");
            }

            update.append(columnName + " = ");
            addValueToValues(update, columnName, columnType, value);

            if (i < size - 1) {
                update.append(", ");
            }
        }

        String pk = DBFactory.getInstance().getAllPkNamesOfTable(tName)[0];
        Object value = null;

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            if (field.getName().equals(pk)) {
                value = field.get(obj);
                break;
            }
        }

        if (null != value) {
            update.append(" where " + pk + " = ");
            update.append(value.toString());
        }

        return update.toString();
    }


    public static String buildDeleteSql(Object obj)
            throws Exception {
        return buildDeleteSql(obj, null);
    }


    public static String buildDeleteSql(Object obj, String tableName)
            throws Exception {
        Class<?> t = obj.getClass();
        Field[] fields = t.getDeclaredFields();
        String tName = null != tableName ? tableName : t.getSimpleName();
        StringBuilder delete = new StringBuilder("delete from " + tName);

        String pk = DBFactory.getInstance().getAllPkNamesOfTable(tName)[0];
        delete.append(" where " + pk);

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String columnName = field.getName();
            Object value = null;

            field.setAccessible(true);
            if (columnName.equals(pk)) {
                value = field.get(obj);
                delete.append(" = " + value);
                break;
            }
        }

        return delete.toString();
    }

    public static String buildSelectSqlByQuery(Query query) {
        if (null == query || query.isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder(" select ");

        if (null != query.getTop() && !"".equals(query.getTop())) {
            sql.append(" top " + query.getTop() + " ");
        }

        if (null != query.getFields() && query.getFields().length > 0) {
            String[] fields = query.getFields();
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) {
                    sql.append(" , ");
                }
                sql.append(fields[i]);
            }
        } else {
            sql.append(" * ");
        }

        sql.append(" from " + query.getTableName() + " where ");

        boolean isNotFirst = false;
        boolean hasJudege = false;
        Iterator iterator = null;

        if (null != query.getEquals() && !query.getEquals().isEmpty()) {
            hasJudege = true;
            Map<String, String> eq = query.getEquals();
            Set set = eq.keySet();

            for (iterator = set.iterator(); iterator.hasNext(); ) {
                if (isNotFirst) {
                    sql.append(" and ");
                } else {
                    if (set.size() > 0) {
                        isNotFirst = true;
                    }
                }

                String key = (String) iterator.next();
                String val = eq.get(key);
                sql.append(key + " = " + val);
            }
        }

        if (null != query.getNotEquals() && !query.getNotEquals().isEmpty()) {
            hasJudege = true;
            Map<String, String> neq = query.getNotEquals();
            Set set = neq.keySet();

            for (iterator = set.iterator(); iterator.hasNext(); ) {
                if (isNotFirst) {
                    sql.append(" and ");
                } else {
                    if (set.size() > 0) {
                        isNotFirst = true;
                    }
                }

                String key = (String) iterator.next();
                String val = neq.get(key);
                sql.append(key + " != " + val);
            }
        }

        if (null != query.getLikes() && !query.getLikes().isEmpty()) {
            hasJudege = true;
            Map<String, String> like = query.getLikes();
            Set set = like.keySet();

            for (iterator = set.iterator(); iterator.hasNext(); ) {
                if (isNotFirst) {
                    sql.append(" and ");
                } else {
                    if (set.size() > 0) {
                        isNotFirst = true;
                    }
                }

                String key = (String) iterator.next();
                String val = like.get(key);
                sql.append(key + " like '" + val + "'");
            }
        }

        if (null != query.getNotLikes() && !query.getNotLikes().isEmpty()) {
            hasJudege = true;
            Map<String, String> notLikes = query.getNotLikes();
            Set set = notLikes.keySet();

            for (iterator = set.iterator(); iterator.hasNext(); ) {
                if (isNotFirst) {
                    sql.append(" and ");
                } else {
                    if (set.size() > 0) {
                        isNotFirst = true;
                    }
                }

                String key = (String) iterator.next();
                String val = notLikes.get(key);
                sql.append(key + " not like '" + val + "'");
            }
        }

        if (null != query.getIn() && !query.getIn().isEmpty()) {
            hasJudege = true;
            Map<String, Object[]> In = query.getIn();
            Set set = In.keySet();

            for (iterator = set.iterator(); iterator.hasNext(); ) {
                if (isNotFirst) {
                    sql.append(" and ");
                } else {
                    if (set.size() > 0) {
                        isNotFirst = true;
                    }
                }

                String key = (String) iterator.next();
                Object[] val = In.get(key);
                sql.append(key + " in ( ");
                for (int i = 0; i < val.length; i++) {
                    if (i > 0) {
                        sql.append(" , ");
                    }

                    sql.append(val[i].toString());
                }
                sql.append(" ) ");
            }
        }

        if (null != query.getNotIn() && !query.getNotIn().isEmpty()) {
            hasJudege = true;
            Map<String, Object[]> notIn = query.getNotIn();
            Set set = notIn.keySet();

            for (iterator = set.iterator(); iterator.hasNext(); ) {
                if (isNotFirst) {
                    sql.append(" and ");
                } else {
                    if (set.size() > 0) {
                        isNotFirst = true;
                    }
                }

                String key = (String) iterator.next();
                Object[] val = notIn.get(key);
                sql.append(key + " not in ( ");
                for (int i = 0; i < val.length; i++) {
                    if (i > 0) {
                        sql.append(" , ");
                    }

                    sql.append(val[i].toString());
                }
                sql.append(" ) ");
            }
        }

        if (null != query.getBetweens() && !query.getBetweens().isEmpty()) {
            hasJudege = true;
            Map<String, Object[]> betweens = query.getBetweens();
            Set set = betweens.keySet();

            for (iterator = set.iterator(); iterator.hasNext(); ) {
                if (isNotFirst) {
                    sql.append(" and ");
                } else {
                    if (set.size() > 0) {
                        isNotFirst = true;
                    }
                }

                String key = (String) iterator.next();
                Object[] val = betweens.get(key);
                sql.append(key + " between '");
                sql.append(val[0].toString());
                sql.append("' and '");
                sql.append(val[1].toString() + "'");
            }
        }

        if (null != query.getNotBetwee() && !query.getNotBetwee().isEmpty()) {
            hasJudege = true;
            Map<String, Object[]> notBetwee = query.getNotBetwee();
            Set set = notBetwee.keySet();

            for (iterator = set.iterator(); iterator.hasNext(); ) {
                if (isNotFirst) {
                    sql.append(" and ");
                } else {
                    if (set.size() > 0) {
                        isNotFirst = true;
                    }
                }

                String key = (String) iterator.next();
                Object[] val = notBetwee.get(key);
                sql.append(key + " not between '");
                sql.append(val[0].toString());
                sql.append("' and '");
                sql.append(val[1].toString() + "'");
            }
        }

        if (null != query.getSorts() && !query.getSorts().isEmpty()) {
            if (!hasJudege) {
                String sql1 = sql.toString();
                sql1 = sql1.replaceAll("where", "");
                sql = new StringBuilder(sql1);
            }

            Map<String, SortDirection> sorts = query.getSorts();
            Set set = sorts.keySet();

            int i = 0;
            for (iterator = set.iterator(); iterator.hasNext(); i++) {

                String key = (String) iterator.next();
                SortDirection direction = sorts.get(key);

                if (i == 0) {
                    sql.append(" order by ");
                } else {
                    sql.append(" , ");
                }

                sql.append(key);
                if (direction.equals(SortDirection.ASCENDING)) {
                    sql.append(" asc");
                } else {
                    sql.append(" desc");
                }
            }
        }

        if (!hasJudege) {
            String sql1 = sql.toString();
            sql1 = sql1.replaceAll("where", "");
            sql = new StringBuilder(sql1);
        }

        if (query.getPageNo() >= 0
                && query.getPageSize() > 0) {
            sql.append(" limit " + query.getPageNo());
            sql.append(" , ");
            sql.append(query.getPageSize());
        }

        sql.append(";");

        return sql.toString();
    }
}
