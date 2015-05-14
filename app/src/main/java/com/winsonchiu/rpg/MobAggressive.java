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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

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
    private int lastX;
    private int lastY;

    public MobAggressive(int tileSize, float widthRatio, float heightRatio, PointF location, int textureName, float textureRowCount, float textureColCount, Rect room, int searchRadius) {
        super(tileSize, WIDTH_RATIO, HEIGHT_RATIO, location, textureName, textureRowCount,
              textureColCount,
              SPEED);
        this.homeRoom = new RectF(room.left, room.top, room.right, room.bottom);
        this.searchRadius = searchRadius;
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        calculateNextPosition(renderer, matrixProjection, matrixView);

        super.render(renderer, matrixProjection, matrixView);
    }

    private void calculateNextPosition(Renderer renderer, float[] matrixProjection, float[] matrixView) {


        Player player = renderer.getPlayer();
        PointF playerLocation = player.getLocation();
        byte[][] walls = renderer.getWorldMap().getWalls();

        if (playerLocation.x > getLocation().x + searchRadius ||
                playerLocation.x < getLocation().x - searchRadius ||
                playerLocation.y > getLocation().y + searchRadius ||
                playerLocation.y < getLocation().y - searchRadius) {

            // TODO: Calculate player trail
            return;

        }

//        if (doesLineIntersectWalls(getLocation(), playerLocation, walls)) {
//            return;
//        }

        long timeDifference = (System.currentTimeMillis() - getLastFrameTime());
        float offset = timeDifference * getMovementSpeed();

        float differenceX = playerLocation.x - getLocation().x;
        float differenceY = playerLocation.y - getLocation().y;

        float ratio = (float) (offset / Math.hypot(differenceX, differenceY));

        setOffsetX(ratio * differenceX);
        setOffsetY(ratio * differenceY);

        setMovementX(playerLocation.x < getLocation().x ? -1 : 1);
        setMovementY(playerLocation.y < getLocation().y ? -1 : 1);

        float yCalculated = getLocation().y + getOffsetY();
        float xCalculated = getLocation().x + getOffsetX();

        if (xCalculated < 0 || yCalculated < 0) {
            return;
        }

        RectF newBoundsX = new RectF(xCalculated, getLocation().y,
                xCalculated + getWidthRatio(),
                getLocation().y + getHeightRatio());
        RectF newBoundsY = new RectF(getLocation().x, yCalculated,
                getLocation().x + getWidthRatio(),
                yCalculated + getHeightRatio());
        boolean moveY = true;
        boolean moveX = true;

        boolean collides = false;

        if (RectF.intersects(player.getBounds(), newBoundsX) || RectF.intersects(player.getBounds(), newBoundsY)) {
            return;
        }

        for (Entity entity : renderer.getEntities()) {
            if (entity != this) {
                if (RectF.intersects(entity.getBounds(), newBoundsX)) {
                    collides = true;
                    xCalculated += -2 * getOffsetX();
                    break;
                }
                if (RectF.intersects(entity.getBounds(), newBoundsY)) {
                    collides = true;
                    yCalculated += -2 * getOffsetY();
                    break;
                }
            }
        }

        int checkHorizontalX;
        int checkVerticalY;

        if (getMovementX() < 0) {
            checkHorizontalX = (int) (getLocation().x - 0.5f);
        }
        else {
            checkHorizontalX = (int) (getLocation().x + getWidthRatio() + 0.5f);
        }

        if (walls[checkHorizontalX][(int) getLocation().y] == WorldMap.COLLIDE ||
                walls[checkHorizontalX][((int) (getLocation().y + getHeightRatio()))] == WorldMap.COLLIDE) {
            moveX = false;
        }

        if (moveX) {
            getLocation().set(xCalculated, getLocation().y);
        }


        if (getMovementY() < 0) {
            checkVerticalY = (int) (getLocation().y - 0.5f);
        }
        else {
            checkVerticalY = (int) (getLocation().y + getHeightRatio() + 0.5f);
        }

        if (walls[(int) getLocation().x][checkVerticalY] == WorldMap.COLLIDE ||
                walls[(int) (getLocation().x + getWidthRatio())][checkVerticalY] == WorldMap.COLLIDE) {
            moveY = false;
        }

        if (moveY) {
            getLocation().set(getLocation().x, yCalculated);
        }

        double angle = Math.atan(getOffsetY() / getOffsetX());

        if (getMovementX() < 0) {

            if (angle > Math.PI / 3) {
                setLastDirection(Direction.UP);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 12));
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.LEFT);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 4));
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.DOWN);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
//                setLastDirection(Direction.LEFT);
//                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 4));
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.DOWN);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
            }
            else {
                setLastDirection(Direction.LEFT);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 4));
            }

        }
        else {
            if (angle > Math.PI / 3) {
                setLastDirection(Direction.UP);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 12));
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.RIGHT);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 8));
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.DOWN);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
//                setLastDirection(Direction.RIGHT);
//                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 8));
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.DOWN);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
            }
            else {
                setLastDirection(Direction.RIGHT);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 8));
            }
        }

//
//        if (getMovementY() != 0) {
//
//            if (!RectF.intersects(player.getBounds(), getBounds()) &&
//                    !RectF.intersects(player.getBounds(), newBounds)) {
//                for (Entity entity : renderer.getEntities()) {
//                    if (entity != this && (RectF.intersects(entity.getBounds(), newBounds) || RectF.intersects(entity.getBounds(), getBounds()))) {
//                        moveY = false;
//                    }
//                }
//            }
//            else {
//                moveY = false;
//            }
//
//            if (moveY) {
//                if (getMovementY() < 0) {
//                    if (walls[((int) getLocation().x)][((int) yCalculated)] != WorldMap.COLLIDE &&
//                            walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) yCalculated)] != WorldMap.COLLIDE) {
//                        moveY = true;
//                    }
//                    else {
//                        yCalculated += -2 * getOffsetY();
//                    }
//                }
//                else {
//                    if (walls[((int) getLocation().x)][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE &&
//                            walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE) {
//                        moveY = true;
//                    }
//                    else {
//                        yCalculated += -2 * getOffsetY();
//                    }
//                }
//            }
//
//            if (moveY) {
//                getLocation().set(getLocation().x, yCalculated);
//            }
//
//        }
//
//        if (getMovementX() != 0) {
//
//            if (!RectF.intersects(player.getBounds(), getBounds()) &&
//                    !RectF.intersects(player.getBounds(), newBounds)) {
//
//                for (Entity entity : renderer.getEntities()) {
//                    if (entity != this && (RectF.intersects(entity.getBounds(), newBounds) || RectF.intersects(entity.getBounds(), getBounds()))) {
//                        moveX = false;
//                    }
//                }
//            }
//            else {
//                moveX = false;
//            }
//
//            if (moveX) {
//                if (getMovementX() < 0) {
//                    if (walls[((int) xCalculated)][((int) getLocation().y)] != WorldMap.COLLIDE &&
//                            walls[((int) xCalculated)][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE) {
//                        moveX = true;
//                    }
//                    else {
//                        xCalculated += -2 * getOffsetX();
//                    }
//                }
//                else {
//                    if (walls[((int) (xCalculated + getWidthRatio()))][((int) getLocation().y)] != WorldMap.COLLIDE &&
//                            walls[((int) (xCalculated + getWidthRatio()))][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE) {
//                        moveX = true;
//                    }
//                    else {
//                        xCalculated += -2 * getOffsetX();
//                    }
//                }
//            }
//
//            if (moveX) {
//                getLocation().set(xCalculated, getLocation().y);
//            }
//
//        }

    }

    private boolean doesLineIntersectWalls(PointF first, PointF second, byte[][] walls) {

        int changeX = (int) Math.abs(second.x - first.x);
        int changeY = (int) Math.abs(second.y - first.y);

        int currentX = (int) first.x;
        int currentY = (int) first.y;
        int increaseX = second.x > first.x ? 1 : -1;
        int increaseY = second.y > first.y ? 1 : -1;
        int error = changeX - changeY;
        changeX *= 2;
        changeY *= 2;

        for (int num = 1 + changeX + changeY; num > 0; num--) {

            if (walls[currentX][currentY] == WorldMap.COLLIDE) {
                return true;
            }

            if (error > 0) {
                currentX += increaseX;
                error -= changeY;
            }
            else {
                currentY += increaseY;
                error += changeX;
            }

        }

        return false;
    }

//    private void calculateNextPositionOld(Renderer renderer,
//                                       float[] matrixProjection,
//                                       float[] matrixView) {
//
//        Player player = renderer.getPlayer();
//        WorldMap worldMap = renderer.getWorldMap();
//        byte[][] playerTrail = worldMap.getPlayerTrail();
//        byte[][] walls = worldMap.getWalls();
//
//        Point startPoint = new Point((int) getLocation().x, (int) getLocation().y);
//        int targetX = (int) player.getLocation().x;
//        int targetY = (int) player.getLocation().y;
//        if ((int) (player.getLocation().x + player.getWidthRatio()) >= (int) (player.getLocation().x) + 1) {
//            targetX++;
//        }
//        if ((int) (player.getLocation().y + player.getHeightRatio()) >= (int) (player.getLocation().y) + 1) {
//            targetY++;
//        }
//
//
//
//        if (lastX != (int) getLocation().x || lastY != (int) getLocation().y || path.isEmpty()) {
//
//            lastX = (int) getLocation().x;
//            lastY = (int) getLocation().y;
//
//            path = calculateAStar(new Point(targetX, targetY), worldMap, startPoint,
//                                  walls, player, renderer, matrixProjection,
//                                  matrixView);
//
//            PointF storeLocation = new PointF(getLocation().x, getLocation().y);
//
//            for (PointF point : path) {
//                getLocation().set(point.x, point.y);
//                super.render(renderer, matrixProjection, matrixView);
//            }
//
//            getLocation().set(storeLocation.x, storeLocation.y);
//        }
//
//        long timeDifference = (System.currentTimeMillis() - getLastFrameTime());
//        float offset = timeDifference * getMovementSpeed();
//
//        if (!path.isEmpty()) {
//            PointF next = path.size() > 1 ? path.get(1) : path.get(0);
//
//            setMovementX(next.x > getLocation().x ? 1 : -1);
//            setMovementY(next.y > getLocation().y ? 1 : -1);
//            setOffsetX(offset * getMovementX());
//            setOffsetY(offset * getMovementY());
//        }
//        else {
//            setMovementX(0);
//            setMovementY(0);
//            setOffsetX(offset * getMovementX());
//            setOffsetY(offset * getMovementY());
//        }
//
//        setVelocityX(getOffsetX() / timeDifference);
//        setVelocityY(getOffsetY() / timeDifference);
//        float yCalculated = getLocation().y + getOffsetY();
//        float xCalculated = getLocation().x + getOffsetX();
//
//        RectF newBounds = new RectF(xCalculated, yCalculated,
//                                    xCalculated + getWidthRatio(),
//                                    yCalculated + getHeightRatio());
//        boolean moveY = true;
//        boolean moveX = true;
//
//        if (getMovementY() != 0) {
//
//            if (getMovementY() < 0) {
//                moveY = yCalculated > 1 &&
//                        yCalculated < walls[0].length - 1 &&
//                        walls[((int) getLocation().x)][((int) yCalculated)] != WorldMap.COLLIDE &&
//                        walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) yCalculated)] != WorldMap.COLLIDE;
//            }
//            else {
//                moveY = yCalculated > 1 &&
//                        yCalculated < walls[0].length - 1 &&
//                        walls[((int) getLocation().x)][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE &&
//                        walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE;
//            }
//
//            if (moveY) {
//
//                if (!RectF.intersects(player.getBounds(), getBounds()) &&
//                        !RectF.intersects(player.getBounds(), newBounds)) {
//                    for (Entity entity : renderer.getEntities()) {
//                        if (entity != this && (RectF.intersects(entity.getBounds(), newBounds) || RectF.intersects(entity.getBounds(), getBounds()))) {
//                            moveY = false;
//                            path.clear();
//                        }
//                    }
//                    if (moveY) {
//                        getLocation().set(getLocation().x, yCalculated);
//                    }
//                }
//            }
//
//        }
//
//        if (getMovementX() != 0) {
//
//            if (getMovementX() < 0) {
//                moveX = xCalculated > 1 &&
//                        xCalculated < walls.length - 1 &&
//                        walls[((int) xCalculated)][((int) getLocation().y)] != WorldMap.COLLIDE &&
//                        walls[((int) xCalculated)][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE;
//            }
//            else {
//                moveX = xCalculated > 1 &&
//                        xCalculated < walls.length - 1 &&
//                        walls[((int) (xCalculated + getWidthRatio()))][((int) getLocation().y)] != WorldMap.COLLIDE &&
//                        walls[((int) (xCalculated + getWidthRatio()))][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE;
//            }
//
//            if (moveX) {
//                if (!RectF.intersects(player.getBounds(), getBounds()) &&
//                        !RectF.intersects(player.getBounds(), newBounds)) {
//
//
//                    for (Entity entity : renderer.getEntities()) {
//                        if (entity != this && (RectF.intersects(entity.getBounds(), newBounds) || RectF.intersects(entity.getBounds(), getBounds()))) {
//                            moveX = false;
//                            path.clear();
//                        }
//                    }
//
//                    if (moveX) {
//                        getLocation().set(xCalculated, getLocation().y);
//                    }
//
//
//                }
//            }
//
//        }
//
//    }

    private List<PointF> calculateAStar(Point target, WorldMap worldMap, Point startPoint, byte[][] walls, Player player, Renderer renderer, float[] matrixProjection, float[] matrixView) {

        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashSet<Node> closedSet = new HashSet<>();

        openList.add(new Node(startPoint, 0));

        while (!openList.isEmpty()) {

            Node currentNode = openList.poll();

            if (currentNode.getPoint().equals(target)) {
                return getPath(currentNode);
            }

            for (Node node : currentNode.getAdjacentNodes()) {

                if (node.getPoint().x < startPoint.x + searchRadius &&
                        node.getPoint().x > startPoint.x - searchRadius &&
                        node.getPoint().y < startPoint.y + searchRadius &&
                        node.getPoint().y > startPoint.y - searchRadius &&
                        node.getPoint().x > 0 &&
                        node.getPoint().x < worldMap.getWidth() &&
                        node.getPoint().y > 0 &&
                        node.getPoint().y < worldMap.getHeight() &&
                        walls[node.getPoint().x][node.getPoint().y] != WorldMap.COLLIDE) {

                    boolean intersect = false;

                    for (Entity entity : renderer.getEntities()) {
                        if (entity != this && RectF.intersects(entity.getBounds(), new RectF(node.getPoint().x + 0.05f, node.getPoint().y + 0.05f, node.getPoint().x + 0.95f, node.getPoint().y + 0.95f))) {
                            intersect = true;
                            break;
                        }
                    }

                    if (!closedSet.contains(node) && !intersect) {
                        node.calculateCostTo(target);
                        openList.offer(node);
                        node.setParent(currentNode);

                        closedSet.add(node);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private boolean isPointValid(Point point, byte[][] walls) {
        return point.x > 0 && point.x < walls.length && point.y > 0 && point.y < walls[0].length && walls[point.x][point.y] != WorldMap.COLLIDE;
    }

    private List<PointF> getPath(Node currentNode) {

        List<PointF> path = new ArrayList<>();

        while (currentNode.getParent() != null) {
            path.add(new PointF(currentNode.getPoint()));
            currentNode = currentNode.getParent();
        }

        return path;
    }


    private boolean hasMob(WorldMap worldMap, Player player, int x, int y) {
        return worldMap.hasMob(x, y);
    }
}
