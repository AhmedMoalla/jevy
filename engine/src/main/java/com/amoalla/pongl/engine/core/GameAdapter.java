package com.amoalla.pongl.engine.core;

import com.amoalla.pongl.engine.gfx.Renderer;
import com.amoalla.pongl.engine.gfx.Window;
import dev.dominion.ecs.api.Dominion;

public class GameAdapter implements Game {

    protected Window window;
    protected Dominion ecs;

    @Override
    public void init(Context context) {
        window = context.window();
        ecs = context.ecs();
    }

    @Override
    public void update() {

    }

    @Override
    public void fixedUpdate() {

    }

    @Override
    public void render(Renderer renderer) {

    }

    @Override
    public void onKeyPress(int key, int scancode, int mods) {

    }

    @Override
    public void onKeyRepeat(int key, int scancode, int mods) {

    }

    @Override
    public void onKeyRelease(int key, int scancode, int mods) {

    }

    @Override
    public void close() {

    }
}
