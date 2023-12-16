package com.amoalla.pongl.engine.gfx.stb;

import org.lwjgl.system.MemoryStack;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.system.MemoryStack.stackPush;

public class JSTBImage {
    public static LoadResult load(Path imagePath, boolean flip) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer nbChannels = stack.mallocInt(1);
            stbi_set_flip_vertically_on_load(flip);
            ByteBuffer data = stbi_load(imagePath.toString(), width, height, nbChannels, 0);
            return new LoadResult(data, width.get(), height.get(), nbChannels.get());
        }
    }

    public static LoadResult load(Path imagePath) {
        return load(imagePath, true);
    }

    public static LoadResult loadFromClassPath(String imagePath) {
        return loadFromClassPath(imagePath, true);
    }

    public static LoadResult loadFromClassPath(String imagePath, boolean flip) {
        try {
            Path absolutePath = Paths.get(JSTBImage.class.getResource(imagePath).toURI()).toAbsolutePath();
            return load(absolutePath, flip);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(STR."Could not load texture from path \{imagePath}", e);
        }
    }

}
