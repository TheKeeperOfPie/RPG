package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;

import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.Staff;
import com.winsonchiu.rpg.items.Sword;
import com.winsonchiu.rpg.items.Weapon;
import com.winsonchiu.rpg.maps.WorldMap;
import com.winsonchiu.rpg.mobs.Mob;
import com.winsonchiu.rpg.mobs.MobAggressive;
import com.winsonchiu.rpg.mobs.MobType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class Player extends Mob {

    private static final float SPEED = 0.005f;
    private static final int BASE_HEALTH = 20;
    private static final int BASE_ARMOR = 1;
    private static final int BASE_DAMAGE = 1;
    private static final long HEALTH_TICK = 15000;

    private static final String TAG = Player.class.getCanonicalName();
    private final EventListener eventListener;

    private long healthTick;

    public Player(EventListener eventListener) {
        super(MobType.PLAYER, BASE_HEALTH, BASE_ARMOR, BASE_DAMAGE, new PointF(0, 0), SPEED);
        this.eventListener = eventListener;
        healthTick = System.currentTimeMillis() / HEALTH_TICK;
        eventListener.onHealthChanged(getHealth(), getMaxHealth());
    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        long currentTick = System.currentTimeMillis() / HEALTH_TICK;
        if (currentTick > healthTick) {
            addHealth((int) (currentTick - healthTick));
            healthTick = currentTick;
        }

        WorldMap worldMap = renderer.getWorldMap();
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

            // TODO: Fix QuadTree and use it to check for collisions

            for (Entity entity : worldMap.getEntityMobs()) {

                if (renderer.isPointVisible(entity.getLocation())) {

                    if (entity instanceof MobAggressive) {
                        if ((getMovementX() < 0 && RectF.intersects(entity.getBounds(),
                                boundLeft)) || (getMovementX() > 0 && RectF.intersects(
                                entity.getBounds(), boundRight))) {
                            moveX = false;
                        }

                        if ((getMovementY() > 0 && RectF.intersects(entity.getBounds(),
                                boundUp)) || (getMovementY() < 0 && RectF.intersects(
                                entity.getBounds(), boundDown))) {
                            moveY = false;
                        }
                    }
                }
            }

            if (getMovementY() != 0 && moveY) {

                if (getMovementY() < 0) {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < worldMap.getHeight() - 1 &&
                            !worldMap.isCollide((int) (getLocation().x + 0.05f), ((int) yCalculated)) &&
                            !worldMap.isCollide((int) (getLocation().x + getWidthRatio() - 0.1f), ((int) yCalculated));

                }
                else {
                    yCalculated = getLocation().y + getOffsetY();
                    moveY = yCalculated > 1 &&
                            yCalculated < worldMap.getHeight() - 1 &&
                            !worldMap.isCollide((int) (getLocation().x + 0.05f), (int) (yCalculated + getHeightRatio())) &&
                            !worldMap.isCollide((int) (getLocation().x + getWidthRatio() - 0.1f), (int) (yCalculated + getHeightRatio()));

                }

                if (moveY) {
                    getLocation().offset(0, getOffsetY());
                }

            }

            if (getMovementX() != 0 && moveX) {

                if (getMovementX() < 0) {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < worldMap.getWidth() - 1 &&
                            !worldMap.isCollide((int) xCalculated, (int) (getLocation().y + 0.05f)) &&
                            !worldMap.isCollide((int) xCalculated, (int) (getLocation().y + getHeightRatio() - 0.1f));

                }
                else {
                    xCalculated = getLocation().x + getOffsetX();
                    moveX = xCalculated > 1 &&
                            xCalculated < worldMap.getWidth() - 1 &&
                            !worldMap.isCollide((int) (xCalculated + getWidthRatio()), (int) (getLocation().y + 0.05f)) &&
                            !worldMap.isCollide((int) (xCalculated + getWidthRatio()), (int) (getLocation().y + getHeightRatio() - 0.1f));

                }

                if (moveX) {
                    getLocation().offset(getOffsetX(), 0);
                }

            }
        }

        if (getLocation().x > renderer.getOffsetCameraX() && getOffsetX() > 0) {
            renderer.offsetCamera(getOffsetX(), 0);
        }
        else if (getLocation().x < renderer.getOffsetCameraX() && getOffsetX() < 0) {
            renderer.offsetCamera(getOffsetX(), 0);
        }
        if (getLocation().y > renderer.getOffsetCameraY() && getOffsetY() > 0) {
            renderer.offsetCamera(0, getOffsetY());
        }
        else if (getLocation().y < renderer.getOffsetCameraY() && getOffsetY() < 0) {
            renderer.offsetCamera(0, getOffsetY());
        }

        if (Math.abs(getMovementX()) > 0 || Math.abs(getMovementY()) > 0) {
            calculateDirection();
        }

        worldMap.refreshPlayerTrail(getLocation());

        GLES20.glUniform1i(getDamageLocation(), 0);
        super.render(renderer, matrixProjection, matrixView);

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
        else if (getMovementX() < 0){
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

    @Override
    public boolean applyAttack(Attack attack) {
        boolean returnValue = super.applyAttack(attack);
        eventListener.onHealthChanged(getHealth(), getMaxHealth());

        return returnValue;
    }

    @Override
    public void calculateAttack(Renderer renderer) {
        if (System.currentTimeMillis() < getAttackEndTime()) {
            return;
        }
        Weapon weapon = eventListener.getWeapon();
        if (weapon instanceof Sword) {
            renderer.getWorldMap().addAttack(
                    new AttackMelee(getDamage(), 1, 1, getLocation(), 300, false,
                            getLastDirection(), this));
        }
        else if (weapon instanceof Staff) {

            PointF end = new PointF(getLocation().x, getLocation().y);
            PointF start = getNewCenterLocation();
            start.offset(-0.6f / 2, -0.9f / 2);
            long time = 1250;

            switch (getLastDirection()) {

                case NORTH:
                    end.offset(0, getVelocityY() * time + 5);
                    break;
                case NORTHEAST:
                    end.offset(getVelocityX() * time + 5, getVelocityY() * time + 5);
                    break;
                case EAST:
                    end.offset(getVelocityX() * time + 5, 0);
                    break;
                case SOUTHEAST:
                    end.offset(getVelocityX() * time + 5, getVelocityY() * time - 5);
                    break;
                case SOUTH:
                    end.offset(0, getVelocityY() * time - 5);
                    break;
                case SOUTHWEST:
                    end.offset(getVelocityX() * time - 5, getVelocityY() * time - 5);
                    break;
                case WEST:
                    end.offset(getVelocityX() * time - 5, 0);
                    break;
                case NORTHWEST:
                    end.offset(getVelocityX() * time - 5, getVelocityY() * time + 5);
                    break;
            }

            renderer.getWorldMap().addAttack(new AttackRanged(getDamage(), 1, 1, getLocation(), end,
                    time, false, getLastDirection()));
        }
        else {
            renderer.getWorldMap().addAttack(
                    new AttackMelee(1, 1, 1, getLocation(), 300, false,
                            getLastDirection(), this));
        }
        setAttackEndTime(System.currentTimeMillis() + 500);
    }

    @Override
    public int getDamage() {
        return eventListener.calculateDamage();
    }

    @Override
    public List<Item> calculateDrops() {
        return new ArrayList<>();
    }

    public void addHealth(int level) {
        setHealth(getHealth() + level);
        if (getHealth() > getMaxHealth()) {
            setHealth(getMaxHealth());
        }
        eventListener.onHealthChanged(getHealth(), getMaxHealth());
    }

    public interface EventListener {

        void onHealthChanged(int health, int maxHealth);
        int calculateDamage();
        Weapon getWeapon();
    }

}
