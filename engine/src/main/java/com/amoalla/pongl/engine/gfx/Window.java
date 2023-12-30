package com.amoalla.pongl.engine.gfx;

import com.amoalla.pongl.engine.config.WindowConfig;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;

import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Vector;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

@Getter
@Accessors(fluent = true)
public class Window {

    private final int width;
    private final int height;
    private final long handle;
    private final Vector2f center;

    public Window(WindowConfig config, boolean debug) {
        this.width = config.width();
        this.height = config.height();
        center = new Vector2f((float) width / 2, (float) height / 2);

        if (!glfwInit()) {
            throw new IllegalStateException("Unabled to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        if (Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        if (debug) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
            glfwSetErrorCallback(GLFWErrorCallback.createThrow());
        }

        handle = glfwCreateWindow(width, height, config.title(), NULL, NULL);
        if (handle == NULL) {
            glfwTerminate();
            throw new IllegalStateException("Could not create window.");
        }

        if (config.center()) {
            centerInPrimaryMonitor();
        }

        glfwMakeContextCurrent(handle);

        glfwShowWindow(handle);
    }

    public Vector2f center() {
        return new Vector2f(center);
    }

    public void loop(Runnable runnable) {
        while (!glfwWindowShouldClose(handle)) {
            glfwPollEvents();
            runnable.run();
            glfwSwapBuffers(handle);
        }

        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).close();
    }

    public void setKeyCallback(GLFWKeyCallbackI keyCallback) {
        GLFWKeyCallback previousCallback = glfwSetKeyCallback(handle, keyCallback);
        if (previousCallback != null) {
            previousCallback.close();
        }
    }

    public boolean isKeyPressed(int key) {
        return glfwGetKey(handle, key) == GLFW_PRESS;
    }

    public void close() {
        glfwSetWindowShouldClose(handle, true);
    }

    private void centerInPrimaryMonitor() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(handle, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            Objects.requireNonNull(vidmode);

            glfwSetWindowPos(
                    handle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
    }
}
