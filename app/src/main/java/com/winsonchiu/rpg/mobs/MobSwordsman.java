package com.winsonchiu.rpg.mobs;

import android.graphics.PointF;
import android.graphics.Rect;

import com.winsonchiu.rpg.AttackMelee;
import com.winsonchiu.rpg.Renderer;
import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.Material;
import com.winsonchiu.rpg.items.Sword;
import com.winsonchiu.rpg.utils.MathUtils;

import java.util.List;
import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class MobSwordsman extends MobAggressive {

    public MobSwordsman(int health,
            int armor,
            int damage,
            PointF location,
            Rect room, int searchRadius) {
        super(MobType.SWORDSMAN, health, armor, damage, location, room, searchRadius);
    }

    @Override
    public List<Item> calculateDrops() {
        List<Item> drops =  super.calculateDrops();

        Random random = new Random();

        if (random.nextFloat() < 0.05f) {
            drops.add(new Sword(getNewCenterLocation(), 1, Material.BRONZE));
        }

        return drops;
    }

    @Override
    public void calculateAttack(Renderer renderer) {

        PointF playerLocation = renderer.getPlayer().getLocation();

        double distance = MathUtils.distance(playerLocation, getLocation());

        if (distance < 2 && System.currentTimeMillis() > getAttackEndTime() && System.currentTimeMillis() / 200 % 5 == 0) {
            renderer.getWorldMap().addAttack(new AttackMelee(getDamage(), 1, 1, getLocation(), 250, true, getLastDirection(), this));
            setAttackEndTime(System.currentTimeMillis() + 1000);
        }
    }
}
