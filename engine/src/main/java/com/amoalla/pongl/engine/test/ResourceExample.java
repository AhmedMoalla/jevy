package com.amoalla.pongl.engine.test;

import com.amoalla.pongl.test.ECSWorld;
import com.amoalla.pongl.test.FunctionalSystem;
import com.amoalla.pongl.test.Schedule;

import java.util.random.RandomGenerator;

public class ResourceExample {
    public static void main(String[] args) {
        var world = new ECSWorld();
        world.addResource(new Counter());

        var schedule = Schedule.defaultSchedule();
        for (int i = 1; i <= 10; i++) {
            System.out.println(STR."Simulating frame \{i}/10");
            schedule.run(world);
        }
    }

    @FunctionalSystem
    public void increaseCounter(Counter counter) {
        if (RandomGenerator.getDefault().nextBoolean()) {
            counter.value += 1;
            System.out.println("\tIncreased counter value");
        }
    }

    @FunctionalSystem
    public void printCounter(Counter counter) {
        System.out.println(STR."\t\{counter.value}");
    }

    public static class Counter {
        int value = 0;
    }
}
