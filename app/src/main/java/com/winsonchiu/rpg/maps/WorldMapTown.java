package com.winsonchiu.rpg.maps;

import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import com.winsonchiu.rpg.Player;
import com.winsonchiu.rpg.R;
import com.winsonchiu.rpg.Renderer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by TheKeeperOfPie on 5/25/2015.
 */
public class WorldMapTown extends WorldMap {

    private static final String TAG = WorldMapTown.class.getCanonicalName();
    private RectF boundsDungeonEntrance;
    private RectF boundsChurch;

    public WorldMapTown(Resources resources) {
        super();

        InputStream is = resources.openRawResource(R.raw.town_start);
        Writer writer = new StringWriter();
        char[] writeBuffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(writeBuffer)) != -1) {
                writer.write(writeBuffer, 0, n);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        boundsDungeonEntrance = new RectF(56, 34, 57, 35);
        boundsChurch = new RectF(6, 42, 47, 60);

        try {
            fromJson(new JSONObject(writer.toString()));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        playerTrail = new byte[width][height];

    }

    @Override
    public void setClearColor() {
        GLES20.glClearColor(0.55294117647f, 0.76862745098f, 0.20784313725f, 1f);
    }

    public PointF getDungeonExitPoint() {
        return new PointF(55, 34);
    }

    public boolean enterDungeon(Renderer renderer) {
        return boundsDungeonEntrance.contains(renderer.getPlayer().getLocation().x + Player.WIDTH_RATIO / 2, renderer.getPlayer().getLocation().y + Player.HEIGHT_RATIO / 2);
    }

    @Override
    public boolean renderRoof(Renderer renderer) {
        return !boundsChurch.contains(renderer.getPlayer().getLocation().x + Player.WIDTH_RATIO / 2, renderer.getPlayer().getLocation().y + Player.HEIGHT_RATIO / 2);
    }

    public PointF getSpawnPoint() {
        return new PointF(26, 37);
    }
}
