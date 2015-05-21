package com.winsonchiu.rpg;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by TheKeeperOfPie on 5/14/2015.
 */
public class AdapterInventory extends RecyclerView.Adapter<AdapterInventory.ViewHolder> {

    private static final String TAG = AdapterInventory.class.getCanonicalName();
    private final BitmapDrawable backgroundItem;
    private final BitmapFactory.Options options;
    private final EventCallback eventCallback;
    private Activity activity;
    private Resources resources;
    private ControllerInventory controllerInventory;

    public AdapterInventory(Activity activity, ControllerInventory controllerInventory, EventCallback eventCallback) {
        this.activity = activity;
        this.controllerInventory = controllerInventory;
        this.eventCallback = eventCallback;
        this.resources = activity.getResources();

        // Force inScaled off to prevent a blurry texture
        options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(),
                R.drawable.background_item, options);

        backgroundItem = new BitmapDrawable(activity.getResources(), bitmap);

        backgroundItem.setAntiAlias(false);
        backgroundItem.setDither(false);
        backgroundItem.setFilterBitmap(false);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_inventory, viewGroup, false), backgroundItem, eventCallback);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        Item item = controllerInventory.getItem(position);

        Drawable drawable = new BitmapDrawable(resources, BitmapFactory.decodeResource(resources,
                item.getItemId().getDrawable(), options));

        viewHolder.onBind(item, drawable);

    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);

        Drawable drawable = holder.imageIcon.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

        }

    }

    @Override
    public int getItemCount() {
        return controllerInventory.getItemList().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final EventCallback callback;
        private ImageView imageIcon;
        private TextView textQuantity;

        public ViewHolder(View view,
                Drawable background,
                EventCallback eventCallback) {
            super(view);

            this.callback = eventCallback;
            this.imageIcon = (ImageView) itemView.findViewById(R.id.image_icon);
            this.imageIcon.setBackground(background);
            this.textQuantity = (TextView) itemView.findViewById(R.id.text_quantity);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    itemView.getLocationOnScreen(location);
                    callback.showPopupWindow(getAdapterPosition(), location[0], location[1]);
                }
            });

        }

        public void onBind(Item item, Drawable drawable) {

            drawable.setDither(false);
            drawable.setFilterBitmap(false);

            imageIcon.setImageDrawable(drawable);
            textQuantity.setText("" + item.getQuantity());
        }
    }

    public interface EventCallback {

        void showPopupWindow(int position, int x, int y);

    }

}