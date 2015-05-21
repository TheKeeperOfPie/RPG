package com.winsonchiu.rpg;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements FragmentInventory.OnFragmentInteractionListener, FragmentRender.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private ControllerInventory controllerInventory;
    private Renderer renderer;
    private GLSurfaceView glSurfaceView;
    private ImageView imageDirectionControls;
    private ImageView imageInteractControl;
    private ImageView imageInventoryControl;
    private FastOutLinearInInterpolator interpolator;
    private RecyclerView recyclerInventory;
    private GridLayoutManager gridLayoutManager;
    private AdapterInventory adapterInventory;
    private ControllerInventory.InventoryListener inventoryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        applyFullscreen();

        setContentView(R.layout.activity_main);

        controllerInventory = new ControllerInventory();

        interpolator = new FastOutLinearInInterpolator();

        renderer = new Renderer(this, new Renderer.EventListener() {
            @Override
            public void pickUpItem(Item item) {
                controllerInventory.addItem(item);
            }

            @Override
            public ControllerInventory getControllerInventory() {
                return controllerInventory;
            }
        });

        glSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setRenderer(renderer);

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
        imageInteractControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    renderer.getPlayer().startNewAttack(renderer);
                    return true;
                }
                return false;
            }
        });

        imageInventoryControl = (ImageView) findViewById(R.id.image_inventory_control);
        imageInventoryControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerInventory.setVisibility(View.VISIBLE);
            }
        });

        inventoryListener = new ControllerInventory.InventoryListener() {
            @Override
            public RecyclerView.Adapter getAdapter() {
                return adapterInventory;
            }
        };

        gridLayoutManager = new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false);

        adapterInventory = new AdapterInventory(this, controllerInventory);

        recyclerInventory = (RecyclerView) findViewById(R.id.recycler_inventory);
        recyclerInventory.setHasFixedSize(true);
        recyclerInventory.setLayoutManager(gridLayoutManager);
        recyclerInventory.setAdapter(adapterInventory);

    }

    private void applyFullscreen() {
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
        applyFullscreen();
        controllerInventory.addListener(inventoryListener);
    }

    @Override
    protected void onPause() {
        controllerInventory.removeListener(inventoryListener);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (recyclerInventory.isShown()) {
            recyclerInventory.setVisibility(View.GONE);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public ControllerInventory getControllerInventory() {
        return controllerInventory;
    }
}