package com.notauthorised.inventoryrestore.gui.menu;

import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.PlayerData;
import com.notauthorised.inventoryrestore.data.RestoreStatsManager;
import com.notauthorised.inventoryrestore.gui.Buttons;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerMenu {

    private final Player staff;
    private final OfflinePlayer offlinePlayer;
    private final boolean refundMode;

    private final Buttons buttons;
    private Inventory inventory;

    public PlayerMenu(Player staff, OfflinePlayer player) {
        this(staff, player, false);
    }

    public PlayerMenu(Player staff, OfflinePlayer player, boolean refundMode) {
        this.staff = staff;
        this.offlinePlayer = player;
        this.refundMode = refundMode;
        this.buttons = new Buttons(player.getUniqueId());
        createInventory();
    }

    private InventoryName menuType() {
        return refundMode ? InventoryName.REFUND_MENU : InventoryName.PLAYER_MENU;
    }

    public void createInventory() {
        InventoryName type = menuType();
        inventory = Bukkit.createInventory(staff, type.getSize(), type.getName());

        inventory.setItem(10, buttons.createDeathLogButton(LogType.DEATH, null));
        inventory.setItem(11, buttons.createJoinLogButton(LogType.JOIN, null));
        inventory.setItem(12, buttons.createQuitLogButton(LogType.QUIT, null));
        inventory.setItem(14, buttons.createWorldChangeLogButton(LogType.WORLD_CHANGE, null));
        inventory.setItem(15, buttons.createForceSaveLogButton(LogType.FORCE, null));
        inventory.setItem(16, buttons.createCrashLogButton(LogType.CRASH, null));

        if (refundMode) {
            inventory.setItem(26, buildRefundHistoryOpener());
        }
    }

    private ItemStack buildRefundHistoryOpener() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = book.getItemMeta();
        UUID id = offlinePlayer.getUniqueId();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Open a list of backups that have");
        lore.add(ChatColor.GRAY + "been used in a refund, with counts.");
        lore.add("");
        long lr = RestoreStatsManager.getLastRestoreTime(id);
        if (lr > 0) {
            lore.add(ChatColor.DARK_GREEN + "Last full restore " + ChatColor.WHITE + RelativeTime.ago(lr)
                    + ChatColor.DARK_GRAY + " (" + PlayerData.getTime(lr) + ")");
        }
        long lrf = RestoreStatsManager.getLastRefundTime(id);
        if (lrf > 0) {
            lore.add(ChatColor.AQUA + "Last refund " + ChatColor.WHITE + RelativeTime.ago(lrf)
                    + ChatColor.DARK_GRAY + " (" + PlayerData.getTime(lrf) + ")");
        }
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Refund history");
            meta.setLore(lore);
            book.setItemMeta(meta);
        }
        CustomDataItemEditor ed = CustomDataItemEditor.editItem(book);
        ed.setString("openRefundHistory", "1");
        ed.setString("uuid", id.toString());
        return ed.setItemData();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void getPlayerMenu() {
        List<String> lore = new ArrayList<>();

        if (offlinePlayer.isOnline()) {
            lore.add(ChatColor.GREEN + "Online now");
        } else if (!offlinePlayer.hasPlayedBefore()) {
            lore.add(ChatColor.RED + "Never played on this server");
        } else {
            lore.add(ChatColor.GRAY + "Offline");
            if (offlinePlayer.getLastPlayed() != 0) {
                long lp = offlinePlayer.getLastPlayed();
                lore.add(ChatColor.GRAY + "Last seen " + ChatColor.WHITE + RelativeTime.ago(lp)
                        + ChatColor.DARK_GRAY + " (" + PlayerData.getTime(lp) + ")");
            }
        }

        inventory.setItem(4, buttons.playerInfoSkull(lore, true));
        UUID uuid = offlinePlayer.getUniqueId();

        PlayerData deathBackup = new PlayerData(uuid, LogType.DEATH, null);
        PlayerData joinBackup = new PlayerData(uuid, LogType.JOIN, null);
        PlayerData quitBackup = new PlayerData(uuid, LogType.QUIT, null);
        PlayerData worldChangeBackup = new PlayerData(uuid, LogType.WORLD_CHANGE, null);
        PlayerData forceSaveBackup = new PlayerData(uuid, LogType.FORCE, null);
        PlayerData crashBackup = new PlayerData(uuid, LogType.CRASH, null);

        if (!joinBackup.doesBackupTypeExist()
                && !quitBackup.doesBackupTypeExist()
                && !deathBackup.doesBackupTypeExist()
                && !worldChangeBackup.doesBackupTypeExist()
                && !forceSaveBackup.doesBackupTypeExist()
                && !crashBackup.doesBackupTypeExist()) {
            staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoBackupError(offlinePlayer.getName()));
        }

        String backupsAvailable = " backup(s)";
        String newestPrefix = ChatColor.DARK_GRAY + "Newest: ";

        inventory.setItem(10, buttons.createDeathLogButton(LogType.DEATH, loreForType(deathBackup, backupsAvailable, newestPrefix)));
        inventory.setItem(11, buttons.createJoinLogButton(LogType.JOIN, loreForType(joinBackup, backupsAvailable, newestPrefix)));
        inventory.setItem(12, buttons.createQuitLogButton(LogType.QUIT, loreForType(quitBackup, backupsAvailable, newestPrefix)));
        inventory.setItem(14, buttons.createWorldChangeLogButton(LogType.WORLD_CHANGE, loreForType(worldChangeBackup, backupsAvailable, newestPrefix)));
        inventory.setItem(15, buttons.createForceSaveLogButton(LogType.FORCE, loreForType(forceSaveBackup, backupsAvailable, newestPrefix)));
        inventory.setItem(16, buttons.createCrashLogButton(LogType.CRASH, loreForType(crashBackup, backupsAvailable, newestPrefix)));

        if (refundMode) {
            inventory.setItem(26, buildRefundHistoryOpener());
        }
    }

    private static List<String> loreForType(PlayerData data, String backupsAvailable, String newestPrefix) {
        int n = data.getAmountOfBackups();
        long ts = data.getNewestBackupTimestamp();
        String newest = data.getNewestBackupTimeFormatted();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "" + n + backupsAvailable);
        if (newest != null && ts > 0) {
            lore.add(newestPrefix + ChatColor.GRAY + newest + ChatColor.DARK_GRAY + " (" + RelativeTime.ago(ts) + ")");
        } else {
            lore.add(ChatColor.DARK_GRAY + "No backups in this category");
        }
        return lore;
    }
}
