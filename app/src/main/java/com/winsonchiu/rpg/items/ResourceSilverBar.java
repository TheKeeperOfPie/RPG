package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class ResourceSilverBar extends Resource {

    public ResourceSilverBar(int tileSize, PointF location) {
        super(tileSize, location, 0);
        setName("Silver Bar");
        setDescription("A bar of silver");
        setResourceId(R.drawable.i_silverbar);
        setTextureId(208);
        setLastAnimationFrame(getTextureId());
    }

    public ResourceSilverBar(Item item) {
        super(item);
    }
}
