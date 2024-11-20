package com.emerson.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;
import com.emerson.gameobjects.Peg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameContactListener implements ContactListener {

    private final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

    private Map<Integer, Peg> pegMap = new HashMap<>();
    private List<Peg> pegs = new ArrayList<>();
    private List<Peg> pegsHitList = new ArrayList<>();
    private List<Peg> orangePegsHitList = new ArrayList<>();
    private int pegsHit = 0;
    private int orangePegsHit = 0;
    private int totalOrangePegsHit = 0;
    private int totalScore = 0;
    private int turnScore = 0;
    private int scoreMultiplier = 1;
    private GameWorld world;
    private Stage stage;
    private boolean freeBall25k = false;
    private boolean freeBall75k = false;
    private boolean freeBall125k = false;

    public GameContactListener(Map<Integer, Peg> pegMap, List<Peg> pegs, GameWorld world, Stage stage) {
        this.pegMap = pegMap;
        this.pegs = pegs;
        this.world = world;
        this.stage = stage;
    }

    @Override
    public void beginContact(Contact contact) {
        // Called when two fixtures begin to touch
        int stuck = 0;
        System.out.println("Collision detected!");
        int pegID = getPegIDFromContact(contact);
        System.out.println(pegID);
        if (pegMap.containsKey(pegID)){
            Peg hitPeg = pegMap.get(pegID);
            handlePegHit(pegID, hitPeg);
        }
        /*
        for (Peg peg : pegsHitList) {
                peg.pegDisappear(peg.getBody().getWorld(), pegMap, pegs); // for unstuck later
        }
        */
    }

    private void handlePegHit(int pegID, Peg hitPeg) {
        if (!hitPeg.isHit()) {
            System.out.println("Peg " + pegID + " has been hit!");
            hitPeg.pegHit();
            pegsHitList.add(hitPeg);
            pegsHit++;
            if (totalOrangePegsHit < 10) {
                scoreMultiplier = 1;
            } else if (totalOrangePegsHit >= 10 && totalOrangePegsHit < 15) {
                scoreMultiplier = 2;
            } else if (totalOrangePegsHit >= 15 && totalOrangePegsHit < 19) {
                scoreMultiplier = 3;
            } else if (totalOrangePegsHit >= 19 && totalOrangePegsHit < 22) {
                scoreMultiplier = 5;
            } else if (totalOrangePegsHit >= 22) {
                scoreMultiplier = 100;
            }
            if (hitPeg.getPegType() == 1) {
                // blue peg on hit stuff
                turnScore = turnScore + (10 * scoreMultiplier);
            } else if (hitPeg.getPegType() == 2) {
                // orange peg on hit stuff
                turnScore = turnScore + (100 * scoreMultiplier);
                orangePegsHitList.add(hitPeg);
                orangePegsHit++;
                totalOrangePegsHit++;
            } else if (hitPeg.getPegType() == 3) {
                int bonusPoints = (10 * scoreMultiplier) * 50;
                turnScore = turnScore + bonusPoints;
                Label messageLabel = new Label("BONUS POINTS! " + bonusPoints, skin);
                messageLabel.setColor(Color.PURPLE);
                messageLabel.setPosition((hitPeg.getPosition().x) - (messageLabel.getWidth() / 2),
                    (hitPeg.getPosition().y + 10));
                messageLabel.setFontScale(1f);
                stage.addActor(messageLabel);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        messageLabel.remove();
                    }
                },2f);
            }
            if (turnScore >= 125000 && !freeBall125k) {
                world.giveFreeBall(new Vector2(hitPeg.getPosition().x, hitPeg.getPosition().y + 10));
                freeBall125k = true;
            } else if (turnScore >= 75000 && !freeBall75k) {
                world.giveFreeBall(new Vector2(hitPeg.getPosition().x, hitPeg.getPosition().y + 10));
                freeBall75k = true;
            } else if (turnScore >= 25000 && !freeBall25k) {
                world.giveFreeBall(new Vector2(hitPeg.getPosition().x, hitPeg.getPosition().y + 10));
                freeBall25k = true;
            }
        }
    }

    private int getPegIDFromContact(Contact contact) {
        Object userDataA = contact.getFixtureA().getUserData();
        Object userDataB = contact.getFixtureB().getUserData();

        if (userDataA instanceof Integer) {
            return (int) userDataA;
        } else if (userDataB instanceof Integer) {
            return (int) userDataB;
        }

        return -1;  // no peg ID found
    }

    @Override
    public void endContact(Contact contact) {
        // Called when two fixtures stop touching
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Called before the collision is resolved (useful for custom collision response)
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Called after the collision is resolved
    }

    public int getPegsHit() {
        return pegsHit;
    }

    public void resetPegsHit() {
        pegsHit = 0;
    }

    public int getOrangePegsHit() {
        return orangePegsHit;
    }

    public void resetOrangePegsHit() {
        orangePegsHit = 0;
    }

    public int getTotalOrangePegsHit() {
        return totalOrangePegsHit;
    }

    public int getTurnScore() {
        return turnScore;
    }

    public void resetTurnScore() {
        turnScore = 0;
    }

    public void resetFreeBalls() {
        freeBall25k = false;
        freeBall75k = false;
        freeBall125k = false;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int newTotalScore) {
        totalScore = newTotalScore;
    }
}
