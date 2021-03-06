package com.dke.pursuitevasion.Entities.Systems.agents;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dke.pursuitevasion.AI.Node;
import com.dke.pursuitevasion.AI.PathFinder;
import com.dke.pursuitevasion.AI.PotentialField.PotentialFieldAlgorithm;
import com.dke.pursuitevasion.CellDecompose.Graph.CXPoint;
import com.dke.pursuitevasion.Entities.Components.ObserverComponent;
import com.dke.pursuitevasion.Entities.Components.StateComponent;
import com.dke.pursuitevasion.Entities.Components.agents.EvaderComponent;
import com.dke.pursuitevasion.Entities.Components.agents.PursuerComponent;
import com.dke.pursuitevasion.Entities.Mappers;
import com.dke.pursuitevasion.Entities.Systems.VisionSystem;
import com.dke.pursuitevasion.PolyMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Envy on 5/30/2017.
 */
public class EvaderSystem extends IteratingSystem {
    private static final float DETECTION_TIME = 0.0f;
    private PathFinder pathFinder;
    private ImmutableArray<Entity> pursuers;
    private VisionSystem visionSystem;
    private Vector2 position = new Vector2();
    private Engine engine;
    private PotentialFieldAlgorithm potentialFieldAlgorithm;
    private int heatSize;

    public int mapSize = 250;
    public float gapSize = 0.1f;

    public EvaderSystem(VisionSystem visionSystem, PolyMap map, Engine engine, int heatSize) {
        super(Family.all(EvaderComponent.class).get());
        this.heatSize = heatSize;
        this.engine = engine;
        this.visionSystem = visionSystem;
        pathFinder = new PathFinder(map, mapSize, gapSize);
        this.potentialFieldAlgorithm = new PotentialFieldAlgorithm(engine,pathFinder.getNodeGrid(),map,heatSize);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        EvaderComponent evaderComponent = Mappers.agentMapper.get(entity);
        StateComponent stateComponent = Mappers.stateMapper.get(entity);
        if(evaderComponent.evaderPath == null || evaderComponent.evaderPath.size() == 0){
            evaderComponent.evaderPath = new ArrayList<CXPoint>();
            createPath(evaderComponent, vector3toCXPoint(evaderComponent.position));
        }
        if (!evaderComponent.captured) {
            moveEvaders(evaderComponent, stateComponent);
        }
        moveEvader(entity, deltaTime);
        updateObserver(entity);
        updateDetection(entity, deltaTime);
    }


    private void moveEvader(Entity entity, float deltaTime) {
        EvaderComponent evaderComponent = Mappers.agentMapper.get(entity);
        StateComponent stateComponent = Mappers.stateMapper.get(entity);
        if(evaderComponent.alerted) {
            trackTarget(evaderComponent, stateComponent);
        }
        else {
            moveVision(evaderComponent, stateComponent, deltaTime);
        }
        moveVision(evaderComponent, stateComponent, deltaTime);
        limitAngle(evaderComponent);

    }

    private void limitAngle(EvaderComponent evaderComponent) {
        evaderComponent.currentAngle = MathUtils.clamp(
                evaderComponent.currentAngle,
                evaderComponent.minAngle,
                evaderComponent.maxAngle
        );
    }

    private void trackTarget(EvaderComponent evader, StateComponent state) {
        position.set(evader.targetPosition);
        Vector2 cameraPosition = new Vector2(state.position.x, state.position.z);
        position.sub(cameraPosition);
        position.nor();
        float angle = position.angle();
        evader.currentAngle = angle;
        state.angle = angle;
    }

    private void moveVision(EvaderComponent evaderComponent, StateComponent stateComponent, float deltaTime) {
        if (evaderComponent.waitTime == 0) {
            evaderComponent.currentAngle += evaderComponent.angularVelocity * evaderComponent.direction.value() * deltaTime;
            if (evaderComponent.currentAngle <= evaderComponent.minAngle) {
                evaderComponent.waitTime = evaderComponent.waitTimeMinAngle;
                evaderComponent.direction = evaderComponent.direction.invert();
            }
            else if (evaderComponent.currentAngle >= evaderComponent.maxAngle) {
                evaderComponent.waitTime = evaderComponent.waitTimeMaxAngle;
                evaderComponent.direction = evaderComponent.direction.invert();
            }
        } else {
            evaderComponent.waitTime = Math.max(evaderComponent.waitTime - deltaTime, 0.0f);
        }
        stateComponent.angle = evaderComponent.currentAngle;
    }

    private void updateObserver(Entity entity) {
        EvaderComponent evaderComponent = Mappers.agentMapper.get(entity);
        ObserverComponent observerComponent = Mappers.observerMapper.get(entity);
        observerComponent.angle = evaderComponent.currentAngle;
    }

    private void updateDetection(Entity entity, float deltaTime) {
        EvaderComponent evader = Mappers.agentMapper.get(entity);
        evader.alerted = false;
        pursuers = engine.getEntitiesFor(Family.all(PursuerComponent.class).get());
        for (Entity target : pursuers) {
            updateDetection(entity, target);
            if (evader.alerted)
                break;
        }

        evader.detectionTime = evader.alerted ? evader.detectionTime + deltaTime : 0.0f;

        if (evader.detectionTime > DETECTION_TIME) {
            //System.out.println("PURSUER DETECTED");
        }
    }

    private void updateDetection(Entity entity, Entity target) {
        Vector2 targetPos = Mappers.observableMapper.get(target).position;
        EvaderComponent evader = Mappers.agentMapper.get(entity);
        PursuerComponent pursuer = Mappers.pursuerMapper.get(target);

        evader.alerted = false;
        evader.targetPosition.set(0.0f, 0.0f);

        if(visionSystem.canSee(entity,target)) {
            evader.alerted = true;
            evader.targetPosition.set(targetPos);
            System.out.println("Evader can see: " + pursuer);
        }
    }

    private void createPath(EvaderComponent evader, CXPoint currentPos){

        potentialFieldAlgorithm.updateEvader(evader);
        evader.evaderPath = new ArrayList<CXPoint>();
        evader.evaderPath.add(currentPos);
        evader.evaderPath.addAll(potentialFieldAlgorithm.calculateCXPoints());

    }

    /*private void createPath(EvaderComponent evader, Vector3 lastPosition){
        boolean[][] nodeGrid = pathFinder.getNodeGrid();
        int width = pathFinder.width;
        Random rand = new Random();
        int counter = 0;
        List<Node> p = null;

        while (counter<1){
            int  n = rand.nextInt(width-1) + 1;
            int  m = rand.nextInt(width-1) + 1;
            if(nodeGrid[n][m]){
                CustomPoint endCP = new CustomPoint(n, m);
                Vector3 end = new Vector3(PathFinder.toWorldCoorX(endCP.x), 0, PathFinder.toWorldCoorX(endCP.y));
                p = pathFinder.findPath(lastPosition, end, endCP);
                counter++;
            }
        }
        addAdditionalSteps(evader, p);
    }*/

    private void moveEvaders(EvaderComponent evader, StateComponent stateComponent){
        if(evader.evaderPath!=null && evader.evaderPath.size()>0){
            Vector3 pos = cxPointToVector(evader.evaderPath.remove(0));
            //CXPoint pos = evader.evaderPath.remove(0);
            evader.position = pos;
            stateComponent.position = pos;
            stateComponent.update();
        }
    }

    private Vector3 cxPointToVector(CXPoint cxPoint) {
        Vector3 v3 = new Vector3((float)cxPoint.x, 0, (float)cxPoint.y);
        return v3;
    }

    private CXPoint vector3toCXPoint(Vector3 vector3) {
        CXPoint cxPoint = new CXPoint((double)vector3.x,(double)vector3.z);
        return cxPoint;
    }

    public static ArrayList<CXPoint> addAdditionalSteps(EvaderComponent eC, List<Node> p){
        //eC.evaderPath = new ArrayList<Vector3>();
        eC.evaderPath = new ArrayList<CXPoint>();
        float stepSize = 10;
        float diagStepSize = (float) Math.floor(1.4*stepSize);
        if(p.size()>1) {
            for (int i = 0; i < p.size() - 1; i++) {
                Vector3 start = new Vector3(p.get(i).worldX, p.get(i).worldY, p.get(i).worldZ);
                Vector3 end = new Vector3(p.get(i + 1).worldX, p.get(i + 1).worldY, p.get(i + 1).worldZ);
                float adjX = end.x - start.x;
                float adjZ = end.z - start.z;
                if(adjX!=0 && adjZ!=0){
                    for (float j = 1; j < diagStepSize; j++) {
                        double scale = j / diagStepSize;
                        BigDecimal sc = BigDecimal.valueOf(scale);
                        BigDecimal xX = BigDecimal.valueOf(adjX);
                        BigDecimal zZ = BigDecimal.valueOf(adjZ);
                        BigDecimal newX = sc.multiply(xX);
                        BigDecimal newZ = sc.multiply(zZ);
                        float bDX = newX.floatValue();
                        float bDZ = newZ.floatValue();
                        Vector3 position = new Vector3(start.x + bDX, 0, start.z + bDZ);
                        CXPoint positionCX = new CXPoint(position.x,position.z);
                        eC.evaderPath.add(positionCX);
                    }
                }else {
                    for (float j = 1; j < stepSize; j++) {
                        double scale = j / stepSize;
                        BigDecimal sc = BigDecimal.valueOf(scale);
                        BigDecimal xX = BigDecimal.valueOf(adjX);
                        BigDecimal zZ = BigDecimal.valueOf(adjZ);
                        BigDecimal newX = sc.multiply(xX);
                        BigDecimal newZ = sc.multiply(zZ);
                        float bDX = newX.floatValue();
                        float bDZ = newZ.floatValue();
                        Vector3 position = new Vector3(start.x + bDX, 0, start.z + bDZ);
                        CXPoint positionCX = new CXPoint(position.x,position.z);
                        eC.evaderPath.add(positionCX);
                    }
                }
            }
        }
        return eC.evaderPath;
    }
}
