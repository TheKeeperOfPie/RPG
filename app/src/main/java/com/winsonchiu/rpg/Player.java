package com.winsonchiu.rpg;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class Player extends Entity {

    public static final int OUT_BOUND_X = 3;
    public static final int OUT_BOUND_Y = 3;
    public static final float HEIGHT_RATIO = 0.9f;
    public static final float WIDTH_RATIO = 0.59999999999f;

    private static final String TAG = Player.class.getCanonicalName();

    // Ratio of grid unit to move every millisecond
    private static final float SPEED = 0.01f;
    private List<Attack> attacks;
    private List<Integer> attacksToRemove;

    public Player(int tileSize, int textureName, PointF location) {
        super(tileSize, WIDTH_RATIO, HEIGHT_RATIO, location, textureName, 4f, 4f);
        attacks = new ArrayList<>();
        attacksToRemove = new ArrayList<>();
    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        boolean moveY = false;
        boolean moveX = false;

        if (getLastFrameTime() > 0) {
            long timeDifference = (System.currentTimeMillis() - getLastFrameTime());
            float offset = (System.currentTimeMillis() - getLastFrameTime()) * SPEED;
            setOffsetX(offset * getMovementX());
            setOffsetY(offset * getMovementY());
            setVelocityX(getOffsetX() / timeDifference);
            setVelocityY(getOffsetY() / timeDifference);
            float yCalculated;
            float xCalculated;
            byte[][] walls = renderer.getWalls();

            if (getMovementY() != 0) {

                if (getMovementY() < 0) {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < walls[0].length - 1 &&
                            renderer.getWalls()[((int) getLocation().x)][((int) yCalculated)] != WorldMap.COLLIDE &&
                            renderer.getWalls()[((int) (getLocation().x + WIDTH_RATIO - 0.05f))][((int) yCalculated)] != WorldMap.COLLIDE;
                }
                else {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < walls[0].length - 1 &&
                            renderer.getWalls()[((int) getLocation().x)][((int) (yCalculated + HEIGHT_RATIO))] != WorldMap.COLLIDE &&
                            renderer.getWalls()[((int) (getLocation().x + WIDTH_RATIO - 0.05f))][((int) (yCalculated + HEIGHT_RATIO))] != WorldMap.COLLIDE;
                }

                if (moveY) {
                    getLocation().offset(0, getOffsetY());

                    if (getLocation().y > renderer.getOffsetCameraY() + renderer.getScreenHeight() / getTileSize() - OUT_BOUND_Y && getOffsetY() > 0) {
                        renderer.offsetCamera(0, getOffsetY());
                    }
                    else if (getLocation().y < renderer.getOffsetCameraY() + (OUT_BOUND_Y - 1) && getOffsetY() < 0) {
                        renderer.offsetCamera(0, getOffsetY());
                    }
                }

            }

            if (getMovementX() != 0) {

                if (getMovementX() < 0) {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < walls.length - 1 &&
                            renderer.getWalls()[((int) xCalculated)][((int) getLocation().y)] != WorldMap.COLLIDE &&
                            renderer.getWalls()[((int) xCalculated)][((int) (getLocation().y + HEIGHT_RATIO - 0.05f))] != WorldMap.COLLIDE;
                }
                else {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < walls.length - 1 &&
                            renderer.getWalls()[((int) (xCalculated + WIDTH_RATIO))][((int) getLocation().y)] != WorldMap.COLLIDE &&
                            renderer.getWalls()[((int) (xCalculated + WIDTH_RATIO))][((int) (getLocation().y + HEIGHT_RATIO - 0.05f))] != WorldMap.COLLIDE;
                }

                if (moveX) {
                    getLocation().offset(getOffsetX(), 0);

                    if (getLocation().x > renderer.getOffsetCameraX() + renderer.getScreenWidth() / getTileSize() - OUT_BOUND_X && getOffsetX() > 0) {
                        renderer.offsetCamera(getOffsetX(), 0);
                    }
                    else if (getLocation().x < renderer.getOffsetCameraX() + (OUT_BOUND_X - 1) && getOffsetX() < 0) {
                        renderer.offsetCamera(getOffsetX(), 0);
                    }
                }

            }
        }
        setLastFrameTime(System.currentTimeMillis());

        if (getMovementX() < 0) {
            setLastDirection(Movement.LEFT);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 4));
        }
        else if (getMovementX() > 0) {
            setLastDirection(Movement.RIGHT);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 8));
        }
        else if (getMovementY() > 0) {
            setLastDirection(Movement.UP);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3 + 12));
        }
        else if (getMovementY() < 0) {
            setLastDirection(Movement.DOWN);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 200) % 3));
        }

        render(matrixProjection, matrixView);

        if (!attacks.isEmpty()) {

            for (int index = 0; index < attacks.size(); index++) {
                Attack attack = attacks.get(index);
                attack.render(renderer, matrixProjection, matrixView);
                if (attack.isFinished()) {
                    attacksToRemove.add(index);
                }
            }

            for (int index = attacksToRemove.size() - 1; index >= 0; index--) {
                attacks.remove((int) attacksToRemove.remove(index));
            }
        }

    }

    public void startNewAttack(Renderer renderer) {

        // TODO: Redo offsets to start centered on player

        PointF start = new PointF(getLocation().x, getLocation().y);
        PointF end = new PointF(getLocation().x, getLocation().y);
        if (getMovementX() < 0 || getLastDirection() == Movement.LEFT) {
            start.offset(-1 * WIDTH_RATIO, 0);
            end.offset(getVelocityX() * 500 - 3, 0);
        }
        else if (getMovementX() > 0 || getLastDirection() == Movement.RIGHT) {
            start.offset(1 * WIDTH_RATIO, 0);
            end.offset(getVelocityX() * 500 + 3, 0);
        }

        if (getMovementY() < 0 || getLastDirection() == Movement.DOWN) {
            start.offset(0, -1 * HEIGHT_RATIO);
            end.offset(0, getVelocityY() * 500 - 3);
        }
        else if (getMovementY() > 0 || getLastDirection() == Movement.UP) {
            start.offset(0, 1 * HEIGHT_RATIO);
            end.offset(0, getVelocityY() * 500 + 3);
        }

        // Make Attack extend Entity
        attacks.add(new AttackRanged(renderer.getTextureNames()[2], getTileSize(), 1, 1, 1, start, end, 500));
    }
}
