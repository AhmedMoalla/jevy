package com.jevy.testbed;

import com.jevy.ecs.ECSWorld;
import com.jevy.ecs.Schedule;
import com.jevy.ecs.annotation.FunctionalSystem;
import com.jevy.testbed.components.*;

public class GlobalMain {

    public static void main(String[] args) {
        var world = new ECSWorld();
        world.addResource(new CustomResource("test"));
        world.createEntity(new EntityName("Without markers"), new Position(2, 3));
        world.createEntity(new EntityName("Marked"), new Position(1, 5), new MarkedEntity());
        world.createEntity(new EntityName("Marked alive"), new Position(1, 5), new MarkedEntity(), new Alive());
        world.createEntity(new EntityName("Alive"), new Position(1, 8), new Alive());
        Schedule schedule = Schedule.defaultSchedule();
         schedule.run(world);

    }

    @FunctionalSystem
    public void global(CustomResource resource) {
        System.out.println(STR."[requiresResource] From system: \{resource}");
    }
}
