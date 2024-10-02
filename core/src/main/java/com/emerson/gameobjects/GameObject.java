package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class GameObject {
    protected Body body;
    protected Vector2 position;
    protected float width, height;

    public GameObject(Body body, Vector2 position, float width, float height) {
        this.body = body;
        this.position = body.getPosition();
        this.width = width;
        this.height = height;
    }

    public abstract void update(float deltaTime);
    public abstract void render(ShapeRenderer shapeRenderer);

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
        body.setTransform(position, body.getAngle()); // update body position
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
}
