package com.winsonchiu.rpg;

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

    private ControllerInventory controllerInventory;

    public AdapterInventory(ControllerInventory controllerInventory) {
        this.controllerInventory = controllerInventory;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_inventory, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        Item item = controllerInventory.getItemList().get(position);

        viewHolder.imageIcon.setImageResource(item.getResourceId());
        viewHolder.textName.setText(item.getName());

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