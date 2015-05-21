package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class Player extends Entity {

    public static final int OUT_BOUND_X = 5;
    public static final int OUT_BOUND_Y = 5;
    public static final float WIDTH_RATIO = 0.59999999999f;
    public static final float HEIGHT_RATIO = 0.9f;
    private static final float SPEED = 0.007f;
    private static final int BASE_HEALTH = 20;
    private static final int BASE_ARMOR = 1;

    private static final String TAG = Player.class.getCanonicalName();

    public Player(int tileSize, PointF location) {
        super(BASE_HEALTH, BASE_ARMOR, tileSize, WIDTH_RATIO, HEIGHT_RATIO, location, 4f, 4f,
                SPEED);
    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        float x = getLocation().x;
        float y = getLocation().y;
        boolean moveY = true;
        boolean moveX = true;

        if (getLastFrameTime() > 0) {
            long timeDifference = (System.currentTimeMillis() - getLastFrameTime());
            float offset = timeDifference * getMovementSpeed();
            setOffsetX(offset * getMovementX());
            setOffsetY(offset * getMovementY());
            setVelocityX(getOffsetX() / timeDifference);
            setVelocityY(getOffsetY() / timeDifference);
            float yCalculated;
            float xCalculated;
            byte[][] walls = renderer.getWorldMap()
                    .getWalls();

            float boundOffset = 0.1f;

            RectF boundLeft = new RectF(getLocation().x - boundOffset, getLocation().y,
                    getLocation().x - boundOffset + getWidthRatio(),
                    getLocation().y + getHeightRatio());

            RectF boundRight = new RectF(getLocation().x + boundOffset, getLocation().y,
                    getLocation().x + boundOffset + getWidthRatio(),
                    getLocation().y + getHeightRatio());

            RectF boundUp = new RectF(getLocation().x, getLocation().y + boundOffset,
                    getLocation().x + getWidthRatio(),
                    getLocation().y + boundOffset + getHeightRatio());

            RectF boundDown = new RectF(getLocation().x, getLocation().y - boundOffset,
                    getLocation().x + getWidthRatio(),
                    getLocation().y - boundOffset + getHeightRatio());

            for (Entity entity : renderer.getEntityMobs()) {
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

            if (getMovementY() != 0 && moveY) {

                if (getMovementY() < 0) {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < walls[0].length - 1 &&
                            walls[((int) getLocation().x)][((int) yCalculated)] != WorldMap.COLLIDE &&
                            walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) yCalculated)] != WorldMap.COLLIDE;

//                    int nextX = (int) getLocation().x;
//                    int nextY = (int) getLocation().y;
//
//                    if ((walls[nextX][nextY - 1] == WorldMap.COLLIDE || walls[((int) (getLocation().x + getWidthRatio() * 0.9f))][nextY - 1] == WorldMap.COLLIDE) && yCalculated < nextY) {
//                        moveY = false;
//                        getLocation().set(getLocation().x, nextY);
//                    }
//

                    if ((walls[(int) (x + getWidthRatio())][(int) (y - getHeightRatio())] == WorldMap.COLLIDE)) {
                        Log.d(TAG, "Collide x + width");
                    }
                    if ((walls[(int) x][(int) (y - getHeightRatio())] == WorldMap.COLLIDE)) {
                        Log.d(TAG, "Collide x");
                    }

//                    if ((walls[(int) (x + getWidthRatio() * 0.9f)][(int) (y - getHeightRatio())] == WorldMap.COLLIDE || walls[(int) x][(int) (y - getHeightRatio())] == WorldMap.COLLIDE) && yCalculated < ((int) getLocation().y)) {
//                        moveY = false;
//                        getLocation().set(getLocation().x, (int) getLocation().y);
//                    }
                }
                else {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < walls[0].length - 1 &&
                            walls[((int) getLocation().x)][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE &&
                            walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE;

//                    int nextX = (int) getLocation().x;
//                    int nextY = (int) getLocation().y;
//
//                    Log.d(TAG, "currentY: " + getLocation().y);
//                    Log.d(TAG, "nextY: " + nextY);
//
//                    if ((walls[nextX][nextY + 1] == WorldMap.COLLIDE || walls[((int) (getLocation().x + getWidthRatio() * 0.9f))][nextY + 1] == WorldMap.COLLIDE) && yCalculated - getHeightRatio() > nextY) {
//                        moveY = false;
//                        getLocation().set(getLocation().x, nextY + 1 - getHeightRatio());
//                    }

//                    if ((walls[(int) (x + getWidthRatio() * 0.9f)][(int) (y + getHeightRatio())] == WorldMap.COLLIDE || walls[(int) x][(int) (y + getHeightRatio())] == WorldMap.COLLIDE) && yCalculated + getHeightRatio() > ((int) getLocation().y) + 1) {
//                        moveX = false;
//                        getLocation().set(getLocation().x, ((int) getLocation().y) + 1 - getHeightRatio());
//                    }
                }

                if (moveY) {
                    getLocation().offset(0, getOffsetY());
                }

            }

            if (getMovementX() != 0 && moveX) {

                if (getMovementX() < 0) {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < walls.length - 1 &&
                            walls[((int) xCalculated)][((int) getLocation().y)] != WorldMap.COLLIDE &&
                            walls[((int) xCalculated)][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE;

//                    if ((walls[(int) (x - getWidthRatio())][(int) (y + getHeightRatio() * 0.9f)] == WorldMap.COLLIDE || walls[(int) (x - getWidthRatio())][(int) y] == WorldMap.COLLIDE) && xCalculated < ((int) getLocation().x)) {
//                        moveX = false;
//                        getLocation().set((int) getLocation().x, getLocation().y);
//                    }
                }
                else {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < walls.length - 1 &&
                            walls[((int) (xCalculated + getWidthRatio()))][((int) getLocation().y)] != WorldMap.COLLIDE &&
                            walls[((int) (xCalculated + getWidthRatio()))][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE;

//                    if ((walls[(int) (x + getWidthRatio())][(int) (y + getHeightRatio() * 0.9f)] == WorldMap.COLLIDE || walls[(int) (x + getWidthRatio())][(int) y] == WorldMap.COLLIDE) && xCalculated + getWidthRatio() > ((int) getLocation().x) + 1) {
//                        moveX = false;
//                        getLocation().set(((int) getLocation().x) + 1 - getWidthRatio(), getLocation().y);
//                    }
                }

                if (moveX) {
                    getLocation().offset(getOffsetX(), 0);
                }

            }
        }

        if (getLocation().y > renderer.getOffsetCameraY() + renderer.getScreenHeight() / getTileSize() - OUT_BOUND_Y && getOffsetY() > 0) {
            renderer.offsetCamera(0, getOffsetY());
        }
        else if (getLocation().y < renderer.getOffsetCameraY() + (OUT_BOUND_Y - 1) && getOffsetY() < 0) {
            renderer.offsetCamera(0, getOffsetY());
        }
        if (getLocation().x > renderer.getOffsetCameraX() + renderer.getScreenWidth() / getTileSize() - OUT_BOUND_X && getOffsetX() > 0) {
            renderer.offsetCamera(getOffsetX(), 0);
        }
        else if (getLocation().x < renderer.getOffsetCameraX() + (OUT_BOUND_X - 1) && getOffsetX() < 0) {
            renderer.offsetCamera(getOffsetX(), 0);
        }

        if (getMovementX() < 0) {
            setLastDirection(Direction.WEST);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 4));
        }
        else if (getMovementX() > 0) {
            setLastDirection(Direction.EAST);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 8));
        }
        else if (getMovementY() > 0) {
            setLastDirection(Direction.NORTH);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 12));
        }
        else if (getMovementY() < 0) {
            setLastDirection(Direction.SOUTH);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
        }

        renderer.getWorldMap()
                .refreshPlayerTrail(getLocation());

        super.render(renderer, matrixProjection, matrixView);

    }

    public void startNewAttack(Renderer renderer) {

        // TODO: Redo offsets to start centered on player

        PointF start = new PointF(getLocation().x, getLocation().y);
        PointF end = new PointF(getLocation().x, getLocation().y);
        if (getMovementX() < 0 || getLastDirection() == Direction.WEST) {
            start.offset(-1 * WIDTH_RATIO, 0);
            end.offset(getVelocityX() * 500 - 3, 0);
        }
        else if (getMovementX() > 0 || getLastDirection() == Direction.EAST) {
            start.offset(1 * WIDTH_RATIO, 0);
            end.offset(getVelocityX() * 500 + 3, 0);
        }

        if (getMovementY() < 0 || getLastDirection() == Direction.SOUTH) {
            start.offset(0, -1 * HEIGHT_RATIO);
            end.offset(0, getVelocityY() * 500 - 3);
        }
        else if (getMovementY() > 0 || getLastDirection() == Direction.NORTH) {
            start.offset(0, 1 * HEIGHT_RATIO);
            end.offset(0, getVelocityY() * 500 + 3);
        }

        renderer.addAttack(new AttackRanged(getTileSize(), 1, 1, 1, start, end, 500, false));
    }
}
