package com.amoalla.pongl.engine.core.ecs.components;

import org.joml.Vector2f;

public final class Transform2D {
    private final Vector2f translation;
    private final Vector2f scale;
    private final float rotationAngle;
    private final Vector2f rotationCenter;

    public Transform2D(Vector2f translation, Vector2f scale, float rotationAngle, Vector2f rotationCenter) {
        this.translation = translation;
        this.scale = scale;
        this.rotationAngle = rotationAngle;
        this.rotationCenter = rotationCenter;
    }

    public Transform2D(Vector2f translation, Vector2f scale, float rotationAngle) {
        this(translation, scale, rotationAngle, scale.mul(0.5f, new Vector2f()));
    }

    public Transform2D(Vector2f translation, Vector2f scale) {
        this(translation, scale, 0.0f);
    }

    public Vector2f translation() {
        return translation;
    }

    public Vector2f scale() {
        return scale;
    }

    public float rotationAngle() {
        return rotationAngle;
    }

    public Vector2f rotationCenter() {
        return rotationCenter;
    }

}