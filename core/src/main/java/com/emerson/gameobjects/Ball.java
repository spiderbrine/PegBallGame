package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Ball extends GameObject{

    private World world;
    private Body body;
    private float radius;

    public Ball(World world, Body body, Vector2 position, float radius) {
        super(body, position, radius * 2, radius * 2);  // width and height are based on diameter
        this.world = world;
        this.body = body;
        this.radius = radius;

        //ballBody.setLinearDamping(0);
        //ballBody.setAngularDamping(0);
        body.setType(BodyDef.BodyType.KinematicBody); // kinematic because the ball can't move until it shoots but needs to move with launcher
        System.out.println("Ball is static");
        body.setGravityScale(0); // disable gravity
        body.setLinearVelocity(0, 0);  // Keep it from moving
        body.setUserData("ball"); // makes the ball identifiable for collision

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        // fixture that attaches circle to body
        FixtureDef ballFixtureDef = new FixtureDef();
        ballFixtureDef.shape = circle;
        ballFixtureDef.density = 0.85f; // mass 0.85 is good
        ballFixtureDef.friction = 0f; // 0.2 is good
        ballFixtureDef.restitution = 0.78f;  // bounce 0.78 is good

        body.createFixture(ballFixtureDef);

        circle.dispose();
    }

    @Override
    public void update(float deltaTime) {
        // sync position with body position
        position = body.getPosition();
    }

    public static final float PPM = 100;  // this is ass 6 hours of my life spent on this***
    @Override
    public void render(ShapeRenderer shapeRenderer) {

        //*** I don't know exactly what's going on here and why it works but we ball
        float ballX = body.getPosition().x * PPM;
        float ballY = body.getPosition().y * PPM;
        shapeRenderer.circle(position.x, position.y, width / 2);  // render based on width (radius)

    }

    public void destroy() {
        world.destroyBody(this.body);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
