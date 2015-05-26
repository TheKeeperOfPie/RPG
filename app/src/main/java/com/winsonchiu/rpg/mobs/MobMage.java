package com.winsonchiu.rpg.mobs;

import android.graphics.PointF;
import android.graphics.Rect;

import com.winsonchiu.rpg.AttackRanged;
import com.winsonchiu.rpg.Renderer;
import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.Material;
import com.winsonchiu.rpg.items.Staff;
import com.winsonchiu.rpg.utils.MathUtils;

import java.util.List;
import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class MobMage extends MobAggressive {

    public MobMage(int health,
            int armor,
            int damage,
            PointF location,
            Rect room, int searchRadius) {
        super(MobType.MAGE, health, armor, damage, location, room, searchRadius);
    }

    @Override
    public List<Item> calculateDrops() {
        List<Item> drops = super.calculateDrops();

        Random random = new Random();

        if (random.nextFloat() < 0.005f) {
            drops.add(new Staff(getNewCenterLocation(), random.nextInt(1) + 1, Material.RUBY));
        }

        return drops;
    }

    @Override
    public void calculateAttack(Renderer renderer) {

        PointF playerLocation = renderer.getPlayer().getLocation();

        float differenceX = playerLocation.x - getLocation().x;
        float differenceY = playerLocation.y - getLocation().y;

        double distance = MathUtils.distance(playerLocation, getLocation());

        if (distance < 4 && System.currentTimeMillis() > getAttackEndTime()) {
            PointF endLocation = new PointF(playerLocation.x + differenceX, playerLocation.y + differenceY);
            renderer.getWorldMap().addAttack(new AttackRanged(getDamage(), 1, 1, getLocation(), endLocation,
                    (long) (distance * 200), true, getLastDirection()));
            setAttackEndTime(System.currentTimeMillis() + 2500);
        }
    }
}
