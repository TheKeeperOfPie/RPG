package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.ResourceGold;
import com.winsonchiu.rpg.utils.MathUtils;
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
public class MobAggressive extends Entity {

    private static final String TAG = MobAggressive.class.getCanonicalName();
    private static final float SPEED = 0.003f;
    public static final float WIDTH_RATIO = 0.6f;
    public static final float HEIGHT_RATIO = 0.9f;
    private RectF homeRoom;
    private int searchRadius;
    private PointF targetLocation;
    private Point homeLocation;
    private boolean isAlerted;
    private long attackEndTime;
    private List<PointF> path;

    public MobAggressive(int health,
            int armor,
            int damage,
            int tileSize,
            float widthRatio,
            float heightRatio,
            PointF location,
            float textureRowCount,
            float textureColCount,
            Rect room,
            int searchRadius) {
        super(health, armor, damage, tileSize, widthRatio, heightRatio, location, textureRowCount,
                textureColCount,
                SPEED);
        this.homeLocation = new Point((int) location.x, (int) location.y);
        this.targetLocation = new PointF(location.x, location.y);
        this.homeRoom = new RectF(room.left, room.top, room.right, room.bottom);
        this.searchRadius = searchRadius;
        path = new ArrayList<>();
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        if (System.currentTimeMillis() > getStunEndTime()) {
            calculateNextPosition(renderer);
        }

        super.render(renderer, matrixProjection, matrixView);
    }

    private void calculateAttack(Renderer renderer) {
        PointF playerLocation = renderer.getPlayer().getLocation();

        float differenceX = playerLocation.x - getLocation().x;
        float differenceY = playerLocation.y - getLocation().y;

        PointF endLocation = new PointF(playerLocation.x + differenceX, playerLocation.y + differenceY);

        double distance = MathUtils.distance(playerLocation, getLocation());

        if (distance < 2 && System.currentTimeMillis() > attackEndTime) {
            renderer.addAttack(new AttackMelee(getTileSize(), getDamage(), 1, 1, getLocation(), 250, true, getLastDirection(), this));
//            renderer.addAttack(new AttackRanged(getTileSize(), getDamage(), 1, 1, getLocation(), endLocation,
//                    (long) (distance * 200), true));
            attackEndTime = System.currentTimeMillis() + 2000;
        }

    }

    @Override
    public List<Item> calculateDrops() {

        List<Item> drops = new ArrayList<>();

        Random random = new Random();
        int numDrops = random.nextInt(2) + 1;
        for (int iteration = 0; iteration < numDrops; iteration++) {
            drops.add(new ResourceGold(getTileSize(), getNewCenterLocation()));
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
                playerLocation.y > getLocation().y - searchRadius &&
                !doesLineIntersectWalls(getNewCenterLocation(), player.getNewCenterLocation(), worldMap.getWalls())) {

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
                isAlerted = true;
                path.clear();
            }


        }
        else if (isAlerted) {
            Point location = searchForTrail(new Point((int) getLocation().x, (int) getLocation().y),
                    renderer.getWorldMap(), renderer);
            if (location != null) {
                targetLocation.set(location.x, location.y);
            }
            else {
                isAlerted = false;
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

            Log.d(TAG, "Location: " + getLocation());
            Log.d(TAG, "Target: " + targetLocation);
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

//        PointF storeLocation = new PointF(getLocation().x, getLocation().y);
//
//        getLocation().set(targetLocation.x, targetLocation.y);
//
//        super.render(renderer, matrixProjection, matrixView);
//
//        getLocation().set(storeLocation.x, storeLocation.y);

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
        setVelocityX(getOffsetX() / timeDifference);
        setVelocityY(getOffsetY() / timeDifference);

        setMovementX(targetLocation.x < getLocation().x ? -1 : 1);
        setMovementY(targetLocation.y < getLocation().y ? -1 : 1);

        float calculatedY = getLocation().y + getOffsetY();
        float calculatedX = getLocation().x + getOffsetX();

        if (calculatedX < 0 || calculatedY < 0 || calculatedX >= renderer.getWorldMap()
                .getWidth() || calculatedY >= renderer.getWorldMap()
                .getHeight()) {
            return;
        }

        calculateAnimationFrame();

        boolean moveX = true;
        boolean moveY = true;

        float centerX = getLocation().x + getWidthRatio() / 2;
        float centerY = getLocation().y + getHeightRatio() / 2;
        float radiusX = getWidthRatio() / 2 + 0.5f;
        float radiusY = getHeightRatio() / 2 + 0.5f;

        Point topLeft = new Point((int) (centerX - radiusX), (int) (centerY + radiusY));
        Point topLeftMost = new Point((int) (centerX - getWidthRatio() / 2), (int) (centerY + radiusY));
//        Point top = new Point((int) (centerX), (int) (centerY + radiusY));
        Point topRightMost = new Point((int) (centerX + getWidthRatio() / 2), (int) (centerY + radiusY));
        Point topRight = new Point((int) (centerX + radiusX), (int) (centerY + radiusY));
        Point leftUpper = new Point((int) (centerX - radiusX), (int) (centerY + getHeightRatio() / 2));
//        Point left = new Point((int) (centerX - radiusX), (int) (centerY));
        Point leftLower = new Point((int) (centerX - radiusX), (int) (centerY - getHeightRatio() / 2));
        Point rightUpper = new Point((int) (centerX + radiusX), (int) (centerY + getHeightRatio() / 2));
//        Point right = new Point((int) (centerX + radiusX), (int) (centerY));
        Point rightLower = new Point((int) (centerX + radiusX), (int) (centerY - getHeightRatio() / 2));
        Point bottomLeft = new Point((int) (centerX - radiusX), (int) (centerY - radiusY));
        Point bottomLeftMost = new Point((int) (centerX - getWidthRatio() / 2), (int) (centerY - radiusY));
//        Point bottom = new Point((int) (centerX), (int) (centerY - radiusY));
        Point bottomRightMost = new Point((int) (centerX + getWidthRatio() / 2), (int) (centerY - radiusY));
        Point bottomRight = new Point((int) (centerX + radiusX), (int) (centerY - radiusY));

        if (worldMap.isCollide(leftUpper, leftLower) && getMovementX() < 0) {
//            calculatedY += getOffsetY();
//            Log.d(TAG, "Collide left");
            moveX = false;
        }
        else if (worldMap.isCollide(rightUpper, rightLower) && getMovementX() > 0) {
//            calculatedY += getOffsetY();
//            Log.d(TAG, "Collide right");
            moveX = false;
        }

        if (worldMap.isCollide(topLeftMost, topRightMost) && getMovementY() > 0) {
//            calculatedX += getOffsetX();
//            Log.d(TAG, "Collide top");
            moveY = false;
        }
        else if (worldMap.isCollide(bottomLeftMost, bottomRightMost) && getMovementY() < 0) {
//            calculatedX += getOffsetX();
//            Log.d(TAG, "Collide bottom");
            moveY = false;
        }

//        if (worldMap.isCollide(bottomLeft) && !worldMap.isCollide(topLeft) && getMovementX() < 0) {
//            calculatedX += Math.abs(getOffsetX());
//            calculatedY += Math.abs(getOffsetY());
//            Log.d(TAG, "Collide bottomLeft not topLeft");
//        }
//        else if (worldMap.isCollide(topLeft) && !worldMap.isCollide(bottomLeft) && getMovementX() < 0) {
//            calculatedX += Math.abs(getOffsetX());
//            calculatedY -= Math.abs(getOffsetY());
//            Log.d(TAG, "Collide topLeft not bottomLeft");
//        }

//        if (worldMap.isCollide(bottomRight) && !worldMap.isCollide(topRight) && getMovementX() > 0) {
//            calculatedX -= Math.abs(getOffsetX());
//            calculatedY += Math.abs(getOffsetY());
//            Log.d(TAG, "Collide bottomRight not topRight");
//        }
//        else if (worldMap.isCollide(topRight) && !worldMap.isCollide(bottomRight) && getMovementX() > 0) {
//            calculatedX -= Math.abs(getOffsetX());
//            calculatedY -= Math.abs(getOffsetY());
//            Log.d(TAG, "Collide topRight not bottomRight");
//        }
//
//        if (worldMap.isCollide(bottomLeft) && !worldMap.isCollide(bottomRight) && getMovementY() < 0) {
//            calculatedX += Math.abs(getOffsetX());
//            calculatedY += Math.abs(getOffsetY());
//            Log.d(TAG, "Collide bottomLeft not bottomRight");
//        }
//        else if (worldMap.isCollide(bottomRight) && !worldMap.isCollide(bottomLeft) && getMovementY() < 0) {
//            calculatedX -= Math.abs(getOffsetX());
//            calculatedY += Math.abs(getOffsetY());
//            Log.d(TAG, "Collide bottomRight not bottomLeft");
//        }
//
//        if (worldMap.isCollide(topLeft) && !worldMap.isCollide(topRight) && getMovementY() > 0) {
//            calculatedX += Math.abs(getOffsetX());
//            calculatedY -= Math.abs(getOffsetY());
//            Log.d(TAG, "Collide topLeft not topRight");
//        }
//        else if (worldMap.isCollide(topRight) && !worldMap.isCollide(topLeft) && getMovementY() > 0) {
//            calculatedX -= Math.abs(getOffsetX());
//            calculatedY -= Math.abs(getOffsetY());
//            Log.d(TAG, "Collide topRight not topLeft");
//        }

//        if (worldMap.isCollide(topLeft) && getMovementX() < 0 && getMovementY() > 0) {
//            calculatedX -= 2 * getOffsetX();
//            calculatedY -= 2 * getOffsetY();
//            Log.d(TAG, "Collide topLeft");
//        }
//        else if (worldMap.isCollide(topRight) && getMovementX() > 0 && getMovementY() > 0) {
//            calculatedX -= 2 * getOffsetX();
//            calculatedY -= 2 * getOffsetY();
//            Log.d(TAG, "Collide topRight");
//        }
//        else if (worldMap.isCollide(bottomLeft) && !worldMap.isCollide(left) && !worldMap.isCollide(bottom)&& getMovementX() < 0 && getMovementY() < 0) {
//            calculatedX -= 2 * getOffsetX();
//            calculatedY -= 2 * getOffsetY();
//            Log.d(TAG, "Collide bottomLeft");
//        }
//        else if (worldMap.isCollide(bottomRight) && getMovementX() > 0 && getMovementY() < 0) {
//            calculatedX -= 2 * getOffsetX();
//            calculatedY -= 2 * getOffsetY();
//            Log.d(TAG, "Collide bottomRight");
//        }

        RectF newBounds = new RectF(calculatedX, calculatedY,
                calculatedX + getWidthRatio(),
                calculatedY + getHeightRatio());

        if (RectF.intersects(player.getBounds(), newBounds)) {
            return;
        }

        boolean collidesX = false;
        boolean collidesY = false;

        float boundOffsetX = getWidthRatio() / 4;
        float boundOffsetY = getHeightRatio() / 4;
        boolean collides = false;

        RectF boundTarget = new RectF(calculatedX, calculatedY,
                calculatedX + getWidthRatio(), calculatedY + getHeightRatio());

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

        for (Entity entity : renderer.getEntityMobs()) {
            if (entity != this) {
//                if (RectF.intersects(entity.getBounds(), boundLeft) && !RectF.intersects(entity.getBounds(), boundRight)) {
//                    calculatedX += 2 * Math.abs(getOffsetX());
//                    Log.d(TAG, "Collide boundLeft");
//                }
//                else if (RectF.intersects(entity.getBounds(), boundRight) && !RectF.intersects(entity.getBounds(), boundLeft)) {
//                    calculatedX -= 2 * Math.abs(getOffsetX());
//                    Log.d(TAG, "Collide boundRight");
//                }

//                if (RectF.intersects(entity.getBounds(), boundUp) && !RectF.intersects(entity.getBounds(), boundDown)) {
//                    calculatedY -= 2 * Math.abs(getOffsetY());
//                    Log.d(TAG, "Collide boundUp");
//                }
//                if (RectF.intersects(entity.getBounds(), boundDown) && !RectF.intersects(entity.getBounds(), boundUp)) {
//                    calculatedY += 2 * Math.abs(getOffsetY());
//                    Log.d(TAG, "Collide boundDown");
//                }

                if ((getMovementX() < 0 && RectF.intersects(entity.getBounds(), boundLeft)) || (getMovementX() > 0 && RectF.intersects(
                        entity.getBounds(), boundRight))) {
                    moveX = false;
                }

                if ((getMovementY() > 0 && RectF.intersects(entity.getBounds(), boundUp)) || (getMovementY() < 0 && RectF.intersects(
                        entity.getBounds(), boundDown))) {
                    moveY = false;
                }

                if (RectF.intersects(entity.getBounds(), boundTarget)) {
                    collides = true;
                }

//                if (RectF.intersects(entity.getBounds(), boundX)) {
////                    calculatedY += getOffsetY();
////                    calculatedX = getLocation().x;
//                    moveX = false;
//                }
//                if (RectF.intersects(entity.getBounds(), boundY)) {
////                    calculatedX += getOffsetX();
////                    calculatedY = getLocation().y;
//                    moveY = false;
//                }
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

//        Entity entity = player;
//
//        if (RectF.intersects(entity.getBounds(), boundLeft) && !RectF.intersects(entity.getBounds(), boundRight)) {
//            calculatedX += 2 * Math.abs(getOffsetX());
//            Log.d(TAG, "Collide boundLeft");
//        }
//        else if (RectF.intersects(entity.getBounds(), boundRight) && !RectF.intersects(entity.getBounds(), boundLeft)) {
//            calculatedX -= 2 * Math.abs(getOffsetX());
//            Log.d(TAG, "Collide boundRight");
//        }
//
//        if (RectF.intersects(entity.getBounds(), boundUp) && !RectF.intersects(entity.getBounds(), boundDown)) {
//            calculatedY -= 2 * Math.abs(getOffsetY());
//            Log.d(TAG, "Collide boundUp");
//        }
//        if (RectF.intersects(entity.getBounds(), boundDown) && !RectF.intersects(entity.getBounds(), boundUp)) {
//            calculatedY += 2 * Math.abs(getOffsetY());
//            Log.d(TAG, "Collide boundDown");
//        }
//
//        newBounds = new RectF(calculatedX, calculatedY,
//                calculatedX + getWidthRatio(),
//                calculatedY + getHeightRatio());
//
//        for (Entity entity : renderer.getEntityMobs()) {
//            if (entity != this) {
//                if (RectF.intersects(entity.getBounds(), newBounds)) {
//                    collidesX = true;
//                    collidesY = true;
//                }
//            }
//        }

//        RectF newBoundsX = new RectF(calculatedX, getLocation().y,
//                calculatedX + getWidthRatio(),
//                getLocation().y + getHeightRatio());
//        RectF newBoundsNewX = new RectF(calculatedX, calculatedY,
//                calculatedX + getWidthRatio(),
//                calculatedY + getHeightRatio());
//        RectF newBoundsY = new RectF(getLocation().x, calculatedY,
//                getLocation().x + getWidthRatio(),
//                calculatedY + getHeightRatio());
//        RectF newBoundsNewY = new RectF(calculatedX, calculatedY,
//                calculatedX + getWidthRatio(),
//                calculatedY + getHeightRatio());
//
////        if (RectF.intersects(player.getBounds(), newBoundsX) || RectF.intersects(player.getBounds(), newBoundsY)) {
////            return;
////        }
//
//        for (Entity entity : renderer.getEntityMobs()) {
//            if (entity != this) {
//                if (RectF.intersects(entity.getBounds(), newBoundsX)) {
//                    collidesX = true;
////                    if (!isAlerted) {
////                        targetLocation.set(getLocation().x, getLocation().y);
////                    }
////                    break;
//                    if (RectF.intersects(entity.getBounds(), newBoundsNewX)) {
//                        collidesY = true;
//                    }
//                    break;
//                }
//                if (RectF.intersects(entity.getBounds(), newBoundsY)) {
//                    collidesY = true;
////                    if (!isAlerted) {
////                        targetLocation.set(getLocation().x, getLocation().y);
////                    }
////                    break;
//                    if (RectF.intersects(entity.getBounds(), newBoundsNewY)) {
//                        collidesX = true;
//                    }
//                    break;
//                }
//            }
//        }

        if (moveX && !collidesX) {
            getLocation().set(calculatedX, getLocation().y);
        }

        if (moveY && !collidesY) {
            getLocation().set(getLocation().x, calculatedY);
        }

        if (!path.isEmpty()) {
            Log.d(TAG, "offsetX: " + getOffsetX());
            Log.d(TAG, "offsetY: " + getOffsetY());
        }

    }

//    private void calculateRaytrace(WorldMap worldMap) {
//
//        Point topLeft = new Point((int) (getLocation().x - 0.2f), (int) (getLocation().y + 0.2f));
//        Point top = new Point((int) (getLocation().x), (int) (getLocation().y + 0.2f));
//        Point topRight = new Point((int) (getLocation().x + 0.2f), (int) (getLocation().y + 0.2f));
//        Point left = new Point((int) (getLocation().x - 0.2f), (int) (getLocation().y));
//        Point right = new Point((int) (getLocation().x + 0.2f), (int) (getLocation().y));
//        Point bottomLeft = new Point((int) (getLocation().x - 0.2f), (int) (getLocation().y - 0.2f));
//        Point bottom = new Point((int) (getLocation().x), (int) (getLocation().y - 0.2f));
//        Point bottomRight = new Point((int) (getLocation().x + 0.2f), (int) (getLocation().y - 0.2f));
//
//        if (worldMap.isCollide(topLeft, left, bottomLeft) && getMovementX() < 0) {
//            moveX = false;
//        }
//
//        if (worldMap.isCollide(topRight, right, bottomRight) && getMovementX() > 0) {
//            moveX = false;
//        }
//
//        if (worldMap.isCollide(topLeft, top, topRight) && getMovementY() > 0) {
//            moveY = false;
//        }
//
//        if (worldMap.isCollide(bottomLeft, bottom, bottomRight) && getMovementY() < 0) {
//            moveY = false;
//        }
//
//    }

    private void calculateAnimationFrame() {
        double angle = Math.atan(getOffsetY() / getOffsetX());

        if (getMovementX() > 0) {

            if (angle > Math.PI / 3) {
                setLastDirection(Direction.NORTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4 + 12));
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.NORTHEAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4 + 12));
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.SOUTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4));
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.SOUTHEAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4));
            }
            else {
                setLastDirection(Direction.EAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4 + 8));
            }

        }
        else if (getMovementX() < 0) {
            if (angle > Math.PI / 3) {
                setLastDirection(Direction.SOUTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4));
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.SOUTHWEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4));
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.NORTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4 + 12));
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.NORTHWEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4 + 12));
            }
            else {
                setLastDirection(Direction.WEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4 + 4));
            }
        }
        else if (getMovementY() > 0) {
            setLastDirection(Direction.NORTH);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4 + 12));
        }
        else {
            setLastDirection(Direction.SOUTH);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 4));
        }
    }

    private List<PointF> searchForHome(Point startPoint, WorldMap worldMap, Renderer renderer) {

        Log.d(TAG, "searchForHome");

        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashSet<Node> closedSet = new HashSet<>();

        openList.add(new Node(startPoint, 0));

        while (!openList.isEmpty()) {

            Node currentNode = openList.poll();
            Point point = currentNode.getPoint();

            List<Node> adjacentNodes = new ArrayList<>();

            if (!worldMap.isCollide(point.x - 1, point.y) &&
                    !worldMap.isCollide(point.x - 2, point.y)) {
                adjacentNodes.add(new Node(new Point(point.x - 1, point.y)));
            }
            if (!worldMap.isCollide(point.x + 1, point.y) &&
                    !worldMap.isCollide(point.x + 2, point.y)) {
                adjacentNodes.add(new Node(new Point(point.x + 1, point.y)));
            }
            if (!worldMap.isCollide(point.x, point.y - 1) &&
                    !worldMap.isCollide(point.x, point.y - 2)) {
                adjacentNodes.add(new Node(new Point(point.x, point.y - 1)));
            }
            if (!worldMap.isCollide(point.x, point.y + 1) &&
                    !worldMap.isCollide(point.x, point.y + 2)) {
                adjacentNodes.add(new Node(new Point(point.x, point.y + 1)));
            }

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

                    // TODO: Trace through collision code

                    for (Entity entity : renderer.getEntityMobs()) {
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

//        Log.d(TAG, "highestTrail: " + highestTrail);

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

        return path;
    }
}