package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.ResourceBronzeBar;
import com.winsonchiu.rpg.items.ResourceBronzeCoin;
import com.winsonchiu.rpg.items.ResourceSilverCoin;
import com.winsonchiu.rpg.mobs.Mob;
import com.winsonchiu.rpg.mobs.MobAggressive;
import com.winsonchiu.rpg.mobs.MobMage;
import com.winsonchiu.rpg.mobs.MobSwordsman;
import com.winsonchiu.rpg.tiles.Tile;
import com.winsonchiu.rpg.tiles.TileSet;
import com.winsonchiu.rpg.tiles.TileSetDungeon;
import com.winsonchiu.rpg.tiles.TileType;
import com.winsonchiu.rpg.utils.Edge;
import com.winsonchiu.rpg.utils.Graph;
import com.winsonchiu.rpg.utils.MathUtils;

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

    public static final byte COLLIDE = 1;
    public static final byte FLOOR = 2;
    public static final byte ROOM = 3;
    public static final byte DOORWAY = 4;
    public static final byte CORRIDOR_DISCONNECTED = 5;
    public static final byte CORRIDOR_CONNECTED = 6;

    private static final String TAG = WorldMap.class.getCanonicalName();
    private static final float CHANCE_TO_CURVE_MAZE = 0.6f;

    private static final String IS_DOORWAY = "Doorway";
    private static final String IS_COLLIDE = "Collide";
    private static final String IS_ABOVE = "Above";
    private static final int MAX_ROOM_WIDTH = 11;
    private static final int MIN_ROOM_WIDTH = 5;
    private static final int MAX_ROOM_HEIGHT = 11;
    private static final int MIN_ROOM_HEIGHT = 5;
    private static final int AREA_PER_ROOM = 50;
    private static final int ATTEMPT_RATIO = 5;
    private static final int CYCLE_RATIO = 10;

    private List<Tile> tilesBelow;
    private List<Tile> tilesAbove;
    private List<Rect> rooms;
    private Graph graph;
    private byte[][] walls;
    private byte[][] playerTrail;
    private final List<Item> items;
    private int width;
    private int height;
    private Random random;
    private boolean[][] itemLocations;
    private Rect roomStart;
    private Set<Edge> roomConnections;
    private RectF boundsGoal;
    private Rect goalRoom;
    private Tile tileChestLeft;
    private Tile tileChestRight;
    private boolean hasFoundGoal;
    private TileSet tileSet;
    private final List<Mob> entityMobs;
    private final List<Attack> entityAttacks;
    private final List<Entity> entities;
    private RectF boundsStart;

    public WorldMap(int width, int height) {

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
        graph = new Graph();
        tileSet = new TileSetDungeon();
        roomStart = new Rect();
        boundsGoal = new RectF();
        boundsStart = new RectF();
        entities = Collections.synchronizedList(new ArrayList<Entity>());
        entityMobs = Collections.synchronizedList(new ArrayList<Mob>());
        entityAttacks = Collections.synchronizedList(new ArrayList<Attack>());
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
            } else {
                return item.decrementQuantity();
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

    public void generateRectangularDungeon(Renderer renderer) {

        long startTime = System.currentTimeMillis();

        fillWalls();
        generateRooms();
        generateConnections();

        roomStart = rooms.get(0);
        boundsStart = new RectF(roomStart.centerX(), roomStart.centerY(), roomStart.centerX() + 1, roomStart.centerY() + 1);

        spawnMobs(renderer);
        spawnItems();

        int currentMaxCost = 0;
        int furtherRoomIndex = 0;
        List<Edge> edges = new ArrayList<>(graph.getEdgeSet());
        for (Edge edge : edges) {
            if (edge.getCost() > currentMaxCost) {
                if (edge.getSource() == 0) {
                    currentMaxCost = edge.getCost();
                    furtherRoomIndex = edge.getDestination();
                }
                else if (edge.getDestination() == 0) {
                    currentMaxCost = edge.getCost();
                    furtherRoomIndex = edge.getSource();
                }
            }
        }

        goalRoom = rooms.get(furtherRoomIndex);
        boundsGoal = new RectF(goalRoom.centerX() - 1, goalRoom.centerY() - 1, goalRoom.centerX() + 2, goalRoom.centerY() + 1);

        setTiles();

        Log.d(TAG, "Time to generate map: " + (System.currentTimeMillis() - startTime));

    }

    private void fillWalls() {
        for (int x = 0; x < walls.length; x++) {
            for (int y = 0; y < walls[0].length; y++) {
                walls[x][y] = COLLIDE;
            }
        }
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

    private void spawnMobs(Renderer renderer) {

        List<Mob> mobs = new ArrayList<>();
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
                float roomY = random.nextInt(room.height() - 2) + 1;

                PointF location = new PointF(roomX + room.left, roomY + room.top);

                // TODO: This is not a good programming practice. Use a better system.
                attempts = 0;
                while (!usedPoints.add(location) && attempts++ < 5) {
                    roomX = random.nextInt(room.width() - 2) + 1;
                    roomY = random.nextInt(room.height() - 2) + 1;

                    location = new PointF(roomX + room.left, roomY + room.top);
                }

                Mob mob;

                if (random.nextFloat() < 0.3f) {
                    mob = new MobMage(2, 0, 2, MobAggressive.WIDTH_RATIO, MobAggressive.HEIGHT_RATIO, location, 4f, 4f,
                            room, 8);
                }
                else {
                    mob = new MobSwordsman(4, 0, 1, MobAggressive.WIDTH_RATIO, MobAggressive.HEIGHT_RATIO, location, 4f, 4f,
                            room, 8);
                }
                mob.setLastDirection(Direction.getRandomDirection());
                mob.calculateAnimationFrame();
                mobs.add(mob);

            }

        }

        addMobs(mobs);

    }

    private void spawnItems() {

    }

    //region Generation
    private void generateRooms() {
        int numRooms = width * height / AREA_PER_ROOM;
        int maxAttempts = numRooms * ATTEMPT_RATIO;
        int attempt = 0;

        while (rooms.size() < numRooms && attempt++ < maxAttempts) {

            int roomWidth = random.nextInt(MAX_ROOM_WIDTH - MIN_ROOM_WIDTH) + MIN_ROOM_WIDTH;
            int roomHeight = random.nextInt(MAX_ROOM_HEIGHT - MIN_ROOM_HEIGHT) + MIN_ROOM_HEIGHT;

            Point startPoint = new Point((random.nextInt(width - 8) + 4), (random.nextInt(height - 8) + 4));

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

    private void generateConnections() {

        for (int row = 0; row < rooms.size(); row++) {
            for (int col = 0; col < row; col++) {

                Rect first = rooms.get(row);
                Rect second = rooms.get(col);

                graph.addEdge(row, col,
                        (int) MathUtils.distance(first.centerX(),
                                first.centerY(),
                                second.centerX(),
                                second.centerY()));

            }

        }

        roomConnections = MathUtils.createMinimumSpanningTree(graph);
        List<Edge> edges = new ArrayList<>(graph.getEdgeSet());

        for (int iteration = 0; iteration < graph.getConnectedVertices().size() / CYCLE_RATIO; iteration++) {
            roomConnections.add(edges.get(random.nextInt(edges.size())));
        }

        for (Edge edge : roomConnections) {

            Rect roomFirst = rooms.get(edge.getSource());
            Rect roomSecond = rooms.get(edge.getDestination());

            // Randomly carve vertically or horizontally first
            if (random.nextBoolean()) {
                carveLine(new Point(roomFirst.centerX(), roomFirst.centerY()),
                        new Point(roomSecond.centerX(), roomFirst.centerY()), CORRIDOR_CONNECTED);
                carvePoint(new Point(roomSecond.centerX(), roomFirst.centerY()), CORRIDOR_CONNECTED);
                carveLine(new Point(roomSecond.centerX(), roomFirst.centerY()),
                        new Point(roomSecond.centerX(), roomSecond.centerY()), CORRIDOR_CONNECTED);
            }
            else {
                carveLine(new Point(roomFirst.centerX(), roomFirst.centerY()),
                        new Point(roomFirst.centerX(), roomSecond.centerY()), CORRIDOR_CONNECTED);
                carvePoint(new Point(roomFirst.centerX(), roomSecond.centerY()), CORRIDOR_CONNECTED);
                carveLine(new Point(roomFirst.centerX(), roomSecond.centerY()),
                        new Point(roomSecond.centerX(), roomSecond.centerY()), CORRIDOR_CONNECTED);
            }

        }

    }

    private void carveLine(Point source, Point target, byte type) {

        int start;
        int end;
        if (source.x == target.x) {
            if (source.y < target.y) {
                start = source.y;
                end = target.y;
            }
            else {
                start = target.y;
                end = source.y;
            }

            for (int y = start; y <= end; y++) {
                walls[source.x - 1][y] = type;
                walls[source.x][y] = type;
                walls[source.x + 1][y] = type;
            }
        }
        else if (source.y == target.y) {
            if (source.x < target.x) {
                start = source.x;
                end = target.x;
            }
            else {
                start = target.x;
                end = source.x;
            }
            for (int x = start; x <= end; x++) {
                walls[x][source.y - 1] = type;
                walls[x][source.y] = type;
                walls[x][source.y + 1] = type;
            }
        }
        else {
            throw new IllegalArgumentException("Diagonal lines are not supported");
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
                        carvePoint(point, CORRIDOR_DISCONNECTED);

                        if (!adjacentWalls.isEmpty()) {
                            // Try cutting maze in same direction
                            Point nextPoint = new Point(point.x + lastOffsetX, point.y + lastOffsetY);

                            if (random.nextFloat() < CHANCE_TO_CURVE_MAZE || !adjacentWalls.contains(nextPoint)) {
                                nextPoint = adjacentWalls.get(random.nextInt(adjacentWalls.size()));
                            }

                            lastOffsetX = nextPoint.x - point.x;
                            lastOffsetY = nextPoint.y - point.y;

                            if (nextPoint.x != point.x) {
                                carvePoint(new Point(
                                        nextPoint.x < point.x ? nextPoint.x + 2 : nextPoint.x - 2,
                                        nextPoint.y), CORRIDOR_DISCONNECTED);
                            }

                            if (nextPoint.y != point.y) {
                                carvePoint(new Point(nextPoint.x,
                                                nextPoint.y < point.y ? nextPoint.y + 2 :
                                                        nextPoint.y - 2),
                                        CORRIDOR_DISCONNECTED);
                            }

                            points.add(nextPoint);

                        }

                    }
                }
            }
        }
    }

    private void carvePoint(Point point, byte type) {
        walls[point.x][point.y] = type;
        walls[point.x][point.y - 1] = type;
        walls[point.x][point.y + 1] = type;
        walls[point.x - 1][point.y] = type;
        walls[point.x + 1][point.y] = type;
        walls[point.x - 1][point.y - 1] = type;
        walls[point.x - 1][point.y + 1] = type;
        walls[point.x + 1][point.y - 1] = type;
        walls[point.x + 1][point.y + 1] = type;
    }

    private void connectRooms() {
        for (Rect room : rooms) {

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
                connections.clear();
                for (int x = room.left + 2; x <= room.right - 2; x++) {
                    if (walls[x][room.top - 2] != COLLIDE &&
                            walls[x - 1][room.top - 2] != COLLIDE &&
                            walls[x + 1][room.top - 2] != COLLIDE) {
                        connections.add(new Point(x, room.top - 1));
                    }
                }
                cutWall(connections, room);
            }
            if (room.bottom + 2 < height) {
                connections.clear();
                for (int x = room.left + 2; x <= room.right - 2; x++) {
                    if (walls[x][room.bottom + 2] != COLLIDE &&
                            walls[x - 1][room.bottom + 2] != COLLIDE &&
                            walls[x + 1][room.bottom + 2] != COLLIDE) {
                        connections.add(new Point(x, room.bottom + 1));
                    }
                }
                cutWall(connections, room);
            }
            if (room.left - 2 > 0) {
                connections.clear();
                for (int y = room.top + 2; y <= room.bottom - 2; y++) {
                    if (walls[room.left - 2][y] != COLLIDE &&
                            walls[room.left - 2][y - 1] != COLLIDE &&
                            walls[room.left - 2][y + 1] != COLLIDE) {
                        connections.add(new Point(room.left - 1, y));
                    }
                }
                cutWall(connections, room);
            }
            if (room.right + 2 < width) {
                connections.clear();
                for (int y = room.top + 2; y <= room.bottom - 2; y++) {
                    if (walls[room.right + 1][y] != COLLIDE &&
                            walls[room.right + 2][y - 1] != COLLIDE &&
                            walls[room.right + 2][y + 1] != COLLIDE) {
                        connections.add(new Point(room.right + 1, y));
                    }
                }
                cutWall(connections, room);
            }

        }
    }

    private void cutWall(List<Point> connections, Rect room) {

        if (connections.isEmpty()) {
            return;
        }

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

    }
    //endregion

    //region Getters and setters
    public PointF getStartPoint() {
        return new PointF(roomStart.centerX(), roomStart.centerY() - 1);
    }

    private void setTiles() {
        tilesBelow.clear();
        tilesAbove.clear();

        TileType tileType;

        tileChestLeft = new Tile(new PointF(goalRoom.centerX(), goalRoom.centerY()),
                tileSet.getTextureForTileType(TileType.CHEST_LEFT_CLOSED));
        tileChestRight = new Tile(new PointF(goalRoom.centerX() + 1, goalRoom.centerY()),
                tileSet.getTextureForTileType(TileType.CHEST_RIGHT_CLOSED));

        tilesBelow.add(tileChestLeft);
        tilesBelow.add(tileChestRight);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                PointF point = new PointF(x, y);
                switch (walls[x][y]) {

                    case COLLIDE:
                        tilesBelow.add(new Tile(point, tileSet.getTextureForTileType(TileType.WALL)));
                        break;
                    case CORRIDOR_CONNECTED:
                        tilesBelow.add(new Tile(point, tileSet.getTextureForTileType(TileType.GROUND)));
                        tilesBelow.add(new Tile(point, tileSet.getTextureForTileType(
                                getTileTypeForPath(
                                        x, y))));
                        break;
                    case ROOM:
                        tileType = getTileTypeForRoom(x, y);
                        if (tileType != TileType.ROOM_FLOOR) {
                            tilesBelow.add(new Tile(point, tileSet.getTextureForTileType(TileType.ROOM_GROUND)));
                        }
                        tilesBelow.add(new Tile(point, tileSet.getTextureForTileType(tileType)));
                        break;

                }
            }
        }

        tilesBelow.add(new Tile(new PointF(roomStart.centerX(), roomStart.centerY() + 1), tileSet.getTextureForTileType(TileType.STAIRS_UP_LEFT)));

    }

    // TODO: Analyze efficiency of rotation vs bitmask calculation

    private TileType getTileTypeForPath(int x, int y) {

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

    private TileType getTileTypeForRoom(int x, int y) {

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

            byte type = -1;

            if (layer.getString("name").contains(IS_DOORWAY)) {
                type = DOORWAY;
            } else if (layer.getString("name").contains(IS_COLLIDE)) {
                type = COLLIDE;
            }

            boolean isAbove = layer.getString("name").contains(IS_ABOVE);

            for (int y = 0; y < width; y++) {
                for (int x = 0; x < height; x++) {
                    int value = data.getInt(position++) - 1;

                    if (value > 0) {
                        if (isAbove) {
                            tilesAbove.add(new Tile(new PointF(x, height - 1 - y), value));
                        } else {
                            tilesBelow.add(new Tile(new PointF(x, height - 1 - y), value));
                        }
                        if (type > -1) {
                            walls[x][height - 1 - y] = type;
                        }
                        else {
                            walls[x][height - 1 - y] = ROOM;
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

    public RectF getBoundsGoal() {
        return boundsGoal;
    }

    public void setBoundsGoal(RectF boundsGoal) {
        this.boundsGoal = boundsGoal;
    }

    public void activateGoal(Renderer renderer) {
        if (hasFoundGoal) {
            return;
        }
        tileChestLeft.setTextureId(tileSet.getTextureForTileType(TileType.CHEST_LEFT_OPEN));
        tileChestRight.setTextureId(tileSet.getTextureForTileType(TileType.CHEST_RIGHT_OPEN));
        tilesBelow.add(new Tile(new PointF(goalRoom.centerX(), goalRoom.centerY() + 1),
                tileSet.getTextureForTileType(TileType.STAIRS_DOWN_RIGHT)));
        renderer.loadVbo(this);

        List<Item> goalDrops = new ArrayList<>();
        int numDrops = random.nextInt(4) + 3;
        for (int num = 0; num < numDrops; num++) {

            if (random.nextFloat() < 0.03f) {
                goalDrops.add(new ResourceSilverCoin(new PointF(goalRoom.centerX(), goalRoom.centerY())));
            }
            else if (random.nextFloat() < 0.2f) {
                goalDrops.add(new ResourceBronzeBar(new PointF(goalRoom.centerX(), goalRoom.centerY())));
            }
            else {
                goalDrops.add(new ResourceBronzeCoin(new PointF(goalRoom.centerX(), goalRoom.centerY())));
            }

        }

        dropItems(goalDrops, renderer.getPlayer().getLastDirection(), new PointF(goalRoom.centerX(), goalRoom.centerY()));
        hasFoundGoal = true;
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

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[Renderer.TEXTURE_PLAYER]);

        player.render(renderer, matrixProjection, matrixView);

        if (RectF.intersects(player.getBounds(), getBoundsGoal())) {
            activateGoal(renderer);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[Renderer.TEXTURE_MOBS]);

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

    public void setBoundsStart(RectF bounds) {
        boundsStart = bounds;
    }

    public RectF getBoundsStart() {
        return boundsStart;
    }
}
