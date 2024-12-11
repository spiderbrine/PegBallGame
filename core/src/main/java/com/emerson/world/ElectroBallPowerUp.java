package com.emerson.world;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.emerson.gameobjects.Ball;
import com.emerson.gameobjects.Peg;

import java.util.ArrayList;
import java.util.List;

public class ElectroBallPowerUp implements PowerUp {
    private boolean activated = false;
    private final float ELECTRO_RADIUS = 70f;
    private List<ElectricArc> electricArcs = new ArrayList<>();
    private boolean isProcessing = false;
    private GameWorld gameWorld;
    private int maxTurns = 2;
    private int electroNum = 2;

    @Override
    public void activate(Ball ball, GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        if (activated) {
            maxTurns += 2;
            electroNum = 3;
            System.out.println("Electro-ball upgraded!");
        }
        activated = true;
        System.out.println("Power up activated");
        System.out.println("Electro-ball activated!");
    }

    public void activateNearbyPegs(Peg hitPeg) {
        if (!activated || isProcessing) return;

        isProcessing = true;

        Vector2 hitPegPosition = hitPeg.getBody().getPosition();
        List<Peg> nearbyPegs = new ArrayList<>();

        // find all pegs within the electro radius
        for (Peg peg : gameWorld.getPegs()) {
            if (!peg.isHit() && peg != hitPeg) { // skip already hit pegs and the current one
                float distance = hitPegPosition.dst(peg.getBody().getPosition());
                if (distance <= ELECTRO_RADIUS) {
                    nearbyPegs.add(peg);
                }
            }
        }

        // activate up to 2 nearby pegs
        int pegsToActivate = Math.min(electroNum, nearbyPegs.size());
        for (int i = 0; i < pegsToActivate; i++) {
            Peg pegToActivate = nearbyPegs.get(i);
            gameWorld.hitPeg(pegToActivate);
            electricArcs.add(new ElectricArc(hitPegPosition, pegToActivate.getPosition()));
            System.out.println("Electro-ball activated peg: " + pegToActivate.getPegID());
        }

        isProcessing = false;
    }

    @Override
    public void update(float deltaTime, Ball ball, GameWorld gameWorld) {
        for (int i = electricArcs.size() - 1; i >= 0; i--) {
            ElectricArc arc = electricArcs.get(i);
            arc.update(deltaTime);
            if (arc.isExpired()) {
                electricArcs.remove(i);
            }
        }
    }

    @Override
    public void deactivate(Ball ball, GameWorld gameWorld) {
        activated = false;
        electroNum = 2;
        System.out.println("Electro deactivated");
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (activated) {
            for (ElectricArc arc : electricArcs) {
                arc.render(shapeRenderer);
            }
            shapeRenderer.setColor(0.5f, 0.5f, 1f, 1f);
            shapeRenderer.circle(gameWorld.getBall().getPosition().x, gameWorld.getBall().getPosition().y, 10);
        }
    }

    public boolean active() {
        return activated;
    }

    public int getMaxTurns() {
        return maxTurns;
    }
}
