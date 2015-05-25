package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.Player;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public abstract class Consumable extends Item {

    public Consumable(PointF location, int level) {
        super(location, level);
    }

    public Consumable(Item item) {
        super(item);
    }

    public abstract boolean consume(Player player);

}
