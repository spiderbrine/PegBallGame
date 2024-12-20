package com.emerson.gamescreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.emerson.pegballgame.PegBallStart;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class TitleScreen implements Screen {

    private final PegBallStart GAME;
    private Stage stage;
    private Skin skin;

    private SpriteBatch batch;
    private Texture backgroundTexture;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Music titleMusic;
    private Sound confirmSound;

    public TitleScreen(PegBallStart game){
        this.GAME = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        // ui skin for buttons
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        titleMusic = Gdx.audio.newMusic(Gdx.files.internal("titleMusic.mp3"));
        titleMusic.setVolume(0.45f);
        titleMusic.setLooping(true);
        titleMusic.play();
        confirmSound = Gdx.audio.newSound(Gdx.files.internal("confirmSound.mp3"));

        Label tipLabel = new Label("Pro Tip: The Ball-O-Tron 2.0 doesn't always start in the most advantageous spot...", skin);
        tipLabel.setColor(Color.GREEN);
        tipLabel.setPosition((Gdx.graphics.getWidth()/2)-(tipLabel.getWidth()/2), 10);
        stage.addActor(tipLabel);

        // play button
        TextButton playButton = new TextButton("Play", skin);
        playButton.setWidth(200);
        playButton.setHeight(150);
        playButton.setColor(Color.LIME);
        playButton.getLabel().setFontScale(3f);
        playButton.setPosition((Gdx.graphics.getWidth() / 2f) - (playButton.getWidth() / 2),
            (Gdx.graphics.getHeight() / 2f) - (playButton.getHeight() / 2));
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                titleMusic.stop();
                confirmSound.play();
                game.setScreen(new LevelSelectScreen(game)); // go to level select
            }
        });
        stage.addActor(playButton);

        // add more buttons (leaderboard) LOL no leaderboard but heres a level editor
        TextButton editButton = new TextButton("Edit", skin);
        editButton.setWidth(150);
        editButton.setHeight(100);
        editButton.setColor(Color.ORANGE);
        editButton.getLabel().setFontScale(3f);
        editButton.setPosition((Gdx.graphics.getWidth() / 2f) - (editButton.getWidth() / 2),
            (Gdx.graphics.getHeight() / 3f) - (editButton.getHeight() / 2) - 50);
        editButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                titleMusic.stop();
                confirmSound.play();
                game.setScreen(new LevelEditorScreen(game));
            }
        });
        stage.addActor(editButton);
    }


    @Override
    public void show() {
        batch = new SpriteBatch();
        backgroundTexture = new Texture(Gdx.files.internal("titleScreen.png"));
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
