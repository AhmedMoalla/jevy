package com.amoalla.pongl.engine.gfx;

import com.amoalla.pongl.engine.gfx.stb.JSTBImage;
import com.amoalla.pongl.engine.gfx.stb.LoadResult;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL;
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

    // Do this because OpenGL45 is unsupported on M1 Macs
    private int createTexture(TextureFormat format) {
        if (GL.getCapabilities().OpenGL45) {
            return createTextureGL45(format);
        } else {
            int textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            return textureId;
        }
    }

    private int createTextureGL45(TextureFormat format) {
        int textureId = glCreateTextures(GL_TEXTURE_2D);
        glTextureStorage2D(textureId, 1, format.glInternalFormat, width, height);

        glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, GL_REPEAT);
        return textureId;
    }

    private void setData(ByteBuffer data) {
        if (GL.getCapabilities().OpenGL45) {
            glTextureSubImage2D(id, 0, 0, 0, width, height, format.glDataFormat, GL_UNSIGNED_BYTE, data);
        } else {
            glBindTexture(GL_TEXTURE_2D, id);
            glTexImage2D(GL_TEXTURE_2D, 0, format.glInternalFormat, width, height, 0, format.glDataFormat, GL_UNSIGNED_BYTE, data);
        }
    }

    public void bind() {
        if (GL.getCapabilities().OpenGL45) {
            glBindTextureUnit(0, id);
        } else {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, id);
        }
    }

    @Override
    public void close() {
        glDeleteTextures(id);
    }
}
