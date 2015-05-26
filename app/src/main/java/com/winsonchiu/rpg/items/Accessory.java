package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import org.json.JSONObject;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Accessory extends Equipment {

    public Accessory(PointF location,
            int level,
            Material material) {
        super(location, level, material);
    }

    public Accessory(Item item) {
        super(item);
    }

    public Accessory(JSONObject jsonObject) {
        super(jsonObject);
    }
}
