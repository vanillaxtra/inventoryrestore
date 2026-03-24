package com.notauthorised.inventoryrestore.gui.menu;

import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.notauthorised.inventoryrestore.data.PlayerData;
import com.notauthorised.inventoryrestore.data.RestoreStatsManager;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import com.notauthorised.inventoryrestore.util.RelativeTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * One row per backup snapshot that has been refunded; shows how many times each was applied.
 */
public class RefundHistoryMenu {

    public static final String NBT_ACTION = "refundHistAction";

    private final Player staff;
    private final UUID targetUuid;
    private Inventory inventory;

    public RefundHistoryMenu(Player staff, UUID targetUuid) {
        this.staff = staff;
        this.targetUuid = targetUuid;
        build();
    }

    private void build() {
        inventory = Bukkit.createInventory(staff, InventoryName.REFUND_HISTORY.getSize(),
                InventoryName.REFUND_HISTORY.getName());

        List<RestoreStatsManager.BackupRefundSummary> rows =
                RestoreStatsManager.listBackupRefundSummaries(targetUuid, 27);

        int slot = 0;
        for (RestoreStatsManager.BackupRefundSummary sum : rows) {
            if (slot >= 27) break;
            inventory.setItem(slot++, summaryItem(sum));
        }

        if (rows.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta em = empty.getItemMeta();
            if (em != null) {
                em.setDisplayName(ChatColor.RED + "No refund data yet");
                em.setLore(Collections.singletonList(
                        ChatColor.GRAY + "When you confirm a refund restore, that backup appears here with usage counts."));
                empty.setItemMeta(em);
            }
            inventory.setItem(13, empty);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(ChatColor.WHITE + "Back");
            bm.setLore(Collections.singletonList(ChatColor.GRAY + "Return to refund menu"));
            back.setItemMeta(bm);
        }
        CustomDataItemEditor ed = CustomDataItemEditor.editItem(back);
        ed.setString(NBT_ACTION, "BACK");
        ed.setString("uuid", targetUuid.toString());
        inventory.setItem(31, ed.setItemData());
    }

    private ItemStack summaryItem(RestoreStatsManager.BackupRefundSummary sum) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Backup from " + ChatColor.WHITE + RelativeTime.ago(sum.backupTimestamp)
                + ChatColor.DARK_GRAY + " (" + PlayerData.getTime(sum.backupTimestamp) + ")");
        lore.add("");
        lore.add(ChatColor.GOLD + "Refunded from this snapshot: " + ChatColor.WHITE + sum.refundCount + "×");
        if (sum.lastRefundTime > 0) {
            lore.add(ChatColor.DARK_AQUA + "Most recent refund " + ChatColor.GRAY + RelativeTime.ago(sum.lastRefundTime)
                    + ChatColor.DARK_GRAY + " (" + PlayerData.getTime(sum.lastRefundTime) + ")");
            lore.add(ChatColor.DARK_AQUA + "Staff: " + ChatColor.WHITE + sum.lastStaff);
        }
        lore.add(ChatColor.DARK_GRAY + "Log type: " + ChatColor.GRAY + sum.logType.name());
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Backup · " + shortWhen(sum.backupTimestamp));
            meta.setLore(lore);
            paper.setItemMeta(meta);
        }
        return paper;
    }

    private static String shortWhen(long ts) {
        String t = PlayerData.getTime(ts);
        if (t.length() > 26) return t.substring(0, 23) + "…";
        return t;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
