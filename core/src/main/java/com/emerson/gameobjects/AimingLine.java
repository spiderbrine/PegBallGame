package com.emerson.gameobjects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.emerson.world.GameWorld;

/*
public class AimingLine {
    private GameWorld gameWorld;
    private World gameWorld;
    private Array<Vector2> trajectoryPoints; // store the points of the trajectory

    public AimingLine(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.gameWorld = gameWorld.getWorld();
        this.trajectoryPoints = new Array<>();
    }

    public void calculateTrajectory(Vector2 startPosition, Vector2 launchVelocity, float maxLength) {
        trajectoryPoints.clear(); // clear previous trajectory
        float timeStep = 1/60f;
        Vector2 gravity = new Vector2(0, -gameWorld.getGravity().y);

        for (float t = 0; t < maxLength; t += timeStep) {
            // update position and velocity
            startPosition.add(launchVelocity.x * timeStep, launchVelocity.y * timeStep);
            launchVelocity.add(gravity.x * timeStep, gravity.y * timeStep);

            // add to current trajectory
            trajectoryPoints.add(new Vector2(startPosition));

            // stop simulation if the point goes out of bounds or hits an object
            if (checkCollisionWithPeg(startPosition)) {
                break;
            }
        }
    }

    private boolean checkCollisionWithPeg(Vector2 position) {

        for (Peg peg : gameWorld.getPegs()) {
            if (peg.getBody().getPosition().dst(position) <= peg.getRadius()) {
                return true;
            }
        }
        return false;
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < trajectoryPoints.size - 1; i++) {
            Vector2 current = trajectoryPoints.get(i);
            Vector2 next = trajectoryPoints.get(i + 1);
            shapeRenderer.line(current.x, current.y, next.x, next.y);
        }
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
    }
}

 */

public class AimingLine {
    private GameWorld gameWorld;
    private Array<Vector2> trajectoryPoints = new Array<>();
    private final float timeStep = .01f;  // Simulation time step (60 FPS)
    private final float gravityScale = 9.8f; // Gravity scale (adjust as needed)

    // Constructor to initialize the aiming line with a reference to the Box2D world
    public AimingLine(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    // Calculate the trajectory path of the ball
    public void calculateTrajectory(Vector2 startPosition, Vector2 launchVelocity, float maxLength) {
        trajectoryPoints.clear();  // Clear previous trajectory
        float totalLength = 0f;
        float velocityScale = .1f;  // A factor to scale down velocity (try experimenting with this value)

        // Adjust the max length for the trajectory (if needed, based on PPM scale)
        Vector2 position = new Vector2(startPosition);
        Vector2 velocity = new Vector2(launchVelocity.scl(velocityScale));
        Vector2 gravity = new Vector2(0, -gravityScale);  // Default gravity (downwards)
        Vector2 lastPosition = position.cpy();  // To calculate the distance from the last point

        // Debug output for initial position and velocity
        System.out.println("Calculating trajectory...");
        System.out.println("Start Position: " + startPosition);
        System.out.println("Launch Velocity: " + launchVelocity);

        // Simulate the trajectory path
        for (float t = 0; t < 5f; t += timeStep) {
            // Update position based on velocity
            position.add(velocity.x * timeStep, velocity.y * timeStep);

            // Apply gravity (adjusting vertical velocity)
            velocity.add(gravity.x * timeStep, gravity.y * timeStep);

            // Calculate the distance from the last point to check for max length
            totalLength += position.dst(lastPosition);
            lastPosition.set(position);

            // Debugging: output trajectory point calculation
            System.out.println("Point at time " + t + ": " + position);
            System.out.println("Total Length so far: " + totalLength);

            // Stop if the trajectory exceeds max length
            if (totalLength > maxLength) {
                System.out.println("Total length reached max length. Stopping trajectory.");
                break;
            }

            // Add the new position to the trajectory points
            trajectoryPoints.add(new Vector2(position));
        }

        // Debug output
        System.out.println("Trajectory points: " + trajectoryPoints.size);
    }

    // Render the trajectory using ShapeRenderer
    public void render(ShapeRenderer shapeRenderer) {
        if (trajectoryPoints.size < 2) {
            System.out.println("No points to render.");
            return;  // No line to draw if there aren't at least two points
        }

        shapeRenderer.setColor(1, 0, 0, 1); // Set the line color to red for the trajectory
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);

        // Draw the trajectory line
        for (int i = 0; i < trajectoryPoints.size - 1; i++) {
            Vector2 current = trajectoryPoints.get(i);
            Vector2 next = trajectoryPoints.get(i + 1);
            shapeRenderer.line(current.x, current.y, next.x, next.y);
        }

        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
    }
}
