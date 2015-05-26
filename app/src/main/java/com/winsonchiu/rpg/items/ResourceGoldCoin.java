package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class ResourceGoldCoin extends Resource {

    public ResourceGoldCoin(PointF location) {
        super(location, 0);
        setName("Gold Coin");
        setDescription("A coin made of gold");
        setResourceId(R.drawable.i_goldcoin);
        setTextureId(186);
        setLastAnimationFrame(getTextureId());
    }

    public ResourceGoldCoin(Item item) {
        super(item);
    }

    public ResourceGoldCoin(PointF location, int level) {
        super(location, level);
    }
}
