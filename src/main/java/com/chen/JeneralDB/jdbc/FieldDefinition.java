package com.chen.JeneralDB.jdbc;

/**
 * Created by sunny on 2017/1/14.
 */
public class FieldDefinition {

    /**
     * field name
     */
    private String name;

    /**
     * field type
     */
    private String type;

    /**
     * field length;
     */
    private Integer length;

    /**
     * field precision
     */
    private Integer precision;

    /**
     * if is key
     */
    private Boolean isKey;

    /**
     * if null-able
     */
    private Boolean nullable = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Boolean getKey() {
        return isKey;
    }

    public void setKey(Boolean key) {
        isKey = key;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }
}
