package com.emerson.gamescreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.emerson.gameobjects.Peg;
import com.emerson.pegballgame.PegBallStart;
import com.emerson.world.GameWorld;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class LevelEditorScreen implements Screen {
    private final PegBallStart GAME;
    private Stage stage;
    private Skin skin;
    private Array<Vector2> pegPositions; // Store peg positions
    private SpriteBatch batch;
    private Texture backgroundTexture = new Texture("background.png");
    private TextButton backButton;
    private TextButton saveButton;
    private TextButton loadButton;
    private Label pegsPlacedLabel;
    private Table table;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;

    public LevelEditorScreen(PegBallStart game) {
        this.GAME = game;

        pegPositions = new Array<>();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        this.batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        setupUI();
    }

    private void setupUI() {

        backButton = new TextButton("Back", skin);
        backButton.setTouchable(Touchable.enabled);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Back button clicked");
                GAME.setScreen(new TitleScreen(GAME));
                event.stop();
            }
        });

        saveButton = new TextButton("Save", skin);
        saveButton.setTouchable(Touchable.enabled);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Save button clicked");
                saveLevel("level.json");
                event.stop();
            }
        });


        loadButton = new TextButton("Load", skin);
        loadButton.setTouchable(Touchable.enabled);
        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Load button clicked");
                loadLevel("level.json");
                event.stop();
            }
        });

        pegsPlacedLabel = new Label("" + pegPositions.size, skin);
        pegsPlacedLabel.setColor(Color.BLUE);

        table = new Table();
        table.setFillParent(false);
        table.setPosition(100, 20);
        table.add(backButton).pad(10);
        table.add(saveButton).pad(10);
        table.add(loadButton).pad(10);
        table.add(pegsPlacedLabel).pad(10);
        stage.addActor(table);
        table.pack();
        System.out.println("Table Dimensions: Width = " + table.getWidth() + ", Height = " + table.getHeight());
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);  // rgba (white)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();


        // render peg visuals
        stage.getBatch().begin();
        for (Vector2 pos : pegPositions) {
            stage.getBatch().draw(new Texture("peg.png"), pos.x - 10, pos.y - 10, 20, 20);
        }
        stage.getBatch().end();
        pegsPlacedLabel.setText("" + pegPositions.size);
        stage.act(delta);
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && checkClick()) {
            Vector2 cursorPosition = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            addPeg(cursorPosition.x, cursorPosition.y);
        }
        stage.draw();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(400, 0, 480, 45);
        shapeRenderer.end();
        //stage.setDebugAll(true);
    }

    private boolean checkClick() {
        Vector2 cursorPosition = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        System.out.println("Cursor Position: " + cursorPosition);
        System.out.println("Table Bounds: X=" + table.getX() + ", Y=" + table.getY() +
            ", Width=" + table.getWidth() + ", Height=" + table.getHeight());

        // check if cursor is over save or load
        if (checkTable()) {
            System.out.println("Mouse is over the table");
            return false;
        }
        System.out.println("Either this is bugged or im trippin");
        return true;
    }

    private boolean checkTable() {
        Vector2 cursorPosition = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        // calculate bounds
        float tableX = table.getX();
        float tableY = table.getY();
        float tableWidth = table.getWidth();
        float tableHeight = table.getHeight();

        // is cursor in table bounds
        boolean isOver = cursorPosition.x >= tableX && cursorPosition.x <= tableX + tableWidth &&
            cursorPosition.y >= tableY && cursorPosition.y <= tableY + tableHeight;
        System.out.println("Checking table: Over = " + isOver);
        return isOver;
    }

    private void addPeg(float x, float y) {
        pegPositions.add(new Vector2(x, y));
        System.out.println("Peg added at: " + x + ", " + y);
    }

    private void saveLevel(String fileName) {
        Json json = new Json();
        String jsonData = json.toJson(pegPositions);
        FileHandle file = Gdx.files.local(fileName);
        file.writeString(jsonData, false);
        System.out.println("Level saved as " + fileName);
    }

    private void loadLevel(String fileName) {
        pegPositions.clear();

        FileHandle file = Gdx.files.local(fileName);
        if (!file.exists()) {
            System.out.println("No saved level found!");
            return;
        }

        Json json = new Json();
        pegPositions = json.fromJson(Array.class, Vector2.class, file.readString());
        System.out.println("Level loaded from " + fileName);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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

    // Other Screen interface methods (pause, resume, hide, show) can be left empty
}
