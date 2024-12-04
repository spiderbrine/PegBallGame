package com.emerson.pegballgame;

import com.badlogic.gdx.Game;
import com.emerson.gamescreens.GameScreen;
import com.emerson.gamescreens.TitleScreen;
import com.emerson.savedata.GameDataManager;
import com.emerson.world.LevelManager;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PegBallStart extends Game {

    private LevelManager levelManager;
    private GameDataManager gameDataManager;

    @Override
    public void create() {
        // Set the initial screen
        levelManager = new LevelManager(this);
        gameDataManager = new GameDataManager(this);
        this.setScreen(new TitleScreen(this));
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }
    public GameDataManager getGameDataManager() {
        return gameDataManager;
    }

}
