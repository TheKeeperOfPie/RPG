package com.winsonchiu.rpg.maps;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import com.winsonchiu.rpg.Direction;
import com.winsonchiu.rpg.Player;
import com.winsonchiu.rpg.Renderer;
import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.ResourceBronzeBar;
import com.winsonchiu.rpg.items.ResourceSilverBar;
import com.winsonchiu.rpg.items.ResourceSilverCoin;
import com.winsonchiu.rpg.mobs.Mob;
import com.winsonchiu.rpg.mobs.MobMage;
import com.winsonchiu.rpg.mobs.MobSwordsman;
import com.winsonchiu.rpg.tiles.Tile;
import com.winsonchiu.rpg.tiles.TileType;
import com.winsonchiu.rpg.utils.Edge;
import com.winsonchiu.rpg.utils.Graph;
import com.winsonchiu.rpg.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Created by TheKeeperOfPie on 5/25/2015.
 */
public class WorldMapDungeon extends WorldMap {

    private static final String TAG = WorldMapDungeon.class.getCanonicalName();
    private Tile tileChestLeft;
    private Tile tileChestRight;
    private Graph graph;
    private List<Rect> rooms;
    private RectF boundsGoal;
    private RectF boundsStart;
    private Rect goalRoom;
    private Rect roomStart;
    private boolean hasFoundGoal;
    private RectF boundsExit;

    public WorldMapDungeon(int width, int height) {
        super(width, height);
        graph = new Graph();
        rooms = new ArrayList<>();
        roomStart = new Rect();
        boundsGoal = new RectF();
        boundsStart = new RectF();
        boundsExit = new RectF();
    }

    @Override
    public PointF getStartPoint() {
        return new PointF(roomStart.centerX(), roomStart.centerY() - 1);
    }

    public void checkGoal(Renderer renderer) {
        if (hasFoundGoal) {
            if (RectF.intersects(renderer.getPlayer()
                    .getBounds(), boundsExit)) {
                generateRectangularDungeon();
                renderer.loadMap(this, getStartPoint());
            }

            return;
        }
        if (!RectF.intersects(renderer.getPlayer()
                .getBounds(), boundsGoal)) {
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
                goalDrops.add(
                        new ResourceSilverBar(new PointF(goalRoom.centerX(), goalRoom.centerY())));
            }
            else if (random.nextFloat() < 0.2f) {
                goalDrops.add(
                        new ResourceSilverCoin(new PointF(goalRoom.centerX(), goalRoom.centerY())));
            }
            else {
                goalDrops.add(
                        new ResourceBronzeBar(new PointF(goalRoom.centerX(), goalRoom.centerY())));
            }

        }

        dropItems(goalDrops, renderer.getPlayer()
                .getLastDirection(), new PointF(goalRoom.centerX(), goalRoom.centerY()));
        hasFoundGoal = true;
    }

    @Override
    public void setClearColor() {
        GLES20.glClearColor(0.65882352941f, 0.49411764705f, 0.3294117647f, 1f);
    }

    @Override
    public void renderEntities(Renderer renderer,
            float[] matrixProjection,
            float[] matrixView,
            int[] textureNames) {
        super.renderEntities(renderer, matrixProjection, matrixView, textureNames);

        checkGoal(renderer);
    }


    private void setTiles() {
        TileType tileType;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                PointF point = new PointF(x, y);
                switch (walls[x][y]) {

                    case COLLIDE:
                        tilesBelow.add(
                                new Tile(point, tileSet.getTextureForTileType(TileType.WALL)));
                        break;
                    case CORRIDOR_CONNECTED:
                        tilesBelow.add(
                                new Tile(point, tileSet.getTextureForTileType(TileType.GROUND)));
                        tilesBelow.add(new Tile(point, tileSet.getTextureForTileType(
                                getTileTypeForPath(
                                        x, y))));
                        break;
                    case ROOM:
                        tileType = getTileTypeForRoom(x, y);
                        if (tileType != TileType.ROOM_FLOOR) {
                            tilesBelow.add(new Tile(point,
                                    tileSet.getTextureForTileType(TileType.ROOM_GROUND)));
                        }
                        tilesBelow.add(new Tile(point, tileSet.getTextureForTileType(tileType)));
                        break;

                }
            }
        }


        tileChestLeft = new Tile(new PointF(goalRoom.centerX(), goalRoom.centerY()),
                tileSet.getTextureForTileType(TileType.CHEST_LEFT_CLOSED));
        tileChestRight = new Tile(new PointF(goalRoom.centerX() + 1, goalRoom.centerY()),
                tileSet.getTextureForTileType(TileType.CHEST_RIGHT_CLOSED));

        tilesBelow.add(tileChestLeft);
        tilesBelow.add(tileChestRight);
        tilesBelow.add(new Tile(new PointF(roomStart.centerX(), roomStart.centerY() + 1),
                tileSet.getTextureForTileType(TileType.STAIRS_UP_LEFT)));

    }

    @Override
    public void clear() {
        super.clear();
        rooms.clear();
        graph = new Graph();
        hasFoundGoal = false;
    }

    public void generateRectangularDungeon() {

        long startTime = System.currentTimeMillis();
        clear();

        fillWalls();
        generateRooms();
        generateConnections();

        roomStart = rooms.get(0);
        boundsStart = new RectF(roomStart.centerX(), roomStart.centerY() + 1,
                roomStart.centerX() + 1, roomStart.centerY() + 2);

        spawnMobs();

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
        boundsGoal = new RectF(goalRoom.centerX() - 1, goalRoom.centerY() - 1,
                goalRoom.centerX() + 2, goalRoom.centerY() + 1);
        boundsExit = new RectF(goalRoom.centerX(), goalRoom.centerY() + 1, goalRoom.centerX() + 1, goalRoom.centerY() + 2);

        setTiles();

        Log.d(TAG, "Time to generate map: " + (System.currentTimeMillis() - startTime));

    }

    private void spawnMobs() {

        List<Mob> mobs = new ArrayList<>();
        Set<Point> usedPoints = new HashSet<>(4);
        PointF location;
        Point point;
        int attempts = 0;

        for (Rect room : rooms) {
            if (room == roomStart) {
                continue;
            }

            usedPoints.clear();
            int iterations = random.nextInt(3) + 1;

            for (int iteration = 0; iteration < iterations; iteration++) {

                int roomX = random.nextInt(room.width() - 3) + 1;
                int roomY = random.nextInt(room.height() - 3) + 1;

                point = new Point(roomX + room.left, roomY + room.top);
                location = new PointF(roomX + room.left, roomY + room.top);

                // TODO: This is not a good programming practice. Use a better system.
                attempts = 0;
                while (!usedPoints.add(point) && attempts++ < 5) {
                    roomX = random.nextInt(room.width() - 3) + 1;
                    roomY = random.nextInt(room.height() - 3) + 1;

                    point = new Point(roomX + room.left, roomY + room.top);
                    location = new PointF(roomX + room.left, roomY + room.top);
                }

                Mob mob;

                if (random.nextFloat() < 0.3f) {
                    mob = new MobMage(3, 0, 2, location, room, 8);
                }
                else {
                    mob = new MobSwordsman(4, 0, 1, location, room, 8);
                }
                mob.setLastDirection(Direction.getRandomDirectionFourWay());
                mob.calculateAnimationFrame();
                mobs.add(mob);

            }

        }

        addMobs(mobs);

    }


    //region Generation

    private void fillWalls() {
        for (int x = 0; x < walls.length; x++) {
            for (int y = 0; y < walls[0].length; y++) {
                walls[x][y] = COLLIDE;
            }
        }
    }

    private void generateRooms() {
        int numRooms = width * height / AREA_PER_ROOM;
        int maxAttempts = numRooms * ATTEMPT_RATIO;
        int attempt = 0;

        while (rooms.size() < numRooms && attempt++ < maxAttempts) {

            int roomWidth = random.nextInt(MAX_ROOM_WIDTH - MIN_ROOM_WIDTH) + MIN_ROOM_WIDTH;
            int roomHeight = random.nextInt(MAX_ROOM_HEIGHT - MIN_ROOM_HEIGHT) + MIN_ROOM_HEIGHT;

            Point startPoint = new Point((random.nextInt(width - 8) + 4),
                    (random.nextInt(height - 8) + 4));

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

            Rect room = new Rect(startPoint.x, startPoint.y, startPoint.x + roomWidth - 1,
                    startPoint.y + roomHeight - 1);
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

        Set<Edge> roomConnections = MathUtils.createMinimumSpanningTree(graph);
        List<Edge> edges = new ArrayList<>(graph.getEdgeSet());

        for (int iteration = 0; iteration < graph.getConnectedVertices()
                .size() / CYCLE_RATIO; iteration++) {
            roomConnections.add(edges.get(random.nextInt(edges.size())));
        }

        for (Edge edge : roomConnections) {

            Rect roomFirst = rooms.get(edge.getSource());
            Rect roomSecond = rooms.get(edge.getDestination());

            // Randomly carve vertically or horizontally first
            if (random.nextBoolean()) {
                carveLine(new Point(roomFirst.centerX(), roomFirst.centerY()),
                        new Point(roomSecond.centerX(), roomFirst.centerY()), CORRIDOR_CONNECTED);
                carvePoint(new Point(roomSecond.centerX(), roomFirst.centerY()),
                        CORRIDOR_CONNECTED);
                carveLine(new Point(roomSecond.centerX(), roomFirst.centerY()),
                        new Point(roomSecond.centerX(), roomSecond.centerY()), CORRIDOR_CONNECTED);
            }
            else {
                carveLine(new Point(roomFirst.centerX(), roomFirst.centerY()),
                        new Point(roomFirst.centerX(), roomSecond.centerY()), CORRIDOR_CONNECTED);
                carvePoint(new Point(roomFirst.centerX(), roomSecond.centerY()),
                        CORRIDOR_CONNECTED);
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

    public boolean returnToTown(Renderer renderer) {
        return boundsStart.contains(renderer.getPlayer()
                .getLocation().x + Player.WIDTH_RATIO / 2, renderer.getPlayer()
                .getLocation().y + Player.HEIGHT_RATIO / 2);
    }

    public boolean isCleared() {
        return hasFoundGoal;
    }
    //endregion

}
