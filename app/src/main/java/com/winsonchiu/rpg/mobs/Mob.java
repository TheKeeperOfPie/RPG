package com.winsonchiu.rpg.mobs;

import android.graphics.PointF;
import android.opengl.GLES20;

import com.winsonchiu.rpg.Attack;
import com.winsonchiu.rpg.Entity;
import com.winsonchiu.rpg.Renderer;
import com.winsonchiu.rpg.items.Item;

import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public abstract class Mob extends Entity {

    public static final float WIDTH_RATIO = 1f;
    public static final float HEIGHT_RATIO = 1f;

    private final MobType mobType;
    private int health;
    private int maxHealth;
    private int armor;
    private int damage;
    private long attackEndTime;
    private long stunEndTime;
    private long damageEndTime;
    protected Attack attack;
    private boolean isAlerted;

    public Mob(MobType mobType,
            int health,
            int armor,
            int damage,
            PointF location,
            float movementSpeed) {
        super(WIDTH_RATIO, HEIGHT_RATIO, location, 12f,
                9f, movementSpeed);
        this.mobType = mobType;
        this.health = health;
        this.maxHealth = health;
        this.armor = armor;
        this.damage = damage;
        setLastAnimationFrame(18 + mobType.getTextureOffset());
    }

    public boolean applyAttack(Attack attack) {
        health -= attack.getDamage();
        stunEndTime = System.currentTimeMillis() + 250;
        damageEndTime = System.currentTimeMillis() + 250;
        isAlerted = true;
        if (health <= 0) {
            setToDestroy(true);
            return true;
        }
        return false;
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {
        if (System.currentTimeMillis() < damageEndTime) {
            GLES20.glUniform1i(getDamageLocation(), 1);
        }
        else {
            GLES20.glUniform1i(getDamageLocation(), 0);
        }
        super.render(renderer, matrixProjection, matrixView);
    }

    public abstract void calculateAttack(Renderer renderer);

    public abstract List<Item> calculateDrops();

    public void calculateAnimationFrame() {
        switch (getLastDirection()) {

            case NORTH:
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9) + mobType.getTextureOffset());
                break;
            case NORTHEAST:
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9) + mobType.getTextureOffset());
                break;
            case EAST:
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 27) + mobType.getTextureOffset());
                break;
            case SOUTHEAST:
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 18) + mobType.getTextureOffset());
                break;
            case SOUTH:
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 18) + mobType.getTextureOffset());
                break;
            case SOUTHWEST:
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 18) + mobType.getTextureOffset());
                break;
            case WEST:
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9 + 9) + mobType.getTextureOffset());
                break;
            case NORTHWEST:
                setLastAnimationFrame((int) ((System.currentTimeMillis() / 100) % 9) + mobType.getTextureOffset());
                break;
        }
    }

    //region Getters and setters
    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public long getAttackEndTime() {
        return attackEndTime;
    }

    public void setAttackEndTime(long attackEndTime) {
        this.attackEndTime = attackEndTime;
    }

    public long getStunEndTime() {
        return stunEndTime;
    }

    public void setStunEndTime(long stunEndTime) {
        this.stunEndTime = stunEndTime;
    }

    public long getDamageEndTime() {
        return damageEndTime;
    }

    public void setDamageEndTime(long damageEndTime) {
        this.damageEndTime = damageEndTime;
    }

    public Attack getAttack() {
        return attack;
    }

    public void setAttack(Attack attack) {
        this.attack = attack;
    }

    public boolean isAlerted() {
        return isAlerted;
    }

    public void setIsAlerted(boolean isAlerted) {
        this.isAlerted = isAlerted;
    }
    //endregion
}
