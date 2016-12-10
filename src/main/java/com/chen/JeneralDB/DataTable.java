package com.chen.JeneralDB;

import bean.Person;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by sunny on 2016/11/29.
 */
public class DataTable {

    /**
     * 游标
     */
    private int cursor;

    /**
     * 列记录
     */
    private ArrayList<String> columns = null;

    /**
     * 行记录
     */
    private ArrayList<Object[]> rows = null;

    /**
     * 默认初始化方法，列记录默认添加"Columns1"；
     */
    public DataTable() {
        columns = new ArrayList<>(1);
        columns.add("Columns1");
        rows = new ArrayList<>();
    }

    /**
     * 列数构造器，构造指定列数的DataTable；
     *
     * @param columnsCount 指定数量
     */
    public DataTable(int columnsCount) {
        this.columns = new ArrayList<>(columnsCount);
        for (int i = 0; i < columnsCount; i++) {
            this.columns.add("Columns" + i);
        }
        rows = new ArrayList<>();
    }

    /**
     * 指定列名，以及对象矩阵构造；
     *
     * @param columnNames 列名数组；
     * @param rowDatas    对象矩阵
     */
    public DataTable(String[] columnNames, Object[] rowDatas) throws Exception {
        if (columnNames != null && columnNames.length > 0) {
            if (columnNames.length != rowDatas.length) {
                throw new Exception("行记录与列记录长度必须保持一致");
            }
            this.columns = new ArrayList<>(columnNames.length);
            for (int i = 0; i < columnNames.length; i++) {
                this.columns.add(columnNames[i]);
            }
        }
        this.rows = new ArrayList<>();
        this.rows.add(rowDatas);
    }

    /**
     * 指定集合构造；
     *
     * @param list 数据集合
     */
    public DataTable(List<?> list) throws Exception {
        if (list == null || (list.size() == 0)) {
            throw new Exception("DataTable初始化list不能为Null或者size为0");
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

        this.columns = new ArrayList<>(colArr.length);
        if (colArr != null && colArr.length > 0) {
            for (int c = 0; c < colArr.length; c++) {
                this.columns.add(colArr[c]);
            }
        }

        this.rows = new ArrayList<>(dataArr.length);
        if (dataArr != null && dataArr.length > 0) {
            for (int k = 0; k < dataArr.length; k++) {
                this.rows.add(dataArr[k]);
            }
        }
    }

    /**
     * 对象构造，将对象中的属性名做为列名，属性值为数据
     *
     * @param t 类对象泛型
     */
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
        this.columns = new ArrayList<>(colArr.length);
        if (colArr != null && colArr.length > 0) {
            for (int c = 0; c < colArr.length; c++) {
                this.columns.add(colArr[c]);
            }
        }
        this.rows = new ArrayList<>(0);
    }

    /**
     * 加入一行数据，对象数组方式；
     *
     * @param rowObjs 数据数组
     */
    public void addSingleRow(Object[] rowObjs) throws Exception {
        if (rowObjs == null || rowObjs.length == 0) {
            throw new Exception("DataTable初始化对象数组不能为Null或者length为0");
        }
        if (rowObjs.length != this.rows.get(0).length) {
            throw new Exception("新插入的对象数组必须等于数据表DataTable的rows的长度");
        }
        this.rows.add(rowObjs);
    }

    /**
     * 加入一行数据，动态数组方式；
     *
     * @param rowObjs 动态数组
     */
    public void addSingleRow(ArrayList<Object> rowObjs) throws Exception {
        if (rowObjs != null && rowObjs.size() == 0) {
            throw new Exception("加入的行对象不能为空或者size为0");
        }
        if (rowObjs.size() != this.rows.get(0).length) {
            throw new Exception("新插入的对象数组必须等于数据表DataTable的rows的长度");
        }
        Object[] addedRow = new Object[rowObjs.size()];
        for (int i = 0, length = addedRow.length; i < length; i++) {
            addedRow[i] = rowObjs.get(i);
        }
        this.rows.add(addedRow);
    }

    /**
     * 加入多行数据；
     *
     * @param rows 内容动态数组
     */
    public void addAll(ArrayList<Object[]> rows) {
        this.rows.addAll(rows);
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
        this.rows = new ArrayList<>(dataArr.length);
        if (dataArr != null && dataArr.length > 0) {
            for (int k = 0; k < dataArr.length; k++) {
                this.rows.add(dataArr[k]);
            }
        }
    }

    /**
     * 删除索引行数
     * */
    public Object[] removeRowAtIndex(int index) {
        return this.rows.remove(index);
    }

    /**
     * 删除索引行数，
     *
     * @param Start 开始下标
     * @param End 结束下标
     * */
    public List<Object[]> removeRowFromStartToEnd(int Start, int End) {
        List<Object[]> theRows = new ArrayList<>();
        int rowSize = this.getRowSize();
        if (Start >= rowSize
                || End >= rowSize) {
            throw new ArrayIndexOutOfBoundsException("start,end不能超过row的size");
        }
        for (int i = Start, j = Start; i < End; i++) {
            theRows.add(this.rows.remove(j));
        }
        return theRows;
    }

    public boolean equals(DataTable dataTable) {
        if (dataTable == null
                || this.columns.size() != dataTable.getColumnsSize()
                    || this.rows.size() != dataTable.getRowSize()) {
            return false;
        }
        if (!this.columns.equals(dataTable.getColumns())) {
            return false;
        }
        boolean finalResult = true;
        {
            for (int i = 0; i < this.rows.size(); i++) {
                Object[] theRow = rows.get(i);
                Object[] otherRow = dataTable.getRowAtIndex(i);
                if (theRow.length != otherRow.length) {
                    finalResult = false;
                    break;
                }
                for (int j = 0; j < theRow.length; j++) {
                    if (!theRow[j].equals(otherRow[j])) {
                        finalResult = false;
                        break;
                    }
                }
                if (!finalResult) {
                    break;
                }
            }
        }
        return finalResult;
    }

    public DataTable clone() throws CloneNotSupportedException {
        DataTable cloneObject = new DataTable();
        cloneObject.setColumns(new ArrayList<>());
        cloneObject.setRows(new ArrayList<>());
        Iterator iterator = this.getColumns().iterator();
        while (iterator.hasNext()) {
            cloneObject.getColumns().add((String) iterator.next());
        }
        cloneObject.addAll(this.getRows());
        return cloneObject;
    }

    public String toString() {
        return null;
    }

    public int getRowSize() {
        return this.rows.size();
    }

    public int getColumnsSize() {
        return this.columns.size();
    }

    public Object[] getRowAtIndex(int index) {
        return this.rows.get(index);
    }

    public ArrayList<String> getColumns() {
        return this.columns;
    }

    public ArrayList<Object[]> getRows() {
        return this.rows;
    }

    public void setColumns(ArrayList<String> columns) {
        this.columns = columns;
    }

    public void setRows(ArrayList<Object[]> rows) {
        this.rows = rows;
    }

    public Iterator<Object[]> iterator() {
        return new Iter();
    }

    public class Iter implements Iterator<Object[]> {

        private int cursor;

        @Override
        public boolean hasNext() {
            return cursor != getRowSize();
        }

        @Override
        public Object[] next() {
            int i = cursor;
            if (i >= getRowSize()) {
                throw new NoSuchElementException();
            } else {
                cursor++;
                return getRowAtIndex(i);
            }
        }

        @Override
        public void remove() {
            int i = cursor;
            if (i >= getRowSize()) {
                throw new NoSuchElementException();
            } else {
                removeRowAtIndex(i);
            }
        }
    }
}
