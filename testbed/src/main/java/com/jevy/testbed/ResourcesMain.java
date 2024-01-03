package com.jevy.testbed;

import com.jevy.ecs.ECSWorld;
import com.jevy.ecs.Schedule;
import com.jevy.ecs.annotation.FunctionalSystem;
import com.jevy.ecs.annotation.ScanSystems;
import com.jevy.testbed.components.CustomResource;

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
