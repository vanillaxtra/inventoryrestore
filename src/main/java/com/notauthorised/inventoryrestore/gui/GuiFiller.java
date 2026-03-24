package com.notauthorised.inventoryrestore.gui;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Decorative glass borders for chest-style menus.
 */
public final class GuiFiller {

    private GuiFiller() {}

    public static ItemStack pane() {
        ItemStack g = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = g.getItemMeta();
        if (m != null) {
            m.setDisplayName(" ");
            g.setItemMeta(m);
        }
        return g;
    }

    /** Fill top and bottom rows; optional side columns for 3-row chests. */
    public static void border27(Inventory inv) {
        ItemStack p = pane();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, p.clone());
            inv.setItem(18 + i, p.clone());
        }
        inv.setItem(9, p.clone());
        inv.setItem(17, p.clone());
    }

    /** Fill outer ring of a 6-row chest (54 slots): rows 0 and 5 full; columns 0 and 8 on middle rows. */
    public static void border54(Inventory inv) {
        ItemStack p = pane();
        for (int c = 0; c < 9; c++) {
            inv.setItem(c, p.clone());
            inv.setItem(45 + c, p.clone());
        }
        for (int r = 1; r <= 4; r++) {
            inv.setItem(r * 9, p.clone());
            inv.setItem(r * 9 + 8, p.clone());
        }
    }

}
