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
import com.winsonchiu.rpg.utils.QuadTree;
import com.winsonchiu.rpg.utils.RenderUtils;

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
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by TheKeeperOfPie on 5/1/2015.
 */
public class Renderer implements GLSurfaceView.Renderer {

    private static final String TAG = Renderer.class.getCanonicalName();

    private static final int NUM_TEXTURES = 6;
    private static final int TEXTURE_MAP = 0;
    private static final int TEXTURE_PLAYER = 1;
    private static final int TEXTURE_ATTACKS = 2;
    private static final int TEXTURE_MOBS = 3;
    private static final int TEXTURE_ITEMS = 4;
    private static final int TEXTURE_NUMBERS = 5;

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
    private int tileSize;
    private float offsetCameraX;
    private float offsetCameraY;
    private WorldMap worldMap;
    private final List<Mob> entityMobs;
    private final List<Attack> entityAttacks;
    private final List<Entity> entities;
    private float tilesOnScreenX;
    private float tilesOnScreenY;
    private QuadTree quadTree;

    public Renderer(Activity activity, EventListener eventListenerRenderer, Player.EventListener eventListenerPlayer) {
        super();
        this.activity = activity;
        this.eventListenerRenderer = eventListenerRenderer;
        this.eventListenerPlayer = eventListenerPlayer;
        entities = Collections.synchronizedList(new ArrayList<Entity>());
        entityMobs = Collections.synchronizedList(new ArrayList<Mob>());
        entityAttacks = Collections.synchronizedList(new ArrayList<Attack>());
        startTime = System.currentTimeMillis();
        targetFrameTime = 1000 / 60;
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        tileSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
//        tileSize = 16 * (int) Math.pow(2, tileSize / 16 / 2);
        tilesOnScreenX = displayMetrics.widthPixels / tileSize;
        tilesOnScreenY = displayMetrics.heightPixels / tileSize;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        initialize();
        Entity.initialize();
        loadTextures();

        worldMap = new WorldMap(150, 150);
        worldMap.generateRectangular(this);

        quadTree = new QuadTree(0, new RectF(0, 0, 150, 150));

        PointF pointStart = worldMap.getStartPoint();

        player = new Player(tileSize, pointStart, getTilesOnScreenX() / 7 * 3, getTilesOnScreenY() / 7 * 3, eventListenerPlayer);
        offsetCameraX = pointStart.x - getTilesOnScreenX() / 2;
        offsetCameraY = pointStart.y - getTilesOnScreenY() / 2;

        buffers = new int[4];
        GLES20.glGenBuffers(4, buffers, 0);

        loadVbo();

        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        screenWidth = width;
            screenHeight = height;

        GLES20.glViewport(0, 0, screenWidth, screenHeight);

        android.opengl.Matrix.orthoM(matrixProjection,
                0,
                0,
                screenWidth,
                0,
                screenHeight,
                0f,
                500f * 2);

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

        renderScene(textureNames[0], buffers[0], buffers[1], worldMap.getTilesBelow()
                .size() * 18);

        renderEntities();

        renderScene(textureNames[0], buffers[2], buffers[3], worldMap.getTilesAbove()
                .size() * 18);

    }

    private void refillQuadTree() {
        quadTree.clear();
        for (Entity entity : entityMobs) {
            quadTree.insert(entity);
        }
        for (Entity entity : entityAttacks) {
            quadTree.insert(entity);
        }
        quadTree.insert(player);
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

    private void renderEntities() {

        GLES20.glUseProgram(Entity.getProgramId());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[TEXTURE_ITEMS]);

        synchronized (worldMap.getItems()) {
            Iterator<Item> iterator = worldMap.getItems().iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item.getLocation().x + 2.0f > getOffsetCameraX() &&
                        item.getLocation().x - 2.0f < getOffsetCameraX() + getTilesOnScreenX() &&
                        item.getLocation().y + 2.0f > getOffsetCameraY() &&
                        item.getLocation().y - 2.0f < getOffsetCameraY() + getTilesOnScreenY()) {
                    if (RectF.intersects(item.getBounds(), player.getBounds())) {
                        iterator.remove();
                        eventListenerRenderer.pickUpItem(item);
                    }
                    else {
                        item.render(this, matrixProjection, matrixView);
                    }
                }
            }
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[TEXTURE_PLAYER]);

        player.render(this, matrixProjection, matrixView);

        if (RectF.intersects(player.getBounds(), worldMap.getGoalBounds())) {
            worldMap.activateGoal(this);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[TEXTURE_MOBS]);

        synchronized (entityMobs) {
            Iterator<Mob> iterator = entityMobs.iterator();
            while (iterator.hasNext()) {
                Mob mob = iterator.next();
                mob.render(this, matrixProjection, matrixView);
                if (mob.getToDestroy()) {
                    iterator.remove();
                }
            }
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[TEXTURE_ATTACKS]);

        synchronized (entityAttacks) {
            Iterator<Attack> iterator = entityAttacks.iterator();
            while (iterator.hasNext()) {
                Attack attack = iterator.next();
                attack.render(this, matrixProjection, matrixView);
                if (attack.getToDestroy()) {
                    iterator.remove();
                }
            }
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[TEXTURE_NUMBERS]);
        synchronized (entities) {
            Iterator<Entity> iterator = entities.iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                entity.render(this, matrixProjection, matrixView);
                if (entity.getToDestroy()) {
                    iterator.remove();
                }
            }
        }
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

    public void loadVbo() {

        bindBufferFromTiles(worldMap.getTilesBelow(), buffers[0], buffers[1]);
        bindBufferFromTiles(worldMap.getTilesAbove(), buffers[2], buffers[3]);
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
        android.opengl.Matrix.setLookAtM(matrixView, 0, offsetCameraX * tileSize,
                offsetCameraY * tileSize, 2f,
                offsetCameraX * tileSize, offsetCameraY * tileSize, 1f,
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

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int[] getTextureNames() {
        return textureNames;
    }

    public WorldMap getWorldMap() {
        return worldMap;
    }

    public List<Mob> getEntityMobs() {
        return entityMobs;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void addMob(Mob mob) {
        entityMobs.add(mob);
    }

    public void addMobs(List<Mob> mobs) {
        entityMobs.addAll(mobs);
    }

    public void addAttack(Attack attack) {
        entityAttacks.add(attack);
    }

    public float getTilesOnScreenX() {
        return tilesOnScreenX;
    }

    public float getTilesOnScreenY() {
        return tilesOnScreenY;
    }

    public int getTileSize() {
        return tileSize;
    }

    public QuadTree getQuadTree() {
        return quadTree;
    }
    //endregion

    public interface EventListener {
        void pickUpItem(Item item);
        ControllerInventory getControllerInventory();
    }

}