package com.amoalla.pongl.engine.core.ecs.components;

import dev.dominion.ecs.api.Entity;

import java.util.function.Consumer;

// TODO replace when Groovy script implemented
public record Script(Consumer<Entity> script) {
}
