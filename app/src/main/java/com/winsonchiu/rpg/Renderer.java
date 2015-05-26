package com.winsonchiu.rpg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.maps.WorldMap;
import com.winsonchiu.rpg.maps.WorldMapDungeon;
import com.winsonchiu.rpg.maps.WorldMapTown;
import com.winsonchiu.rpg.tiles.Tile;
import com.winsonchiu.rpg.utils.RenderUtils;

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

    public static final int TEXTURE_MAP = 0;
    public static final int TEXTURE_MOBS = 1;
    public static final int TEXTURE_ATTACKS = 2;
    public static final int TEXTURE_ITEMS = 3;
    public static final int TEXTURE_NUMBERS = 4;
    public static final int NUM_TEXTURES = 5;

    private static final int TILES_IN_ROW = 57;
    private static final int TILES_IN_COL = 31;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_DATA_SIZE = 3;
    private static final int TEXTURE_DATA_SIZE = 2;
    private static final float RENDER_PADDING = 3f;

    private static int programId;
    private static int samplerLocation;
    private static int positionLocation;
    private static int textureLocation;
    private static int matrixLocation;
    private static int alphaLocation;

    private static final String VERTEX_SHADER =
            "uniform mat4 matrix;" +
            "attribute vec4 positionCoordinate;" +
            "attribute vec2 textureCoordinateVertex;" +
            "varying vec2 textureCoordinateFragment;" +
            "void main() {" +
            "  gl_Position = matrix * positionCoordinate;" +
            "  textureCoordinateFragment = textureCoordinateVertex;" +
            "}";

    private static final String FRAGMENT_SHADER =
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
    private float scaleFactor;
    private float minScaleFactor;
    private float maxScaleFactor;
    private float tilesLeftOfOrigin;
    private float tilesRightOfOrigin;
    private float tilesBottomOfOrigin;
    private float tilesTopOfOrigin;
    private WorldMapDungeon worldMapDungeon;
    private WorldMapTown worldMapTown;
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
        player = new Player(eventListenerPlayer);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        initialize();
        Entity.initialize();
        loadTextures();

        buffers = new int[6];
        GLES20.glGenBuffers(6, buffers, 0);

        worldMapTown = new WorldMapTown(activity.getResources());

        worldMapDungeon = new WorldMapDungeon(100, 100);
        worldMapDungeon.generateRectangularDungeon();

        loadMap(worldMapTown, worldMapTown.getSpawnPoint());

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

        renderScene(textureNames[0], buffers[0], buffers[1], getWorldMap().getTilesBelow()
                .size() * 18);

        currentWorldMap.renderEntities(this, matrixProjection, matrixView, textureNames);

        renderScene(textureNames[0], buffers[2], buffers[3], getWorldMap().getTilesAbove()
                .size() * 18);

        if (getWorldMap().renderRoof(this)) {
            renderScene(textureNames[0], buffers[4], buffers[5], getWorldMap().getTilesRoof()
                    .size() * 18);
        }

        if (getWorldMap() instanceof WorldMapDungeon && ((WorldMapDungeon) getWorldMap()).returnToTown(this)) {
            loadMap(worldMapTown, worldMapTown.getDungeonExitPoint());
        }
        else if (getWorldMap() instanceof WorldMapTown && ((WorldMapTown) getWorldMap()).enterDungeon(this)) {
            loadMap(worldMapDungeon, worldMapDungeon.getStartPoint());
        }

        Log.d(TAG, "Frame time: " + (System.currentTimeMillis() - startTime));

    }

    private void loadMap(WorldMap worldMap, PointF pointStart) {

        currentWorldMap = worldMap;

        offsetCameraX = pointStart.x;
        offsetCameraY = pointStart.y;
        player.setLocation(pointStart.x, pointStart.y);

        loadVbo(worldMap);

        if (screenWidth > 0 && screenHeight > 0) {
            setCamera();
        }

    }

    public void loadVbo(WorldMap worldMap) {

        bindBufferFromTiles(worldMap.getTilesBelow(), buffers[0], buffers[1]);
        bindBufferFromTiles(worldMap.getTilesAbove(), buffers[2], buffers[3]);
        bindBufferFromTiles(worldMap.getTilesRoof(), buffers[4], buffers[5]);
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

    public void respawnPlayer() {
        loadMap(worldMapTown, worldMapTown.getSpawnPoint());
        player.setHealth(player.getMaxHealth());
        player.setLastDirection(Direction.SOUTH);
        player.calculateAnimationFrame();
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

            dataPosition[offsetPosition] = xPosition - 0.01f;
            dataPosition[offsetPosition + 1] = yPosition + 1.01f;
            dataPosition[offsetPosition + 2] = zPosition;

            dataPosition[offsetPosition + 3] = xPosition - 0.01f;
            dataPosition[offsetPosition + 4] = yPosition - 0.01f;
            dataPosition[offsetPosition + 5] = zPosition;

            dataPosition[offsetPosition + 6] = xPosition + 1.01f;
            dataPosition[offsetPosition + 7] = yPosition - 0.01f;
            dataPosition[offsetPosition + 8] = zPosition;

            dataPosition[offsetPosition + 9] = xPosition - 0.01f;
            dataPosition[offsetPosition + 10] = yPosition + 1.01f;
            dataPosition[offsetPosition + 11] = zPosition;

            dataPosition[offsetPosition + 12] = xPosition + 1.01f;
            dataPosition[offsetPosition + 13] = yPosition + 1.01f;
            dataPosition[offsetPosition + 14] = zPosition;

            dataPosition[offsetPosition + 15] = xPosition + 1.01f;
            dataPosition[offsetPosition + 16] = yPosition - 0.01f;
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
                        R.drawable.mobs_sheet, options),
                textureNames[TEXTURE_MOBS]);

        bindAndRecycleTexture(BitmapFactory.decodeResource(activity.getResources(),
                        R.drawable.attack_sheet, options),
                textureNames[TEXTURE_ATTACKS]);
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

    public void onScaleChange(float factor) {
        scaleFactor *= factor;
        scaleFactor = Math.max(minScaleFactor, Math.min(scaleFactor, maxScaleFactor));
        setCamera();
    }

    public boolean isPointVisible(PointF location) {

        // Add padding to create a side buffer for Entities to render properly
        return location.x > getOffsetCameraX() + tilesLeftOfOrigin - RENDER_PADDING &&
                location.x < getOffsetCameraX() + tilesRightOfOrigin + RENDER_PADDING &&
                location.y > getOffsetCameraY() + tilesBottomOfOrigin - RENDER_PADDING &&
                location.y < getOffsetCameraY() + tilesTopOfOrigin + RENDER_PADDING;
    }

    public void pickUpItem(Item item) {
        eventListenerRenderer.pickUpItem(item);
    }
    //endregion

    public interface EventListener {
        void pickUpItem(Item item);
    }

}