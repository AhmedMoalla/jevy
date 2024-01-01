package com.amoalla.pongl.engine.test;

import com.amoalla.pongl.test.*;

public class Main2 {

    public static void main(String[] args) {
        // TODO add way to query with State using World#withState
        var world = new ECSWorld();
        world.addResource(new CustomResource("test"));
        world.addResource(new AnotherResource("another"));
        world.createEntity(new EntityName("Without markers"), new Position(2, 3));
        world.createEntity(new EntityName("Marked"), new Position(1, 5), new MarkedEntity());
        world.createEntity(new EntityName("Marked alive"), new Position(1, 5), new MarkedEntity(), new Alive());
        world.createEntity(new EntityName("Alive"), new Position(1, 8), new Alive());

        Schedule startup = Schedule.fromLabel(Startup.class);
        startup.run(world);

        Schedule update = Schedule.fromLabel(Update.class);
        update.run(world);
    }

    public record Startup() implements ScheduleLabel { }

    public record Update() implements ScheduleLabel { }

    @FunctionalSystem(Startup.class)
    public void single(CustomResource resource) {
//        System.out.println(STR."[single][Startup] From system: \{resource}");
    }

    @FunctionalSystem(Startup.class)
    public void all(CustomResource resource, Query2<Position, EntityName> query) {
//        System.out.println(STR."[all][Startup] From system: \{resource}");
        query.stream().forEach(comps -> System.out.println("[all][Startup] All Entities: " + comps.comp1() + " + " + comps.comp2()));
    }

    @FunctionalSystem(Update.class)
    public void marked(CustomResource resource,
                       AnotherResource another,
                       @Filter(with = MarkedEntity.class) Query1<EntityName> query) {
//        System.out.println(STR."[marked][Update] From system: \{resource}");
//        System.out.println(STR."[marked][Update] From system: \{another}");
        query.stream().forEach(comps -> System.out.println("[marked][Update] Marked Entities: " + comps));
    }

    @FunctionalSystem(Update.class)
    public void notAlive(@Filter(without = Alive.class) Query1<EntityName> query) {
        query.stream().forEach(comps -> System.out.println("[notAlive][Update] Without Alive Entities: " + comps));
    }

    @FunctionalSystem(Update.class)
    public void markedNotAlive(@Filter(with = MarkedEntity.class, without = Alive.class) Query1<EntityName> query) {
        query.stream().forEach(comps -> System.out.println("[markedNotAlive][Update] Without Alive Marked Entities: " + comps));
    }

    public static class AnotherResource {
        String test;

        AnotherResource(String test) {
            this.test = test;
        }
    }

    public record EntityName(String name) {
    }

    public record Position(int x, int y) {
    }

    public record MarkedEntity() {
    }

    public record Alive() {
    }
}
