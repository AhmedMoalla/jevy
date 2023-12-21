package com.amoalla.pongl.engine.core;

import com.amoalla.pongl.engine.gfx.Window;
import dev.dominion.ecs.api.Dominion;

public record Context(Window window, Dominion ecs) {
}
