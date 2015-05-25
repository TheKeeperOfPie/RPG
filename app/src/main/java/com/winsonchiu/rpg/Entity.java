package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.utils.RenderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/5/2015.
 */
public abstract class Entity {

    public static final String VERTEX_SHADER =
            "uniform mat4 matrix;" +
            "attribute vec4 positionCoordinate;" +
            "attribute vec2 textureCoordinateVertex;" +
            "uniform float rowCount;" +
            "uniform float colCount;" +
            "uniform int animationFrame;" +
            "varying vec2 textureCoordinateFragment;" +
            "void main() {" +
            "    gl_Position = matrix * positionCoordinate;" +
            "    textureCoordinateFragment = textureCoordinateVertex;" +
            "    textureCoordinateFragment.x = (textureCoordinateVertex.x + float(mod(float(animationFrame), colCount))) / colCount;" +
            "    textureCoordinateFragment.y = (textureCoordinateVertex.y + float(animationFrame / int(colCount))) / rowCount;" +
            "}";

    public static final String FRAGMENT_SHADER =
            "varying vec2 textureCoordinateFragment;" +
            "uniform float opacity;" +
            "uniform sampler2D texture;" +
            "uniform int damage;" +
            "void main() {" +
            "    gl_FragColor = texture2D(texture, textureCoordinateFragment);" +
            "    gl_FragColor.a *= opacity;" +
            "    if (damage > 0) {" +
            "        gl_FragColor.r = gl_FragColor.r * 0.5 + 0.5;" +
            "    }" +
            "}";
    private static final String TAG = Entity.class.getCanonicalName();

    private static int programId;
    private static int samplerLocation;
    private static int positionLocation;
    private static int textureLocation;
    private static int matrixLocation;
    private static int alphaLocation;
    private static int animationFrameLocation;
    private static int rowCountLocation;
    private static int colCountLocation;
    private static int damageLocation;

    private float[] matrixProjectionAndView = new float[16];
    private float[] transMatrix = new float[16];
    private FloatBuffer uvBuffer;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private short[] indices;
    private PointF location;
    private long lastFrameTime;
    private float movementX;
    private float movementY;
    private float offsetX;
    private float offsetY;
    private float velocityX;
    private float velocityY;
    private int lastAnimationFrame;
    private Direction lastDirection;
    private float textureRowCount;
    private float textureColCount;
    private float widthRatio;
    private float heightRatio;
    private float angle;
    private boolean toDestroy;

    private float movementSpeed;

    public Entity(float widthRatio,
            float heightRatio,
            PointF location,
            float textureRowCount,
            float textureColCount,
            float movementSpeed) {
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        this.location = location;
        this.textureRowCount = textureRowCount;
        this.textureColCount = textureColCount;
        this.movementSpeed = movementSpeed;
        this.lastDirection = Direction.SOUTH;
        setupBuffers();

    }

    //region Rendering
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        setLastFrameTime(System.currentTimeMillis());

        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glEnableVertexAttribArray(textureLocation);

        android.opengl.Matrix.setIdentityM(getTransMatrix(), 0);

        android.opengl.Matrix.translateM(transMatrix,
                0,
                (getLocation().x - renderer.getOffsetCameraX() + getWidthRatio() / 2f),
                (getLocation().y - renderer.getOffsetCameraY() + getHeightRatio() / 2f),
                0);
        android.opengl.Matrix.rotateM(transMatrix, 0, angle, 0.0f, 0.0f, 1.0f);
        android.opengl.Matrix.translateM(transMatrix,
                0,
                -(getLocation().x - renderer.getOffsetCameraX() + getWidthRatio() / 2f),
                -(getLocation().y - renderer.getOffsetCameraY() + getHeightRatio() / 2f),
                0);

        android.opengl.Matrix.translateM(getTransMatrix(), 0, getLocation().x,
                getLocation().y,
                0f);

        android.opengl.Matrix.multiplyMM(getMatrixProjectionAndView(),
                0,
                matrixProjection,
                0,
                getTransMatrix(),
                0);

        android.opengl.Matrix.multiplyMM(getMatrixProjectionAndView(),
                0,
                getMatrixProjectionAndView(),
                0,
                matrixView,
                0);

        GLES20.glUniform1f(rowCountLocation, textureRowCount);
        GLES20.glUniform1f(colCountLocation, textureColCount);
        GLES20.glUniform1f(alphaLocation, 1.0f);

        GLES20.glVertexAttribPointer(positionLocation, 3,
                GLES20.GL_FLOAT, false,
                0, getVertexBuffer());
        GLES20.glVertexAttribPointer(textureLocation, 2, GLES20.GL_FLOAT,
                false,
                0, getUvBuffer());


        GLES20.glUniform1i(animationFrameLocation, getLastAnimationFrame());

        GLES20.glUniformMatrix4fv(matrixLocation, 1, false, getMatrixProjectionAndView(),
                0);

        GLES20.glUniform1i(samplerLocation, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, getIndices().length,
                GLES20.GL_UNSIGNED_SHORT, getDrawListBuffer());

        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glDisableVertexAttribArray(textureLocation);


    }
    //endregion

    //region Getters and Setters

    public PointF getNewCenterLocation() {
        return new PointF(getLocation().x + getWidthRatio() / 2, getLocation().y + getHeightRatio() / 2);
    }

    public RectF getBounds() {
        return new RectF(getLocation().x, getLocation().y, getLocation().x + widthRatio, getLocation().y + heightRatio);
    }

    public void setToDestroy(boolean toDestroy) {
        this.toDestroy = toDestroy;
    }

    public boolean getToDestroy() {
        return toDestroy;
    }

    public float[] getMatrixProjectionAndView() {
        return matrixProjectionAndView;
    }

    public void setMatrixProjectionAndView(float[] matrixProjectionAndView) {
        this.matrixProjectionAndView = matrixProjectionAndView;
    }

    public float[] getTransMatrix() {
        return transMatrix;
    }

    public void setTransMatrix(float[] transMatrix) {
        this.transMatrix = transMatrix;
    }

    public FloatBuffer getUvBuffer() {
        return uvBuffer;
    }

    public void setUvBuffer(FloatBuffer uvBuffer) {
        this.uvBuffer = uvBuffer;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(FloatBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public ShortBuffer getDrawListBuffer() {
        return drawListBuffer;
    }

    public void setDrawListBuffer(ShortBuffer drawListBuffer) {
        this.drawListBuffer = drawListBuffer;
    }

    public short[] getIndices() {
        return indices;
    }

    public void setIndices(short[] indices) {
        this.indices = indices;
    }

    public PointF getLocation() {
        return location;
    }

    public void setLocation(PointF location) {
        this.location = location;
    }

    public void setLocation(float x, float y) {
        location.set(x, y);
    }

    public long getLastFrameTime() {
        return lastFrameTime;
    }

    public void setLastFrameTime(long lastFrameTime) {
        this.lastFrameTime = lastFrameTime;
    }

    public float getMovementX() {
        return movementX;
    }

    public void setMovementX(float movementX) {
        this.movementX = movementX;
    }

    public float getMovementY() {
        return movementY;
    }

    public void setMovementY(float movementY) {
        this.movementY = movementY;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public int getLastAnimationFrame() {
        return lastAnimationFrame;
    }

    public void setLastAnimationFrame(int lastAnimationFrame) {
        this.lastAnimationFrame = lastAnimationFrame;
    }

    public Direction getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(Direction lastDirection) {
        this.lastDirection = lastDirection;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public float getWidthRatio() {
        return widthRatio;
    }

    public void setWidthRatio(float widthRatio) {
        this.widthRatio = widthRatio;
    }

    public float getHeightRatio() {
        return heightRatio;
    }

    public void setHeightRatio(float heightRatio) {
        this.heightRatio = heightRatio;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public static int getProgramId() {
        return programId;
    }

    public static void setProgramId(int programId) {
        Entity.programId = programId;
    }

    public static int getSamplerLocation() {
        return samplerLocation;
    }

    public static void setSamplerLocation(int samplerLocation) {
        Entity.samplerLocation = samplerLocation;
    }

    public static int getPositionLocation() {
        return positionLocation;
    }

    public static void setPositionLocation(int positionLocation) {
        Entity.positionLocation = positionLocation;
    }

    public static int getTextureLocation() {
        return textureLocation;
    }

    public static void setTextureLocation(int textureLocation) {
        Entity.textureLocation = textureLocation;
    }

    public static int getMatrixLocation() {
        return matrixLocation;
    }

    public static void setMatrixLocation(int matrixLocation) {
        Entity.matrixLocation = matrixLocation;
    }

    public static int getAlphaLocation() {
        return alphaLocation;
    }

    public static void setAlphaLocation(int alphaLocation) {
        Entity.alphaLocation = alphaLocation;
    }

    public static int getAnimationFrameLocation() {
        return animationFrameLocation;
    }

    public static void setAnimationFrameLocation(int animationFrameLocation) {
        Entity.animationFrameLocation = animationFrameLocation;
    }

    public static int getRowCountLocation() {
        return rowCountLocation;
    }

    public static void setRowCountLocation(int rowCountLocation) {
        Entity.rowCountLocation = rowCountLocation;
    }

    public static int getColCountLocation() {
        return colCountLocation;
    }

    public static void setColCountLocation(int colCountLocation) {
        Entity.colCountLocation = colCountLocation;
    }

    public static int getDamageLocation() {
        return damageLocation;
    }

    public static void setDamageLocation(int damageLocation) {
        Entity.damageLocation = damageLocation;
    }

    public float getTextureRowCount() {
        return textureRowCount;
    }

    public void setTextureRowCount(float textureRowCount) {
        this.textureRowCount = textureRowCount;
    }

    public float getTextureColCount() {
        return textureColCount;
    }

    public void setTextureColCount(float textureColCount) {
        this.textureColCount = textureColCount;
    }
    //endregion

    //region Intializers
    private void setupBuffers() {
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
                0.0f, heightRatio, -5f,
                0.0f, 0.0f, -5f,
                widthRatio, 0.0f, -5f,
                widthRatio, heightRatio, -5f
        };

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        indices = new short[]{0, 1, 2, 0, 2, 3};

        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);

        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    public static void initialize() {
        Log.d(TAG, "Entity initialize");
        programId = GLES20.glCreateProgram();
        Log.d(TAG, "glError: " + GLES20.glGetError());
        int vertexShaderId = RenderUtils.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        Log.d(TAG, "glError: " + GLES20.glGetError());
        int fragmentShaderId = RenderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        Log.d(TAG, "glError: " + GLES20.glGetError());
        GLES20.glAttachShader(programId, vertexShaderId);
        Log.d(TAG, "glError: " + GLES20.glGetError());
        GLES20.glAttachShader(programId, fragmentShaderId);
        Log.d(TAG, "glError: " + GLES20.glGetError());
        GLES20.glLinkProgram(programId);
        Log.d(TAG, "glError: " + GLES20.glGetError());
        GLES20.glUseProgram(programId);
        Log.d(TAG, "glError: " + GLES20.glGetError());
        Entity.positionLocation = GLES20.glGetAttribLocation(programId, "positionCoordinate");
        Entity.textureLocation = GLES20.glGetAttribLocation(programId, "textureCoordinateVertex");
        Entity.matrixLocation = GLES20.glGetUniformLocation(programId, "matrix");
        Entity.alphaLocation = GLES20.glGetUniformLocation(programId, "opacity");
        Entity.animationFrameLocation = GLES20.glGetUniformLocation(programId, "animationFrame");
        Entity.rowCountLocation = GLES20.glGetUniformLocation(programId, "rowCount");
        Entity.colCountLocation = GLES20.glGetUniformLocation(programId, "colCount");
        Entity.samplerLocation = GLES20.glGetUniformLocation(programId, "texture");
        Entity.damageLocation = GLES20.glGetUniformLocation(programId, "damage");
    }
    //endregion

}
