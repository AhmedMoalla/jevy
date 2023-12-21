package com.amoalla.pongl.engine.ecs.systems.physics;

import com.amoalla.pongl.engine.ecs.components.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.joml.Vector2f;

import static com.amoalla.pongl.engine.ecs.systems.physics.ConversionUtils.toGdxVec2;

@Accessors(fluent = true)
public class Physics2DSystem implements Runnable {

    private final static float TIME_STEP = 1 / 60f;
    private final static int VELOCITY_ITERATIONS = 6, POSITION_ITERATIONS = 2;
    public static final Vector2f GRAVITY = new Vector2f(0, 10);

    @Getter
    private final World world;

    private final Dominion ecs;
    private final float widthInPixels;
    private final float heightInPixels;
    private boolean initialized = false;

    public Physics2DSystem(Dominion ecs, Vector2f gravity, float widthInPixels, float heightInPixels) {
        this.ecs = ecs;
        this.widthInPixels = widthInPixels;
        this.heightInPixels = heightInPixels;
        world = new World(toGdxVec2(gravity), true);
    }

    public Physics2DSystem(Dominion ecs, float widthInPixels, float heightInPixels) {
        this(ecs, GRAVITY, widthInPixels, heightInPixels);
    }

    public void init() {
        var compos = ecs.findEntitiesWith(RigidBody2DComponent.class, Transform2DComponent.class);
        for (var compo : compos) {
            Entity entity = compo.entity();
            RigidBody2DComponent rigidBody = compo.comp1();
            Transform2DComponent transform = compo.comp2();

            Body body = createBody(rigidBody, transform);
            body.setUserData(entity.get(EntityNameComponent.class).name());
            rigidBody.body(body);

            if (entity.has(BoxCollider2DComponent.class)) {
                BoxCollider2DComponent collider = entity.get(BoxCollider2DComponent.class);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(collider.size().x * transform.scale().x, collider.size().y * transform.scale().y, toGdxVec2(collider.offset()), 0);

                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                fixtureDef.density = collider.density();
                fixtureDef.friction = collider.friction();
                fixtureDef.restitution = collider.restitution();
                Fixture fixture = body.createFixture(fixtureDef);
                fixture.setUserData(new Vector2f(collider.size().x * transform.scale().x, collider.size().y * transform.scale().y));
                collider.fixture(fixture);
                shape.dispose();
            }
            if (entity.has(CircleCollider2DComponent.class)) {
                CircleCollider2DComponent collider = entity.get(CircleCollider2DComponent.class);

                CircleShape shape = new CircleShape();
                shape.setPosition(toGdxVec2(collider.offset()));
                shape.setRadius(transform.scale().x * collider.radius());

                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                fixtureDef.density = collider.density();
                fixtureDef.friction = collider.friction();
                fixtureDef.restitution = collider.restitution();
                Fixture fixture = body.createFixture(fixtureDef);
                collider.fixture(fixture);
                shape.dispose();
            }
        }
        initialized = true;
    }

    private Body createBody(RigidBody2DComponent rigidBody, Transform2DComponent transform) {
        Vector2f position = transform.translation();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = rigidBody.type();
        bodyDef.fixedRotation = rigidBody.fixedRotation();
        bodyDef.angle = transform.rotationAngle();
        bodyDef.position.set(position.x, position.y);
        return world.createBody(bodyDef);
    }

    @Override
    public void run() {
        if (!initialized) throw new IllegalStateException("Physics2DSystem was not initialized.");

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        var compos = ecs.findCompositionsWith(RigidBody2DComponent.class, Transform2DComponent.class);
        for (var compo : compos) {
            RigidBody2DComponent rigidBody = compo.comp1();
            Transform2DComponent transform = compo.comp2();

            Body body = rigidBody.body();
            Vector2 position = body.getPosition();
            transform.translation().set(position.x, position.y);
            transform.rotationAngle(body.getAngle());
        }
    }

}
