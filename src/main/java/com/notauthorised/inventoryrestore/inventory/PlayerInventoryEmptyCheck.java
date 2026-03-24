package com.notauthorised.inventoryrestore.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerInventoryEmptyCheck {

    private PlayerInventoryEmptyCheck() {}

    public static boolean hasAnything(Player player) {
        if (player == null) return false;
        for (ItemStack s : player.getInventory().getStorageContents()) {
            if (s != null && !s.getType().isAir()) return true;
        }
        for (ItemStack s : player.getInventory().getArmorContents()) {
            if (s != null && !s.getType().isAir()) return true;
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        return off != null && !off.getType().isAir();
    }
}
