package com.amoalla.pongl.engine.ecs.components;

import com.amoalla.pongl.engine.gfx.Texture2D;

import java.awt.*;

public record SpriteComponent(Texture2D texture, Color tint) {
    public SpriteComponent(Texture2D texture) {
        this(texture, Color.WHITE);
    }

    public SpriteComponent(Color color) {
        this(null, color);
    }
}