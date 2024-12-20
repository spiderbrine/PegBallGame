package com.emerson.gamescreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.emerson.pegballgame.PegBallStart;
import com.emerson.world.GameWorld;
import com.emerson.world.Level;
import com.emerson.world.LevelManager;

public class GameScreen extends ScreenAdapter {

    private final PegBallStart GAME;
    private final LevelManager LEVEL_MANAGER;

    public static final float VIRTUAL_WIDTH = 1280;  // Virtual resolution width
    public static final float VIRTUAL_HEIGHT = 720;  // Virtual resolution height

    private GameWorld gameWorld;
    private Level level;
    private ShapeRenderer shapeRenderer;

    private Box2DDebugRenderer debugRenderer;

    private OrthographicCamera camera;
    private Viewport viewport;

    public GameScreen(PegBallStart game, int levelIndex) {
        this.GAME = game;
        this.LEVEL_MANAGER = GAME.getLevelManager();
        // create camera
        camera = new OrthographicCamera();

        // create FitViewport to maintain resolution
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();

        // put camera in the center
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        LEVEL_MANAGER.setCurrentLevel(levelIndex);
        loadLevel(LEVEL_MANAGER.getCurrentLevelIndex());
        shapeRenderer = new ShapeRenderer();

        debugRenderer = new Box2DDebugRenderer();
    }

    private void loadLevel(int index) {
        if (gameWorld != null) {
            level.getGameWorld().reset();
            gameWorld = null;
        }
        if (level != null) {
            level.reset();
        }
        Level level = LEVEL_MANAGER.getLevel(index);
        gameWorld = level.getGameWorld();
        level.setupLevel(LEVEL_MANAGER.getCurrentLevelIndex());
    }

    // camera methods if needed for ending sequence

    @Override
    public void render(float deltaTime) {
        // update camera every frame
        camera.update();

        // set projection matrix of shapeRenderer to match camera
        shapeRenderer.setProjectionMatrix(camera.combined);

        // clear screen
        Gdx.gl.glClearColor(1, 1, 1, 1);  // rgba (white)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // update world (60 times per second)
        gameWorld.update(deltaTime);

        // render objects
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameWorld.renderObjects(shapeRenderer);  // Call the render method for all objects
        shapeRenderer.end();
        gameWorld.renderStage(deltaTime);

        //debugRenderer.render(gameWorld.getWorld(), camera.combined);
    }

    public void resize(int width, int height) {
        // resize so stuff doesn't look weird
        if (!gameWorld.characterSelected) {
            gameWorld.characterSelectMenu.resize(width, height);
        }
        viewport.update(width, height);


    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
