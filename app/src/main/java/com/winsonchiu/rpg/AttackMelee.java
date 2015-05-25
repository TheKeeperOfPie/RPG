package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.graphics.RectF;

import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.mobs.Mob;
import com.winsonchiu.rpg.mobs.MobAggressive;
import com.winsonchiu.rpg.utils.Number;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public class AttackMelee extends Attack {

    private static final String TAG = AttackMelee.class.getCanonicalName();

    private float widthRatioOffset;
    private float heightRatioOffset;
    private long time;
    private boolean isHostile;
    private long endTime;
    private List<Entity> entities;
    private RectF bounds;

    public AttackMelee(int tileSize,
            int damage,
            int range,
            int accuracy,
            PointF location,
            long time,
            boolean isHostile,
            Direction direction,
            Entity source) {
        super(tileSize, damage, range, accuracy, 1f, range * 2, location, time, 0, isHostile);
        this.isHostile = isHostile;
        this.time = time;
        entities = new ArrayList<>();
        bounds = new RectF();
        setLastAnimationFrame(1);
        setLastDirection(direction);

        switch(getLastDirection()) {

            case NORTH:
                setAngle(180);
                break;
            case NORTHEAST:
                setAngle(135);
                break;
            case EAST:
                setAngle(90);
                break;
            case SOUTHEAST:
                setAngle(45);
                break;
            case SOUTH:
                setAngle(0);
                break;
            case SOUTHWEST:
                setAngle(315);
                break;
            case WEST:
                setAngle(270);
                break;
            case NORTHWEST:
                setAngle(225);
                break;
        }

        widthRatioOffset = source.getWidthRatio();
        heightRatioOffset = source.getHeightRatio();

    }

    public void render(Renderer renderer, float[] matrixProjection, float[] matrixView) {

        if (endTime == 0) {
            endTime = System.currentTimeMillis() + time;
        }

        if (System.currentTimeMillis() > endTime) {
            entities.clear();
            setToDestroy(true);
            return;
        }

        PointF centerLocation = new PointF(getLocation().x + widthRatioOffset / 2, getLocation().y + heightRatioOffset / 2);

        float storeX = getLocation().x;
        float storeY = getLocation().y;

        getLocation().offset(widthRatioOffset / 2 - getWidthRatio() / 2,
                heightRatioOffset / 2 - getHeightRatio() / 2);
        getLocation().offset(getLastDirection().getOffsetX() * 0.25f,
                getLastDirection().getOffsetY() * 0.25f);

        super.render(renderer, matrixProjection, matrixView);

        // TODO: Move to a rotation based algorithm
        switch (getLastDirection()) {

            case NORTH:
                bounds.set(centerLocation.x - getWidthRatio() / 2, centerLocation.y, centerLocation.x + getWidthRatio() / 2, centerLocation.y + getRange());
                break;
            case NORTHEAST:
                bounds.set(centerLocation.x, centerLocation.y, centerLocation.x + getRange(), centerLocation.y + getRange());
                break;
            case EAST:
                bounds.set(centerLocation.x, centerLocation.y - getWidthRatio() / 2, centerLocation.x + getRange(), centerLocation.y + getWidthRatio() / 2);
                break;
            case SOUTHEAST:
                bounds.set(centerLocation.x, centerLocation.y - getRange(), centerLocation.x + getRange(), centerLocation.y);
                break;
            case SOUTH:
                bounds.set(centerLocation.x - getWidthRatio() / 2, centerLocation.y - getRange(), centerLocation.x + getWidthRatio() / 2, centerLocation.y);
                break;
            case SOUTHWEST:
                bounds.set(centerLocation.x - getRange(), centerLocation.y - getRange(), centerLocation.x, centerLocation.y);
                break;
            case WEST:
                bounds.set(centerLocation.x - getRange(), centerLocation.y - getWidthRatio() / 2, centerLocation.x, centerLocation.y + getWidthRatio() / 2);
                break;
            case NORTHWEST:
                bounds.set(centerLocation.x - getRange(), centerLocation.y, centerLocation.x, centerLocation.y + getRange());
                break;
        }

        if (isHostile) {
            if (!entities.contains(renderer.getPlayer()) && RectF.intersects(bounds, renderer.getPlayer()
                    .getBounds())) {
                renderer.getPlayer()
                        .applyAttack(this);
                entities.add(renderer.getPlayer());
            }
        }
        else {
            for (Mob mob : renderer.getEntityMobs()) {
                if (mob instanceof MobAggressive && !entities.contains(mob) && RectF.intersects(mob.getBounds(),
                        bounds)) {
                    if (mob.applyAttack(this)) {
                        List<Item> drops = mob.calculateDrops();
                        renderer.getWorldMap()
                                .dropItems(drops, mob.getLastDirection(), mob.getNewCenterLocation());
                    }
                    renderer.addEntity(new Number(getTileSize(), mob.getLocation(), 500, -getDamage(), mob));
                    entities.add(mob);
                    break;
                }
            }
        }

        getLocation().set(storeX, storeY);

    }

    @Override
    public RectF getBounds() {
        return bounds;
    }
}