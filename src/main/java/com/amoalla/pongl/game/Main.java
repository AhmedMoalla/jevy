package com.amoalla.pongl.game;

import com.amoalla.pongl.engine.config.GameConfig;
import com.amoalla.pongl.engine.config.WindowConfig;
import com.amoalla.pongl.engine.core.Context;
import com.amoalla.pongl.engine.core.GameAdapter;
import com.amoalla.pongl.engine.core.GameRunner;
import com.amoalla.pongl.engine.core.ecs.components.EntityName;
import com.amoalla.pongl.engine.core.ecs.components.Script;
import com.amoalla.pongl.engine.core.ecs.components.Sprite;
import com.amoalla.pongl.engine.core.ecs.components.Transform2D;
import com.amoalla.pongl.engine.gfx.Renderer;
import com.amoalla.pongl.engine.gfx.Texture2D;
import dev.dominion.ecs.api.Entity;
import org.joml.Vector2f;

import java.awt.*;

import static com.amoalla.pongl.engine.util.Constants.VECTOR_Y_COMPONENT;
import static com.amoalla.pongl.engine.util.Constants.ZERO;
import static org.lwjgl.glfw.GLFW.*;

public class Main extends GameAdapter {

    @Override
    public void init(Context context) {
        super.init(context);

        Texture2D background = new Texture2D("/textures/background.jpg");

        Texture2D paddleTexture = new Texture2D("/textures/paddle.png");
        Vector2f size = new Vector2f(100, 20);
        Vector2f position = new Vector2f(0, (window.height() / 2.0f) + (size.x / 2));
        Vector2f rotationCenter = new Vector2f(0, 0);

        ecs.createEntity(
                new EntityName("Paddle"),
                new Transform2D(position, size, -90.0f, rotationCenter),
                new Sprite(paddleTexture),
                new Script(this::moveEntityOnKeyPress));

        Vector2f bgSize = new Vector2f(window.width(), window.height());
        ecs.createEntity(
                new EntityName("Background"),
                new Transform2D(ZERO, bgSize),
                new Sprite(background));
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
            Transform2D transform = entity.get(Transform2D.class);
            Vector2f translation = transform.translation();
            Vector2f scale = transform.scale();
            float newY = Math.max(translation.y - speed, scale.x);
            translation.setComponent(VECTOR_Y_COMPONENT, newY);
        }
        if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            Transform2D transform = entity.get(Transform2D.class);
            Vector2f translation = transform.translation();
            float newY = Math.min(translation.y + speed, window.height());
            translation.setComponent(VECTOR_Y_COMPONENT, newY);
        }
    }

    public static void main(String[] args) {
        GameConfig config = GameConfig.builder()
                .window(WindowConfig.builder()
                        .width(800)
                        .height(600)
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