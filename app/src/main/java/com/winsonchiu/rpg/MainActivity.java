package com.winsonchiu.rpg;

import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private Renderer renderer;
    private GLSurfaceView glSurfaceView;
    private ImageView imageDirectionControls;
    private ImageView imageInteractControl;
    private FastOutLinearInInterpolator interpolator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        else {
            getWindow().getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_main);

        interpolator = new FastOutLinearInInterpolator();
        renderer = new Renderer(this);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        imageDirectionControls = (ImageView) findViewById(R.id.image_direction_controls);
        imageDirectionControls.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // TODO: Adjust speed based on how far stick is shifted

                int width = imageDirectionControls.getWidth();
                int height = imageDirectionControls.getHeight();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:

                        Log.d(TAG, "height: " + height);
                        Log.d(TAG, "y: " + event.getY());
                        if (event.getY() < height / 5 * 2) {
                            // Move up
                            renderer.getPlayer().setMovementY(interpolator.getInterpolation((event.getY() - height) / height * -2 - 0.8f));
                        }
                        else if (event.getY() > height / 5 * 3) {
                            // Move down
                            renderer.getPlayer().setMovementY(interpolator.getInterpolation((event.getY() - height) / height * 2 + 1.2f) * -1);
                        }
                        else {
                            renderer.getPlayer().setMovementY(0);
                        }

                        if (event.getX() < width / 5 * 2) {
                            // Move left
                            renderer.getPlayer().setMovementX(interpolator.getInterpolation((event.getX() - width) / width * -2 - 0.8f) * -1);
                        }
                        else if (event.getX() > width / 5 * 3) {
                            // Move right
                            renderer.getPlayer().setMovementX(interpolator.getInterpolation((event.getX() - width) / width * 2 + 1.2f));
                        }
                        else {
                            renderer.getPlayer().setMovementX(0);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        renderer.getPlayer().setMovementX(0);
                        renderer.getPlayer().setMovementY(0);
                        break;
                }

                return true;
            }
        });

        imageInteractControl = (ImageView) findViewById(R.id.image_interact_control);
        imageInteractControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.getPlayer().startNewAttack();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        glSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        renderer.release();
        super.onStop();
    }
}
