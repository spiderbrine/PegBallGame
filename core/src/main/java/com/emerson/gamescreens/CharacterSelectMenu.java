package com.emerson.gamescreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.emerson.gamescreens.GameScreen.VIRTUAL_HEIGHT;
import static com.emerson.gamescreens.GameScreen.VIRTUAL_WIDTH;

public class CharacterSelectMenu {
    private Stage stage;
    private Window characterSelectWindow;
    private Table characterSelectTable;
    private Image characterImage;
    private Label characterDescription;
    private final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

    public boolean characterSelected = false;

    private OrthographicCamera camera;
    private Viewport viewport;

    public CharacterSelectMenu(Stage stage) {
        this.stage = stage;

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        characterSelectWindow = new Window("Select Character", skin);

        // table for selection
        characterSelectTable = new Table();
        characterSelectTable.setFillParent(true);

        // make buttons for each character
        Table leftGroup = new Table();
        leftGroup.setSize(200, 600);
        characterSelectTable.add(leftGroup).size(200, 600).pad(10).left();

        String[] characters = {"Whirly-Ball", "Bouncy-Ball", "Electro-Ball", "Sludge-Ball"};

        for (String charName : characters) {
            TextButton charButton = new TextButton(charName, skin);

            // hover listener to show image and description
            charButton.addListener(new InputListener() {
                @Override
                public boolean mouseMoved(InputEvent event, float x, float y) {
                    updateCharacterInfo(charName); // update image and description
                    return true;
                }
            });
            charButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    characterSelected = true;
                }
            });

            leftGroup.add(charButton).size(180, 50).pad(10).row(); // fixed size hopefully
        }

        Table rightGroup = new Table();
        rightGroup.setSize(200, 600);
        characterSelectTable.add(rightGroup).size(200, 600).pad(10).right();

        // image and description label
        characterImage = new Image(); // empty for now, gets updated based on hover
        characterImage.setScaling(Scaling.fit);
        characterDescription = new Label("", skin); // empty for now, gets updated on hover
        characterDescription.setWrap(true);
        characterDescription.setAlignment(Align.topLeft);

        rightGroup.add(characterImage).size(200, 300).pad(10).top().center().row();
        rightGroup.add(characterDescription).size(200, 300).pad(10).bottom().center();

        // add table to window
        characterSelectWindow.setSize(500, 700);
        characterSelectWindow.setPosition((stage.getWidth() / 2) - (characterSelectWindow.getWidth() / 2),
            (stage.getHeight() / 2) - (characterSelectWindow.getHeight() / 2));
        characterSelectWindow.addActor(characterSelectTable);

        // add window to stage
        stage.addActor(characterSelectWindow);
        characterSelectWindow.setVisible(false);
    }

    private void updateCharacterInfo(String characterName) {
        // update info window based on character the cursor is hovered over
        if (characterName.equals("Whirly-Ball")) {
            characterImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("whirlyBall.png"))));
            characterDescription.setText("When activated: Next turn, the ball shoots through pegs in a swirly wind pattern.");
        } else if (characterName.equals("Bouncy-Ball")) {
            characterImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("bouncyBall.png"))));
            characterDescription.setText("When activated: For the rest of the current turn and next turn, " +
                "the ball becomes bouncier and places a bouncy floor over the bottom of the screen. " +
                "The ball can bounce off of the floor 1 time and then the floor opens back up to normal.");
        } else if (characterName.equals("Electro-Ball")) {
            characterImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("electroBall.png"))));
            characterDescription.setText("When activated: For the rest of the current turn and next turn, " +
                "the ball becomes electric and each peg hit will arc electricity to 2 nearby pegs.");
        } else if (characterName.equals("Sludge-Ball")) {
            characterImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("sludgeBall.png"))));
            characterDescription.setText("When activated: For the rest of the current turn and the next turn, " +
                "the ball becomes covered in corrosive sludge. Each peg hit will decay and drip sludge to the pegs below.");
        }
    }

    public void display(){
        characterSelectWindow.setVisible(true);
        System.out.println("Display character select menu window");
    }

    public void remove(){
        characterSelectWindow.setVisible(false);
        System.out.println("Remove character select menu window");
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
        stage.getViewport().update(width, height);
    }

    public void render(float delta){
        stage.act(delta);
        stage.draw();
        //stage.setDebugAll(true);
    }

}
