package com.winsonchiu.rpg.items;

import android.graphics.PointF;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Weapon extends Equipment {

    public Weapon(PointF location, int level, Material material) {
        super(location, level, material);
    }

    public Weapon(Item item) {
        super(item);
    }
}
