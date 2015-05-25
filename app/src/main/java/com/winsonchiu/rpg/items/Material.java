package com.winsonchiu.rpg.items;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public enum Material {

    BRONZE(1), SILVER(2), GOLD(4),
    AGATE(1), AMETHYST(2), RUBY(4), SAPPHIRE(6), DIAMOND(8);

    private final int modifier;

    Material(int modifier) {
        this.modifier = modifier;
    }

    public int getModifier() {
        return modifier;
    }

    public String getName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

}