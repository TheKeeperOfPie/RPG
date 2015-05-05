package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class Player {

    private static final String TAG = Player.class.getCanonicalName();
    private static final int OUT_BOUND_X = 4;
    private static final int OUT_BOUND_Y = 2;
    public static final float HEIGHT_RATIO = 0.9f;
    public static final float WIDTH_RATIO = 0.59999999999f;

    // Ratio of grid unit to move every millisecond
    private static final float SPEED = 0.01f;

    private float[] matrixProjectionAndView = new float[16];
    private float[] transMatrix = new float[16];
    private FloatBuffer uvBuffer;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private short[] indices;
    private int tileSize;
    private PointF pointF;
    private int renderScreenWidth;
    private int renderScreenHeight;
    private long lastFrameTime;
    private float movementX;
    private float movementY;
    private RenderValues renderValues;
    private List<Attack> attacks;
    private List<Integer> attacksToRemove;
    private float offsetX;
    private float offsetY;
    private float velocityX;
    private float velocityY;
    private int lastAnimationFrame;
    private Movement lastDirection;

    public Player(int tileSize) {
        this.tileSize = tileSize;
        pointF = new PointF(OUT_BOUND_X, OUT_BOUND_Y);
        attacks = new ArrayList<>();
        attacksToRemove = new ArrayList<>();

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

        renderValues = new RenderValues(RenderValues.vertexShaderPlayer, RenderValues.fragmentShaderPlayer);

    }

    public void render(int[] textureNames, Renderer renderer, float[] matrixProjection, float[] matrixView) {

        boolean moveY = false;
        boolean moveX = false;

        if (lastFrameTime > 0) {
            long timeDifference = (System.currentTimeMillis() - lastFrameTime);
            float offset = (System.currentTimeMillis() - lastFrameTime) * SPEED;
            offsetY = offset * movementY;
            offsetX = offset * movementX;
            velocityX = offsetX / timeDifference;
            velocityY = offsetY / timeDifference;
            float yCalculated;
            float xCalculated;
            byte[][] walls = renderer.getWalls();

            if (movementY != 0) {

                if (movementY < 0) {
                    yCalculated = pointF.y + offsetY;
                    moveY = yCalculated > OUT_BOUND_Y &&
                            yCalculated < walls[0].length - OUT_BOUND_Y &&
                            renderer.getWalls()[((int) pointF.x)][((int) yCalculated)] != Map.COLLIDE &&
                            renderer.getWalls()[((int) (pointF.x + WIDTH_RATIO - 0.05f))][((int) yCalculated)] != Map.COLLIDE;
                }
                else {
                    yCalculated = pointF.y + offsetY;
                    moveY = yCalculated > OUT_BOUND_Y &&
                            yCalculated < walls[0].length - OUT_BOUND_Y &&
                            renderer.getWalls()[((int) pointF.x)][((int) (yCalculated + HEIGHT_RATIO))] != Map.COLLIDE &&
                            renderer.getWalls()[((int) (pointF.x + WIDTH_RATIO - 0.05f))][((int) (yCalculated + HEIGHT_RATIO))] != Map.COLLIDE;
                }

                if (moveY) {
                    pointF.offset(0, offsetY);

                    if (pointF.y * tileSize > renderer.getOffsetCameraY() + renderScreenHeight - OUT_BOUND_Y * tileSize && offsetY > 0) {
                        renderer.offsetCamera(0, offsetY);
                    }
                    else if (pointF.y * tileSize < renderer.getOffsetCameraY() + (OUT_BOUND_Y - 1) * tileSize && offsetY < 0) {
                        renderer.offsetCamera(0, offsetY);
                    }
                }

            }

            if (movementX != 0) {

                if (movementX < 0) {
                    xCalculated = pointF.x + offsetX;
                    moveX = xCalculated > OUT_BOUND_X &&
                            xCalculated < walls.length - OUT_BOUND_X &&
                            renderer.getWalls()[((int) xCalculated)][((int) pointF.y)] != Map.COLLIDE &&
                            renderer.getWalls()[((int) xCalculated)][((int) (pointF.y + HEIGHT_RATIO - 0.05f))] != Map.COLLIDE;
                }
                else {
                    xCalculated = pointF.x + offsetX;
                    moveX = xCalculated > OUT_BOUND_X &&
                            xCalculated < walls.length - OUT_BOUND_X &&
                            renderer.getWalls()[((int) (xCalculated + WIDTH_RATIO))][((int) pointF.y)] != Map.COLLIDE &&
                            renderer.getWalls()[((int) (xCalculated + WIDTH_RATIO))][((int) (pointF.y + HEIGHT_RATIO - 0.05f))] != Map.COLLIDE;
                }

                if (moveX) {
                    pointF.offset(offsetX, 0);

                    if (pointF.x * tileSize > renderer.getOffsetCameraX() + renderScreenWidth - OUT_BOUND_X * tileSize && offsetX > 0) {
                        renderer.offsetCamera(offsetX, 0);
                    }
                    else if (pointF.x * tileSize < renderer.getOffsetCameraX() + (OUT_BOUND_X - 1) * tileSize && offsetX < 0) {
                        renderer.offsetCamera(offsetX, 0);
                    }
                }

            }
        }
        lastFrameTime = System.currentTimeMillis();

        GLES20.glUseProgram(renderValues.getProgram());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[1]);

        android.opengl.Matrix.setIdentityM(transMatrix, 0);
        android.opengl.Matrix.translateM(transMatrix, 0, pointF.x * tileSize, pointF.y * tileSize,
                                         0f);

        android.opengl.Matrix.multiplyMM(matrixProjectionAndView,
                                         0,
                                         matrixProjection,
                                         0,
                                         transMatrix,
                                         0);

        android.opengl.Matrix.multiplyMM(matrixProjectionAndView,
                                         0,
                                         matrixProjectionAndView,
                                         0,
                                         matrixView,
                                         0);

        GLES20.glUniform1f(renderValues.getRowCount(), 4f);
        GLES20.glUniform1f(renderValues.getColCount(), 4f);
        GLES20.glUniform1f(renderValues.getAlphaHandle(), 1.0f);
        GLES20.glVertexAttribPointer(renderValues.getPositionHandle(), 3,
                                     GLES20.GL_FLOAT, false,
                                     0, vertexBuffer);
        GLES20.glVertexAttribPointer(renderValues.getTexCoordLoc(), 2, GLES20.GL_FLOAT,
                                     false,
                                     0, uvBuffer);
        if (movementX < 0) {
            lastDirection = Movement.LEFT;
            lastAnimationFrame = (int) ((System.currentTimeMillis() / 200) % 3 + 4);
        }
        else if (movementX > 0) {
            lastDirection = Movement.RIGHT;
            lastAnimationFrame = (int) ((System.currentTimeMillis() / 200) % 3 + 8);
        }
        else if (movementY > 0) {
            lastDirection = Movement.UP;
            lastAnimationFrame = (int) ((System.currentTimeMillis() / 200) % 3 + 12);
        }
        else if (movementY < 0) {
            lastDirection = Movement.DOWN;
            lastAnimationFrame = (int) ((System.currentTimeMillis() / 200) % 3);
        }
        GLES20.glUniform1i(renderValues.getSpriteFrameHandle(), lastAnimationFrame);

        GLES20.glUniformMatrix4fv(renderValues.getMatrixHandle(), 1, false, matrixProjectionAndView,
                                  0);

        GLES20.glUniform1i(renderValues.getSamplerLoc(), 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        if (!attacks.isEmpty()) {

            for (int index = 0; index < attacks.size(); index++) {
                Attack attack = attacks.get(index);
                attack.render(textureNames[2], renderer, matrixProjection, matrixView);
                if (attack.isFinished()) {
                    attacksToRemove.add(index);
                }
            }

            for (int index = attacksToRemove.size() - 1; index >= 0; index--) {
                attacks.remove((int) attacksToRemove.remove(index));
            }
        }

    }

    public PointF getLocation() {
        return pointF;
    }

    public void setDimensions(int width, int height) {
        renderScreenWidth = width;
        renderScreenHeight = height;
    }

    public void setMovementX(float movementX) {
        this.movementX = movementX;
    }

    public void setMovementY(float movementY) {
        this.movementY = movementY;
    }

    public RenderValues getRenderValues() {
        return renderValues;
    }

    public void startNewAttack() {

        PointF start = new PointF(pointF.x, pointF.y);
        PointF end = new PointF(pointF.x, pointF.y);
        if (movementX < 0 || lastDirection == Movement.LEFT) {
            start.offset(-1 * WIDTH_RATIO, 0);
            end.offset(velocityX * 500 - 3, 0);
        }
        else if (movementX > 0 || lastDirection == Movement.RIGHT) {
            start.offset(1 * WIDTH_RATIO, 0);
            end.offset(velocityX * 500 + 3, 0);
        }

        if (movementY < 0 || lastDirection == Movement.DOWN) {
            start.offset(0, -1 * HEIGHT_RATIO);
            end.offset(0, velocityY * 500 - 3);
        }
        else if (movementY > 0 || lastDirection == Movement.UP) {
            start.offset(0, 1 * HEIGHT_RATIO);
            end.offset(0, velocityY * 500 + 3);
        }

        attacks.add(new AttackRanged(renderValues, tileSize, 1, 1, 1, start, end, 500));
    }
}
