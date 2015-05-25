package com.winsonchiu.rpg.items;

import android.graphics.PointF;

import com.winsonchiu.rpg.R;

/**
 * Created by TheKeeperOfPie on 5/22/2015.
 */
public class ResourceBronzeBar extends Resource {

    public ResourceBronzeBar(PointF location) {
        super(location, 0);
        setName("Bronze Bar");
        setDescription("A bar of bronze");
        setResourceId(R.drawable.i_bronzebar);
        setTextureId(37);
        setLastAnimationFrame(getTextureId());
    }

    public ResourceBronzeBar(Item item) {
        super(item);
    }
}
