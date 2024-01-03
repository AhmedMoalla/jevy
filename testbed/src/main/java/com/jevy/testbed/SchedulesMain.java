package com.jevy.testbed;

import com.jevy.ecs.*;
import com.jevy.ecs.annotation.FunctionalSystem;
import com.jevy.ecs.annotation.ScanSystems;
import com.jevy.testbed.components.CustomResource;
import com.jevy.testbed.components.EntityName;
import com.jevy.testbed.components.Position;

@ScanSystems
public class SchedulesMain {

    public static void main(String[] args) {
        var world = new ECSWorld();
        world.addResource(new CustomResource("custom"));
        world.createEntity(new Position(1, 2), new EntityName("Entity 1"));

        Schedule startup = Schedule.fromLabel(Startup.class);
        startup.run(world);

        Schedule update = Schedule.fromLabel(Update.class);
        update.run(world);

        Schedule defaultSchedule = Schedule.defaultSchedule();
        defaultSchedule.run(world);
    }

    public record Startup() implements ScheduleLabel { }

    public record Update() implements ScheduleLabel { }

    @FunctionalSystem(Startup.class)
    public void getResource(CustomResource resource) {
        System.out.println(STR."[getResource][Startup] resource \{resource}");
    }

    @FunctionalSystem(Update.class)
    public void queries2Components(Query2<Position, EntityName> query) {
        query.stream().forEach(comps -> System.out.println(STR."[queries2Components][Update] All Entities: \{comps.comp1()} + \{comps.comp2()}"));
    }

    @FunctionalSystem(Update.class)
    public void query1Component(Query1<EntityName> query) {
        query.stream().forEach(comps -> System.out.println(STR."[query1Component][Update] All Entities: \{comps}"));
    }

    @FunctionalSystem
    public void requiresResource(CustomResource resource) {
        System.out.println(STR."[requiresResource] From system: \{resource}");
    }

}
