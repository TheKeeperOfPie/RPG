package com.winsonchiu.rpg;

import android.graphics.PointF;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class Tile {

    private PointF vertex;
    private int textureId;

    public Tile(PointF vertex, int textureId) {
        this.vertex = vertex;
        this.textureId = textureId;
    }

    public PointF getVertex() {
        return vertex;
    }

    public void setVertex(PointF vertex) {
        this.vertex = vertex;
    }

    public int getTextureId() {
        return textureId;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    @Override
    public String toString() {
        return "Tile{" +
                "vertex=" + vertex +
                ", textureId=" + textureId +
                '}';
    }
}
