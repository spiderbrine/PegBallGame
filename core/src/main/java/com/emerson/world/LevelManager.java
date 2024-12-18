package com.emerson.world;

import com.badlogic.gdx.graphics.Texture;
import com.emerson.pegballgame.PegBallStart;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {

    private final PegBallStart GAME;
    private List<Level> levels;
    private int currentLevelIndex;

    public LevelManager(PegBallStart game) {
        this.GAME = game;
        levels = new ArrayList<>();
        currentLevelIndex = 0;
        loadLevels();
    }

    private void loadLevels() {
        //levels.add(new Level(GAME, this, "Test 100/1", null, 100, 1, false));
        //levels.add(new Level(GAME, this, "Practice", null, 100, 25, false));
        //levels.add(new Level(GAME, this, "Test 10/1", null, 10, 1, false));

        levels.add(new Level(GAME, this, "Alien", new Texture("Alien.png"), 77, 25, false));
        levels.add(new Level(GAME, this, "Solid Snake", new Texture("Snake.png"), 81, 25, false));
        levels.add(new Level(GAME, this, "Koala Mario", new Texture("Mario.png"), 82, 25, false));
        levels.add(new Level(GAME, this, "Houndeye", new Texture("Houndeye.png"), 86, 25, false));
        levels.add(new Level(GAME, this, "Roblox Dio", new Texture("Dio.png"), 88, 25, false));
        levels.add(new Level(GAME, this, "Snowman", new Texture("Snowman.png"), 93, 25, false));
        levels.add(new Level(GAME, this, "Garfield", new Texture("Garfield.png"), 94, 25, false));
        levels.add(new Level(GAME, this, "Bingus", new Texture("Bingus.png"), 96, 25, false));
        levels.add(new Level(GAME, this, "Godzilla", new Texture("Godzilla.png"), 97, 25, false));
        levels.add(new Level(GAME, this, "Fire Giant", new Texture("Giant.png"), 98, 25, false));

        levels.add(new Level(GAME, this, "Butterfly", new Texture("Butterfly.png"), 88, 25, true));
        levels.add(new Level(GAME, this, "Christmas", new Texture("Christmas.png"), 92, 25, true));
        levels.add(new Level(GAME, this, "Ladybug", new Texture("Ladybug.png"), 100, 25, true));
        levels.add(new Level(GAME, this, "Mushroom", new Texture("Mushroom.png"), 100, 25, true));
        levels.add(new Level(GAME, this, "Shield", new Texture("Shield.png"), 100, 25, true));

        levels.add(new Level(GAME, this, "Practice", null, 100, 25, false));
        levels.add(new Level(GAME, this, "Custom", null, 100, 1, false));
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public void nextLevel() {
        if (currentLevelIndex < levels.size() - 1) {
            currentLevelIndex++;
        } else {
            System.out.println("No more levels!");
        }
    }

    public Level getLevel(int index) {
        if (index >= 0 && index < levels.size()) {
            return levels.get(index);
        }
        return null;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setCurrentLevel(int index) {
        if (index >= 0 && index < levels.size()) {
            currentLevelIndex = index;
        }
    }

}
