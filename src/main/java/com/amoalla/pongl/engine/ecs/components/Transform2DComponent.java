package com.amoalla.pongl.engine.ecs.components;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.joml.Vector2f;

@Setter
@Getter
@Accessors(fluent = true)
@ToString
public final class Transform2DComponent {
    private final Vector2f translation;
    private final Vector2f scale;
    private float rotationAngle;
    private final Vector2f rotationCenter;

    public Transform2DComponent(Vector2f translation, Vector2f scale, float rotationAngle, Vector2f rotationCenter) {
        this.translation = translation;
        this.scale = scale;
        this.rotationAngle = rotationAngle;
        this.rotationCenter = rotationCenter;
    }

    public Transform2DComponent(Vector2f translation, Vector2f scale, float rotationAngle) {
        this(translation, scale, rotationAngle, scale.mul(0.5f, new Vector2f(scale.x / 2, scale.y / 2)));
    }

    public Transform2DComponent(Vector2f translation, Vector2f scale) {
        this(translation, scale, 0.0f);
    }

}