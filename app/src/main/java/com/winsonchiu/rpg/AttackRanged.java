package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.graphics.RectF;

import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.mobs.Mob;
import com.winsonchiu.rpg.mobs.MobAggressive;

import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public class AttackRanged extends Attack {

    private static final String TAG = AttackRanged.class.getCanonicalName();
    private static final float SPEED = 0.01f;
    private long time;
    private PointF startLocation;
    private PointF endLocation;
    private long startTime;
    private long endTime;

    public AttackRanged(int tileSize,
            int damage,
            int range,
            int accuracy,
            PointF startLocation,
            PointF endLocation,
            long time,
            boolean hostile) {
        super(tileSize, damage, range, accuracy, 1f, 1f, new PointF(startLocation.x, startLocation.y), time, SPEED, hostile);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.time = time;
        setLastAnimationFrame(0);
    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        if (endTime == 0) {
            startTime = System.currentTimeMillis();
            endTime = startTime + time;
        }

        if (System.currentTimeMillis() > endTime) {
            setToDestroy(true);
            return;
        }

        float ratio = (System.currentTimeMillis() - startTime) / (float) (endTime - startTime);

        float offsetX = (endLocation.x - startLocation.x) * ratio;
        float offsetY = (endLocation.y - startLocation.y) * ratio;

        byte[][] walls = renderer.getWorldMap()
                .getWalls();

        int checkFirstX = (int) (startLocation.x + offsetX);
        int checkFirstY = (int) (startLocation.y + offsetY);
        int checkSecondX = (int) (startLocation.x + offsetX + 0.5f);
        int checkSecondY = (int) (startLocation.y + offsetY + 0.5f);

        if (checkFirstX < 0 || checkFirstY < 0 || checkSecondX >= walls.length || checkSecondY >= walls[0].length || walls[checkFirstX][checkFirstY] == WorldMap.COLLIDE || walls[checkSecondX][checkSecondY] == WorldMap.COLLIDE) {
            setToDestroy(true);
            return;
        }

        getLocation().set(startLocation.x + offsetX, startLocation.y + offsetY);

        if (isHostile()) {
            if (RectF.intersects(getBounds(), renderer.getPlayer()
                    .getBounds())) {
                renderer.getPlayer()
                        .applyAttack(this);
                setToDestroy(true);
            }
        }
        else {
            for (Mob mob : renderer.getEntityMobs()) {
                if (mob instanceof MobAggressive && RectF.intersects(mob.getBounds(),
                        getBounds())) {
                    if (mob.applyAttack(this)) {
                        List<Item> drops = mob.calculateDrops();
                        renderer.getWorldMap()
                                .dropItems(drops, mob.getLastDirection(), mob.getNewCenterLocation());
                    }
                    setToDestroy(true);
                    break;
                }
            }
        }

        super.render(renderer, matrixProjection, matrixView);
    }

}