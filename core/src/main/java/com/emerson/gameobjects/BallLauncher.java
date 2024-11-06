package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

    private Body body;

    public BallLauncher(GameWorld gameWorld, Body body, Vector2 position, float width, float height) {

        super(body, position, width, height);
        this.body = body;
        this.launchPower = 50f * POWER_MULTIPLIER;
        this.launchDirection = new Vector2(0f, -1f); // down
        this.launchVelocity = new Vector2();
        this.aimingLine = new AimingLine(gameWorld);
        this.ballCenter = new Vector2(position.x + width / 2, position.y);


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

    public void shootBall(Ball ball) {

        ball.getBody().setType(BodyDef.BodyType.DynamicBody); // change the ball to dynamic so it can be affected by forces
        System.out.println("Ball is now able to move");
        ball.getBody().applyLinearImpulse(new Vector2(launchVelocity), ball.getBody().getWorldCenter(), true); // "shoot" ball
        ball.getBody().setGravityScale(1);
        System.out.println("Ball shot!");
        System.out.println(ball.getBody().getLinearVelocity());
    }

    public void updateAimingLine(Vector2 startPosition, Vector2 launchVelocity){
        float maxLength = 100f;
        aimingLine.calculateTrajectory(startPosition, launchVelocity, maxLength);
    }

    @Override
    public void update(float deltaTime) {
        position = body.getPosition();
        calculateLaunchVelocity();
        updateAimingLine(ballCenter, launchVelocity);
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(position.x, position.y, width, height);
        aimingLine.render(shapeRenderer);
    }
}
