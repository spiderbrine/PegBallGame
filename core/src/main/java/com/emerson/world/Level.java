package com.emerson.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Timer;
import com.emerson.gameobjects.Peg;
import com.emerson.pegballgame.PegBallStart;

import java.util.ArrayList;

public class Level {

    private final PegBallStart GAME;
    private LevelManager levelManager;
    private String levelName;
    private ArrayList<Peg> pegs;
    private Texture background;
    private GameWorld gameWorld;
    private int totalPegs;
    private int orangePegs;

    public Level(PegBallStart game, LevelManager levelManager, String levelName, Texture background, int totalPegs, int orangePegs) {
        this.levelName = levelName;
        this.GAME = game;
        this.background = background;
        this.totalPegs = totalPegs;
        this.orangePegs = orangePegs;
    }

    public void setupLevel(int index) {
        gameWorld.setBackgroundTexture(background);
        gameWorld.createObstacles();
        if (index < 3) {
            createTestLevel();
        } else if (index == 3) {
            gameWorld.loadLevelFromFile("level.json", 7);
        }
        Timer.schedule(new Timer.Task() {
            @Override
            public void run(){
                gameWorld.displayCharacterSelectMenu();
            }
        }, 1.5f);
        gameWorld.createFreeBallBucket();
    }

    public void createTestLevel() {
        gameWorld.createPegsStaggeredGrid(10, 40, 20f, gameWorld.getWorldWidth() / 2.8f, 150f, 7f);
    }

    public GameWorld getGameWorld() {
        if (gameWorld == null) {
            gameWorld = new GameWorld(GAME, levelManager, totalPegs, orangePegs);
        }
        return gameWorld;
    }

    public void reset() {
        if (gameWorld != null) {
            gameWorld.reset();
            gameWorld = null;
        }
    }

    public String getLevelName() {
        return levelName;
    }
}
