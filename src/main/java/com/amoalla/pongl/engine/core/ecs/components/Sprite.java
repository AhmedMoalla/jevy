package com.amoalla.pongl.engine.core.ecs.components;

import com.amoalla.pongl.engine.gfx.Texture2D;

import java.awt.*;

public record Sprite(Texture2D texture, Color tint) {
    public Sprite(Texture2D texture) {
        this(texture, Color.WHITE);
    }

    public Sprite(Color color) {
        this(null, color);
    }
}