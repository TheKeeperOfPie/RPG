package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by TheKeeperOfPie on 5/11/2015.
 */
public class MathUtils {

    public static double distance(Point first, Point second) {
        return Math.sqrt(Math.pow(first.x - second.x, 2) + Math.pow(first.y - second.y, 2));
    }

    public static double distance(PointF first, PointF second) {
        return Math.sqrt(Math.pow(first.x - second.x, 2) + Math.pow(first.y - second.y, 2));
    }

}
