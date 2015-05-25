package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Staff extends Weapon {

    public Staff(PointF location, int level, Material material) {
        super(location, level, material);
        setName(getMaterial().getName() + " Staff");
        setDescription(
                "A staff crafted with " + material.getName() + " which deals " + getMaterial().getModifier() + " - " + ((getLevel() + 1) * getMaterial().getModifier()) + " damage");
        switch (material) {
            case RUBY:
                setResourceId(R.drawable.w_mace007);
                setTextureId(284);
                break;
            case SAPPHIRE:
                setResourceId(R.drawable.w_mace008);
                setTextureId(312);
                break;
            default:
                throw new IllegalArgumentException("Staff cannot be made of " + material.getName());
        }
        setLastAnimationFrame(getTextureId());
    }

    public Staff(Item item) {
        super(item);
    }

    @Override
    public int getDamageBoost() {
        return (new Random().nextInt(getLevel() + 1) + 1) * getMaterial().getModifier();
    }
}