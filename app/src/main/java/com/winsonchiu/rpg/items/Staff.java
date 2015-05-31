package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

import org.json.JSONObject;

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
        setTextureAndResourceIds();
    }

    public Staff(Item item) {
        super(item);
    }

    public Staff(JSONObject jsonObject) {
        super(jsonObject);
        setTextureAndResourceIds();
    }

    @Override
    public int getDamageBoost() {
        return (new Random().nextInt(getLevel() + 1) + 1) * getMaterial().getModifier();
    }

    @Override
    public void setTextureAndResourceIds() {
        switch (getMaterial()) {
            case RUBY:
                setResourceId(R.drawable.w_mace007);
                setTextureId(284);
                break;
            case SAPPHIRE:
                setResourceId(R.drawable.w_mace008);
                setTextureId(312);
                break;
            default:
                throw new IllegalArgumentException("Staff cannot be made of " + getMaterial().getName());
        }
        setLastAnimationFrame(getTextureId());
    }

    @Override
    public int getMinDamage() {
        return 0;
    }

    @Override
    public int getMaxDamage() {
        return 0;
    }
}