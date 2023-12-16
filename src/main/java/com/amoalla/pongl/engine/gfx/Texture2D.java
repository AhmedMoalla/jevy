package com.amoalla.pongl.engine.gfx;

import com.amoalla.pongl.engine.gfx.stb.JSTBImage;
import com.amoalla.pongl.engine.gfx.stb.LoadResult;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;

@Getter
@Accessors(fluent = true)
public class Texture2D implements AutoCloseable {

    private final int id;
    private final int width;
    private final int height;
    private final TextureFormat format;

    public Texture2D(int width, int height, Color color) {
        this.width = width;
        this.height = height;
        this.format = TextureFormat.RGBA;
        id = createTexture(format);

        try (MemoryStack stack = stackPush()) {
            ByteBuffer data = stack.bytes((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) color.getAlpha());
            setData(data);
        }
    }

    public Texture2D(String classpath) {
        try (LoadResult result = JSTBImage.loadFromClassPath(classpath)) {
            this.width = result.width();
            this.height = result.height();
            format = result.format();
            id = createTexture(format);
            setData(result.data());
        }
    }

    private int createTexture(TextureFormat format) {
        int textureId = glCreateTextures(GL_TEXTURE_2D);
        glTextureStorage2D(textureId, 1, format.glInternalFormat, width, height);

        glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, GL_REPEAT);
        return textureId;
    }

    private void setData(ByteBuffer data) {
        glTextureSubImage2D(id, 0, 0, 0, width, height, format.glDataFormat, GL_UNSIGNED_BYTE, data);
    }

    @Override
    public void close() {
        glDeleteTextures(id);
    }
}
