package com.chen.JeneralDB.annotation;

import java.lang.annotation.*;

/**
 * Created by sunny on 17-6-16.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {

    public String value() default "";

}
