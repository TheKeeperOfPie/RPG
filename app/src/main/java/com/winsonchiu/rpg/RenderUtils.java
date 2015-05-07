package com.winsonchiu.rpg;

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
}
