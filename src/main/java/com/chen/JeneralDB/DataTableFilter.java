package com.chen.JeneralDB;

import java.util.List;

/**
 * Created by sunny on 2017/1/2.
 */
public interface DataTableFilter {

    public boolean accept(List<String> column, Object[] row);

}
