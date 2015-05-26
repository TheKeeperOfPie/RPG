package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

import org.json.JSONObject;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class ResourceBronzeCoin extends Resource {

    public ResourceBronzeCoin(PointF location) {
        super(location, 0);
        setName("Bronze Coin");
        setDescription("A coin made of bronze");
        setResourceId(R.drawable.i_bronzecoin);
        setTextureId(65);
        setLastAnimationFrame(getTextureId());
    }

    public ResourceBronzeCoin(Item item) {
        super(item);
    }

    public ResourceBronzeCoin(JSONObject jsonObject) {
        super(jsonObject);
    }
}
