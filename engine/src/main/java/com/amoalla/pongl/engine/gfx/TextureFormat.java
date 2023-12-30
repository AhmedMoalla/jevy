package com.amoalla.pongl.engine.gfx;

import lombok.RequiredArgsConstructor;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_RG;
import static org.lwjgl.opengl.GL30.GL_RG8;

@RequiredArgsConstructor
public enum TextureFormat {
    RED(GL_RED, GL_RED, 1),
    RED_GREEN(GL_RG, GL_RG8, 2),
    RGB(GL_RGB, GL_RGB8, 3),
    RGBA(GL_RGBA, GL_RGBA8, 4);

    public final int glDataFormat;
    public final int glInternalFormat;
    public final int nbChannels;

    public static TextureFormat fromNbChannels(int nbChannels) {
        for (TextureFormat format : values()) {
            if (format.nbChannels == nbChannels) {
                return format;
            }
        }
        throw new IllegalArgumentException(STR."Unsupported number of channels: \{nbChannels}");
    }
}