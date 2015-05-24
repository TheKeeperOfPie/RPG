package com.winsonchiu.rpg;

import android.graphics.PointF;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public abstract class Attack extends Entity {

    public static final float HEIGHT_RATIO = 1f;
    public static final float WIDTH_RATIO = 1f;
    protected float[] matrixProjectionAndView = new float[16];
    protected float[] transMatrix = new float[16];
    protected FloatBuffer uvBuffer;
    protected FloatBuffer vertexBuffer;
    protected ShortBuffer drawListBuffer;
    protected short[] indices;

    protected int tileSize;
    protected int damage;
    protected float range;
    protected int accuracy;
    protected long time;
    protected boolean isHostile;

    public Attack(int tileSize, int damage, float range, int accuracy, float widthRatio, float heightRatio, PointF location, long time, float movementSpeed, boolean isHostile) {
        super(0, 0, damage, tileSize, widthRatio, heightRatio, location, 1f, 2f, movementSpeed);
        this.tileSize = tileSize;
        this.damage = damage;
        this.range = range;
        this.accuracy = accuracy;
        this.time = time;
        this.isHostile = isHostile;
    }

    public int calculateDamage() {
        return damage;
    }

}
