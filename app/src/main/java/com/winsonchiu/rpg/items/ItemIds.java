package com.winsonchiu.rpg.items;

import com.winsonchiu.rpg.R;

/**
 * Created by TheKeeperOfPie on 5/20/2015.
 */
public enum ItemIds {

    GOLD(R.drawable.i_goldcoin, 186, 0, 0, 0, 0, "Gold", "A piece of gold"),
    SWORD(R.drawable.w_sword010, 400, 0, 0, 0, 0, "Sword", "A worn metal sword"),
    ARMOR(R.drawable.a_armor04, 0, 0, 0, 0, 0, "Chestplate", "A used and tattered chestplate");

    private final int resourceId;
    private final int textureId;
    private final int healthBoost;
    private final int armorBoost;
    private final int damageBoost;
    private final int speedBoost;
    private final String name;
    private final String description;

    ItemIds(int resourceId, int textureId, int healthBoost, int armorBoost, int damageBoost, int speedBoost, String name, String description) {
        this.resourceId = resourceId;
        this.textureId = textureId;
        this.healthBoost = healthBoost;
        this.armorBoost = armorBoost;
        this.damageBoost = damageBoost;
        this.speedBoost = speedBoost;
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public int getTextureId() {
        return textureId;
    }

    public String getDescription() {
        return description;
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
}