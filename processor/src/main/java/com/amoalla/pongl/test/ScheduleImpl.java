package com.amoalla.pongl.test;

import java.util.ArrayList;
import java.util.List;

public class ScheduleImpl implements Schedule {

    private static final ScheduleInitializer INITIALIZER = ScheduleInitializer.defaultInitializer();

    private final Class<? extends ScheduleLabel> label;
    private final List<SystemRunner> systems = new ArrayList<>();

    private boolean initialized = false;

    public ScheduleImpl(Class<? extends ScheduleLabel> label) {
        this.label = label;
    }

    @Override
    public Class<? extends ScheduleLabel> label() {
        return label;
    }

    @Override
    public void run(ECSWorld world) {
        if (!initialized) {
            INITIALIZER.initialize(world, this);
            initialized = true;
        }
        // Run in parallel if possible or build a graph using the dependencies of each system
        // to decide what to run safely in parallel and what to run sequentially
        systems.forEach(SystemRunner::run);
    }

    @Override
    public void addSystem(SystemRunner systemRunner) {
        systems.add(systemRunner);
    }
}