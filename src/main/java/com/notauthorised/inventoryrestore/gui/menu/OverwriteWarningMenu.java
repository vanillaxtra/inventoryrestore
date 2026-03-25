package com.notauthorised.inventoryrestore.gui.menu;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.data.LastLiveInventoryStore;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.RestoreSession;
import com.notauthorised.inventoryrestore.gui.Buttons;
import com.notauthorised.inventoryrestore.gui.GuiDecorItems;
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

        ItemStack confirmBtn = new ItemStack(Material.CLOCK);
        ItemMeta confirmMeta = confirmBtn.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm restore");
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
                warnMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Clock restores items & armor from backup only"));
            } else {
                warnMeta.setDisplayName(ChatColor.YELLOW + "Their inventory (last quit)");
                warnMeta.setLore(Collections.singletonList(ChatColor.GRAY + "If empty, they have not quit since plugin update"));
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
            placeArmorOffhandAndGap(p.getInventory().getArmorContents(), p.getInventory().getItemInOffHand());
        } else {
            LastLiveInventoryStore.Loaded live = LastLiveInventoryStore.load(target.getUniqueId());
            if (live != null && live.contents36 != null) {
                for (int i = 0; i < 36; i++) {
                    ItemStack it = i < live.contents36.length ? live.contents36[i] : null;
                    inventory.setItem(i, it != null ? it.clone() : null);
                }
                placeArmorOffhandAndGap(live.armor, live.offhand);
            } else {
                for (int i = 0; i < 36; i++) {
                    inventory.setItem(i, null);
                }
                inventory.setItem(13, new ItemStack(Material.BARRIER));
                ItemMeta meta = inventory.getItem(13).getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.RED + "No saved layout");
                    meta.setLore(Collections.singletonList(ChatColor.GRAY + "They need to quit once while the plugin is on"));
                    inventory.getItem(13).setItemMeta(meta);
                }
                placeArmorOffhandAndGap(null, null);
            }
        }
    }

    /** Same layout as main backup: 36–39 gap, 40 off-hand, 41–44 armor (matches restore screen). */
    private void placeArmorOffhandAndGap(ItemStack[] armorArr, ItemStack offHand) {
        ItemStack gap = GuiDecorItems.grayGap();
        for (int s = 36; s <= 39; s++) {
            inventory.setItem(s, gap.clone());
        }

        if (offHand != null && !offHand.getType().isAir()) {
            inventory.setItem(40, offHand.clone());
        } else {
            inventory.setItem(40, GuiDecorItems.orangeOffhandPlaceholder());
        }

        String[] armorNames = {"Boots", "Leggings", "Chestplate", "Helmet"};
        for (int i = 0; i < 4; i++) {
            int slot = 44 - i;
            ItemStack piece = armorArr != null && i < armorArr.length ? armorArr[i] : null;
            if (piece != null && !piece.getType().isAir()) {
                inventory.setItem(slot, piece.clone());
            } else {
                inventory.setItem(slot, GuiDecorItems.blueArmorSlot(armorNames[i]));
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
