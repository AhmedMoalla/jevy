package com.jevy.testbed;

import com.jevy.ecs.*;
import com.jevy.ecs.annotation.*;
import com.jevy.ecs.schedule.*;
import com.jevy.testbed.components.*;

@ScanSystems
public class ResourcesMain {

    public static void main(String[] args) {
        var world = new ECSWorld();
        world.addResource(new CustomResource("test"));

        Schedule schedule = Schedule.defaultSchedule();
        schedule.run(world);
    }

    @FunctionalSystem
    public void requiresResource(CustomResource resource) {
        System.out.println(STR."[requiresResource] From system: \{resource}");
    }

}
