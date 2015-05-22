package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class ResourceGold extends Resource {

    public ResourceGold(int tileSize, PointF location) {
        super(tileSize, location);
        name = "Gold";
        description = "A piece of gold";
        resourceId = R.drawable.i_goldcoin;
        textureId = 186;
        setLastAnimationFrame(textureId);
    }
}
