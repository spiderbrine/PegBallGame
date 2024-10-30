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
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // test button loads test world
        TextButton levelButton = new TextButton("Start Test Level", skin);
        levelButton.setWidth(200);
        levelButton.setHeight(50);
        levelButton.setColor(Color.LIME);
        levelButton.getLabel().setFontScale(1.7f);
        levelButton.setPosition((Gdx.graphics.getWidth() / 2f) - (levelButton.getWidth() / 2),
            (Gdx.graphics.getHeight() / 2f) - (levelButton.getHeight() / 2));
        levelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME)); // load test world
            }
        });
        stage.addActor(levelButton);

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
                GAME.setScreen(new TitleScreen(GAME)); // load test world
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
