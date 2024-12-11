package com.emerson.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.*;
import com.emerson.gameobjects.Ball;

public class BouncyBallPowerUp implements PowerUp {
    private boolean activated = false;
    private GameWorld gameWorld;
    private Body floor = null;
    private int maxFloorHits = 3;

    @Override
    public void activate(Ball ball, GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        if (activated) {
            maxFloorHits = 6;
            System.out.println("Bouncy-ball upgraded!");
        } else {
            createBouncyFloor(gameWorld);
        }
        activated = true;
        System.out.println("Power up activated");
        setBallDef(ball, activated);
        System.out.println("Bouncy-ball activated!");
    }

    public void setBallDef(Ball ball, boolean activated) {
        Body ballBody = ball.getBody();

        System.out.println("Body");

        // get current fixture
        Fixture ballFixture = ballBody.getFixtureList().first();
        System.out.println("fixture copy");
        // basically just dupe the whole thing (this is beyond silly)
        FixtureDef newFixtureDef = new FixtureDef();
        CircleShape circle = new CircleShape();
        circle.setRadius(ball.getRadius());
        newFixtureDef.shape = circle;
        newFixtureDef.friction = 0f;
        System.out.println("Dupe fixture stats");

        if (activated) {
            newFixtureDef.density = 0.7f;
            newFixtureDef.restitution = 3f;
            System.out.println("Make changes (active)");
        } else {
            newFixtureDef.density = 0.85f;
            newFixtureDef.restitution = 0.78f;
            System.out.println("Make changes (deactive)");
        }

        // destroy old fixture and apply new one
        gameWorld.queuePhysicsChange(() -> {
            ballBody.destroyFixture(ballFixture);
            System.out.println("Destroy old fixture");
            Fixture newFixture = ballBody.createFixture(newFixtureDef);
            newFixture.setUserData("ball");
            System.out.println("Ball fixture userData set to 'ball'");
            System.out.println("Apply new fixture");
            circle.dispose();
            System.out.println("Ball updated to activated: " + activated);
        });

    }

    private void createBouncyFloor(GameWorld gameWorld) {
        BodyDef floorDef = new BodyDef();
        floorDef.type = BodyDef.BodyType.StaticBody;
        floorDef.position.set(400, 0);

        PolygonShape floorShape = new PolygonShape();
        floorShape.setAsBox(480, 50f);

        FixtureDef floorFixtureDef = new FixtureDef();
        floorFixtureDef.shape = floorShape;
        floorFixtureDef.friction = 0f;
        floorFixtureDef.restitution = 0.5f;

        if (floor == null) {
            gameWorld.queuePhysicsChange(() -> {
                floor = gameWorld.getWorld().createBody(floorDef);
                floor.setUserData("floor");
                Fixture floorFixture = floor.createFixture(floorFixtureDef);
                floorFixture.setUserData("floor");
                floorShape.dispose();
                System.out.println("Bouncy floor fixture userData set to 'floor'");
                System.out.println("Bouncy floor created!");
            });
        }
    }

    @Override
    public void deactivate(Ball ball, GameWorld gameWorld) {
        activated = false;
        setBallDef(ball, activated);
    }

    @Override
    public void update(float deltaTime, Ball ball, GameWorld gameWorld) {

    }

    public void render(ShapeRenderer shapeRenderer) {
        if (activated) {
            shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1f);
            shapeRenderer.circle(gameWorld.getBall().getPosition().x, gameWorld.getBall().getPosition().y, 10);
        }
        if (floor != null) {
            shapeRenderer.setColor(Color.CORAL);
            shapeRenderer.rect(400, 0, 480, 50);
        }
    }

    public int getMaxFloorHits() {
        return maxFloorHits;
    }

    public Body getFloor() {
        return floor;
    }

    public void setFloor(Body floor) {
        this.floor = floor;
    }

    public boolean active() {
        return activated;
    }
}
