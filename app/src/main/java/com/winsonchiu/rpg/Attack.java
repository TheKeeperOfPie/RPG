package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.opengl.GLES20;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public abstract class Attack extends Entity {

    private int damage;
    private float range;
    private int accuracy;
    private long time;
    private boolean isHostile;

    public Attack(int damage, float range, int accuracy, float widthRatio, float heightRatio, PointF location, long time, float movementSpeed, boolean isHostile) {
        super(widthRatio, heightRatio, location, 1f, 2f, movementSpeed);
        this.damage = damage;
        this.range = range;
        this.accuracy = accuracy;
        this.time = time;
        this.isHostile = isHostile;
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {
        GLES20.glUniform1i(getDamageLocation(), 0);
        super.render(renderer, matrixProjection, matrixView);
    }

    //region Getters and setters
    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isHostile() {
        return isHostile;
    }

    public void setIsHostile(boolean isHostile) {
        this.isHostile = isHostile;
    }
    //endregion
}
