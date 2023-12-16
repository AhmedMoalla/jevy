package com.amoalla.pongl.engine.gfx;

import lombok.extern.java.Log;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.Version;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;

import java.awt.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.amoalla.pongl.engine.util.Constants.ZERO;
import static java.lang.Math.toRadians;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.system.MemoryUtil.NULL;

@Log
public class Renderer implements AutoCloseable {

    private static final float[] VERTICES = {
            // pos      // tex
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f
    };

    private static final String VERTEX_SHADER = "/shaders/default.vert";
    private static final String FRAGMENT_SHADER = "/shaders/default.frag";

    private final List<AutoCloseable> closeables = new ArrayList<>();
    private final Shader shader;

    private final Texture2D whiteTexture;

    private final int vao;

    private final Matrix4f model = new Matrix4f();
    private final Matrix4f projection;

    public Renderer(boolean debug, int width, int height) {
        GL.createCapabilities();
        if (debug) {
            closeables.add(GLUtil.setupDebugMessageCallback());
        }

        log.info(STR."LWJGL \{Version.getVersion()}!");
        log.info(STR."OpenGL: \{glGetString(GL_VERSION)}");

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        try {
            shader = Shader.fromClasspath(VERTEX_SHADER, FRAGMENT_SHADER);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not load shaders.");
        }

        this.projection = new Matrix4f()
                .ortho(0.0f, width, height, 0.0f, -1.0f, 1.0f);

        vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, NULL);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        whiteTexture = new Texture2D(1, 1, Color.WHITE);
    }

    public void fill(Color color) {
        glClearColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        glClear(GL_COLOR_BUFFER_BIT);
    }

    public void drawQuad(Vector2f position, Vector2f size, Color color) {
        drawQuad(position, size, 0.0f, ZERO, whiteTexture, color);
    }

    public void drawQuad(Vector2f position, Vector2f size, float rotation, Vector2f rotationCenter, Color color) {
        drawQuad(position, size, rotation, rotationCenter, whiteTexture, color);
    }

    public void drawQuad(Vector2f position, Vector2f size, Texture2D texture) {
        drawQuad(position, size, 0.0f, ZERO, texture, Color.WHITE);
    }

    public void drawQuad(Vector2f position, Vector2f size, float rotation, Vector2f rotationCenter, Texture2D texture) {
        drawQuad(position, size, rotation, rotationCenter, texture, Color.WHITE);
    }

    public void drawQuad(Vector2f position, Vector2f size, float rotationAngle, Vector2f rotationCenter, Texture2D texture, Color tint) {
        shader.use();

        model.translation(position.x, position.y, 0.0f)
                .translate(rotationCenter.x, rotationCenter.y, 0.0f)
                .rotateZ((float) toRadians(rotationAngle))
                .translate(-rotationCenter.x, -rotationCenter.y, 0.0f)
                .scale(size.x, size.y, 1.0f);
        shader.setUniform("u_model", model);
        shader.setUniform("u_projection", projection);
        shader.setUniform("u_spriteColor", tint);
        shader.setUniform("u_texture", 0);

        glBindTextureUnit(0, texture.id());
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        for (AutoCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        shader.close();
    }
}
