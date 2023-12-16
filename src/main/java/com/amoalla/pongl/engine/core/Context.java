package com.amoalla.pongl.engine.core;

import com.amoalla.pongl.engine.gfx.Window;

public record Context(Window window, dev.dominion.ecs.api.Dominion ecs) {
}
