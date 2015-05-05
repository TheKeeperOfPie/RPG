package com.winsonchiu.rpg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.TypedValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by TheKeeperOfPie on 5/1/2015.
 */
public class Renderer implements GLSurfaceView.Renderer {

    private static final String TAG = Renderer.class.getCanonicalName();

    private static final int TILES_IN_ROW = 57;
    private static final int TILES_IN_COL = 31;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_DATA_SIZE = 3;
    private static final int TEXTURE_DATA_SIZE = 2;

    private Activity activity;
    private RenderValues renderValues;
    private int screenWidth;
    private int screenHeight;
    private float[] matrixProjection = new float[16];
    private float[] matrixView = new float[16];
    private float[] matrixProjectionAndView = new float[16];
    private int[] buffers;
    private int[] textureNames;
    private float[] transMatrix = new float[16];
    private long endTime;
    private long startTime;
    private long frameTime;
    private long targetFrameTime;
    private Player player;
    private int tileSize;
    private float offsetCameraX;
    private float offsetCameraY;
    private Map map;

    public Renderer(Activity activity) {
        super();
        this.activity = activity;
        startTime = System.currentTimeMillis();
        targetFrameTime = 1000 / 60;
        tileSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, activity.getResources().getDisplayMetrics());
        tileSize = 16 * (int) Math.pow(2, tileSize / 16 / 2);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        player = new Player(tileSize);
        loadTextures();

        GLES20.glClearColor(0f, 0f, 0f, 1f);

        renderValues = new RenderValues(RenderValues.vertexShaderTiles, RenderValues.fragmentShaderTiles);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        screenWidth = width;
        screenHeight = height;

        GLES20.glViewport(0, 0, screenWidth, screenHeight);

        player.setDimensions(width, height);

        android.opengl.Matrix.orthoM(matrixProjection,
                                     0,
                                     0,
                                     screenWidth,
                                     0,
                                     screenHeight,
                                     0f,
                                     500f * 2);

        android.opengl.Matrix.setLookAtM(matrixView, 0, offsetCameraX, offsetCameraY, 2f,
                                         offsetCameraX, offsetCameraY, 1f, 0.0f, 1.0f, 0.0f);

        loadVbo();

    }

    private void loadVbo() {

        InputStream is = activity.getResources().openRawResource(R.raw.level);
        Writer writer = new StringWriter();
        char[] writeBuffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(writeBuffer)) != -1) {
                writer.write(writeBuffer, 0, n);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            map = Map.fromJson(new JSONObject(writer.toString()));
        }
        catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        buffers = new int[4];
        GLES20.glGenBuffers(4, buffers, 0);

        bindBufferFromTiles(map.getTilesBelow(), buffers[0], buffers[1]);
        bindBufferFromTiles(map.getTilesAbove(), buffers[2], buffers[3]);
    }

    private void bindBufferFromTiles(List<Tile> tiles, int positionBufferId, int textureBufferId) {

        float[] dataPosition = new float[tiles.size() * 18];
        float[] dataTexture = new float[tiles.size() * 12];
        int offsetPosition = 0;
        int offsetTexture = 0;

        float xTextureSize = 1f / TILES_IN_ROW / 17 * 16;
        float yTextureSize = 1f / TILES_IN_COL / 17 * 16;

        for (Tile tile : tiles) {

            float xPosition = tile.getVertex().x * tileSize;
            float yPosition = tile.getVertex().y * tileSize;
            float zPosition = 0f;

            dataPosition[offsetPosition] = xPosition;
            dataPosition[offsetPosition + 1] = yPosition + tileSize;
            dataPosition[offsetPosition + 2] = zPosition;

            dataPosition[offsetPosition + 3] = xPosition;
            dataPosition[offsetPosition + 4] = yPosition;
            dataPosition[offsetPosition + 5] = zPosition;

            dataPosition[offsetPosition + 6] = xPosition + tileSize;
            dataPosition[offsetPosition + 7] = yPosition;
            dataPosition[offsetPosition + 8] = zPosition;

            dataPosition[offsetPosition + 9] = xPosition;
            dataPosition[offsetPosition + 10] = yPosition + tileSize;
            dataPosition[offsetPosition + 11] = zPosition;

            dataPosition[offsetPosition + 12] = xPosition + tileSize;
            dataPosition[offsetPosition + 13] = yPosition + tileSize;
            dataPosition[offsetPosition + 14] = zPosition;

            dataPosition[offsetPosition + 15] = xPosition + tileSize;
            dataPosition[offsetPosition + 16] = yPosition;
            dataPosition[offsetPosition + 17] = zPosition;

            offsetPosition += 18;

            float xTexture = 1f / TILES_IN_ROW * ((tile.getTextureId() - 1) % TILES_IN_ROW);
            float yTexture = 1f / TILES_IN_COL * ((tile.getTextureId() - 1) / TILES_IN_ROW);

            dataTexture[offsetTexture] = xTexture;
            dataTexture[offsetTexture + 1] = yTexture;

            dataTexture[offsetTexture + 2] = xTexture;
            dataTexture[offsetTexture + 3] = yTexture + yTextureSize;

            dataTexture[offsetTexture + 4] = xTexture + xTextureSize;
            dataTexture[offsetTexture + 5] = yTexture + yTextureSize;

            dataTexture[offsetTexture + 6] = xTexture;
            dataTexture[offsetTexture + 7] = yTexture;

            dataTexture[offsetTexture + 8] = xTexture + xTextureSize;
            dataTexture[offsetTexture + 9] = yTexture;

            dataTexture[offsetTexture + 10] = xTexture + xTextureSize;
            dataTexture[offsetTexture + 11] = yTexture + yTextureSize;

            offsetTexture += 12;
        }

        FloatBuffer positionBuffer = ByteBuffer.allocateDirect(
                dataPosition.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(dataPosition);
        positionBuffer.position(0);

        FloatBuffer textureBuffer = ByteBuffer.allocateDirect(dataTexture.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(dataTexture);
        textureBuffer.position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionBufferId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, positionBuffer.capacity() * BYTES_PER_FLOAT,
                            positionBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureBufferId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureBuffer.capacity() * BYTES_PER_FLOAT,
                            textureBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        try {
            endTime = System.currentTimeMillis();
            frameTime = endTime - startTime;
            if (frameTime < targetFrameTime) {
                Thread.sleep(targetFrameTime - frameTime);
            }
            startTime = System.currentTimeMillis();
        }
        catch (InterruptedException e) {
        }

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glEnableVertexAttribArray(renderValues.getPositionHandle());
        GLES20.glEnableVertexAttribArray(renderValues.getTexCoordLoc());
        GLES20.glUniform1i(renderValues.getSamplerLoc(), 0);

        android.opengl.Matrix.setIdentityM(transMatrix, 0);
        android.opengl.Matrix.translateM(transMatrix, 0, 0f, 0f, 0);

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

        renderScene(textureNames[0], buffers[0], buffers[1], map.getTilesBelow()
                .size() * 18);

        player.render(textureNames, this, matrixProjection, matrixView);

        renderScene(textureNames[0], buffers[2], buffers[3], map.getTilesAbove()
                .size() * 18);

        GLES20.glDisableVertexAttribArray(renderValues.getPositionHandle());
        GLES20.glDisableVertexAttribArray(renderValues.getTexCoordLoc());
    }

    private void renderScene(int textureId, int positionBufferId, int textureBufferId, int size) {

        GLES20.glUseProgram(renderValues.getProgram());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1f(renderValues.getAlphaHandle(), 1.0f);
        GLES20.glUniformMatrix4fv(renderValues.getMatrixHandle(), 1, false, matrixProjectionAndView,
                                  0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionBufferId);
        GLES20.glVertexAttribPointer(renderValues.getPositionHandle(), POSITION_DATA_SIZE,
                                     GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureBufferId);
        GLES20.glVertexAttribPointer(renderValues.getTexCoordLoc(), TEXTURE_DATA_SIZE,
                                     GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, size);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void offsetCamera(float x, float y) {
        offsetCameraX += x * tileSize;
        offsetCameraY += y * tileSize;
        android.opengl.Matrix.setLookAtM(matrixView, 0, offsetCameraX, offsetCameraY, 2f,
                                         offsetCameraX, offsetCameraY, 1f, 0.0f, 1.0f, 0.0f);
    }

    private void loadTextures() {
        textureNames = new int[3];
        GLES20.glGenTextures(3, textureNames, 0);

        // TODO: Scale check for maximum texture size
        int[] maxTextureSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Force inScaled off to prevent a blurry texture
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                                                           R.drawable.texture_sheet, options),
                              textureNames[0]);

        GLES20.glUseProgram(player.getRenderValues()
                                    .getProgram());

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                                                           R.drawable.character_sheet, options),
                              textureNames[1]);

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                                                           R.drawable.attack_sheet, options),
                              textureNames[2]);

    }

    private void bindAndRecycleTexture(Bitmap bitmap, int textureId) {

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                               GLES20.GL_TEXTURE_MIN_FILTER,
                               GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                               GLES20.GL_TEXTURE_MAG_FILTER,
                               GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                               GLES20.GL_TEXTURE_WRAP_S,
                               GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                               GLES20.GL_TEXTURE_WRAP_T,
                               GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();
    }

    public void release() {
        if (buffers != null) {
            GLES20.glDeleteBuffers(buffers.length, buffers, 0);
        }

        if (textureNames != null) {
            GLES20.glDeleteTextures(textureNames.length, textureNames, 0);
        }
    }

    public float getOffsetCameraX() {
        return offsetCameraX;
    }

    public float getOffsetCameraY() {
        return offsetCameraY;
    }

    public byte[][] getWalls() {
        return map.getWalls();
    }

    public Player getPlayer() {
        return player;
    }

    public RenderValues getRenderValues() {
        return renderValues;
    }

    public float[] getMatrixProjectionAndView() {
        return matrixProjectionAndView;
    }

    public float[] getMatrixView() {
        return matrixView;
    }

    public float[] getMatrixProjection() {
        return matrixProjection;
    }
}
