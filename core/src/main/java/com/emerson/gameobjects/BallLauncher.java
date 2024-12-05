package com.emerson.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.emerson.world.GameWorld;

public class BallLauncher extends GameObject {

    private final float POWER_MULTIPLIER = 150f;
    private static final float TOP_LINE_Y = Gdx.graphics.getHeight() - 50; // height used for movement
    private static final float LEFT_BOUNDARY = 400; // left wall
    private static final float RIGHT_BOUNDARY = Gdx.graphics.getWidth() - 435; // right wall
    private static final float BOTTOM_BOUNDARY = 50; // bottom height used for movement

    private float launchPower;
    private Vector2 launchDirection;
    private Vector2 launchVelocity;
    private AimingLine aimingLine;
    private Vector2 ballCenter;
    private boolean ballLauncherLoaded = false;

    private boolean isDragging = false;
    private float initialX;
    private float initialY;

    private boolean usingMouse = false;
    private float aimAngle;
    private Vector2 previousMousePosition = new Vector2();

    private Body body;
    private final GameWorld GAMEWORLD;

    public BallLauncher(GameWorld gameWorld, Body body, Vector2 position, float width, float height) {

        super(body, position, width, height);
        this.GAMEWORLD = gameWorld;
        this.body = body;
        this.launchPower = 75f * POWER_MULTIPLIER;
        this.launchDirection = new Vector2(0f, -1f); // down
        this.launchVelocity = new Vector2();
        this.aimingLine = new AimingLine(gameWorld);
        this.ballCenter = new Vector2(position.x + width / 2, position.y - 10); // -10 from gameworld placing ball below launcher


        body.setType(BodyDef.BodyType.KinematicBody); // not affected by world forces but able to be moved manually

        PolygonShape rectangle = new PolygonShape();
        // weird math shit cause the rectangle goes from the actual middle not bottom left corner
        rectangle.setAsBox(width / 2, height / 2, new Vector2(width / 2, height / 2), 0);


        FixtureDef ballLauncherFixtureDef = new FixtureDef();
        ballLauncherFixtureDef.shape = rectangle;
        body.createFixture(ballLauncherFixtureDef);

        rectangle.dispose();

    }

    public void setLaunchPower(float power) {
        this.launchPower = power * POWER_MULTIPLIER;
    }

    public void setLaunchDirection(float x, float y) {
        launchDirection.set(x, y).nor(); // must be normalized to 1
    }

    public void calculateLaunchVelocity(){
        launchDirection.nor();
        float launchVelocityX = launchDirection.x * launchPower;
        float launchVelocityY = launchDirection.y * launchPower;
        // take launch direction and power and combine them for velocity
        launchVelocity.set(launchVelocityX, launchVelocityY);
    }

    public void handleMouseAiming() {

        Vector2 currentMousePosition = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

        // check if mouse has moved
        if (currentMousePosition.x != previousMousePosition.x || currentMousePosition.y != previousMousePosition.y) {
            // update aim
            Vector2 mousePos = new Vector2(currentMousePosition.x, currentMousePosition.y);
            Vector2 launcherPos = new Vector2(ballCenter.x, ballCenter.y); // Launcher position

            launchDirection.set(mousePos).sub(launcherPos).nor();  // Normalize to get direction
            aimAngle = launchDirection.angleDeg();

            usingMouse = true;

            // update last mouse position
            previousMousePosition.set(currentMousePosition.x, currentMousePosition.y);
        }

        /*
        if (!currentMousePosition.epsilonEquals(previousMousePosition, 0.1f)) {  // threshold to ignore jitter
            // Convert screen coordinates to world coordinates if needed
            // For example: gameCamera.unproject(currentMousePosition);

            // Calculate direction from launcher to mouse
            launchDirection = currentMousePosition.sub(body.getPosition()).nor();
            aimAngle = launchDirection.angleDeg(); // angle if needed for aiming line or something

            usingMouse = true;

            previousMousePosition.set(currentMousePosition);
        }
        */
    }

    public void handleKeyboardAiming() {
        float aimSpeed = 0.2f;  // how much to adjust the aim per frame

        if (!usingMouse) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                aimAngle -= aimSpeed;
                usingMouse = false;
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                aimAngle += aimSpeed;
                usingMouse = false;
            }
            launchDirection.set(MathUtils.cosDeg(aimAngle), MathUtils.sinDeg(aimAngle)).nor();
        }

        // clamp aimAngle to a specific range if needed (e.g., 0 to 360)
    }

    public void shootBall(Ball ball) {

        ball.getBody().setType(BodyDef.BodyType.DynamicBody); // change the ball to dynamic so it can be affected by forces
        System.out.println("Ball is now able to move");
        ball.getBody().applyLinearImpulse(new Vector2(launchVelocity), ball.getBody().getWorldCenter(), true); // "shoot" ball
        ball.getBody().setGravityScale(1);
        System.out.println("Ball shot!");
        System.out.println(ball.getBody().getLinearVelocity());
    }

    public boolean getBallLauncherLoaded() {
        return ballLauncherLoaded;
    }

    public void setBallLauncherLoaded(boolean loaded) {
        ballLauncherLoaded = loaded;
    }

    public void updateAimingLine(Vector2 startPosition, Vector2 launchVelocity) {
        float maxLength = 100f;
        aimingLine.calculateTrajectory(startPosition, launchVelocity, maxLength);
    }

    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    private boolean launcherHit(float x, float y) {
        y = Gdx.graphics.getHeight() - y;
        System.out.println("Cursor: (" + x + ", " + y + ")");
        System.out.println("Launcher: (" + position.x + ", " + position.y + ")");
        boolean hit = Math.abs(x - position.x) < width && Math.abs(y - position.y) < height;
        System.out.println("Hit detected: " + hit);
        return hit;
    }


    public void handleInput() {
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if ((!isDragging && launcherHit(Gdx.input.getX(), Gdx.input.getY())) && ballLauncherLoaded) {
                System.out.println("Mouse on launcher " + Gdx.input.getX() + ", " + Gdx.input.getY());
                isDragging = true;
                initialX = Gdx.input.getX() - position.x; // offset for smooth dragging, keeps mouse on the spot you clicked
                initialY = Gdx.graphics.getHeight() - Gdx.input.getY() - position.y;
                System.out.println("Dragging " + isDragging);
            }

            if (isDragging) {

                if (Math.abs(position.y - TOP_LINE_Y) < 10f &&
                    Math.abs(Gdx.input.getX() - initialX - LEFT_BOUNDARY) > 10f && Math.abs(Gdx.input.getX() - initialX - RIGHT_BOUNDARY) > 10f) { // Top line

                    position.x = MathUtils.clamp(Gdx.input.getX() - initialX, LEFT_BOUNDARY, RIGHT_BOUNDARY);
                    position.y = TOP_LINE_Y; // Lock to top line

                    ballCenter.set(position.x + width/2f, position.y - 10f);
                    GAMEWORLD.getBall().setPosition(ballCenter);
                    //position.y = Gdx.graphics.getHeight() - Gdx.input.getY() - initialY;
                } else if (Math.abs(position.x - LEFT_BOUNDARY) < 10f) { // Left side
                    position.x = LEFT_BOUNDARY; // Lock to left side
                    //position.y = Gdx.graphics.getHeight() - Gdx.input.getY() - initialY;
                    position.y = MathUtils.clamp(Gdx.graphics.getHeight() - Gdx.input.getY() - initialY, BOTTOM_BOUNDARY, TOP_LINE_Y);
                    ballCenter.set(position.x + width + 10f, position.y + height / 2f);
                    GAMEWORLD.getBall().setPosition(ballCenter);
                } else if (Math.abs(position.x - RIGHT_BOUNDARY) < 10f) { // Right side
                    position.x = RIGHT_BOUNDARY; // Lock to right side
                    //position.y = Gdx.graphics.getHeight() - Gdx.input.getY() - initialY;
                    position.y = MathUtils.clamp(Gdx.graphics.getHeight() - Gdx.input.getY() - initialY, BOTTOM_BOUNDARY, TOP_LINE_Y);
                    ballCenter.set(position.x - 10f, position.y + height / 2f);
                    GAMEWORLD.getBall().setPosition(ballCenter);
                } else if (Gdx.input.getX() - initialX <= LEFT_BOUNDARY) {
                    // Transition to left side
                    position.x = LEFT_BOUNDARY;
                    position.y = MathUtils.clamp(Gdx.graphics.getHeight() - Gdx.input.getY() - initialY, BOTTOM_BOUNDARY, TOP_LINE_Y);
                } else if (Gdx.input.getX() - initialX >= RIGHT_BOUNDARY) {
                    // Transition to right side
                    position.x = RIGHT_BOUNDARY;
                    position.y = MathUtils.clamp(Gdx.graphics.getHeight() - Gdx.input.getY() - initialY, BOTTOM_BOUNDARY, TOP_LINE_Y);
                } else {
                    // Free movement (shouldn't normally occur)
                    position.x = MathUtils.clamp(Gdx.input.getX() - initialX, LEFT_BOUNDARY, RIGHT_BOUNDARY);
                    position.y = MathUtils.clamp(Gdx.graphics.getHeight() - Gdx.input.getY() - initialY, BOTTOM_BOUNDARY, TOP_LINE_Y);
                    ballCenter.set(position.x + width/2f, position.y - 10f);
                    GAMEWORLD.getBall().setPosition(ballCenter);

                }

                /*
                // free movement
                position.x = MathUtils.clamp(Gdx.input.getX() - initialX, 400, Gdx.graphics.getWidth() - 435);
                position.y = MathUtils.clamp(Gdx.graphics.getHeight() - Gdx.input.getY() - initialY, 50, Gdx.graphics.getHeight() - 50);
                 */
                System.out.println("dragging " + position);

                this.getBody().setTransform(position.x, position.y, this.getBody().getAngle());
                System.out.println(position);
            }
        } else {
            isDragging = false;
        }
    }

    public Vector2 getBallCenter() {
        return ballCenter;
    }

    public Vector2 getLaunchVelocity() {
        return launchVelocity;
    }

    @Override
    public void update(float deltaTime) {
        position = body.getPosition();

        handleMouseAiming();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            usingMouse = false;  // disable mouse control when using arrow keys
            handleKeyboardAiming();
        }

        handleInput();

        calculateLaunchVelocity();
        //updateAimingLine(ballCenter, launchVelocity);
    }

    private void drawAimingLine(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.CYAN);
        float startX = ballCenter.x;
        float startY = ballCenter.y;

        // Calculate where the line ends based on the aim direction and a line length
        float lineLength = 100f;  // Length of the line, you can adjust this
        float endX = startX + MathUtils.cosDeg(aimAngle) * lineLength;
        float endY = startY + MathUtils.sinDeg(aimAngle) * lineLength;

        // Draw the line from the launcher's position to the aim direction
        shapeRenderer.line(startX, startY, endX, endY);
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.SLATE);
        shapeRenderer.line(LEFT_BOUNDARY + width/2, BOTTOM_BOUNDARY + height/2, LEFT_BOUNDARY + width/2, TOP_LINE_Y + height/2); // Left boundary
        shapeRenderer.line(RIGHT_BOUNDARY + width/2, BOTTOM_BOUNDARY + height/2, RIGHT_BOUNDARY + width/2, TOP_LINE_Y + height/2); // Right boundary
        shapeRenderer.line(LEFT_BOUNDARY + width/2, TOP_LINE_Y + height/2, RIGHT_BOUNDARY  + width/2, TOP_LINE_Y + height/2); // Top boundary
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.TAN);
        shapeRenderer.rect(position.x, position.y, width, height);
        drawAimingLine(shapeRenderer);
        //aimingLine.render(shapeRenderer);
    }
}
