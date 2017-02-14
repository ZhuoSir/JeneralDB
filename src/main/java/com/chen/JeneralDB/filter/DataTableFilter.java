package com.chen.JeneralDB.filter;

import java.util.List;

/**
 *
 * Created by sunny on 2017/1/2.
 */
public interface DataTableFilter extends Filter {

    public boolean accept(List<String> column, Object[] row);

}
