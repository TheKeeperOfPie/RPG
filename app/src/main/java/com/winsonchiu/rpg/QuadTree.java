package com.winsonchiu.rpg;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/21/2015.
 */
public class QuadTree {


    private static final int MAX_OBJECTS = 4;
    private static final int MAX_LEVELS = 10;
    private int level;
    private QuadTree[] children;
    private RectF bounds;
    private List<Entity> entities;

    public QuadTree(int level, RectF bounds) {
        this.level = level;
        this.bounds = bounds;
        entities = new ArrayList<>();
        children = new QuadTree[4];
    }

    public void clear() {

        entities.clear();

        for (int index = 0; index < children.length; index++) {
            if (children[index] != null) {
                children[index].clear();
                children[index] = null;
            }
        }
    }

    public void split() {

        float x = bounds.left;
        float y = bounds.top;
        float width = bounds.width() / 2;
        float height = bounds.height() / 2;

        children[0] = new QuadTree(level + 1, new RectF(x, y, x + width, y + height));
        children[1] = new QuadTree(level + 1, new RectF(x + width, y, x + 2 * width, y + height));
        children[2] = new QuadTree(level + 1, new RectF(x, y + height, x + width, y + 2 * height));
        children[3] = new QuadTree(level + 1, new RectF(x + width, y + height, x + 2 * width, y + 2 * height));

    }

    public int getIndex(RectF target) {
        int index = -1;
        double verticalMidpoint = bounds.left + (bounds.width() / 2);
        double horizontalMidpoint = bounds.top + (bounds.height() / 2);

        // Object can completely fit within the top quadrants
        boolean topQuadrant = (target.top < horizontalMidpoint && target.top + target.height() < horizontalMidpoint);
        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = (target.top > horizontalMidpoint);

        // Object can completely fit within the left quadrants
        if (target.left < verticalMidpoint && target.left + target.width() < verticalMidpoint) {
            if (topQuadrant) {
                index = 1;
            }
            else if (bottomQuadrant) {
                index = 2;
            }
        }
        // Object can completely fit within the right quadrants
        else if (target.left > verticalMidpoint) {
            if (topQuadrant) {
                index = 0;
            }
            else if (bottomQuadrant) {
                index = 3;
            }
        }

        return index;
    }

    public void insert(Entity entity) {
        if (children[0] != null) {
            int index = getIndex(entity.getBounds());

            if (index != -1) {
                children[index].insert(entity);

                return;
            }
        }

        entities.add(entity);

        if (entities.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (children[0] == null) {
                split();
            }

            int num = 0;
            while (num < entities.size()) {
                int index = getIndex(entities.get(num).getBounds());
                if (index != -1) {
                    children[index].insert(entities.remove(num));
                }
                else {
                    num++;
                }
            }
        }
    }

    public List retrieve(List<Entity> entitiesInBounds, RectF targetBounds) {
        int index = getIndex(targetBounds);
        if (index >= 0 && children[0] != null) {
            children[index].retrieve(entitiesInBounds, targetBounds);
        }

        entitiesInBounds.addAll(entities);

        return entitiesInBounds;
    }

}
