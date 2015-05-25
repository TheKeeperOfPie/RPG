package com.winsonchiu.rpg.items;

import android.graphics.PointF;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Accessory extends Equipment {

    public Accessory(int tileSize,
            PointF location,
            int level,
            Material material) {
        super(tileSize, location, level, material);
    }

    public Accessory(Item item) {
        super(item);
    }

}
