package com.chen.JeneralDB.annotation;

import java.lang.annotation.*;

/**
 * Created by sunny on 17-6-16.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {

    public String value() default "columnName";

    enum index {NORMAL, PRIMARYKEY, UNIQUE, FULLTEXT};

    index index() default index.NORMAL;
}
