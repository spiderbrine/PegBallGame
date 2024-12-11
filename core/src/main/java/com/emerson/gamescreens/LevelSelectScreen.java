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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.emerson.pegballgame.PegBallStart;
import com.emerson.savedata.GameDataManager;
import com.emerson.savedata.SaveData;
import com.emerson.world.Level;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class LevelSelectScreen implements Screen {

    private final PegBallStart GAME;
    private Stage stage;
    private Skin skin;

    private Table table;

    private final float buttonWidth = 250f;
    private final float buttonHeight = 75f;


    private SpriteBatch batch;
    private Texture backgroundTexture;

    private OrthographicCamera camera;
    private Viewport viewport;

    public LevelSelectScreen(PegBallStart game){
        this.GAME = game;
        SaveData saveData = GAME.getGameDataManager().loadGameData();
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
        TextButton levelButton = new TextButton(GAME.getLevelManager().getLevel(1).getLevelName() + "\nHigh Score: "
            + saveData.highScores.get(GAME.getLevelManager().getLevel(1).getLevelName()), skin);

        if (saveData.levelCompletion.getOrDefault(GAME.getLevelManager().getLevel(1).getLevelName(), false)) {
            levelButton.setColor(Color.LIME);
        } else {
            levelButton.setColor(Color.RED);
        }
        levelButton.getLabel().setFontScale(1.7f);

        levelButton.setWidth(buttonWidth);
        levelButton.setHeight(buttonHeight);

        //levelButton.setPosition(((Gdx.graphics.getWidth() * 3f)/4f) - (levelButton.getWidth() / 2), (Gdx.graphics.getHeight() / 2f) - (levelButton.getHeight() / 2));
        levelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, 1));
            }
        });
        //stage.addActor(levelButton);

        TextButton levelButton2 = new TextButton(GAME.getLevelManager().getLevel(0).getLevelName() + "\nHigh Score: "
            + saveData.highScores.get(GAME.getLevelManager().getLevel(0).getLevelName()), skin);
        levelButton2.setWidth(buttonWidth);
        levelButton2.setHeight(buttonHeight);

        if (saveData.levelCompletion.getOrDefault(GAME.getLevelManager().getLevel(0).getLevelName(), false)) {
            levelButton2.setColor(Color.LIME);
        } else {
            levelButton2.setColor(Color.RED);
        }
        levelButton2.getLabel().setFontScale(1.7f);
        //levelButton2.setPosition((Gdx.graphics.getWidth() / 4f) - (levelButton2.getWidth() / 2), (Gdx.graphics.getHeight() / 2f) - (levelButton2.getHeight() / 2));
        levelButton2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, 0));
            }
        });
        //stage.addActor(levelButton2);

        TextButton levelButton3 = new TextButton(GAME.getLevelManager().getLevel(2).getLevelName() + "\nHigh Score: "
            + saveData.highScores.get(GAME.getLevelManager().getLevel(2).getLevelName()), skin);
        levelButton3.setWidth(buttonWidth);
        levelButton3.setHeight(buttonHeight);

        if (saveData.levelCompletion.getOrDefault(GAME.getLevelManager().getLevel(2).getLevelName(), false)) {
            levelButton3.setColor(Color.LIME);
        } else {
            levelButton3.setColor(Color.RED);
        }
        levelButton3.getLabel().setFontScale(1.7f);
        //levelButton3.setPosition((Gdx.graphics.getWidth() / 2f) - (levelButton3.getWidth() / 2), (Gdx.graphics.getHeight() / 2f) - (levelButton3.getHeight() / 2));
        levelButton3.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, 2));
            }
        });
        //stage.addActor(levelButton3);

        TextButton levelButton4 = new TextButton(GAME.getLevelManager().getLevel(3).getLevelName() + "\nHigh Score: "
            + saveData.highScores.get(GAME.getLevelManager().getLevel(3).getLevelName()), skin);
        levelButton4.setWidth(buttonWidth);
        levelButton4.setHeight(buttonHeight);

        if (saveData.levelCompletion.getOrDefault(GAME.getLevelManager().getLevel(3).getLevelName(), false)) {
            levelButton4.setColor(Color.LIME);
        } else {
            levelButton4.setColor(Color.RED);
        }
        levelButton4.getLabel().setFontScale(1.7f);
        //levelButton4.setPosition((Gdx.graphics.getWidth() / 2f) - (levelButton4.getWidth() / 2), (Gdx.graphics.getHeight() / 2f) - (levelButton4.getHeight() / 2) + 80);
        levelButton4.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, 3));
            }
        });
        //stage.addActor(levelButton4);

        TextButton levelButton5 = new TextButton(GAME.getLevelManager().getLevel(4).getLevelName() + "\nHigh Score: "
            + saveData.highScores.get(GAME.getLevelManager().getLevel(4).getLevelName()), skin);
        levelButton5.setWidth(buttonWidth);
        levelButton5.setHeight(buttonHeight);

        if (saveData.levelCompletion.getOrDefault(GAME.getLevelManager().getLevel(4).getLevelName(), false)) {
            levelButton5.setColor(Color.LIME);
        } else {
            levelButton5.setColor(Color.RED);
        }
        levelButton5.getLabel().setFontScale(1.7f);
        //levelButton5.setPosition((Gdx.graphics.getWidth() / 2f) - (levelButton5.getWidth() / 2), (Gdx.graphics.getHeight() / 2f) - (levelButton5.getHeight() / 2) + 80);
        levelButton5.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, 4));
            }
        });
        //stage.addActor(levelButton5);

        TextButton levelButton6 = new TextButton(GAME.getLevelManager().getLevel(5).getLevelName() + "\nHigh Score: "
            + saveData.highScores.get(GAME.getLevelManager().getLevel(5).getLevelName()), skin);
        levelButton6.setWidth(buttonWidth);
        levelButton6.setHeight(buttonHeight);

        if (saveData.levelCompletion.getOrDefault(GAME.getLevelManager().getLevel(5).getLevelName(), false)) {
            levelButton6.setColor(Color.LIME);
        } else {
            levelButton6.setColor(Color.RED);
        }
        levelButton6.getLabel().setFontScale(1.7f);
        //levelButton6.setPosition((Gdx.graphics.getWidth() / 2f) - (levelButton6.getWidth() / 2), (Gdx.graphics.getHeight() / 2f) - (levelButton6.getHeight() / 2) + 80);
        levelButton6.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME, 5));
            }
        });
        //stage.addActor(levelButton6);

        table = new Table();
        table.setFillParent(false);
        table.add(levelButton).pad(10);
        table.add(levelButton2).pad(10);
        table.add(levelButton3).pad(10);
        table.add(levelButton4).pad(10);
        table.add(levelButton5).pad(10);
        table.add(levelButton6).pad(10);
        stage.addActor(table);
        // pack then set position
        table.pack();
        table.setPosition((Gdx.graphics.getWidth()/2f)-(table.getWidth()/2), Gdx.graphics.getHeight()/2f);

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
