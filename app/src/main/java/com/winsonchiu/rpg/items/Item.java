package com.winsonchiu.rpg.items;

import android.graphics.PointF;
import android.util.JsonWriter;
import android.util.Log;

import com.winsonchiu.rpg.Entity;
import com.winsonchiu.rpg.Renderer;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/14/2015.
 */
public class Item extends Entity {

    private static final String TAG = Item.class.getCanonicalName();

    private static final float WIDTH_RATIO = 0.75f;
    private static final float HEIGHT_RATIO = 0.75f;
    private static final float TEXTURE_ROW_COUNT = 14f;
    private static final float TEXTURE_COL_COUNT = 29f;

    public static final String CLASS = "class";
    public static final String LOCATION_X = "locationX";
    public static final String LOCATION_Y = "locationY";
    public static final String HEALTH_BOOST = "healthBoost";
    public static final String ARMOR_BOOST = "armorBoost";
    public static final String DAMAGE_BOOST = "damageBoost";
    public static final String SPEED_BOOST = "speedBoost";
    public static final String LEVEL = "level";
    public static final String DURATION = "duration";
    public static final String RESOURCE_ID = "resourceId";
    public static final String TEXTURE_ID = "textureId";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String QUANTITY = "quantity";
    public static final String MATERIAL = "material";

    private int healthBoost;
    private int armorBoost;
    private int damageBoost;
    private int speedBoost;
    private int level;
    private int duration;
    private int resourceId;
    private int textureId;
    private String name;
    private String description;
    private int quantity;

    public Item(PointF location, int level) {
        super(WIDTH_RATIO, HEIGHT_RATIO, new PointF(location.x - WIDTH_RATIO / 2, location.y - HEIGHT_RATIO / 2), TEXTURE_ROW_COUNT, TEXTURE_COL_COUNT,
                0);
        this.level = level;
        this.quantity = 1;
    }

    public Item(Item item) {
        super(WIDTH_RATIO, HEIGHT_RATIO, new PointF(item.getLocation().x, item.getLocation().y), TEXTURE_ROW_COUNT, TEXTURE_COL_COUNT,
                0);
        this.healthBoost = item.getHealthBoost();
        this.armorBoost = item.getArmorBoost();
        this.damageBoost = item.getDamageBoost();
        this.speedBoost = item.getSpeedBoost();
        this.level = item.getLevel();
        this.duration = item.getDuration();
        this.resourceId = item.getResourceId();
        this.textureId = item.getTextureId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.quantity = 1;
        setLastAnimationFrame(textureId);
    }

    public Item(JSONObject jsonObject) {
        super(WIDTH_RATIO, HEIGHT_RATIO, new PointF(0, 0), TEXTURE_ROW_COUNT, TEXTURE_COL_COUNT,
                0);
        this.healthBoost = jsonObject.optInt(HEALTH_BOOST);
        this.armorBoost = jsonObject.optInt(ARMOR_BOOST);
        this.damageBoost = jsonObject.optInt(DAMAGE_BOOST);
        this.speedBoost = jsonObject.optInt(SPEED_BOOST);
        this.level = jsonObject.optInt(LEVEL);
        this.duration = jsonObject.optInt(DURATION);
        this.resourceId = jsonObject.optInt(RESOURCE_ID);
        this.textureId = jsonObject.optInt(TEXTURE_ID);
        this.name = jsonObject.optString(NAME, "");
        this.description = jsonObject.optString(DESCRIPTION, "");
        this.quantity = jsonObject.optInt(QUANTITY, 1);
        setLastAnimationFrame(textureId);
    }

    //region Getters, setters, changers

    public void incrementQuantity(int amount) {
        quantity += amount;
    }

    public Item decrementQuantity() {

        if (quantity == 0) {
            throw new IllegalStateException("Not enough quantity for item: " + toString());
        }

        quantity--;

        return new Item(this);

    }

    public List<Item> decrementQuantity(int amount) {

        if (amount > quantity) {
            throw new IllegalStateException("Not enough quantity for item: " + toString());
        }

        quantity -= amount;

        List<Item> removed = new ArrayList<>(amount);
        for (int num = 0; num < amount; num++) {
            removed.add(new Item(this));
        }

        return removed;
    }

    public int getHealthBoost() {
        return healthBoost;
    }

    public void setHealthBoost(int healthBoost) {
        this.healthBoost = healthBoost;
    }

    public int getArmorBoost() {
        return armorBoost;
    }

    public void setArmorBoost(int armorBoost) {
        this.armorBoost = armorBoost;
    }

    public int getDamageBoost() {
        return damageBoost;
    }

    public void setDamageBoost(int damageBoost) {
        this.damageBoost = damageBoost;
    }

    public int getSpeedBoost() {
        return speedBoost;
    }

    public void setSpeedBoost(int speedBoost) {
        this.speedBoost = speedBoost;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getTextureId() {
        return textureId;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    //endregion

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Item item = (Item) o;

        if (getLevel() != item.getLevel()) {
            return false;
        }
        if (getResourceId() != item.getResourceId()) {
            return false;
        }
        if (getTextureId() != item.getTextureId()) {
            return false;
        }
        if (!getName().equals(item.getName())) {
            return false;
        }
        return getDescription().equals(item.getDescription());

    }

    @Override
    public int hashCode() {
        int result = getHealthBoost();
        result = 31 * result + getArmorBoost();
        result = 31 * result + getDamageBoost();
        result = 31 * result + getSpeedBoost();
        result = 31 * result + getLevel();
        result = 31 * result + getDuration();
        result = 31 * result + getResourceId();
        result = 31 * result + getTextureId();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getQuantity();
        return result;
    }

    @Override
    public String toString() {
        return "Item{" +
                "healthBoost=" + healthBoost +
                ", armorBoost=" + armorBoost +
                ", damageBoost=" + damageBoost +
                ", speedBoost=" + speedBoost +
                ", level=" + level +
                ", duration=" + duration +
                ", resourceId=" + resourceId +
                ", textureId=" + textureId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                '}';
    }

    public JSONObject toJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(CLASS, this.getClass().getCanonicalName());
        jsonObject.put(LOCATION_X, getLocation().x);
        jsonObject.put(LOCATION_Y, getLocation().y);
        jsonObject.put(HEALTH_BOOST, healthBoost);
        jsonObject.put(ARMOR_BOOST, armorBoost);
        jsonObject.put(DAMAGE_BOOST, damageBoost);
        jsonObject.put(SPEED_BOOST, speedBoost);
        jsonObject.put(LEVEL, level);
        jsonObject.put(DURATION, duration);
        jsonObject.put(RESOURCE_ID, resourceId);
        jsonObject.put(TEXTURE_ID, textureId);
        jsonObject.put(NAME, name);
        jsonObject.put(DESCRIPTION, description);
        jsonObject.put(QUANTITY, quantity);

        if (this instanceof Equipment) {
            jsonObject.put(MATERIAL, ((Equipment) this).getMaterial().toString());
        }

        return jsonObject;
    }

    public static Item fromJsonObject(JSONObject jsonObject) {
        try {
            Class itemClass = Item.class.getClassLoader().loadClass(jsonObject.optString(CLASS));
            Constructor constructor = itemClass.getConstructor(JSONObject.class);
            return (Item) constructor.newInstance(jsonObject);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return new Item(jsonObject);
    }
}