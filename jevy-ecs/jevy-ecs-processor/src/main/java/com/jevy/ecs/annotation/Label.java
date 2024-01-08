package com.jevy.ecs.annotation;

import com.jevy.ecs.systemset.*;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Label {
    Class<? extends SystemLabel>[] value();
}