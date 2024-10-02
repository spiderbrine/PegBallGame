package com.emerson.pegballgame;

import com.badlogic.gdx.Game;
import com.emerson.gamescreen.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PegBallStart extends Game {
    @Override
    public void create() {
        // Set the initial screen
        this.setScreen(new GameScreen());
    }
}
