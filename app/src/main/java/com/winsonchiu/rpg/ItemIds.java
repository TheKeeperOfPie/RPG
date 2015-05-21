package com.winsonchiu.rpg;

/**
 * Created by TheKeeperOfPie on 5/20/2015.
 */
public enum ItemIds {

    GOLD("Gold", R.drawable.i_goldcoin, 186),
    SWORD("Sword", R.drawable.w_sword010, 400),
    ARMOR("Chestplate", R.drawable.a_armor04, 0);

    private final String name;
    private final int drawable;
    private final int id;

    ItemIds(String name, int drawable, int id) {
        this.name = name;
        this.drawable = drawable;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getDrawable() {
        return drawable;
    }

    public int getId() {
        return id;
    }
}