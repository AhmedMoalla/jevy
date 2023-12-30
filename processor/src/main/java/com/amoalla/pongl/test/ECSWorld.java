package com.amoalla.pongl.test;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;

import java.util.List;

public class ECSWorld {

    private final Dominion dominion = Dominion.create();
    private final Entity resources;

    public ECSWorld() {
        resources = dominion.createEntity();
    }

    public void addResource(Object object) {
        // store in dominion later
        resources.add(object);
    }

    public <T> T getResource(Class<T> clazz) {
        return resources.get(clazz);
    }

    public Entity createEntity(List<Object> components) {
        return dominion.createEntity(components.toArray());
    }

    public Entity createEntity(Object... components) {
        return dominion.createEntity(components);
    }

    public Dominion dominion() {
        return dominion;
    }
}