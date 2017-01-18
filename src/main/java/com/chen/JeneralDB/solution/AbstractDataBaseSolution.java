package com.chen.JeneralDB.solution;

import com.chen.JeneralDB.jdbc.FieldDefinition;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by sunny on 2017/1/15.
 */
public abstract class AbstractDataBaseSolution implements jdbcDataBase {

    @Override
    public boolean createTable(String tableName, List<FieldDefinition> fieldDefinitions) throws SQLException {


        return false;
    }


    @Override
    public boolean clearTable(String tableName, boolean isdrop) throws SQLException {
        return false;
    }

}
