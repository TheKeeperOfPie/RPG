package com.winsonchiu.rpg;

/**
 * Created by TheKeeperOfPie on 5/14/2015.
 */
public class Item {

    private String name;
    private int healthBoost;
    private int armorBoost;
    private int damageBoost;
    private int speedBoost;
    private int resourceId;

    public Item(String name, int healthBoost, int armorBoost, int damageBoost, int speedBoost) {
        this.name = name;
        this.healthBoost = healthBoost;
        this.armorBoost = armorBoost;
        this.damageBoost = damageBoost;
        this.speedBoost = speedBoost;
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
}
