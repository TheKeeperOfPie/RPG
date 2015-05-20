package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public class AttackRanged extends Attack {

    private static final String TAG = AttackRanged.class.getCanonicalName();
    private static final float SPEED = 0.01f;

    public AttackRanged(int tileSize, int damage, int range, int accuracy, PointF startLocation, PointF endLocation, long time) {
        super(tileSize, damage, range, accuracy, startLocation, endLocation, time, SPEED);
    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        if (endTime == 0) {
            startTime = System.currentTimeMillis();
            endTime = startTime + time;
        }

        if (System.currentTimeMillis() > endTime) {
            setToDestroy(true);
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
            setToDestroy(true);
        }

        getLocation().set(startLocation.x + offsetX, startLocation.y + offsetY);

        // TODO: Move to a single pass check using a QuadTree
        for (Entity entity : renderer.getEntityMobs()) {

            if (RectF.intersects(entity.getBounds(), getBounds()) && entity instanceof MobAggressive) {
                List<Item> drops = entity.applyAttack(this);
                if (drops != null) {
                    renderer.getWorldMap().addItems(drops);
                }
                setToDestroy(true);
                break;
            }

        }

        super.render(renderer, matrixProjection, matrixView);
    }

}
