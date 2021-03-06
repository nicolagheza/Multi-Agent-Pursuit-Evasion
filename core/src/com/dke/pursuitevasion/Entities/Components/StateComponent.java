package com.dke.pursuitevasion.Entities.Components;


import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

/**
 * StateComponent
 * <p>
 * This component stores the position of the entity, the quaternion (rotation data) and the scale of the entity.
 */
public class StateComponent implements Component {
    //Primary state
    public Vector3 position = new Vector3();                ///< the position of the cube center of mass in world coordinates (meters).
    public Quaternion orientation = new Quaternion();         ///< the orientation of the cube represented by a unit quaternion.
    //Secondary state
    public Vector3 velocity = new Vector3(0.25f,0,0.25f);
    //Constant state
    public Vector3 scale = new Vector3(1, 1, 1);


    public float angle = 0.0f;

    //Model transformation
    public Matrix4 transform = new Matrix4();
    public boolean autoTransformUpdate = true;

    private boolean original = true; //is this the original or the copy
    private StateComponent save = null;

    public StateComponent() {

    }

    private StateComponent(StateComponent o) {
        this.position = o.position.cpy();
        this.orientation = o.orientation.cpy();
        this.velocity = o.velocity.cpy();
        this.scale = o.scale.cpy();
        this.transform = o.transform.cpy();
        this.autoTransformUpdate = o.autoTransformUpdate;
        this.original = false;
    }

    public void save(){
        if(original){
            if(save == null){
                save = new StateComponent(this);
            }else{
                save.set(this);
            }
        }
    }

    public void restore(){
        if(original){
            if(save != null)
                set(save);
        }
    }

    private void set(StateComponent o){
        this.position.set(o.position);
        this.orientation.set(o.orientation);
        this.velocity.set(o.velocity);
        this.scale.set(o.scale);
        this.transform.set(o.transform);
        this.autoTransformUpdate = o.autoTransformUpdate;
    }

    public void update() {
        if (autoTransformUpdate) {
            transform.idt().translate(position);
        }

    }

    public void randomizeVelo(){
        double x = (Math.random() * (50 - 10)) + 10;
        float xVelo = (float) x/100;
        double z = (Math.random() * (50 - 10)) + 10;
        float zVelo = (float) z/100*-1;

        if(velocity.z<0)
            zVelo*=-1;

        Random ran = new Random();
        int n = ran.nextInt(2);
        if(n>1){
            xVelo*=-1;
        }

        velocity = new Vector3(xVelo, 0, zVelo);
    }
}
