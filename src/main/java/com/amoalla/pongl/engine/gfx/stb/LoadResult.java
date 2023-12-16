package com.amoalla.pongl.engine.gfx.stb;

import com.amoalla.pongl.engine.gfx.TextureFormat;

import java.nio.ByteBuffer;

import static com.amoalla.pongl.engine.gfx.TextureFormat.fromNbChannels;
import static org.lwjgl.stb.STBImage.stbi_image_free;

public record LoadResult(ByteBuffer data, int width, int height, TextureFormat format) implements AutoCloseable {

    public LoadResult(ByteBuffer data, int width, int height, int nbChannels) {
        this(data, width, height, fromNbChannels(nbChannels));
    }

    @Override
    public void close() {
        stbi_image_free(data);
    }
}