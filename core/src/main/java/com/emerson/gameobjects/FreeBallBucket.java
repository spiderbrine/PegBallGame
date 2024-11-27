package com.emerson.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class FreeBallBucket extends GameObject {

    private float speed;
    private boolean movingRight = true;
    private World world;

    private Body leftWallBody;
    private Body rightWallBody;

    public FreeBallBucket(World world, Body body, Vector2 position, float width, float height, float speed) {
        super(body, position, width, height);
        this.world = world;
        this.speed = speed;

        leftWallBody = createWallBody(world, body.getPosition().x - 2.5f, body.getPosition().y, 5f, height);
        rightWallBody = createWallBody(world, body.getPosition().x + width - 2.5f, body.getPosition().y, 5f, height);
    }

    private Body createWallBody(World world, float x, float y, float wallWidth, float wallHeight) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        Body wallBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(wallWidth / 2, wallHeight /2, new Vector2(wallWidth / 2, wallHeight /2), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.5f;

        wallBody.createFixture(fixtureDef);
        shape.dispose();

        return wallBody;
    }

    public void destroyWalls() {
        if (leftWallBody != null) {
            world.destroyBody(leftWallBody);
            leftWallBody = null;
        }
        if (rightWallBody != null) {
            world.destroyBody(rightWallBody);
            rightWallBody = null;
        }
    }

    public boolean isBallInBucket(Ball ball) {
        return ball.getPosition().x >= position.x &&
            ball.getPosition().x <= position.x + width &&
            ball.getPosition().y <= position.y + height;
    }

    @Override
    public void update(float deltaTime) {
        // move bucket
        float slowdownRange = 25f; // slowdown near wall (in pix)
        float minSpeed = speed / 2; // min speed near wall

        // calculate distance from wall
        float distanceToLeftEdge = position.x - 401;
        float distanceToRightEdge = Gdx.graphics.getWidth() - (position.x + width) - 401;
        float distanceToClosestEdge = Math.min(distanceToLeftEdge, distanceToRightEdge);

        // proximity
        float proximityFactor = MathUtils.clamp(distanceToClosestEdge / slowdownRange, 0.5f, 1f);
        float currentSpeed = speed * proximityFactor;

        // move
        if (movingRight) {
            position.x += currentSpeed * deltaTime;
            if (position.x + width >= Gdx.graphics.getWidth() - 401) {
                position.x = Gdx.graphics.getWidth() - width - 401f;
                movingRight = false;
            }
        } else {
            position.x -= currentSpeed * deltaTime;
            if (position.x <= 401) {
                position.x = 401f;
                movingRight = true;
            }
        }

        leftWallBody.setTransform(position.x - 2.5f, position.y, leftWallBody.getAngle());
        rightWallBody.setTransform(position.x + width - 2.5f, position.y, rightWallBody.getAngle());
        //System.out.println("Bucket Position: " + position.x + ", Speed: " + currentSpeed);
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(position.x, position.y, width, height);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(leftWallBody.getPosition().x, leftWallBody.getPosition().y, 5f, height);
        shapeRenderer.rect(rightWallBody.getPosition().x, rightWallBody.getPosition().y, 5f, height);
    }
}
