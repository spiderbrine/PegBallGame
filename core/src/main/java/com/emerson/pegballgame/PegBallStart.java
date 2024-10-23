package com.emerson.pegballgame;

import com.badlogic.gdx.Game;
import com.emerson.gamescreens.GameScreen;
import com.emerson.gamescreens.TitleScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PegBallStart extends Game {
    @Override
    public void create() {
        // Set the initial screen
        this.setScreen(new TitleScreen(this));
    }
}
