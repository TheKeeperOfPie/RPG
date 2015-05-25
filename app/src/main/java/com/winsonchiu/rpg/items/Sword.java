package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Sword extends Weapon {

    public Sword(PointF location, int level, Material material) {
        super(location, level, material);
        setName(getMaterial().getName() + " Sword");
        setDescription(
                "A sword made of " + material.getName() + " which deals " + getMaterial().getModifier() + " - " + ((getLevel() + 1) * getMaterial().getModifier()) + " damage");
        switch (material) {
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
                throw new IllegalArgumentException("Sword cannot be made of " + material.getName());
        }
        setLastAnimationFrame(getTextureId());
    }

    public Sword(Item item) {
        super(item);
    }

    @Override
    public int getDamageBoost() {
        return (new Random().nextInt(getLevel() + 1) + 1) * getMaterial().getModifier();
    }
}