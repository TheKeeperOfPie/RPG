package com.winsonchiu.rpg;

/**
 * Created by TheKeeperOfPie on 5/20/2015.
 */
public enum ItemIds {

    GOLD("Gold", R.drawable.i_goldcoin, 186, "A piece of gold"),
    SWORD("Sword", R.drawable.w_sword010, 400, "A worn metal sword"),
    ARMOR("Chestplate", R.drawable.a_armor04, 0, "A used and tattered chestplate");

    private final String name;
    private final String description;
    private final int drawable;
    private final int id;

    ItemIds(String name, int drawable, int id, String description) {
        this.name = name;
        this.drawable = drawable;
        this.id = id;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
}