package com.amoalla.pongl.engine.test;

import com.amoalla.pongl.test.*;
import dev.dominion.ecs.api.Dominion;

public class Main2 {

    public static void main(String[] args) {
        var world = new ECSWorld();
        world.addResource(new CustomResource("test"));
        world.addResource(new AnotherResource("another"));
        world.createEntity(new EntityName("Without markers"), new Position(2, 3));
        world.createEntity(new EntityName("Marked"), new Position(1, 5), new MarkedEntity());
        world.createEntity(new EntityName("Marked alive"), new Position(1, 5), new MarkedEntity(), new Alive());
        world.createEntity(new EntityName("Alive"), new Position(1, 8), new Alive());
        System.out.println(STR."From world: \{world.getResource(CustomResource.class)}");
        WorldRunner runner = new WorldRunner(world);
        runner.run();
    }

    @FunctionalSystem(Schedule.Startup)
    public void simple(CustomResource resource) {
        System.out.println(STR."[simple][Startup] From system: \{resource}");
    }

    @FunctionalSystem(Schedule.Startup)
    public void setup(CustomResource resource, Query2<Position, EntityName> query) {
        System.out.println(STR."[setup][Startup] From system: \{resource}");
        query.stream().forEach(comps -> System.out.println("[setup][Startup] All Entities: " + comps.comp1() + " + " + comps.comp2()));
    }

    @FunctionalSystem(Schedule.Update)
    public void update(CustomResource resource,
                              AnotherResource another,
                              @Filter(with = MarkedEntity.class) Query1<EntityName> query) {
        System.out.println(STR."[update][Update] From system: \{resource}");
        System.out.println(STR."[update][Update] From system: \{another}");
        query.stream().forEach(comps -> System.out.println("[update][Update] Marked Entities: " + comps));
    }

    @FunctionalSystem(Schedule.Update)
    public void another(@Filter(without = Alive.class) Query1<EntityName> query) {
        query.stream().forEach(comps -> System.out.println("[another][Update] Without Alive Entities: " + comps));
    }

    @FunctionalSystem(Schedule.Update)
    public void anothera(@Filter(with = MarkedEntity.class, without = Alive.class) Query1<EntityName> query) {
        query.stream().forEach(comps -> System.out.println("[anothera][Update] Without Alive Marked Entities: " + comps));
    }

    public static class AnotherResource {
        String test;

        AnotherResource(String test) {
            this.test = test;
        }
    }

    public record EntityName(String name) {}

    public record Position(int x, int y) {
    }

    public record MarkedEntity() {}

    public record Alive() {}
}

//enum CustomSchedule {
//    TestSchedule
//}
//
//@ECSSystem
//@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.SOURCE)
//@interface RunOn {
//    CustomSchedule value();
//}

