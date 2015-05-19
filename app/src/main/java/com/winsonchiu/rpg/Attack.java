package com.winsonchiu.rpg;

import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public abstract class Attack extends Entity {

    public static final float HEIGHT_RATIO = 0.9f;
    public static final float WIDTH_RATIO = 0.59999999999f;
    protected float[] matrixProjectionAndView = new float[16];
    protected float[] transMatrix = new float[16];
    protected FloatBuffer uvBuffer;
    protected FloatBuffer vertexBuffer;
    protected ShortBuffer drawListBuffer;
    protected short[] indices;

    protected int tileSize;
    protected int damage;
    protected int range;
    protected int accuracy;
    protected PointF startLocation;
    protected PointF endLocation;
    protected long startTime;
    protected long endTime;
    protected long time;

    public Attack(int tileSize, int damage, int range, int accuracy, PointF startLocation, PointF endLocation, long time, float movementSpeed) {
        super(0, 0, tileSize, WIDTH_RATIO, HEIGHT_RATIO, new PointF(startLocation.x, startLocation.y), 1f, 1f,
              movementSpeed);
        this.tileSize = tileSize;
        this.damage = damage;
        this.range = range;
        this.accuracy = accuracy;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.time = time;
    }

    public int calculateDamage() {
        return damage;
    }

}
