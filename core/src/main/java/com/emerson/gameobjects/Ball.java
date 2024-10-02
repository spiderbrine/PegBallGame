package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Ball extends GameObject{

    private float radius;

    public Ball(Body body, Vector2 position, float radius) {
        super(body, position, radius * 2, radius * 2);  // width and height are based on diameter
        this.radius = radius;
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
