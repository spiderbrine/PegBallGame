package com.emerson.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

import java.util.*;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class GameWorld {

    private final PegBallStart GAME;
    private final int TOTAL_PEGS = 100;
    private final int NUM_ORANGE_PEGS = 25;

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

    // balls (haha)
    private Queue<Ball> ballPool = new LinkedList<>();
    private int maxBalls = 50; // max number of balls to account for (bonus level is why its so high)
    private int ballsLeft;
    private Label ballsLeftLabel;
    private Label scoreLabel;
    private Label orangePegsHitLabel;

    private Ball ball;
    private List<Peg> pegs = new ArrayList<>();
    private Map<Integer, Peg> pegMap;
    private List<Peg> pegsToDisappear = new ArrayList<>();
    private float timeSinceLastDisappear = 0f;
    private int pegIndex = 0;

    private FreeBallBucket freeBallBucket;

    private BallLauncher ballLauncher;
    private Vector2 launchPosition = new Vector2(665, 660);

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

        world = new World(new Vector2(0f, -50.0f), true);

        gameContactListener = new GameContactListener(pegMap, getPegs());
        world.setContactListener(gameContactListener);

        createObstacles();

        createPegsStaggeredGrid(10, 40, 20f, viewport.getWorldWidth() / 2.8f, 150f, 7f);
        //createBrickPegsPattern(5, 40, 20f, viewport.getWorldWidth() / 2.8f, 130f, 15f, 10f);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run(){
                displayCharacterSelectMenu();
            }
        }, 1.5f);

        //createBall(665f, 660f, 7f);
        initializeBallPool(10);
        createLabels();
        ball = getBall();
        createBallLauncher(640f, 670f, 50f, 35f);
        // randomize ball horizontal aiming for testing
        //ballLauncher.setLaunchDirection((float)(Math.random() * 2) - 1, -1);
        ballLauncher.setBallLauncherLoaded(true);
        launchPosition.set(ballLauncher.getPosition().x + ballLauncher.getWidth()/2, ballLauncher.getPosition().y - 10);
        createFreeBallBucket();

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

    public void createFreeBallBucket() {
        BodyDef bucketDef = new BodyDef();
        bucketDef.position.set(640, 0);
        Body bucketBody = world.createBody(bucketDef);
        freeBallBucket = new FreeBallBucket(bucketBody, bucketBody.getPosition(), 80, 45, 150);
    }

    public void createBallLauncher(float startX, float startY, float width, float height) {
        // ALL CREATION AND DEFINITION STUFF SHOULD BE IN OBJECT CLASSES LATER ON
        // gotta make def position and body for constructor but thats all
        BodyDef ballLauncherDef = new BodyDef();
        ballLauncherDef.position.set(startX, startY);
        Body ballLauncherBody = world.createBody(ballLauncherDef);

        ballLauncher = new BallLauncher(this, ballLauncherBody, ballLauncherBody.getPosition(), width, height);
    }


    public Ball createBall(float startX, float startY, float radius){
        BodyDef ballDef = new BodyDef();
        ballDef.position.set(startX, startY); // position in the world or on screen idk
        Body ballBody = world.createBody(ballDef);
        ballBody.setActive(false);

        Ball ball = new Ball(getWorld(), ballBody, ballBody.getPosition(), radius);
        return ball;
    }

    public void initializeBallPool(int initialCount) {
        for (int i = 0; i < initialCount; i++) {
            ballPool.offer(createBall(launchPosition.x, launchPosition.y, 7f));
            int ballNum = i + 1;
            System.out.println("Ball " + ballNum + " created");
        }
        ballsLeft = initialCount;
    }

    public Ball getBall() {
        if (!ballPool.isEmpty()) {
            Ball ball = ballPool.poll();  // get from pool
            ball.getBody().setActive(true);  // reactivate the ball
            ballsLeft--;
            System.out.println("Balls left" + getBallsLeft());
            return ball;
        } else {
            if (ballPool.size() + 1 <= maxBalls) {
                // if max balls isnt reached and you need a new one, make a new one
                System.out.println("New ball created");
                return createBall(launchPosition.x, launchPosition.y, 7f);
            } else {
                System.out.println("Max balls reached");
                return null;
            }
        }
    }

    public void prepareNextBall(){
        ball = getBall();
    }

    public void returnBallToPool(Ball ball) {
        ball.getBody().setLinearVelocity(0, 0);  // reset velocity
        ball.setPosition(launchPosition);  // reset position
        ball.getBody().setType(BodyDef.BodyType.StaticBody);
        ball.getBody().setActive(false);  // deactivate ball
        ballPool.offer(ball);  // return to the pool for future reuse
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
        // use RNG to do other peg colors and add them to their own lists and remove them from the original list
        List<Integer> allPegIDs = new ArrayList<>();
        for (Peg peg : pegs) {
            allPegIDs.add(peg.getPegID());
        }
        Collections.shuffle(allPegIDs);

        List<Integer> orangePegs = new ArrayList<>(allPegIDs.subList(0, NUM_ORANGE_PEGS));
        for (Peg peg : pegs) {
            for (int i = 0; i < orangePegs.size(); i++) {
                if (peg.getPegID() == orangePegs.get(i)) {
                    peg.setPegType(2);
                }
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

    public boolean checkBallOutOfBounds() {
        if (ball.getPosition().y < 0) {
            return true;
        } else {
            return false;
        }
    }

    public void endTurn(){

        // update score message
        int newTotalScore = gameContactListener.getTurnScore() * gameContactListener.getPegsHit();
        int totalScore = gameContactListener.getTotalScore() + newTotalScore;
        gameContactListener.setTotalScore(totalScore);

        for (int i = 0; i < pegsToDisappear.size(); i++) {
            final int index = i;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    Peg peg = pegsToDisappear.get(index);
                    peg.pegDisappear(getWorld(), getPegMap(), getPegs());
                }
            }, i * 0.2f);
        }

        returnBallToPool(ball);
    }

    private void showBallOutOfBoundsMessage() {
        Label messageLabel = new Label("PEGS HIT: " + gameContactListener.getPegsHit(), skin);
        messageLabel.setColor(Color.BLACK);
        messageLabel.setPosition((viewport.getWorldWidth() / 2) - (messageLabel.getWidth()),
            (viewport.getWorldHeight() / 2));
        messageLabel.setFontScale(2f);
        stage.addActor(messageLabel);

        Label messageLabel2 = new Label("ORANGE PEGS HIT: " + gameContactListener.getOrangePegsHit(), skin);
        messageLabel2.setColor(Color.ORANGE);
        messageLabel2.setPosition((viewport.getWorldWidth() / 2) - (messageLabel2.getWidth()),
            (viewport.getWorldHeight() / 2) - 40);
        messageLabel2.setFontScale(2f);
        stage.addActor(messageLabel2);

        int newTotalScore = gameContactListener.getTurnScore() * gameContactListener.getPegsHit();
        Label turnScoreLabel = new Label(gameContactListener.getTurnScore() + " X " + gameContactListener.getPegsHit() + " PEGS = " + newTotalScore, skin);
        turnScoreLabel.setColor(Color.BLACK);
        turnScoreLabel.setPosition((viewport.getWorldWidth() / 2) - (turnScoreLabel.getWidth()),
            (viewport.getWorldHeight() / 2) - 80);
        turnScoreLabel.setFontScale(2f);
        stage.addActor(turnScoreLabel);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageLabel.remove();
                messageLabel2.remove();
                turnScoreLabel.remove();
                gameContactListener.resetTurnScore();
                updateScoreLabel();
                pegsToDisappear.clear();
                if (ballsLeft == 0) {
                    Label noMoreBalls = new Label("NO MORE BALLS", skin);
                    noMoreBalls.setColor(Color.RED);
                    noMoreBalls.setPosition((viewport.getWorldWidth() / 2) - (noMoreBalls.getWidth()),
                        (viewport.getWorldHeight() / 2));
                    noMoreBalls.setFontScale(2f);
                    stage.addActor(noMoreBalls);
                } else if (checkOrangePegs()) {
                    showWinMessage();
                } else {
                    prepareNextBall();
                    ballLauncher.setBallLauncherLoaded(true);
                }
            }
        }, (((float)gameContactListener.getPegsHit()) * .2f));

        gameContactListener.resetPegsHit();
        gameContactListener.resetOrangePegsHit();
    }

    private void createLabels() {
        ballsLeftLabel = new Label("Balls left: " + getBallsLeft(), skin);
        ballsLeftLabel.setColor(Color.RED);
        ballsLeftLabel.setPosition(225, Gdx.graphics.getHeight() - 50);
        ballsLeftLabel.setFontScale(1.5f);
        stage.addActor(ballsLeftLabel);

        scoreLabel = new Label("Score: " + gameContactListener.getTotalScore(), skin);
        scoreLabel.setColor(Color.BLACK);
        scoreLabel.setPosition(425, Gdx.graphics.getHeight() - 50);
        scoreLabel.setFontScale(1.5f);
        stage.addActor(scoreLabel);

        orangePegsHitLabel = new Label("Orange Pegs hit: " + gameContactListener.getTotalOrangePegsHit(), skin);
        orangePegsHitLabel.setColor(Color.ORANGE);
        orangePegsHitLabel.setPosition(885, Gdx.graphics.getHeight() - 50);
        orangePegsHitLabel.setFontScale(1.3f);
        stage.addActor(orangePegsHitLabel);
    }

    private void updateLabels() {
        ballsLeftLabel.setText("Balls left: " + getBallsLeft());

        orangePegsHitLabel.setText("Orange Pegs hit: " + gameContactListener.getTotalOrangePegsHit());
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + gameContactListener.getTotalScore());
    }

    private boolean checkOrangePegs() {
        return gameContactListener.getTotalOrangePegsHit() == 25;
    }

    public void showWinMessage() {
        Label winMessage = new Label("YOU WIN!!!", skin);
        winMessage.setColor(Color.GREEN);
        winMessage.setPosition((viewport.getWorldWidth() / 2) - (winMessage.getWidth()),
            (viewport.getWorldHeight() / 2));
        winMessage.setFontScale(3f);
        stage.addActor(winMessage);

        Label scoreWinLabel = new Label("SCORE: " + gameContactListener.getTotalScore(), skin);
        scoreWinLabel.setColor(Color.GREEN);
        scoreWinLabel.setPosition((viewport.getWorldWidth() / 2) - (scoreWinLabel.getWidth()),
            (viewport.getWorldHeight() / 2) - 40);
        scoreWinLabel.setFontScale(2f);
        stage.addActor(scoreWinLabel);
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

        freeBallBucket.update(deltaTime);
        ballLauncher.update(deltaTime);
        ball.update(deltaTime);

        for (Peg peg : pegs) {
            peg.update(deltaTime);
        }

        updateLabels();


        // checks if ball is out of bounds
        if (checkBallOutOfBounds() && freeBallBucket.isBallInBucket(ball)) {
            endTurn();
            showBallOutOfBoundsMessage();
            ballsLeft++;
        } else if (checkBallOutOfBounds()) {
            endTurn();
            showBallOutOfBoundsMessage();
        }

        if (ballLauncher.getBallLauncherLoaded() && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))) {
            ballLauncher.shootBall(ball); // shoot if space or lmb is pressed
            ballLauncher.setBallLauncherLoaded(false);  // mark the launcher as unloaded
        }

        /*
        // ball shooting test mechanism that took me 3 fucking hours
        if (launchDelay > 0) {
            launchDelay--;
        } else {
            ballLauncher.setBallLauncherLoaded(true);
        }

        if (!ballShot && ballLauncher.getBallLauncherLoaded()) {
            ballLauncher.shootBall(ball);
            ballLauncher.setBallLauncherLoaded(false);
            ballShot = true;
        }
        */

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

    public int getBallsLeftInPool() {
        int ballsLeft = ballPool.size();
        //System.out.println("Balls Left: " + ballsLeft);
        return ballsLeft;
    }

    public int getBallsLeft() {
        return ballsLeft;
    }

    public void renderObjects(ShapeRenderer shapeRenderer) {

        shapeRenderer.setColor(Color.BLACK);
        leftWall.render(shapeRenderer);
        rightWall.render(shapeRenderer);

        // renders pegs using list
        for (Peg peg : pegs) {
            if(peg.isHit()) {
                // can also check type here to handle other colors
                if (peg.getPegType() == 2) {
                    shapeRenderer.setColor(Color.SCARLET);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 1) {
                    shapeRenderer.setColor(Color.CYAN);
                    peg.render(shapeRenderer);
                }
            } else {
                if (peg.getPegType() == 2) {
                    shapeRenderer.setColor(Color.ORANGE);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 1) {
                    shapeRenderer.setColor(0, 0, 1, 1);
                    peg.render(shapeRenderer);
                }
            }
        }

        shapeRenderer.setColor(Color.BROWN);
        freeBallBucket.render(shapeRenderer);

        shapeRenderer.setColor(1, 0.5f, 0.5f, 1);
        ball.render(shapeRenderer);

        shapeRenderer.setColor(Color.TAN);
        ballLauncher.render(shapeRenderer);

    }

    public void renderStage(float delta) {
        stage.act(delta);
        stage.draw();
        if (!characterSelectMenu.characterSelected) {
            characterSelectMenu.render(delta);
        }
        //stage.setDebugAll(true);
        // pause button is weird and I have no idea why
        // pause menu also
        // they dont line up and display properly when screen size changes
    }
}
