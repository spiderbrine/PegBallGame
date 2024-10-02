package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.emerson.world.GameWorld;

public class AimingLine {
    private GameWorld gameWorld;
    private World world;
    private Array<Vector2> trajectoryPoints; // store the points of the trajectory

    public AimingLine(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.world = gameWorld.getWorld();
        this.trajectoryPoints = new Array<>();
    }

    public void calculateTrajectory(Vector2 startPosition, Vector2 launchVelocity, float maxLength) {
        trajectoryPoints.clear();

        Vector2 currentPosition = new Vector2(startPosition);
        Vector2 velocity = new Vector2(launchVelocity);
        float timeStep = 1 / 60f;  // time step for accuracy
        float accumulatedLength = 0f;

        for (int i = 0; i < 300; i++) { // max iterations to avoid infinite loops
            // calculate the next position using simple physics (displacement)
            velocity.add(0f, world.getGravity().y * timeStep);  // apply gravity
            Vector2 nextPosition = new Vector2(currentPosition).mulAdd(velocity, timeStep);

            // calculate the distance traveled in this step
            float stepLength = nextPosition.dst(currentPosition);
            accumulatedLength += stepLength;

            // check if max length is reached
            if (accumulatedLength >= maxLength) {
                break;
            }

            // Check for peg collisions (basic AABB or radius-based detection)
            if (checkCollisionWithPeg(nextPosition)) {
                break;
            }

            // Add the point to the trajectory
            trajectoryPoints.add(nextPosition);

            // Move to the next position
            currentPosition.set(nextPosition);
        }
    }

    // Basic collision check with pegs (you'll need to adjust for your specific collision logic)
    private boolean checkCollisionWithPeg(Vector2 position) {
        // Loop through your Peg objects and check if the ball position intersects with a peg
        for (Peg peg : gameWorld.getPegs()) {
            if (position.dst(peg.getPosition()) <= peg.getRadius()) {
                return true;  // Collision detected
            }
        }
        return false;
    }

    public void render(ShapeRenderer shapeRenderer) {
        for (int i = 0; i < trajectoryPoints.size - 1; i++) {
            Vector2 pointA = trajectoryPoints.get(i);
            Vector2 pointB = trajectoryPoints.get(i + 1);
            shapeRenderer.line(pointA.x, pointA.y, pointB.x, pointB.y);
        }
    }
}
