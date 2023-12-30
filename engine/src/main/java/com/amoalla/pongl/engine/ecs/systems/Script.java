package com.amoalla.pongl.engine.ecs.systems;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;

public interface Script {
    void init(Entity entity, Dominion ecs);
    void run(Entity entity);
}
