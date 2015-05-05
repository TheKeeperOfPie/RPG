package com.winsonchiu.rpg;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/2/2015.
 */
public class Map {

    private static final String TAG = Map.class.getCanonicalName();

    public static final byte DOORWAY = 0;
    public static final byte COLLIDE = 1;
    public static final byte ABOVE = 2;

    private static final String IS_DOORWAY = "Doorway";
    private static final String IS_COLLIDE = "Collide";
    private static final String IS_ABOVE = "Above";

    private List<Tile> tilesBelow;
    private List<Tile> tilesAbove;
    private byte[][] walls;
    private int width;
    private int height;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static Map fromJson(JSONObject jsonObject) throws JSONException {

        int width = jsonObject.getInt("width");
        int height = jsonObject.getInt("height");
        Map map = new Map(width, height);
        JSONArray layers = jsonObject.getJSONArray("layers");
        ArrayList<Tile> tilesBelow = new ArrayList<>();
        ArrayList<Tile> tilesAbove = new ArrayList<>();
        byte[][] walls = new byte[width][height];

        for (int index = 0; index < layers.length(); index++) {
            JSONObject layer = layers.getJSONObject(index);
            JSONArray data = layer.getJSONArray("data");
            int position = 0;

            byte type = 0;

            if (layer.getString("name").contains(IS_DOORWAY)) {
                type = DOORWAY;
            }
            else if (layer.getString("name").contains(IS_COLLIDE)) {
                type = COLLIDE;
            }

            boolean isAbove = layer.getString("name").contains(IS_ABOVE);

            for (int y = 0; y < width; y++) {
                for (int x = 0; x < height; x++) {
                    int value = data.getInt(position++);

                    if (value > 0) {
                        if (isAbove) {
                            tilesAbove.add(new Tile(new PointF(x, height - 1 - y), value));
                        }
                        else {
                            tilesBelow.add(new Tile(new PointF(x, height - 1 - y), value));
                        }
                        if (type > 0) {
                            walls[x][height - 1 - y] = type;
                        }
                    }
                }
            }
        }

        map.setTilesBelow(tilesBelow);
        map.setTilesAbove(tilesAbove);
        map.setWalls(walls);

        return map;

    }

    public byte[][] getWalls() {
        return walls;
    }

    public void setWalls(byte[][] walls) {
        this.walls = walls;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<Tile> getTilesBelow() {
        return tilesBelow;
    }

    public void setTilesBelow(List<Tile> tilesBelow) {
        this.tilesBelow = tilesBelow;
    }

    public List<Tile> getTilesAbove() {
        return tilesAbove;
    }

    public void setTilesAbove(List<Tile> tilesAbove) {
        this.tilesAbove = tilesAbove;
    }
}
