package com.jevy.ecs.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ScanSystems {
    String packageName() default "";
}
