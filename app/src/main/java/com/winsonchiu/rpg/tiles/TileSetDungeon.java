package com.winsonchiu.rpg.tiles;

/**
 * Created by TheKeeperOfPie on 5/25/2015.
 */
public class TileSetDungeon extends TileSet {

    private static final int TEXTURE_WALL = 290;
    private static final int TEXTURE_GROUND = 290;
    private static final int TEXTURE_ROOM_GROUND = 290;
    private static final int TEXTURE_PATH_FLOOR = 920;
    private static final int TEXTURE_PATH_TOP_LEFT = 862;
    private static final int TEXTURE_PATH_TOP_RIGHT = 864;
    private static final int TEXTURE_PATH_BOTTOM_LEFT = 976;
    private static final int TEXTURE_PATH_BOTTOM_RIGHT = 978;
    private static final int TEXTURE_PATH_T_UP = 977;
    private static final int TEXTURE_PATH_T_DOWN = 863;
    private static final int TEXTURE_PATH_T_LEFT = 921;
    private static final int TEXTURE_PATH_T_RIGHT = 919;
    private static final int TEXTURE_PATH_HORIZONTAL = 807;
    private static final int TEXTURE_PATH_VERTICAL = 750;

    private static final int TEXTURE_ROOM_FLOOR = 920;
    private static final int TEXTURE_ROOM_TOP_LEFT = 862;
    private static final int TEXTURE_ROOM_TOP_RIGHT = 864;
    private static final int TEXTURE_ROOM_BOTTOM_LEFT = 976;
    private static final int TEXTURE_ROOM_BOTTOM_RIGHT = 978;
    private static final int TEXTURE_ROOM_T_UP = 977;
    private static final int TEXTURE_ROOM_T_DOWN = 863;
    private static final int TEXTURE_ROOM_T_LEFT = 921;
    private static final int TEXTURE_ROOM_T_RIGHT = 919;

    private static final int TEXTURE_CHEST_LEFT_CLOSED = 609;
    private static final int TEXTURE_CHEST_RIGHT_CLOSED = 610;
    private static final int TEXTURE_CHEST_LEFT_OPEN = 666;
    private static final int TEXTURE_CHEST_RIGHT_OPEN = 667;

    private static final int TEXTURE_STAIRS_UP_RIGHT = 1053;
    private static final int TEXTURE_STAIRS_UP_LEFT = 1054;
    private static final int TEXTURE_STAIRS_DOWN_RIGHT = 1056;
    private static final int TEXTURE_STAIRS_DOWN_LEFT = 1055;

    @Override
    public int getTextureForTileType(TileType tileType) {

        switch (tileType) {
            case INVALID:
                return 0;
            case PATH_VERTICAL:
                return TEXTURE_PATH_VERTICAL;
            case PATH_HORIZONTAL:
                return TEXTURE_PATH_HORIZONTAL;
            case PATH_TOP_LEFT:
                return TEXTURE_PATH_TOP_LEFT;
            case PATH_BOTTOM_LEFT:
                return TEXTURE_PATH_BOTTOM_LEFT;
            case PATH_BOTTOM_RIGHT:
                return TEXTURE_PATH_BOTTOM_RIGHT;
            case PATH_TOP_RIGHT:
                return TEXTURE_PATH_TOP_RIGHT;
            case PATH_T_DOWN:
                return TEXTURE_PATH_T_DOWN;
            case PATH_T_RIGHT:
                return TEXTURE_PATH_T_RIGHT;
            case PATH_T_UP:
                return TEXTURE_PATH_T_UP;
            case PATH_T_LEFT:
                return TEXTURE_PATH_T_LEFT;
            case PATH_FLOOR:
                return TEXTURE_PATH_FLOOR;
            case ROOM_T_DOWN:
                return TEXTURE_ROOM_T_DOWN;
            case ROOM_T_RIGHT:
                return TEXTURE_ROOM_T_RIGHT;
            case ROOM_T_UP:
                return TEXTURE_ROOM_T_UP;
            case ROOM_T_LEFT:
                return TEXTURE_ROOM_T_LEFT;
            case ROOM_TOP_LEFT:
                return TEXTURE_ROOM_TOP_LEFT;
            case ROOM_BOTTOM_LEFT:
                return TEXTURE_ROOM_BOTTOM_LEFT;
            case ROOM_BOTTOM_RIGHT:
                return TEXTURE_ROOM_BOTTOM_RIGHT;
            case ROOM_TOP_RIGHT:
                return TEXTURE_ROOM_TOP_RIGHT;
            case WALL:
                return TEXTURE_WALL;
            case GROUND:
                return TEXTURE_GROUND;
            case CHEST_LEFT_CLOSED:
                return TEXTURE_CHEST_LEFT_CLOSED;
            case CHEST_RIGHT_CLOSED:
                return TEXTURE_CHEST_RIGHT_CLOSED;
            case CHEST_LEFT_OPEN:
                return TEXTURE_CHEST_LEFT_OPEN;
            case CHEST_RIGHT_OPEN:
                return TEXTURE_CHEST_RIGHT_OPEN;
            case ROOM_GROUND:
                return TEXTURE_ROOM_GROUND;
            case ROOM_FLOOR:
                return TEXTURE_ROOM_FLOOR;
            case STAIRS_UP_RIGHT:
                return TEXTURE_STAIRS_UP_RIGHT;
            case STAIRS_UP_LEFT:
                return TEXTURE_STAIRS_UP_LEFT;
            case STAIRS_DOWN_RIGHT:
                return TEXTURE_STAIRS_DOWN_RIGHT;
            case STAIRS_DOWN_LEFT:
                return TEXTURE_STAIRS_DOWN_LEFT;
        }

        return 0;
    }
}
