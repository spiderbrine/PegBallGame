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

    private final float POWER_MULTIPLIER = 150;

    private float launchPower;
    private Vector2 launchDirection;
    private Vector2 launchVelocity;
    private AimingLine aimingLine;
    private Vector2 ballCenter;
    private boolean ballLauncherLoaded = false;

    private boolean usingMouse = false;
    private float aimAngle;
    private Vector2 previousMousePosition = new Vector2();

    private Body body;

    public BallLauncher(GameWorld gameWorld, Body body, Vector2 position, float width, float height) {

        super(body, position, width, height);
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

    @Override
    public void update(float deltaTime) {
        position = body.getPosition();

        handleMouseAiming();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            usingMouse = false;  // disable mouse control when using arrow keys
            handleKeyboardAiming();
        }

        calculateLaunchVelocity();

        updateAimingLine(ballCenter, launchVelocity);
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
        shapeRenderer.rect(position.x, position.y, width, height);
        aimingLine.render(shapeRenderer);
        drawAimingLine(shapeRenderer);
    }
}
