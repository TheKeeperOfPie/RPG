package com.winsonchiu.rpg.mobs;

import android.graphics.PointF;
import android.graphics.Rect;

import com.winsonchiu.rpg.AttackMelee;
import com.winsonchiu.rpg.AttackRanged;
import com.winsonchiu.rpg.Renderer;
import com.winsonchiu.rpg.utils.MathUtils;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class MobMage extends MobAggressive {

    public MobMage(int health,
            int armor,
            int damage,
            float widthRatio,
            float heightRatio,
            PointF location,
            float textureRowCount,
            float textureColCount, Rect room, int searchRadius) {
        super(health, armor, damage, widthRatio, heightRatio, location, textureRowCount,
                textureColCount, room, searchRadius);
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
