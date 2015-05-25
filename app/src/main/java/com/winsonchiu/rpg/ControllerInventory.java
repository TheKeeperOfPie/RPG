package com.winsonchiu.rpg;

import android.support.v7.widget.RecyclerView;

import com.winsonchiu.rpg.items.Accessory;
import com.winsonchiu.rpg.items.Armor;
import com.winsonchiu.rpg.items.Equipment;
import com.winsonchiu.rpg.items.Item;
import com.winsonchiu.rpg.items.Weapon;

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
    private volatile Weapon weapon;
    private volatile Armor armor;
    private volatile Accessory accessory;

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
            itemList.get(index).incrementQuantity(item.getQuantity());
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

    public Item removeItem(Item item) {
        int removeIndex = itemList.indexOf(item);

        if (item.getQuantity() > 1) {
            item = item.decrementQuantity();
            for (InventoryListener listener : listeners) {
                listener.notifyItemChanged(removeIndex);
            }
        }
        else {
            item = itemList.remove(removeIndex);
            for (InventoryListener listener : listeners) {
                listener.notifyItemRemoved(removeIndex);
            }
        }
        return item;
    }

    public void dropItem(int position) {
        for (InventoryListener listener : listeners) {
            listener.dropItem(removeItem(position));
        }
    }

    public void dropItem(Item item) {
        for (InventoryListener listener : listeners) {
            listener.dropItem(removeItem(item));
        }
    }

    public void equip(Item item) {
        if (itemList.contains(item)) {
            item = removeItem(item);
        }

        if (item instanceof Weapon) {
            if (weapon != null) {
                itemList.add(weapon);
            }
            weapon = (Weapon) item;
        }
        else if (item instanceof Armor) {
            if (armor != null) {
                itemList.add(armor);
            }
            armor = (Armor) item;
        }
        else if (item instanceof Accessory) {
            if (accessory != null) {
                itemList.add(accessory);
            }
            accessory = (Accessory) item;
        }

        for (InventoryListener listener : listeners) {
            listener.notifyDataSetChanged();
            listener.onEquipmentChanged();
        }

    }

    public boolean hasWeapon() {
        return weapon != null;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public boolean hasArmor() {
        return armor != null;
    }

    public Armor getArmor() {
        return armor;
    }

    public boolean hasAccessory() {
        return accessory != null;
    }

    public Accessory getAccessory() {
        return accessory;
    }

    public void dropEquipment(Equipment equipment) {
        if (equipment instanceof Weapon && hasWeapon()) {
            weapon = null;
        }
        else if (equipment instanceof Armor && hasArmor()) {
            armor = null;
        }
        else if (equipment instanceof Accessory && hasAccessory()) {
            accessory = null;
        }
        dropItem(equipment);
    }

    public void unequip(Equipment equipment) {
        if (equipment instanceof Weapon && hasWeapon()) {
            weapon = null;
        }
        else if (equipment instanceof Armor && hasArmor()) {
            armor = null;
        }
        else if (equipment instanceof Accessory && hasAccessory()) {
            accessory = null;
        }
        addItem(equipment);
        for (InventoryListener inventoryListener : listeners) {
            inventoryListener.onEquipmentChanged();
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
        void onEquipmentChanged();
    }

}