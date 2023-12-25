package com.amoalla.pongl.engine.core;

import com.amoalla.pongl.engine.gfx.Renderer;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class Layer implements AutoCloseable {
    @Getter
    private final String name;

    public Layer(String name) {
        this.name = name;
    }

    public abstract void init();

    public abstract void update();

    public abstract void render(Renderer renderer);
}
