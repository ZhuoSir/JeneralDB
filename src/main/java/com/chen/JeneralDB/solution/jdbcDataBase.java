package com.chen.JeneralDB.solution;

import com.chen.JeneralDB.jdbc.FieldDefinition;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by sunny on 2017/1/14.
 */
public interface jdbcDataBase {

    boolean createTable(String tableName, List<FieldDefinition> fieldDefinitions) throws SQLException;

    boolean clearTable(final String tableName, final boolean isdrop) throws SQLException;

}
