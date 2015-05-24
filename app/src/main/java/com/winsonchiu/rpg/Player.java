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

    public static final float WIDTH_RATIO = 1f;
    public static final float HEIGHT_RATIO = 1f;
    private static final float SPEED = 0.006f;
    private static final int BASE_HEALTH = 20;
    private static final int BASE_ARMOR = 1;
    private static final int BASE_DAMAGE = 1;

    private static final String TAG = Player.class.getCanonicalName();
    private final EventListener eventListener;

    private float outBoundX;
    private float outBoundY;

    public Player(int tileSize, PointF location, float outBoundX, float outBoundY, EventListener eventListener) {
        super(BASE_HEALTH, BASE_ARMOR, BASE_DAMAGE, tileSize, WIDTH_RATIO, HEIGHT_RATIO, location, 21f, 13f,
                SPEED);
        setLastAnimationFrame(130);
        this.outBoundX = outBoundX;
        this.outBoundY = outBoundY;
        this.eventListener = eventListener;
        eventListener.onHealthChanged(getHealth(), getMaxHealth());
    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

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

            float boundOffsetX = getWidthRatio() / 12;
            float boundOffsetY = getHeightRatio() / 8;

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

//            List<Entity> possibleCollisions = new ArrayList<>();
//            renderer.getQuadTree().retrieve(possibleCollisions, new RectF(getLocation().x - 5f, getLocation().y - 5f, getLocation().x + getWidthRatio() + 5f, getLocation().y + getHeightRatio() + 5f));

            // TODO: Fix QuadTree and use it to check for collisions

            for (Entity entity : renderer.getEntityMobs()) {
                if (entity instanceof MobAggressive) {
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

                }
                else {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < walls[0].length - 1 &&
                            walls[((int) getLocation().x)][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE &&
                            walls[((int) (getLocation().x + getWidthRatio() - 0.05f))][((int) (yCalculated + getHeightRatio()))] != WorldMap.COLLIDE;

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

                }
                else {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < walls.length - 1 &&
                            walls[((int) (xCalculated + getWidthRatio()))][((int) getLocation().y)] != WorldMap.COLLIDE &&
                            walls[((int) (xCalculated + getWidthRatio()))][((int) (getLocation().y + getHeightRatio() - 0.05f))] != WorldMap.COLLIDE;

                }

                if (moveX) {
                    getLocation().offset(getOffsetX(), 0);
                }

            }
        }

        if (getLocation().x > renderer.getOffsetCameraX() + renderer.getScreenWidth() / getTileSize() - outBoundX && getOffsetX() > 0) {
            renderer.offsetCamera(getOffsetX(), 0);
        }
        else if (getLocation().x < renderer.getOffsetCameraX() + (outBoundX - 1) && getOffsetX() < 0) {
            renderer.offsetCamera(getOffsetX(), 0);
        }
        if (getLocation().y > renderer.getOffsetCameraY() + renderer.getScreenHeight() / getTileSize() - outBoundY && getOffsetY() > 0) {
            renderer.offsetCamera(0, getOffsetY());
        }
        else if (getLocation().y < renderer.getOffsetCameraY() + (outBoundY - 1) && getOffsetY() < 0) {
            renderer.offsetCamera(0, getOffsetY());
        }

        if (Math.abs(getMovementX()) > 0 || Math.abs(getMovementY()) > 0) {
            calculateAnimationFrame();
        }

        renderer.getWorldMap()
                .refreshPlayerTrail(getLocation());

        super.render(renderer, matrixProjection, matrixView);

    }

    private void calculateAnimationFrame() {
        double angle = Math.atan(getOffsetY() / getOffsetX());

        if (getMovementX() > 0) {

            if (angle > Math.PI / 3) {
                setLastDirection(Direction.NORTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 104));
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.NORTHEAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 104));
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.SOUTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 130));
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.SOUTHEAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 130));
            }
            else {
                setLastDirection(Direction.EAST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 143));
            }

        }
        else if (getMovementX() < 0){
            if (angle > Math.PI / 3) {
                setLastDirection(Direction.SOUTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 130));
            }
            else if (angle > Math.PI / 6) {
                setLastDirection(Direction.SOUTHWEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 130));
            }
            else if (angle < -Math.PI / 3) {
                setLastDirection(Direction.NORTH);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 104));
            }
            else if (angle < -Math.PI / 6) {
                setLastDirection(Direction.NORTHWEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 104));
            }
            else {
                setLastDirection(Direction.WEST);
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 117));
            }
        }
        else if (getMovementY() > 0) {
            setLastDirection(Direction.NORTH);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 104));
        }
        else {
            setLastDirection(Direction.SOUTH);
            setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 130));
        }
    }

    @Override
    public boolean applyAttack(Attack attack) {
        boolean returnValue = super.applyAttack(attack);
        eventListener.onHealthChanged(getHealth(), getMaxHealth());

        return returnValue;
    }

    public void startNewAttack(Renderer renderer) {

        PointF end = new PointF(getLocation().x, getLocation().y);
        PointF start = getNewCenterLocation();
        start.offset(-0.6f / 2, -0.9f / 2);

        switch (getLastDirection()) {

            case NORTH:
                end.offset(0, getVelocityY() * 500 + 3);
                break;
            case NORTHEAST:
                end.offset(getVelocityX() * 500 + 3, getVelocityY() * 500 + 3);
                break;
            case EAST:
                end.offset(getVelocityX() * 500 + 3, 0);
                break;
            case SOUTHEAST:
                end.offset(getVelocityX() * 500 + 3, getVelocityY() * 500 - 3);
                break;
            case SOUTH:
                end.offset(0, getVelocityY() * 500 - 3);
                break;
            case SOUTHWEST:
                end.offset(getVelocityX() * 500 - 3, getVelocityY() * 500 - 3);
                break;
            case WEST:
                end.offset(getVelocityX() * 500 - 3, 0);
                break;
            case NORTHWEST:
                end.offset(getVelocityX() * 500 - 3, getVelocityY() * 500 + 3);
                break;
        }

        renderer.addAttack(new AttackRanged(getTileSize(), 1, 1, 1, start, end, 500, false));
    }

    public interface EventListener {

        void onHealthChanged(int health, int maxHealth);
    }

}
