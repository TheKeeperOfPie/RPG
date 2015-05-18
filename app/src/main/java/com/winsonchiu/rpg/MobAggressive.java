package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

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
    private PointF targetLocation;
    private Point homeLocation;
    private boolean isAlerted;

    public MobAggressive(int tileSize, float widthRatio, float heightRatio, PointF location, int textureName, float textureRowCount, float textureColCount, Rect room, int searchRadius) {
        super(tileSize, widthRatio, heightRatio, location, textureName, textureRowCount,
              textureColCount,
              SPEED);
        this.homeLocation = new Point((int) location.x, (int) location.y);
        this.targetLocation = new PointF(location.x, location.y);
        this.homeRoom = new RectF(room.left, room.top, room.right, room.bottom);
        this.searchRadius = searchRadius;
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

//        calculateNextPosition(renderer);

        super.render(renderer, matrixProjection, matrixView);
    }

    private void calculateNextPosition(Renderer renderer) {


        Player player = renderer.getPlayer();
        PointF playerLocation = player.getLocation();
        byte[][] walls = renderer.getWorldMap().getWalls();

        if (playerLocation.x < getLocation().x + searchRadius &&
                playerLocation.x > getLocation().x - searchRadius &&
                playerLocation.y < getLocation().y + searchRadius &&
                playerLocation.y > getLocation().y - searchRadius) {

            if (!doesLineIntersectWalls(getLocation(), playerLocation, walls)) {
                targetLocation.set(playerLocation.x, playerLocation.y);
                isAlerted = true;
            }

        }
        else if (isAlerted) {
            Point location = searchForTrail(new Point((int) getLocation().x, (int) getLocation().y), renderer.getWorldMap(), renderer);
            if (location != null) {
                targetLocation.set(location.x, location.y);
            }
            else {
                isAlerted = false;
            }
        }
        else {
//            List<PointF> path = searchForHome(new Point((int) getLocation().x, (int) getLocation().y), renderer.getWorldMap(), renderer);
//            if (!path.isEmpty()) {
//                targetLocation = path.size() > 1 ? path.get(1) : path.get(0);
//            }
        }

        if (doesLineIntersectWalls(getLocation(), targetLocation, walls)) {
            return;
        }

        long timeDifference = (System.currentTimeMillis() - getLastFrameTime());
        float offset = timeDifference * getMovementSpeed();

        float differenceX = targetLocation.x - getLocation().x;
        float differenceY = targetLocation.y - getLocation().y;

        double distance = Math.hypot(differenceX, differenceY);

        float ratio = (float) (offset / distance);

        if (offset > distance) {
            targetLocation.set(getLocation().x, getLocation().y);
            return;
        }

        setOffsetX(ratio * differenceX);
        setOffsetY(ratio * differenceY);

        setMovementX(targetLocation.x < getLocation().x ? -1 : 1);
        setMovementY(targetLocation.y < getLocation().y ? -1 : 1);

        float calculatedY = getLocation().y + getOffsetY();
        float calculatedX = getLocation().x + getOffsetX();

        if (calculatedX < 0 || calculatedY < 0 || calculatedX >= renderer.getWorldMap().getWidth() || calculatedY >= renderer.getWorldMap().getHeight()) {
            return;
        }

        RectF newBoundsX = new RectF(calculatedX, getLocation().y,
                calculatedX + getWidthRatio(),
                getLocation().y + getHeightRatio());
        RectF newBoundsY = new RectF(getLocation().x, calculatedY,
                getLocation().x + getWidthRatio(),
                calculatedY + getHeightRatio());

        if (RectF.intersects(player.getBounds(), newBoundsX) || RectF.intersects(player.getBounds(), newBoundsY)) {
            return;
        }

        for (Entity entity : renderer.getEntities()) {
            if (entity != this) {
                if (RectF.intersects(entity.getBounds(), newBoundsX)) {
                    calculatedX += -2 * getOffsetX();
                    if (!isAlerted) {
                        targetLocation.set(getLocation().x, getLocation().y);
                    }
                    break;
                }
                if (RectF.intersects(entity.getBounds(), newBoundsY)) {
                    calculatedY += -2 * getOffsetY();
                    if (!isAlerted) {
                        targetLocation.set(getLocation().x, getLocation().y);
                    }
                    break;
                }
            }
        }

        int checkHorizontalX;
        int checkVerticalY;

        checkHorizontalX = getMovementX() < 0 ? ((int) (getLocation().x - 0.5f)) : ((int) (getLocation().x + getWidthRatio() + 0.5f));
        checkVerticalY = getMovementY() < 0 ? ((int) (getLocation().y - 0.5f)) : ((int) (getLocation().y + getHeightRatio() + 0.5f));


        if (walls[checkHorizontalX][(int) getLocation().y] != WorldMap.COLLIDE &&
                walls[checkHorizontalX][((int) (getLocation().y + getHeightRatio()))] != WorldMap.COLLIDE) {
            getLocation().set(calculatedX, getLocation().y);
        }
        if (walls[(int) getLocation().x][checkVerticalY] != WorldMap.COLLIDE &&
                walls[(int) (getLocation().x + getWidthRatio())][checkVerticalY] != WorldMap.COLLIDE) {
            getLocation().set(getLocation().x, calculatedY);
        }

        calculateAnimationFrame();

    }

    private void calculateAnimationFrame() {
        double angle = Math.atan(getOffsetY() / getOffsetX());

        if (getMovementX() > 0) {

            if (angle > Math.PI / 3) {
                setLastDirection(Direction.NORTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 12));
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.NORTHEAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 12));
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.SOUTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.SOUTHEAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
            }
            else {
                setLastDirection(Direction.EAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 8));
            }

        }
        else {
            if (angle > Math.PI / 3) {
                setLastDirection(Direction.SOUTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.SOUTHWEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.NORTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 12));
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.NORTHWEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 12));
            }
            else {
                setLastDirection(Direction.WEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 4));
            }
        }
    }

    private List<PointF> searchForHome(Point startPoint, WorldMap worldMap, Renderer renderer) {

        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashSet<Node> closedSet = new HashSet<>();

        openList.add(new Node(startPoint, 0));

        while (!openList.isEmpty()) {

            Node currentNode = openList.poll();

            for (Node node : currentNode.getAdjacentNodes()) {

                if (node.getPoint().equals(homeLocation)) {
                    node.setParent(currentNode);
                    return getPath(currentNode);
                }
                else if (node.getPoint().x > 0 &&
                        node.getPoint().x < worldMap.getWidth() &&
                        node.getPoint().y > 0 &&
                        node.getPoint().y < worldMap.getHeight() &&
                        worldMap.getWalls()[node.getPoint().x][node.getPoint().y] != WorldMap.COLLIDE) {

                    boolean intersect = false;

                    for (Entity entity : renderer.getEntities()) {
                        if (entity != this && RectF.intersects(entity.getBounds(), new RectF(node.getPoint().x + 0.05f, node.getPoint().y + 0.05f, node.getPoint().x + 0.95f, node.getPoint().y + 0.95f))) {
                            intersect = true;
                            break;
                        }
                    }

                    if (!closedSet.contains(node) && !intersect) {
                        node.calculateCostTo(homeLocation);
                        openList.offer(node);
                        node.setParent(currentNode);

                        closedSet.add(node);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private Point searchForTrail(Point startPoint, WorldMap worldMap, Renderer renderer) {

        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashSet<Node> closedSet = new HashSet<>();
        Point end = null;
        int highestTrail = 0;

        openList.add(new Node(startPoint, 0));

        while (!openList.isEmpty()) {

            Node currentNode = openList.poll();
            int trail = worldMap.getPlayerTrail()[currentNode.getPoint().x][currentNode.getPoint().y];

            if (trail > highestTrail && !doesLineIntersectWalls(getLocation(), new PointF(currentNode.getPoint()), worldMap.getWalls())) {
                end = currentNode.getPoint();
                highestTrail = trail;
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
                        worldMap.getWalls()[node.getPoint().x][node.getPoint().y] != WorldMap.COLLIDE) {

                    if (!closedSet.contains(node)) {
                        node.calculateCostTo(renderer.getPlayer().getLocation());
                        openList.offer(node);
                        node.setParent(currentNode);

                        closedSet.add(node);
                    }
                }
            }
        }

        Log.d(TAG, "highestTrail: " + highestTrail);

        return end;

    }

    private boolean doesLineIntersectWalls(PointF first, PointF second, byte[][] walls) {

        double changeX = Math.abs(second.x - first.x);
        double changeY = Math.abs(second.y - first.y);

        int currentX = (int) first.x;
        int currentY = (int) first.y;
        int increaseX;
        int increaseY;
        int num = 1;
        double error;

        if (changeX == 0)
        {
            increaseX = 0;
            error = Double.POSITIVE_INFINITY;
        }
        else if (second.x > first.x)
        {
            increaseX = 1;
            num += (int) second.x - currentX;
            error = ((int) first.x + 1 - first.x) * changeY;
        }
        else
        {
            increaseX = -1;
            num += currentX - (int) second.x;
            error = (first.x - (int) first.x) * changeY;
        }

        if (changeY == 0)
        {
            increaseY = 0;
            error -= Double.POSITIVE_INFINITY;
        }
        else if (second.y > first.y)
        {
            increaseY = 1;
            num += (int) second.y - currentY;
            error -= ((int) first.y + 1 - first.y) * changeX;
        }
        else
        {
            increaseY = -1;
            num += currentY - (int) second.y;
            error -= (first.y - (int) first.y) * changeX;
        }

        for (; num > 0; num--) {

            if (walls[currentX][currentY] == WorldMap.COLLIDE) {
                return true;
            }

            if (error > 0) {
                currentY += increaseY;
                error -= changeX;
            }
            else {
                currentX += increaseX;
                error += changeY;
            }

        }

        return false;
    }

    private List<PointF> getPath(Node currentNode) {

        List<PointF> path = new ArrayList<>();

        while (currentNode.getParent() != null) {
            path.add(new PointF(currentNode.getPoint()));
            currentNode = currentNode.getParent();
        }

        return path;
    }

}
