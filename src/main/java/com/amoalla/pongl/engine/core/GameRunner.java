package com.amoalla.pongl.engine.core;

import com.amoalla.pongl.engine.config.GameConfig;
import com.amoalla.pongl.engine.config.WindowConfig;
import com.amoalla.pongl.engine.core.ecs.components.Sprite;
import com.amoalla.pongl.engine.core.ecs.components.Transform2D;
import com.amoalla.pongl.engine.core.ecs.systems.ScriptExecutionSystem;
import com.amoalla.pongl.engine.gfx.Renderer;
import com.amoalla.pongl.engine.gfx.Window;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Scheduler;

import static org.lwjgl.glfw.GLFW.*;

public class GameRunner implements AutoCloseable {

    private static final int TICKS_PER_SECOND = 60;

    private final Window window;
    private final Renderer renderer;

    private final Context context;
    private final Dominion ecs = Dominion.create();
    private final Scheduler tickScheduler = ecs.createScheduler();

    public GameRunner(GameConfig config) {
        WindowConfig windowConfig = config.window();
        window = new Window(windowConfig, config.debug());
        renderer = new Renderer(config.debug(), windowConfig.width(), windowConfig.height());
        context = new Context(window, ecs);
        tickScheduler.schedule(new ScriptExecutionSystem(ecs));
    }

    public void run(Game game) {
        game.init(context);
        window.setKeyCallback((_, key, scancode, action, mods) -> {
            switch (action) {
                case GLFW_PRESS -> game.onKeyPress(key, scancode, mods);
                case GLFW_RELEASE -> game.onKeyRelease(key, scancode, mods);
                case GLFW_REPEAT -> game.onKeyRepeat(key, scancode, mods);
            }
        });
        tickScheduler.schedule(game::fixedUpdate);
        tickScheduler.tickAtFixedRate(TICKS_PER_SECOND);
        window.loop(() -> {
            game.update();
            game.render(renderer);
            render();
        });
        game.close();
    }

    private void render() {
        for (var composition : ecs.findCompositionsWith(Sprite.class, Transform2D.class)) {
            Sprite sprite = composition.comp1();
            Transform2D transform = composition.comp2();
            if (sprite.texture() == null) {
                renderer.drawQuad(transform.translation(), transform.scale(), transform.rotationAngle(), transform.rotationCenter(), sprite.tint());
            } else {
                renderer.drawQuad(transform.translation(), transform.scale(), transform.rotationAngle(), transform.rotationCenter(), sprite.texture(), sprite.tint());
            }
        }
    }

    @Override
    public void close() {
        renderer.close();
        ecs.close();
        tickScheduler.shutDown();
        window.close();
    }
}
