package com.amoalla.pongl.engine.ecs.systems;

import com.amoalla.pongl.engine.ecs.components.ScriptComponent;
import dev.dominion.ecs.api.Dominion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScriptExecutionSystem implements Runnable {

    private final Dominion ecs;
    private boolean initialized = false;

    public void init() {
        ecs.findEntitiesWith(ScriptComponent.class)
                .forEach(composition -> composition.comp().script().init(composition.entity(), ecs));
        initialized = true;
    }

    @Override
    public void run() {
        if (!initialized) throw new IllegalStateException("ScriptExecutionSystem was not initialized.");
        ecs.findEntitiesWith(ScriptComponent.class)
                .forEach(composition -> composition.comp().script().run(composition.entity()));
    }
}
