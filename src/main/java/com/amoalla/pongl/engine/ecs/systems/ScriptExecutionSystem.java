package com.amoalla.pongl.engine.ecs.systems;

import com.amoalla.pongl.engine.ecs.components.ScriptComponent;
import dev.dominion.ecs.api.Dominion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScriptExecutionSystem implements Runnable {

    private final Dominion ecs;

    @Override
    public void run() {
        ecs.findEntitiesWith(ScriptComponent.class)
                .forEach(script -> script.comp().script().accept(script.entity()));
    }
}
