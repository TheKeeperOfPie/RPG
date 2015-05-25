package com.winsonchiu.rpg.items;

import android.graphics.PointF;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class Resource extends Item {

    public Resource(PointF location, int level) {
        super(location, level);
    }

    public Resource(Item item) {
        super(item);
    }
}
