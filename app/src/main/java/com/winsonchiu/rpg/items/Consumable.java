package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.Player;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public abstract class Consumable extends Item {

    public Consumable(int tileSize, PointF location, int level) {
        super(tileSize, location, level);
    }

    public Consumable(Item item) {
        super(item);
    }

    public abstract void consume(Player player);

}
