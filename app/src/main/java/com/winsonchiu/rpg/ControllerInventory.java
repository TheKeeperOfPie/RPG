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
        listener.getAdapter().notifyDataSetChanged();
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
        itemList.add(item);
    }

    public Item getItem(int position) {
        return itemList.get(position);
    }

    public Item removeItem(int position) {
        return itemList.get(position);
    }


    public interface InventoryListener {

        RecyclerView.Adapter getAdapter();

    }

}
