package com.chen.JeneralDB;

import bean.Person;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunny on 2016/11/29.
 */
public class DataTable {

    //行记录
    private ArrayList<Object[]> rows = null;

    //列记录
    private ArrayList<String> columns = null;

    public DataTable() {
        columns = new ArrayList<String>(1);
        columns.add("Columns1");
        rows = new ArrayList<Object[]>();
    }

    public DataTable(int columnsCount) {
        this.columns = new ArrayList<String>(columnsCount);
        for (int i = 0; i < columnsCount; i++) {
            this.columns.add("columns" + i);
        }
        rows = new ArrayList<Object[]>();
    }

    public DataTable(String[] columnNames, Object[] rowDatas) {
        if (columnNames != null && columnNames.length > 0) {
            this.columns = new ArrayList<String>(columnNames.length);
            for (int i = 0; i < columnNames.length; i++) {
                this.columns.add(columnNames[i]);
            }
        }
        this.rows = new ArrayList<Object[]>();
        this.rows.add(rowDatas);
    }

    public DataTable(List<?> list) throws Exception {
        if (list == null || (list.size() == 0)) {
            throw new NullPointerException("DataTable初始化list不能为Null");
        }
        String[] colArr = null;
        {
            Class<?> ownerClass = list.get(0).getClass();
            Field[] fields = ownerClass.getDeclaredFields();
            colArr = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                colArr[i] = fields[i].getName();
            }
        }
        Object[][] dataArr = new Object[list.size()][colArr.length];
        for (int i = 0; i < dataArr.length; i++) {
            Object obj = list.get(i);
            Class<?> ownerClass = obj.getClass();
            Field[] fields = ownerClass.getDeclaredFields();
            for (int j = 0; j < fields.length; j++) {
                Field field = fields[j];
                field.setAccessible(true);
                dataArr[i][j] = field.get(obj);
            }
        }

        this.columns = new ArrayList<String>(colArr.length);
        if (colArr != null && colArr.length > 0) {
            for (int c = 0; c < colArr.length; c++) {
                this.columns.add(colArr[c]);
            }
        }

        this.rows = new ArrayList<Object[]>(dataArr.length);
        if (dataArr != null && dataArr.length > 0) {
            for (int k = 0; k < dataArr.length; k++) {
                this.rows.add(dataArr[k]);
            }
        }
    }

    public DataTable(Class<?> t) {
        String[] colArr = null;
        {
            Class<?> ownerClass = t;
            Field[] fields = ownerClass.getDeclaredFields();
            colArr = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                colArr[i] = fields[i].getName();
            }
        }
        this.columns = new ArrayList<String>(colArr.length);
        if (colArr != null && colArr.length > 0) {
            for (int c = 0; c < colArr.length; c++) {
                this.columns.add(colArr[c]);
            }
        }
        this.rows = new ArrayList<Object[]>(1);
    }

    public void addRow(Object[] rowObjs) throws Exception {
        if (rowObjs.length != this.rows.get(0).length) {
            throw new Exception("新插入的对象数组必须等于数据表DataTable的rows的长度");
        }
        this.rows.add(rowObjs);
    }

    public void addColumn(String columnName) {
        this.columns.add(columnName);
        int columnSize = this.columns.size();
        int rowSize = this.rows.size();
        Object[][] dataArr = new Object[rowSize][columnSize];
        for (int i = 0; i < columnSize; i++) {
            Object[] theRow = this.rows.get(i);
            for (int j = 0; j < rowSize; j++) {
                if (j < theRow.length) {
                    dataArr[i][j] = theRow[j];
                } else {
                    dataArr[i][j] = null;
                }
            }
        }
        this.rows = new ArrayList<Object[]>(dataArr.length);
        if (dataArr != null && dataArr.length > 0) {
            for (int k = 0; k < dataArr.length; k++) {
                this.rows.add(dataArr[k]);
            }
        }
    }

    public int getRowLenth() {
        return this.rows.size();
    }

    public Object[] getRowAtIndex(int index) {
        return this.rows.get(index);
    }

}
