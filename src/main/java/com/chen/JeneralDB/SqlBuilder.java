package com.chen.JeneralDB;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

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
            Object value = field.get(obj);
            field.setAccessible(true);

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
}
