package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class ResourceSilverCoin extends Resource {

    public ResourceSilverCoin(int tileSize, PointF location) {
        super(tileSize, location, 0);
        setName("Silver Coin");
        setDescription("A coin made of silver");
        setResourceId(R.drawable.i_silvercoin);
        setTextureId(265);
        setLastAnimationFrame(getTextureId());
    }

    public ResourceSilverCoin(Item item) {
        super(item);
    }
}