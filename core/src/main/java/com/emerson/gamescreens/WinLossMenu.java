package com.emerson.gamescreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.emerson.pegballgame.PegBallStart;

public class WinLossMenu extends Window {

    private final PegBallStart GAME;
    private final Skin SKIN;

    public WinLossMenu(PegBallStart game, boolean isVictory, String characterUsed, int score, int shots, Skin skin) {
        super(isVictory ? "YOU WIN!" : "Game Over!", skin);
        this.GAME = game;
        this.SKIN = skin;

        this.setModal(true);
        this.setMovable(true);

        Label resultLabel = new Label(isVictory ? "Congratulations!" : "Try again!", skin);
        Label scoreLabel = new Label("Score: " + score, skin);
        Label shotsLabel = new Label("Shots: " + shots, skin);

        this.add(resultLabel).pad(10).row();
        if (isVictory) {
            Label characterLabel = new Label("Character Used: " + characterUsed, skin);
            this.add(characterLabel).pad(10).row();
        }
        this.add(scoreLabel).pad(10).row();
        this.add(shotsLabel).pad(10).row();

        // buttons
        TextButton retryButton = new TextButton("Retry", skin);
        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GAME.setScreen(new GameScreen(GAME));// retry callback
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
