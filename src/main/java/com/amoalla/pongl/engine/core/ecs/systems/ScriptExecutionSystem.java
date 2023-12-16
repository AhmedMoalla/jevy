package com.amoalla.pongl.engine.core.ecs.systems;

import com.amoalla.pongl.engine.core.ecs.components.Script;
import dev.dominion.ecs.api.Dominion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScriptExecutionSystem implements Runnable {

    private final Dominion ecs;

    @Override
    public void run() {
        ecs.findEntitiesWith(Script.class)
                .forEach(script -> script.comp().script().accept(script.entity()));
    }
}
