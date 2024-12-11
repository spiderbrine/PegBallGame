package com.emerson.gamescreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.emerson.pegballgame.PegBallStart;
import com.emerson.savedata.GameDataManager;
import com.emerson.savedata.SaveData;
import com.emerson.world.Level;
import com.emerson.world.LevelManager;

public class WinLossMenu extends Window {

    private final PegBallStart GAME;
    private final Skin SKIN;
    private boolean highScore = false;

    public WinLossMenu(PegBallStart game, LevelManager levelManager, boolean isVictory, String characterUsed, int score, int shots, float elapsedTime, Skin skin) {
        super(isVictory ? "YOU WIN!" : "Game Over!", skin);
        this.GAME = game;
        this.SKIN = skin;

        this.setModal(true);
        this.setMovable(true);

        Label resultLabel = new Label(isVictory ? "Congratulations!" : "Try again!", skin);
        Label scoreLabel = new Label("Score: " + score, skin);
        Label shotsLabel = new Label("Shots: " + shots, skin);
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        Label timerLabel = new Label(String.format("Time: %02d:%02d", minutes, seconds), skin);

        this.add(resultLabel).pad(10).row();
        if (isVictory) {
            System.out.println("Victory");
            // update save data

            SaveData saveData = GAME.getGameDataManager().loadGameData();
            System.out.println("Load data");
            saveData.levelCompletion.put(GAME.getLevelManager().getCurrentLevel().getLevelName(), true);
            System.out.println("Completion written to save data");
            // if score is higher than the saved high score, update with new score
            if (score > saveData.highScores.get(GAME.getLevelManager().getCurrentLevel().getLevelName())) {
                highScore = true;
                saveData.highScores.put(GAME.getLevelManager().getCurrentLevel().getLevelName(), score);
                System.out.println("Score written to save data");
            }
            GAME.getGameDataManager().saveGameData(saveData);
            System.out.println("Save data saved to file");

            Label characterLabel = new Label("Character Used: " + characterUsed, skin);
            this.add(characterLabel).pad(10).row();
        }
        if (highScore) {
            Label highScoreLabel = new Label("NEW HIGH SCORE!!!", skin);
            this.add(highScoreLabel).pad(10).row();
        }
        this.add(scoreLabel).pad(10).row();
        this.add(shotsLabel).pad(10).row();
        this.add(timerLabel).pad(10).row();

        // buttons
        TextButton retryButton = new TextButton("Retry", skin);
        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                for (Level level : GAME.getLevelManager().getLevels()) {
                    level.reset();
                }
                GAME.setScreen(new GameScreen(GAME, levelManager.getCurrentLevelIndex()));// retry callback
                remove();
            }
        });

        TextButton levelSelectButton = new TextButton("Level Select", skin);
        levelSelectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new LevelSelectScreen(GAME));
                remove();
            }
        });

        // add buttons to table to make it look pretty
        Table buttonTable = new Table();
        buttonTable.add(retryButton).pad(10);
        buttonTable.add(levelSelectButton).pad(10);

        this.add(buttonTable).padTop(20).row();

        // size and pos
        this.pack();
        this.setPosition(Gdx.graphics.getWidth() / 2f - getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f - getHeight() / 2f);
    }
}
