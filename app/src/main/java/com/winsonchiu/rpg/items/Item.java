package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.Entity;
import com.winsonchiu.rpg.Renderer;

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

    protected int healthBoost;
    protected int armorBoost;
    protected int damageBoost;
    protected int speedBoost;
    protected int duration;
    protected int resourceId;
    protected int textureId;
    protected String name;
    protected String description;
    private int quantity = 1;

    // TODO: Move enum constants to some database implementation
    public Item(int tileSize, PointF location) {
        super(0, 0, tileSize, WIDTH_RATIO, HEIGHT_RATIO, new PointF(location.x - WIDTH_RATIO / 2, location.y - HEIGHT_RATIO / 2), TEXTURE_ROW_COUNT, TEXTURE_COL_COUNT,
                0);
    }

    public Item(Item item) {
        super(0, 0, item.getTileSize(), WIDTH_RATIO, HEIGHT_RATIO, new PointF(item.getLocation().x, item.getLocation().y), TEXTURE_ROW_COUNT, TEXTURE_COL_COUNT,
                0);
        this.name = item.getName();
        this.description = item.getDescription();
        this.healthBoost = item.getHealthBoost();
        this.armorBoost = item.getArmorBoost();
        this.damageBoost = item.getDamageBoost();
        this.speedBoost = item.getSpeedBoost();
    }

    //region Getters, setters, changers
    public int getTextureId() {
        return textureId;
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

    public void incrementQuantity(int amount) {
        quantity += amount;
    }

    public Item decrementQuantity() {

        if (quantity == 0) {
            throw new IllegalStateException("Not enough quantity for item: " + toString());
        }

        quantity--;

        return new Item(this);

    }

    public List<Item> decrementQuantity(int amount) {

        if (amount > quantity) {
            throw new IllegalStateException("Not enough quantity for item: " + toString());
        }

        quantity -= amount;

        List<Item> removed = new ArrayList<>(amount);
        for (int num = 0; num < amount; num++) {
            removed.add(new Item(this));
        }

        return removed;
    }

    public int getHealthBoost() {
        return healthBoost;
    }

    public int getArmorBoost() {
        return armorBoost;
    }

    public int getDamageBoost() {
        return damageBoost;
    }

    public int getSpeedBoost() {
        return speedBoost;
    }

    public int getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    //endregion
}