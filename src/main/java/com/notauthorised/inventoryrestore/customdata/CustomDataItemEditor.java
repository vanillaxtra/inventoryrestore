package com.notauthorised.inventoryrestore.customdata;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.tcoded.lightlibs.bukkitversion.MCVersion;
import com.notauthorised.inventoryrestore.reflections.LegacyNBTWrapper;
import org.bukkit.inventory.ItemStack;

public interface CustomDataItemEditor {

    static CustomDataItemEditor editItem(ItemStack item) {
        if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(MCVersion.v1_20_4.toBukkitVersion())) {
            return new ModernPdcItemEditor(item);
        } else {
            return new LegacyNBTWrapper(item);
        }
    }

    boolean hasUUID();

    ItemStack setString(String key, String data);

    ItemStack setInt(String key, Integer data);

    ItemStack setLong(String key, Long data);

    ItemStack setDouble(String key, Double data);

    ItemStack setFloat(String key, Float data);

    String getString(String key);

    int getInt(String key);

    Long getLong(String key);

    double getDouble(String key);

    Float getFloat(String key);

    ItemStack setItemData();

}