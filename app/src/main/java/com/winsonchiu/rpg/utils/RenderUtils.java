package com.winsonchiu.rpg.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by TheKeeperOfPie on 5/1/2015.
 */
public class RenderUtils {

    private static final String TAG = RenderUtils.class.getCanonicalName();

    public static int loadShader(int type, String shader) {
        int shaderId = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shaderId, shader);
        GLES20.glCompileShader(shaderId);
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "glError " + error);
        }
        return shaderId;
    }

    public static Drawable getPixelatedDrawable(Resources resources, int resourceId) {

        // Force inScaled off to prevent a blurry texture
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap bitmap = BitmapFactory.decodeResource(resources,
                resourceId, options);

        Drawable drawable;

        if (bitmap.getNinePatchChunk() != null) {
            drawable = new NinePatchDrawable(resources, bitmap, bitmap.getNinePatchChunk(), new Rect(), null);
        }
        else {
            drawable = new BitmapDrawable(resources, bitmap);
            ((BitmapDrawable) drawable).setAntiAlias(false);
        }

        drawable.setDither(false);
        drawable.setFilterBitmap(false);

        return drawable;
    }

}
