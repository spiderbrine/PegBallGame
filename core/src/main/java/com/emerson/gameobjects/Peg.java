package com.emerson.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;
import com.emerson.world.GameWorld;

import java.util.List;
import java.util.Map;

public class Peg extends GameObject{

    private GameWorld gameWorld;
    private Body body;
    private float radius;
    private int pegType;
    private int pegID;
    private boolean isHit = false;
    private boolean decayed = false;

    private Sound pegDisappear;

    public Peg(GameWorld gameWorld, Body body, Vector2 position, float radius, int pegID) {
        super(body, position, radius * 2, radius * 2);
        this.gameWorld = gameWorld;
        this.body = body;
        this.radius = radius;
        this.pegType = 1;
        this.pegID = pegID;
        pegDisappear = Gdx.audio.newSound(Gdx.files.internal("hover.ogg"));

        body.setType(BodyDef.BodyType.StaticBody);

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        FixtureDef pegFixtureDef = new FixtureDef();
        pegFixtureDef.shape = circle;
        pegFixtureDef.density = 0.5f;  // mass
        pegFixtureDef.friction = 0.2f; // 0.2
        pegFixtureDef.restitution = 0.7f;  // bounce 0.7

        body.createFixture(pegFixtureDef);
        body.getFixtureList().first().setUserData(pegID); // makes the peg identifiable for collision detection

        circle.dispose();
    }

    public void pegDisappear(World world, Map pegMap, List pegs) {
        if (body == null) return;

        Timer.schedule(new Timer.Task() {
            @Override
            public void run(){
                if (body != null) {
                    pegDisappear.setVolume(pegDisappear.play(), 0.25f);
                    pegMap.remove(body.getUserData());
                    System.out.println("Peg " + getPegID() + " removed from map");
                    world.destroyBody(body);
                    System.out.println("Peg " + getPegID() + "destroyed");
                    pegs.remove(Peg.this);
                    System.out.println("Peg " + getPegID() + " removed from list");
                    body = null;
                    System.out.println("Body " + getPegID() + " set null");
                }
            }
        }, 0.2f);
    }

    public void pegHit() {
        // hit flag true
        this.isHit = true;
        gameWorld.addPegToDisappearQueue(this);
    }

    public boolean isHit() {
        return isHit;
    }

    @Override
    public void update(float deltaTime) {
        if (body == null) {
            System.out.println("Body is null for peg with ID: " + pegID);
        } else {
            position = body.getPosition();
        }
    }

    public static final float PPM = 100;  // this is ass 6 hours of my life spent on this***
    @Override
    public void render(ShapeRenderer shapeRenderer) {

        //*** I don't know exactly what's going on here and why it works but we peg
        //float pegX = body.getPosition().x * PPM;
        //float pegY = body.getPosition().y * PPM;

        if (decayed) {
            shapeRenderer.setColor(0f, 1f, 0.5f, 1f);
        }
        shapeRenderer.circle(position.x, position.y, width / 2);  // render based on width (radius)

        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(position.x, position.y, width / 2);  // render based on width (radius)
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);

    }

    public boolean isDecayed() {
        return decayed;
    }

    public void setDecayed(boolean decayed) {
        this.decayed = decayed;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setPegType(int pegType) {
        this.pegType = pegType;
    }

    public int getPegType() {
        return pegType;
    }

    public void setPegID(int pegID) {
        this.pegID = pegID;
    }

    public int getPegID() {
        return pegID;
    }
}
