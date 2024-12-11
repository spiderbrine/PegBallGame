package com.emerson.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
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
    public WhirlyBallPowerUp whirlyBallPowerUp = new WhirlyBallPowerUp();
    public BouncyBallPowerUp bouncyBallPowerUp = new BouncyBallPowerUp();
    public ElectroBallPowerUp electroBallPowerUp = new ElectroBallPowerUp();
    public SludgeBallPowerUp sludgeBallPowerUp = new SludgeBallPowerUp();
    public MirrorBallPowerUp mirrorBallPowerUp = new MirrorBallPowerUp();
    public OsmiumBallPowerUp osmiumBallPowerUp = new OsmiumBallPowerUp();
    private int floorHits = 0;
    private int electroTurns = 0;
    private int sludgeTurns = 0;
    private int mirrorTurns = 0;

    public GameContactListener(Map<Integer, Peg> pegMap, List<Peg> pegs, GameWorld world, Stage stage) {
        this.pegMap = pegMap;
        this.pegs = pegs;
        this.world = world;
        this.stage = stage;
    }

    @Override
    public void beginContact(Contact contact) {
        // Called when two fixtures begin to touch
        System.out.println("Collision detected!");
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        System.out.println("Collision detected between: "
            + fixtureA.getUserData() + " and " + fixtureB.getUserData());

        boolean isBall = (fixtureA.getUserData() != null && fixtureA.getUserData().equals("ball")) ||
            (fixtureB.getUserData() != null && fixtureB.getUserData().equals("ball"));

        boolean isFloor = (fixtureA.getUserData() != null && fixtureA.getUserData().equals("floor")) ||
            (fixtureB.getUserData() != null && fixtureB.getUserData().equals("floor"));

        if (isBall && isFloor) {
            System.out.println("Ball hit the floor!");
            handleFloorHit();
        }
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

    private void handleFloorHit() {
        floorHits++;
        System.out.println("Floor hit" + floorHits);
        if ((bouncyBallPowerUp.getFloor() != null) && floorHits >= bouncyBallPowerUp.getMaxFloorHits()) {
            world.queuePhysicsChange(() -> {
                world.getWorld().destroyBody(bouncyBallPowerUp.getFloor());
                bouncyBallPowerUp.setFloor(null);
                System.out.println("Floor destroyed!");
            });
            floorHits = 0;
        }
    }

    public void handlePegHit(int pegID, Peg hitPeg) {
        if (!hitPeg.isHit()) {
            System.out.println("Peg " + pegID + " has been hit!");
            hitPeg.pegHit();
            pegsHitList.add(hitPeg);
            pegsHit++;
            if (hitPeg.getPegType() == 1) {
                // blue peg on hit stuff
                int bluePegPoints = 10 * scoreMultiplier;
                turnScore = turnScore + bluePegPoints;
                Label messageLabel = new Label("" + bluePegPoints, skin);
                messageLabel.setColor(0, 0, 1, 1);
                messageLabel.setPosition((hitPeg.getPosition().x) - (messageLabel.getWidth() / 2),
                    (hitPeg.getPosition().y + 10));
                messageLabel.setFontScale(1f);
                stage.addActor(messageLabel);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        messageLabel.remove();
                    }
                },1.5f);
            } else if (hitPeg.getPegType() == 2) {
                // orange peg on hit stuff
                int orangePegPoints = 100 * scoreMultiplier;
                turnScore = turnScore + orangePegPoints;
                orangePegsHitList.add(hitPeg);
                orangePegsHit++;
                totalOrangePegsHit++;
                world.removeOrangePegFromList(hitPeg);
                Label messageLabel = new Label("" + orangePegPoints, skin);
                messageLabel.setColor(Color.ORANGE);
                messageLabel.setPosition((hitPeg.getPosition().x) - (messageLabel.getWidth() / 2),
                    (hitPeg.getPosition().y + 10));
                messageLabel.setFontScale(1f);
                stage.addActor(messageLabel);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        messageLabel.remove();
                    }
                },1.5f);
            } else if (hitPeg.getPegType() == 3) {
                // purple peg
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
                },1.5f);
            } else if (hitPeg.getPegType() == 4) {
                // green peg
                // if level is mirror, activate mirror ball
                // if not check character and activate corresponding power
                if (world.getGAME().getLevelManager().getCurrentLevel().isMirror()) {
                    world.activatePowerUp(mirrorBallPowerUp, world.getBall());
                    mirrorTurns++;
                } else {
                    if (world.characterSelectMenu.getCharacter().equals("Whirly-Ball")) {
                        world.activatePowerUp(whirlyBallPowerUp, world.getBall());
                    } else if (world.characterSelectMenu.getCharacter().equals("Bouncy-Ball")) {
                        world.activatePowerUp(bouncyBallPowerUp, world.getBall());
                    } else if (world.characterSelectMenu.getCharacter().equals("Electro-Ball")) {
                        world.activatePowerUp(electroBallPowerUp, world.getBall());
                        electroTurns++;
                    } else if (world.characterSelectMenu.getCharacter().equals("Sludge-Ball")) {
                        world.activatePowerUp(sludgeBallPowerUp, world.getBall());
                        sludgeTurns++;
                    }
                }

                String labelText;
                if (mirrorBallPowerUp.active()) {
                    labelText = "Mirror-Ball!";
                } else {
                    labelText = world.characterSelectMenu.getCharacter() + "!";
                }

                Label messageLabel = new Label(labelText, skin);
                messageLabel.setColor(Color.LIME);
                messageLabel.setPosition((hitPeg.getPosition().x) - (messageLabel.getWidth() / 2),
                    (hitPeg.getPosition().y + 10));
                messageLabel.setFontScale(1f);
                stage.addActor(messageLabel);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        messageLabel.remove();
                    }
                },1.5f);

            } else if (hitPeg.getPegType() == 5) {
                // red peg
                world.activatePowerUp(osmiumBallPowerUp, world.getBall());

                Label messageLabel = new Label("OSMIUM BALL!", skin);
                messageLabel.setColor(Color.RED);
                messageLabel.setPosition((hitPeg.getPosition().x) - (messageLabel.getWidth() / 2),
                    (hitPeg.getPosition().y + 10));
                messageLabel.setFontScale(1f);
                stage.addActor(messageLabel);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        messageLabel.remove();
                    }
                },1.5f);
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
            if (electroBallPowerUp.active()) {
                electroBallPowerUp.activateNearbyPegs(hitPeg);
            }
            if (sludgeBallPowerUp.active()) {
                sludgeBallPowerUp.handlePegHit(hitPeg);
            }
            if (mirrorBallPowerUp.active()) {
                mirrorBallPowerUp.hitMirrorPeg(hitPeg);
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

    public int getScoreMultiplier() {
        return scoreMultiplier;
    }

    public void setScoreMultiplier(int scoreMultiplier) {
        this.scoreMultiplier = scoreMultiplier;
    }

    public int getElectroTurns() {
        return electroTurns;
    }

    public void incrementElectroTurns() {
        electroTurns++;
    }

    public void resetElectroTurns() {
        electroTurns = 0;
    }

    public int getSludgeTurns() {
        return sludgeTurns;
    }

    public void incrementSludgeTurns() {
        sludgeTurns++;
    }

    public void resetSludgeTurns() {
        sludgeTurns = 0;
    }

    public int getMirrorTurns() {
        return mirrorTurns;
    }

    public void incrementMirrorTurns() {
        mirrorTurns++;
    }

    public void resetMirrorTurns() {
        mirrorTurns = 0;
    }
}
