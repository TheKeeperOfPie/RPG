package com.winsonchiu.rpg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.mobs.Mob;
import com.winsonchiu.rpg.tiles.Tile;
import com.winsonchiu.rpg.utils.QuadTree;
import com.winsonchiu.rpg.utils.RenderUtils;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by TheKeeperOfPie on 5/1/2015.
 */
public class Renderer implements GLSurfaceView.Renderer {

    private static final String TAG = Renderer.class.getCanonicalName();

    private static final int NUM_TEXTURES = 6;
    public static final int TEXTURE_MAP = 0;
    public static final int TEXTURE_PLAYER = 1;
    public static final int TEXTURE_ATTACKS = 2;
    public static final int TEXTURE_MOBS = 3;
    public static final int TEXTURE_ITEMS = 4;
    public static final int TEXTURE_NUMBERS = 5;

    private static final int TILES_IN_ROW = 57;
    private static final int TILES_IN_COL = 31;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_DATA_SIZE = 3;
    private static final int TEXTURE_DATA_SIZE = 2;

    private static int programId;
    private static int samplerLocation;
    private static int positionLocation;
    private static int textureLocation;
    private static int matrixLocation;
    private static int alphaLocation;

    public static final String VERTEX_SHADER =
            "uniform mat4 matrix;" +
            "attribute vec4 positionCoordinate;" +
            "attribute vec2 textureCoordinateVertex;" +
            "varying vec2 textureCoordinateFragment;" +
            "void main() {" +
            "  gl_Position = matrix * positionCoordinate;" +
            "  textureCoordinateFragment = textureCoordinateVertex;" +
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

    private Activity activity;
    private EventListener eventListenerRenderer;
    private Player.EventListener eventListenerPlayer;
    private int screenWidth;
    private int screenHeight;
    private float[] matrixProjection = new float[16];
    private float[] matrixView = new float[16];
    private float[] matrixProjectionAndView = new float[16];
    private int[] buffers;
    private int[] textureNames;
    private long endTime;
    private long startTime;
    private long frameTime;
    private long targetFrameTime;
    private Player player;
    private float offsetCameraX;
    private float offsetCameraY;
    private WorldMap worldMapDungeon;
    private QuadTree quadTree;
    private float scaleFactor;
    private float minScaleFactor;
    private float maxScaleFactor;
    private float tilesLeftOfOrigin;
    private float tilesRightOfOrigin;
    private float tilesBottomOfOrigin;
    private float tilesTopOfOrigin;
    private WorldMap worldMapTown;
    private WorldMap currentWorldMap;

    public Renderer(Activity activity, EventListener eventListenerRenderer, Player.EventListener eventListenerPlayer) {
        super();
        this.activity = activity;
        this.eventListenerRenderer = eventListenerRenderer;
        this.eventListenerPlayer = eventListenerPlayer;
        startTime = System.currentTimeMillis();
        targetFrameTime = 1000 / 60;
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        float targetTileSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
        scaleFactor = targetTileSize;
        maxScaleFactor = targetTileSize;
        minScaleFactor = targetTileSize / 20;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        initialize();
        Entity.initialize();
        loadTextures();

        buffers = new int[4];
        GLES20.glGenBuffers(4, buffers, 0);

        player = new Player(new PointF(3, 3), eventListenerPlayer);

        worldMapTown = getTown();

        worldMapDungeon = new WorldMap(150, 150);
        worldMapDungeon.generateRectangularDungeon(this);

        quadTree = new QuadTree(0, new RectF(0, 0, 150, 150));

        loadMap(worldMapDungeon, worldMapDungeon.getStartPoint());

        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        screenWidth = width;
        screenHeight = height;

        GLES20.glViewport(0, 0, screenWidth, screenHeight);
        setCamera();

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

        android.opengl.Matrix.multiplyMM(matrixProjectionAndView,
                0,
                matrixProjection,
                0,
                matrixView,
                0);

        refillQuadTree();

        renderScene(textureNames[0], buffers[0], buffers[1], getWorldMap().getTilesBelow()
                .size() * 18);

        currentWorldMap.renderEntities(this, matrixProjection, matrixView, textureNames);

        renderScene(textureNames[0], buffers[2], buffers[3], getWorldMap().getTilesAbove()
                .size() * 18);

        if (getWorldMap() == worldMapDungeon && getWorldMap().getBoundsStart().contains(player.getLocation().x + Player.WIDTH_RATIO / 2, player.getLocation().y + Player.HEIGHT_RATIO / 2)) {
            loadMap(worldMapTown, new PointF(55, 35));
        }
        else if (getWorldMap() == worldMapTown && getWorldMap().getBoundsStart().contains(player.getLocation().x + Player.WIDTH_RATIO / 2, player.getLocation().y + Player.HEIGHT_RATIO / 2)) {
            loadMap(worldMapDungeon, worldMapDungeon.getStartPoint());
        }

        Log.d(TAG, "Bounds start: " + getWorldMap().getBoundsStart());
        Log.d(TAG, "Player location: " + player.getLocation());

        Log.d(TAG, "Frame time: " + (System.currentTimeMillis() - startTime));

    }

    private void loadMap(WorldMap worldMap, PointF pointStart) {

        currentWorldMap = worldMap;

        offsetCameraX = pointStart.x;
        offsetCameraY = pointStart.y;
        player.setLocation(pointStart.x, pointStart.y);

        loadVbo(worldMap);

    }

    public void loadVbo(WorldMap worldMap) {

        bindBufferFromTiles(worldMap.getTilesBelow(), buffers[0], buffers[1]);
        bindBufferFromTiles(worldMap.getTilesAbove(), buffers[2], buffers[3]);
    }

    private WorldMap getTown() {

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

        WorldMap worldMap;

        try {
            worldMap = WorldMap.fromJson(new JSONObject(writer.toString()));
        }
        catch (JSONException e) {
            worldMap = new WorldMap(50, 50);
        }

        worldMap.setBoundsStart(new RectF(56, 35, 57, 36));

        return worldMap;
    }

    private void refillQuadTree() {

        // TODO: Fix and implement QuadTree
//        quadTree.clear();
//        for (Entity entity : entityMobs) {
//            quadTree.insert(entity);
//        }
//        for (Entity entity : entityAttacks) {
//            quadTree.insert(entity);
//        }
//        quadTree.insert(player);
    }

    private void renderScene(int textureId, int positionBufferId, int textureBufferId, int size) {

        GLES20.glUseProgram(programId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glEnableVertexAttribArray(textureLocation);

        GLES20.glUniform1i(samplerLocation, 0);
        GLES20.glUniform1f(alphaLocation, 1.0f);
        GLES20.glUniformMatrix4fv(matrixLocation, 1, false, matrixProjectionAndView,
                0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionBufferId);
        GLES20.glVertexAttribPointer(positionLocation, POSITION_DATA_SIZE,
                GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureBufferId);
        GLES20.glVertexAttribPointer(textureLocation, TEXTURE_DATA_SIZE,
                GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, size);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glDisableVertexAttribArray(textureLocation);
    }

    //region Setup and teardown
    private void initialize() {
        programId = GLES20.glCreateProgram();
        int vertexShaderId = RenderUtils.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShaderId = RenderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(programId, vertexShaderId);
        GLES20.glAttachShader(programId, fragmentShaderId);
        GLES20.glLinkProgram(programId);
        GLES20.glUseProgram(programId);
        Renderer.positionLocation = GLES20.glGetAttribLocation(programId, "positionCoordinate");
        Renderer.textureLocation = GLES20.glGetAttribLocation(programId, "textureCoordinateVertex");
        Renderer.matrixLocation = GLES20.glGetUniformLocation(programId, "matrix");
        Renderer.alphaLocation = GLES20.glGetUniformLocation(programId, "opacity");
        Renderer.samplerLocation = GLES20.glGetUniformLocation(programId, "texture");
    }

    private void bindBufferFromTiles(List<Tile> tiles, int positionBufferId, int textureBufferId) {

        float[] dataPosition = new float[tiles.size() * 18];
        float[] dataTexture = new float[tiles.size() * 12];
        int offsetPosition = 0;
        int offsetTexture = 0;

        float xTextureSize = 1f / TILES_IN_ROW / 17 * 16;
        float yTextureSize = 1f / TILES_IN_COL / 17 * 16;

        for (Tile tile : tiles) {

            float xPosition = tile.getVertex().x;
            float yPosition = tile.getVertex().y;
            float zPosition = 0f;

            dataPosition[offsetPosition] = xPosition;
            dataPosition[offsetPosition + 1] = yPosition + 1f;
            dataPosition[offsetPosition + 2] = zPosition;

            dataPosition[offsetPosition + 3] = xPosition;
            dataPosition[offsetPosition + 4] = yPosition;
            dataPosition[offsetPosition + 5] = zPosition;

            dataPosition[offsetPosition + 6] = xPosition + 1f;
            dataPosition[offsetPosition + 7] = yPosition;
            dataPosition[offsetPosition + 8] = zPosition;

            dataPosition[offsetPosition + 9] = xPosition;
            dataPosition[offsetPosition + 10] = yPosition + 1f;
            dataPosition[offsetPosition + 11] = zPosition;

            dataPosition[offsetPosition + 12] = xPosition + 1f;
            dataPosition[offsetPosition + 13] = yPosition + 1f;
            dataPosition[offsetPosition + 14] = zPosition;

            dataPosition[offsetPosition + 15] = xPosition + 1f;
            dataPosition[offsetPosition + 16] = yPosition;
            dataPosition[offsetPosition + 17] = zPosition;

            offsetPosition += 18;

            float xTexture = 1f / TILES_IN_ROW * (tile.getTextureId() % TILES_IN_ROW);
            float yTexture = 1f / TILES_IN_COL * (tile.getTextureId() / TILES_IN_ROW);

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

    private void loadTextures() {
        textureNames = new int[NUM_TEXTURES];
        GLES20.glGenTextures(NUM_TEXTURES, textureNames, 0);

        // TODO: Scale check for maximum texture size
        int[] maxTextureSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Force inScaled off to prevent a blurry texture
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                        R.drawable.texture_sheet, options),
                textureNames[TEXTURE_MAP]);

        GLES20.glUseProgram(Entity.getProgramId());

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                        R.drawable.player_sheet, options),
                textureNames[TEXTURE_PLAYER]);

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                        R.drawable.attack_sheet, options),
                textureNames[TEXTURE_ATTACKS]);

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                        R.drawable.mob_sheet, options),
                textureNames[TEXTURE_MOBS]);

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                        R.drawable.item_sheet, options),
                textureNames[TEXTURE_ITEMS]);

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                        R.drawable.number_sheet, options),
                textureNames[TEXTURE_NUMBERS]);

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
    //endregion

    //region Getters, setters, changers
    private void setCamera() {
        // Cache values to prevent recalculation on every Entity every frame
        tilesLeftOfOrigin = -screenWidth / 2 / scaleFactor;
        tilesRightOfOrigin = screenWidth / 2 / scaleFactor;
        tilesBottomOfOrigin = -screenHeight / 2 / scaleFactor;
        tilesTopOfOrigin = screenHeight / 2 / scaleFactor;

        android.opengl.Matrix.orthoM(matrixProjection,
                0,
                tilesLeftOfOrigin,
                tilesRightOfOrigin,
                tilesBottomOfOrigin,
                tilesTopOfOrigin,
                0f,
                500f * 2);

        android.opengl.Matrix.setLookAtM(matrixView, 0, offsetCameraX,
                offsetCameraY, 2f,
                offsetCameraX, offsetCameraY, 1f,
                0.0f, 1.0f, 0.0f);
    }

    public void offsetCamera(float x, float y) {
        offsetCameraX += x;
        offsetCameraY += y;
        setCamera();
    }

    public float getOffsetCameraX() {
        return offsetCameraX;
    }

    public float getOffsetCameraY() {
        return offsetCameraY;
    }

    public Player getPlayer() {
        return player;
    }

    public WorldMap getWorldMap() {
        return currentWorldMap;
    }

    public QuadTree getQuadTree() {
        return quadTree;
    }

    public void onScaleChange(float factor) {
        scaleFactor *= factor;
        scaleFactor = Math.max(minScaleFactor, Math.min(scaleFactor, maxScaleFactor));
        setCamera();
    }

    public boolean isPointVisible(PointF location) {

         return location.x > getOffsetCameraX() + tilesLeftOfOrigin &&
                 location.x < getOffsetCameraX() + tilesRightOfOrigin &&
                 location.y > getOffsetCameraY() + tilesBottomOfOrigin &&
                 location.y < getOffsetCameraY() + tilesTopOfOrigin;
    }

    public void pickUpItem(Item item) {
        eventListenerRenderer.pickUpItem(item);
    }

    //endregion

    public interface EventListener {
        void pickUpItem(Item item);
    }

}