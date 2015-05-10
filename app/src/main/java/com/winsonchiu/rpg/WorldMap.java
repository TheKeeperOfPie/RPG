package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class WorldMap {

    public static final byte COLLIDE = 0;
    public static final byte FLOOR = 1;
    public static final byte ROOM = 2;
    public static final byte DOORWAY = 3;
    public static final byte CORRIDOR_DISCONNECTED = 4;
    public static final byte CORRIDOR_CONNECTED = 5;

    private static final String TAG = WorldMap.class.getCanonicalName();
    private static final float CHANCE_TO_CURVE_MAZE = 0.6f;

    private static final int WALL = 177;
    private static final int CORRIDOR = 920;

    private static final String IS_DOORWAY = "Doorway";
    private static final String IS_COLLIDE = "Collide";
    private static final String IS_ABOVE = "Above";
    private static final int MAX_ROOM_WIDTH = 19;
    private static final int MIN_ROOM_WIDTH = 15;
    private static final int MAX_ROOM_HEIGHT = 19;
    private static final int MIN_ROOM_HEIGHT = 15;
    private static final int AREA_PER_ROOM = 60;
    private static final int ATTEMPT_RATIO = 3;

    private List<Tile> tilesBelow;
    private List<Tile> tilesAbove;
    private List<Rect> rooms;
    private byte[][] walls;
    private byte[][] playerTrail;
    private int width;
    private int height;

    public WorldMap(int width, int height) {

        // Width and height must be odd to fit mazes/rooms correctly
        if (width % 2 == 0) {
            width++;
        }
        if (height % 2 == 0) {
            height++;
        }

        this.width = width;
        this.height = height;
        rooms = new ArrayList<>();
        walls = new byte[width][height];
        playerTrail = new byte[width][height];
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
                    int value = data.getInt(position++) - 1;

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

    public void generateRectangular() {

        Random random = new Random();
        int numRooms = width * height / AREA_PER_ROOM;
        int maxAttempts = numRooms * ATTEMPT_RATIO;
        int attempt = 0;

        while (rooms.size() < numRooms && attempt++ < maxAttempts) {

            Log.d(TAG, "Generation attempt: " + attempt);

            // Use of / 2 * 2 + 1 to make numbers always odd

            int roomWidth = (random.nextInt(MAX_ROOM_WIDTH - MIN_ROOM_WIDTH) + MIN_ROOM_WIDTH) / 2 * 2 + 1;
            int roomHeight = (random.nextInt(MAX_ROOM_HEIGHT - MIN_ROOM_HEIGHT) + MIN_ROOM_HEIGHT) / 2 * 2 + 1;


            // Use of / 2 * 2 to make rooms start on even number to line up properly
            Point startPoint = new Point((random.nextInt(width - Player.OUT_BOUND_X) + Player.OUT_BOUND_X) / 2 * 2, (random.nextInt(height - Player.OUT_BOUND_Y) + Player.OUT_BOUND_Y) / 2 * 2);

            // TODO: Change to check intersection with list of rooms
            boolean isRoomValid = true;
            if (startPoint.x + roomWidth >= width || startPoint.y + roomHeight >= height) {
                isRoomValid = false;
            }
            else {
                for (int x = startPoint.x; x <= startPoint.x + roomWidth; x++) {
                    for (int y = startPoint.y; y <= startPoint.y + roomHeight; y++) {
                        if (walls[x][y] != COLLIDE) {
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
                        walls[x][y] = ROOM;
                    }
                }

                rooms.add(new Rect(startPoint.x, startPoint.y, startPoint.x + roomWidth - 1, startPoint.y + roomHeight - 1));

            }

        }

        for (int x = 1; x < width; x += 2) {
            for (int y = 1; y < height; y += 2) {
                if (walls[x][y] == COLLIDE) {

                    LinkedList<Point> points = new LinkedList<>();
                    points.add(new Point(x, y));

                    int lastOffsetX = 0;
                    int lastOffsetY = 0;

                    while (!points.isEmpty()) {

                        Point point = points.getLast();

                        List<Point> adjacentWalls = new ArrayList<>();

                        if (point.x - 2 > 0 && walls[point.x - 2][point.y] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x - 1, point.y));
                        }
                        if (point.x + 2 < width && walls[point.x + 2][point.y] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x + 1, point.y));
                        }
                        if (point.y - 2 > 0 && walls[point.x][point.y - 2] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x, point.y - 1));
                        }
                        if (point.y + 2 < height && walls[point.x][point.y + 2] == COLLIDE) {
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

                            if (random.nextFloat() < CHANCE_TO_CURVE_MAZE || !adjacentWalls.contains(nextPoint)) {
                                nextPoint = adjacentWalls.get(random.nextInt(adjacentWalls.size()));
                            }

                            lastOffsetX = nextPoint.x - point.x;
                            lastOffsetY = nextPoint.y - point.y;

                            walls[nextPoint.x][nextPoint.y] = CORRIDOR_DISCONNECTED;
                            walls[nextPoint.x + lastOffsetX][nextPoint.y + lastOffsetY] = CORRIDOR_DISCONNECTED;

                            points.add(new Point(nextPoint.x + lastOffsetX, nextPoint.y + lastOffsetY));

                        }

                    }
                }
            }
        }

        for (Rect room : rooms) {

            int numConnections = 0;

            List<Point> connections = new ArrayList<>();

            for (int x = room.left; x <= room.right; x++) {
                walls[x][room.top] = COLLIDE;
                walls[x][room.bottom] = COLLIDE;
            }
            for (int y = room.top; y <= room.bottom; y++) {
                walls[room.left][y] = COLLIDE;
                walls[room.right][y] = COLLIDE;
            }

            if (room.top - 1 > 0) {
                for (int x = room.left + 1; x < room.right - 1; x++) {
                    connections.add(new Point(x, room.top - 1));
                }
            }
            if (room.bottom + 1 < height) {
                for (int x = room.left + 1; x < room.right - 1; x++) {
                    connections.add(new Point(x, room.bottom + 1));
                }
            }
            if (room.left - 1 > 0) {
                for (int y = room.top + 1; y < room.bottom - 1; y++) {
                    connections.add(new Point(room.left - 1, y));
                }
            }
            if (room.right + 1 < width) {
                for (int y = room.top + 1; y < room.bottom - 1; y++) {
                    connections.add(new Point(room.right + 1, y));
                }
            }

            while (!connections.isEmpty()) {

                Point pointStart = connections.get(random.nextInt(connections.size()));
                Point cutPoint = new Point(pointStart);

                if (pointStart.x < room.left) {
                    cutPoint.offset(1, 0);
                }
                if (pointStart.x > room.right) {
                    cutPoint.offset(-1, 0);
                }
                if (pointStart.y < room.top) {
                    cutPoint.offset(0, 1);
                }
                if (pointStart.y > room.bottom) {
                    cutPoint.offset(0, -1);
                }

                walls[cutPoint.x][cutPoint.y] = CORRIDOR_CONNECTED;

                if (walls[pointStart.x][pointStart.y] == CORRIDOR_DISCONNECTED) {

                    Stack<Point> points = new Stack<>();

                    if (pointStart.x - 1 > 0 && walls[pointStart.x - 1][pointStart.y] == CORRIDOR_DISCONNECTED) {
                        points.add(new Point(pointStart.x - 1, pointStart.y));
                    }
                    if (pointStart.x + 1 < width && walls[pointStart.x + 1][pointStart.y] == CORRIDOR_DISCONNECTED) {
                        points.add(new Point(pointStart.x + 1, pointStart.y));
                    }
                    if (pointStart.y - 1 > 0 && walls[pointStart.x][pointStart.y - 1] == CORRIDOR_DISCONNECTED) {
                        points.add(new Point(pointStart.x, pointStart.y - 1));
                    }
                    if (pointStart.y + 1 < height && walls[pointStart.x][pointStart.y + 1] == CORRIDOR_DISCONNECTED) {
                        points.add(new Point(pointStart.x, pointStart.y + 1));
                    }

                    while (!points.isEmpty()) {

                        Point point = points.pop();
                        walls[point.x][point.y] = CORRIDOR_CONNECTED;

                        if (point.x - 1 > 0 && walls[point.x - 1][point.y] == CORRIDOR_DISCONNECTED) {
                            points.add(new Point(point.x - 1, point.y));
                        }
                        if (point.x + 1 < width && walls[point.x + 1][point.y] == CORRIDOR_DISCONNECTED) {
                            points.add(new Point(point.x + 1, point.y));
                        }
                        if (point.y - 1 > 0 && walls[point.x][point.y - 1] == CORRIDOR_DISCONNECTED) {
                            points.add(new Point(point.x, point.y - 1));
                        }
                        if (point.y + 1 < height && walls[point.x][point.y + 1] == CORRIDOR_DISCONNECTED) {
                            points.add(new Point(point.x, point.y + 1));
                        }

                    }

                }

                connections.remove(pointStart);
                connections.remove(new Point(pointStart.x - 1, pointStart.y));
                connections.remove(new Point(pointStart.x + 1, pointStart.y));
                connections.remove(new Point(pointStart.x, pointStart.y - 1));
                connections.remove(new Point(pointStart.x, pointStart.y + 1));

                if (numConnections++ >= 2) {
                    Iterator<Point> iterator = connections.iterator();
                    while (iterator.hasNext()) {
                        Point point = iterator.next();
                        if (walls[point.x][point.y] == CORRIDOR_CONNECTED) {
                            iterator.remove();
                        }
                    }
                }

            }

        }

        removeDeadEnds();


        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                PointF point = new PointF(x, y);
                tilesBelow.add(new Tile(point, CORRIDOR));
                switch (walls[x][y]) {

                    case COLLIDE:
                        tilesBelow.add(new Tile(point, WALL));
                        break;
                    case CORRIDOR_CONNECTED:
                        tilesBelow.add(new Tile(point, getTextureForPath(x, y)));
                        break;
                    case ROOM:
                        tilesBelow.add(new Tile(point, getTextureForRoom(x, y)));
                        break;

                }
            }
        }

    }

    private static final int PATH_TOP_LEFT = 748;
    private static final int PATH_TOP_RIGHT = 749;
    private static final int PATH_BOTTOM_LEFT = 805;
    private static final int PATH_BOTTOM_RIGHT = 806;
    private static final int PATH_T_UP = 804;
    private static final int PATH_T_DOWN = 746;
    private static final int PATH_T_LEFT = 747;
    private static final int PATH_T_RIGHT = 803;
    private static final int PATH_DEAD_END_DOWN = 975;
    private static final int PATH_DEAD_END_UP = 974;
    private static final int PATH_DEAD_END_RIGHT = 1032;
    private static final int PATH_DEAD_END_LEFT = 1031;
    private static final int PATH_CROSSROADS = 1033;
    private static final int PATH_HORIZONTAL = 807;
    private static final int PATH_VERTICAL = 750;

    // TODO: Analyze efficiency of rotation vs state calculation

    private int getTextureForPath(int x, int y) {

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
                return PATH_VERTICAL;
            case 0b1010:
                return PATH_HORIZONTAL;
            case 0b0011:
                return PATH_TOP_LEFT;
            case 0b0110:
                return PATH_BOTTOM_LEFT;
            case 0b1100:
                return PATH_BOTTOM_RIGHT;
            case 0b1001:
                return PATH_TOP_RIGHT;
            case 0b1011:
                return PATH_T_DOWN;
            case 0b0111:
                return PATH_T_RIGHT;
            case 0b1110:
                return PATH_T_UP;
            case 0b1101:
                return PATH_T_LEFT;
            case 0b0001:
                return PATH_DEAD_END_DOWN;
            case 0b0010:
                return PATH_DEAD_END_RIGHT;
            case 0b0100:
                return PATH_DEAD_END_UP;
            case 0b1000:
                return PATH_DEAD_END_LEFT;
            case 0b1111:
                return PATH_CROSSROADS;


        }


        return CORRIDOR;
    }

    private static final int ROOM_FLOOR = 920;
    private static final int ROOM_TOP_LEFT = 862;
    private static final int ROOM_TOP_RIGHT = 864;
    private static final int ROOM_BOTTOM_LEFT = 976;
    private static final int ROOM_BOTTOM_RIGHT = 978;
    private static final int ROOM_T_UP = 977;
    private static final int ROOM_T_DOWN = 863;
    private static final int ROOM_T_LEFT = 921;
    private static final int ROOM_T_RIGHT = 919;

    private int getTextureForRoom(int x, int y) {

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
                return ROOM_T_DOWN;
            case 0b0111:
                return ROOM_T_RIGHT;
            case 0b1110:
                return ROOM_T_UP;
            case 0b1101:
                return ROOM_T_LEFT;
            case 0b0011:
                return ROOM_TOP_LEFT;
            case 0b0110:
                return ROOM_BOTTOM_LEFT;
            case 0b1100:
                return ROOM_BOTTOM_RIGHT;
            case 0b1001:
                return ROOM_TOP_RIGHT;
            default:
                return ROOM_FLOOR;

        }
    }

    private void removeDeadEnds() {

        Stack<Point> deadEnds = new Stack<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Point point = new Point(x, y);
                int adjacentWalls = 0;

                if (x - 1 > 0 && walls[x - 1][y] == COLLIDE) {
                    adjacentWalls++;
                }
                if (x + 1 < width && walls[x +  1][y] == COLLIDE) {
                    adjacentWalls++;
                }
                if (y - 1 > 0 && walls[x][y - 1] == COLLIDE) {
                    adjacentWalls++;
                }
                if (y + 1 < height && walls[x][y + 1] == COLLIDE) {
                    adjacentWalls++;
                }

                if (adjacentWalls >= 3) {
                    deadEnds.add(point);
                }

            }
        }

        while (!deadEnds.isEmpty()) {

            Point deadEnd = deadEnds.pop();

            if (walls[deadEnd.x][deadEnd.y] == COLLIDE) {
                continue;
            }
            walls[deadEnd.x][deadEnd.y] = COLLIDE;

            List<Point> adjacent = new ArrayList<>(4);

            if (deadEnd.x - 1 > 0 && walls[deadEnd.x - 1][deadEnd.y] == CORRIDOR_CONNECTED) {
                adjacent.add(new Point(deadEnd.x - 1, deadEnd.y));
            }
            if (deadEnd.x + 1 < width && walls[deadEnd.x + 1][deadEnd.y] == CORRIDOR_CONNECTED) {
                adjacent.add(new Point(deadEnd.x + 1, deadEnd.y));
            }
            if (deadEnd.y - 1 > 0 && walls[deadEnd.x][deadEnd.y - 1] == CORRIDOR_CONNECTED) {
                adjacent.add(new Point(deadEnd.x, deadEnd.y - 1));
            }
            if (deadEnd.y + 1 < height && walls[deadEnd.x][deadEnd.y + 1] == CORRIDOR_CONNECTED) {
                adjacent.add(new Point(deadEnd.x, deadEnd.y + 1));
            }

            for (Point point : adjacent) {

                int adjacentWalls = 0;

                if (point.x - 1 > 0 && walls[point.x - 1][point.y] == COLLIDE) {
                    adjacentWalls++;
                }
                if (point.x + 1 < width && walls[point.x +  1][point.y] == COLLIDE) {
                    adjacentWalls++;
                }
                if (point.y - 1 > 0 && walls[point.x][point.y - 1] == COLLIDE) {
                    adjacentWalls++;
                }
                if (point.y + 1 < height && walls[point.x][point.y + 1] == COLLIDE) {
                    adjacentWalls++;
                }

                if (adjacentWalls >= 3) {
                    deadEnds.push(point);
                }

            }

        }
    }

    public byte[][] getPlayerTrail() {
        return playerTrail;
    }

    public void refreshPlayerTrail(PointF point) {

        for (int x = 0; x < playerTrail.length; x++) {
            for (int y = 0; y < playerTrail.length; y++) {
                if (playerTrail[x][y] > 0) {
                    playerTrail[x][y]--;
                }
            }
        }

        playerTrail[(int) point.x][(int) point.y] = Byte.MAX_VALUE;
    }

    public List<Rect> getRooms() {
        return rooms;
    }

    /*
        Sorta kinda works, but doesn't fit well with the low resolution
        of the rendered screen.
     */
//    public void generateCellular(int mapWidth, int mapHeight) {
//
//        // Add 1 to both dimensions to simplify neighbor checking
//        layout = new boolean[1 + mapWidth + 1][1 + mapHeight + 1];
//        walls = new byte[mapWidth][mapHeight];
//        WorldMap worldMap = new WorldMap(mapWidth, mapHeight);
//        List<Tile> tilesBelow = new ArrayList<>();
//
//        // TODO: Scale repetitions based on map size
//        int repetitions = 7;
//
//        Random random = new Random();
//
//        for (int x = 0; x < layout.length; x++) {
//            layout[x][0] = true;
//            layout[x][layout[0].length - 1] = true;
//        }
//
//        for (int y = 0; y < layout.length; y++) {
//            layout[0][y] = true;
//            layout[layout.length - 1][y] = true;
//        }
//
//        for (int x = 1; x < layout.length - 1; x++) {
//            for (int y = 1; y < layout[0].length - 1; y++) {
//                if (random.nextFloat() < 0.45f) {
//                    layout[x][y] = true;
//                }
//            }
//        }
//
//        for (int repetition = 0; repetition < repetitions; repetition++) {
//
//            boolean[][] newLayout = new boolean[layout.length][layout[0].length];
//
//            for (int x = 0; x < newLayout.length; x++) {
//                newLayout[x][0] = true;
//                newLayout[x][newLayout[0].length - 1] = true;
//            }
//
//            for (int y = 0; y < newLayout.length; y++) {
//                newLayout[0][y] = true;
//                newLayout[newLayout.length - 1][y] = true;
//            }
//
//
//            for (int x = 1; x < layout.length - 1; x++) {
//                for (int y = 1; y < layout[0].length - 1; y++) {
//
//                    int adjacentWalls = 0;
//
//                    for (int offsetX = -1; offsetX <= 1; offsetX++) {
//                        for (int offsetY = -1; offsetY <= 1; offsetY++) {
//                            if (layout[x + offsetX][y + offsetY]) {
//                                adjacentWalls++;
//                            }
//                        }
//                    }
//
//                    if (layout[x][y]) {
//                        adjacentWalls--;
//                    }
//
//                    if (adjacentWalls >= 5 || (repetition < repetitions - 2 && adjacentWalls <= 1)) {
//                        newLayout[x][y] = true;
//                    }
//
//                }
//            }
//
//            layout = newLayout;
//        }
//
//        for (int x = 1; x < layout.length - 1; x++) {
//            for (int y = 1; y < layout[0].length - 1; y++) {
//                if (layout[x][y]) {
//                    walls[x - 1][y - 1] = COLLIDE;
//                    tilesBelow.add(new Tile(new PointF(x - 1, y - 1), WALL));
//                }
//                else {
//                    tilesBelow.add(new Tile(new PointF(x - 1, y - 1), ROOM_FLOOR));
//                }
//
//            }
//        }
//
//        worldMap.setWalls(walls);
//        worldMap.setTilesBelow(tilesBelow);
//
//
//        return worldMap;
//    }

}
