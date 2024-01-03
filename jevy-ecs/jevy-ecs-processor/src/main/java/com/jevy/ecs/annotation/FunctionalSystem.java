package com.jevy.ecs.annotation;

import com.jevy.ecs.schedule.*;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface FunctionalSystem {
    Class<? extends ScheduleLabel> value() default DefaultScheduleLabel.class;
}