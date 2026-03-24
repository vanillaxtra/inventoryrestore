package com.notauthorised.inventoryrestore.gui.menu;

import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import com.notauthorised.inventoryrestore.util.ExportStorageHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.UUID;

/**
 * Pick how to export a backup: shulker, barrel, chest, bundle, or drop.
 */
public class ExportStorageMenu {

    public static final String NBT_EXPORT_MODE = "exportMode";

    private final Player staff;
    private final UUID targetUuid;
    private final LogType logType;
    private final long timestamp;
    private Inventory inventory;

    public ExportStorageMenu(Player staff, UUID targetUuid, LogType logType, long timestamp) {
        this.staff = staff;
        this.targetUuid = targetUuid;
        this.logType = logType;
        this.timestamp = timestamp;
        build();
    }

    private void build() {
        inventory = Bukkit.createInventory(staff, InventoryName.EXPORT_STORAGE.getSize(),
                InventoryName.EXPORT_STORAGE.getName());

        inventory.setItem(10, option(Material.SHULKER_BOX, ChatColor.GOLD + "Shulker boxes",
                ChatColor.GRAY + "Two shulkers (27 slots each)", ExportStorageHelper.Mode.SHULKER));
        inventory.setItem(12, option(Material.BARREL, ChatColor.GOLD + "Barrels",
                ChatColor.GRAY + "Two barrels, 27 slots each", ExportStorageHelper.Mode.BARREL));
        inventory.setItem(14, option(Material.CHEST, ChatColor.GOLD + "Chests",
                ChatColor.GRAY + "Two chests, 27 slots each", ExportStorageHelper.Mode.CHEST));
        inventory.setItem(16, option(Material.BUNDLE, ChatColor.GOLD + "Bundles",
                ChatColor.GRAY + "Portable bundles (1.20.5+)", ExportStorageHelper.Mode.BUNDLE));

        inventory.setItem(20, option(Material.HOPPER, ChatColor.YELLOW + "Drop at my feet",
                ChatColor.GRAY + "Spawns item entities on the ground", ExportStorageHelper.Mode.DROP));

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(ChatColor.WHITE + "Back");
            bm.setLore(Collections.singletonList(ChatColor.GRAY + "Return to backup view"));
            back.setItemMeta(bm);
        }
        CustomDataItemEditor ed = CustomDataItemEditor.editItem(back);
        ed.setString(NBT_EXPORT_MODE, "BACK");
        ed.setString("uuid", targetUuid.toString());
        ed.setString("logType", logType.name());
        ed.setLong("timestamp", timestamp);
        inventory.setItem(22, ed.setItemData());
    }

    private ItemStack option(Material mat, String name, String loreLine, ExportStorageHelper.Mode mode) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            m.setLore(Collections.singletonList(loreLine));
            it.setItemMeta(m);
        }
        CustomDataItemEditor ed = CustomDataItemEditor.editItem(it);
        ed.setString(NBT_EXPORT_MODE, mode.name());
        ed.setString("uuid", targetUuid.toString());
        ed.setString("logType", logType.name());
        ed.setLong("timestamp", timestamp);
        return ed.setItemData();
    }

    public Inventory getInventory() {
        return inventory;
    }
}
