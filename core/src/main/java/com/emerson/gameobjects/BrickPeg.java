package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.emerson.world.GameWorld;

public class BrickPeg extends Peg {

    private float width;
    private float height;

    public BrickPeg(GameWorld gameWorld, Body body, Vector2 position, float width, float height, int pegID) {
        super(gameWorld, body, position, width, pegID);
        this.width = width;
        this.height = height;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.StaticBody;

        PolygonShape rectangleShape = new PolygonShape();
        rectangleShape.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectangleShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.7f;

        body.createFixture(fixtureDef);
        body.getFixtureList().first().setUserData(getPegID());
        body.getFixtureList().first().setUserData("brick");

        rectangleShape.dispose();
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(position.x - width / 2, position.y - height / 2, width, height);
    }
}
