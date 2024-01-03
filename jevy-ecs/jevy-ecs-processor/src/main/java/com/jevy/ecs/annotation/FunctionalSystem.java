package com.jevy.ecs.annotation;

import com.jevy.ecs.DefaultScheduleLabel;
import com.jevy.ecs.ScheduleLabel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface FunctionalSystem {
    Class<? extends ScheduleLabel> value() default DefaultScheduleLabel.class;
}