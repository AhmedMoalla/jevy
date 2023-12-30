package com.amoalla.pongl.test;

public class WorldRunner {

    public static final String SCHEDULER_INITIALIZER_PACKAGE = "com.amoalla.pongl.test";
    public static final String SCHEDULER_INITIALIZER_CLASS_NAME = "DefaultScheduleInitializer";
    public static final String DEFAULT_INITIALIZER_IMPLEMENTATION_CLASS = SCHEDULER_INITIALIZER_PACKAGE + "." + SCHEDULER_INITIALIZER_CLASS_NAME;

    private final ScheduleInitializer initializer;

    public WorldRunner(ECSWorld world) {
        this.initializer = loadInitializer();
        for (Schedule schedule : Schedule.values()) {
            initializer.initialize(world, schedule);
        }
    }

    private ScheduleInitializer loadInitializer() {
        try {
            Class<?> clazz = Class.forName(DEFAULT_INITIALIZER_IMPLEMENTATION_CLASS);
            return (ScheduleInitializer) clazz.getDeclaredConstructors()[0].newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        for (Schedule schedule : Schedule.values()) {
            schedule.run();
        }
    }
}