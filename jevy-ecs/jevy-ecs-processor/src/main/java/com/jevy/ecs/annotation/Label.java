package com.jevy.ecs.annotation;

import com.jevy.ecs.systemset.*;

public @interface Label {
    Class<? extends SystemLabel>[] value();
}