package com.winsonchiu.rpg;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by TheKeeperOfPie on 5/14/2015.
 */
public class ControllerInventory {

    private Set<InventoryListener> listeners;
    private List<Item> itemList;

    public ControllerInventory() {
        listeners = new HashSet<>();
        itemList = new ArrayList<>();
    }

    public void addListener(InventoryListener listener) {
        listeners.add(listener);
        listener.notifyDataSetChanged();
    }

    public void removeListener(InventoryListener listener) {
        listeners.remove(listener);
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public void addItem(Item item) {
        int index = itemList.indexOf(item);

        if (index > -1) {
            itemList.get(index).incrementQuantity(1);
            for (InventoryListener listener : listeners) {
                listener.notifyItemChanged(index);
            }
        }
        else {
            itemList.add(item);
            for (InventoryListener listener : listeners) {
                listener.notifyItemInserted(itemList.size() - 1);
            }
        }
    }

    public Item getItem(int position) {
        return itemList.get(position);
    }

    public Item removeItem(int position) {

        Item item = itemList.get(position);

        if (item.getQuantity() > 1) {
            item = item.decrementQuantity();
            for (InventoryListener listener : listeners) {
                listener.notifyItemChanged(position);
            }
        }
        else {
            item = itemList.remove(position);
            for (InventoryListener listener : listeners) {
                listener.notifyItemRemoved(position);
            }
        }
        return item;
    }

    public void dropItem(int position) {
        for (InventoryListener listener : listeners) {
            listener.dropItem(removeItem(position));
        }
    }

    public interface InventoryListener {

        RecyclerView.Adapter getAdapter();
        void dropItem(Item item);
        void equipItem();
        void notifyItemInserted(int position);
        void notifyItemRemoved(int position);
        void notifyDataSetChanged();
        void notifyItemChanged(int position);
    }

}
