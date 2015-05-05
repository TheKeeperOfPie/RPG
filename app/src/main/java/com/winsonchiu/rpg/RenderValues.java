package com.winsonchiu.rpg;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by TheKeeperOfPie on 5/1/2015.
 */
public class RenderValues {

    // Move handles and shaders to specific rendered classes

    private static final String TAG = RenderValues.class.getCanonicalName();
    private int program;
    private int positionHandle;
    private int texCoordLoc;
    private int matrixHandle;
    private int alphaHandle;
    private int samplerLoc;
    private int spriteFrameHandle;
    private int rowCount;
    private int colCount;

    public static final String vertexShaderTiles =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 aTexCoords;" +
            "varying vec2 vTexCoords;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  vTexCoords = aTexCoords;" +
            "}";

    public static final String fragmentShaderTiles =
            "precision mediump float;" +
            "varying vec2 vTexCoords;" +
            "uniform float opacity;" +
            "uniform sampler2D sTexture;" +
            "void main() {" +
            "  gl_FragColor = texture2D(sTexture, vTexCoords);" +
            "  gl_FragColor.a *= opacity;" +
            "}";

    public static final String vertexShaderPlayer =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 aTexCoords;" +
            "uniform float rowCount;" +
            "uniform float colCount;" +
            "uniform int spriteFrame;" +
            "varying vec2 vTexCoords;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  vTexCoords = aTexCoords;" +
            "  vTexCoords.x = (aTexCoords.x + float(mod(float(spriteFrame), rowCount))) / rowCount;" +
            "  vTexCoords.y = (aTexCoords.y + float(spriteFrame / int(colCount))) / colCount;" +
            "}";

    public static final String fragmentShaderPlayer =
            "precision mediump float;" +
            "varying vec2 vTexCoords;" +
            "uniform float opacity;" +
            "uniform sampler2D sTexture;" +
            "void main() {" +
            "  gl_FragColor = texture2D(sTexture, vTexCoords);" +
            "  gl_FragColor.a *= opacity;" +
            "}";

    /**
     * Should only be called from the onSurfaceCreated method of a GLSurfaceView.Renderer
     */
    public RenderValues(String vertexShader, String fragmentShader) {

        program = GLES20.glCreateProgram();
        int vertexShaderId = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        int fragmentShaderId = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        GLES20.glAttachShader(program, vertexShaderId);
        GLES20.glAttachShader(program, fragmentShaderId);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        texCoordLoc = GLES20.glGetAttribLocation(program, "aTexCoords");
        matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        alphaHandle = GLES20.glGetUniformLocation(program, "opacity");
        samplerLoc = GLES20.glGetUniformLocation(program, "sTexture");

        if (vertexShader.equals(vertexShaderPlayer)) {
            spriteFrameHandle = GLES20.glGetUniformLocation(program, "spriteFrame");
            rowCount = GLES20.glGetUniformLocation(program, "rowCount");
            colCount = GLES20.glGetUniformLocation(program, "colCount");
        }

    }

    public int getProgram() {
        return program;
    }

    public int getPositionHandle() {
        return positionHandle;
    }

    public int getTexCoordLoc() {
        return texCoordLoc;
    }

    public int getMatrixHandle() {
        return matrixHandle;
    }

    public int getAlphaHandle() {
        return alphaHandle;
    }

    public int getSamplerLoc() {
        return samplerLoc;
    }

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

    public int getSpriteFrameHandle() {
        return spriteFrameHandle;
    }

    public void setSpriteFrameHandle(int spriteFrameHandle) {
        this.spriteFrameHandle = spriteFrameHandle;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public void setColCount(int colCount) {
        this.colCount = colCount;
    }
}
