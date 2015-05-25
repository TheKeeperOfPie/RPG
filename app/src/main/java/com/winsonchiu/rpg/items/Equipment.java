package com.winsonchiu.rpg.items;

import android.graphics.PointF;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Equipment extends Item {

    private Material material;

    public Equipment(PointF location, int level, Material material) {
        super(location, level);
        this.material = material;
    }

    public Equipment(Item item) {
        super(item);
        this.material = ((Equipment) item).getMaterial();
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}
