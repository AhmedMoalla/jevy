package com.amoalla.pongl.engine.config;

import lombok.Builder;

@Builder
public record GameConfig(WindowConfig window, boolean debug) {
}