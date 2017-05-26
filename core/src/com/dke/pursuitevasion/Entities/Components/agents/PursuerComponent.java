package com.dke.pursuitevasion.Entities.Components.agents;

import com.badlogic.ashley.core.Component;
import com.dke.pursuitevasion.Entities.Direction;

/**
 * Created by Nicola Gheza on 23/05/2017.
 */
public class PursuerComponent implements Component {
    public float angularVelocity = 40.0f;
    public float maxAngle = 90.0f;
    public float minAngle = 0.0f;
    public float currentAngle = 0.0f;
    public float waitTimeMaxAngle = 0.5f;
    public float waitTimeMinAngle = 0.5f;
    public float waitTime = 0.0f;
    public Direction direction = Direction.COUNTERCLOCKWISE;
    public boolean patrolStarted = false;
}