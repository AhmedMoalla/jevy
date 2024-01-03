package com.jevy.ecs.annotation;

import com.jevy.ecs.systemset.*;

public @interface SystemSet {
    Class<? extends SystemSetRulesProvider> value();
}