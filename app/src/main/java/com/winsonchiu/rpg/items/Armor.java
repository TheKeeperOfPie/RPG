package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import org.json.JSONObject;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Armor extends Equipment {

    public Armor(Item item) {
        super(item);
    }

    public Armor(PointF location,
            int level,
            Material material) {
        super(location, level, material);
    }

    public Armor(JSONObject jsonObject) {
        super(jsonObject);
    }
}
