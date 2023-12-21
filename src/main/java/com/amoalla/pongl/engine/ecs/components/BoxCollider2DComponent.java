package com.amoalla.pongl.engine.ecs.components;

import com.badlogic.gdx.physics.box2d.Fixture;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.joml.Vector2f;

@Setter
@Getter
@Accessors(fluent = true, chain = true)
@ToString
public class BoxCollider2DComponent {
    private Vector2f offset = new Vector2f();
    private Vector2f size = new Vector2f(0.5f, 0.5f);

    private float density = 1;
    private float friction = 0.5f;
    private float restitution = 0;

    private Fixture fixture;
}
