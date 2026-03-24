package com.notauthorised.inventoryrestore.util;

import com.notauthorised.inventoryrestore.config.ConfigData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Strips configured materials from snapshots so they are not stored in backups.
 */
public final class BackupIgnoreFilter {

    private BackupIgnoreFilter() {}

    public static boolean isIgnored(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        return ConfigData.isBackupIgnoredMaterial(stack.getType());
    }

    public static ItemStack[] filterArray(ItemStack[] arr) {
        if (arr == null) return null;
        ItemStack[] out = new ItemStack[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ItemStack s = arr[i];
            out[i] = (s != null && !isIgnored(s)) ? s.clone() : null;
        }
        return out;
    }

    public static ItemStack filterSingle(ItemStack stack) {
        if (stack == null || isIgnored(stack)) return null;
        return stack.clone();
    }
}
