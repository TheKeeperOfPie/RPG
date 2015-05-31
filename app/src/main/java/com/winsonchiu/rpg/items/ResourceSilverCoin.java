package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

import org.json.JSONObject;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class ResourceSilverCoin extends Resource {

    public ResourceSilverCoin(PointF location) {
        super(location, 0);
        setName("Silver Coin");
        setDescription("A coin made of silver");
        setResourceId(R.drawable.i_silvercoin);
        setTextureId(265);
        setLastAnimationFrame(getTextureId());
    }

    public ResourceSilverCoin(Item item) {
        super(item);
    }

    public ResourceSilverCoin(JSONObject jsonObject) {
        super(jsonObject);
        setResourceId(R.drawable.i_silvercoin);
        setTextureId(265);
        setLastAnimationFrame(getTextureId());
    }
}
