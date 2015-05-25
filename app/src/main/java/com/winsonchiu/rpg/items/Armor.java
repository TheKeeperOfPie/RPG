package com.winsonchiu.rpg.items;

import android.graphics.PointF;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Armor extends Equipment {

    public Armor(Item item) {
        super(item);
    }

    public Armor(int tileSize,
            PointF location,
            int level,
            Material material) {
        super(tileSize, location, level, material);
    }

}
