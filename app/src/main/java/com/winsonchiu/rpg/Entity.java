package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by TheKeeperOfPie on 5/5/2015.
 */
public class Entity {

    public static final String VERTEX_SHADER =
            "uniform mat4 matrix;" +
            "attribute vec4 positionCoordinate;" +
            "attribute vec2 textureCoordinateVertex;" +
            "uniform float rowCount;" +
            "uniform float colCount;" +
            "uniform int animationFrame;" +
            "varying vec2 textureCoordinateFragment;" +
            "void main() {" +
            "  gl_Position = matrix * positionCoordinate;" +
            "  textureCoordinateFragment = textureCoordinateVertex;" +
            "  textureCoordinateFragment.x = (textureCoordinateVertex.x + float(mod(float(animationFrame), rowCount))) / rowCount;" +
            "  textureCoordinateFragment.y = (textureCoordinateVertex.y + float(animationFrame / int(colCount))) / colCount;" +
            "}";

    public static final String FRAGMENT_SHADER =
            "precision mediump float;" +
            "varying vec2 textureCoordinateFragment;" +
            "uniform float opacity;" +
            "uniform sampler2D texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D(texture, textureCoordinateFragment);" +
            "  gl_FragColor.a *= opacity;" +
            "}";

    private static int programId;
    private static int samplerLocation;
    private static int positionLocation;
    private static int textureLocation;
    private static int matrixLocation;
    private static int alphaLocation;
    private static int animationFrameLocation;
    private static int rowCountLocation;
    private static int colCountLocation;

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
    private Movement lastDirection;
    private int tileSize;
    private float textureRowCount;
    private float textureColCount;
    private int textureName;

    public static void initialize() {

        programId = GLES20.glCreateProgram();
        int vertexShaderId = RenderUtils.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShaderId = RenderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(programId, vertexShaderId);
        GLES20.glAttachShader(programId, fragmentShaderId);
        GLES20.glLinkProgram(programId);
        GLES20.glUseProgram(programId);
        Entity.positionLocation = GLES20.glGetAttribLocation(programId, "positionCoordinate");
        Entity.textureLocation = GLES20.glGetAttribLocation(programId, "textureCoordinateVertex");
        Entity.matrixLocation = GLES20.glGetUniformLocation(programId, "matrix");
        Entity.alphaLocation = GLES20.glGetUniformLocation(programId, "opacity");
        Entity.animationFrameLocation = GLES20.glGetUniformLocation(programId, "animationFrame");
        Entity.rowCountLocation = GLES20.glGetUniformLocation(programId, "rowCount");
        Entity.colCountLocation = GLES20.glGetUniformLocation(programId, "colCount");
        Entity.samplerLocation = GLES20.glGetUniformLocation(programId, "texture");
    }

    public Entity(int tileSize, float widthRatio, float heightRatio, PointF location, int textureName, float textureRowCount, float textureColCount) {
        this.tileSize = tileSize;
        this.location = location;
        this.textureName = textureName;
        this.textureRowCount = textureRowCount;
        this.textureColCount = textureColCount;

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
                0.0f, tileSize * heightRatio, -5f,
                0.0f, 0.0f, -5f,
                tileSize * widthRatio, 0.0f, -5f,
                tileSize * widthRatio, tileSize * heightRatio, -5f
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

    public static int getProgram() {
        return programId;
    }

    public void render(float[] matrixProjection, float[] matrixView) {

        GLES20.glUseProgram(programId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureName);
        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glEnableVertexAttribArray(textureLocation);

        android.opengl.Matrix.setIdentityM(getTransMatrix(), 0);
        android.opengl.Matrix.translateM(getTransMatrix(), 0, getLocation().x * tileSize, getLocation().y * tileSize,
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

    public Movement getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(Movement lastDirection) {
        this.lastDirection = lastDirection;
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }
}
