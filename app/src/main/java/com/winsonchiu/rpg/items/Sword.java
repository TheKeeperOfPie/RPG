package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

import org.json.JSONObject;

import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Sword extends Weapon {

    public Sword(PointF location, int level, Material material) {
        super(location, level, material);
        setName(getMaterial().getName() + " Sword");
        setDescription(
                "A sword made of " + material.getName() + " which deals " + getMinDamage() + " - " + getMaxDamage() + " damage");
        setTextureAndResourceIds();
    }

    public Sword(Item item) {
        super(item);
    }

    public Sword(JSONObject jsonObject) {
        super(jsonObject);
        setTextureAndResourceIds();
    }

    @Override
    public Material getMaterial() {
        return super.getMaterial();
    }

    @Override
    public int getDamageBoost() {
        return (new Random().nextInt(getLevel() + 1) + 1) * getMaterial().getModifier();
    }

    @Override
    public void setTextureAndResourceIds() {
        switch (getMaterial()) {
            case BRONZE:
                setResourceId(R.drawable.w_sword006);
                setTextureId(315);
                break;
            case SILVER:
                setResourceId(R.drawable.w_sword007);
                setTextureId(343);
                break;
            case GOLD:
                setResourceId(R.drawable.w_sword010);
                setTextureId(400);
                break;
            default:
                throw new IllegalArgumentException("Sword cannot be made of " + getMaterial().getName());
        }
        setLastAnimationFrame(getTextureId());
    }

    @Override
    public int getMinDamage() {
        return getMaterial().getModifier();
    }

    @Override
    public int getMaxDamage() {
        return (getLevel() + 1) * getMaterial().getModifier();
    }
}