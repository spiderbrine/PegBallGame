package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Obstacle extends GameObject {

    private World world;
    private Body body;
    private Shape shape;

    public Obstacle(World world, Shape shape, Body body, Vector2 position,float width, float height) {
        super(body, position, width, height);
        this.world = world;
        this.body = body;
        this.shape = shape;

        body.setType(BodyDef.BodyType.StaticBody);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.7f;
        body.createFixture(fixtureDef);
        body.getFixtureList().first().setUserData("wall");
    }

    @Override
    public void update(float deltaTime) {
        position = body.getPosition();
    }

    public static final float PPM = 100;
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(position.x, position.y, width, height);
    }
}
