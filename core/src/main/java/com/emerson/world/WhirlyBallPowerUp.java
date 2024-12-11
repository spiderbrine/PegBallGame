package com.emerson.world;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.emerson.gameobjects.Ball;
import com.emerson.gameobjects.Peg;

public class WhirlyBallPowerUp implements PowerUp {
    private boolean activated = false;
    private float ACTIVATION_RADIUS; // 50f
    private GameWorld gameWorld;

    @Override
    public void activate(Ball ball, GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        if (activated) {
            ACTIVATION_RADIUS = ACTIVATION_RADIUS + 25f;
            System.out.println("Whirly-ball upgraded!");
        } else {
            ACTIVATION_RADIUS = 45f;
        }
        activated = true;
        System.out.println("Power up activated");
        setBallDef(ball, activated);
        System.out.println("Whirly-ball activated!");
    }

    private void activateNearbyPegs(Vector2 center, float radius) {
        for (Peg peg : gameWorld.getPegs()) {
            if (!peg.isHit() && peg.getPosition().dst(center) <= radius) {
                gameWorld.hitPeg(peg);
            }
        }
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
            newFixtureDef.restitution = 0.85f;
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

    @Override
    public void update(float deltaTime, Ball ball, GameWorld gameWorld) {
        if (activated) {
            activateNearbyPegs(gameWorld.getBall().getPosition(), ACTIVATION_RADIUS);
        }
    }

    @Override
    public void deactivate(Ball ball, GameWorld gameWorld) {
        activated = false;
        setBallDef(ball, activated);
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (activated) {
            shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 0.01f);
            shapeRenderer.circle(gameWorld.getBall().getPosition().x, gameWorld.getBall().getPosition().y, ACTIVATION_RADIUS);
        }
    }

    public boolean active() {
        return activated;
    }
}
