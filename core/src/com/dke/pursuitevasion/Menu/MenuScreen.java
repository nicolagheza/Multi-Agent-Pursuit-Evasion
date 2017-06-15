package com.dke.pursuitevasion.Menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.dke.pursuitevasion.Editor.MapEditorScreen;
import com.dke.pursuitevasion.PursuitEvasion;
import com.dke.pursuitevasion.UI.FileChooser;

/**
 * Created by jeeza on 10-2-17.
 */
public class MenuScreen implements Screen {
    private final PursuitEvasion game;
    private NewSimulationWindow newSimulationWindow;
    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;

    public MenuScreen(PursuitEvasion game) {
        this.game = game;
        batch = new SpriteBatch();
        stage = new Stage();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        initGUI();
    }

    private void initGUI() {
        Table table = new Table();
        table.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Label project = new Label("Multi-Agent Pursuit Evasion", skin);

        TextButton buttonMapBuilder = new TextButton("Map Editor", skin);
        //buttonMapBuilder.setPosition(550, 350);
        //buttonMapBuilder.setSize(200, 50);
        buttonMapBuilder.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                newMapBuilder();
            }
        });

        TextButton buttonStartSim = new TextButton("Simulator", skin);
        //buttonStartSim.setPosition(550, 450);
        //buttonStartSim.setSize(200, 50);
        buttonStartSim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                newSimWindow();
            }
        });

        table.add(project);
        table.row();
        table.add(buttonMapBuilder).size(200,50);
        table.row();
        table.add(buttonStartSim).size(200,50);

        //table.setOrigin(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/6);

        stage.addActor(table);
    }

    private void newMapBuilder() {
        game.setScreen(new MapEditorScreen(game));
    }

    private void newSimWindow() {
        newSimulationWindow = new NewSimulationWindow(skin, game, this);
        stage.addActor(newSimulationWindow);
        newSimulationWindow.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 1);
    }


    public void chooseFile() {
        FileChooser files = new FileChooser("Select Course File", skin) {
            @Override
            protected void result(Object object) {
                if (object.equals("OK")) {
                    FileHandle file = getFile();
                    newSimulationWindow.setFile(file);
                    newSimulationWindow.setMapSelected();
                }
            }
        };
        files.setDirectory(Gdx.files.local("maps/"));
        files.show(stage);
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }
}
