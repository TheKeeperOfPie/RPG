package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.winsonchiu.rpg.items.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
    private static final int MAX_ROOM_WIDTH = 17;
    private static final int MIN_ROOM_WIDTH = 9;
    private static final int MAX_ROOM_HEIGHT = 17;
    private static final int MIN_ROOM_HEIGHT = 9;
    private static final int AREA_PER_ROOM = 50;
    private static final int ATTEMPT_RATIO = 3;

    private static final int PATH_FLOOR = 920;
    private static final int PATH_TOP_LEFT = 862;
    private static final int PATH_TOP_RIGHT = 864;
    private static final int PATH_BOTTOM_LEFT = 976;
    private static final int PATH_BOTTOM_RIGHT = 978;
    private static final int PATH_T_UP = 977;
    private static final int PATH_T_DOWN = 863;
    private static final int PATH_T_LEFT = 921;
    private static final int PATH_T_RIGHT = 919;
    private static final int PATH_HORIZONTAL = 807;
    private static final int PATH_VERTICAL = 750;

    private static final int ROOM_FLOOR = 920;
    private static final int ROOM_TOP_LEFT = 862;
    private static final int ROOM_TOP_RIGHT = 864;
    private static final int ROOM_BOTTOM_LEFT = 976;
    private static final int ROOM_BOTTOM_RIGHT = 978;
    private static final int ROOM_T_UP = 977;
    private static final int ROOM_T_DOWN = 863;
    private static final int ROOM_T_LEFT = 921;
    private static final int ROOM_T_RIGHT = 919;

    private List<Tile> tilesBelow;
    private List<Tile> tilesAbove;
    private List<Rect> rooms;
    private byte[][] walls;
    private byte[][] playerTrail;
    private final List<Item> items;
    private int width;
    private int height;
    private Random random;
    private boolean[][] itemLocations;
    private Rect roomStart;

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
        items = Collections.synchronizedList(new ArrayList<Item>());
        random = new Random();
        rooms = new ArrayList<>();
        walls = new byte[width][height];
        itemLocations = new boolean[width][height];
        playerTrail = new byte[width][height];
        tilesBelow = new ArrayList<>();
        tilesAbove = new ArrayList<>();
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        itemLocations[(int) item.getLocation().x][(int) item.getLocation().y] = true;
        items.add(item);
    }

    public void addItems(List<Item> newItems) {
        for (Item item : newItems) {
            itemLocations[(int) item.getLocation().x][(int) item.getLocation().y] = true;
            items.add(item);
        }
    }

    public Item getItem(int x, int y) {
        synchronized (items) {
            for (Item item : items) {
                if ((int) item.getLocation().x == x && (int) item.getLocation().y == y) {
                    return item;
                }
            }
        }
        return null;
    }

    public Item removeItem(int x, int y) {

        int indexItem = -1;

        synchronized (items) {
            for (int index = 0; index < items.size(); index++) {

                Item item = items.get(index);

                if ((int) item.getLocation().x == x && (int) item.getLocation().y == y) {
                    indexItem = index;
                    break;
                }
            }

            if (indexItem < 0) {
                return null;
            }
            Item item = items.get(indexItem);

            if (item.getQuantity() <= 1) {
                itemLocations[(int) item.getLocation().x][(int) item.getLocation().y] = false;
                return items.remove(indexItem);
            }
            else {
                return item.decrementQuantity();
            }
        }
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

    public void generateRectangular(Renderer renderer) {

        generateRooms();
        generateMaze();
        connectRooms();

//        removeDeadEnds();

        setTiles();

        roomStart = rooms.get(0);

        spawnMobs(renderer);
        spawnItems();

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

    private void spawnMobs(Renderer renderer) {

        List<Entity> mobs = new ArrayList<>();
        Set<PointF> usedPoints = new HashSet<>(4);
        int attempts = 0;

        for (Rect room : rooms) {
            if (room == roomStart) {
                continue;
            }

            usedPoints.clear();
            int iterations = random.nextInt(3) + 1;

            for (int iteration = 0; iteration < iterations; iteration++) {

                float roomX = random.nextInt(room.width() - 2) + 1;
                float roomY = random.nextInt(room.width() - 2) + 1;

                PointF location = new PointF(roomX + room.left, roomY + room.top);

                // TODO: This is not a good programming practice. Use a better system.
                attempts = 0;
                while (!usedPoints.add(location) && attempts++ < 5) {
                    roomX = random.nextInt(room.width() - 2) + 1;
                    roomY = random.nextInt(room.width() - 2) + 1;

                    location = new PointF(roomX + room.left, roomY + room.top);
                }

                mobs.add(new MobAggressive(5, 0, renderer.getTileSize(), MobAggressive.WIDTH_RATIO, MobAggressive.HEIGHT_RATIO, location, 4f, 4f, room, 8));

            }

        }

        renderer.addEntities(mobs);

    }

    private void spawnItems() {

    }

    //region Generation
    private void generateRooms() {
        int numRooms = width * height / AREA_PER_ROOM;
        int maxAttempts = numRooms * ATTEMPT_RATIO;
        int attempt = 0;

        while (rooms.size() < numRooms && attempt++ < maxAttempts) {

            Log.d(TAG, "Generation attempt: " + attempt);

            // Use of / 4 * 4 to make rooms line up properly

            int roomWidth = (random.nextInt(MAX_ROOM_WIDTH - MIN_ROOM_WIDTH) + MIN_ROOM_WIDTH) / 4 * 4 + 1;
            int roomHeight = (random.nextInt(MAX_ROOM_HEIGHT - MIN_ROOM_HEIGHT) + MIN_ROOM_HEIGHT) / 4 * 4 + 1;

            Point startPoint = new Point((random.nextInt(width - 6) + 3) / 4 * 4, (random.nextInt(height - 6) + 3) / 4 * 4);

            // TODO: Change to check intersection with list of rooms
            boolean isRoomValid = true;
            if (startPoint.x + roomWidth >= width || startPoint.y + roomHeight >= height) {
                isRoomValid = false;
            }
            else {
                for (int x = startPoint.x - 3; x <= startPoint.x + roomWidth + 3; x++) {
                    for (int y = startPoint.y - 3; y <= startPoint.y + roomHeight + 3; y++) {
                        if (x < 0 || x >= width || y < 0 || y >= height || walls[x][y] != COLLIDE) {
                            isRoomValid = false;
                            break;
                        }
                    }
                    if (!isRoomValid) {
                        break;
                    }
                }
            }

            if (!isRoomValid) {
                continue;
            }
            for (int x = startPoint.x; x < startPoint.x + roomWidth; x++) {
                for (int y = startPoint.y; y < startPoint.y + roomHeight; y++) {
                    walls[x][y] = ROOM;
                }
            }

            Rect room = new Rect(startPoint.x, startPoint.y, startPoint.x + roomWidth - 1, startPoint.y + roomHeight - 1);
            rooms.add(room);

        }

        numRooms = width * height / AREA_PER_ROOM;
        maxAttempts = numRooms * ATTEMPT_RATIO;
        attempt = 0;

        while (rooms.size() < numRooms && attempt++ < maxAttempts) {

            Log.d(TAG, "Generation attempt: " + attempt);

            // Use of / 4 * 4 to make rooms line up properly

            int roomWidth = (9);
            int roomHeight = (9);

            Point startPoint = new Point((random.nextInt(width - 6) + 3) / 4 * 4, (random.nextInt(height - 6) + 3) / 4 * 4);

            // TODO: Change to check intersection with list of rooms
            boolean isRoomValid = true;
            if (startPoint.x + roomWidth >= width || startPoint.y + roomHeight >= height) {
                isRoomValid = false;
            }
            else {
                for (int x = startPoint.x - 3; x <= startPoint.x + roomWidth + 3; x++) {
                    for (int y = startPoint.y - 3; y <= startPoint.y + roomHeight + 3; y++) {
                        if (x < 0 || x >= width || y < 0 || y >= height || walls[x][y] != COLLIDE) {
                            isRoomValid = false;
                            break;
                        }
                    }
                    if (!isRoomValid) {
                        break;
                    }
                }
            }

            if (!isRoomValid) {
                continue;
            }
            for (int x = startPoint.x; x < startPoint.x + roomWidth; x++) {
                for (int y = startPoint.y; y < startPoint.y + roomHeight; y++) {
                    walls[x][y] = ROOM;
                }
            }

            Rect room = new Rect(startPoint.x, startPoint.y, startPoint.x + roomWidth - 1, startPoint.y + roomHeight - 1);
            rooms.add(room);

        }
    }

    private void generateMaze() {
        for (int x = 2; x < width - 2; x += 4) {
            for (int y = 2; y < height - 2; y += 4) {
                if (walls[x][y] == COLLIDE &&
                        walls[x - 1][y] == COLLIDE &&
                        walls[x + 1][y] == COLLIDE &&
                        walls[x][y - 1] == COLLIDE &&
                        walls[x][y + 1] == COLLIDE &&
                        walls[x - 1][y - 1] == COLLIDE &&
                        walls[x + 1][y - 1] == COLLIDE &&
                        walls[x - 1][y + 1] == COLLIDE &&
                        walls[x + 1][y + 1] == COLLIDE) {

                    Stack<Point> points = new Stack<>();
                    points.add(new Point(x, y));

                    int lastOffsetX = 0;
                    int lastOffsetY = 0;

                    while (!points.isEmpty()) {

                        Point point = points.pop();

                        List<Point> adjacentWalls = new ArrayList<>();

                        if (point.x - 4 > 0 && walls[point.x - 4][point.y] == COLLIDE && walls[point.x - 4][point.y - 1] == COLLIDE && walls[point.x - 4][point.y + 1] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x - 4, point.y));
                        }
                        if (point.x + 4 < width - 4 && walls[point.x + 4][point.y] == COLLIDE && walls[point.x + 4][point.y - 1] == COLLIDE && walls[point.x + 4][point.y + 1] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x + 4, point.y));
                        }
                        if (point.y - 4 > 0 && walls[point.x][point.y - 4] == COLLIDE && walls[point.x - 1][point.y - 4] == COLLIDE && walls[point.x + 1][point.y - 4] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x, point.y - 4));
                        }
                        if (point.y + 4 < height - 4 && walls[point.x][point.y + 4] == COLLIDE && walls[point.x - 1][point.y + 4] == COLLIDE && walls[point.x + 1][point.y + 4] == COLLIDE) {
                            adjacentWalls.add(new Point(point.x, point.y + 4));
                        }
                        carveCorridor(point);

                        if (!adjacentWalls.isEmpty()) {
                            // Try cutting maze in same direction
                            Point nextPoint = new Point(point.x + lastOffsetX, point.y + lastOffsetY);

                            if (random.nextFloat() < CHANCE_TO_CURVE_MAZE || !adjacentWalls.contains(nextPoint)) {
                                nextPoint = adjacentWalls.get(random.nextInt(adjacentWalls.size()));
                            }

                            lastOffsetX = nextPoint.x - point.x;
                            lastOffsetY = nextPoint.y - point.y;

                            if (nextPoint.x != point.x) {
                                carveCorridor(new Point(nextPoint.x < point.x ? nextPoint.x + 2 : nextPoint.x - 2, nextPoint.y));
                            }

                            if (nextPoint.y != point.y) {
                                carveCorridor(new Point(nextPoint.x, nextPoint.y < point.y ? nextPoint.y + 2 : nextPoint.y - 2));
                            }

                            points.add(nextPoint);

                        }

                    }
                }
            }
        }
    }

    private void carveCorridor(Point point) {
        walls[point.x][point.y] = CORRIDOR_DISCONNECTED;
        walls[point.x][point.y - 1] = CORRIDOR_DISCONNECTED;
        walls[point.x][point.y + 1] = CORRIDOR_DISCONNECTED;
        walls[point.x - 1][point.y] = CORRIDOR_DISCONNECTED;
        walls[point.x + 1][point.y] = CORRIDOR_DISCONNECTED;
        walls[point.x - 1][point.y - 1] = CORRIDOR_DISCONNECTED;
        walls[point.x - 1][point.y + 1] = CORRIDOR_DISCONNECTED;
        walls[point.x + 1][point.y - 1] = CORRIDOR_DISCONNECTED;
        walls[point.x + 1][point.y + 1] = CORRIDOR_DISCONNECTED;
    }

    private void connectRooms() {
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

            if (room.top - 2 > 0) {
                for (int x = room.left + 2; x < room.right - 2; x++) {
                    if (walls[x][room.top - 1] != COLLIDE &&
                            walls[x - 1][room.top - 1] != COLLIDE &&
                            walls[x + 1][room.top - 1] != COLLIDE) {
                        connections.add(new Point(x, room.top - 1));
                    }
                }
            }
            if (room.bottom + 2 < height) {
                for (int x = room.left + 2; x < room.right - 2; x++) {
                    if (walls[x][room.bottom + 1] != COLLIDE &&
                            walls[x - 1][room.bottom + 1] != COLLIDE &&
                            walls[x + 1][room.bottom + 1] != COLLIDE) {
                        connections.add(new Point(x, room.bottom + 1));
                    }
                }
            }
            if (room.left - 2 > 0) {
                for (int y = room.top + 2; y < room.bottom - 2; y++) {
                    if (walls[room.left - 1][y] != COLLIDE &&
                            walls[room.left - 1][y - 1] != COLLIDE &&
                            walls[room.left - 1][y + 1] != COLLIDE) {
                        connections.add(new Point(room.left - 1, y));
                    }
                }
            }
            if (room.right + 2 < width) {
                for (int y = room.top + 2; y < room.bottom - 2; y++) {
                    if (walls[room.right + 1][y] != COLLIDE &&
                            walls[room.right + 1][y - 1] != COLLIDE &&
                            walls[room.right + 1][y + 1] != COLLIDE) {
                        connections.add(new Point(room.right + 1, y));
                    }
                }
            }

            while (!connections.isEmpty()) {

                Point pointStart = connections.get(random.nextInt(connections.size()));
                List<Point> cutPoints = new ArrayList<>();

                if (pointStart.x < room.left) {
                    cutPoints.add(new Point(pointStart.x + 1, pointStart.y));
                    cutPoints.add(new Point(pointStart.x + 1, pointStart.y - 1));
                    cutPoints.add(new Point(pointStart.x + 1, pointStart.y + 1));
                }
                if (pointStart.x > room.right) {
                    cutPoints.add(new Point(pointStart.x - 1, pointStart.y));
                    cutPoints.add(new Point(pointStart.x - 1, pointStart.y - 1));
                    cutPoints.add(new Point(pointStart.x - 1, pointStart.y + 1));
                }
                if (pointStart.y < room.top) {
                    cutPoints.add(new Point(pointStart.x, pointStart.y + 1));
                    cutPoints.add(new Point(pointStart.x - 1, pointStart.y + 1));
                    cutPoints.add(new Point(pointStart.x + 1, pointStart.y + 1));
                }
                if (pointStart.y > room.bottom) {
                    cutPoints.add(new Point(pointStart.x, pointStart.y - 1));
                    cutPoints.add(new Point(pointStart.x - 1, pointStart.y - 1));
                    cutPoints.add(new Point(pointStart.x + 1, pointStart.y - 1));
                }

                for (Point point : cutPoints) {
                    walls[point.x][point.y] = CORRIDOR_CONNECTED;
                }

                // Fill corridor
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

            room.inset(1, 1);

        }
    }

    // TODO: Reimplement dead end checks
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
    //endregion

    //region Getters and setters
    public PointF getStartPoint() {
        return new PointF(roomStart.centerX(), roomStart.centerY());
    }

    private void setTiles() {
        tilesBelow.clear();
        tilesAbove.clear();

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
            case 0b1111:
                return PATH_FLOOR;


        }


        return CORRIDOR;
    }

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

    public byte[][] getPlayerTrail() {
        return playerTrail;
    }

    public List<Rect> getRooms() {
        return rooms;
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
    //endregion

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
}
