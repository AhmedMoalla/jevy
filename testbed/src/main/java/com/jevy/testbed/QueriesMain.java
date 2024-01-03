package com.jevy.testbed;

import com.jevy.ecs.ECSWorld;
import com.jevy.ecs.Query1;
import com.jevy.ecs.Query2;
import com.jevy.ecs.Schedule;
import com.jevy.ecs.annotation.Filter;
import com.jevy.ecs.annotation.FunctionalSystem;
import com.jevy.ecs.annotation.ScanSystems;
import com.jevy.testbed.components.Alive;
import com.jevy.testbed.components.EntityName;
import com.jevy.testbed.components.MarkedEntity;
import com.jevy.testbed.components.Position;

@ScanSystems
public class QueriesMain {

    public static void main(String[] args) {
        var world = new ECSWorld();
        world.createEntity(new EntityName("Without markers"), new Position(2, 3));
        world.createEntity(new EntityName("Marked"), new Position(1, 5), new MarkedEntity());
        world.createEntity(new EntityName("Marked alive"), new Position(1, 5), new MarkedEntity(), new Alive());
        world.createEntity(new EntityName("Alive"), new Position(1, 8), new Alive());

        Schedule schedule = Schedule.defaultSchedule();
        schedule.run(world);
    }

    @FunctionalSystem
    public void queries2Components(Query2<Position, EntityName> query) {
        query.stream().forEach(comps -> System.out.println(STR."[queries2Components][Startup] All Entities: \{comps.comp1()} + \{comps.comp2()}"));
    }

    @FunctionalSystem
    public void marked(@Filter(with = MarkedEntity.class) Query1<EntityName> query) {
        query.stream().forEach(comps -> System.out.println(STR."[marked][Update] Marked Entities: \{comps}"));
    }

    @FunctionalSystem
    public void notAlive(@Filter(without = Alive.class) Query1<EntityName> query) {
        query.stream().forEach(comps -> System.out.println(STR."[notAlive][Update] Without Alive Entities: \{comps}"));
    }

    @FunctionalSystem
    public void markedNotAlive(@Filter(with = MarkedEntity.class, without = Alive.class) Query1<EntityName> query) {
        query.stream().forEach(comps -> System.out.println(STR."[markedNotAlive][Update] Without Alive Marked Entities: \{comps}"));
    }
}
