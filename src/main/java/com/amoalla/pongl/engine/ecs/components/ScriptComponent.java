package com.amoalla.pongl.engine.ecs.components;

import dev.dominion.ecs.api.Entity;

import java.util.function.Consumer;

// TODO replace when Groovy script implemented
public record ScriptComponent(Consumer<Entity> script) {
}
