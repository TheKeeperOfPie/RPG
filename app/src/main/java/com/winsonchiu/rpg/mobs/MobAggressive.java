package com.winsonchiu.rpg.mobs;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.winsonchiu.rpg.Direction;
import com.winsonchiu.rpg.Entity;
import com.winsonchiu.rpg.Player;
import com.winsonchiu.rpg.Renderer;
import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.PotionHealth;
import com.winsonchiu.rpg.items.ResourceBronzeBar;
import com.winsonchiu.rpg.items.ResourceBronzeCoin;
import com.winsonchiu.rpg.items.ResourceSilverCoin;
import com.winsonchiu.rpg.maps.WorldMap;
import com.winsonchiu.rpg.utils.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/5/2015.
 */
public abstract class MobAggressive extends Mob {

    private static final String TAG = MobAggressive.class.getCanonicalName();
    private static final float SPEED = 0.0035f;
    private RectF homeRoom;
    private int searchRadius;
    private PointF targetLocation;
    private Point homeLocation;
    private List<PointF> path;
    private Random random;

    public MobAggressive(MobType mobType,
            int health,
            int armor,
            int damage,
            PointF location,
            Rect room,
            int searchRadius) {
        super(mobType, health, armor, damage, location, SPEED);
        this.homeLocation = new Point((int) location.x, (int) location.y);
        this.targetLocation = new PointF(location.x, location.y);
        this.homeRoom = new RectF(room.left, room.top, room.right, room.bottom);
        this.searchRadius = searchRadius;
        path = new ArrayList<>();
        random = new Random();
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        if (System.currentTimeMillis() > getStunEndTime()) {
            calculateNextPosition(renderer);
        }

        if (renderer.isPointVisible(getLocation())) {
            super.render(renderer, matrixProjection, matrixView);
        }

    }

    @Override
    public List<Item> calculateDrops() {

        List<Item> drops = new ArrayList<>();

        Random random = new Random();

        drops.add(new ResourceBronzeCoin(getNewCenterLocation()));
        if (random.nextFloat() < 0.25f) {
            drops.add(new PotionHealth(getNewCenterLocation(), random.nextInt(2) + 2));
        }
        if (random.nextFloat() < 0.01f) {
            drops.add(new ResourceBronzeBar(getNewCenterLocation()));
        }
        return drops;
    }

    private void calculateNextPosition(Renderer renderer) {

        Player player = renderer.getPlayer();
        PointF playerLocation = player.getLocation();
        byte[][] walls = renderer.getWorldMap()
                .getWalls();

        WorldMap worldMap = renderer.getWorldMap();

        if (playerLocation.x < getLocation().x + searchRadius &&
                playerLocation.x > getLocation().x - searchRadius &&
                playerLocation.y < getLocation().y + searchRadius &&
                playerLocation.y > getLocation().y - searchRadius) {

            switch (getLastDirection()) {

                case NORTH:
                    if (playerLocation.y < getLocation().y) {
                        return;
                    }
                    break;
                case NORTHEAST:
                    if (playerLocation.y < getLocation().y) {
                        return;
                    }
                    if (playerLocation.x < getLocation().x) {
                        return;
                    }
                    break;
                case EAST:
                    if (playerLocation.x < getLocation().x) {
                        return;
                    }
                    break;
                case SOUTHEAST:
                    if (playerLocation.y > getLocation().y) {
                        return;
                    }
                    if (playerLocation.x < getLocation().x) {
                        return;
                    }
                    break;
                case SOUTH:
                    if (playerLocation.y > getLocation().y) {
                        return;
                    }
                    break;
                case SOUTHWEST:
                    if (playerLocation.y > getLocation().y) {
                        return;
                    }
                    if (playerLocation.x > getLocation().x) {
                        return;
                    }
                    break;
                case WEST:
                    if (playerLocation.x > getLocation().x) {
                        return;
                    }
                    break;
                case NORTHWEST:
                    if (playerLocation.y < getLocation().y) {
                        return;
                    }
                    if (playerLocation.x > getLocation().x) {
                        return;
                    }
                    break;
            }

            if (doesLineIntersectWalls(getNewCenterLocation(), player.getNewCenterLocation(), worldMap.getWalls())) {
                return;
            }

            PointF newTargetLocation = new PointF(playerLocation.x, playerLocation.y);//new PointF(((int) playerLocation.x) + 0.2f, ((int) playerLocation.y) + 0.2f);

            float centerX = newTargetLocation.x + getWidthRatio() / 2;
            float centerY = newTargetLocation.y + getHeightRatio() / 2;
            float radiusX = getWidthRatio() / 2 + 0.2f;
            float radiusY = getHeightRatio() / 2 + 0.2f;

            Point topLeft = new Point((int) (centerX - radiusX), (int) (centerY + radiusY));
            Point top = new Point((int) (centerX), (int) (centerY + radiusY));
            Point topRight = new Point((int) (centerX + radiusX), (int) (centerY + radiusY));
            Point left = new Point((int) (centerX - radiusX), (int) (centerY));
            Point right = new Point((int) (centerX + radiusX), (int) (centerY));
            Point bottomLeft = new Point((int) (centerX - radiusX), (int) (centerY - radiusY));
            Point bottom = new Point((int) (centerX), (int) (centerY - radiusY));
            Point bottomRight = new Point((int) (centerX + radiusX), (int) (centerY - radiusY));

            if (worldMap.isCollide(topLeft)) {
                newTargetLocation.offset(0.1f, -0.1f);
            }
            else if (worldMap.isCollide(bottomLeft)) {
                newTargetLocation.offset(0.1f, 0.1f);
            }

            if (worldMap.isCollide(topRight)) {
                newTargetLocation.offset(-0.1f, -0.1f);
            }
            else if (worldMap.isCollide(bottomRight)) {
                newTargetLocation.offset(-0.1f, 0.1f);
            }

            if (!doesLineIntersectWalls(getLocation(), newTargetLocation, walls) &&
                    !doesLineIntersectWalls(new PointF(getLocation().x + getWidthRatio(),
                            getLocation().y + getHeightRatio()),
                            new PointF(newTargetLocation.x + getWidthRatio(),
                                    newTargetLocation.y + getHeightRatio()), walls) &&
                    !doesLineIntersectWalls(
                            new PointF(getLocation().x, getLocation().y + getHeightRatio()),
                            new PointF(newTargetLocation.x, newTargetLocation.y + getHeightRatio()),
                            walls) &&
                    !doesLineIntersectWalls(
                            new PointF(getLocation().x + getWidthRatio(), getLocation().y),
                            new PointF(newTargetLocation.x + getWidthRatio(), newTargetLocation.y),
                            walls)) {
                targetLocation.set(newTargetLocation.x, newTargetLocation.y);
                setIsAlerted(true);
                path.clear();
            }

        }
        else if (isAlerted()) {
            Point location = searchForTrail(new Point((int) getLocation().x, (int) getLocation().y),
                    renderer.getWorldMap(), renderer);

            if (location != null) {
                targetLocation.set(location.x, location.y);
            }
            else {
                setIsAlerted(false);
            }
        }
        else if (path.isEmpty()) {
            if (!homeRoom.contains(getBounds())) {
                PointF centerPoint = getNewCenterLocation();
                path = searchForHome(new Point((int) centerPoint.x, (int) centerPoint.y),
                        renderer.getWorldMap(), renderer);
                if (!path.isEmpty()) {
                    targetLocation = path.remove(0);
//                targetLocation = path.size() > 1 ? path.get(1) : path.get(0);
                }
            }
        }
        else {
            if (Math.abs(targetLocation.x - getLocation().x) < 0.15f && Math.abs(targetLocation.y - getLocation().y) < 0.15f) {
                targetLocation = path.remove(0);
            }

//            Log.d(TAG, "Location: " + getLocation());
//            Log.d(TAG, "Target: " + targetLocation);
        }

        if (path.isEmpty() && (doesLineIntersectWalls(getLocation(), targetLocation, walls) ||
                doesLineIntersectWalls(new PointF(getLocation().x + getWidthRatio(),
                        getLocation().y + getHeightRatio()),
                        new PointF(targetLocation.x + getWidthRatio(),
                                targetLocation.y + getHeightRatio()), walls) ||
                doesLineIntersectWalls(
                        new PointF(getLocation().x, getLocation().y + getHeightRatio()),
                        new PointF(targetLocation.x, targetLocation.y + getHeightRatio()), walls) ||
                doesLineIntersectWalls(
                        new PointF(getLocation().x + getWidthRatio(), getLocation().y),
                        new PointF(targetLocation.x + getWidthRatio(), targetLocation.y), walls))) {
            return;
        }

        calculateAttack(renderer);

        long timeDifference = (System.currentTimeMillis() - getLastFrameTime());
        float offset = timeDifference * getMovementSpeed();

        float differenceX = targetLocation.x - getLocation().x;
        float differenceY = targetLocation.y - getLocation().y;

        double distance = Math.hypot(differenceX, differenceY);

        float ratio = (float) (offset / distance);

        if (offset > distance) {
            targetLocation.set(getLocation().x, getLocation().y);
            if (random.nextFloat() < (homeRoom.contains(playerLocation.x, playerLocation.y) ? 0.15f : 0.05f)) {
                setLastDirection(Direction.getRandomDirectionFourWay());
                calculateAnimationFrame();
            }
            return;
        }

        setOffsetX(ratio * differenceX);
        setOffsetY(ratio * differenceY);
        setVelocityX(getOffsetX() / timeDifference);
        setVelocityY(getOffsetY() / timeDifference);

        setMovementX(targetLocation.x + 0.5f < getLocation().x + getWidthRatio() / 2 ? -1 : 1);
        setMovementY(targetLocation.y + 0.5f < getLocation().y + getHeightRatio() / 2 ? -1 : 1);

        float calculatedY = getLocation().y + getOffsetY();
        float calculatedX = getLocation().x + getOffsetX();

//        if (calculatedX < 0 || calculatedY < 0 || calculatedX >= renderer.getWorldMap()
//                .getWidth() || calculatedY >= renderer.getWorldMap()
//                .getHeight()) {
//            return;
//        }

        if (Math.abs(getMovementX()) > 0 || Math.abs(getMovementY()) > 0) {
            calculateDirection();
        }

        boolean moveX = true;
        boolean moveY = true;

        float centerX = getLocation().x + getWidthRatio() / 2;
        float centerY = getLocation().y + getHeightRatio() / 2;
        float radiusX = getWidthRatio() / 2 + 0.5f;
        float radiusY = getHeightRatio() / 2 + 0.5f;

        Point topLeftMost = new Point((int) (centerX - getWidthRatio() / 2), (int) (centerY + radiusY));
        Point topRightMost = new Point((int) (centerX + getWidthRatio() / 2), (int) (centerY + radiusY));
        Point leftUpper = new Point((int) (centerX - radiusX), (int) (centerY + getHeightRatio() / 2));
        Point leftLower = new Point((int) (centerX - radiusX), (int) (centerY - getHeightRatio() / 2));
        Point rightUpper = new Point((int) (centerX + radiusX), (int) (centerY + getHeightRatio() / 2));
        Point rightLower = new Point((int) (centerX + radiusX), (int) (centerY - getHeightRatio() / 2));
        Point bottomLeftMost = new Point((int) (centerX - getWidthRatio() / 2), (int) (centerY - radiusY));
        Point bottomRightMost = new Point((int) (centerX + getWidthRatio() / 2), (int) (centerY - radiusY));

        if (getMovementX() < 0 && worldMap.isCollide(leftUpper, leftLower)) {
            moveX = false;
        }
        else if (getMovementX() > 0 && worldMap.isCollide(rightUpper, rightLower)) {
            moveX = false;
        }

        if (getMovementY() > 0 && worldMap.isCollide(topLeftMost, topRightMost)) {
            moveY = false;
        }
        else if (getMovementY() < 0 && worldMap.isCollide(bottomLeftMost, bottomRightMost)) {
            moveY = false;
        }

        RectF newBounds = new RectF(calculatedX, calculatedY,
                calculatedX + getWidthRatio(),
                calculatedY + getHeightRatio());

        if (RectF.intersects(player.getBounds(), newBounds)) {
            return;
        }

        float boundOffsetX = getWidthRatio() / 4;
        float boundOffsetY = getHeightRatio() / 4;

        RectF boundLeft = new RectF(getLocation().x - boundOffsetX, getLocation().y,
                getLocation().x - boundOffsetX + getWidthRatio(),
                getLocation().y + getHeightRatio());

        RectF boundRight = new RectF(getLocation().x + boundOffsetX, getLocation().y,
                getLocation().x + boundOffsetX + getWidthRatio(),
                getLocation().y + getHeightRatio());

        RectF boundUp = new RectF(getLocation().x, getLocation().y + boundOffsetY,
                getLocation().x + getWidthRatio(),
                getLocation().y + boundOffsetY + getHeightRatio());

        RectF boundDown = new RectF(getLocation().x, getLocation().y - boundOffsetY,
                getLocation().x + getWidthRatio(),
                getLocation().y - boundOffsetY + getHeightRatio());

        for (Entity entity : renderer.getWorldMap().getEntityMobs()) {
            if (entity != this) {
                if ((getMovementX() < 0 && RectF.intersects(entity.getBounds(), boundLeft)) || (getMovementX() > 0 && RectF.intersects(
                        entity.getBounds(), boundRight))) {
                    moveX = false;
                }

                if ((getMovementY() > 0 && RectF.intersects(entity.getBounds(), boundUp)) || (getMovementY() < 0 && RectF.intersects(
                        entity.getBounds(), boundDown))) {
                    moveY = false;
                }
            }
        }

        if ((getMovementX() < 0 && RectF.intersects(player.getBounds(), boundLeft)) || (getMovementX() > 0 && RectF.intersects(
                player.getBounds(), boundRight))) {
            moveX = false;
        }

        if ((getMovementY() > 0 && RectF.intersects(player.getBounds(), boundUp)) || (getMovementY() < 0 && RectF.intersects(
                player.getBounds(), boundDown))) {
            moveY = false;
        }

        if (moveX) {
            getLocation().set(calculatedX, getLocation().y);
        }

        if (moveY) {
            getLocation().set(getLocation().x, calculatedY);
        }

    }

    private void calculateDirection() {
        double angle = Math.atan(getOffsetY() / getOffsetX());

        if (getMovementX() > 0) {

            if (angle > Math.PI / 3) {
                setLastDirection(Direction.NORTH);
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.NORTHEAST);
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.SOUTH);
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.SOUTHEAST);
            }
            else {
                setLastDirection(Direction.EAST);
            }

        }
        else if (getMovementX() < 0) {
            if (angle > Math.PI / 3) {
                setLastDirection(Direction.SOUTH);
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.SOUTHWEST);
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.NORTH);
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.NORTHWEST);
            }
            else {
                setLastDirection(Direction.WEST);
            }
        }
        else if (getMovementY() > 0) {
            setLastDirection(Direction.NORTH);
        }
        else {
            setLastDirection(Direction.SOUTH);
        }

        calculateAnimationFrame();
    }

    private List<PointF> searchForHome(Point startPoint, WorldMap worldMap, Renderer renderer) {

        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashSet<Node> closedSet = new HashSet<>();

        openList.add(new Node(startPoint, 0));

        while (!openList.isEmpty()) {

            Node currentNode = openList.poll();
            Point point = currentNode.getPoint();

            List<Node> adjacentNodes = new ArrayList<>();
            int x = point.x;
            int y = point.y;

            if (!worldMap.isCollide(new Rect(x - 2, y - 1, x - 1, y + 1))) {
                adjacentNodes.add(new Node(new Point(x - 1, y)));
            }

            if (!worldMap.isCollide(new Rect(x + 1, y - 1, x + 2, y + 1))) {
                adjacentNodes.add(new Node(new Point(x + 1, y)));
            }

            if (!worldMap.isCollide(new Rect(x - 1, y - 2, x + 1, y - 1))) {
                adjacentNodes.add(new Node(new Point(x, y - 1)));
            }

            if (!worldMap.isCollide(new Rect(x - 1, y + 1, x + 1, y + 2))) {
                adjacentNodes.add(new Node(new Point(x, y + 1)));
            }

//            if (!worldMap.isCollide(x - 1, y) &&
//                    !worldMap.isCollide(x - 2, y) &&
//                    !worldMap.isCollide(x - 1, y - 1) &&
//                    !worldMap.isCollide(x - 2, y - 1) &&
//                    !worldMap.isCollide(x - 1, y + 1) &&
//                    !worldMap.isCollide(x - 2, y + 1)) {
//                adjacentNodes.add(new Node(new Point(x - 1, y)));
//            }
//            if (!worldMap.isCollide(x + 1, y) &&
//                    !worldMap.isCollide(x + 2, y) &&
//                    !worldMap.isCollide(x + 1, y - 1) &&
//                    !worldMap.isCollide(x + 2, y - 1) &&
//                    !worldMap.isCollide(x + 1, y + 1) &&
//                    !worldMap.isCollide(x + 2, y + 1)) {
//                adjacentNodes.add(new Node(new Point(x + 1, y)));
//            }
//            if (!worldMap.isCollide(x, y - 1) &&
//                    !worldMap.isCollide(x, y - 2) &&
//                    !worldMap.isCollide(x - 1, y - 1) &&
//                    !worldMap.isCollide(x - 1, y - 2) &&
//                    !worldMap.isCollide(x + 1, y - 1) &&
//                    !worldMap.isCollide(x + 1, y - 2)) {
//                adjacentNodes.add(new Node(new Point(x, y - 1)));
//            }
//            if (!worldMap.isCollide(x, y + 1) &&
//                    !worldMap.isCollide(x, y + 2) &&
//                    !worldMap.isCollide(x - 1, y + 1) &&
//                    !worldMap.isCollide(x - 1, y + 2) &&
//                    !worldMap.isCollide(x + 1, y + 1) &&
//                    !worldMap.isCollide(x + 1, y + 2)) {
//                adjacentNodes.add(new Node(new Point(x, y + 1)));
//            }

            for (Node node : adjacentNodes) {

                if (node.getPoint()
                        .equals(homeLocation)) {
                    node.setParent(currentNode);
                    return getPath(currentNode);
                }
                else if (node.getPoint().x > 0 &&
                        node.getPoint().x < worldMap.getWidth() &&
                        node.getPoint().y > 0 &&
                        node.getPoint().y < worldMap.getHeight() &&
                        worldMap.getWalls()[node.getPoint().x][node.getPoint().y] != WorldMap.COLLIDE) {

                    boolean intersect = false;

                    // TODO: Trace through and improve collision code

                    for (Entity entity : renderer.getWorldMap().getEntityMobs()) {
                        if (entity != this && RectF.intersects(entity.getBounds(),
                                new RectF(node.getPoint().x + 0.05f, node.getPoint().y + 0.05f,
                                        node.getPoint().x + 0.95f, node.getPoint().y + 0.95f))) {
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

            // TODO: Check for trail validity
//            Point point = currentNode.getPoint();
//            if (trail > highestTrail && !doesLineIntersectWalls(getLocation(), new PointF(point), worldMap.getWalls()) &&
//                    !doesLineIntersectWalls(new PointF(getLocation().x + getWidthRatio(),
//                                    getLocation().y + getHeightRatio()),
//                            new PointF(point.x + getWidthRatio(),
//                                    point.y + getHeightRatio()), worldMap.getWalls()) &&
//                    !doesLineIntersectWalls(
//                            new PointF(getLocation().x, getLocation().y + getHeightRatio()),
//                            new PointF(point.x, point.y + getHeightRatio()), worldMap.getWalls()) &&
//                    !doesLineIntersectWalls(
//                            new PointF(getLocation().x + getWidthRatio(), getLocation().y),
//                            new PointF(point.x + getWidthRatio(), point.y), worldMap.getWalls())) {
//                end = currentNode.getPoint();
//                highestTrail = trail;
//            }

            if (trail > highestTrail && !doesLineIntersectWalls(getLocation(),
                    new PointF(currentNode.getPoint()), worldMap.getWalls())) {
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
                        node.calculateCostTo(renderer.getPlayer()
                                .getLocation());
                        openList.offer(node);
                        node.setParent(currentNode);

                        closedSet.add(node);
                    }
                }
            }
        }

        return highestTrail > 0 ? end : null;

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

        if (changeX == 0) {
            increaseX = 0;
            error = Double.POSITIVE_INFINITY;
        }
        else if (second.x > first.x) {
            increaseX = 1;
            num += (int) second.x - currentX;
            error = ((int) first.x + 1 - first.x) * changeY;
        }
        else {
            increaseX = -1;
            num += currentX - (int) second.x;
            error = (first.x - (int) first.x) * changeY;
        }

        if (changeY == 0) {
            increaseY = 0;
            error -= Double.POSITIVE_INFINITY;
        }
        else if (second.y > first.y) {
            increaseY = 1;
            num += (int) second.y - currentY;
            error -= ((int) first.y + 1 - first.y) * changeX;
        }
        else {
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

        Collections.reverse(path);
        if (!path.isEmpty()) {
            path.remove(0);
        }

        return path;
    }
}