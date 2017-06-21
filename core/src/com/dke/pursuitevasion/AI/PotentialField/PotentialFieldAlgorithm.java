package com.dke.pursuitevasion.AI.PotentialField;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.math.Vector3;
import com.dke.pursuitevasion.CXSearchingAlgorithm.CXAgentUtility;
import com.dke.pursuitevasion.CellDecompose.Graph.CXPoint;
import com.dke.pursuitevasion.Entities.Components.agents.EvaderComponent;
import com.dke.pursuitevasion.PolyMap;

import java.util.ArrayList;

/**
 * Created by imabeast on 6/19/17.
 */

public class PotentialFieldAlgorithm {

    private Engine engine;
    private boolean[][] bMap;
    private int[][] obstacleMap;
    //private Entity evader;
    EvaderComponent evader;
    private Vector3 nextMove;
    PolyMap map;

    public PotentialFieldAlgorithm(Engine e, boolean[][] b, PolyMap map){
        this.engine = e;
        this.bMap = b;
        this.map = map;
        generateInitialPFMap();
    }

    public void generateInitialPFMap(){

        int[][] map = new int[100][100];

        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (bMap[i][j] == false)
                    map[i][j] = 1000; // make sure this value is greater than the greatest value within any potential field
            }
        }

        this.obstacleMap = map;
    }

    public void updateEvader(EvaderComponent evader) {
        this.evader = evader;
    }

    private void computePositionToMoveTo(){

        PFMap pfMap = new PFMap(obstacleMap, engine, map, evader);
        pfMap.updateMap();

        Vector3 v = pfMap.getNextMove();
        this.nextMove = v;

    }

    public Vector3 computeNextVector() {

        PFMap pfMap = new PFMap(obstacleMap, engine, map, evader);
        pfMap.updateMap();

        Vector3 v = pfMap.getNextMove();
        this.nextMove = v;

        return v;
    }

    private CXPoint vectorToCXPoint(Vector3 v3) {
        CXPoint cxPoint = new CXPoint(v3.x,v3.z);
        return cxPoint;
    }

    public ArrayList<CXPoint> calculateCXPoints() {

        //EvaderComponent evaderComponent = Mappers.agentMapper.get(evader);

        //Vector3 v3 = evader.position;
        CXPoint currentPos = vectorToCXPoint(evader.position);
        computePositionToMoveTo();
        CXPoint nextPos = new CXPoint(nextMove.x, nextMove.z);

        CXAgentUtility cxAgentUtility = new CXAgentUtility();
        ArrayList<CXPoint> cxPoints = cxAgentUtility.discretizePath(currentPos, nextPos);

        return cxPoints;
    }
}
