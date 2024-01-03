package com.jevy.testbed;

import com.jevy.ecs.annotation.FunctionalSystem;
import com.jevy.testbed.components.CustomResource;

public class Test {

    @FunctionalSystem(SchedulesMain.Startup.class)
    public void testSystem(CustomResource resource) {
        System.out.println(STR."[TEST][getResource][Startup] resource \{resource}");
    }
}
