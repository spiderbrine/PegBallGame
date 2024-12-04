package com.emerson.savedata;

import com.emerson.pegballgame.PegBallStart;
import com.emerson.world.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveData {
    public Map<String, Boolean> levelCompletion = new HashMap<>();
    public Map<String, Integer> highScores = new HashMap<>();

    public SaveData() {

    }

    public SaveData(List<Level> levels) {
        initializeDefaultData(levels);
    }

    private void initializeDefaultData(List<Level> levels) {
        // initialize
        for (int i = 0; i < levels.size(); i++) {
            String levelName = levels.get(i).getLevelName();
            levelCompletion.put(levelName, false);
            highScores.put(levelName, 0);
            System.out.println("Initialized default data " + levelName);
        }
    }
}
