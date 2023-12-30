package com.amoalla.pongl.test;

import java.util.ArrayList;
import java.util.List;

public enum Schedule {
    None,
    Startup,
    Update;

    private final List<SystemRunner> systems = new ArrayList<>();

    public void addSystems(SystemRunner system) {
        if (this == None) throw new IllegalStateException("Cannot add system to the 'None' schedule.");
        systems.add(system);
    }

    public void run() {
        // Run in parallel if possible or build a graph using the dependencies of each system
        // to decide what to run safely in parallel and what to run sequentially
        systems.forEach(SystemRunner::run);
    }
}