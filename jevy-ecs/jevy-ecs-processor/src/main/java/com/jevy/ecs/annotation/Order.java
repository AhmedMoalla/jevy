package com.jevy.ecs.annotation;

import com.jevy.ecs.systemset.*;

public @interface Order {
    String[] before() default "";

    String[] after() default "";

    Class<? extends SystemLabel>[] beforeLabel() default DefaultSystemLabel.class;

    Class<? extends SystemLabel>[] afterLabel() default DefaultSystemLabel.class;
}