package com.amoalla.pongl.engine.gfx;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.Version;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;

import java.awt.*;
import java.net.URISyntaxException;

import static com.amoalla.pongl.engine.util.Constants.ZERO;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

@Log
@Accessors(fluent = true)
public class Renderer implements AutoCloseable {
    private static final String VERTEX_SHADER = "/shaders/default.vert";
    private static final String FRAGMENT_SHADER = "/shaders/default.frag";

    private final Shader shader;

    private final Texture2D whiteTexture;

    private int quadVao;
    private int lineVao, lineVbo;

    private final Matrix4f model = new Matrix4f();
    @Getter
    private final Matrix4f defaultProjection;
    @Getter
    private final Matrix4f projection;

    public Renderer(boolean debug, int width, int height) {
        GL.createCapabilities();
        if (debug) {
            GLUtil.setupDebugMessageCallback();
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

        this.defaultProjection = new Matrix4f()
                .ortho(0.0f, width, height, 0.0f, -1.0f, 1.0f);
        projection = new Matrix4f(defaultProjection);

        quadVao = createQuadVao();
        createLineVao();
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        whiteTexture = new Texture2D(1, 1, Color.WHITE);
    }

    public void setProjectionDefault() {
        projection.set(defaultProjection);
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

        model.translation(position.x - (size.x / 2), position.y - (size.y / 2), 0.0f)
                .translate(rotationCenter.x, rotationCenter.y, 0.0f)
                .rotateZ(rotationAngle)
                .translate(-rotationCenter.x, -rotationCenter.y, 0.0f)
                .scale(size.x, size.y, 1.0f);
        shader.setUniform("u_model", model);
        shader.setUniform("u_projection", projection);
        shader.setUniform("u_spriteColor", tint);
        shader.setUniform("u_texture", 0);

        texture.bind();
        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }


    public void drawLine(float x1, float y1, float x2, float y2, Color color) {
        shader.use();
        shader.setUniform("u_model", model.identity());
        shader.setUniform("u_projection", projection);
        shader.setUniform("u_spriteColor", color);
        shader.setUniform("u_texture", 0);


        float[] vertices = new float[]{
                x1, y1, 0.0f, 0.0f,
                x2, y2, 1.0f, 1.0f
        };

        glBindBuffer(GL_ARRAY_BUFFER, lineVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        whiteTexture.bind();
        glBindVertexArray(lineVao);
        glDrawArrays(GL_LINES, 0, 2);
        glBindVertexArray(0);
    }

    public void drawPoint(float x, float y, Color color) {
        // TODO: replace with draw circle
        drawQuad(new Vector2f(x, y), new Vector2f(5, 5), color);
    }

    private int createQuadVao() {
        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        float[] vertices = {
                // pos      // tex
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f,

                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f
        };

        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, NULL);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        return vao;
    }


    private void createLineVao() {
        lineVao = glGenVertexArrays();
        lineVbo = glGenBuffers();
        glBindVertexArray(lineVao);
        glBindBuffer(GL_ARRAY_BUFFER, lineVbo);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, NULL);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        shader.close();
    }
}
