package com.jevy.testbed;

import com.jevy.ecs.*;
import com.jevy.ecs.annotation.*;
import com.jevy.ecs.event.*;
import com.jevy.ecs.schedule.*;

import java.util.random.*;

@ScanSystems
public class EventsMain {

    public static void main(String[] args) {
        var world = new ECSWorld();
        Schedule schedule = Schedule.defaultSchedule();
        schedule.run(world);
    }
    public record MyEvent(String message, float randomValue) {
    }

    @FunctionalSystem
    public void sendingSystem(EventWriter<MyEvent> eventWriter) {
        System.out.println("[sendingSystem] " + eventWriter);
        if (true) {
            return;
        }
        float random = RandomGenerator.getDefault().nextFloat();
        if (random > 0.5f) {
            eventWriter.send(new MyEvent("A random event with value > 0.5", random));
        }
    }

    @FunctionalSystem
    public void receivingSystem(EventReader<MyEvent> eventReader) {
        System.out.println("[receivingSystem] " + eventReader);
        if (true) {
            return;
        }
        for (MyEvent event : eventReader.read()) {
            System.out.println(STR."\tReceived message \{event.message}, with random value of \{event.randomValue}");
        }
    }
}
