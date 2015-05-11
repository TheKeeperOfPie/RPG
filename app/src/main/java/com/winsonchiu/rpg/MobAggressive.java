package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by TheKeeperOfPie on 5/5/2015.
 */
public class MobAggressive extends Entity {

    private static final String TAG = MobAggressive.class.getCanonicalName();
    private static final float SPEED = 0.004f;
    public static final float WIDTH_RATIO = 0.59999999999f;
    public static final float HEIGHT_RATIO = 0.9f;
    private RectF homeRoom;
    private int searchRadius;
    private boolean isAlerted;
    private boolean isMoving;
    private Point nextLocation;
    private long movingStartTime;
    private Point oldLocation;

    public MobAggressive(int tileSize, float widthRatio, float heightRatio, PointF location, int textureName, float textureRowCount, float textureColCount, Rect room, int searchRadius) {
        super(tileSize, WIDTH_RATIO, HEIGHT_RATIO, location, textureName, textureRowCount,
              textureColCount,
              SPEED);
        this.homeRoom = new RectF(room.left, room.top, room.right, room.bottom);
        this.searchRadius = searchRadius;
        oldLocation = new Point((int) location.x, (int) location.y);
        nextLocation = oldLocation;
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        calculateNextPosition(renderer, matrixProjection, matrixView);

        super.render(renderer, matrixProjection, matrixView);
    }

    private void calculateNextPosition(Renderer renderer,
                                       float[] matrixProjection,
                                       float[] matrixView) {

        Player player = renderer.getPlayer();
        WorldMap worldMap = renderer.getWorldMap();
        byte[][] playerTrail = worldMap.getPlayerTrail();
        byte[][] walls = worldMap.getWalls();

        Point startPoint = new Point((int) getLocation().x, (int) getLocation().y);
        int targetX = (int) player.getLocation().x;
        int targetY = (int) player.getLocation().y;
        if ((int) (player.getLocation().x + player.getWidthRatio()) >= (int) (player.getLocation().x) + 1) {
            targetX++;
        }
        if ((int) (player.getLocation().y + player.getHeightRatio()) >= (int) (player.getLocation().y) + 1) {
            targetY++;
        }

        if (!isAlerted) {
            Point target = new Point(targetX, targetY);

            Map<Point, Point> parentMap = new HashMap<>();
            parentMap.put(startPoint, startPoint);

            Queue<Point> queue = new LinkedList<>();
            queue.offer(startPoint);

            final PointF playerLocation = player.getLocation();

            while (!queue.isEmpty()) {

                Point point = queue.element();

                if (point.x < startPoint.x - searchRadius || point.x > startPoint.x + searchRadius || point.y < startPoint.y - searchRadius || point.y > startPoint.y + searchRadius) {
                    break;
                }

                if (target.equals(point)) {
                    isAlerted = true;
                    break;
                }

                queue.remove();

                List<Point> validAdjacent = new ArrayList<>();

                boolean canMoveLeft = point.x - 1 > 0 && (walls[point.x - 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x - 1, point.y);
                boolean canMoveRight = point.x + 1 < worldMap.getWidth() && (walls[point.x + 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x + 1, point.y);
                boolean canMoveUp = point.y - 1 > 0 && (walls[point.x][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y - 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x, point.y - 1);
                boolean canMoveDown = point.y + 1 < worldMap.getHeight() && (walls[point.x][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y + 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x, point.y + 1);

                if (canMoveLeft) {
                    validAdjacent.add(new Point(point.x - 1, point.y));
                    if (canMoveDown && (walls[point.x - 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
                            worldMap, player, point.x - 1, point.y - 1)) {
                        validAdjacent.add(new Point(point.x - 1, point.y - 1));
                    }
                    if (canMoveUp && (walls[point.x - 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
                            worldMap, player, point.x - 1, point.y + 1)) {
                        validAdjacent.add(new Point(point.x - 1, point.y + 1));
                    }
                }
                if (canMoveRight) {
                    validAdjacent.add(new Point(point.x + 1, point.y));
                    if (canMoveDown && (walls[point.x + 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
                            worldMap, player, point.x + 1, point.y - 1)) {
                        validAdjacent.add(new Point(point.x + 1, point.y - 1));
                    }
                    if (canMoveUp && (walls[point.x + 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
                            worldMap, player, point.x + 1, point.y + 1)) {
                        validAdjacent.add(new Point(point.x + 1, point.y + 1));
                    }
                }
                if (canMoveUp) {
                    validAdjacent.add(new Point(point.x, point.y - 1));
                }
                if (canMoveDown) {
                    validAdjacent.add(new Point(point.x, point.y + 1));
                }

                validAdjacent.removeAll(parentMap.keySet());

                Collections.sort(validAdjacent, new Comparator<Point>() {
                    @Override
                    public int compare(Point lhs, Point rhs) {
                        return Double.compare(Math.sqrt(Math.pow(lhs.x - playerLocation.x, 2) + Math.pow(lhs.y - playerLocation.y, 2)), Math.sqrt(Math.pow(rhs.x - playerLocation.x, 2) + Math.pow(rhs.y - playerLocation.y, 2)));
                    }
                });

                for (Point adjacent : validAdjacent) {
                    queue.offer(adjacent);
                    parentMap.put(adjacent, point);
                }

            }
        }

        if (movingStartTime > 0) {
            long time = System.currentTimeMillis();
            if (time > movingStartTime + 250) {
                movingStartTime = 0;
                getLocation().set(nextLocation.x + (1f - getWidthRatio()) / 2f, nextLocation.y + (1f - getHeightRatio()) / 2f);
                return;
            }

            float ratio = (time - movingStartTime) / 250f;
            getLocation().set(
                    (nextLocation.x - oldLocation.x) * ratio + oldLocation.x + (1f - getWidthRatio()) / 2f,
                    (nextLocation.y - oldLocation.y) * ratio + oldLocation.y + (1f - getHeightRatio()) / 2f);

        }
        else if (isAlerted) {

            Point target = new Point(targetX, targetY);

            movingStartTime = calculateAStar(target, worldMap, startPoint, walls, player, renderer, matrixProjection, matrixView);

            if (movingStartTime <= 0) {
                movingStartTime = calculateAStar(worldMap, startPoint, walls, player, renderer, matrixProjection, matrixView);
            }

        }
        else {
            Point target = new Point((int) homeRoom.centerX(), (int) homeRoom.centerY());

            movingStartTime = calculateAStar(target, worldMap, startPoint, walls, player, renderer, matrixProjection, matrixView);
        }

        if (movingStartTime <= 0) {
            isAlerted = false;
        }

    }

    private long calculateAStar(Point target,
                                WorldMap worldMap,
                                Point startPoint,
                                byte[][] walls,
                                Player player,
                                Renderer renderer, float[] matrixProjection, float[] matrixView) {

        Map<Point, Point> parentMap = new HashMap<>();
        parentMap.put(startPoint, startPoint);

        Queue<Point> queue = new LinkedList<>();
        queue.offer(startPoint);

        final PointF playerLocation = player.getLocation();

        Point end = new Point(startPoint.x, startPoint.y);

        while (!queue.isEmpty()) {

            Point point = queue.element();

            if (!homeRoom.contains(getLocation().x, getLocation().y) && isAlerted) {
                if (point.x < startPoint.x - searchRadius || point.x > startPoint.x + searchRadius || point.y < startPoint.y - searchRadius || point.y > startPoint.y + searchRadius) {
                    return 0;
                }
            }

            if (target.equals(point)) {
                end = queue.remove();
                break;
            }

            queue.remove();

            List<Point> validAdjacent = new ArrayList<>();

            boolean canMoveLeft = point.x - 1 > 0 && (walls[point.x - 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x - 1, point.y);
            boolean canMoveRight = point.x + 1 < worldMap.getWidth() && (walls[point.x + 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x + 1, point.y);
            boolean canMoveUp = point.y - 1 > 0 && (walls[point.x][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y - 1] == WorldMap.ROOM) && !hasMob(
                    worldMap, player, point.x, point.y - 1);
            boolean canMoveDown = point.y + 1 < worldMap.getHeight() && (walls[point.x][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y + 1] == WorldMap.ROOM) && !hasMob(
                    worldMap, player, point.x, point.y + 1);

            if (canMoveLeft) {
                validAdjacent.add(new Point(point.x - 1, point.y));
//                if (canMoveDown && (walls[point.x - 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x - 1, point.y - 1)) {
//                    validAdjacent.add(new Point(point.x - 1, point.y - 1));
//                }
//                if (canMoveUp && (walls[point.x - 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x - 1, point.y + 1)) {
//                    validAdjacent.add(new Point(point.x - 1, point.y + 1));
//                }
            }
            if (canMoveRight) {
                validAdjacent.add(new Point(point.x + 1, point.y));
//                if (canMoveDown && (walls[point.x + 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x + 1, point.y - 1)) {
//                    validAdjacent.add(new Point(point.x + 1, point.y - 1));
//                }
//                if (canMoveUp && (walls[point.x + 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x + 1, point.y + 1)) {
//                    validAdjacent.add(new Point(point.x + 1, point.y + 1));
//                }
            }
            if (canMoveUp) {
                validAdjacent.add(new Point(point.x, point.y - 1));
            }
            if (canMoveDown) {
                validAdjacent.add(new Point(point.x, point.y + 1));
            }

            validAdjacent.removeAll(parentMap.keySet());

            Collections.sort(validAdjacent, new Comparator<Point>() {
                @Override
                public int compare(Point lhs, Point rhs) {
                    return Double.compare(Math.sqrt(Math.pow(lhs.x - playerLocation.x, 2) + Math.pow(lhs.y - playerLocation.y, 2)), Math.sqrt(Math.pow(rhs.x - playerLocation.x, 2) + Math.pow(rhs.y - playerLocation.y, 2)));
                }
            });

            for (Point adjacent : validAdjacent) {
                queue.offer(adjacent);
                parentMap.put(adjacent, point);
            }

        }

        List<Point> path = new ArrayList<>();

        Point point = end;
        while (point != startPoint) {
            path.add(0, point);
            point = parentMap.get(point);
        }

        PointF storeLocation = new PointF(getLocation().x, getLocation().y);
        for (Point drawPoint : path) {

            getLocation().set(drawPoint.x, drawPoint.y);
            super.render(renderer, matrixProjection, matrixView);

        }

        getLocation().set(storeLocation.x, storeLocation.y);

        oldLocation = nextLocation;
        if (textureName == renderer.getTextureNames()[1] && !path.isEmpty()) {
            Log.d(TAG, "mob: " + worldMap.hasMob(path.get(0).x, path.get(0).y));
        }
        if (!path.isEmpty() && !worldMap.hasMob(path.get(0).x, path.get(0).y) && !(((int) (player.getLocation().x + 0.5f)) == path.get(0).x && ((int) (player.getLocation().y + 0.5f)) == path.get(0).y)) {
            worldMap.setMob(oldLocation.x, oldLocation.y, false);
            nextLocation = path.get(0);
            worldMap.setMob(nextLocation.x, nextLocation.y, true);
        }

        return System.currentTimeMillis();

    }

    private long calculateAStar(WorldMap worldMap,
                                Point startPoint,
                                byte[][] walls,
                                Player player,
                                Renderer renderer, float[] matrixProjection, float[] matrixView) {

        final PointF playerLocation = player.getLocation();
        Map<Point, Point> parentMap = new HashMap<>();
        parentMap.put(startPoint, startPoint);

        Queue<Point> queue = new LinkedList<>();
        queue.offer(startPoint);

        int highestTrail = 0;
        Point end = startPoint;

        while (!queue.isEmpty()) {

            Point point = queue.element();

            if (point.x < startPoint.x - searchRadius || point.x > startPoint.x + searchRadius || point.y < startPoint.y - searchRadius || point.y > startPoint.y + searchRadius) {
                break;
            }

            if (worldMap.getPlayerTrail()[point.x][point.y] > highestTrail) {
                highestTrail = worldMap.getPlayerTrail()[point.x][point.y];
                end = point;
            }

            queue.remove();

            List<Point> validAdjacent = new ArrayList<>();

            boolean canMoveLeft = point.x - 1 > 0 && (walls[point.x - 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x - 1, point.y);
            boolean canMoveRight = point.x + 1 < worldMap.getWidth() && (walls[point.x + 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x + 1, point.y);
            boolean canMoveDown = point.y - 1 > 0 && (walls[point.x][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y - 1] == WorldMap.ROOM) && !hasMob(
                    worldMap, player, point.x, point.y - 1);
            boolean canMoveUp = point.y + 1 < worldMap.getHeight() && (walls[point.x][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y + 1] == WorldMap.ROOM) && !hasMob(
                    worldMap, player, point.x, point.y + 1);

            if (walls[point.x][point.y] == WorldMap.ROOM) {

                if ((walls[point.x - 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x - 1, point.y - 1)) {
                    validAdjacent.add(new Point(point.x - 1, point.y - 1));
                }
                if ((walls[point.x - 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x - 1, point.y + 1)) {
                    validAdjacent.add(new Point(point.x - 1, point.y + 1));
                }
                if ((walls[point.x + 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x + 1, point.y - 1)) {
                    validAdjacent.add(new Point(point.x + 1, point.y - 1));
                }
                if ((walls[point.x + 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x + 1, point.y + 1)) {
                    validAdjacent.add(new Point(point.x + 1, point.y + 1));
                }
            }

            if (canMoveLeft) {
                validAdjacent.add(new Point(point.x - 1, point.y));
                if (canMoveDown && (walls[point.x - 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x - 1, point.y - 1)) {
                    validAdjacent.add(new Point(point.x - 1, point.y - 1));
                }
                if (canMoveUp && (walls[point.x - 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x - 1, point.y + 1)) {
                    validAdjacent.add(new Point(point.x - 1, point.y + 1));
                }
            }
            if (canMoveRight) {
                validAdjacent.add(new Point(point.x + 1, point.y));
                if (canMoveDown && (walls[point.x + 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x + 1, point.y - 1)) {
                    validAdjacent.add(new Point(point.x + 1, point.y - 1));
                }
                if (canMoveUp && (walls[point.x + 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
                        worldMap, player, point.x + 1, point.y + 1)) {
                    validAdjacent.add(new Point(point.x + 1, point.y + 1));
                }
            }
            if (canMoveDown) {
                validAdjacent.add(new Point(point.x, point.y - 1));
            }
            if (canMoveUp) {
                validAdjacent.add(new Point(point.x, point.y + 1));
            }

            validAdjacent.removeAll(parentMap.keySet());

            Collections.sort(validAdjacent, new Comparator<Point>() {
                @Override
                public int compare(Point lhs, Point rhs) {
                    return Double.compare(Math.sqrt(
                            Math.pow(lhs.x - playerLocation.x, 2) + Math.pow(
                                    lhs.y - playerLocation.y, 2)), Math.sqrt(
                            Math.pow(rhs.x - playerLocation.x, 2) + Math.pow(
                                    rhs.y - playerLocation.y, 2)));
                }
            });

            for (Point adjacent : validAdjacent) {
                queue.offer(adjacent);
                parentMap.put(adjacent, point);
            }

        }

        if (end.equals(startPoint)) {
            return 0;
        }

        List<Point> path = new ArrayList<>();

        Point point = end;
        while (point != startPoint) {
            path.add(0, point);
            point = parentMap.get(point);
        }

//        PointF storeLocation = new PointF(getLocation().x, getLocation().y);
//        for (Point drawPoint : path) {
//
//            getLocation().set(drawPoint.x, drawPoint.y);
//            super.render(renderer, matrixProjection, matrixView);
//
//        }
//
//        getLocation().set(storeLocation.x, storeLocation.y);

        oldLocation = nextLocation;
        if (!path.isEmpty() && !worldMap.hasMob(path.get(0).x, path.get(0).y) && !(((int) (player.getLocation().x + 0.5f)) == path.get(0).x && ((int) (player.getLocation().y + 0.5f)) == path.get(0).y)) {

            worldMap.setMob(oldLocation.x, oldLocation.y, false);
            nextLocation = path.get(0);
            worldMap.setMob(nextLocation.x, nextLocation.y, true);
        }

        return System.currentTimeMillis();

    }

    private boolean hasMob(WorldMap worldMap, Player player, int x, int y) {
        return worldMap.hasMob(x, y);
//        return worldMap.getWalls()[x][y] == WorldMap.ROOM && worldMap.hasMob(x, y) && !((int) player.getLocation().x == x && (int) player.getLocation().y == y);
    }

    @Override
    public void setToDestroy(WorldMap worldMap, boolean toDestroy) {
        super.setToDestroy(worldMap, toDestroy);
        worldMap.setMob(nextLocation.x, nextLocation.y, false);
    }
}
