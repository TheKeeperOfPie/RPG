package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class WorldMap {

    public static final byte DOORWAY = 0;
    public static final byte COLLIDE = 1;
    public static final byte FLOOR = 2;
    public static final byte ROOM = 3;

    private static final String TAG = WorldMap.class.getCanonicalName();

    private static final int WALL = 874;
    private static final int CORRIDOR = 921;
    private static final int ROOM_FLOOR = 64;

    private static final String IS_DOORWAY = "Doorway";
    private static final String IS_COLLIDE = "Collide";
    private static final String IS_ABOVE = "Above";
    private static final int MAX_ROOM_WIDTH = 13;
    private static final int MIN_ROOM_WIDTH = 5;
    private static final int MAX_ROOM_HEIGHT = 13;
    private static final int MIN_ROOM_HEIGHT = 5;
    private static final int AREA_PER_ROOM = 300;
    private static final int ATTEMPT_RATIO = 3;

    private List<Tile> tilesBelow;
    private List<Tile> tilesAbove;
    private byte[][] walls;
    private int width;
    private int height;

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
        tilesBelow = new ArrayList<>();
        tilesAbove = new ArrayList<>();
    }

    public static WorldMap fromJson(JSONObject jsonObject) throws JSONException {

        int width = jsonObject.getInt("width");
        int height = jsonObject.getInt("height");
        WorldMap worldMap = new WorldMap(width, height);
        JSONArray layers = jsonObject.getJSONArray("layers");
        ArrayList<Tile> tilesBelow = new ArrayList<>();
        ArrayList<Tile> tilesAbove = new ArrayList<>();

        byte[][] walls = new byte[width][height];

        for (int index = 0; index < layers.length(); index++) {
            JSONObject layer = layers.getJSONObject(index);
            JSONArray data = layer.getJSONArray("data");
            int position = 0;

            byte type = 0;

            if (layer.getString("name").contains(IS_DOORWAY)) {
                type = DOORWAY;
            }
            else if (layer.getString("name").contains(IS_COLLIDE)) {
                type = COLLIDE;
            }

            boolean isAbove = layer.getString("name").contains(IS_ABOVE);

            for (int y = 0; y < width; y++) {
                for (int x = 0; x < height; x++) {
                    int value = data.getInt(position++);

                    if (value > 0) {
                        if (isAbove) {
                            tilesAbove.add(new Tile(new PointF(x, height - 1 - y), value));
                        }
                        else {
                            tilesBelow.add(new Tile(new PointF(x, height - 1 - y), value));
                        }
                        if (type > 0) {
                            walls[x][height - 1 - y] = type;
                        }
                    }
                }
            }
        }

        worldMap.setTilesBelow(tilesBelow);
        worldMap.setTilesAbove(tilesAbove);
        worldMap.setWalls(walls);

        return worldMap;

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

    public static WorldMap generateRectangular(int mapWidth, int mapHeight) {

        if (mapWidth % 2 == 0) {
            mapWidth++;
        }
        if (mapHeight % 2 == 0) {
            mapHeight++;
        }

        WorldMap worldMap = new WorldMap(mapWidth, mapHeight);
        byte[][] layout = new byte[mapWidth][mapHeight];
        Random random = new Random();
        int numRooms = mapWidth * mapHeight / AREA_PER_ROOM;
        int maxAttempts = numRooms * ATTEMPT_RATIO;
        int attempt = 0;
        List<Rect> rooms = new ArrayList<>();

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                layout[x][y] = COLLIDE;
            }
        }

        while (rooms.size() != numRooms && attempt++ < maxAttempts) {

            Log.d(TAG, "Generation attempt: " + attempt);

            int roomWidth = (random.nextInt(MAX_ROOM_WIDTH - MIN_ROOM_WIDTH) + MIN_ROOM_WIDTH) / 2 * 2 + 1;
            int roomHeight = (random.nextInt(MAX_ROOM_HEIGHT - MIN_ROOM_HEIGHT) + MIN_ROOM_HEIGHT) / 2 * 2 + 1;

            Point startPoint = new Point((random.nextInt(mapWidth - Player.OUT_BOUND_X) + Player.OUT_BOUND_X) / 2 * 2 + 1, (random.nextInt(mapHeight - Player.OUT_BOUND_Y) + Player.OUT_BOUND_Y) / 2 * 2 + 1);

            boolean isRoomValid = true;
            if (startPoint.x + roomWidth >= mapWidth || startPoint.y + roomHeight >= mapHeight) {
                isRoomValid = false;
            }
            else {
                for (int x = startPoint.x; x <= startPoint.x + roomWidth; x++) {
                    for (int y = startPoint.y; y <= startPoint.y + roomHeight; y++) {
                        if (layout[x][y] != COLLIDE) {
                            isRoomValid = false;
                            break;
                        }
                    }
                    if (!isRoomValid) {
                        break;
                    }
                }
            }

            if (isRoomValid) {
                for (int x = startPoint.x; x < startPoint.x + roomWidth; x++) {
                    for (int y = startPoint.y; y < startPoint.y + roomHeight; y++) {
                        layout[x][y] = ROOM;
                    }
                }

                rooms.add(new Rect(startPoint.x, startPoint.y, startPoint.x + roomWidth, startPoint.y + roomHeight));

            }

        }

        for (int x = 0; x < mapWidth; x += 2) {
            for (int y = 0; y < mapHeight; y += 2) {
                if (layout[x][y] == COLLIDE) {

                    LinkedList<Point> points = new LinkedList<>();
                    points.add(new Point(x, y));

                    int lastOffsetX = 0;
                    int lastOffsetY = 0;

                    while (!points.isEmpty()) {

                        Point point = points.getLast();

                        List<Point> adjacentWalls = new ArrayList<>();

                        if (point.x - 2 > 0 && layout[point.x - 1][point.y] == COLLIDE && layout[point.x - 2][point.y] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x - 1, point.y));
                        }
                        if (point.x + 2 < mapWidth && layout[point.x + 1][point.y] == COLLIDE && layout[point.x + 2][point.y] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x + 1, point.y));
                        }
                        if (point.y - 2 > 0 && layout[point.x][point.y - 1] == COLLIDE && layout[point.x][point.y - 2] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x, point.y - 1));
                        }
                        if (point.y + 2 < mapHeight && layout[point.x][point.y + 1] == COLLIDE && layout[point.x][point.y + 2] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x, point.y + 1));
                        }

                        if (adjacentWalls.isEmpty()) {
                            points.removeLast();
                            lastOffsetX = 0;
                            lastOffsetY = 0;
                        }
                        else {
                            // Try cutting maze in same direction
                            Point nextPoint = new Point(point.x + lastOffsetX, point.y + lastOffsetY);

                            if (random.nextFloat() < 20f || !adjacentWalls.contains(nextPoint)) {
                                nextPoint = adjacentWalls.get(random.nextInt(adjacentWalls.size()));
                            }

                            lastOffsetX = nextPoint.x - point.x;
                            lastOffsetY = nextPoint.y - point.y;

                            layout[nextPoint.x][nextPoint.y] = FLOOR;
                            layout[nextPoint.x + lastOffsetX][nextPoint.y + lastOffsetY] = FLOOR;

                            points.add(new Point(nextPoint.x + lastOffsetX, nextPoint.y + lastOffsetY));

                        }

                    }
                }
            }
        }

        LinkedList<Point> deadEnds = new LinkedList<>();

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                Point point = new Point(x, y);
                int adjacentWalls = 0;

                if (x - 1 > 0 && layout[x - 1][y] == COLLIDE) {
                    adjacentWalls++;
                }
                if (x + 1 < mapWidth && layout[x +  1][y] == COLLIDE) {
                    adjacentWalls++;
                }
                if (y - 1 > 0 && layout[x][y - 1] == COLLIDE) {
                    adjacentWalls++;
                }
                if (y + 1 < mapHeight && layout[x][y + 1] == COLLIDE) {
                    adjacentWalls++;
                }

                if (adjacentWalls >=3) {
                    deadEnds.add(point);
                }

            }
        }

        while (!deadEnds.isEmpty()) {

            Point deadEnd = deadEnds.removeLast();

            layout[deadEnd.x][deadEnd.y] = COLLIDE;

            List<Point> adjacent = new ArrayList<>(4);

            if (deadEnd.x - 1 > 0 && layout[deadEnd.x - 1][deadEnd.y] == FLOOR) {
                adjacent.add(new Point(deadEnd.x - 1, deadEnd.y));
            }
            if (deadEnd.x + 1 < mapWidth && layout[deadEnd.x + 1][deadEnd.y] == FLOOR) {
                adjacent.add(new Point(deadEnd.x - 1, deadEnd.y));
            }
            if (deadEnd.y - 1 > 0 && layout[deadEnd.x][deadEnd.y - 1] == FLOOR) {
                adjacent.add(new Point(deadEnd.x, deadEnd.y - 1));
            }
            if (deadEnd.y + 1 < mapHeight && layout[deadEnd.x][deadEnd.y + 1] == FLOOR) {
                adjacent.add(new Point(deadEnd.x, deadEnd.y + 1));
            }

            for (Point point : adjacent) {

                int adjacentWalls = 0;

                if (point.x - 1 > 0 && layout[point.x - 1][point.y] == COLLIDE) {
                    adjacentWalls++;
                }
                if (point.x + 1 < mapWidth && layout[point.x +  1][point.y] == COLLIDE) {
                    adjacentWalls++;
                }
                if (point.y - 1 > 0 && layout[point.x][point.y - 1] == COLLIDE) {
                    adjacentWalls++;
                }
                if (point.y + 1 < mapHeight && layout[point.x][point.y + 1] == COLLIDE) {
                    adjacentWalls++;
                }

                if (adjacentWalls >= 3) {
                    deadEnds.addLast(point);
                }

            }

        }

        List<Tile> tilesBelow = new ArrayList<>();

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {

                PointF point = new PointF(x, y);
                tilesBelow.add(new Tile(point, CORRIDOR));
                switch (layout[x][y]) {

                    case COLLIDE:
                        tilesBelow.add(new Tile(point, WALL));
                        break;
//                    case FLOOR:
//                        tilesBelow.add(new Tile(point, CORRIDOR));
//                        break;
                    case ROOM:
                        tilesBelow.add(new Tile(point, ROOM_FLOOR));
                        break;

                }
            }
        }

        worldMap.setWalls(layout);
        worldMap.setTilesBelow(tilesBelow);

        return worldMap;
    }

    /*
        Sorta kinda works, but doesn't fit well with the low resolution
        of the rendered screen.
     */
    public static WorldMap generateCellular(int mapWidth, int mapHeight) {

        // Add 1 to both dimensions to simplify neighbor checking
        boolean[][] layout = new boolean[1 + mapWidth + 1][1 + mapHeight + 1];
        byte[][] walls = new byte[mapWidth][mapHeight];
        WorldMap worldMap = new WorldMap(mapWidth, mapHeight);
        List<Tile> tilesBelow = new ArrayList<>();

        // TODO: Scale repetitions based on map size
        int repetitions = 7;

        Random random = new Random();

        for (int x = 0; x < layout.length; x++) {
            layout[x][0] = true;
            layout[x][layout[0].length - 1] = true;
        }

        for (int y = 0; y < layout.length; y++) {
            layout[0][y] = true;
            layout[layout.length - 1][y] = true;
        }

        for (int x = 1; x < layout.length - 1; x++) {
            for (int y = 1; y < layout[0].length - 1; y++) {
                if (random.nextFloat() < 0.45f) {
                    layout[x][y] = true;
                }
            }
        }

        for (int repetition = 0; repetition < repetitions; repetition++) {

            boolean[][] newLayout = new boolean[layout.length][layout[0].length];

            for (int x = 0; x < newLayout.length; x++) {
                newLayout[x][0] = true;
                newLayout[x][newLayout[0].length - 1] = true;
            }

            for (int y = 0; y < newLayout.length; y++) {
                newLayout[0][y] = true;
                newLayout[newLayout.length - 1][y] = true;
            }


            for (int x = 1; x < layout.length - 1; x++) {
                for (int y = 1; y < layout[0].length - 1; y++) {

                    int adjacentWalls = 0;

                    for (int offsetX = -1; offsetX <= 1; offsetX++) {
                        for (int offsetY = -1; offsetY <= 1; offsetY++) {
                            if (layout[x + offsetX][y + offsetY]) {
                                adjacentWalls++;
                            }
                        }
                    }

                    if (layout[x][y]) {
                        adjacentWalls--;
                    }

                    if (adjacentWalls >= 5 || (repetition < repetitions - 2 && adjacentWalls <= 1)) {
                        newLayout[x][y] = true;
                    }

                }
            }

            layout = newLayout;
        }

        for (int x = 1; x < layout.length - 1; x++) {
            for (int y = 1; y < layout[0].length - 1; y++) {
                if (layout[x][y]) {
                    walls[x - 1][y - 1] = COLLIDE;
                    tilesBelow.add(new Tile(new PointF(x - 1, y - 1), WALL));
                }
                else {
                    tilesBelow.add(new Tile(new PointF(x - 1, y - 1), ROOM_FLOOR));
                }

            }
        }

        worldMap.setWalls(walls);
        worldMap.setTilesBelow(tilesBelow);


        return worldMap;
    }

}
