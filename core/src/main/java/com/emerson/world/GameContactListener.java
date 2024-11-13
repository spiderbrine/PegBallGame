package com.emerson.world;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;
import com.emerson.gameobjects.Peg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameContactListener implements ContactListener {

    private Map<Integer, Peg> pegMap = new HashMap<>();
    private List<Peg> pegs = new ArrayList<>();
    private List<Peg> pegsHitList = new ArrayList<>();
    private List<Peg> orangePegsHitList = new ArrayList<>();
    private int pegsHit = 0;
    private int orangePegsHit = 0;
    private int totalOrangePegsHit = 0;

    public GameContactListener(Map<Integer, Peg> pegMap, List<Peg> pegs) {
        this.pegMap = pegMap;
        this.pegs = pegs;
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
            if (hitPeg.getPegType() == 2) {
                orangePegsHitList.add(hitPeg);
                orangePegsHit++;
                totalOrangePegsHit++;
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
}
