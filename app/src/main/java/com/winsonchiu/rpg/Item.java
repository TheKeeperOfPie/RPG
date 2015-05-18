package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.util.Log;

/**
 * Created by TheKeeperOfPie on 5/14/2015.
 */
public class Item extends Entity {

    private static final String TAG = Item.class.getCanonicalName();
    private String name;
    private int healthBoost;
    private int armorBoost;
    private int damageBoost;
    private int speedBoost;
    private int resourceId;
    private int quantity = 1;
    private float x;
    private float y;

    public Item(String name, int healthBoost, int armorBoost, int damageBoost, int speedBoost, int tileSize, float widthRatio, float heightRatio, PointF location, int textureName, float textureRowCount, float textureColCount, float movementSpeed) {
        super(tileSize, widthRatio, heightRatio, location, textureName, textureRowCount, textureColCount, movementSpeed);
        x = location.x;
        y = location.y;
        this.name = name;
        this.healthBoost = healthBoost;
        this.armorBoost = armorBoost;
        this.damageBoost = damageBoost;
        this.speedBoost = speedBoost;
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        getLocation().set(x, y);

        super.render(renderer, matrixProjection, matrixView);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealthBoost() {
        return healthBoost;
    }

    public void setHealthBoost(int healthBoost) {
        this.healthBoost = healthBoost;
    }

    public int getArmorBoost() {
        return armorBoost;
    }

    public void setArmorBoost(int armorBoost) {
        this.armorBoost = armorBoost;
    }

    public int getDamageBoost() {
        return damageBoost;
    }

    public void setDamageBoost(int damageBoost) {
        this.damageBoost = damageBoost;
    }

    public int getSpeedBoost() {
        return speedBoost;
    }

    public void setSpeedBoost(int speedBoost) {
        this.speedBoost = speedBoost;
    }

    public int getResourceId() {
        return resourceId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
