package com.winsonchiu.rpg;

import java.util.Random;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public enum Direction {

    NORTH(0, 1),
    NORTHEAST(1, 1),
    EAST(1, 0),
    SOUTHEAST(1, -1),
    SOUTH(0, -1),
    SOUTHWEST(-1, -1),
    WEST(-1, 0),
    NORTHWEST(-1, 1);

    private static Direction[] directionValues;
    private static Random random;
    private final int offsetX;
    private final int offsetY;

    static {
        directionValues = values();
        random = new Random();
    }

    Direction(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public static Direction offset(Direction direction, int offsetDirection) {

        int ordinal = (direction.ordinal() + offsetDirection) % directionValues.length;
        if (ordinal < 0) {
            ordinal += directionValues.length;
        }

        return directionValues[ordinal];
    }

    public static Direction getRandomDirection() {
        return directionValues[random.nextInt(directionValues.length)];
    }

    public static Direction getRandomDirectionFourWay() {
        return directionValues[random.nextInt(directionValues.length / 2) * 2];
    }

}