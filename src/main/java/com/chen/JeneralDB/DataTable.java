package com.chen.JeneralDB;

import com.chen.JeneralDB.filter.DataTableFilter;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by sunny on 2016/11/29.
 */
public class DataTable {

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
     */
    public DataTable(ArrayList<String> columnNames) {
        int size = columnNames.size();
        if (columnNames != null && size > 0) {
            this.columns = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                this.columns.add(columnNames.get(i));
            }
        }

        this.rows = new ArrayList<>(10);
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
    public <T> DataTable(List<T> list, Class<T> tClass) throws Exception {
        if (list == null || list.isEmpty()) {
            throw new Exception("DataTable初始化list不能为Null或者size为0");
        }
        String[] colArr;
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
                Object value = field.get(obj);
                dataArr[i][j] = value != null ? value : null;
            }
        }

        this.columns = new ArrayList<>(colArr.length);
        if (colArr.length > 0) {
            for (int c = 0; c < colArr.length; c++) {
                this.columns.add(colArr[c]);
            }
        }

        this.rows = new ArrayList<>(dataArr.length);
        if (dataArr.length > 0) {
            for (int k = 0; k < dataArr.length; k++) {
                this.rows.add(dataArr[k]);
            }
        }
    }

    public DataTable(List<Map<String, Object>> list)
            throws Exception {
        if (list == null || list.isEmpty()) {
            throw new Exception("DataTable初始化list不能为Null或者size为0");
        }
        String[] colArr = null;
        {
            Map<String, Object> temp = list.get(0);
            Set<String> key = temp.keySet();
            colArr = new String[key.size()];
            Iterator<String> iterator = key.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                colArr[i] = iterator.next();
                i++;
            }
        }

        int size = list.size();
        Object[][] dataArr = new Object[size][colArr.length];
        {
            for (int i = 0; i < size; i++) {
                Map<String, Object> map = list.get(i);
                for (int j = 0; j < colArr.length; j++) {
                    String key = colArr[j];
                    dataArr[i][j] = map.get(key);
                }
            }
        }

        this.columns = new ArrayList<>(colArr.length);
        if (colArr.length > 0) {
            for (int c = 0; c < colArr.length; c++) {
                this.columns.add(colArr[c]);
            }
        }

        this.rows = new ArrayList<>(dataArr.length);
        if (dataArr.length > 0) {
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

        if (colArr.length > 0) {
            for (int c = 0; c < colArr.length; c++) {
                this.columns.add(colArr[c]);
            }
        }

        this.rows = new ArrayList<>(0);
    }

    public DataTable(Object obj) throws IllegalAccessException {
        String[] colArr;
        Object[] rowArr;
        Class<?> ownerClass = obj.getClass();
        Field[] fields = ownerClass.getDeclaredFields();
        {
            colArr = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                colArr[i] = fields[i].getName();
            }
        }

        this.columns = new ArrayList<>(colArr.length);
        this.columns.addAll(Arrays.asList(colArr));
        this.rows = new ArrayList<>(1);
        rowArr = new Object[colArr.length];

        for (int j = 0; j < fields.length; j++) {
            fields[j].setAccessible(true);
            Object value = fields[j].get(obj);
            rowArr[j] = value;
        }

        this.rows.add(rowArr);
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
        List<String> columns = this.getColumns();

        for (String column : columns) {
            cloneObject.getColumns().add(column);
        }

        cloneObject.addAll(this.getRows());

        return cloneObject;
    }

    public String toJSON() {
        return null;
    }

    public void print() {
        DBUtil.print("DataTable内容输出：");
        int columnSize = getColumnsSize();
        List<String> column = getColumns();

        StringBuffer table = new StringBuffer();
        StringBuffer horizontaLine = new StringBuffer("-------------------");
        StringBuffer verticalLine = new StringBuffer("|");
        int strCapacity = 18;
        String format = "%-" + strCapacity + "s";

        addLine(table, horizontaLine, columnSize);
        table.append(verticalLine);

        for (int i = 0; i < columnSize; i++) {
            table.append(String.format(format, column.get(i)));
            table.append(verticalLine);
        }

        table.append("\n");
        addLine(table, horizontaLine, columnSize);

        int rowSize = this.getRowSize();
        for (int i = 0; i < rowSize; i++) {
            Object[] data = this.getRowAtIndex(i);
            table.append(verticalLine);

            for (int j = 0; j < data.length; j++) {

                if (data[j] != null) {
                    String dataStr = null;

                    try {
                        dataStr = checkCapacity(data[j].toString(), strCapacity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    table.append(String.format(format, dataStr));
                } else {
                    table.append(String.format(format, "null"));
                }

                table.append(verticalLine);
            }

            table.append("\n");
            addLine(table, horizontaLine, columnSize);
        }

        System.out.println(table.toString());
    }

    private String checkCapacity(String str, int capacity)
            throws Exception {
        if (str.length() > capacity) {
            return str.substring(0, capacity);
        } else {
            return str;
        }
    }

    private void addLine(StringBuffer buffer,
                         StringBuffer addBuffer,
                         int number) {
        for (int i = 0; i < number; i++) {
            buffer.append(addBuffer);
        }

        buffer.append("\n");
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

    /**
     * 获取迭代器
     */
    public Iterator<Object[]> iterator() {
        return new Iter();
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

        if (!this.rows.isEmpty() && rowObjs.length != this.rows.get(0).length) {
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

    /**
     * 加入一列
     *
     * @param columnName 列名
     */
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

        if (dataArr.length > 0) {
            for (int k = 0; k < dataArr.length; k++) {
                this.rows.add(dataArr[k]);
            }
        }
    }

    /**
     * 删除索引行数
     */
    public Object[] removeRowAtIndex(int index) {
        return this.rows.remove(index);
    }

    /**
     * 删除索引行数，
     *
     * @param Start 开始下标
     * @param End   结束下标
     */
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

    /**
     * 将DataTable转成BeanList
     *
     * @param beanClass 对象类
     */
    public <T> List<T> toBeanList(Class<T> beanClass)
            throws Exception {
        int rowSize = this.getRowSize();
        List<T> beanList = new ArrayList<>(rowSize);

        for (int i = 0; i < rowSize; i++) {
            T t = beanClass.newInstance();
            Field[] fields = t.getClass().getDeclaredFields();

            for (int j = 0; j < fields.length; j++) {
                fields[j].setAccessible(true);
                String rowName = fields[j].getName();
                Object value;

                try {
                    value = getObjectByColumnNameInRow(rowName, i);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    System.err.println("DataTable中没有" + rowName + "列");
                    continue;
                }

                setValues(t, fields[j] , value);
            }

            beanList.add(t);
        }

        return beanList;
    }

    /**
     * 根据类型，给Field设置值
     * */
    private <T> void setValues(T t, Field field, Object value)
            throws IllegalAccessException {
        String type = field.getType().toString();

        if (null == value) {
            Object defaultValue = null;

            switch (type) {
                case "int":
                case "Integer":
                    defaultValue = 0;
                    break;
                case "long":
                case "Long":
                    defaultValue = 0L;
                    break;
                case "float":
                case "Float":
                    defaultValue = 0f;
            }

            field.set(t, defaultValue);
            return;
        }

        field.set(t, value);
    }

    /**
     * 根据列名获取一系列对象
     *
     * @param rowName 列名
     */
    public Object[] getObjectsByColumnName(String rowName) {
        int size = this.getRowSize();
        Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            Object value;

            try {
                value = getObjectByColumnNameInRow(rowName, i);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("dataTable中没有" + rowName + "列");
                e.printStackTrace();
                break;
            }

            values[i] = value;
        }

        return values;
    }

    /**
     * 根据列名获取某一行中的某一列的对象
     *
     * @param rowName 列名
     * @param index   行数
     */
    public Object getObjectByColumnNameInRow(String rowName, int index) {
        Object[] valuesOfRowAtIndex = this.getRowAtIndex(index);
        int indexOfColumnName = getIndexOfColumnNameInColumn(rowName);

        return valuesOfRowAtIndex[indexOfColumnName];
    }

    /**
     * 获取列名的序号
     *
     * @param rowName 列名
     */
    public int getIndexOfColumnNameInColumn(String rowName) {
        return this.columns.indexOf(rowName);
    }

    /**
     * 根据坐标获取对象
     *
     * @param x X轴值
     * @param y Y轴值
     */
    public Object getObjectAtCoordinate(int x, int y) {
        if ((x + 1) > getColumnsSize()) {
            System.err.println("X值超过了dataTable中列名的size");
            return null;
        }

        if ((y + 1) > getRowSize()) {
            System.err.println("Y值超过了dataTable中数据的size");
            return null;
        }

        Object[] rows = getRowAtIndex(y);

        return rows[x];
    }

    /**
     * DataTable过滤方法
     *
     * @param filter 过滤器接口
     * @return 过滤结果DataTable
     */
    public DataTable filter(DataTableFilter filter) {
        ArrayList<String> columns = this.getColumns();
        DataTable dataTable = new DataTable(columns);

        for (int i = 0; i < this.getRowSize(); i++) {
            Object[] row = getRowAtIndex(i);

            if (filter.accept(columns, row)) {
                try {
                    dataTable.addSingleRow(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return dataTable;
    }
}
