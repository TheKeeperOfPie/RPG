package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.Player;
import com.winsonchiu.rpg.R;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class PotionHealth extends Consumable {

    public PotionHealth(PointF location, int level) {
        super(location, level);
        setName("Health Potion");
        setDescription("A drink that recovers " + level + " health");
        setResourceId(R.drawable.p_red03);
        setTextureId(270);
        setLastAnimationFrame(getTextureId());
    }

    public PotionHealth(Item item) {
        super(item);
    }

    @Override
    public boolean consume(Player player) {
        if (player.getHealth() < player.getMaxHealth()) {
            player.addHealth(getLevel());
            return true;
        }
        return false;
    }

}
