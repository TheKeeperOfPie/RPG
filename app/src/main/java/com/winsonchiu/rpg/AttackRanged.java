package com.winsonchiu.rpg;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by TheKeeperOfPie on 5/3/2015.
 */
public class AttackRanged extends Attack {

    private static final String TAG = AttackRanged.class.getCanonicalName();

    public AttackRanged(RenderValues renderValues, int tileSize, int damage, int range, int accuracy, PointF startLocation, PointF endLocation, long time) {
        super(renderValues, tileSize, damage, range, accuracy, startLocation, endLocation, time);
    }

    public void render(int texture, Renderer renderer,
                       float[] matrixProjection, float[] matrixView) {

        if (endTime == 0) {
            startTime = System.currentTimeMillis();
            endTime = startTime + time;
        }

        if (System.currentTimeMillis() > endTime) {
            isFinished = true;
            return;
        }

        float ratio = (System.currentTimeMillis() - startTime) / (float) (endTime - startTime);

        float offsetX = (endLocation.x - startLocation.x) * ratio;
        float offsetY = (endLocation.y - startLocation.y) * ratio;

        GLES20.glUseProgram(renderValues.getProgram());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        android.opengl.Matrix.setIdentityM(transMatrix, 0);
        android.opengl.Matrix.translateM(transMatrix, 0, (startLocation.x + offsetX) * tileSize,
                                         (startLocation.y + offsetY) * tileSize,
                                         0f);

        android.opengl.Matrix.multiplyMM(matrixProjectionAndView,
                                         0,
                                         renderer.getMatrixProjection(),
                                         0,
                                         transMatrix,
                                         0);

        android.opengl.Matrix.multiplyMM(matrixProjectionAndView,
                                         0,
                                         matrixProjectionAndView,
                                         0,
                                         renderer.getMatrixView(),
                                         0);

        GLES20.glUniform1f(renderValues.getRowCount(), 1f);
        GLES20.glUniform1f(renderValues.getColCount(), 1f);
        GLES20.glUniform1f(renderValues.getAlphaHandle(), 1.0f);
        GLES20.glUniform1i(renderValues.getSpriteFrameHandle(), 0);
        GLES20.glVertexAttribPointer(renderValues.getPositionHandle(), 3,
                                     GLES20.GL_FLOAT, false,
                                     0, vertexBuffer);
        GLES20.glVertexAttribPointer(renderValues.getTexCoordLoc(), 2, GLES20.GL_FLOAT,
                                     false,
                                     0, uvBuffer);

        GLES20.glUniformMatrix4fv(renderValues.getMatrixHandle(), 1, false, matrixProjectionAndView,
                                  0);

        GLES20.glUniform1i(renderValues.getSamplerLoc(), 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
    }

}
