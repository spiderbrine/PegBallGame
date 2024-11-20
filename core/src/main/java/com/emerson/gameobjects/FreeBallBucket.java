package com.emerson.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class FreeBallBucket extends GameObject {

    private float speed;
    private boolean movingRight = true;

    public FreeBallBucket(Body body, Vector2 position, float width, float height, float speed) {
        super(body, position, width, height);
        this.speed = speed;
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
                position.x = Gdx.graphics.getWidth() - width - 401;
                movingRight = false;
            }
        } else {
            position.x -= currentSpeed * deltaTime;
            if (position.x <= 401) {
                position.x = 401;
                movingRight = true;
            }
        }

        //System.out.println("Bucket Position: " + position.x + ", Speed: " + currentSpeed);
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(position.x, position.y, width, height);
    }
}
