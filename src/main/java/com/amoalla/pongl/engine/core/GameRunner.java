package com.amoalla.pongl.engine.core;

import com.amoalla.pongl.engine.config.GameConfig;
import com.amoalla.pongl.engine.config.WindowConfig;
import com.amoalla.pongl.engine.ecs.components.SpriteComponent;
import com.amoalla.pongl.engine.ecs.components.Transform2DComponent;
import com.amoalla.pongl.engine.ecs.systems.ScriptExecutionSystem;
import com.amoalla.pongl.engine.ecs.systems.physics.Physics2DSystem;
import com.amoalla.pongl.engine.gfx.PhysicsRenderer;
import com.amoalla.pongl.engine.gfx.Renderer;
import com.amoalla.pongl.engine.gfx.Window;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Scheduler;

import static org.lwjgl.glfw.GLFW.*;

public class GameRunner implements AutoCloseable {

    private static final int TICKS_PER_SECOND = 60;

    private final Window window;
    private final Renderer renderer;
    private PhysicsRenderer physicsRenderer;
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
        Physics2DSystem physics = new Physics2DSystem(ecs, window.width(), window.height());
        game.init(context);
        physics.init();
        window.setKeyCallback((_, key, scancode, action, mods) -> {
            switch (action) {
                case GLFW_PRESS -> game.onKeyPress(key, scancode, mods);
                case GLFW_RELEASE -> game.onKeyRelease(key, scancode, mods);
                case GLFW_REPEAT -> game.onKeyRepeat(key, scancode, mods);
            }
        });
        tickScheduler.schedule(physics);
        // TODO: Render only if debug is true
        physicsRenderer = new PhysicsRenderer();
        tickScheduler.schedule(game::fixedUpdate);
        tickScheduler.tickAtFixedRate(TICKS_PER_SECOND);
        window.loop(() -> {
            game.update();
            game.render(renderer);
            render();
            physicsRenderer.render(physics.world(), renderer);
        });
        game.close();
    }

    private void render() {
        for (var composition : ecs.findCompositionsWith(SpriteComponent.class, Transform2DComponent.class)) {
            SpriteComponent sprite = composition.comp1();
            Transform2DComponent transform = composition.comp2();
            if (sprite.texture() == null) {
                renderer.drawQuad(transform.translation(), transform.scale(), transform.rotationAngle(), transform.rotationCenter(), sprite.tint());
            } else {
                renderer.drawQuad(transform.translation(), transform.scale(), transform.rotationAngle(), transform.rotationCenter(), sprite.texture(), sprite.tint());
            }
        }

//        int step = 50;
//        for (int x = 0; x < window.width(); x+=step) {
//            Color color = Color.WHITE;
//            if (x == window.width() / 2) {
//                color = Color.GREEN;
//            }
//            renderer.drawLine(x, 0, x, window.height(), color);
//        }
//        for (int y = 0; y < window.height(); y+=step) {
//            Color color = Color.WHITE;
//            if (y == window.height() / 2) {
//                color = Color.GREEN;
//            }
//            renderer.drawLine(0, y, window.width(), y, color);
//        }
    }

    @Override
    public void close() {
        renderer.close();
        ecs.close();
        tickScheduler.shutDown();
    }
}
