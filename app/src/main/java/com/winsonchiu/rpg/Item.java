package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.util.Log;

/**
 * Created by TheKeeperOfPie on 5/14/2015.
 */
public class Item extends Entity {

    private static final String TAG = Item.class.getCanonicalName();

    private static final float WIDTH_RATIO = 0.75f;
    private static final float HEIGHT_RATIO = 0.75f;
    private static final float TEXTURE_ROW_COUNT = 14f;
    private static final float TEXTURE_COL_COUNT = 29f;

    private ItemIds itemId;
    private int healthBoost;
    private int armorBoost;
    private int damageBoost;
    private int speedBoost;
    private int quantity = 1;

    // TODO: Move enum constants to some database implementation
    public Item(ItemIds itemId, int healthBoost, int armorBoost, int damageBoost, int speedBoost, int tileSize, PointF location) {
        super(0, 0, tileSize, WIDTH_RATIO, HEIGHT_RATIO, new PointF(location.x + 0.125f, location.y + 0.125f), TEXTURE_ROW_COUNT, TEXTURE_COL_COUNT,
                0);
        this.itemId = itemId;
        this.healthBoost = healthBoost;
        this.armorBoost = armorBoost;
        this.damageBoost = damageBoost;
        this.speedBoost = speedBoost;
        setLastAnimationFrame(itemId.getId());
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {
        super.render(renderer, matrixProjection, matrixView);
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ItemIds getItemId() {
        return itemId;
    }

    public void setItemId(ItemIds itemId) {
        this.itemId = itemId;
    }
}
