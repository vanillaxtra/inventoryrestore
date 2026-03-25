package com.notauthorised.inventoryrestore.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Stained-glass fillers for backup GUIs (not real items — filtered out of restore snapshots).
 */
public final class GuiDecorItems {

    private GuiDecorItems() {}

    public static ItemStack grayGap() {
        return namedPane(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
    }

    public static ItemStack blueArmorSlot(String slotName) {
        return namedPane(Material.BLUE_STAINED_GLASS_PANE, ChatColor.AQUA + slotName);
    }

    public static ItemStack orangeOffhandPlaceholder() {
        return namedPane(Material.ORANGE_STAINED_GLASS_PANE, ChatColor.GOLD + "Off-hand");
    }

    public static ItemStack namedPane(Material material, String displayName) {
        ItemStack s = new ItemStack(material);
        ItemMeta m = s.getItemMeta();
        if (m != null) {
            m.setDisplayName(displayName);
            s.setItemMeta(m);
        }
        return s;
    }

    public static boolean isArmorPlaceholder(ItemStack stack) {
        return stack != null
                && stack.getType() == Material.BLUE_STAINED_GLASS_PANE
                && stack.hasItemMeta()
                && stack.getItemMeta().hasDisplayName();
    }

    public static boolean isOffhandPlaceholder(ItemStack stack) {
        return stack != null
                && stack.getType() == Material.ORANGE_STAINED_GLASS_PANE
                && stack.hasItemMeta()
                && stack.getItemMeta().hasDisplayName();
    }

    public static boolean isGrayGap(ItemStack stack) {
        return stack != null
                && stack.getType() == Material.GRAY_STAINED_GLASS_PANE;
    }

    /** Grey filler from {@link #grayGap()} (named pane — not plain survival glass). */
    public static boolean isGrayGapDecor(ItemStack stack) {
        if (stack == null || stack.getType() != Material.GRAY_STAINED_GLASS_PANE || !stack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta.hasDisplayName() && (ChatColor.DARK_GRAY + " ").equals(meta.getDisplayName());
    }

    /** Preview fillers staff should not pull into their inventory, but may replace by placing items. */
    public static boolean isNonTakeableDecor(ItemStack stack) {
        return isGrayGapDecor(stack) || isArmorPlaceholder(stack) || isOffhandPlaceholder(stack);
    }
}
