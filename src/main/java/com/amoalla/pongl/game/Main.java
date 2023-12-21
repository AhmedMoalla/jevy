package com.amoalla.pongl.game;

import com.amoalla.pongl.engine.config.GameConfig;
import com.amoalla.pongl.engine.config.WindowConfig;
import com.amoalla.pongl.engine.core.Context;
import com.amoalla.pongl.engine.core.GameAdapter;
import com.amoalla.pongl.engine.core.GameRunner;
import com.amoalla.pongl.engine.ecs.components.*;
import com.amoalla.pongl.engine.gfx.Renderer;
import com.amoalla.pongl.engine.gfx.Texture2D;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import dev.dominion.ecs.api.Entity;
import org.joml.Math;
import org.joml.Vector2f;

import java.awt.*;

import static com.amoalla.pongl.engine.util.Constants.VECTOR_Y_COMPONENT;
import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;

public class Main extends GameAdapter {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    @Override
    public void init(Context context) {
        super.init(context);

        Texture2D background = new Texture2D("/textures/background.jpg");

        Texture2D paddleTexture = new Texture2D("/textures/paddle.png");
        Vector2f size = new Vector2f(100, 20);
        Vector2f position = new Vector2f((window.width() / 2.0f), (window.height() / 2.0f));

        ecs.createEntity(
                new EntityNameComponent("Paddle"),
                new Transform2DComponent(position, size, toRadians(90)),
                new SpriteComponent(paddleTexture),
                new RigidBody2DComponent(BodyType.KinematicBody),
                new BoxCollider2DComponent());

        for (int i = 0; i < window.width(); i += 25) {
            var ballPos = new Vector2f(i, 200);
            ecs.createEntity(
                    new EntityNameComponent("Ball"),
                    new Transform2DComponent(ballPos, new Vector2f(20)),
                    new SpriteComponent(paddleTexture),
                    new RigidBody2DComponent(BodyType.DynamicBody),
                    new CircleCollider2DComponent()
            );
        }

        Vector2f bgSize = new Vector2f(window.width(), window.height());
//        ecs.createEntity(
//                new EntityNameComponent("Background"),
//                new Transform2DComponent(ZERO, bgSize),
//                new SpriteComponent(background)
//        );

//        ecs.createEntity(
//                new EntityNameComponent("Floor"),
//                new Transform2DComponent(new Vector2f(window.width() / 2f, window.height()), new Vector2f(window.width(), 1)),
//                new RigidBody2DComponent(),
//                new BoxCollider2DComponent()
//        );
//
//        float platformWidth = 500;
//        float platformSpacing = 50;
//        for (int i = 0; i < 4; i++) {
//            ecs.createEntity(
//                    new EntityNameComponent(STR."Platform \{i}"),
//                    new Transform2DComponent(new Vector2f(window.width() / 2f, window.height() / 2f + platformSpacing * i), new Vector2f(platformWidth, 3)),
//                    new RigidBody2DComponent(),
//                    new BoxCollider2DComponent()
//            );
//        }
//
//        int numPerRow = 25;
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < numPerRow; j++) {
//                float platformX = window.width() / 2f - platformWidth / 2f;
//                float platformY = window.height() / 2f + platformSpacing * i;
//                float dominoWidth = (platformWidth / 3f) / numPerRow;
//                float dominoHeight = 35;
//                float spacing = (platformWidth * (2 / 3f)) / numPerRow;
//                var pos = new Vector2f(platformX + (dominoWidth / 2) + (j * (dominoWidth + spacing)), platformY - (dominoHeight / 2));
//                Transform2DComponent transform;
//                if (i == 2 && j == 0) {
//                    transform = new Transform2DComponent(pos.add(-5, 0), new Vector2f(dominoWidth, dominoHeight), 0.5f);
//                    } else if (i == 3 && j == numPerRow - 1) {
//                    transform = new Transform2DComponent(pos.add(5, 0), new Vector2f(dominoWidth, dominoHeight), -0.5f);
//                    } else
//                        transform = new Transform2DComponent(pos, new Vector2f(dominoWidth, dominoHeight));
//
//                ecs.createEntity(
//                        new EntityNameComponent(STR."Domino \{i},\{j}"),
//                        transform,
//                        new RigidBody2DComponent(BodyType.DynamicBody),
//                        new BoxCollider2DComponent()
//                                .density(25)
//                                .friction(.5f)
//                );
//            }
//        }
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
    }

    public void moveEntityOnKeyPress(Entity entity) {
        float speed = 5;
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            Transform2DComponent transform = entity.get(Transform2DComponent.class);
            Vector2f translation = transform.translation();
            Vector2f scale = transform.scale();
            float newY = Math.max(translation.y - speed, scale.x);
            translation.setComponent(VECTOR_Y_COMPONENT, newY);
        }
        if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            Transform2DComponent transform = entity.get(Transform2DComponent.class);
            Vector2f translation = transform.translation();
            float newY = Math.min(translation.y + speed, window.height());
            translation.setComponent(VECTOR_Y_COMPONENT, newY);
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