package com.notauthorised.inventoryrestore.gui.menu;

import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.notauthorised.inventoryrestore.data.BackupActivityTracker;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.PlayerData;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import com.notauthorised.inventoryrestore.util.RelativeTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Staff heads for each view/restore event on a backup snapshot.
 */
public class BackupActivityMenu {

    public static final String NBT_ACTION = "backupAct";

    private final Player staff;
    private final UUID targetUuid;
    private final LogType logType;
    private final long backupTimestamp;
    private Inventory inventory;

    public BackupActivityMenu(Player staff, UUID targetUuid, LogType logType, long backupTimestamp) {
        this.staff = staff;
        this.targetUuid = targetUuid;
        this.logType = logType;
        this.backupTimestamp = backupTimestamp;
        build();
    }

    private void build() {
        inventory = Bukkit.createInventory(staff, InventoryName.BACKUP_ACTIVITY.getSize(),
                InventoryName.BACKUP_ACTIVITY.getName());

        List<BackupActivityTracker.ActivityEntry> events =
                BackupActivityTracker.listEvents(targetUuid, logType, backupTimestamp, 36);

        int slot = 0;
        for (BackupActivityTracker.ActivityEntry ev : events) {
            if (slot >= 36) break;
            inventory.setItem(slot++, headFor(ev));
        }

        if (events.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta em = empty.getItemMeta();
            if (em != null) {
                em.setDisplayName(ChatColor.RED + "No activity yet");
                em.setLore(Collections.singletonList(
                        ChatColor.GRAY + "Opens and full restores on this backup will show here."));
                empty.setItemMeta(em);
            }
            inventory.setItem(13, empty);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(ChatColor.WHITE + "Back");
            bm.setLore(Collections.singletonList(ChatColor.GRAY + "Return to backup"));
            back.setItemMeta(bm);
        }
        CustomDataItemEditor ed = CustomDataItemEditor.editItem(back);
        ed.setString(NBT_ACTION, "BACK");
        ed.setString("uuid", targetUuid.toString());
        ed.setString("logType", logType.name());
        ed.setLong("timestamp", backupTimestamp);
        inventory.setItem(36, ed.setItemData());
    }

    private ItemStack headFor(BackupActivityTracker.ActivityEntry ev) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(ev.staffName);
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(op);
            String action = ev.restore ? ChatColor.RED + "" + ChatColor.BOLD + "RESTORE"
                    : ChatColor.AQUA + "" + ChatColor.BOLD + "VIEW";
            meta.setDisplayName(ChatColor.WHITE + (ev.staffName != null ? ev.staffName : "Unknown"));
            List<String> lore = new ArrayList<>();
            lore.add(action);
            lore.add(ChatColor.GRAY + (ev.restore ? "Full inventory restore" : "Opened backup (view)"));
            lore.add(ChatColor.GRAY + PlayerData.getTime(ev.time));
            lore.add(ChatColor.DARK_GRAY + "(" + RelativeTime.ago(ev.time) + ")");
            meta.setLore(lore);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
