package com.amoalla.pongl.engine.game;

import com.amoalla.pongl.engine.config.GameConfig;
import com.amoalla.pongl.engine.config.WindowConfig;
import com.amoalla.pongl.engine.core.Context;
import com.amoalla.pongl.engine.core.GameAdapter;
import com.amoalla.pongl.engine.core.GameRunner;
import com.amoalla.pongl.engine.ecs.components.*;
import com.amoalla.pongl.engine.ecs.systems.Script;
import com.amoalla.pongl.engine.gfx.Renderer;
import com.amoalla.pongl.engine.gfx.Texture2D;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
import org.joml.Vector2f;

import java.awt.*;

import static com.amoalla.pongl.engine.ecs.systems.physics.ConversionUtils.toGdxVec2;
import static com.amoalla.pongl.engine.ecs.systems.physics.Physics2DSystem.PPM;
import static com.amoalla.pongl.engine.game.Main.WallLocation.*;
import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;

public class Main extends GameAdapter {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int distanceFromWall = 30;

    private World world;

    @Override
    public void init(Context context) {
        super.init(context);
        world = context.physics().world();
        /**
         * A game has Scene
         * Only one Scene can be displayed at once.
         * A scene can have multiple layers
         * Layers are used to define the order of rendering and for organizing different parts of a scene
         * Each scene has it's own Box2D world and Dominion (ECS)
         * Only the currently active scene is rendered and updated
         */

        //Layer bgLayer = new BackgroundLayer();
        //Layer gameLayer = new GameLayer();
        context.physics().gravity(0, 0);

        // Should be in the background layer
        Texture2D background = new Texture2D("/textures/background.jpg");
        Vector2f bgPosition = window.center();
        Vector2f bgSize = new Vector2f(window.width(), window.height());
        ecs.createEntity(
                new EntityNameComponent("Background"),
                new Transform2DComponent(bgPosition, bgSize),
                new SpriteComponent(background),
                new Background()
        );

        Texture2D paddleTexture = new Texture2D("/textures/paddle.png");
        Vector2f size = new Vector2f(100, 20);
        Vector2f positionLeft = new Vector2f(size.y / 2 + distanceFromWall, (window.height() / 2.0f));
        BoxCollider2DComponent paddleCollider = new BoxCollider2DComponent();
        paddleCollider.restitution(1);

        ecs.createEntity(
                new EntityNameComponent("Paddle Left"),
                new Transform2DComponent(positionLeft, size, toRadians(90)),
                new SpriteComponent(paddleTexture),
                new RigidBody2DComponent(BodyType.DynamicBody, true),
                paddleCollider,
                new ScriptComponent(new PaddleScript()),
                new Paddle());

        Vector2f positionRight = new Vector2f(window.width() - size.y / 2 - distanceFromWall, (window.height() / 2.0f));
//        ecs.createEntity(
//                new EntityNameComponent("Paddle Right"),
//                new Transform2DComponent(positionRight, size, toRadians(90)),
//                new SpriteComponent(paddleTexture),
//                new RigidBody2DComponent(BodyType.DynamicBody, true),
//                paddleCollider,
//                new ScriptComponent(new PaddleScript()),
//                new Paddle());

        BoxCollider2DComponent wallCollider = new BoxCollider2DComponent();
        wallCollider.restitution(1);
        ecs.createEntity(
                new EntityNameComponent("Wall Top"),
                new Transform2DComponent(new Vector2f((float) window.width() / 2, -10), new Vector2f(window.width(), 5)),
                new RigidBody2DComponent(BodyType.StaticBody),
                wallCollider, // TODO Should be edgecollider see polygonShape.setAsEdge
                new Wall(TOP)
        ).setState(TOP);

        ecs.createEntity(
                new EntityNameComponent("Wall Bottom"),
                new Transform2DComponent(new Vector2f((float) window.width() / 2, window.height() + 5), new Vector2f(window.width(), 10)),
                new RigidBody2DComponent(BodyType.StaticBody),
                wallCollider,
                new Wall(BOTTOM)
        ).setState(BOTTOM);

        ecs.createEntity(
                new EntityNameComponent("Wall Left"),
                new Transform2DComponent(new Vector2f(-5, (float) window.height() / 2), new Vector2f(10, window.height())),
                new RigidBody2DComponent(BodyType.StaticBody),
                wallCollider,
                new Wall(LEFT)
        ).setState(LEFT);

        ecs.createEntity(
                new EntityNameComponent("Wall Right"),
                new Transform2DComponent(new Vector2f(window.width() + 5, (float) window.height() / 2), new Vector2f(10, window.height())),
                new RigidBody2DComponent(BodyType.StaticBody),
                wallCollider,
                new Wall(WallLocation.RIGHT)
        ).setState(RIGHT);

        Texture2D ballTexture = new Texture2D("/textures/awesomeface.png");
        Vector2f ballPosition = window.center();
        ecs.createEntity(
                new EntityNameComponent("Ball"),
                new Transform2DComponent(ballPosition, new Vector2f(25)),
                new SpriteComponent(ballTexture),
                new RigidBody2DComponent(BodyType.DynamicBody),
                new CircleCollider2DComponent(),
                new ScriptComponent(new BallScript())
        );
    }

    record Paddle() {}

    record Wall(WallLocation location) {}
    enum WallLocation {
        LEFT, RIGHT, TOP, BOTTOM
    }

    @Override
    public void render(Renderer renderer) {
        renderer.fill(Color.BLACK);
    }

    @Override
    public void onKeyPress(int key, int scancode, int mods) {
        if (key == GLFW_KEY_ESCAPE) {
            window.close();
        }
        if (key == GLFW_KEY_T) {
            var compositions = ecs.findEntitiesWith(Paddle.class, ScriptComponent.class);
            for (Results.With2<Paddle, ScriptComponent> composition : compositions) {
                PaddleScript script = (PaddleScript) composition.comp2().script();
                script.remove();
                script.init(composition.entity(), ecs);
            }
        }
    }

    private class PaddleScript implements Script {

        Joint joint;
        @Override
        public void init(Entity entity, Dominion ecs) {
            Body paddleBody = entity.get(RigidBody2DComponent.class).body();
            WallLocation location = switch (entity.get(EntityNameComponent.class).name()) {
                case "Paddle Left" -> LEFT;
                case "Paddle Right" -> RIGHT;
                default -> throw new IllegalStateException();
            };

            var walls = ecs.findCompositionsWith(Wall.class, RigidBody2DComponent.class).withState(location);
            var compo = walls.iterator().next();
            Body wallBody = compo.comp2().body();

            PrismaticJointDef jointDef = new PrismaticJointDef();
            jointDef.bodyA = wallBody;
            jointDef.bodyB = paddleBody;
            jointDef.collideConnected = false;

            jointDef.localAxisA.set(0f, 1f);

            jointDef.localAnchorA.set(0, 0);
            jointDef.localAnchorB.set(0, 2.5f);

            float limit = (((float) window.height() / 2) - 10 - 50) / PPM;
            jointDef.enableLimit = true;
            jointDef.lowerTranslation = -limit;
            jointDef.upperTranslation = limit;

            jointDef.enableMotor = true;
            jointDef.maxMotorForce = 500;
            jointDef.motorSpeed = 0;

            joint = world.createJoint(jointDef);
        }

        public void remove() {
            if (joint != null)
                world.destroyJoint(joint);
        }

        @Override
        public void run(Entity entity) {
            float speed = 50;
            Body body = entity.get(RigidBody2DComponent.class).body();
            if (window.isKeyPressed(GLFW_KEY_UP)) {
                body.setLinearVelocity(0, -speed);
            } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
                body.setLinearVelocity(0, speed);
            } else {
                body.setLinearVelocity(0, 0);
            }
        }
    }

    private class BallScript implements Script {

        @Override
        public void init(Entity entity, Dominion ecs) {

        }

        @Override
        public void run(Entity entity) {
            Body body = entity.get(RigidBody2DComponent.class).body();
            if (window.isKeyPressed(GLFW_KEY_SPACE)) {
                if (!body.isAwake()) {
                    body.setLinearVelocity(30, 0);
                    body.setLinearDamping(0);
                    body.setAngularVelocity(toRadians(120));
                    body.setAngularDamping(0);
                }
            } else if (window.isKeyPressed(GLFW_KEY_R)) {
                Transform transform = body.getTransform();
                body.setTransform(toGdxVec2(window.center().div(PPM, new Vector2f())), transform.getRotation());
                body.setLinearVelocity(0, 0);
                body.setAngularVelocity(0);
            }
            System.out.println(body.getLinearVelocity());
            System.out.println(body.getAngularVelocity());
        }
    }

    public static void main(String[] args) {
        GameConfig config = GameConfig.builder()
                .window(WindowConfig.builder()
                        .width(WIDTH)
                        .height(HEIGHT)
                        .title("PonGL")
                        .center(true)
                        .build())
                .debug(true)
                .build();
        try (GameRunner runner = new GameRunner(config)) {
            runner.run(new Main());
        }
    }
}