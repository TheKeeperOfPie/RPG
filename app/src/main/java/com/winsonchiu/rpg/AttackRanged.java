package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.opengl.GLES20;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public class AttackRanged extends Attack {

    private static final String TAG = AttackRanged.class.getCanonicalName();

    public AttackRanged(int texture, int tileSize, int damage, int range, int accuracy, PointF startLocation, PointF endLocation, long time) {
        super(texture, tileSize, damage, range, accuracy, startLocation, endLocation, time);
    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        if (endTime == 0) {
            startTime = System.currentTimeMillis();
            endTime = startTime + time;
        }

        if (System.currentTimeMillis() > endTime) {
            isFinished = true;
            return;
        }


        float ratio = (System.currentTimeMillis() - startTime) / (float) (endTime - startTime);

        float offsetX = (endLocation.x - startLocation.x) * ratio;
        float offsetY = (endLocation.y - startLocation.y) * ratio;

        byte[][] walls = renderer.getWorldMap().getWalls();

        int checkFirstX = (int) (startLocation.x + offsetX);
        int checkFirstY = (int) (startLocation.y + offsetY);
        int checkSecondX = (int) (startLocation.x + offsetX + 0.5f);
        int checkSecondY = (int) (startLocation.y + offsetY + 0.5f);

        if (checkFirstX < 0 || checkFirstY < 0 || checkSecondX >= walls.length || checkSecondY >= walls[0].length || walls[checkFirstX][checkFirstY] == WorldMap.COLLIDE || walls[checkSecondX][checkSecondY] == WorldMap.COLLIDE) {
            isFinished = true;
            return;
        }

        getLocation().set(startLocation.x + offsetX, startLocation.y + offsetY);

        render(renderer, matrixProjection, matrixView);
    }

}
