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

    private int health;
    private int maxHealth;
    private int armor;
    private int damage;
    private float movementSpeed;
    private long attackEndTime;
    private long stunEndTime;
    private long damageEndTime;
    protected Attack attack;
    private boolean isAlerted;

    public Mob(int health,
            int armor,
            int damage,
            int tileSize,
            float widthRatio,
            float heightRatio,
            PointF location,
            float textureRowCount,
            float textureColCount,
            float movementSpeed) {
        super(tileSize, widthRatio, heightRatio, location, textureRowCount,
                textureColCount, movementSpeed);
        this.health = health;
        this.maxHealth = health;
        this.armor = armor;
        this.damage = damage;
    }

    public boolean applyAttack(Attack attack) {
        health -= attack.calculateDamage();
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

    public abstract void calculateAnimationFrame();

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
}
