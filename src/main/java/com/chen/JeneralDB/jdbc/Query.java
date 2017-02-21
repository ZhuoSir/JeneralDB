package com.chen.JeneralDB.jdbc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sunny on 2017/1/15.
 */
public final class Query {

    private String tableName;

    private String[] fields;

    private int pageNo = -1;

    private int pageSize;

    private String top;

    private Map<String, SortDirection> sorts = new LinkedHashMap<>();

    private Map<String, String> equals = new LinkedHashMap<>();

    private Map<String, String> notEquals = new LinkedHashMap<>();

    private Map<String, String> likes = new LinkedHashMap<>();

    private Map<String, String> notLikes = new LinkedHashMap<>();

    private Map<String, Object[]> in = new LinkedHashMap<>();

    private Map<String, Object[]> notIn = new LinkedHashMap<>();

    private Map<String, Object[]> betweens = new LinkedHashMap<>();

    private Map<String, Object[]> notBetwee = new LinkedHashMap<>();

    public Query equal(String index, String value) {
        equals.put(index, value);
        return this;
    }

    public Query notequal(String index, String value) {
        notEquals.put(index, value);
        return this;
    }

    public Query like(String index, String value) {
        likes.put(index, value);
        return this;
    }

    public Query notlike(String index, String value) {
        notLikes.put(index, value);
        return this;
    }

    public Query sort(String index, SortDirection direction) {
        sorts.put(index, direction);
        return this;
    }

    public Query top(String top) {
        if (null != top && "".equals(top)) {
            this.top = top;
        }

        return this;
    }

    public Query in(String index, String[] values) {
        in.put(index, values);
        return this;
    }

    public Query notin(String index, String[] values) {
        notIn.put(index, values);
        return this;
    }

    public Query between(String index, String A, String B) {
        String[] AB = {A, B};
        betweens.put(index, AB);
        return this;
    }

    public Query notbetween(String index, String A, String B) {
        String[] AB = {A, B};
        notBetwee.put(index, AB);
        return this;
    }

    public Query page(int pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public Query page(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        return this;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String[] getFields() {
        return fields;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public String getTop() {
        return top;
    }

    public Map<String, SortDirection> getSorts() {
        return sorts;
    }

    public Map<String, String> getEquals() {
        return equals;
    }

    public Map<String, String> getNotEquals() {
        return notEquals;
    }

    public Map<String, String> getLikes() {
        return likes;
    }

    public Map<String, String> getNotLikes() {
        return notLikes;
    }

    public Map<String, Object[]> getIn() {
        return in;
    }

    public Map<String, Object[]> getNotIn() {
        return notIn;
    }

    public Map<String, Object[]> getBetweens() {
        return betweens;
    }

    public Map<String, Object[]> getNotBetwee() {
        return notBetwee;
    }

    public boolean isEmpty() {

        if (null != tableName && !"".equals(tableName)) {
            return false;
        }

        if (null != top && !"".equals(top)) {
            return false;
        }

        if (!sorts.isEmpty()) {
            return false;
        }

        if (!equals.isEmpty()) {
            return false;
        }

        if (!notEquals.isEmpty()) {
            return false;
        }

        if (!likes.isEmpty()) {
            return false;
        }

        if (!notLikes.isEmpty()) {
            return false;
        }

        if (!in.isEmpty()) {
            return false;
        }

        if (!notIn.isEmpty()) {
            return false;
        }

        if (!betweens.isEmpty()) {
            return false;
        }

        if (!notBetwee.isEmpty()) {
            return false;
        }

        return true;
    }
}
