package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class ScoreBucket extends GameObject {

    private int scoreValue;
    private World world;

    private Body leftWallBody;
    private Body rightWallBody;

    public ScoreBucket(World world, Body body, Vector2 position, float width, float height, int scoreValue) {
        super(body, position, width, height);
        this.world = world;
        this.scoreValue = scoreValue;
        body.setType(BodyDef.BodyType.StaticBody);

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

    public int getScoreValue() {
        return scoreValue;
    }

    @Override
    public void update(float deltaTime) {
        position = body.getPosition();
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(position.x, position.y, width, height);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(leftWallBody.getPosition().x, leftWallBody.getPosition().y, 5f, height);
        shapeRenderer.rect(rightWallBody.getPosition().x, rightWallBody.getPosition().y, 5f, height);
    }

}
