package com.chen.JeneralDB.filter;

import com.chen.JeneralDB.filter.filter;

import java.util.List;

/**
 *
 * Created by sunny on 2017/1/2.
 */
public interface DataTableFilter extends filter {

    public boolean accept(List<String> column, Object[] row);

}
