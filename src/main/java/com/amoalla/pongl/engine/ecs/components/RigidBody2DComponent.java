package com.amoalla.pongl.engine.ecs.components;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(fluent = true)
public class RigidBody2DComponent {
    private BodyType type;
    private boolean fixedRotation;
    private Body body;

    public RigidBody2DComponent(BodyType type, boolean fixedRotation) {
        this.type = type;
        this.fixedRotation = fixedRotation;
    }

    public RigidBody2DComponent(BodyType type) {
        this(type, false);
    }

    public RigidBody2DComponent() {
        this(BodyType.StaticBody, false);
    }

    @Override
    public String toString() {
        return STR."RigidBody2DComponent(type=\{type}, fixedRotation=\{fixedRotation}, body=\{toString(body)})";
    }

    private String toString(Body body) {
        return STR."Body(transform.rotation=\{body.getTransform().getRotation()}, transform.position=\{body.getTransform().getPosition()}, transform.orientation=\{body.getTransform().getOrientation()}, angle=\{body.getAngle()})";
    }
}
