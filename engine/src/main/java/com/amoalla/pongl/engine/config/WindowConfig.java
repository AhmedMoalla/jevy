package com.amoalla.pongl.engine.config;

import lombok.Builder;

@Builder
public record WindowConfig(String title, int width, int height, boolean center) {
}
