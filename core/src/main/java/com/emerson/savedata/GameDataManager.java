package com.emerson.savedata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.emerson.pegballgame.PegBallStart;

public class GameDataManager {
    private static final String SAVE_FILE = "saveData.json"; // name of file
    private final Json json; // LibGDX JSON object
    private final PegBallStart GAME;

    public GameDataManager(PegBallStart game) {
        this.GAME = game;
        this.json = new Json();
    }

    public void saveGameData(SaveData saveData) {
        try {
            System.out.println("Starting save...");
            String saveDataString = json.prettyPrint(saveData); // Use class-level json
            System.out.println("save data string " + saveDataString);
            FileHandle file = Gdx.files.local(SAVE_FILE);
            System.out.println("File exists " + file.exists());
            file.writeString(saveDataString, false);
            System.out.println("Game data saved successfully.");
        } catch (Exception e) {
            System.err.println("Failed to save game data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public SaveData loadGameData() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);

            if (!file.exists()) {
                System.out.println("Save file not found. Returning new SaveData.");
                System.out.println("Load file path: " + file.file().getAbsolutePath());
                return new SaveData(GAME.getLevelManager().getLevels());
            }

            System.out.println("Save file found. Reading data...");
            SaveData saveData = json.fromJson(SaveData.class, file.readString());
            System.out.println("Data loaded:\n" + json.prettyPrint(saveData));
            return saveData;
        } catch (Exception e) {
            System.err.println("Failed to load game data: " + e.getMessage());
            return new SaveData(GAME.getLevelManager().getLevels());
        }
    }
}
