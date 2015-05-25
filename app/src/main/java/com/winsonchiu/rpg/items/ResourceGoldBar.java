package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class ResourceGoldBar extends Resource {

    public ResourceGoldBar(PointF location) {
        super(location, 0);
        setName("Gold Bar");
        setDescription("A bar of gold");
        setResourceId(R.drawable.i_goldbar);
        setTextureId(130);
        setLastAnimationFrame(getTextureId());
    }

    public ResourceGoldBar(Item item) {
        super(item);
    }
}
