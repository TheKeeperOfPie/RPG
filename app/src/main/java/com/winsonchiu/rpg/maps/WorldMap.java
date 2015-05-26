package com.winsonchiu.rpg.maps;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;

import com.winsonchiu.rpg.Attack;
import com.winsonchiu.rpg.Direction;
import com.winsonchiu.rpg.Entity;
import com.winsonchiu.rpg.Player;
import com.winsonchiu.rpg.Renderer;
import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.mobs.Mob;
import com.winsonchiu.rpg.tiles.Tile;
import com.winsonchiu.rpg.tiles.TileSet;
import com.winsonchiu.rpg.tiles.TileSetDungeon;
import com.winsonchiu.rpg.tiles.TileType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class WorldMap {

    public static final byte COLLIDE = 1;
    public static final byte ROOM = 2;
    public static final byte CORRIDOR_DISCONNECTED = 3;
    public static final byte CORRIDOR_CONNECTED = 4;

    protected static final String IS_ROOF = "Roof";
    protected static final String IS_COLLIDE = "Collide";
    protected static final String IS_ABOVE = "Above";
    protected static final int MAX_ROOM_WIDTH = 11;
    protected static final int MIN_ROOM_WIDTH = 5;
    protected static final int MAX_ROOM_HEIGHT = 11;
    protected static final int MIN_ROOM_HEIGHT = 5;
    protected static final int AREA_PER_ROOM = 50;
    protected static final int ATTEMPT_RATIO = 5;
    protected static final int CYCLE_RATIO = 10;

    private static final String TAG = WorldMap.class.getCanonicalName();

    protected List<Tile> tilesBelow;
    protected List<Tile> tilesAbove;
    protected List<Tile> tilesRoof;
    protected byte[][] walls;
    protected byte[][] playerTrail;
    protected int width;
    protected int height;
    protected Random random;
    protected TileSet tileSet;
    private final List<Item> items;
    private final List<Mob> entityMobs;
    private final List<Attack> entityAttacks;
    private final List<Entity> entities;

    public WorldMap(int width, int height) {

        this.width = width;
        this.height = height;
        items = Collections.synchronizedList(new ArrayList<Item>());
        random = new Random();
        walls = new byte[width][height];
        playerTrail = new byte[width][height];
        tilesBelow = new ArrayList<>();
        tilesAbove = new ArrayList<>();
        tilesRoof = new ArrayList<>();
        tileSet = new TileSetDungeon();
        entities = Collections.synchronizedList(new ArrayList<Entity>());
        entityMobs = Collections.synchronizedList(new ArrayList<Mob>());
        entityAttacks = Collections.synchronizedList(new ArrayList<Attack>());
    }

    public WorldMap() {
        items = Collections.synchronizedList(new ArrayList<Item>());
        random = new Random();
        walls = new byte[width][height];
        playerTrail = new byte[width][height];
        tilesBelow = new ArrayList<>();
        tilesAbove = new ArrayList<>();
        tilesRoof = new ArrayList<>();
        tileSet = new TileSetDungeon();
        entities = Collections.synchronizedList(new ArrayList<Entity>());
        entityMobs = Collections.synchronizedList(new ArrayList<Mob>());
        entityAttacks = Collections.synchronizedList(new ArrayList<Attack>());
    }

    public void fromJson(JSONObject jsonObject) throws JSONException {

        width = jsonObject.getInt("width");
        height = jsonObject.getInt("height");
        walls = new byte[width][height];
        JSONArray layers = jsonObject.getJSONArray("layers");
        tilesBelow.clear();
        tilesAbove.clear();
        tilesRoof.clear();

        for (int index = 0; index < layers.length(); index++) {
            JSONObject layer = layers.getJSONObject(index);
            JSONArray data = layer.getJSONArray("data");
            int position = 0;

            boolean isCollide = layer.getString("name").contains(IS_COLLIDE);
            boolean isRoof = layer.getString("name").contains(IS_ROOF);
            boolean isAbove = layer.getString("name").contains(IS_ABOVE);

            for (int y = 0; y < width; y++) {
                for (int x = 0; x < height; x++) {
                    int value = data.getInt(position++) - 1;

                    PointF point = new PointF(x, height - 1 - y);

                    if (value > 0) {
                        if (isRoof) {
                            tilesRoof.add(new Tile(point, value));
                        }
                        else if (isAbove) {
                            tilesAbove.add(new Tile(point, value));
                        }
                        else {
                            tilesBelow.add(new Tile(point, value));
                        }
                        if (isCollide) {
                            walls[x][height - 1 - y] = COLLIDE;
                        }
                    }
                }
            }
        }

    }

    public void renderEntities(Renderer renderer, float[] matrixProjection, float[] matrixView, int[] textureNames) {

        Player player = renderer.getPlayer();

        GLES20.glUseProgram(Entity.getProgramId());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[Renderer.TEXTURE_ITEMS]);


        // TODO: Move to proper QuadTree implementation
        synchronized (getItems()) {
            Iterator<Item> iterator = getItems().iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (renderer.isPointVisible(item.getLocation())) {
                    if (RectF.intersects(item.getBounds(), player.getBounds())) {
                        iterator.remove();
                        renderer.pickUpItem(item);
                    }
                    else {
                        item.render(renderer, matrixProjection, matrixView);
                    }
                }
            }
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[Renderer.TEXTURE_MOBS]);

        player.render(renderer, matrixProjection, matrixView);

        synchronized (entityMobs) {
            Iterator<Mob> iterator = entityMobs.iterator();
            while (iterator.hasNext()) {
                Mob mob = iterator.next();
                mob.render(renderer, matrixProjection, matrixView);
                if (mob.getToDestroy()) {
                    iterator.remove();
                }
            }
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[Renderer.TEXTURE_ATTACKS]);

        synchronized (entityAttacks) {
            Iterator<Attack> iterator = entityAttacks.iterator();
            while (iterator.hasNext()) {
                Attack attack = iterator.next();
                attack.render(renderer, matrixProjection, matrixView);
                if (attack.getToDestroy()) {
                    iterator.remove();
                }
            }
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[Renderer.TEXTURE_NUMBERS]);
        synchronized (entities) {
            Iterator<Entity> iterator = entities.iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                entity.render(renderer, matrixProjection, matrixView);
                if (entity.getToDestroy()) {
                    iterator.remove();
                }
            }
        }
    }

    public boolean isCollide(int x, int y) {
        return walls[x][y] == COLLIDE;
    }

    public boolean isCollide(Point point) {
        return walls[point.x][point.y] == COLLIDE;
    }

    public boolean isCollide(Point... points) {

        boolean isCollide = false;

        for (Point point : points) {
            if (isCollide(point)) {
                isCollide = true;
                break;
            }
        }

        return isCollide;
    }

    public void refreshPlayerTrail(PointF point) {

        for (int x = 0; x < playerTrail.length; x++) {
            for (int y = 0; y < playerTrail[0].length; y++) {
                if (playerTrail[x][y] > 0) {
                    playerTrail[x][y]--;
                }
            }
        }

        playerTrail[(int) point.x][(int) point.y] = Byte.MAX_VALUE;
    }

    public boolean dropItem(Item item, Direction direction, PointF location) {

        List<PointF> validLocations = new ArrayList<>();

        for (int offsetDirection = -2; offsetDirection <= 2; offsetDirection++) {

            Direction directionDrop = Direction.offset(direction, offsetDirection);
            validLocations.add(new PointF(location.x + directionDrop.getOffsetX() - item.getWidthRatio() / 2,
                    location.y + directionDrop.getOffsetY() - item.getHeightRatio() / 2));

        }

        Iterator<PointF> iterator = validLocations.iterator();
        while (iterator.hasNext()) {

            PointF point = iterator.next();

            if (isCollide(new Point((int) point.x, (int) point.y)) ||
                    isCollide(
                            new Point((int) (point.x + item.getWidthRatio()), (int) point.y)) ||
                    isCollide(
                            new Point((int) point.x, (int) (point.y + item.getHeightRatio()))) ||
                    isCollide(new Point((int) (point.x + item.getWidthRatio()),
                            (int) (point.y + item.getHeightRatio())))) {
                iterator.remove();
            }

        }

        if (validLocations.isEmpty()) {
            return false;
        }

        PointF pointDrop = validLocations.get(random.nextInt(validLocations.size()));

        item.setLocation(pointDrop);
        addItem(item);

        return true;
    }

    public void dropItems(List<Item> items,  Direction direction, PointF location) {
        for (Item item : items) {
            dropItem(item, direction, location);
        }
    }

    public boolean renderRoof(Renderer renderer) {
        return true;
    }

    //region Getters, setters, and changers

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public PointF getStartPoint() {
        return new PointF(0, 0);
    }

    // TODO: Analyze efficiency of rotation vs bitmask calculation

    public TileType getTileTypeForPath(int x, int y) {

        byte bitMask = 0b0000;

        if (y - 1 > 0 && (walls[x][y - 1] == CORRIDOR_CONNECTED || walls[x][y - 1] == ROOM)) {
            bitMask |= 1;
        }
        if (x + 1 < width && (walls[x + 1][y] == CORRIDOR_CONNECTED || walls[x + 1][y] == ROOM)) {
            bitMask |= 1 << 1;
        }
        if (y + 1 < height && (walls[x][y + 1] == CORRIDOR_CONNECTED || walls[x][y + 1] == ROOM)) {
            bitMask |= 1 << 2;
        }
        if (x - 1 > 0 && (walls[x - 1][y] == CORRIDOR_CONNECTED || walls[x - 1][y] == ROOM)) {
            bitMask |= 1 << 3;
        }

        switch (bitMask) {

            case 0b0101:
                return TileType.PATH_VERTICAL;
            case 0b1010:
                return TileType.PATH_HORIZONTAL;
            case 0b0011:
                return TileType.PATH_TOP_LEFT;
            case 0b0110:
                return TileType.PATH_BOTTOM_LEFT;
            case 0b1100:
                return TileType.PATH_BOTTOM_RIGHT;
            case 0b1001:
                return TileType.PATH_TOP_RIGHT;
            case 0b1011:
                return TileType.PATH_T_DOWN;
            case 0b0111:
                return TileType.PATH_T_RIGHT;
            case 0b1110:
                return TileType.PATH_T_UP;
            case 0b1101:
                return TileType.PATH_T_LEFT;
            case 0b1111:
                return TileType.PATH_FLOOR;

        }

        return TileType.INVALID;
    }

    public TileType getTileTypeForRoom(int x, int y) {

        byte bitMask = 0b0000;

        if (y - 1 > 0 && (walls[x][y - 1] == CORRIDOR_CONNECTED || walls[x][y - 1] == ROOM)) {
            bitMask |= 1;
        }
        if (x + 1 < width && (walls[x + 1][y] == CORRIDOR_CONNECTED || walls[x + 1][y] == ROOM)) {
            bitMask |= 1 << 1;
        }
        if (y + 1 < height && (walls[x][y + 1] == CORRIDOR_CONNECTED || walls[x][y + 1] == ROOM)) {
            bitMask |= 1 << 2;
        }
        if (x - 1 > 0 && (walls[x - 1][y] == CORRIDOR_CONNECTED || walls[x - 1][y] == ROOM)) {
            bitMask |= 1 << 3;
        }

        switch (bitMask) {
            case 0b1011:
                return TileType.ROOM_T_DOWN;
            case 0b0111:
                return TileType.ROOM_T_RIGHT;
            case 0b1110:
                return TileType.ROOM_T_UP;
            case 0b1101:
                return TileType.ROOM_T_LEFT;
            case 0b0011:
                return TileType.ROOM_TOP_LEFT;
            case 0b0110:
                return TileType.ROOM_BOTTOM_LEFT;
            case 0b1100:
                return TileType.ROOM_BOTTOM_RIGHT;
            case 0b1001:
                return TileType.ROOM_TOP_RIGHT;
            case 0b1111:
                return TileType.ROOM_FLOOR;

        }

        return TileType.INVALID;
    }

    public byte[][] getPlayerTrail() {
        return playerTrail;
    }

    public byte[][] getWalls() {
        return walls;
    }

    public void setWalls(byte[][] walls) {
        this.walls = walls;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<Tile> getTilesBelow() {
        return tilesBelow;
    }

    public void setTilesBelow(List<Tile> tilesBelow) {
        this.tilesBelow = tilesBelow;
    }

    public List<Tile> getTilesAbove() {
        return tilesAbove;
    }

    public void setTilesAbove(List<Tile> tilesAbove) {
        this.tilesAbove = tilesAbove;
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

    public List<Tile> getTilesRoof() {
        return tilesRoof;
    }

    public void setClearColor() {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }
    //endregion
}
