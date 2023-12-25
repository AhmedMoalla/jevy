package com.amoalla.pongl.engine.ecs.systems.physics;

import com.badlogic.gdx.math.Vector2;
import org.joml.Vector2f;

public class ConversionUtils {
    public static Vector2 toGdxVec2(Vector2f vector) {
        return new Vector2(vector.x, vector.y);
    }
}
