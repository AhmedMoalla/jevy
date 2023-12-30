package com.amoalla.pongl.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Filter {

    String FQN = "com.amoalla.pongl.test.Filter";
    // synonym of with()
    Class<?>[] value() default NoFilters.class;

    Class<?>[] with() default NoFilters.class;

    Class<?>[] without() default NoFilters.class;

    record NoFilters() {
    }
}