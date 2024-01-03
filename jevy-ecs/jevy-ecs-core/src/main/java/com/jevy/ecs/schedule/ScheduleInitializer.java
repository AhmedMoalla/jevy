package com.jevy.ecs.schedule;

import com.jevy.ecs.*;

import java.util.*;

public interface ScheduleInitializer {
    String SCHEDULER_INITIALIZER_PACKAGE = "com.jevy.ecs";
    String SCHEDULER_INITIALIZER_CLASS_NAME = "DefaultScheduleInitializer";
    String DEFAULT_INITIALIZER_IMPLEMENTATION_CLASS = SCHEDULER_INITIALIZER_PACKAGE + "." + SCHEDULER_INITIALIZER_CLASS_NAME;

    void initialize(ECSWorld world, Schedule schedule);

    static ScheduleInitializer defaultInitializer() {
        return ServiceLoader
                .load(ScheduleInitializer.class)
                .stream()
                .filter(p -> !p.type().getName().equals(DEFAULT_INITIALIZER_IMPLEMENTATION_CLASS))
                .findFirst()
                .map(ServiceLoader.Provider::get)
                .orElse(loadDefaultInitializer());
    }

    private static ScheduleInitializer loadDefaultInitializer() {
        try {
            Class<?> clazz = Class.forName(DEFAULT_INITIALIZER_IMPLEMENTATION_CLASS);
            return (ScheduleInitializer) clazz.getDeclaredConstructors()[0].newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}