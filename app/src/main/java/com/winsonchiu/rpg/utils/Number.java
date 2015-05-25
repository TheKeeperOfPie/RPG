package com.winsonchiu.rpg.utils;

import android.graphics.PointF;

import com.winsonchiu.rpg.Entity;
import com.winsonchiu.rpg.Renderer;

/**
 * Created by TheKeeperOfPie on 5/24/2015.
 */
public class Number extends Entity {

    private static final float WIDTH_RATIO = 0.3125f;
    private static final float HEIGHT_RATIO = 0.4f;

    private float heightOffset;
    private int[] numArray;
    private long startTime;
    private long endTime;
    private long time;
    private boolean isNegative;

    public Number(int tileSize,
            PointF location,
            long time,
            int num,
            Entity source) {
        super(tileSize, WIDTH_RATIO, HEIGHT_RATIO, location, 1f,
                11f, 0);
        this.time = time;
        this.heightOffset = source.getHeightRatio() - HEIGHT_RATIO;
        if (num < 0) {
            isNegative = true;
            num = Math.abs(num);
        }

        String numString = String.valueOf(num);
        numArray = new int[numString.length()];

        for (int index = 0; index < numString.length(); index++) {
            numArray[index] = Integer.valueOf(String.valueOf(numString.charAt(index)));
        }
    }

    @Override
    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        if (endTime == 0) {
            startTime = System.currentTimeMillis();
            endTime = startTime + time;
        }

        if (System.currentTimeMillis() > endTime) {
            setToDestroy(true);
            return;
        }

        float storeX = getLocation().x;
        float storeY = getLocation().y;

        getLocation().offset(0, heightOffset);

        if (isNegative) {
            setLastAnimationFrame(10);
            super.render(renderer, matrixProjection, matrixView);
            getLocation().offset(WIDTH_RATIO, 0);
        }

        for (int num : numArray) {
            setLastAnimationFrame(num);
            super.render(renderer, matrixProjection, matrixView);
            getLocation().offset(WIDTH_RATIO, 0);
        }

        getLocation().set(storeX, storeY);


    }
}
