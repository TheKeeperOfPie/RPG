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
    private boolean isAlerted;
    private boolean isMoving;
    private Point nextLocation;
    private long movingStartTime;
    private Point oldLocation;
    private List<PointF> path;
    private int lastX;
    private int lastY;

    public MobAggressive(int tileSize, float widthRatio, float heightRatio, PointF location, int textureName, float textureRowCount, float textureColCount, Rect room, int searchRadius) {
        super(tileSize, WIDTH_RATIO, HEIGHT_RATIO, location, textureName, textureRowCount,
              textureColCount,
              SPEED);
        path = new ArrayList<>();
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


        if (lastX != (int) getLocation().x || lastY != (int) getLocation().y || path.isEmpty()) {

            lastX = (int) getLocation().x;
            lastY = (int) getLocation().y;

            path = calculateAStar(new Point(targetX, targetY), worldMap, startPoint,
                                  walls, player, renderer, matrixProjection,
                                  matrixView);

            PointF storeLocation = new PointF(getLocation().x, getLocation().y);

            for (PointF point : path) {
                getLocation().set(point.x, point.y);
                super.render(renderer, matrixProjection, matrixView);
            }

            getLocation().set(storeLocation.x, storeLocation.y);
        }

        long timeDifference = (System.currentTimeMillis() - getLastFrameTime());
        float offset = timeDifference * getMovementSpeed();

//        if (walls[(int) player.getLocation().x][(int) player.getLocation().y] == WorldMap.ROOM && MathUtils.distance(player.getLocation(), getLocation()) < 2) {
//
//            Log.d(TAG, "track player");
//
//            PointF next = new PointF(player.getLocation().x + player.getWidthRatio() / 2, player.getLocation().y + player.getHeightRatio() / 2);
//
//            setMovementX(next.x > getLocation().x ? 1 : -1);
//            setMovementY(next.y > getLocation().y ? 1 : -1);
//            setOffsetX(offset * getMovementX());
//            setOffsetY(offset * getMovementY());
//
//            if ((int) next.x == (int) getLocation().x) {
//                float maxOffsetX = 0.5f - getLocation().x;
//                if (Math.abs(getOffsetX()) > maxOffsetX) {
//                    setOffsetX(maxOffsetX);
//                }
//            }
//            if ((int) next.y == (int) getLocation().y) {
//                float maxOffsetY = 0.5f - getLocation().y;
//                if (Math.abs(getOffsetY()) > maxOffsetY) {
//                    setOffsetY(maxOffsetY);
//                }
//            }
//        }
        if (!path.isEmpty()) {
            PointF next = path.size() > 1 ? path.get(1) : path.get(0);

            setMovementX(next.x > getLocation().x ? 1 : -1);
            setMovementY(next.y > getLocation().y ? 1 : -1);
            setOffsetX(offset * getMovementX());
            setOffsetY(offset * getMovementY());

//            if ((int) next.x == (int) getLocation().x) {
//                float maxOffsetX = (int) (getLocation().x) + 0.5f - getLocation().x;
//                if (Math.abs(getOffsetX()) > maxOffsetX) {
//                    setOffsetX(maxOffsetX);
//                }
//            }
//            if ((int) next.y == (int) getLocation().y) {
//                float maxOffsetY = (int) (getLocation().y) + 0.5f  - getLocation().y;
//                if (Math.abs(getOffsetY()) > maxOffsetY) {
//                    setOffsetY(maxOffsetY);
//                }
//            }
        }
        else {
            setMovementX(0);
            setMovementY(0);
            setOffsetX(offset * getMovementX());
            setOffsetY(offset * getMovementY());
        }

        setVelocityX(getOffsetX() / timeDifference);
        setVelocityY(getOffsetY() / timeDifference);
        float yCalculated = getLocation().y + getOffsetY();
        float xCalculated = getLocation().x + getOffsetX();

        RectF newBounds = new RectF(xCalculated, yCalculated,
                                    xCalculated + getWidthRatio(),
                                    yCalculated + getHeightRatio());
        boolean moveY = true;
        boolean moveX = true;

        if (getMovementY() != 0) {

            if (getMovementY() < 0) {
                moveY = yCalculated > 1 &&
                        yCalculated < walls[0].length - 1 &&
                        walls[((int) getLocation().x)][((int) yCalculated)] != WorldMap.COLLIDE &&
                        walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) yCalculated)] != WorldMap.COLLIDE;
            }
            else {
                moveY = yCalculated > 1 &&
                        yCalculated < walls[0].length - 1 &&
                        walls[((int) getLocation().x)][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE &&
                        walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE;
            }

            if (moveY) {

                if (!RectF.intersects(player.getBounds(), getBounds()) &&
                        !RectF.intersects(player.getBounds(), newBounds)) {
                    for (Entity entity : renderer.getEntities()) {
                        if (entity != this && (RectF.intersects(entity.getBounds(), newBounds) || RectF.intersects(entity.getBounds(), getBounds()))) {
                            moveY = false;
                            path.clear();
                        }
                    }
                    if (moveY) {
                        getLocation().set(getLocation().x, yCalculated);
                    }
                }
            }

        }

        if (getMovementX() != 0) {

            if (getMovementX() < 0) {
                moveX = xCalculated > 1 &&
                        xCalculated < walls.length - 1 &&
                        walls[((int) xCalculated)][((int) getLocation().y)] != WorldMap.COLLIDE &&
                        walls[((int) xCalculated)][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE;
            }
            else {
                moveX = xCalculated > 1 &&
                        xCalculated < walls.length - 1 &&
                        walls[((int) (xCalculated + getWidthRatio()))][((int) getLocation().y)] != WorldMap.COLLIDE &&
                        walls[((int) (xCalculated + getWidthRatio()))][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE;
            }

            if (moveX) {
                if (!RectF.intersects(player.getBounds(), getBounds()) &&
                        !RectF.intersects(player.getBounds(), newBounds)) {


                    for (Entity entity : renderer.getEntities()) {
                        if (entity != this && (RectF.intersects(entity.getBounds(), newBounds) || RectF.intersects(entity.getBounds(), getBounds()))) {
                            moveX = false;
                            path.clear();
                        }
                    }

                    if (moveX) {
                        getLocation().set(xCalculated, getLocation().y);
                    }


                }
            }

        }


//        if (!isAlerted) {
//            Point target = new Point(targetX, targetY);
//
//            Map<Point, Point> parentMap = new HashMap<>();
//            parentMap.put(startPoint, startPoint);
//
//            Queue<Point> queue = new LinkedList<>();
//            queue.offer(startPoint);
//
//            final PointF playerLocation = player.getLocation();
//
//            while (!queue.isEmpty()) {
//
//                Point point = queue.element();
//
//                if (point.x < startPoint.x - searchRadius || point.x > startPoint.x + searchRadius || point.y < startPoint.y - searchRadius || point.y > startPoint.y + searchRadius) {
//                    break;
//                }
//
//                if (target.equals(point)) {
//                    isAlerted = true;
//                    break;
//                }
//
//                queue.remove();
//
//                List<Point> validAdjacent = new ArrayList<>();
//
//                boolean canMoveLeft = point.x - 1 > 0 && (walls[point.x - 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x - 1, point.y);
//                boolean canMoveRight = point.x + 1 < worldMap.getWidth() && (walls[point.x + 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x + 1, point.y);
//                boolean canMoveUp = point.y - 1 > 0 && (walls[point.x][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x, point.y - 1);
//                boolean canMoveDown = point.y + 1 < worldMap.getHeight() && (walls[point.x][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x, point.y + 1);
//
//                if (canMoveLeft) {
//                    validAdjacent.add(new Point(point.x - 1, point.y));
//                    if (canMoveDown && (walls[point.x - 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                            worldMap, player, point.x - 1, point.y - 1)) {
//                        validAdjacent.add(new Point(point.x - 1, point.y - 1));
//                    }
//                    if (canMoveUp && (walls[point.x - 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                            worldMap, player, point.x - 1, point.y + 1)) {
//                        validAdjacent.add(new Point(point.x - 1, point.y + 1));
//                    }
//                }
//                if (canMoveRight) {
//                    validAdjacent.add(new Point(point.x + 1, point.y));
//                    if (canMoveDown && (walls[point.x + 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                            worldMap, player, point.x + 1, point.y - 1)) {
//                        validAdjacent.add(new Point(point.x + 1, point.y - 1));
//                    }
//                    if (canMoveUp && (walls[point.x + 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                            worldMap, player, point.x + 1, point.y + 1)) {
//                        validAdjacent.add(new Point(point.x + 1, point.y + 1));
//                    }
//                }
//                if (canMoveUp) {
//                    validAdjacent.add(new Point(point.x, point.y - 1));
//                }
//                if (canMoveDown) {
//                    validAdjacent.add(new Point(point.x, point.y + 1));
//                }
//
//                validAdjacent.removeAll(parentMap.keySet());
//
//                Collections.sort(validAdjacent, new Comparator<Point>() {
//                    @Override
//                    public int compare(Point lhs, Point rhs) {
//                        return Double.compare(Math.sqrt(Math.pow(lhs.x - playerLocation.x, 2) + Math.pow(lhs.y - playerLocation.y, 2)), Math.sqrt(Math.pow(rhs.x - playerLocation.x, 2) + Math.pow(rhs.y - playerLocation.y, 2)));
//                    }
//                });
//
//                for (Point adjacent : validAdjacent) {
//                    queue.offer(adjacent);
//                    parentMap.put(adjacent, point);
//                }
//
//            }
//        }
//
//        if (movingStartTime > 0) {
//            long time = System.currentTimeMillis();
//            if (time > movingStartTime + 250) {
//                movingStartTime = 0;
//                getLocation().set(nextLocation.x + (1f - getWidthRatio()) / 2f, nextLocation.y + (1f - getHeightRatio()) / 2f);
//                return;
//            }
//
//            float ratio = (time - movingStartTime) / 250f;
//            getLocation().set(
//                    (nextLocation.x - oldLocation.x) * ratio + oldLocation.x + (1f - getWidthRatio()) / 2f,
//                    (nextLocation.y - oldLocation.y) * ratio + oldLocation.y + (1f - getHeightRatio()) / 2f);
//
//        }
//        else if (isAlerted) {
//
//            Point target = new Point(targetX, targetY);
//
//            movingStartTime = calculateAStar(target, worldMap, startPoint, walls, player, renderer, matrixProjection, matrixView);
//
//            if (movingStartTime <= 0) {
//                movingStartTime = calculateAStar(worldMap, startPoint, walls, player, renderer, matrixProjection, matrixView);
//            }
//
//        }
//        else {
//            Point target = new Point((int) homeRoom.centerX(), (int) homeRoom.centerY());
//
//            movingStartTime = calculateAStar(target, worldMap, startPoint, walls, player, renderer, matrixProjection, matrixView);
//        }
//
//        if (movingStartTime <= 0) {
//            isAlerted = false;
//        }

    }

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
//        return worldMap.getWalls()[x][y] == WorldMap.ROOM && worldMap.hasMob(x, y) && !((int) player.getLocation().x == x && (int) player.getLocation().y == y);
    }

    @Override
    public void setToDestroy(WorldMap worldMap, boolean toDestroy) {
        super.setToDestroy(worldMap, toDestroy);
        worldMap.setMob(nextLocation.x, nextLocation.y, false);
    }

//    private long calculateAStar(Point target,
//                                WorldMap worldMap,
//                                Point startPoint,
//                                byte[][] walls,
//                                Player player,
//                                Renderer renderer, float[] matrixProjection, float[] matrixView) {
//
//        Map<Point, Point> parentMap = new HashMap<>();
//        parentMap.put(startPoint, startPoint);
//
//        Queue<Point> queue = new LinkedList<>();
//        queue.offer(startPoint);
//
//        final PointF playerLocation = player.getLocation();
//
//        Point end = new Point(startPoint.x, startPoint.y);
//
//        while (!queue.isEmpty()) {
//
//            Point point = queue.element();
//
//            if (!homeRoom.contains(getLocation().x, getLocation().y) && isAlerted) {
//                if (point.x < startPoint.x - searchRadius || point.x > startPoint.x + searchRadius || point.y < startPoint.y - searchRadius || point.y > startPoint.y + searchRadius) {
//                    return 0;
//                }
//            }
//
//            if (target.equals(point)) {
//                end = queue.remove();
//                break;
//            }
//
//            queue.remove();
//
//            List<Point> validAdjacent = new ArrayList<>();
//
//            boolean canMoveLeft = point.x - 1 > 0 && (walls[point.x - 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x - 1, point.y);
//            boolean canMoveRight = point.x + 1 < worldMap.getWidth() && (walls[point.x + 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x + 1, point.y);
//            boolean canMoveUp = point.y - 1 > 0 && (walls[point.x][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                    worldMap, player, point.x, point.y - 1);
//            boolean canMoveDown = point.y + 1 < worldMap.getHeight() && (walls[point.x][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                    worldMap, player, point.x, point.y + 1);
//
//            if (canMoveLeft) {
//                validAdjacent.add(new Point(point.x - 1, point.y));
////                if (canMoveDown && (walls[point.x - 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
////                        worldMap, player, point.x - 1, point.y - 1)) {
////                    validAdjacent.add(new Point(point.x - 1, point.y - 1));
////                }
////                if (canMoveUp && (walls[point.x - 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
////                        worldMap, player, point.x - 1, point.y + 1)) {
////                    validAdjacent.add(new Point(point.x - 1, point.y + 1));
////                }
//            }
//            if (canMoveRight) {
//                validAdjacent.add(new Point(point.x + 1, point.y));
////                if (canMoveDown && (walls[point.x + 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
////                        worldMap, player, point.x + 1, point.y - 1)) {
////                    validAdjacent.add(new Point(point.x + 1, point.y - 1));
////                }
////                if (canMoveUp && (walls[point.x + 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
////                        worldMap, player, point.x + 1, point.y + 1)) {
////                    validAdjacent.add(new Point(point.x + 1, point.y + 1));
////                }
//            }
//            if (canMoveUp) {
//                validAdjacent.add(new Point(point.x, point.y - 1));
//            }
//            if (canMoveDown) {
//                validAdjacent.add(new Point(point.x, point.y + 1));
//            }
//
//            validAdjacent.removeAll(parentMap.keySet());
//
//            Collections.sort(validAdjacent, new Comparator<Point>() {
//                @Override
//                public int compare(Point lhs, Point rhs) {
//                    return Double.compare(Math.sqrt(Math.pow(lhs.x - playerLocation.x, 2) + Math.pow(lhs.y - playerLocation.y, 2)), Math.sqrt(Math.pow(rhs.x - playerLocation.x, 2) + Math.pow(rhs.y - playerLocation.y, 2)));
//                }
//            });
//
//            for (Point adjacent : validAdjacent) {
//                queue.offer(adjacent);
//                parentMap.put(adjacent, point);
//            }
//
//        }
//
//        List<Point> path = new ArrayList<>();
//
//        Point point = end;
//        while (point != startPoint) {
//            path.add(0, point);
//            point = parentMap.get(point);
//        }
//
//        PointF storeLocation = new PointF(getLocation().x, getLocation().y);
//        for (Point drawPoint : path) {
//
//            getLocation().set(drawPoint.x, drawPoint.y);
//            super.render(renderer, matrixProjection, matrixView);
//
//        }
//
//        getLocation().set(storeLocation.x, storeLocation.y);
//
//        oldLocation = nextLocation;
//        if (textureName == renderer.getTextureNames()[1] && !path.isEmpty()) {
//            Log.d(TAG, "mob: " + worldMap.hasMob(path.get(0).x, path.get(0).y));
//        }
//        if (!path.isEmpty() && !worldMap.hasMob(path.get(0).x, path.get(0).y) && !(((int) (player.getLocation().x + 0.5f)) == path.get(0).x && ((int) (player.getLocation().y + 0.5f)) == path.get(0).y)) {
//            worldMap.setMob(oldLocation.x, oldLocation.y, false);
//            nextLocation = path.get(0);
//            worldMap.setMob(nextLocation.x, nextLocation.y, true);
//        }
//
//        return System.currentTimeMillis();
//
//    }
//
//    private long calculateAStar(WorldMap worldMap,
//                                Point startPoint,
//                                byte[][] walls,
//                                Player player,
//                                Renderer renderer, float[] matrixProjection, float[] matrixView) {
//
//        final PointF playerLocation = player.getLocation();
//        Map<Point, Point> parentMap = new HashMap<>();
//        parentMap.put(startPoint, startPoint);
//
//        Queue<Point> queue = new LinkedList<>();
//        queue.offer(startPoint);
//
//        int highestTrail = 0;
//        Point end = startPoint;
//
//        while (!queue.isEmpty()) {
//
//            Point point = queue.element();
//
//            if (point.x < startPoint.x - searchRadius || point.x > startPoint.x + searchRadius || point.y < startPoint.y - searchRadius || point.y > startPoint.y + searchRadius) {
//                break;
//            }
//
//            if (worldMap.getPlayerTrail()[point.x][point.y] > highestTrail) {
//                highestTrail = worldMap.getPlayerTrail()[point.x][point.y];
//                end = point;
//            }
//
//            queue.remove();
//
//            List<Point> validAdjacent = new ArrayList<>();
//
//            boolean canMoveLeft = point.x - 1 > 0 && (walls[point.x - 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x - 1, point.y);
//            boolean canMoveRight = point.x + 1 < worldMap.getWidth() && (walls[point.x + 1][point.y] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y] == WorldMap.ROOM) && !hasMob(worldMap, player, point.x + 1, point.y);
//            boolean canMoveDown = point.y - 1 > 0 && (walls[point.x][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                    worldMap, player, point.x, point.y - 1);
//            boolean canMoveUp = point.y + 1 < worldMap.getHeight() && (walls[point.x][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                    worldMap, player, point.x, point.y + 1);
//
//            if (walls[point.x][point.y] == WorldMap.ROOM) {
//
//                if ((walls[point.x - 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x - 1, point.y - 1)) {
//                    validAdjacent.add(new Point(point.x - 1, point.y - 1));
//                }
//                if ((walls[point.x - 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x - 1, point.y + 1)) {
//                    validAdjacent.add(new Point(point.x - 1, point.y + 1));
//                }
//                if ((walls[point.x + 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x + 1, point.y - 1)) {
//                    validAdjacent.add(new Point(point.x + 1, point.y - 1));
//                }
//                if ((walls[point.x + 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x + 1, point.y + 1)) {
//                    validAdjacent.add(new Point(point.x + 1, point.y + 1));
//                }
//            }
//
//            if (canMoveLeft) {
//                validAdjacent.add(new Point(point.x - 1, point.y));
//                if (canMoveDown && (walls[point.x - 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x - 1, point.y - 1)) {
//                    validAdjacent.add(new Point(point.x - 1, point.y - 1));
//                }
//                if (canMoveUp && (walls[point.x - 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x - 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x - 1, point.y + 1)) {
//                    validAdjacent.add(new Point(point.x - 1, point.y + 1));
//                }
//            }
//            if (canMoveRight) {
//                validAdjacent.add(new Point(point.x + 1, point.y));
//                if (canMoveDown && (walls[point.x + 1][point.y - 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y - 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x + 1, point.y - 1)) {
//                    validAdjacent.add(new Point(point.x + 1, point.y - 1));
//                }
//                if (canMoveUp && (walls[point.x + 1][point.y + 1] == WorldMap.CORRIDOR_CONNECTED || walls[point.x + 1][point.y + 1] == WorldMap.ROOM) && !hasMob(
//                        worldMap, player, point.x + 1, point.y + 1)) {
//                    validAdjacent.add(new Point(point.x + 1, point.y + 1));
//                }
//            }
//            if (canMoveDown) {
//                validAdjacent.add(new Point(point.x, point.y - 1));
//            }
//            if (canMoveUp) {
//                validAdjacent.add(new Point(point.x, point.y + 1));
//            }
//
//            validAdjacent.removeAll(parentMap.keySet());
//
//            Collections.sort(validAdjacent, new Comparator<Point>() {
//                @Override
//                public int compare(Point lhs, Point rhs) {
//                    return Double.compare(Math.sqrt(
//                            Math.pow(lhs.x - playerLocation.x, 2) + Math.pow(
//                                    lhs.y - playerLocation.y, 2)), Math.sqrt(
//                            Math.pow(rhs.x - playerLocation.x, 2) + Math.pow(
//                                    rhs.y - playerLocation.y, 2)));
//                }
//            });
//
//            for (Point adjacent : validAdjacent) {
//                queue.offer(adjacent);
//                parentMap.put(adjacent, point);
//            }
//
//        }
//
//        if (end.equals(startPoint)) {
//            return 0;
//        }
//
//        List<Point> path = new ArrayList<>();
//
//        Point point = end;
//        while (point != startPoint) {
//            path.add(0, point);
//            point = parentMap.get(point);
//        }
//
////        PointF storeLocation = new PointF(getLocation().x, getLocation().y);
////        for (Point drawPoint : path) {
////
////            getLocation().set(drawPoint.x, drawPoint.y);
////            super.render(renderer, matrixProjection, matrixView);
////
////        }
////
////        getLocation().set(storeLocation.x, storeLocation.y);
//
//        oldLocation = nextLocation;
//        if (!path.isEmpty() && !worldMap.hasMob(path.get(0).x, path.get(0).y) && !(((int) (player.getLocation().x + 0.5f)) == path.get(0).x && ((int) (player.getLocation().y + 0.5f)) == path.get(0).y)) {
//
//            worldMap.setMob(oldLocation.x, oldLocation.y, false);
//            nextLocation = path.get(0);
//            worldMap.setMob(nextLocation.x, nextLocation.y, true);
//        }
//
//        return System.currentTimeMillis();
//
//    }
}
