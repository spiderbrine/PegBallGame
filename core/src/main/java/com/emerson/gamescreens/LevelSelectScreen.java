package com.emerson.gamescreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.emerson.pegballgame.PegBallStart;
import com.emerson.world.Level;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class LevelSelectScreen implements Screen {

    private final PegBallStart GAME;
    private Stage stage;
    private Skin skin;

    private SpriteBatch batch;
    private Texture backgroundTexture;

    private OrthographicCamera camera;
    private Viewport viewport;

    public LevelSelectScreen(PegBallStart game){
        this.GAME = game;
        for (Level level : GAME.getLevelManager().getLevels()) {
            level.reset();
        }
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);


        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // test button loads test world
        TextButton levelButton = new TextButton("Normal Level", skin);
        levelButton.setWidth(200);
        levelButton.setHeight(50);
        levelButton.setColor(Color.LIME);
        levelButton.getLabel().setFontScale(1.7f);
        levelButton.setPosition(((Gdx.graphics.getWidth() * 3f)/4f) - (levelButton.getWidth() / 2),
            (Gdx.graphics.getHeight() / 2f) - (levelButton.getHeight() / 2));
        levelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, GAME.getLevelManager(), 1));
            }
        });
        stage.addActor(levelButton);

        TextButton levelButton2 = new TextButton("Test Level 1", skin);
        levelButton2.setWidth(200);
        levelButton2.setHeight(50);
        levelButton2.setColor(Color.LIME);
        levelButton2.getLabel().setFontScale(1.7f);
        levelButton2.setPosition((Gdx.graphics.getWidth() / 4f) - (levelButton2.getWidth() / 2),
            (Gdx.graphics.getHeight() / 2f) - (levelButton2.getHeight() / 2));
        levelButton2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, GAME.getLevelManager(), 0));
            }
        });
        stage.addActor(levelButton2);

        TextButton levelButton3 = new TextButton("Test Level 2", skin);
        levelButton3.setWidth(200);
        levelButton3.setHeight(50);
        levelButton3.setColor(Color.LIME);
        levelButton3.getLabel().setFontScale(1.7f);
        levelButton3.setPosition((Gdx.graphics.getWidth() / 2f) - (levelButton3.getWidth() / 2),
            (Gdx.graphics.getHeight() / 2f) - (levelButton3.getHeight() / 2));
        levelButton3.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, GAME.getLevelManager(), 2));
            }
        });
        stage.addActor(levelButton3);

        TextButton backButton = new TextButton("Back", skin);
        backButton.setWidth(70);
        backButton.setHeight(50);
        backButton.setColor(Color.RED);
        backButton.getLabel().setFontScale(1.7f);
        backButton.setPosition((Gdx.graphics.getWidth() / 2f) - (backButton.getWidth() / 2),
            (Gdx.graphics.getHeight() / 3f) - (backButton.getHeight() / 2));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new TitleScreen(GAME)); // back to title
            }
        });
        stage.addActor(backButton);

    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        backgroundTexture = new Texture(Gdx.files.internal("levelSelectScreen.png"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();

        stage.act(delta);
        stage.draw();
        //stage.setDebugAll(true);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        stage.getViewport().update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        backgroundTexture.dispose();
        stage.dispose();
        skin.dispose();
    }
}
