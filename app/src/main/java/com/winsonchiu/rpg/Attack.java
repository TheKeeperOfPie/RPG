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

    public Attack(int texture, int tileSize, int damage, int range, int accuracy, PointF startLocation, PointF endLocation, long time, float movementSpeed) {
        super(tileSize, WIDTH_RATIO, HEIGHT_RATIO, new PointF(startLocation.x, startLocation.y), texture, 1f, 1f,
              movementSpeed);
        this.tileSize = tileSize;
        this.damage = damage;
        this.range = range;
        this.accuracy = accuracy;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.time = time;

        float[] uvs = new float[]{
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
        };

        ByteBuffer uvByteBuffer = ByteBuffer.allocateDirect(uvs.length * 4);
        uvByteBuffer.order(ByteOrder.nativeOrder());
        uvBuffer = uvByteBuffer.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        float[] vertices = new float[]{
                0.0f, tileSize * HEIGHT_RATIO, -5f,
                0.0f, 0.0f, -5f,
                tileSize * WIDTH_RATIO, 0.0f, -5f,
                tileSize * WIDTH_RATIO, tileSize * HEIGHT_RATIO, -5f
        };

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        indices = new short[]{0, 1, 2, 0, 2, 3};

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }
}
