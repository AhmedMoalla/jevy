package com.amoalla.pongl.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ECSSystem
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface FunctionalSystem {
    String FQN = "com.amoalla.pongl.test.FunctionalSystem";
    Schedule value() default Schedule.None;
}