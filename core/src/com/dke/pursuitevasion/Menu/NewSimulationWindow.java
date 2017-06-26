package com.dke.pursuitevasion.Menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.dke.pursuitevasion.PolyMap;
import com.dke.pursuitevasion.PursuitEvasion;
import com.dke.pursuitevasion.Simulator.SimulatorScreen;

/**
 * Created by Nicola Gheza on 27/02/2017.
 */
public class NewSimulationWindow extends Window {

    private PursuitEvasion game;
    private final MenuScreen menuScreen;
    private Skin skin;
    private FileHandle file;
    private boolean mapSelected, fromSimScreen;
    PolyMap map;

    public NewSimulationWindow(Skin skin, PursuitEvasion game, MenuScreen screen, PolyMap Map) {
        super("New Simulation", skin);
        this.game = game;
        this.setModal(false);
        this.skin = skin;
        this.setSize(400, 325);
        this.menuScreen = screen;
        this.setMovable(false);
        if(Map!=null){
            map = Map;
            fromSimScreen = true;
        }
        initGUI();
    }

    private void initGUI() {
        int buttonWidth = 120, buttonHeight = 100;
        int padding = 30;

        Table table=new Table();
        table.setSize(800, 480);

        TextButton selectMapButton = new TextButton("Select map", skin);
        //selectMapButton.setOrigin(140,50);
        selectMapButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showFileChooser();
            }
        });

        CheckBox graphCheckBox = new CheckBox("Graph Searcher", skin);
        graphCheckBox.setName("GRAPHSEARCHER");
        graphCheckBox.setChecked(true);
        CheckBox coordinateExplorer = new CheckBox("Coordinate Explorer", skin);
        coordinateExplorer.setName("COORDINATEEXPLORER");
        final ButtonGroup aiGroup = new ButtonGroup(graphCheckBox, coordinateExplorer);
        aiGroup.setMaxCheckCount(1);
        aiGroup.setMinCheckCount(0);
        aiGroup.setUncheckLast(true);

        final Label heatSizeInfo = new Label("Set PF size",skin);
        final TextField heatSize = new TextField("19",skin);

        TextButton startButton = new TextButton("Start", skin);

        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println(aiGroup.getChecked().getName());
                if(file!= null) {
                    game.setScreen(new SimulatorScreen(game, file, null, aiGroup.getChecked().getName(), Integer.parseInt(heatSize.getText())));
                    remove();
                }
                if(map!=null){
                    game.setScreen(new SimulatorScreen(game, null, map, aiGroup.getChecked().getName(), Integer.parseInt(heatSize.getText())));
                    remove();
                }
            }
        });
        //selectMapButton.setPosition(140,150);


        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                remove();
            }
        });
        //selectMapButton.setPosition(140,250);

        if(!fromSimScreen) {
            table.add(selectMapButton).width(150);
            table.row();
        }
        table.add(graphCheckBox).width(150);
        table.row();
        table.add(coordinateExplorer).width(150);
        table.row();
        table.add(heatSizeInfo);
        table.row();
        table.add(heatSize);
        table.row();
        table.add(startButton).width(150);
        table.row();
        table.add(backButton).width(150);
        table.row();
        add(table);
    }

    private void showFileChooser() {
        menuScreen.chooseFile();
    }

    public void setFile(FileHandle file) {
        this.file = file;
    }

    public void setMapSelected() {
        mapSelected = true;
    }
}
