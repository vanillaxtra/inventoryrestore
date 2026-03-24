package com.notauthorised.inventoryrestore.gui.menu;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.RestoreSession;
import com.notauthorised.inventoryrestore.gui.Buttons;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.UUID;

/**
 * Warning GUI shown before restoring - displays target's current inventory (live if online)
 * with options to go back or confirm rollback.
 */
public class OverwriteWarningMenu {

    private final InventoryRestore main;
    private final Player staff;
    private final OfflinePlayer target;
    private final LogType logType;
    private final Long timestamp;
    private Inventory inventory;

    public OverwriteWarningMenu(Player staff, OfflinePlayer target, LogType logType, Long timestamp) {
        this.main = InventoryRestore.getInstance();
        this.staff = staff;
        this.target = target;
        this.logType = logType;
        this.timestamp = timestamp;
        createInventory();
    }

    private void createInventory() {
        inventory = Bukkit.createInventory(staff, InventoryName.OVERWRITE_WARNING.getSize(),
                InventoryName.OVERWRITE_WARNING.getName());

        Buttons buttons = new Buttons(target.getUniqueId());

        // Row 5 (slots 45-53): Back button (45), Warning (46-47), Confirm (48)
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back");
            backBtn.setItemMeta(backMeta);
        }
        CustomDataItemEditor backNbt = CustomDataItemEditor.editItem(backBtn);
        backNbt.setString("action", "back");
        backNbt.setString("uuid", target.getUniqueId().toString());
        backNbt.setString("logType", logType.name());
        backNbt.setLong("timestamp", timestamp);
        inventory.setItem(45, backNbt.setItemData());

        ItemStack confirmBtn = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirmBtn.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm Rollback");
            confirmMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Overwrites inventory, ender chest, XP, hunger"));
            confirmBtn.setItemMeta(confirmMeta);
        }
        CustomDataItemEditor confirmNbt = CustomDataItemEditor.editItem(confirmBtn);
        confirmNbt.setString("action", "confirm");
        confirmNbt.setString("uuid", target.getUniqueId().toString());
        confirmNbt.setString("logType", logType.name());
        confirmNbt.setLong("timestamp", timestamp);
        if (RestoreSession.isRefundContext(staff.getUniqueId())) {
            confirmNbt.setString("refund", "1");
        }
        inventory.setItem(48, confirmNbt.setItemData());

        // Warning text
        ItemStack warnItem = new ItemStack(Material.YELLOW_BANNER);
        ItemMeta warnMeta = warnItem.getItemMeta();
        if (warnMeta != null) {
            if (target.isOnline()) {
                warnMeta.setDisplayName(ChatColor.YELLOW + "Their inventory now");
                warnMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Confirm still applies the backup"));
            } else {
                warnMeta.setDisplayName(ChatColor.YELLOW + "Player offline");
                warnMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Restore when they join"));
            }
            warnItem.setItemMeta(warnMeta);
        }
        inventory.setItem(46, warnItem);

        // Fill top 36 slots with current inventory (or placeholder if offline)
        refreshInventoryDisplay();
    }

    public void refreshInventoryDisplay() {
        if (target.isOnline()) {
            Player p = (Player) target;
            ItemStack[] contents = p.getInventory().getContents();
            for (int i = 0; i < 36; i++) {
                ItemStack it = i < contents.length ? contents[i] : null;
                inventory.setItem(i, it != null ? it.clone() : null);
            }
        } else {
            for (int i = 0; i < 36; i++) {
                inventory.setItem(i, null);
            }
            inventory.setItem(13, new ItemStack(Material.BARRIER));
            ItemMeta meta = inventory.getItem(13).getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "Offline");
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Inventory not shown"));
                inventory.getItem(13).setItemMeta(meta);
            }
        }
    }

    /** No periodic refresh so staff can pull items from the preview without copies respawning. */
    public void startLiveRefresh() {
    }

    public void stopRefresh() {
    }

    public Inventory getInventory() {
        return inventory;
    }
}
