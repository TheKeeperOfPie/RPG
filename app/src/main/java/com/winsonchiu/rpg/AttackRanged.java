package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public class AttackRanged extends Attack {

    private static final String TAG = AttackRanged.class.getCanonicalName();
    private static final float SPEED = 0.01f;

    public AttackRanged(int texture, int tileSize, int damage, int range, int accuracy, PointF startLocation, PointF endLocation, long time) {
        super(texture, tileSize, damage, range, accuracy, startLocation, endLocation, time, SPEED);
    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        if (endTime == 0) {
            startTime = System.currentTimeMillis();
            endTime = startTime + time;
        }

        if (System.currentTimeMillis() > endTime) {
            setToDestroy(renderer.getWorldMap(), true);
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
            setToDestroy(renderer.getWorldMap(), true);
        }

        getLocation().set(startLocation.x + offsetX, startLocation.y + offsetY);

        for (Entity entity : renderer.getEntities()) {

            if (RectF.intersects(entity.getBounds(), getBounds()) && entity instanceof MobAggressive) {
                entity.setToDestroy(renderer.getWorldMap(), true);
                setToDestroy(renderer.getWorldMap(), true);
                break;
            }

        }

        super.render(renderer, matrixProjection, matrixView);
    }

}
