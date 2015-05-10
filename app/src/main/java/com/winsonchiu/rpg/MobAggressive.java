package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by TheKeeperOfPie on 5/5/2015.
 */
public class MobAggressive extends Entity {

    private static final String TAG = MobAggressive.class.getCanonicalName();
    private static final float SPEED = 0.008f;
    public static final float WIDTH_RATIO = 0.59999999999f;
    public static final float HEIGHT_RATIO = 0.9f;
    private Rect homeRoom;
    private int searchRadius;
    private boolean isAlerted;
    private boolean isMoving;
    private Point nextLocation;

    public MobAggressive(int tileSize, float widthRatio, float heightRatio, PointF location, int textureName, float textureRowCount, float textureColCount, Rect room, int searchRadius) {
        super(tileSize, WIDTH_RATIO, HEIGHT_RATIO, location, textureName, textureRowCount, textureColCount);
        this.homeRoom = room;
        this.searchRadius = searchRadius;
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        calculateNextPosition(renderer);

        super.render(renderer, matrixProjection, matrixView);
    }

    private void calculateNextPosition(Renderer renderer) {

        Player player = renderer.getPlayer();
        WorldMap worldMap = renderer.getWorldMap();
        byte[][] playerTrail = worldMap.getPlayerTrail();
        byte[][] walls = worldMap.getWalls();

        if (!isAlerted) {
            if (new Rect((int) (getLocation().x - searchRadius), (int) (getLocation().y - searchRadius), (int) (getLocation().x + searchRadius), (int) (getLocation().y + searchRadius)).contains((int) player.getLocation().x, (int) player.getLocation().y)) {
                isAlerted = true;
            }
        }

        Log.d(TAG, "player: " + player.getLocation());
        Log.d(TAG, "nextLocation: " + nextLocation);

        if (isMoving) {

//            if (Math.abs(nextLocation.x - getLocation().x) < 0.1f && Math.abs( nextLocation.y - getLocation().y) < 0.1f) {
//                isMoving = false;
//            }
            if (nextLocation.x + 0.5f == getLocation().x + 0.5f) {
                setMovementX(0);
            }
            else if (nextLocation.x + 0.5f < getLocation().x + 0.5f) {
                setMovementX(-1);
            }
            else {
                setMovementX(1);
            }

            if (nextLocation.y + 0.5f == getLocation().y + 0.5f) {
                setMovementY(0);
            }
            else if (nextLocation.y + 0.5f < getLocation().y + 0.5f) {
                setMovementY(-1);
            }
            else {
                setMovementY(1);
            }

            long timeDifference = (System.currentTimeMillis() - getLastFrameTime());
            float offset = timeDifference * SPEED;
            setOffsetX(offset * getMovementX());
            setOffsetY(offset * getMovementY());

            float yCalculated;
            float xCalculated;
            boolean moveY = false;
            boolean moveX = false;

            if (getMovementY() != 0) {

                if (getMovementY() < 0) {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < walls[0].length - 1;

                    if (walls[((int) getLocation().x)][((int) yCalculated)] == WorldMap.COLLIDE) {
                        if (yCalculated < (int) (getLocation().y)) {
                            getLocation().set(getLocation().x, (int) (getLocation().y));
                        }
                        moveY = false;
                    }
                    if (walls[((int) (getLocation().x + WIDTH_RATIO - 0.05f))][((int) yCalculated)] == WorldMap.COLLIDE) {
                        if (yCalculated > (int) (getLocation().y)) {
                            getLocation().set(getLocation().x, (int) (getLocation().y));
                        }
                        moveY = false;
                    }
                }
                else {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < walls[0].length - 1;

                    if (walls[((int) getLocation().x)][((int) (yCalculated + HEIGHT_RATIO))] == WorldMap.COLLIDE) {
                        if (yCalculated < (int) (getLocation().y)) {
                            getLocation().set(getLocation().x, (int) (getLocation().y));
                        }
                        moveY = false;
                    }
                    if (walls[((int) (getLocation().x + WIDTH_RATIO - 0.05f))][((int) (yCalculated + HEIGHT_RATIO))] == WorldMap.COLLIDE) {
                        if (yCalculated > (int) (getLocation().y)) {
                            getLocation().set(getLocation().x, (int) (getLocation().y));
                        }
                        moveY = false;
                    }
                }

                if (moveY) {
                    getLocation().offset(0, getOffsetY());
                }

            }

            if (getMovementX() != 0) {

                if (getMovementX() < 0) {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < walls.length - 1;

                    if (walls[((int) xCalculated)][((int) getLocation().y)] == WorldMap.COLLIDE) {
                        if (xCalculated < (int) (getLocation().x)) {
                            getLocation().set((int) getLocation().x, getLocation().y);
                        }
                        moveX = false;
                    }
                    if (walls[((int) xCalculated)][((int) (getLocation().y + HEIGHT_RATIO - 0.05f))] == WorldMap.COLLIDE) {
                        if (xCalculated > (int) (getLocation().x)) {
                            getLocation().set((int) getLocation().x, getLocation().y);
                        }
                        moveX = false;
                    }

                }
                else {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < walls.length - 1;

                    if (walls[((int) (xCalculated + WIDTH_RATIO))][((int) getLocation().y)] == WorldMap.COLLIDE) {
                        if (xCalculated < (int) (getLocation().x)) {
                            getLocation().set((int) getLocation().x, getLocation().y);
                        }
                        moveX = false;
                    }
                    if (walls[((int) (xCalculated + WIDTH_RATIO))][((int) (getLocation().y + HEIGHT_RATIO - 0.05f))] == WorldMap.COLLIDE) {
                        if (xCalculated > (int) (getLocation().x)) {
                            getLocation().set((int) getLocation().x, getLocation().y);
                        }
                        moveX = false;
                    }
                }

                if (moveX) {
                    getLocation().offset(getOffsetX(), 0);
                }

            }

        }
        if (isAlerted) {

            Point startPoint = new Point((int) getLocation().x, (int) getLocation().y);
            Point target = new Point((int) player.getLocation().x, (int) player.getLocation().y);

            calculateAStar(target, worldMap, startPoint, walls);

        }

    }

    private void calculateAStar(Point target, WorldMap worldMap, Point startPoint, byte[][] walls) {

        Map<Point, Point> parentMap = new HashMap<>();
        parentMap.put(startPoint, startPoint);

        Queue<Point> queue = new LinkedList<>();
        queue.offer(startPoint);

        Point end = new Point(startPoint.x, startPoint.y);

        while (!queue.isEmpty()) {

            Point point = queue.element();
            if (target.equals(point)) {
                end = queue.remove();
                break;
            }

            queue.remove();

            List<Point> validAdjacent = new ArrayList<>();

            if (point.x - 1 > 0 && (walls[point.x - 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y] == WorldMap.ROOM)) {
                validAdjacent.add(new Point(point.x - 1, point.y));
            }
            if (point.x + 1 < worldMap.getWidth() && (walls[point.x + 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y] == WorldMap.ROOM)) {
                validAdjacent.add(new Point(point.x + 1, point.y));
            }
            if (point.y - 1 > 0 && (walls[point.x][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y - 1] == WorldMap.ROOM)) {
                validAdjacent.add(new Point(point.x, point.y - 1));
            }
            if (point.y + 1 < worldMap.getHeight() && (walls[point.x][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y + 1] == WorldMap.ROOM)) {
                validAdjacent.add(new Point(point.x, point.y + 1));
            }

            validAdjacent.removeAll(parentMap.keySet());

            for (Point adjacent : validAdjacent) {
                queue.offer(adjacent);
                parentMap.put(adjacent, point);
            }

        }

        List<Point> path = new ArrayList<>();

        Point point = end;
        while (parentMap.get(point) != startPoint) {
            path.add(0, point);
            point = parentMap.get(point);
        }

        if (!path.isEmpty()) {
            nextLocation = path.get(0);
            isMoving = true;
        }

    }

//    private void calculateAStar(PointF target, WorldMap worldMap, PointF startPoint, byte[][] walls) {
//
//        Map<PointF, PointF> parentMap = new HashMap<>();
//        parentMap.put(startPoint, startPoint);
//
//        Queue<PointF> queue = new LinkedList<>();
//        queue.offer(startPoint);
//
//        PointF end = new PointF(startPoint.x, startPoint.y);
//
//        while (!queue.isEmpty()) {
//
//            PointF pointF = queue.element();
//            if ((int) pointF.x == (int) target.x && (int) pointF.y == (int) target.y) {
//                end = queue.remove();
//                break;
//            }
//
//            queue.remove();
//            Point point = new Point((int) pointF.x, (int) pointF.y);
//
//            List<PointF> validAdjacent = new ArrayList<>();
//
//            if (point.x - 1 > 0 && (walls[point.x - 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y] == WorldMap.ROOM)) {
//                validAdjacent.add(new PointF(pointF.x - 1, pointF.y));
//            }
//            if (point.x + 1 < worldMap.getWidth() && (walls[point.x + 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y] == WorldMap.ROOM)) {
//                validAdjacent.add(new PointF(pointF.x + 1, pointF.y));
//            }
//            if (point.y - 1 > 0 && (walls[point.x][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y - 1] == WorldMap.ROOM)) {
//                validAdjacent.add(new PointF(pointF.x, pointF.y - 1));
//            }
//            if (point.y + 1 < worldMap.getHeight() && (walls[point.x][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y + 1] == WorldMap.ROOM)) {
//                validAdjacent.add(new PointF(pointF.x, pointF.y + 1));
//            }
//
//            validAdjacent.removeAll(parentMap.keySet());
//
//            for (PointF adjacent : validAdjacent) {
//                queue.offer(adjacent);
//                parentMap.put(adjacent, pointF);
//            }
//
//        }
//
//        List<PointF> path = new ArrayList<>();
//
//        PointF point = end;
//        while (parentMap.get(point) != startPoint) {
//            path.add(0, point);
//            point = parentMap.get(point);
//        }
//
//        if (!path.isEmpty()) {
//            nextLocation = path.get(0);
//            isMoving = true;
//        }
//
//    }


}
