package com.emerson.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.emerson.gameobjects.*;
import com.emerson.gamescreens.CharacterSelectMenu;
import com.emerson.gamescreens.LevelSelectScreen;
import com.emerson.pegballgame.PegBallStart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class GameWorld {

    private final PegBallStart GAME;
    private final int TOTAL_PEGS = 100;

    private boolean isPaused = true;

    private World world;
    private GameContactListener gameContactListener;

    private Stage stage;
    private final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
    private Sound pauseSound;
    private Sound resumeSound;

    public CharacterSelectMenu characterSelectMenu;
    private Stage characterSelectStage;
    public boolean characterSelected = false;

    private TextButton pauseButton = new TextButton("Pause", skin);

    private Obstacle leftWall;
    private Obstacle rightWall;

    private Ball ball;
    private List<Peg> pegs = new ArrayList<>();
    private Map<Integer, Peg> pegMap;
    private List<Peg> pegsToDisappear = new ArrayList<>();
    private float timeSinceLastDisappear = 0f;
    private int pegIndex = 0;

    private BallLauncher ballLauncher;

    private OrthographicCamera camera;
    private Viewport viewport;

    public GameWorld(PegBallStart game) {

        pauseButton.setVisible(false);

        this.GAME = game;
        stage = new Stage(new ScreenViewport());
        pegMap = new HashMap<>();

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        characterSelectStage = new Stage();
        characterSelectMenu = new CharacterSelectMenu(characterSelectStage);
        Gdx.input.setInputProcessor(characterSelectStage); // character select gets the input processor
        System.out.println("char select stage has input processor");

        pauseSound = Gdx.audio.newSound(Gdx.files.internal("pauseSound.mp3"));
        resumeSound = Gdx.audio.newSound(Gdx.files.internal("resumeSound.mp3"));

        // on the first line, god created the world
        world = new World(new Vector2(0f, -50.0f), true);

        // on the second line, god created collision detection
        gameContactListener = new GameContactListener(pegMap, getPegs());
        world.setContactListener(gameContactListener);

        createObstacles();

        // on the third line, god initialized GameObjects
        createPegsStaggeredGrid(10, 40, 20f, viewport.getWorldWidth() / 2.8f, 150f, 7f);
        //createBrickPegsPattern(5, 40, 20f, viewport.getWorldWidth() / 2.8f, 130f, 15f, 10f);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run(){
                displayCharacterSelectMenu();
            }
        }, 1.5f);

        createBall(665f, 660f, 7f);
        createBallLauncher(640f, 670f, 50f, 35f);
        // randomize ball horizontal aiming for testing
        ballLauncher.setLaunchDirection((float)(Math.random() * 2) - 1, -1);

        pauseButton.setWidth(200);
        pauseButton.setHeight(150);
        pauseButton.setColor(Color.LIME);
        pauseButton.getLabel().setFontScale(3f);
        pauseButton.setPosition((Gdx.graphics.getWidth() / 7f) - (pauseButton.getWidth() / 2),
            (Gdx.graphics.getHeight() / 5f) - (pauseButton.getHeight() / 2));
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
                pauseSound.play();
                showPauseMenu();
            }
        });
        stage.addActor(pauseButton);


    }

    public void displayCharacterSelectMenu(){
        isPaused = true;
        characterSelectMenu.display();
    }

    public void removeCharacterSelectMenu(){
        characterSelectMenu.remove();
        characterSelectStage.dispose();
        isPaused = false;
        Gdx.input.setInputProcessor(stage); // world gets the input processor back for buttons and game inputs
        System.out.println("world stage has input processor");
        pauseButton.setVisible(true);
    }

    public void createObstacles() {

        BodyDef leftWallBodyDef = new BodyDef();
        leftWallBodyDef.position.set(200, 0);
        BodyDef rightWallBodyDef = new BodyDef();
        rightWallBodyDef.position.set(880, 0);
        Body leftWallBody = world.createBody(leftWallBodyDef);
        Body rightWallBody = world.createBody(rightWallBodyDef);
        PolygonShape shape = new PolygonShape();
        int leftWallWidth = 200;
        int leftWallHeight = 720;
        shape.setAsBox(leftWallWidth / 2, leftWallHeight / 2, new Vector2(leftWallWidth / 2, leftWallHeight / 2), 0);

        leftWall = new Obstacle(getWorld(), shape, leftWallBody, leftWallBody.getPosition(),leftWallWidth, leftWallHeight);
        rightWall = new Obstacle(getWorld(), shape, rightWallBody, rightWallBody.getPosition(), leftWallWidth, leftWallHeight);
        shape.dispose();
    }

    public void createBallLauncher(float startX, float startY, float width, float height) {
        // ALL CREATION AND DEFINITION STUFF SHOULD BE IN OBJECT CLASSES LATER ON
        // gotta make def position and body for constructor but thats all
        BodyDef ballLauncherDef = new BodyDef();
        ballLauncherDef.position.set(startX, startY);
        Body ballLauncherBody = world.createBody(ballLauncherDef);

        ballLauncher = new BallLauncher(this, ballLauncherBody, ballLauncherBody.getPosition(), width, height);
    }


    public void createBall(float startX, float startY, float radius){
        BodyDef ballDef = new BodyDef();
        ballDef.position.set(startX, startY); // position in the world or on screen idk
        Body ballBody = world.createBody(ballDef);

        ball = new Ball(getWorld(), ballBody, ballBody.getPosition(), radius);

    }

    int pegID = 1;
    // creates a staggered grid of pegs and adds them to a list which is used for rendering and a map used for collision
    public void createPegsStaggeredGrid(int rows, float pegSpacing, float rowOffset, float startX, float startY, float radius) {

        int cols = TOTAL_PEGS / rows;

        // maybe peg spacing can be automatically calculated but I don't feel like it right now
        for (int row = 0; row < rows; row++){
            for (int col = 0; col < cols; col++){

                float x = startX + col * pegSpacing;
                float y = startY + row * pegSpacing;

                if (row % 2 == 0) {
                    x += rowOffset;
                }

                // define peg and stuff (type is static because it doesn't move yet)
                BodyDef pegDef = new BodyDef();
                pegDef.position.set(x, y);

                Body pegBody = world.createBody(pegDef);
                // create peg and add to list
                Peg peg = new Peg(this, pegBody, pegBody.getPosition(), radius, pegID++);
                pegs.add(peg);
                pegMap.put(peg.getPegID(), peg);

            }
        }
    }


    public void createBrickPegsPattern(int rows, float pegSpacing, float rowOffset, float startX, float startY, float width, float height) {
        int cols = TOTAL_PEGS / rows; // do 5 rows

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // stagger
                float offsetX = (row % 2 == 0) ? 0 : width / 2;
                Vector2 position = new Vector2(startX + col * width + offsetX, startY - row * height);

                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set(position);

                Body body = world.createBody(bodyDef);

                BrickPeg brickPeg = new BrickPeg(this, body, position, width, height, pegID++);
                pegs.add(brickPeg);
                pegMap.put(brickPeg.getPegID(), brickPeg);
            }
        }
    }

    public void addPegToDisappearQueue(Peg peg) {
        pegsToDisappear.add(peg);
    }

    boolean ballShot = false;
    int launchDelay = 60;
    boolean readyToShoot = false;
    private void showBallOutOfBoundsMessage() {
        Label messageLabel = new Label("PEGS HIT: " + gameContactListener.getPegsHit(), skin);
        messageLabel.setColor(Color.RED);
        messageLabel.setPosition((viewport.getWorldWidth() / 2) - (messageLabel.getWidth()),
            (viewport.getWorldHeight() / 2));
        messageLabel.setFontScale(2f);
        stage.addActor(messageLabel);
    }

    public void endTurn(){

        for (int i = 0; i < pegsToDisappear.size(); i++) {
            final int index = i;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    Peg peg = pegsToDisappear.get(index);
                    peg.pegDisappear(getWorld(), getPegMap(), getPegs());
                }
            }, i * 0.2f);

        // original end turn, gets rid of all pegs at once
        /*
        for (Peg peg : pegs) {
            if (peg.isHit()) {
                peg.pegDisappear(getWorld(), getPegMap(), getPegs());
            }
        }
        */
        }
    }


    public void update(float deltaTime) {

        if (isPaused) {
            if (!characterSelected) {
                if (characterSelectMenu.characterSelected) {
                    removeCharacterSelectMenu();
                    characterSelected = true;
                }
            }
            return; // dont update if the game is paused (duh)
        }

        // physics and world updates
        float timeStep = 1/60f;  // 60 times per second
        int velocityIterations = 6;  //
        int positionIterations = 2;  //

        world.step(timeStep, velocityIterations, positionIterations);

        ballLauncher.update(deltaTime);
        ball.update(deltaTime);



        for (Peg peg : pegs) {
            peg.update(deltaTime);
        }

        if (ball.getPosition().y < 0 ||
            ball.getPosition().y > stage.getViewport().getWorldHeight()
            || ball.getPosition().x < 0 ||
            ball.getPosition().x > stage.getViewport().getWorldWidth()) {
            endTurn();
            showBallOutOfBoundsMessage();
        }

        // ball shooting test mechanism that took me 3 fucking hours
        if (launchDelay > 0) {
            launchDelay--;
        } else {
            readyToShoot = true;
            ball.getBody().setType(BodyDef.BodyType.DynamicBody);
        }

        if (!ballShot && readyToShoot) {
            ballLauncher.shootBall(ball);
            readyToShoot = false;
            ballShot = true;
        }

    }

    public void togglePause() {
        if (isPaused) { // if the game is paused
            isPaused = false; // resume it
            pauseButton.setVisible(true); // put the pause button back
        }
        else { // if game is running
            isPaused = true; // pause game
            pauseButton.setVisible(false); // take away pause button
        }


    }

    public void showPauseMenu() {

        Window pauseMenu = new Window("Paused", skin);
        TextButton resumeButton = new TextButton("Resume", skin);
        TextButton quitToLevelSelectButton = new TextButton("Quit to Level Select", skin);

        resumeButton.setWidth(100);
        resumeButton.setHeight(50);
        resumeButton.setColor(Color.LIME);
        resumeButton.getLabel().setFontScale(2f);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause(); // resumes the game
                resumeSound.play();
                pauseMenu.remove(); // hides the menu
            }
        });

        quitToLevelSelectButton.setWidth(100);
        quitToLevelSelectButton.setHeight(50);
        quitToLevelSelectButton.setColor(Color.RED);
        quitToLevelSelectButton.getLabel().setFontScale(2f);
        quitToLevelSelectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new LevelSelectScreen(GAME)); // switch to level select menu
            }
        });

        pauseMenu.add(resumeButton).row();
        pauseMenu.add(quitToLevelSelectButton);
        pauseMenu.setSize(400, 400);
        pauseMenu.setPosition((stage.getWidth() / 2) - (pauseMenu.getWidth() / 2),
            (stage.getHeight() / 2) - (pauseMenu.getHeight() / 2));
        stage.addActor(pauseMenu);
    }


    public World getWorld() {
        return world;
    }

    public List<Peg> getPegs(){
        return pegs;
    }

    public Map<Integer, Peg> getPegMap(){
        return pegMap;
    }

    public void renderObjects(ShapeRenderer shapeRenderer) {
        // I have colors here for later when characters might change things up

        shapeRenderer.setColor(Color.BLACK);
        leftWall.render(shapeRenderer);
        rightWall.render(shapeRenderer);

        // renders pegs using list
        for (Peg peg : pegs) {
            if(peg.isHit()) {
                // can also check type here to handle other colors
                shapeRenderer.setColor(Color.CYAN);
                peg.render(shapeRenderer);
            } else {
                shapeRenderer.setColor(0, 0, 1, 1);
                peg.render(shapeRenderer);
            }
        }

        shapeRenderer.setColor(1, 0.5f, 0.5f, 1);
        ball.render(shapeRenderer);

        shapeRenderer.setColor(Color.TAN);
        ballLauncher.render(shapeRenderer);

    }

    public void renderStage(float delta) {
        if (!characterSelectMenu.characterSelected) {
            characterSelectMenu.render(delta);
        }
        stage.act(delta);
        stage.draw();
        //stage.setDebugAll(true); // pause button is weird and I have no idea why
    }
}
