package com.emerson.world;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.emerson.gameobjects.Ball;
import com.emerson.gameobjects.Peg;

import java.util.ArrayList;
import java.util.List;

public class SludgeBallPowerUp implements PowerUp {
    private boolean activated = false;
    private GameWorld gameWorld;
    private float DRIP_RADIUS = 85f;
    private boolean isProcessing = false;
    private List<SludgeDrip> sludgeLines = new ArrayList<>();
    private int maxTurns = 2;

    @Override
    public void activate(Ball ball, GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        // upgrade logic
        if (activated) {
            maxTurns += 1;
            DRIP_RADIUS = 100f;
            System.out.println("Sludge-ball upgraded!");
        }
        activated = true;
        System.out.println("Sludge-ball activated!");
    }

    @Override
    public void deactivate(Ball ball, GameWorld gameWorld) {
        activated = false;
        System.out.println("Sludge deactivated");
    }

    public void handlePegHit(Peg hitPeg) {
        if (!activated || isProcessing) return;

        isProcessing = true;

        System.out.println("Sludge-ball hit peg: " + hitPeg.getPegID());

        applySludgeEffect(hitPeg);

        // find pegs below the hit peg within drip radius
        Vector2 hitPegPosition = hitPeg.getBody().getPosition();
        List<Peg> pegsBelow = new ArrayList<>();

        for (Peg peg : gameWorld.getPegs()) {
            if (!peg.isHit() && !peg.isDecayed() && peg.getBody().getPosition().y < hitPegPosition.y) {
                float distance = hitPegPosition.dst(peg.getBody().getPosition());
                if (distance <= DRIP_RADIUS) {
                    pegsBelow.add(peg);
                }
            }
        }

        // sludge below
        for (Peg pegBelow : pegsBelow) {
            applySludgeEffect(pegBelow);

            sludgeLines.add(new SludgeDrip(
                hitPeg.getBody().getPosition().cpy(),
                pegBelow.getBody().getPosition().cpy()
            ));
        }

        isProcessing = false;
    }

    private void applySludgeEffect(Peg peg) {
        peg.setDecayed(true);
        gameWorld.hitPeg(peg);
        System.out.println("Peg " + peg.getPegID() + " is now decayed!");

        sludgeLines.add(new SludgeDrip(
            peg.getBody().getPosition().cpy(), // start
            peg.getBody().getPosition().cpy() // end
        ));
    }

    @Override
    public void update(float deltaTime, Ball ball, GameWorld gameWorld) {
        for (int i = sludgeLines.size() - 1; i >= 0; i--) {
            SludgeDrip arc = sludgeLines.get(i);
            arc.update(deltaTime);
            if (arc.isExpired()) {
                sludgeLines.remove(i);
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (activated) {
            for (SludgeDrip arc : sludgeLines) {
                arc.render(shapeRenderer);
            }
        }
    }

    public boolean active() {
        return activated;
    }

    public int getMaxTurns() {
        return maxTurns;
    }
}
