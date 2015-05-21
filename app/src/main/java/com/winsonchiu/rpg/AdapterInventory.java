package com.winsonchiu.rpg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by TheKeeperOfPie on 5/14/2015.
 */
public class AdapterInventory extends RecyclerView.Adapter<AdapterInventory.ViewHolder> {

    private Activity activity;
    private ControllerInventory controllerInventory;

    public AdapterInventory(Activity activity, ControllerInventory controllerInventory) {
        this.activity = activity;
        this.controllerInventory = controllerInventory;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_inventory, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        Item item = controllerInventory.getItem(position);

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//        options.inDither = false;
//
//        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), item.getItemId().getDrawable(), options);
//
////        Matrix matrix = new Matrix();
//
//        bitmap = Bitmap.createScaledBitmap(bitmap, viewHolder.imageIcon.getMaxWidth(), viewHolder.imageIcon.getMaxWidth(), false);
//
//        viewHolder.imageIcon.setImageBitmap(bitmap);
        viewHolder.imageIcon.setImageResource(item.getItemId().getDrawable());
        viewHolder.textName.setText(item.getItemId().getName());

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

        protected ImageView imageIcon;
        protected TextView textName;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageIcon = (ImageView) itemView.findViewById(R.id.image_icon);
            this.textName = (TextView) itemView.findViewById(R.id.text_name);
        }
    }

}