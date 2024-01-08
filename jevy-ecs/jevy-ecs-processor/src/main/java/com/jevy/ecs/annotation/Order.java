package com.jevy.ecs.annotation;

import com.jevy.ecs.systemset.*;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Order {
    String[] before() default "";

    String[] after() default "";

    Class<? extends SystemLabel>[] beforeLabel() default DefaultSystemLabel.class;

    Class<? extends SystemLabel>[] afterLabel() default DefaultSystemLabel.class;
}