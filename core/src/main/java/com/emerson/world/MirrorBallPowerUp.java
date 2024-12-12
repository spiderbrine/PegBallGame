package com.emerson.world;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.emerson.gameobjects.Ball;
import com.emerson.gameobjects.Peg;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class MirrorBallPowerUp implements PowerUp {
    private boolean activated = false;
    private GameWorld gameWorld;
    private int maxTurns = 3;

    @Override
    public void activate(Ball ball, GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        if (activated) {
            maxTurns += 4;
            System.out.println("Mirror-ball upgraded!");
        }
        activated = true;
        System.out.println("Mirror-ball activated!");
    }

    @Override
    public void deactivate(Ball ball, GameWorld gameWorld) {
        activated = false;
        System.out.println("Mirror-ball deactivated!");
    }

    public void hitMirrorPeg(Peg hitPeg) {
        float mirrorX = (VIRTUAL_WIDTH - hitPeg.getBody().getPosition().x);

        // find opposite peg
        for (Peg peg : gameWorld.getPegs()) {
            if (Math.abs(peg.getBody().getPosition().x - mirrorX) < 10f &&
                Math.abs(peg.getBody().getPosition().y - hitPeg.getBody().getPosition().y) < 10f) {
                System.out.println("Mirror peg found");
                gameWorld.hitPeg(peg);
            }
        }
    }

    @Override
    public void update(float deltaTime, Ball ball, GameWorld gameWorld) {

    }

    public void render(ShapeRenderer shapeRenderer) {

    }

    public boolean active() {
        return activated;
    }

    public int getMaxTurns() {
        return maxTurns;
    }
}
