package com.amoalla.pongl.engine.ecs.systems.physics;

import com.badlogic.gdx.math.Vector2;
import org.joml.Vector2f;

public class ConversionUtils {
    public static Vector2 toGdxVec2(Vector2f vector) {
        return new Vector2(vector.x, vector.y);
    }

    public static float mapInterval(float lower1, float upper1, float lower2, float upper2, float value) {
        return lower2 + ((upper2 - lower2) / (upper1 - lower1)) * (value - lower1);
    }
}
