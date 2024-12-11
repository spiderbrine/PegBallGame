package com.emerson.world;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.emerson.gameobjects.Ball;

public class OsmiumBallPowerUp implements PowerUp {
    private boolean activated = false;
    private GameWorld gameWorld;

    @Override
    public void activate(Ball ball, GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        activated = true;
        System.out.println("Power down activated");
        setBallDef(ball, activated);
        System.out.println("Osmium-ball activated!");
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
            newFixtureDef.density = 5f;
            newFixtureDef.restitution = 0.815f;
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
    public void deactivate(Ball ball, GameWorld gameWorld) {
        activated = false;
        setBallDef(ball, activated);
    }

    @Override
    public void update(float deltaTime, Ball ball, GameWorld gameWorld) {

    }

    public boolean active() {
        return activated;
    }
}
