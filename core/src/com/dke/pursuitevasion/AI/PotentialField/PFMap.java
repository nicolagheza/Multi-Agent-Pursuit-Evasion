package com.dke.pursuitevasion.AI.PotentialField;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dke.pursuitevasion.AI.CustomPoint;
import com.dke.pursuitevasion.AI.PathFinder;
import com.dke.pursuitevasion.Entities.Components.StateComponent;
import com.dke.pursuitevasion.Entities.Components.agents.EvaderComponent;
import com.dke.pursuitevasion.Entities.Components.agents.PursuerComponent;
import com.dke.pursuitevasion.Entities.Mappers;
import com.dke.pursuitevasion.PolyMap;

import java.util.ArrayList;

/**
 * Created by imabeast on 6/19/17.
 */

public class PFMap {

    private int[][] obstacleMap;
    private int[][] heatMap;
    private Engine engine;
    private Entity evader;
    private PathFinder pathFinder;
    private ImmutableArray<Entity> entities;

    public PFMap(int[][] o, Engine e, PolyMap map, Entity evader){
        this.obstacleMap = o;
        this.engine = e;
        this.evader = evader;
        this.entities = engine.getEntitiesFor(Family.all(StateComponent.class).get());
        this.pathFinder = new PathFinder(map);
    }

    public Vector3 getNextMove() {

        EvaderComponent evaderComponent = Mappers.agentMapper.get(evader);
        Vector3 v3 = evaderComponent.position;
        Vector2 v2 = coordsToArray(v3);

        Triplet triplet = findBestMove((int)v2.x, (int)v2.y);

        return new Vector3(arrayToCoords(new Vector2(triplet.getX(),triplet.getY())));
    }

    private Triplet findBestMove(int x, int y) {

        int currentValue = heatMap[x][y];
        System.out.println("Current value = " + currentValue);
        int bestValue = currentValue;
        Triplet coords = new Triplet(x,y);

        // implement corner checks

        // check top
        try {
            if (heatMap[x-1][y] < bestValue){
                bestValue = heatMap[x-1][y];
                coords.setAll(x-1,y,bestValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // check top right
        try {
            if (heatMap[x-1][y+1] < bestValue){
                bestValue = heatMap[x-1][y+1];
                coords.setAll(x-1,y+1,bestValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // check right
        try {
            if (heatMap[x][y+1] < bestValue){
                bestValue = heatMap[x][y+1];
                coords.setAll(x,y+1,bestValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // check bottom right
        try {
            if (heatMap[x+1][y+1] < bestValue){
                bestValue = heatMap[x+1][y+1];
                coords.setAll(x+1,y+1,bestValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // check bottom
        try {
            if (heatMap[x+1][y] < bestValue){
                bestValue = heatMap[x+1][y];
                coords.setAll(x+1,y,bestValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // check bottom left
        try {
            if (heatMap[x+1][y-1] < bestValue){
                bestValue = heatMap[x+1][y-1];
                coords.setAll(x+1,y-1,bestValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // check left
        try {
            if (heatMap[x][y-1] < bestValue){
                bestValue = heatMap[x][y-1];
                coords.setAll(x,y-1,bestValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // check top left
        try {
            if (heatMap[x-1][y-1] < bestValue){
                bestValue = heatMap[x-1][y-1];
                coords.setAll(x-1,y-1,bestValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        return coords;
    }

    public void updateMap(){

        heatMap = obstacleMap; // reset the heat map upon every iteration

        // find positions of pursuers first, then convert them to array coordinates
        ArrayList<Vector2> pursuerPositions = new ArrayList<Vector2>();
        Vector3 tempVector = new Vector3();

        for (Entity e : entities) {
            if (Mappers.pursuerMapper.has(e)) {
                PursuerComponent pursuerComponent = Mappers.pursuerMapper.get(e);
                tempVector = pursuerComponent.position;
            }
            pursuerPositions.add(coordsToArray(tempVector));
        }

        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (pursuerPositions.contains(new Vector2((float)i, (float)j)))
                    generatePotentialField(i,j,10);
            }
        }
    }

    private Vector2 coordsToArray(Vector3 vector3){

        Vector2 botCoords = new Vector2();

        float x = vector3.x;
        float y = vector3.z;

        CustomPoint customPoint = PathFinder.getNodeFromWorldCoor(x,y);
        if (customPoint == null)
            customPoint = pathFinder.approximatePosition(x,y);

        botCoords.set(customPoint.x, customPoint.y);

        return botCoords;
    }

    private Vector3 arrayToCoords(Vector2 vector2){
        return PathFinder.positionFromIndex((int)vector2.x, (int)vector2.y, pathFinder.pF);
    }

    private void generatePotentialField(int x, int y, int startValue){

        for (int i = 0; i < startValue; i++) {
            generateColumn(i,x,y,startValue); // going left
            generateColumn(-i,x,y,startValue); // going right
        }

    }

    private void generateColumn(int step, int centerX, int centerY, int startValue) {

        int tempCX = centerX;
        int tempCY = centerY - step;
        int var = startValue - Math.abs(step);

        for (int i = 0; i < var; i++) {
            try {
                if (heatMap[tempCX-i][tempCY] != 1000 && heatMap[tempCX-i][tempCY] < var-i)
                    heatMap[tempCX-i][tempCY] = var-i; } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                if (heatMap[tempCX+i][tempCY] != 1000 && heatMap[tempCX+i][tempCY] < var-i)
                    heatMap[tempCX+i][tempCY] = var-i; } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
    }
}