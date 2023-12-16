package com.amoalla.pongl.engine.core;

import com.amoalla.pongl.engine.gfx.Renderer;

public interface Game extends AutoCloseable {
    void init(Context context);

    void update();

    void fixedUpdate();

    void render(Renderer renderer);

    void onKeyPress(int key, int scancode, int mods);

    void onKeyRepeat(int key, int scancode, int mods);

    void onKeyRelease(int key, int scancode, int mods);

    @Override
    void close();
}
