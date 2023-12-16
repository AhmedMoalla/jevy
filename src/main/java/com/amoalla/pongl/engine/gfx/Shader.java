package com.amoalla.pongl.engine.gfx;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Shader implements AutoCloseable {

    private static final Logger log = Logger.getLogger(Shader.class.getName());
    private final int id;
    private final Path vertexPath;
    private final Path fragmentPath;

    private boolean inUse = false;

    public Shader(int id, Path vertexPath, Path fragmentPath) {
        this.id = id;
        this.vertexPath = vertexPath;
        this.fragmentPath = fragmentPath;
    }

    public Shader(Path vertexPath, Path fragmentPath) {
        this(createShaderProgram(vertexPath, fragmentPath), vertexPath, fragmentPath);
    }

    public static Shader fromClasspath(String vertexPath, String fragmentPath) throws URISyntaxException {
        URL vertexUrl = Objects.requireNonNull(Shader.class.getResource(vertexPath));
        URL fragmentUrl = Objects.requireNonNull(Shader.class.getResource(fragmentPath));
        return new Shader(Paths.get(vertexUrl.toURI()), Paths.get(fragmentUrl.toURI()));
    }

    public void use() {
        glUseProgram(id);
        inUse = true;
    }

    @Override
    public void close() {
        glUseProgram(0);
        glDeleteShader(id);
        inUse = false;
    }

    public void setUniform(String uniformName, boolean value) {
        if (!inUse) use();
        setUniform(uniformName, value ? 1 : 0);
    }

    public void setUniform(String uniformName, int value) {
        if (!inUse) use();
        glUniform1i(glGetUniformLocation(id, uniformName), value);
    }

    public void setUniform(String uniformName, float value) {
        if (!inUse) use();
        glUniform1f(glGetUniformLocation(id, uniformName), value);
    }

    public void setUniform(String uniformName, float x, float y, float z) {
        if (!inUse) use();
        glUniform3f(glGetUniformLocation(id, uniformName), x, y, z);
    }

    public void setUniform(String uniformName, Vector3f vector3f) {
        if (!inUse) use();
        glUniform3f(glGetUniformLocation(id, uniformName), vector3f.x, vector3f.y, vector3f.z);
    }

    public void setUniform(String uniformName, float x, float y, float z, float w) {
        if (!inUse) use();
        glUniform4f(glGetUniformLocation(id, uniformName), x, y, z, w);
    }

    public void setUniform(String uniformName, Matrix4f transform) {
        if (!inUse) use();
        try (MemoryStack stack = stackPush()) {
            FloatBuffer floatBuffer = stack.mallocFloat(16);
            glUniformMatrix4fv(glGetUniformLocation(id, uniformName), false, transform.get(floatBuffer));
        }
    }

    public void setUniform(String uniformName, Color color) {
        if (!inUse) use();
        // Normalize Color as it is in the range 0-255 and OpenGL expects a range of 0-1
        glUniform3f(glGetUniformLocation(id, uniformName), color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }

    private static int createShaderProgram(Path vertexPath, Path fragmentPath) {
        try {
            int vertexShader = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vertexShader, Files.readString(vertexPath));
            glCompileShader(vertexShader);
            checkShaderCompilation(vertexShader);

            int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fragmentShader, Files.readString(fragmentPath));
            glCompileShader(fragmentShader);
            checkShaderCompilation(fragmentShader);

            int shaderProgram = glCreateProgram();
            glAttachShader(shaderProgram, vertexShader);
            glAttachShader(shaderProgram, fragmentShader);
            glLinkProgram(shaderProgram);
            checkShaderLinking(shaderProgram);

            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            return shaderProgram;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void checkShaderLinking(int shaderProgram) {
        // Check linking status
        int status = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            // Linking failed
            String errorLog = glGetProgramInfoLog(shaderProgram);
            String errMsg = STR."Program linking failed. Error log:\n\{errorLog}";
            log.severe(errMsg);
            throw new IllegalStateException(errMsg);
        } else {
            // Linking succeeded
            log.info("Program linking succeeded.");
        }
    }

    private static void checkShaderCompilation(int shaderID) {
        String shaderType = glGetShaderi(shaderID, GL_SHADER_TYPE) == GL_VERTEX_SHADER ? "GL_VERTEX_SHADER" : "GL_FRAGMENT_SHADER";

        // Check compile status
        int status = glGetShaderi(shaderID, GL_COMPILE_STATUS);
        if (status == GL11.GL_FALSE) {
            // Compilation failed
            String errorLog = glGetShaderInfoLog(shaderID);
            String errMsg = STR."Shader [\{shaderType}] Compilation failed. Error log:\n\{errorLog}";
            log.severe(errMsg);
            throw new IllegalStateException(errMsg);
        } else {
            // Compilation succeeded
            log.info(STR."Shader [\{shaderType}] Compilation succeeded.");
        }
    }
}
