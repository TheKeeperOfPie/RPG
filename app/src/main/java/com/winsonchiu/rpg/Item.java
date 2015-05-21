package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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

    public void incrementQuantity(int amount) {
        quantity += amount;
    }

    public Item decrementQuantity() {

        if (quantity == 0) {
            throw new IllegalStateException("Not enough quantity for item: " + toString());
        }

        quantity--;

        return new Item(itemId, healthBoost, armorBoost, damageBoost, speedBoost, getTileSize(), new PointF(getLocation().x, getLocation().y));

    }

    public List<Item> decrementQuantity(int amount) {

        if (amount > quantity) {
            throw new IllegalStateException("Not enough quantity for item: " + toString());
        }

        quantity -= amount;

        List<Item> removed = new ArrayList<>(amount);
        for (int num = 0; num < amount; num++) {
            removed.add(new Item(itemId, healthBoost, armorBoost, damageBoost, speedBoost, getTileSize(), new PointF(getLocation().x, getLocation().y)));
        }

        return removed;
    }

    public ItemIds getItemId() {
        return itemId;
    }

    public void setItemId(ItemIds itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Item item = (Item) o;

        if (getHealthBoost() != item.getHealthBoost()) {
            return false;
        }
        if (getArmorBoost() != item.getArmorBoost()) {
            return false;
        }
        if (getDamageBoost() != item.getDamageBoost()) {
            return false;
        }
        if (getSpeedBoost() != item.getSpeedBoost()) {
            return false;
        }
        return getItemId() == item.getItemId();

    }

    @Override
    public int hashCode() {
        int result = getItemId().hashCode();
        result = 31 * result + getHealthBoost();
        result = 31 * result + getArmorBoost();
        result = 31 * result + getDamageBoost();
        result = 31 * result + getSpeedBoost();
        return result;
    }
}
