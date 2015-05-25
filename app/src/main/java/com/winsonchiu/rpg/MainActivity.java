package com.winsonchiu.rpg;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.winsonchiu.rpg.items.Accessory;
import com.winsonchiu.rpg.items.Armor;
import com.winsonchiu.rpg.items.Consumable;
import com.winsonchiu.rpg.items.Equipment;
import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.Weapon;
import com.winsonchiu.rpg.utils.RenderUtils;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private ControllerInventory controllerInventory;
    private Renderer renderer;
    private GLSurfaceView glSurfaceView;
    private ImageView imageDirectionControls;
    private ImageView imageInteractControl;
    private ImageView imageInventoryControl;
    private FastOutLinearInInterpolator interpolator;

    private ProgressBar progressBarHealth;
    private LinearLayout layoutHealth;
    private ImageView imageQuickSlot1;
    private ImageView imageQuickSlot2;
    private ImageView imageQuickSlot3;

    // Inventory screen
    private RelativeLayout layoutInventory;
    private RecyclerView recyclerInventory;
    private GridLayoutManager gridLayoutManager;
    private AdapterInventory adapterInventory;
    private ControllerInventory.InventoryListener inventoryListener;
    private ImageButton buttonClose;
    private FrameLayout frameView;
    private TextView textHealth;
    private TextView textArmor;
    private TextView textDamage;
    private TextView textSpeed;
    private ImageView imageEquipSlotWeapon;
    private ImageView imageEquipSlotArmor;
    private ImageView imageEquipSlotAccessory;
    private LinearLayout layoutStats;
    private LinearLayout layoutEquipment;
    private Drawable drawableWeapon;
    private Drawable drawableArmor;
    private Drawable drawableAccessory;
    private Button buttonEquipment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        applyFullscreen();

        controllerInventory = new ControllerInventory();

        interpolator = new FastOutLinearInInterpolator();

        imageDirectionControls = (ImageView) findViewById(R.id.image_direction_controls);
        imageDirectionControls.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int width = imageDirectionControls.getWidth();
                int height = imageDirectionControls.getHeight();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() < height / 5 * 2) {
                            // Move up
                            renderer.getPlayer()
                                    .setMovementY(interpolator.getInterpolation(
                                            (event.getY() - height) / height * -2 - 0.8f));
                        }
                        else if (event.getY() > height / 5 * 3) {
                            // Move down
                            renderer.getPlayer()
                                    .setMovementY(interpolator.getInterpolation(
                                            (event.getY() - height) / height * 2 + 1.2f) * -1);
                        }
                        else {
                            renderer.getPlayer()
                                    .setMovementY(0);
                        }

                        if (event.getX() < width / 5 * 2) {
                            // Move left
                            renderer.getPlayer()
                                    .setMovementX(interpolator.getInterpolation(
                                            (event.getX() - width) / width * -2 - 0.8f) * -1);
                        }
                        else if (event.getX() > width / 5 * 3) {
                            // Move right
                            renderer.getPlayer()
                                    .setMovementX(interpolator.getInterpolation(
                                            (event.getX() - width) / width * 2 + 1.2f));
                        }
                        else {
                            renderer.getPlayer()
                                    .setMovementX(0);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        renderer.getPlayer()
                                .setMovementX(0);
                        renderer.getPlayer()
                                .setMovementY(0);
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
                    renderer.getPlayer()
                            .calculateAttack(renderer);
                    return true;
                }
                return false;
            }
        });

        imageInventoryControl = (ImageView) findViewById(R.id.image_inventory_control);
        imageInventoryControl.setImageDrawable(
                RenderUtils.getPixelatedDrawable(getResources(), R.drawable.i_chest01));
        imageInventoryControl.setBackground(
                RenderUtils.getPixelatedDrawable(getResources(), R.drawable.background_inventory));
        imageInventoryControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayerStatistics();
                frameView.removeAllViewsInLayout();
                frameView.addView(layoutInventory);
                frameView.setVisibility(View.VISIBLE);
            }
        });

        layoutHealth = (LinearLayout) findViewById(R.id.layout_health);
        layoutHealth.setBackground(
                RenderUtils.getPixelatedDrawable(getResources(), R.drawable.background_inventory));
        layoutHealth.getLayoutParams().width = getResources().getDisplayMetrics().widthPixels / 4;

        progressBarHealth = (ProgressBar) findViewById(R.id.progress_bar_health);

        Drawable backgroundItem = RenderUtils.getPixelatedDrawable(getResources(),
                R.drawable.background_item);

        imageQuickSlot1 = (ImageView) findViewById(R.id.cell_quick_slot_1).findViewById(
                R.id.image_icon);
        imageQuickSlot2 = (ImageView) findViewById(R.id.cell_quick_slot_2).findViewById(
                R.id.image_icon);
        imageQuickSlot3 = (ImageView) findViewById(R.id.cell_quick_slot_3).findViewById(
                R.id.image_icon);

        imageQuickSlot1.setBackground(backgroundItem);
        imageQuickSlot2.setBackground(backgroundItem);
        imageQuickSlot3.setBackground(backgroundItem);

        frameView = (FrameLayout) findViewById(R.id.frame_view);

        inflateInventory();

        renderer = new Renderer(this, new Renderer.EventListener() {
            @Override
            public void pickUpItem(Item item) {
                controllerInventory.addItem(item);
            }

            @Override
            public ControllerInventory getControllerInventory() {
                return controllerInventory;
            }
        }, new Player.EventListener() {
            @Override
            public void onHealthChanged(final int health, final int maxHealth) {
                progressBarHealth.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBarHealth.setMax(maxHealth);
                        progressBarHealth.setProgress(health);
                        textHealth.setText(getString(R.string.health) + " " + health + " / " + maxHealth);
                    }
                });
            }

            @Override
            public int calculateDamage() {

                int damage = 0;
                if (controllerInventory.hasWeapon()) {
                    damage += controllerInventory.getWeapon().getDamageBoost();
                }
                if (controllerInventory.hasArmor()) {
                    damage += controllerInventory.getArmor().getDamageBoost();
                }

                if (controllerInventory.hasAccessory()) {
                    damage += controllerInventory.getAccessory().getDamageBoost();
                }

                return damage > 0 ? damage : 1;
            }

            @Override
            public Weapon getWeapon() {
                return controllerInventory.getWeapon();
            }
        });

        glSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    private void setPlayerStatistics() {
        Player player = renderer.getPlayer();
        textHealth.setText(
                getString(
                        R.string.health) + " " + player.getHealth() + " / " + player.getMaxHealth());
        textArmor.setText(getString(R.string.armor) + " " + player.getArmor());
        textDamage.setText(getString(R.string.damage) + " " + player.getDamage());
        textSpeed.setText(getString(R.string.speed) + " " + player.getMovementSpeed());
    }

    private void inflateInventory() {
        layoutInventory = (RelativeLayout) LayoutInflater.from(this)
                .inflate(R.layout.inventory,
                        frameView, false);
        layoutInventory.setBackground(
                RenderUtils.getPixelatedDrawable(getResources(), R.drawable.background_inventory));
        layoutInventory.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        layoutStats = (LinearLayout) layoutInventory.findViewById(R.id.layout_stats);
        layoutStats.setBackground(
                RenderUtils.getPixelatedDrawable(getResources(), R.drawable.background_stats));

        inventoryListener = new ControllerInventory.InventoryListener() {
            @Override
            public RecyclerView.Adapter getAdapter() {
                return adapterInventory;
            }

            @Override
            public void dropItem(Item item) {
                if (!renderer.getWorldMap()
                        .dropItem(item, renderer.getPlayer().getLastDirection(), renderer.getPlayer().getNewCenterLocation())) {
                    Toast.makeText(MainActivity.this, "No room to drop item", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void equipItem() {

            }

            @Override
            public void notifyItemInserted(final int position) {
                recyclerInventory.post(new Runnable() {
                    @Override
                    public void run() {
                        adapterInventory.notifyItemInserted(position);
                    }
                });
            }

            @Override
            public void notifyItemRemoved(final int position) {
                recyclerInventory.post(new Runnable() {
                    @Override
                    public void run() {
                        adapterInventory.notifyItemRemoved(position);
                    }
                });
            }

            @Override
            public void notifyDataSetChanged() {
                recyclerInventory.post(new Runnable() {
                    @Override
                    public void run() {
                        adapterInventory.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void notifyItemChanged(final int position) {
                recyclerInventory.post(new Runnable() {
                    @Override
                    public void run() {
                        adapterInventory.notifyItemChanged(position);
                    }
                });
            }

            @Override
            public void onEquipmentChanged() {
                if (controllerInventory.hasWeapon()) {
                    imageEquipSlotWeapon.setImageDrawable(
                            RenderUtils.getPixelatedDrawable(getResources(),
                                    controllerInventory.getWeapon()
                                            .getResourceId()));
                }
                else {
                    imageEquipSlotWeapon.setImageDrawable(drawableWeapon);
                }
                if (controllerInventory.hasArmor()) {
                    imageEquipSlotArmor.setImageDrawable(
                            RenderUtils.getPixelatedDrawable(getResources(),
                                    controllerInventory.getArmor()
                                            .getResourceId()));
                }
                else {
                    imageEquipSlotArmor.setImageDrawable(drawableArmor);
                }
                if (controllerInventory.hasAccessory()) {
                    imageEquipSlotAccessory.setImageDrawable(
                            RenderUtils.getPixelatedDrawable(getResources(),
                                    controllerInventory.getAccessory()
                                            .getResourceId()));
                }
                else {
                    imageEquipSlotAccessory.setImageDrawable(drawableAccessory);
                }
            }
        };

        gridLayoutManager = new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false);
        adapterInventory = new AdapterInventory(this, controllerInventory,
                new AdapterInventory.EventCallback() {
                    @Override
                    public void showPopupWindow(final int position, int x, int y) {

                        showPopup(controllerInventory.getItem(position), x, y);

                    }
                });

        recyclerInventory = (RecyclerView) layoutInventory.findViewById(R.id.recycler_inventory);
        recyclerInventory.setHasFixedSize(true);
        recyclerInventory.setLayoutManager(gridLayoutManager);
        recyclerInventory.setAdapter(adapterInventory);

        buttonClose = (ImageButton) layoutInventory.findViewById(R.id.button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutEquipment.setVisibility(View.GONE);
                frameView.setVisibility(View.GONE);
            }
        });
        buttonClose.setImageDrawable(
                RenderUtils.getPixelatedDrawable(getResources(), R.drawable.button_close));
        buttonClose.setBackground(
                RenderUtils.getPixelatedDrawable(getResources(), R.drawable.background_item));

        textHealth = (TextView) layoutInventory.findViewById(R.id.text_health);
        textArmor = (TextView) layoutInventory.findViewById(R.id.text_armor);
        textDamage = (TextView) layoutInventory.findViewById(R.id.text_damage);
        textSpeed = (TextView) layoutInventory.findViewById(R.id.text_speed);

        layoutEquipment = (LinearLayout) layoutInventory.findViewById(R.id.layout_equipment);
        layoutEquipment.setBackground(
                RenderUtils.getPixelatedDrawable(getResources(), R.drawable.background_item));


        Drawable backgroundItem = RenderUtils.getPixelatedDrawable(getResources(),
                R.drawable.background_item);

        buttonEquipment = (Button) layoutInventory.findViewById(R.id.button_equipment);
        buttonEquipment.setBackground(backgroundItem);
        buttonEquipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutEquipment.setVisibility(layoutEquipment.isShown() ? View.GONE : View.VISIBLE);
            }
        });

        imageEquipSlotWeapon = (ImageView) layoutInventory.findViewById(R.id.cell_equip_slot_1).findViewById(
                R.id.image_icon);
        imageEquipSlotArmor = (ImageView) layoutInventory.findViewById(R.id.cell_equip_slot_2).findViewById(
                R.id.image_icon);
        imageEquipSlotAccessory = (ImageView) layoutInventory.findViewById(R.id.cell_equip_slot_3).findViewById(
                R.id.image_icon);

        imageEquipSlotWeapon.setBackground(backgroundItem);
        imageEquipSlotArmor.setBackground(backgroundItem);
        imageEquipSlotAccessory.setBackground(backgroundItem);

        imageEquipSlotWeapon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                imageEquipSlotWeapon.getLocationOnScreen(location);
                if (controllerInventory.hasWeapon()) {
                    showEquipmentPopup(controllerInventory.getWeapon(), location[0], location[1]);
                }
            }
        });

        imageEquipSlotArmor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                imageEquipSlotArmor.getLocationOnScreen(location);
                if (controllerInventory.hasArmor()) {
                    showEquipmentPopup(controllerInventory.getArmor(), location[0], location[1]);
                }
            }
        });

        imageEquipSlotAccessory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                imageEquipSlotAccessory.getLocationOnScreen(location);
                if (controllerInventory.hasAccessory()) {
                    showEquipmentPopup(controllerInventory.getAccessory(), location[0], location[1]);
                }
            }
        });

        drawableWeapon = RenderUtils.getPixelatedDrawable(getResources(), R.drawable.placeholder_weapon);
        drawableArmor = RenderUtils.getPixelatedDrawable(getResources(), R.drawable.placeholder_armor);
        drawableAccessory = RenderUtils.getPixelatedDrawable(getResources(), R.drawable.placeholder_accessory);

        imageEquipSlotWeapon.setImageDrawable(drawableWeapon);
        imageEquipSlotArmor.setImageDrawable(drawableArmor);
        imageEquipSlotAccessory.setImageDrawable(drawableAccessory);
    }

    private void showEquipmentPopup(final Equipment equipment, int x, int y) {

        View view = LayoutInflater.from(MainActivity.this)
                .inflate(
                        R.layout.popup_item, frameView, false);

        final PopupWindow popup = new PopupWindow(MainActivity.this);

        Button buttonDrop = (Button) view.findViewById(R.id.button_drop);
        buttonDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controllerInventory.dropEquipment(equipment);
                popup.dismiss();
            }
        });

        Button buttonUnequip = (Button) view.findViewById(R.id.button_unequip);
        buttonUnequip.setVisibility(View.VISIBLE);
        buttonUnequip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controllerInventory.unequip(equipment);
                popup.dismiss();
            }
        });

        TextView textName = (TextView) view.findViewById(R.id.text_name);
        textName.setText(equipment.getName());

        TextView textDescription = (TextView) view.findViewById(
                R.id.text_description);
        textDescription.setText(equipment.getDescription());
        textDescription.setMovementMethod(new ScrollingMovementMethod());

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        ImageView imageIcon = (ImageView) view.findViewById(R.id.image_icon);
        imageIcon.setImageDrawable(RenderUtils.getPixelatedDrawable(getResources(),
                equipment.getResourceId()));

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        popup.setContentView(view);
        popup.setWidth(view.getMeasuredWidth());
        popup.setHeight(view.getMeasuredHeight());
        popup.setFocusable(true);
        popup.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
        applyFullscreen();

        Log.d(TAG, "Popup shown at (" + x + ", " + y + ")");
    }

    private void showPopup(final Item item, int x, int y) {

        View view = LayoutInflater.from(MainActivity.this)
                .inflate(
                        R.layout.popup_item, frameView, false);

        final PopupWindow popup = new PopupWindow(MainActivity.this);

        Button buttonDrop = (Button) view.findViewById(R.id.button_drop);
        buttonDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controllerInventory.dropItem(item);
                popup.dismiss();
            }
        });

        Button buttonUse = (Button) view.findViewById(R.id.button_use);
        buttonUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Consumable) item).consume(renderer.getPlayer());
                controllerInventory.removeItem(item);
                popup.dismiss();
            }
        });

        Button buttonEquip = (Button) view.findViewById(R.id.button_equip);
        buttonEquip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controllerInventory.equip((Equipment) item);
                popup.dismiss();
            }
        });

        if (item instanceof Consumable) {
            buttonUse.setVisibility(View.VISIBLE);
        }
        if (item instanceof Equipment) {
            buttonEquip.setVisibility(View.VISIBLE);
        }

        TextView textName = (TextView) view.findViewById(R.id.text_name);
        textName.setText(item.getName());

        TextView textDescription = (TextView) view.findViewById(
                R.id.text_description);
        textDescription.setText(item.getDescription());
        textDescription.setMovementMethod(new ScrollingMovementMethod());

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        ImageView imageIcon = (ImageView) view.findViewById(R.id.image_icon);
        imageIcon.setImageDrawable(RenderUtils.getPixelatedDrawable(getResources(),
                item.getResourceId()));

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        popup.setContentView(view);
        popup.setWidth(view.getMeasuredWidth());
        popup.setHeight(view.getMeasuredHeight());
        popup.setFocusable(true);
        popup.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
        applyFullscreen();

        Log.d(TAG, "Popup shown at (" + x + ", " + y + ")");
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
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        glSurfaceView.onPause();
        controllerInventory.removeListener(inventoryListener);
        super.onPause();
    }

    @Override
    protected void onStop() {
        renderer.release();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (frameView.isShown()) {
            frameView.setVisibility(View.GONE);
        }
        else {
            super.onBackPressed();
        }
    }

}