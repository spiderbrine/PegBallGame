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
import com.emerson.gameobjects.Ball;
import com.emerson.gameobjects.BallLauncher;
import com.emerson.gameobjects.Peg;
import com.emerson.gamescreens.CharacterSelectMenu;
import com.emerson.gamescreens.LevelSelectScreen;
import com.emerson.pegballgame.PegBallStart;

import java.util.ArrayList;
import java.util.List;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class GameWorld {

    private final PegBallStart GAME;

    private boolean isPaused = true;

    private World world;

    private Stage stage;
    private final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
    private Sound pauseSound;
    private Sound resumeSound;

    public CharacterSelectMenu characterSelectMenu;
    private Stage characterSelectStage;
    public boolean characterSelected = false;

    private TextButton pauseButton = new TextButton("Pause", skin);

    private Ball ball;
    List<Peg> pegs = new ArrayList<>();
    private BallLauncher ballLauncher;

    private OrthographicCamera camera;
    private Viewport viewport;

    public GameWorld(PegBallStart game) {

        pauseButton.setVisible(false);

        this.GAME = game;
        stage = new Stage(new ScreenViewport());

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
        world.setContactListener(new GameContactListener());


        // on the third line, god initialized GameObjects
        createPegsStaggeredGrid(100, 10, 15, 40f, 25f, 350f, 75f, 7f);

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

    public void createBallLauncher(float startX, float startY, float width, float height) {
        // ALL CREATION AND DEFINITION STUFF SHOULD BE IN OBJECT CLASSES LATER ON
        BodyDef ballLauncherDef = new BodyDef();
        ballLauncherDef.type = BodyDef.BodyType.KinematicBody; // not affected by world forces but able to be moved manually
        ballLauncherDef.position.set(startX, startY);
        Body ballLauncherBody = world.createBody(ballLauncherDef);

        PolygonShape rectangle = new PolygonShape();
        // weird math shit cause the rectangle goes from the actual middle not bottom left corner
        rectangle.setAsBox(width / 2, height / 2, new Vector2(width / 2, height / 2), 0);

        FixtureDef ballLauncherFixtureDef = new FixtureDef();
        ballLauncherFixtureDef.shape = rectangle;
        ballLauncherBody.createFixture(ballLauncherFixtureDef);

        rectangle.dispose();

        ballLauncher = new BallLauncher(this, ballLauncherBody, ballLauncherBody.getPosition(), width, height);
    }


    public void createBall(float startX, float startY, float radius){
        BodyDef ballDef = new BodyDef();
        //ballDef.type = BodyDef.BodyType.StaticBody;  // static because the ball can't move until it shoots
        System.out.println("Ball is static");
        ballDef.position.set(startX, startY); // position in the world or on screen idk

        Body ballBody = world.createBody(ballDef);
        //ballBody.setLinearDamping(0);
        //ballBody.setAngularDamping(0);
        ballBody.setGravityScale(0); // disable gravity
        ballBody.setLinearVelocity(0, 0);  // Keep it from moving

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        // fixture that attaches circle to body
        FixtureDef ballFixtureDef = new FixtureDef();
        ballFixtureDef.shape = circle;
        ballFixtureDef.density = 0.85f; // mass 0.85 is good
        ballFixtureDef.friction = 0.2f; // 0.2 is good
        ballFixtureDef.restitution = 0.78f;  // bounce 0.75 is good

        ballBody.createFixture(ballFixtureDef);

        circle.dispose();

        ball = new Ball(ballBody, ballBody.getPosition(), radius);

    }

    // creates a staggered grid of pegs and adds them to a list which is used for rendering
    // numPegs does nothing currently but will be used to set a number of pegs to be created
    // rather than an amount calculated by row/col and spacing values
    // also need a way to ID each peg when it comes time for assignment of colors and behaviors
    public void createPegsStaggeredGrid(int numPegs, int rows, int cols, float pegSpacing, float rowOffset,
                                        float startX, float startY, float radius) {

        // uses same single circle to make pegs to save on resources then disposes afterward
        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        // maybe peg spacing can be automatically calculated but I don't feel like it right now
        for (int row = 0; row < rows; row++){
            for (int col = 0; col < cols; col++){
                float pegX = startX + col * pegSpacing + (row % 2 == 0 ? rowOffset : 0);
                float pegY = startY + row * pegSpacing;

                // define peg and stuff (type is static because it doesn't move yet)
                BodyDef pegDef = new BodyDef();
                pegDef.type = BodyDef.BodyType.StaticBody;
                pegDef.position.set(pegX, pegY);

                Body pegBody = world.createBody(pegDef);

                FixtureDef pegFixtureDef = new FixtureDef();
                pegFixtureDef.shape = circle;
                pegFixtureDef.density = 0.5f;  // mass
                pegFixtureDef.friction = 0.2f;
                pegFixtureDef.restitution = 0.7f;  // bounce

                pegBody.createFixture(pegFixtureDef);

                // create peg and add to list
                Peg peg = new Peg(pegBody, pegBody.getPosition(), radius);
                pegs.add(peg);
            }
        }

        circle.dispose();
    }

    boolean ballShot = false;
    int launchDelay = 60;
    boolean readyToShoot = false;
    private void showBallOutOfBoundsMessage() {
        Label messageLabel = new Label("OH MY GOD THE BALL HAS LEFT THE SCREEN", skin);
        messageLabel.setColor(Color.RED);
        messageLabel.setPosition((viewport.getWorldWidth() / 2) - (messageLabel.getWidth()),
            (viewport.getWorldHeight() / 2));
        messageLabel.setFontScale(2f);
        stage.addActor(messageLabel);
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

            showBallOutOfBoundsMessage();
        }

        // ball shooting test mechanism that took me 3 fucking hours
        if (launchDelay > 0) {
            launchDelay--;
        } else {
            readyToShoot = true;
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

    public void renderObjects(ShapeRenderer shapeRenderer) {
        // I have colors here for later when characters might change things up

        // renders pegs using list
        shapeRenderer.setColor(0, 0, 1, 1);
        for (Peg peg : pegs) {
            peg.render(shapeRenderer);
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
        //stage.setDebugAll(true);
    }
}
