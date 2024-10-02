package com.emerson.world;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.ContactImpulse;

public class GameContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        // Called when two fixtures begin to touch
        System.out.println("Collision detected!");

        // You can get the bodies involved in the collision like this:
        // contact.getFixtureA().getBody()
        // contact.getFixtureB().getBody()

        // From here, you can handle specific collision events
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
}
