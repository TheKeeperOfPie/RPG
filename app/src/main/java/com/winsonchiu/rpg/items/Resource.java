package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import org.json.JSONObject;

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

    public Resource(JSONObject jsonObject) {
        super(jsonObject);
    }
}
