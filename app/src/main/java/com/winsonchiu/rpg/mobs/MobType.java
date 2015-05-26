package com.winsonchiu.rpg.mobs;

/**
 * Created by TheKeeperOfPie on 5/25/2015.
 */
public enum MobType {

    PLAYER(0),
    SWORDSMAN(36),
    MAGE(72);

    private final int textureOffset;

    MobType(int textureOffset) {
        this.textureOffset = textureOffset;
    }

    public int getTextureOffset() {
        return textureOffset;
    }
}
