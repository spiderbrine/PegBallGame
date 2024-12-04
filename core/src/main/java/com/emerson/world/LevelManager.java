package com.emerson.world;

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
        levels.add(new Level(GAME, this, "Test 100/1", 100, 1));
        levels.add(new Level(GAME, this, "Test 100/25", 100, 25));
        levels.add(new Level(GAME, this, "Test 10/1", 10, 1));
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
