package com.emerson.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.emerson.gameobjects.*;
import com.emerson.gamescreens.CharacterSelectMenu;
import com.emerson.gamescreens.GameScreen;
import com.emerson.gamescreens.LevelSelectScreen;
import com.emerson.gamescreens.WinLossMenu;
import com.emerson.pegballgame.PegBallStart;
import com.emerson.savedata.SaveData;

import java.util.*;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class GameWorld {

    private final PegBallStart GAME;
    private LevelManager levelManager;
    private final int TOTAL_PEGS; // 100 total
    private final int NUM_ORANGE_PEGS; // 25 orange
    private static final float PROXIMITY_THRESHOLD = 35f;

    private boolean isPaused = true;

    private World world;
    private GameContactListener gameContactListener;
    private SaveData saveData;

    private SpriteBatch batch;
    private Texture backgroundTexture = new Texture("background.png");

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
    private Label highScoreLabel;
    private Label multiplierLabel;
    private Label orangePegsHitLabel;
    private Label orangePegsLeftLabel;

    private float elapsedTime = 0;
    private Label timerLabel;
    private boolean isTimerRunning = true;

    private Ball ball;
    private List<Peg> pegs = new ArrayList<>();
    private List<Peg> orangePegsList = new ArrayList<>();
    private Map<Integer, Peg> pegMap;
    private List<Peg> pegsToDisappear = new ArrayList<>();
    private float timeSinceLastDisappear = 0f;
    private int pegIndex = 0;
    private Peg currentPurplePeg = null;
    private Peg focusedPeg = null;

    private boolean endCalled = false;
    private List<Ball> pegBalls = new ArrayList<>();
    private int frenzyScore = 0;

    private FreeBallBucket freeBallBucket;
    private ScoreBucket scoreBucket1;
    private ScoreBucket scoreBucket2;
    private ScoreBucket scoreBucket3;
    private ScoreBucket scoreBucket4;
    private ScoreBucket scoreBucket5;
    private List<ScoreBucket> scoreBucketList = new ArrayList<>();

    private BallLauncher ballLauncher;
    private Vector2 launchPosition = new Vector2(615, 660);
    private int shotsTaken = 0;

    private final Queue<Runnable> physicsChangesQueue = new LinkedList<>();

    private boolean isZooming = false;
    private OrthographicCamera camera;
    private Viewport viewport;

    public GameWorld(PegBallStart game, LevelManager levelManager, int totalPegs, int orangePegs) {

        pauseButton.setVisible(false);

        this.GAME = game;
        this.saveData = GAME.getGameDataManager().loadGameData();
        this.levelManager = GAME.getLevelManager();
        stage = new Stage(new ScreenViewport());
        this.batch = new SpriteBatch();
        pegMap = new HashMap<>();

        TOTAL_PEGS = totalPegs;
        NUM_ORANGE_PEGS = orangePegs;

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.setToOrtho(false, viewport.getWorldWidth(), viewport.getWorldHeight());
        camera.update();
        System.out.println("Initial Camera Position: " + camera.position);
        System.out.println("Initial Camera Zoom: " + camera.zoom);

        characterSelectStage = new Stage();
        characterSelectMenu = new CharacterSelectMenu(characterSelectStage);
        Gdx.input.setInputProcessor(characterSelectStage); // character select gets the input processor
        System.out.println("char select stage has input processor");

        pauseSound = Gdx.audio.newSound(Gdx.files.internal("pauseSound.mp3"));
        resumeSound = Gdx.audio.newSound(Gdx.files.internal("resumeSound.mp3"));

        world = new World(new Vector2(0f, -50.0f), true);

        gameContactListener = new GameContactListener(pegMap, getPegs(), this, stage);
        world.setContactListener(gameContactListener);

        //createObstacles();


        //createPegsStaggeredGrid(10, 40, 20f, viewport.getWorldWidth() / 2.8f, 150f, 7f);
        //createBrickPegsPattern(5, 40, 20f, viewport.getWorldWidth() / 2.8f, 130f, 15f, 10f);

        /*
        Timer.schedule(new Timer.Task() {
            @Override
            public void run(){
                displayCharacterSelectMenu();
            }
        }, 1.5f);

         */

        createBallLauncher(640f - 25f, 670f, 35f, 35f);
        ballLauncher.setLaunchPower(100f); // 100 is good, but tweak maybe when aim line is added
        // randomize ball horizontal aiming for testing
        //ballLauncher.setLaunchDirection((float)(Math.random() * 2) - 1, -1);
        ballLauncher.setBallLauncherLoaded(true);
        launchPosition.set(ballLauncher.getBallCenter().x, ballLauncher.getBallCenter().y);
        //createBall(665f, 660f, 7f);
        // 10 balls duh
        initializeBallPool(10);
        createLabels();
        ball = getNewBall();
        //createFreeBallBucket();

        timerLabel = new Label("Time: 00:00", skin);
        pauseButton.setTouchable(Touchable.enabled);
        pauseButton.setWidth(200);
        pauseButton.setHeight(150);
        pauseButton.setColor(Color.LIME);
        pauseButton.getLabel().setFontScale(3f);
        pauseButton.setPosition((Gdx.graphics.getWidth() / 7f) - (pauseButton.getWidth() / 2),
            (Gdx.graphics.getHeight() / 5f) - (pauseButton.getHeight() / 2));
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("THE WORLD! STOP TIME!");
                togglePause();
                pauseSound.play();
                showPauseMenu();
                event.stop();
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
        System.out.println("World get " + characterSelectMenu.getCharacter());
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
        bucketDef.position.set(640 - 50, 0); // minus width of bucket
        Body bucketBody = world.createBody(bucketDef);
        freeBallBucket = new FreeBallBucket(world, bucketBody, bucketBody.getPosition(), 100, 45, 125);
        // small bucket 80x45 150 speed
        // bigger bucket 100x45 125 speed
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

    private Ball getNewBall() {
        if (!ballPool.isEmpty()) {
            Ball ball = ballPool.poll();  // get from pool
            ball.getBody().setActive(true);  // reactivate the ball
            ballsLeft--;
            System.out.println("Balls left" + getBallsLeft());
            ball.setPosition(new Vector2(ballLauncher.getBallCenter().x, ballLauncher.getBallCenter().y));
            return ball;
        } else {
            if (ballPool.size() + 1 <= maxBalls) {
                // if max balls isnt reached and you need a new one, make a new one
                System.out.println("New ball created");
                return createBall(ballLauncher.getBallCenter().x, ballLauncher.getBallCenter().y, 7f);
            } else {
                System.out.println("Max balls reached");
                return null;
            }
        }
    }

    public void prepareNextBall(){
        ball = getNewBall();
    }

    public void returnBallToPool(Ball ball) {
        ball.getBody().setLinearVelocity(0, 0);  // reset velocity
        ball.setPosition(ballLauncher.getBallCenter());  // reset position (i love steve jobs)
        ball.getBody().setType(BodyDef.BodyType.KinematicBody);
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
        List<Integer> greenPegs = new ArrayList<>(allPegIDs.subList(NUM_ORANGE_PEGS+1, NUM_ORANGE_PEGS+3));
        List<Integer> redPegs = new ArrayList<>(allPegIDs.subList(NUM_ORANGE_PEGS+4, NUM_ORANGE_PEGS+6));
        for (Peg peg : pegs) {
            for (int i = 0; i < orangePegs.size(); i++) {
                if (peg.getPegID() == orangePegs.get(i)) {
                    peg.setPegType(2);
                    orangePegsList.add(peg);
                }
            }
            for (int i = 0; i < greenPegs.size(); i++) {
                if (peg.getPegID() == greenPegs.get(i)) {
                    peg.setPegType(4);
                }
            }
            for (int i = 0; i < redPegs.size(); i++) {
                if (peg.getPegID() == redPegs.get(i)) {
                    peg.setPegType(5);
                }
            }
        }
        relocatePurplePeg();

    }

    public void createLevelFromPositions(List<Vector2> pegPositions, float pegRadius) {

        for (Vector2 pos : pegPositions) {
            BodyDef pegDef = new BodyDef();
            pegDef.position.set(pos);

            Body pegBody = world.createBody(pegDef);
            // create peg and add to list
            Peg peg = new Peg(this, pegBody, pegBody.getPosition(), pegRadius, pegID++);
            pegs.add(peg);
            pegMap.put(peg.getPegID(), peg);
        }
        // use RNG to do other peg colors and add them to their own lists and remove them from the original list
        List<Integer> allPegIDs = new ArrayList<>();
        for (Peg peg : pegs) {
            allPegIDs.add(peg.getPegID());
        }
        Collections.shuffle(allPegIDs);

        List<Integer> orangePegs = new ArrayList<>(allPegIDs.subList(0, NUM_ORANGE_PEGS));
        List<Integer> greenPegs = new ArrayList<>(allPegIDs.subList(NUM_ORANGE_PEGS+1, NUM_ORANGE_PEGS+3));
        List<Integer> redPegs = new ArrayList<>(allPegIDs.subList(NUM_ORANGE_PEGS+4, NUM_ORANGE_PEGS+6));
        for (Peg peg : pegs) {
            for (int i = 0; i < orangePegs.size(); i++) {
                if (peg.getPegID() == orangePegs.get(i)) {
                    peg.setPegType(2);
                    orangePegsList.add(peg);
                }
            }
            for (int i = 0; i < greenPegs.size(); i++) {
                if (peg.getPegID() == greenPegs.get(i)) {
                    peg.setPegType(4);
                }
            }
            for (int i = 0; i < redPegs.size(); i++) {
                if (peg.getPegID() == redPegs.get(i)) {
                    peg.setPegType(5);
                }
            }
        }
        relocatePurplePeg();

    }

    public void loadLevelFromFile(String fileName, float pegRadius) {
        FileHandle file = Gdx.files.local(fileName);
        if (!file.exists()) {
            System.out.println("Level file not found: " + fileName);
            return;
        }

        Json json = new Json();
        List<Vector2> pegPositions = json.fromJson(ArrayList.class, Vector2.class, file.readString());

        createLevelFromPositions(pegPositions, pegRadius);
    }

    public void loadMirrorLevelFromFile(String fileName, float pegRadius) {
        FileHandle file = Gdx.files.local(fileName);
        if (!file.exists()) {
            System.out.println("Level file not found: " + fileName);
            return;
        }

        Json json = new Json();
        List<Vector2> leftPegPositions = json.fromJson(ArrayList.class, Vector2.class, file.readString());
        List<Vector2> pegPositions = json.fromJson(ArrayList.class, Vector2.class, file.readString());

        // make mirror pegs
        float centerX = Gdx.graphics.getWidth() / 2f;
        for (Vector2 pos : leftPegPositions) {
            float mirroredX = centerX + (centerX - pos.x); // reflect X
            pegPositions.add(new Vector2(mirroredX, pos.y));
        }

        createLevelFromPositions(pegPositions, pegRadius);
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


    public void activatePowerUp(PowerUp powerUp, Ball ball) {
        powerUp.activate(ball, this);
    }

    public void addPegToDisappearQueue(Peg peg) {
        pegsToDisappear.add(peg);
    }

    public boolean checkBallOutOfBounds() {
        return ball.getPosition().y < 0;
    }

    public boolean checkPegBallOutOfBounds(Ball pegBall) {
        return pegBall.getPosition().y < 0;
    }

    public boolean checkPegBallsOutOfBounds() {
        for (Ball pegBall : pegBalls) {
            if (pegBall.getPosition().y >= 0) {
                // if any ball is above or equal to 0, they are not out of bounds
                return false;
            }
        }
        // all balls are below 0 out of bounds.
        return true;
    }

    public void endTurn(){

        // update score message
        int newTotalScore = gameContactListener.getTurnScore() * gameContactListener.getPegsHit();
        System.out.println("Turn score: " + newTotalScore);
        int totalScore = gameContactListener.getTotalScore() + newTotalScore;
        System.out.println("Total score: " + totalScore);
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
        gameContactListener.resetFreeBalls();
        returnBallToPool(ball);
    }

    private void showBallOutOfBoundsMessage() {
        if (gameContactListener.whirlyBallPowerUp.active()) {
            gameContactListener.whirlyBallPowerUp.deactivate(ball, GameWorld.this);
        }
        if (gameContactListener.bouncyBallPowerUp.active()) {
            gameContactListener.bouncyBallPowerUp.deactivate(ball, GameWorld.this);
        }
        if (gameContactListener.electroBallPowerUp.active()) {
            gameContactListener.incrementElectroTurns();
            if ((gameContactListener.getElectroTurns() > gameContactListener.electroBallPowerUp.getMaxTurns())) {
                gameContactListener.electroBallPowerUp.deactivate(ball, GameWorld.this);
                gameContactListener.resetElectroTurns();
            }
        }
        if (gameContactListener.sludgeBallPowerUp.active()) {
            gameContactListener.incrementSludgeTurns();
            if ((gameContactListener.getSludgeTurns() > gameContactListener.sludgeBallPowerUp.getMaxTurns())) {
                gameContactListener.sludgeBallPowerUp.deactivate(ball, GameWorld.this);
                gameContactListener.resetSludgeTurns();
            }
            gameContactListener.sludgeBallPowerUp.deactivate(ball, GameWorld.this);
        }
        if (gameContactListener.mirrorBallPowerUp.active()) {
            gameContactListener.incrementMirrorTurns();
            if ((gameContactListener.getMirrorTurns() > gameContactListener.mirrorBallPowerUp.getMaxTurns())) {
                gameContactListener.mirrorBallPowerUp.deactivate(ball,  GameWorld.this);
                gameContactListener.resetMirrorTurns();
            }
        }
        if (gameContactListener.osmiumBallPowerUp.active()) {
            gameContactListener.osmiumBallPowerUp.deactivate(ball, GameWorld.this);
        }


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
                relocatePurplePeg();
                if (ballsLeft == 0 && !checkOrangePegs()) {
                    showWinLossMessage();
                    /*
                    Label noMoreBalls = new Label("NO MORE BALLS", skin);
                    noMoreBalls.setColor(Color.RED);
                    noMoreBalls.setPosition((viewport.getWorldWidth() / 2) - (noMoreBalls.getWidth()),
                        (viewport.getWorldHeight() / 2));
                    noMoreBalls.setFontScale(2f);
                    stage.addActor(noMoreBalls);

                     */
                } else if (checkOrangePegs()) {
                    // might have to change something up here for ending sequence with score buckets
                    Label frenzyScoreLabel = new Label("FRENZY SCORE: " + frenzyScore, skin);
                    frenzyScoreLabel.setColor(Color.ORANGE);
                    frenzyScoreLabel.setPosition((VIRTUAL_WIDTH / 2) - (frenzyScoreLabel.getWidth()), (VIRTUAL_HEIGHT / 2) + (VIRTUAL_HEIGHT / 4));
                    frenzyScoreLabel.setFontScale(2f);
                    stage.addActor(frenzyScoreLabel);
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            frenzyScoreLabel.remove();
                            showWinLossMessage();
                        }
                    },3f);
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
        scoreLabel.setPosition(425, Gdx.graphics.getHeight() - 20);
        scoreLabel.setFontScale(1.5f);
        stage.addActor(scoreLabel);

        multiplierLabel = new Label("Multiplier: " + gameContactListener.getScoreMultiplier() + "x", skin);
        multiplierLabel.setColor(Color.ORANGE);
        multiplierLabel.setPosition(885, Gdx.graphics.getHeight() - 90);
        multiplierLabel.setFontScale(1.5f);
        stage.addActor(multiplierLabel);

        highScoreLabel = new Label("High Score: " + saveData.highScores.get(levelManager.getCurrentLevel().getLevelName()), skin);
        highScoreLabel.setColor(Color.BLACK);
        highScoreLabel.setPosition(700, Gdx.graphics.getHeight() - 20);
        highScoreLabel.setFontScale(1.3f);
        stage.addActor(highScoreLabel);

        orangePegsHitLabel = new Label("Orange Pegs hit: " + gameContactListener.getTotalOrangePegsHit(), skin);
        orangePegsHitLabel.setColor(Color.ORANGE);
        orangePegsHitLabel.setPosition(885, Gdx.graphics.getHeight() - 50);
        orangePegsHitLabel.setFontScale(1.3f);
        stage.addActor(orangePegsHitLabel);

        orangePegsLeftLabel = new Label("Orange Pegs left: " + orangePegsList.size(), skin);
        orangePegsLeftLabel.setColor(Color.ORANGE);
        orangePegsLeftLabel.setPosition(885, Gdx.graphics.getHeight() - 70);
        orangePegsLeftLabel.setFontScale(1.3f);
        stage.addActor(orangePegsLeftLabel);
    }

    private void updateLabels() {
        ballsLeftLabel.setText("Balls left: " + getBallsLeft());

        multiplierLabel.setText("Multiplier: " + gameContactListener.getScoreMultiplier() + "x");

        orangePegsHitLabel.setText("Orange Pegs hit: " + gameContactListener.getTotalOrangePegsHit());

        orangePegsLeftLabel.setText("Orange Pegs left: " + orangePegsList.size());
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + gameContactListener.getTotalScore());
    }

    private boolean checkOrangePegs() {
        return gameContactListener.getTotalOrangePegsHit() == NUM_ORANGE_PEGS;
    }

    public void giveFreeBall(Vector2 position) {
        Label messageLabel = new Label("FREE BALL!!!", skin);
        messageLabel.setColor(1, 0.5f, 0.5f, 1);
        messageLabel.setPosition(position.x  - (messageLabel.getWidth()), position.y);
        messageLabel.setFontScale(1.5f);
        stage.addActor(messageLabel);
        ballsLeft++;
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageLabel.remove();
            }
        },2f);
    }

    private void checkProximityToFinalOrangePeg() {
        if (orangePegsList.size() == 1) {
            Peg finalPeg = orangePegsList.get(0);
            float distance = ball.getPosition().dst(finalPeg.getPosition());

            if (distance < PROXIMITY_THRESHOLD) {
                System.out.println("Distance in proximity with Peg: " + finalPeg + " " + distance);
                triggerFinalPegSequence(finalPeg);
            } else {
                // zoom back out / resume time normal
                untriggerFinalPegSequence();
            }
        } else if (orangePegsList.isEmpty() && !endCalled) {
            // if no orange pegs left
            // ending sequence
            // for now just resume time/cam to normal after 2 seconds
            // maybe test freezing (out here) then resuming in the timer, (works btw)
            endLevelSequence();
        }
    }

    private void triggerFinalPegSequence(Peg finalPeg) {
        System.out.println("Sequence triggered");
        timeScale = 0.2f;
        System.out.println("Time slowed");

    }

    private void untriggerFinalPegSequence() {
        if (!endCalled) {
            timeScale = 1f;
        }
    }

    private void endLevelSequence() {
        endCalled = true;
        System.out.println("ALL ORANGE HIT!!!");
        timeScale = 0f;
        Label feverLabel = new Label("PEGBALL FRENZY!!!", skin);
        feverLabel.setColor(Color.ORANGE);
        feverLabel.setPosition((VIRTUAL_WIDTH / 2) - (feverLabel.getWidth()), (VIRTUAL_HEIGHT / 2) + (VIRTUAL_HEIGHT / 4));
        feverLabel.setFontScale(2f);
        stage.addActor(feverLabel);
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                enableScoreBuckets();
                feverLabel.remove();
                makePegBalls();
                timeScale = 3f;
                world.setGravity(new Vector2(0,world.getGravity().y/4f));
                System.out.println("Time sped up");
                //enableScoreBuckets();
            }
        },2f);

    }

    private Ball transformPegToBall(Peg peg) {
        peg.pegDisappear(world, pegMap, pegs);
        Ball newBall = createBall(peg.getPosition().x, peg.getPosition().y, 7f);
        // push ball up, in a random direction horizontally
        newBall.getBody().setType(BodyDef.BodyType.DynamicBody);
        newBall.getBody().setGravityScale(1);
        pegBalls.add(newBall);
        return newBall;
    }

    private void makePegBalls() {
        for (Peg peg : pegs) {
            peg.pegDisappear(getWorld(), getPegMap(), getPegs());
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    Ball pegBall = transformPegToBall(peg);
                    // might wanna change density or something to create more variance, maybe restitution values
                    pegBall.getBody().setActive(true);
                    float horizontalImpulse = ((float)(Math.random() * 5) - 1) * 11250f; // Small random horizontal variance
                    float verticalImpulse = 10f * 11250f; // Strong upward force
                    pegBall.getBody().applyLinearImpulse(new Vector2(horizontalImpulse, verticalImpulse), pegBall.getBody().getWorldCenter(), true);
                }
            }, 0.2f);
        }
    }

    private void enableScoreBuckets () {
        // remove free ball bucket
        if (freeBallBucket != null) {
            freeBallBucket.destroyWalls();
            world.destroyBody(freeBallBucket.getBody());
            freeBallBucket = null;
        }

        // create score buckets
        // check for ball and pegBalls leaving bounds through buckets
        float bucketWidth = 96f;
        float bucketHeight = 45f;
        float spacing = 0f;
        float screenBottom = 0f;
        int[] scores;
        if (pegs.size() >= 60) {
            scores = new int[]{1000, 2000, 3000, 2000, 1000};
        } else if (60 > pegs.size() && pegs.size() >= 50) {
            scores = new int[]{2500, 3000, 4000, 3000, 2500};
        } else if (50 > pegs.size() && pegs.size() >= 40) {
            scores = new int[]{3000, 4000, 5000, 4000, 3000};
        } else if (40 > pegs.size() && pegs.size() >= 30) {
            scores = new int[]{4000, 5000, 6000, 5000, 4000};
        } else if (30 > pegs.size() && pegs.size() >= 20) {
            scores = new int[]{5000, 6500, 7000, 6500, 5000};
        } else if (20 > pegs.size() && pegs.size() >= 10) {
            scores = new int[]{6000, 7000, 8000, 7000, 6000};
        } else if (10 > pegs.size() && pegs.size() >= 5) {
            scores = new int[]{7500, 9000, 15000, 9000, 7500};
        } else {
            scores = new int[]{10000, 15000, 100000, 15000, 10000};
        }
        float x;
        BodyDef scoreBucketDef = new BodyDef();
        for (int i = 0; i < 5; i++) {
            x = i * (bucketWidth + spacing) + 400;
            scoreBucketDef.position.set(x, screenBottom);
            Body scoreBucketBody = world.createBody(scoreBucketDef);
            ScoreBucket scoreBucket = new ScoreBucket(world, scoreBucketBody, new Vector2(x, screenBottom), bucketWidth, bucketHeight, scores[i]);
            scoreBucketList.add(scoreBucket);
            Label messageLabel = new Label("" + scores[i], skin);
            messageLabel.setColor(Color.BLACK);
            messageLabel.setPosition((x + (bucketWidth / 2)) - (messageLabel.getWidth() - 10), screenBottom + 20);
            messageLabel.setFontScale(1.5f);
            stage.addActor(messageLabel);
        }
    }

    public void showWinLossMessage() {
        isTimerRunning = false;
        WinLossMenu menu = new WinLossMenu(GAME, GAME.getLevelManager(), checkOrangePegs(), characterSelectMenu.getCharacter(), gameContactListener.getTotalScore(),
            shotsTaken, elapsedTime, skin);
        stage.addActor(menu);
        /*
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

         */
    }

    public void queuePhysicsChange(Runnable change) {
        physicsChangesQueue.add(change);
    }

    public void applyQueuedChanges() {
        while (!physicsChangesQueue.isEmpty()) {
            physicsChangesQueue.poll().run();
        }
    }

    private float timeScale = 1f;
    public void update(float deltaTime) {
        deltaTime *= timeScale;

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

        world.step(timeStep * timeScale, velocityIterations, positionIterations);
        applyQueuedChanges();
        if (isTimerRunning) {
            elapsedTime += Gdx.graphics.getDeltaTime(); // Increment by frame time
        }

        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

        if (!endCalled) {
            freeBallBucket.update(deltaTime);
        }
        ballLauncher.update(deltaTime);
        ball.update(deltaTime);
        if (gameContactListener.whirlyBallPowerUp.active()) {
            gameContactListener.whirlyBallPowerUp.update(deltaTime, ball, this);
        } else if (gameContactListener.bouncyBallPowerUp.active()) {
            gameContactListener.bouncyBallPowerUp.update(deltaTime, ball, this);
        } else if (gameContactListener.electroBallPowerUp.active()) {
            gameContactListener.electroBallPowerUp.update(deltaTime, ball, this);
        } else if (gameContactListener.sludgeBallPowerUp.active()) {
            gameContactListener.sludgeBallPowerUp.update(deltaTime, ball, this);
        }

        if (endCalled) {
            for (Ball pegBall : pegBalls) {
                if (pegBall != null) {
                    pegBall.update(deltaTime);
                }
            }
            for (ScoreBucket scoreBucket : scoreBucketList) {
                scoreBucket.update(deltaTime);
            }
            Iterator<Ball> pegBallIterator = pegBalls.iterator();
            while (pegBallIterator.hasNext()) {
                Ball pegBall = pegBallIterator.next();
                for (ScoreBucket scoreBucket : scoreBucketList) {
                    if (checkPegBallOutOfBounds(pegBall) && scoreBucket.isBallInBucket(pegBall)) {
                        frenzyScore += scoreBucket.getScoreValue();
                        System.out.println("Frenzy score: " + frenzyScore);
                        int totalScore = gameContactListener.getTotalScore() + scoreBucket.getScoreValue();
                        System.out.println("Total score " + gameContactListener.getTotalScore() + " + bucket value: " + scoreBucket.getScoreValue());
                        System.out.println("New total: " + totalScore);
                        gameContactListener.setTotalScore(totalScore);
                        pegBall.destroy();
                        pegBallIterator.remove();
                        break;
                    }
                }
            }
        }

        for (Peg peg : pegs) {
            peg.update(deltaTime);
        }

        if (gameContactListener.getTotalOrangePegsHit() < 10) {
            gameContactListener.setScoreMultiplier(1);
        } else if (gameContactListener.getTotalOrangePegsHit() >= 10 && gameContactListener.getTotalOrangePegsHit() < 15) {
            gameContactListener.setScoreMultiplier(2);
        } else if (gameContactListener.getTotalOrangePegsHit() >= 15 && gameContactListener.getTotalOrangePegsHit() < 19) {
            gameContactListener.setScoreMultiplier(3);
        } else if (gameContactListener.getTotalOrangePegsHit() >= 19 && gameContactListener.getTotalOrangePegsHit() < 22) {
            gameContactListener.setScoreMultiplier(5);
        } else if (gameContactListener.getTotalOrangePegsHit() >= 22) {
            gameContactListener.setScoreMultiplier(10);
        }

        updateLabels();

        checkProximityToFinalOrangePeg();

        // checks if end has started and if ball is out of bounds
        if (!endCalled) {
            if (checkBallOutOfBounds() && freeBallBucket.isBallInBucket(ball)) {
                endTurn();
                showBallOutOfBoundsMessage();
                giveFreeBall(new Vector2((viewport.getWorldWidth() / 2),50));
            } else if (pegBalls.isEmpty() && checkBallOutOfBounds()) {
                endTurn();
                showBallOutOfBoundsMessage();
            }
        } else {
            if (checkBallOutOfBounds() && checkPegBallsOutOfBounds()) {
                endTurn();
                showBallOutOfBoundsMessage();
            }
        }

        /*
        if ((checkBallOutOfBounds() && freeBallBucket.isBallInBucket(ball)) && (freeBallBucket != null)) {
            endTurn();
            showBallOutOfBoundsMessage();
            giveFreeBall(new Vector2((viewport.getWorldWidth() / 2),50));
        } else if ((pegBalls.isEmpty() && !endCalled) && checkBallOutOfBounds()) {
            endTurn();
            showBallOutOfBoundsMessage();
        } else if (endCalled && (checkBallOutOfBounds() && checkPegBallsOutOfBounds())) {
            endTurn();
            showBallOutOfBoundsMessage();
        }

         */

        if (ballLauncher.getBallLauncherLoaded() && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))) {
            Vector2 cursorPosition = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            // check if cursor is over pause or launcher
            if (cursorPosition.x >= pauseButton.getX() && cursorPosition.x <= pauseButton.getX() + pauseButton.getWidth() &&
                cursorPosition.y >= pauseButton.getY() && cursorPosition.y <= pauseButton.getY() + pauseButton.getHeight()) {
                System.out.println("Mouse is over the pause button.");
                return; // dont fucking shoot
            } else if (cursorPosition.x >= ballLauncher.getPosition().x && cursorPosition.x <= ballLauncher.getPosition().x + ballLauncher.getWidth() &&
                cursorPosition.y >= ballLauncher.getPosition().y && cursorPosition.y <= ballLauncher.getPosition().y + ballLauncher.getHeight()) {
                System.out.println("Mouse is over the ball launcher");
                return; // dont fucking shoot
            }
            ballLauncher.shootBall(ball); // shoot if space or lmb is pressed
            ballLauncher.setBallLauncherLoaded(false);  // mark the launcher as unloaded
            shotsTaken++;

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

    /*
    private void updateCamera(float delta) {
        if (isZooming && focusedPeg != null) {
            Vector2 targetPosition = focusedPeg.getPosition();
            System.out.println("Camera focusing on peg at: " + targetPosition);
            camera.position.lerp(new Vector3(targetPosition, 0), 0.1f); // smooth moves
            System.out.println("Camera position: " + camera.position);
            camera.zoom = MathUtils.lerp(camera.zoom, 0.5f, 0.1f); // zoom
            System.out.println("Camera zoom: " + camera.zoom);
            System.out.println("Zooming: " + isZooming + ", Focused Peg: " + (focusedPeg != null ? focusedPeg.getPosition() : "None"));
            camera.update();

            if (Math.abs(camera.zoom - 0.5f) < 0.01f) { // check zoom is near target
                isZooming = false; // stop zoomin
                System.out.println("stop zoomin");
            }
        }
    }

     */

    public void togglePause() {
        if (isPaused) { // if the game is paused
            isPaused = false; // resume it
            isTimerRunning = true;
            pauseButton.setVisible(true); // put the pause button back
        }
        else { // if game is running
            isPaused = true; // pause game
            isTimerRunning = false;
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
                togglePause();
                pauseMenu.remove();
                reset();
                Gdx.input.setInputProcessor(null);
                GAME.setScreen(new LevelSelectScreen(GAME)); // switch to level select menu
            }
        });

        pauseMenu.add(timerLabel).row();
        pauseMenu.add(resumeButton).row();
        pauseMenu.add(quitToLevelSelectButton);
        pauseMenu.setSize(400, 400);
        pauseMenu.setPosition((stage.getWidth() / 2) - (pauseMenu.getWidth() / 2),
            (stage.getHeight() / 2) - (pauseMenu.getHeight() / 2));
        stage.addActor(pauseMenu);
    }

    public void reset() {
        if (stage != null) {
            stage.clear();
        }
        Gdx.input.setInputProcessor(null);
        timeScale = 1f;
        isPaused = false;
        resetTimer();
        characterSelected = false;
        characterSelectMenu.reset();

        Gdx.input.setInputProcessor(null);
    }

    public void resetTimer() {
        elapsedTime = 0;
        timerLabel.setText("Time: 00:00");
    }

    private void retryLevel() {
        // Logic to restart the current level
        GAME.setScreen(new GameScreen(GAME, GAME.getLevelManager().getCurrentLevelIndex()));
    }

    private void goToLevelSelect() {
        // Logic to navigate back to the level select screen
        GAME.setScreen(new LevelSelectScreen(GAME));
    }


    public World getWorld() {
        return world;
    }

    public float getWorldWidth() {
        return viewport.getWorldWidth();
    }

    public List<Peg> getPegs(){
        return pegs;
    }

    public Map<Integer, Peg> getPegMap(){
        return pegMap;
    }

    public void relocatePurplePeg() {
        // reset current purple to blue if it exists
        if (currentPurplePeg != null) {
            currentPurplePeg.setPegType(1);
        }

        // compile all blues currently on board
        List<Peg> bluePegs = new ArrayList<>();
        for (Peg peg : pegs) {
            if (peg.getPegType() == 1) {
                bluePegs.add(peg);
            }
        }

        // choose random blue to be purple
        if (!bluePegs.isEmpty()) {
            int randomIndex = MathUtils.random(bluePegs.size() - 1);
            Peg newPurplePeg = bluePegs.get(randomIndex);
            newPurplePeg.setPegType(3);
            currentPurplePeg = newPurplePeg;
        }
    }

    public int getBallsLeftInPool() {
        int ballsLeft = ballPool.size();
        //System.out.println("Balls Left: " + ballsLeft);
        return ballsLeft;
    }

    public void removeOrangePegFromList(Peg peg) {
        // remove orange peg from orange peg list list
        orangePegsList.remove(peg);
    }

    public Ball getBall() {
        return ball;
    }

    public BallLauncher getBallLauncher() {
        return ballLauncher;
    }

    public int getBallsLeft() {
        return ballsLeft;
    }

    public PegBallStart getGAME() {
        return GAME;
    }

    public void hitPeg(Peg peg) {
        System.out.println("Collision detected!");
        int pegID = peg.getPegID();
        System.out.println(pegID);
        if (pegMap.containsKey(pegID)){
            Peg hitPeg = pegMap.get(pegID);
            gameContactListener.handlePegHit(pegID, hitPeg);
        }
    }

    public void renderObjects(ShapeRenderer shapeRenderer) {

        if (!(backgroundTexture == null)) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }

        shapeRenderer.setColor(Color.BLACK);
        leftWall.render(shapeRenderer);
        rightWall.render(shapeRenderer);

        // renders pegs using list
        for (Peg peg : pegs) {
            if(peg.isHit()) {
                // can also check type here to handle other colors
                if (peg.getPegType() == 5) {
                    shapeRenderer.setColor(Color.BROWN);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 4) {
                    shapeRenderer.setColor(Color.GREEN);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 3) {
                    shapeRenderer.setColor(Color.PINK);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 2) {
                    shapeRenderer.setColor(1f, 0.35f, 0f, 1f);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 1) {
                    shapeRenderer.setColor(Color.CYAN);
                    peg.render(shapeRenderer);
                }
            } else {
                if (peg.getPegType() == 5) {
                    shapeRenderer.setColor(Color.RED);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 4) {
                    shapeRenderer.setColor(Color.LIME);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 3) {
                    shapeRenderer.setColor(Color.MAGENTA);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 2) {
                    shapeRenderer.setColor(Color.ORANGE);
                    peg.render(shapeRenderer);
                } else if (peg.getPegType() == 1) {
                    shapeRenderer.setColor(0, 0, 1, 1);
                    peg.render(shapeRenderer);
                }
            }
        }

        if (!endCalled) {
            shapeRenderer.setColor(Color.BROWN);
            freeBallBucket.render(shapeRenderer);
        }

        for (ScoreBucket scoreBucket : scoreBucketList) {

            if (scoreBucket.getScoreValue() == 1000) {
                shapeRenderer.setColor(Color.DARK_GRAY);
            } else if (scoreBucket.getScoreValue() == 2000) {
                shapeRenderer.setColor(Color.GRAY);
            } else if (scoreBucket.getScoreValue() == 2500) {
                shapeRenderer.setColor(Color.LIGHT_GRAY);
            } else if (scoreBucket.getScoreValue() == 3000) {
                shapeRenderer.setColor(Color.BLUE);
            } else if (scoreBucket.getScoreValue() == 4000) {
                shapeRenderer.setColor(Color.CYAN);
            } else if (scoreBucket.getScoreValue() == 5000) {
                shapeRenderer.setColor(Color.SKY);
            } else if (scoreBucket.getScoreValue() == 6000) {
                shapeRenderer.setColor(Color.CHARTREUSE);
            } else if (scoreBucket.getScoreValue() == 6500) {
                shapeRenderer.setColor(1f, .5f, .02f, 1f);
            } else if (scoreBucket.getScoreValue() == 7000) {
                shapeRenderer.setColor(Color.GOLDENROD);
            } else if (scoreBucket.getScoreValue() == 7500) {
                shapeRenderer.setColor(Color.GOLD);
            } else if (scoreBucket.getScoreValue() == 8000) {
                shapeRenderer.setColor(Color.MAROON);
            } else if (scoreBucket.getScoreValue() == 9000) {
                shapeRenderer.setColor(Color.PINK);
            } else if (scoreBucket.getScoreValue() == 10000) {
                shapeRenderer.setColor(Color.MAGENTA);
            } else if (scoreBucket.getScoreValue() == 15000) {
                shapeRenderer.setColor(Color.PURPLE);
            } else if (scoreBucket.getScoreValue() == 100000) {
                shapeRenderer.setColor(Color.ROYAL);
            }
            scoreBucket.render(shapeRenderer);
        }

        gameContactListener.whirlyBallPowerUp.render(shapeRenderer);
        gameContactListener.bouncyBallPowerUp.render(shapeRenderer);
        gameContactListener.electroBallPowerUp.render(shapeRenderer);
        gameContactListener.sludgeBallPowerUp.render(shapeRenderer);

        if (gameContactListener.sludgeBallPowerUp.active()) {
            shapeRenderer.setColor(Color.FOREST);
        } else if (gameContactListener.mirrorBallPowerUp.active()) {
            shapeRenderer.setColor(0.5f, 0.5f, 1f, 1f);
        } else if (gameContactListener.osmiumBallPowerUp.active()) {
            shapeRenderer.setColor(Color.LIGHT_GRAY);
        } else {
            shapeRenderer.setColor(1, 0.5f, 0.5f, 1);
        }
        ball.render(shapeRenderer);
        for (Ball ball : pegBalls) {
            ball.render(shapeRenderer);
        }

        ballLauncher.render(shapeRenderer);

    }

    public void setBackgroundTexture(Texture backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
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
