package com.notauthorised.inventoryrestore.listeners;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.tcoded.lightlibs.bukkitversion.BukkitVersion;
import com.tcoded.lightlibs.bukkitversion.MCVersion;
import io.papermc.lib.PaperLib;
import com.notauthorised.inventoryrestore.InventoryRollback;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.config.SoundData;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.OfflineRestoreManager;
import com.notauthorised.inventoryrestore.data.PlayerData;
import com.notauthorised.inventoryrestore.gui.Buttons;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import com.notauthorised.inventoryrestore.gui.menu.*;
import com.notauthorised.inventoryrestore.data.RestoreSession;
import com.notauthorised.inventoryrestore.data.RestoreStatsManager;
import com.notauthorised.inventoryrestore.inventory.PlayerInventoryEmptyCheck;
import com.notauthorised.inventoryrestore.inventory.RestoreInventory;
import com.notauthorised.inventoryrestore.util.DiscordRefundWebhook;
import com.notauthorised.inventoryrestore.util.ExportStorageHelper;
import org.bukkit.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ClickGUI implements Listener {

    private final InventoryRestore main;

    private static boolean isLocationAvailable(Location location) {
        return location != null;
    }

    public ClickGUI() {
        this.main = InventoryRestore.getInstance();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        //Cancel listener if the event is not for an EpicFishing GUI menu
        String title = e.getView().getTitle();
        if (!title.equals(InventoryName.MAIN_MENU.getName()) 
                && !title.equals(InventoryName.PLAYER_MENU.getName()) 
                && !title.equals(InventoryName.REFUND_MENU.getName())
                && !title.equalsIgnoreCase(InventoryName.ROLLBACK_LIST.getName())
                && !title.equalsIgnoreCase(InventoryName.MAIN_BACKUP.getName())
                && !title.equalsIgnoreCase(InventoryName.ENDER_CHEST_BACKUP.getName())
                && !title.equalsIgnoreCase(InventoryName.EXPORT_STORAGE.getName())
                && !title.equals(InventoryName.OVERWRITE_WARNING.getName())
                && !title.equals(InventoryName.REFUND_HISTORY.getName()))
            return;

        e.setCancelled(true);

        //Check if inventory is a virtual one and not one that has the same name on a player chest
        if (this.main.getVersion().greaterOrEqThan(BukkitVersion.v1_9_R1) && isLocationAvailable(e.getInventory().getLocation())) {
            e.setCancelled(false);
            return;
        }

        for (Integer slot : e.getRawSlots()) {            
            if (slot < e.getInventory().getSize()) {
                return;
            }
        }

        e.setCancelled(false);
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!title.equals(InventoryName.MAIN_MENU.getName()) 
                && !title.equals(InventoryName.PLAYER_MENU.getName()) 
                && !title.equals(InventoryName.REFUND_MENU.getName())
                && !title.equalsIgnoreCase(InventoryName.ROLLBACK_LIST.getName())
                && !title.equalsIgnoreCase(InventoryName.MAIN_BACKUP.getName())
                && !title.equalsIgnoreCase(InventoryName.ENDER_CHEST_BACKUP.getName())
                && !title.equalsIgnoreCase(InventoryName.EXPORT_STORAGE.getName())
                && !title.equals(InventoryName.OVERWRITE_WARNING.getName())
                && !title.equals(InventoryName.REFUND_HISTORY.getName()))
            return;

        //Check if inventory is a virtual one and not one that has the same name on a player chest
        if (this.main.getVersion().greaterOrEqThan(BukkitVersion.v1_9_R1) && isLocationAvailable(e.getInventory().getLocation())) {
            return;
        }

        e.setCancelled(true);

        Player staff = (Player) e.getWhoClicked();
        ItemStack icon = e.getCurrentItem();

        //Listener for player menu
        if (title.equals(InventoryName.MAIN_MENU.getName())) {
            mainMenu(e,staff, icon);
        }

        //Listener for player menu
        else if (title.equals(InventoryName.PLAYER_MENU.getName())
                || title.equals(InventoryName.REFUND_MENU.getName())) {
            playerMenu(e, staff, icon);
        }

        else if (title.equals(InventoryName.REFUND_HISTORY.getName())) {
            refundHistoryMenu(e, staff, icon);
        }

        //Listener for rollback list menu
        else if (title.equals(InventoryName.ROLLBACK_LIST.getName())) {
            rollbackMenu(e,staff, icon);
        }

        //Listener for main inventory backup menu
        else if (title.equals(InventoryName.MAIN_BACKUP.getName())) {
            mainBackupMenu(e,staff, icon);
        }

        //Listener for enderchest backup menu
        else if (title.equals(InventoryName.ENDER_CHEST_BACKUP.getName())) {
            enderChestBackupMenu(e,staff, icon);
        }

        else if (title.equalsIgnoreCase(InventoryName.EXPORT_STORAGE.getName())) {
            exportStorageMenu(e, staff, icon);
        }

        //Listener for overwrite warning menu
        else if (title.equals(InventoryName.OVERWRITE_WARNING.getName())) {
            overwriteWarningMenu(e, staff, icon);
        }

        else {
            e.setCancelled(true);
        }
    }

    private void mainMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if ((e.getRawSlot() >= 0 && e.getRawSlot() < InventoryName.MAIN_MENU.getSize())) {                
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            //Clicked a page button
            if (icon.getType().equals(Buttons.getPageSelectorIcon())) {
                int page = nbt.getInt("page");

                //Selected to go back to main menu
                boolean showNames = !RestoreSession.isRefundContext(staff.getUniqueId());
                MainMenu menu = new MainMenu(staff, page, showNames);

                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getMainMenu);
            } 
            //Clicked a player head
            else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(nbt.getString("uuid")));
                boolean refund = RestoreSession.isRefundContext(staff.getUniqueId());
                PlayerMenu menu = new PlayerMenu(staff, offlinePlayer, refund);

                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getPlayerMenu);
            }
        } else {
            if (e.getRawSlot() >= e.getInventory().getSize() && !e.isShiftClick()) {
                e.setCancelled(false);
            }
        }
    }

    private void playerMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        //Return if a blank slot is selected
        if (icon == null)
            return;

        int menuSize = InventoryName.PLAYER_MENU.getSize();
        if ((e.getRawSlot() >= 0 && e.getRawSlot() < menuSize)) {
            if (e.getRawSlot() == 26 && icon.getType() == Material.WRITTEN_BOOK
                    && InventoryName.REFUND_MENU.getName().equals(e.getView().getTitle())) {
                CustomDataItemEditor bookNbt = CustomDataItemEditor.editItem(icon);
                if ("1".equals(bookNbt.getString("openRefundHistory")) && bookNbt.getString("uuid") != null) {
                    UUID hid = UUID.fromString(bookNbt.getString("uuid"));
                    staff.openInventory(new RefundHistoryMenu(staff, hid).getInventory());
                    return;
                }
            }
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if ("1".equals(nbt.getString("staticInfo"))) {
                return;
            }
            if (!nbt.hasUUID())
                return;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(nbt.getString("uuid")));

            LogType logType = LogType.valueOf(nbt.getString("logType"));
            RollbackListMenu menu = new RollbackListMenu(staff, offlinePlayer, logType, 1);

            staff.openInventory(menu.getInventory());
            Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::showBackups);

        } else {
            if (e.getRawSlot() >= e.getInventory().getSize() && !e.isShiftClick()) {
                e.setCancelled(false);
            }
        }
    }

    private void refundHistoryMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (e.getRawSlot() >= 0 && e.getRawSlot() < InventoryName.REFUND_HISTORY.getSize()) {
            if (icon == null) return;
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if ("BACK".equals(nbt.getString(RefundHistoryMenu.NBT_ACTION)) && nbt.getString("uuid") != null) {
                UUID id = UUID.fromString(nbt.getString("uuid"));
                OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                PlayerMenu menu = new PlayerMenu(staff, op, true);
                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getPlayerMenu);
            }
        } else {
            if (e.getRawSlot() >= e.getInventory().getSize() && !e.isShiftClick()) {
                e.setCancelled(false);
            }
        }
    }

    private void rollbackMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (e.getRawSlot() >= 0 && e.getRawSlot() < InventoryName.ROLLBACK_LIST.getSize()) {
            if (icon == null) return;

            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            //Player has selected a backup to open
            if (icon.getType().equals(Material.CHEST)) {
                UUID uuid = UUID.fromString(nbt.getString("uuid"));
                Long timestamp = nbt.getLong("timestamp");
                LogType logType = LogType.valueOf(nbt.getString("logType"));
                String location = nbt.getString("location");

                // Run all data retrieval operations async to avoid tick lag
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Init from MySQL or, if YAML, init & load config file
                        PlayerData data = new PlayerData(uuid, logType, timestamp);

                        // Get from MySQL
                        if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                            try {
                                data.getAllBackupData().get();
                            } catch (ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }

                        // Create inventory
                        MainInventoryBackupMenu menu = new MainInventoryBackupMenu(staff, data, location);

                        // Display inventory to player
                        Future<InventoryView> inventoryViewFuture =
                                main.getServer().getScheduler().callSyncMethod(main,
                                        () -> staff.openInventory(menu.getInventory()));
                        //If the backup file is invalid it will return null, we want to catch it here
                        try {
                            inventoryViewFuture.get();
                            // Start placing items in the inventory async
                            menu.showBackupItems();
                        } catch (NullPointerException | ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(main);
            } 

            // Page / back (banner for pages, arrow for "back to player" on page 1)
            else if (icon.getType().equals(Buttons.getPageSelectorIcon())
                    || icon.getType().equals(Buttons.getNavBackIcon())) {
                int page = nbt.getInt("page");

                //Selected to go back to main menu
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(nbt.getString("uuid")));
                if (page == 0) {
                    PlayerMenu menu = new PlayerMenu(staff, player);

                    staff.openInventory(menu.getInventory());
                    Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getPlayerMenu);
                } else {
                    LogType logType = LogType.valueOf(nbt.getString("logType"));
                    RollbackListMenu menu = new RollbackListMenu(staff, player, logType, page);

                    staff.openInventory(menu.getInventory());
                    Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::showBackups);
                }
            }	
        } else {
            if (e.getRawSlot() >= e.getInventory().getSize() && !e.isShiftClick()) {
                e.setCancelled(false);
            }
        }
    }

    private void mainBackupMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (!e.getView().getTitle().equals(InventoryName.MAIN_BACKUP.getName()))
            return;

        if (e.getRawSlot() >= (InventoryName.MAIN_BACKUP.getSize() - 9) && e.getRawSlot() < InventoryName.MAIN_BACKUP.getSize()) {
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(nbt.getString("uuid")));            
            LogType logType = LogType.valueOf(nbt.getString("logType"));
            Long timestamp = nbt.getLong("timestamp");

            // Back to rollback list (arrow) or legacy banner back
            if (icon.getType().equals(Buttons.getNavBackIcon())
                    || icon.getType().equals(Buttons.getPageSelectorIcon())) {
                RollbackListMenu menu = new RollbackListMenu(staff, offlinePlayer, logType, 1);

                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::showBackups);
            }

            else if (e.getRawSlot() == MainInventoryBackupMenu.EXPORT_STORAGE_BUTTON_SLOT
                    && icon.getType().equals(Buttons.getExportStorageIcon())) {
                if (!staff.hasPermission("inventoryrestore.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }
                if (main.getVersion().lessThan(MCVersion.v1_11.toBukkitVersion())) {
                    return;
                }
                Bukkit.getScheduler().runTask(main, () ->
                        staff.openInventory(new ExportStorageMenu(staff, offlinePlayer.getUniqueId(), logType, timestamp).getInventory()));
            }

            //Clicked icon to overwrite player inventory - open warning menu first (skip if online & empty inv)
            else if (icon.getType().equals(Buttons.getRestoreAllInventoryIcon())) {
                if (!staff.hasPermission("inventoryrestore.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }
                if (offlinePlayer.isOnline()) {
                    Player online = (Player) offlinePlayer;
                    if (!PlayerInventoryEmptyCheck.hasAnything(online)) {
                        performFullRestore(staff, offlinePlayer, logType, timestamp,
                                RestoreSession.isRefundContext(staff.getUniqueId()));
                        return;
                    }
                }
                OverwriteWarningMenu warnMenu = new OverwriteWarningMenu(staff, offlinePlayer, logType, timestamp);
                staff.openInventory(warnMenu.getInventory());
                warnMenu.startLiveRefresh();
            }

            // Clicked icon to teleport player to backup coordinates
            else if (icon.getType().equals(Buttons.getTeleportLocationIcon())) {
                // Perm check
                if (!staff.hasPermission("inventoryrestore.restore.teleport")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                String[] location = nbt.getString("location").split(",");			
                World world = Bukkit.getWorld(location[0]);

                if (world == null) {
                    //World is not available
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getDeathLocationInvalidWorldError(location[0]));
                    return;
                }

                Location loc = new Location(world, 
                        Math.floor(Double.parseDouble(location[1])), 
                        Math.floor(Double.parseDouble(location[2])), 
                        Math.floor(Double.parseDouble(location[3])))
                        .add(0.5, 0.5, 0.5);				

                // Teleport player on a slight delay to block the teleport icon glitching out into the player inventory
                Bukkit.getScheduler().runTaskLater(InventoryRollback.getInstance(), () -> {
                    e.getWhoClicked().closeInventory();
                    PaperLib.teleportAsync(staff,loc).thenAccept((result) -> {
                        if (SoundData.isTeleportEnabled())
                            staff.playSound(loc, SoundData.getTeleport(), 1, 1);

                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getDeathLocationTeleport(loc));
                    });
                }, 1L);
            } 

            // Clicked icon to restore backup players ender chest
            else if (icon.getType().equals(Buttons.getEnderChestIcon())) {

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Init from MySQL or, if YAML, init & load config file
                        PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                        // Get data if using MySQL
                        if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                            try {
                                data.getAllBackupData().get();
                            } catch (ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }

                        // Create Inventory
                        EnderChestBackupMenu menu = new EnderChestBackupMenu(staff, data, 1);

                        // Open inventory sync (compressed code)
                        Future<Void> futureOpenInv = main.getServer().getScheduler().callSyncMethod(main,
                                () -> {
                                    staff.openInventory(menu.getInventory());
                                    return null;
                                });
                        try {
                            futureOpenInv.get();
                        } catch (ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        // Place items async
                        menu.showEnderChestItems();
                    }
                }.runTaskAsynchronously(this.main);
            }

            //Clicked icon to restore backup players hunger (health restore removed)
            else if (icon.getType().equals(Buttons.getHungerIcon())) {
                // Perm check
                if (!staff.hasPermission("inventoryrestore.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                if (offlinePlayer.isOnline()) {
                    Player player = (Player) offlinePlayer;	
                    int hunger = nbt.getInt("hunger");
                    Float saturation = nbt.getFloat("saturation");

                    player.setFoodLevel(hunger);
                    player.setSaturation(saturation);

                    if (SoundData.isHungerRestoredEnabled())
                        player.playSound(player.getLocation(), SoundData.getHungerRestored(), 1, 1);

                    player.sendMessage(MessageData.getPluginPrefix() + MessageData.getHungerRestoredPlayer(staff.getName()));
                    if (!staff.getUniqueId().equals(player.getUniqueId()))
                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getHungerRestored(player.getName()));
                } else {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getHungerNotOnline(offlinePlayer.getName()));
                }
            } 

            //Clicked icon to restore backup players experience
            else if (icon.getType().equals(Buttons.getExperienceIcon())) {
                // Perm check
                if (!staff.hasPermission("inventoryrestore.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                if (offlinePlayer.isOnline()) {				
                    Player player = (Player) offlinePlayer;	
                    Float xp = nbt.getFloat("xp");

                    RestoreInventory.setTotalExperience(player, xp);

                    if (SoundData.isExperienceRestoredEnabled())
                        player.playSound(player.getLocation(), SoundData.getExperienceSound(), 1, 1);

                    int level = (int) RestoreInventory.getLevel(xp);
                    player.sendMessage(MessageData.getPluginPrefix() + MessageData.getExperienceRestoredPlayer(staff.getName(), level));
                    if (!staff.getUniqueId().equals(player.getUniqueId()))
                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getExperienceRestored(player.getName(), level));
                } else {				    
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getExperienceNotOnlinePlayer(offlinePlayer.getName()));
                }
            }
        } else {
            int slotIndex = e.getRawSlot();
            int topInvSize = e.getView().getTopInventory().getSize();
            boolean clickIsWithinPlayerInventory = slotIndex >= topInvSize;

            boolean clickIsWithinMainBackupInv = slotIndex < topInvSize - 18;
            boolean notInLastLine = slotIndex < topInvSize - 9;
            boolean notBeforeArmorSlots = slotIndex > topInvSize - 15;

            boolean clickIsWithinArmorOrOffHandSlots = notInLastLine && notBeforeArmorSlots;
            boolean isValidBackupMenuInteraction = clickIsWithinMainBackupInv || clickIsWithinArmorOrOffHandSlots;

            //Allow items to be grabbed in the top inventory except the bottom line AND NOT player inventory items to be shift clicked to top inventory
            if (clickIsWithinPlayerInventory && !e.isShiftClick()) {
                e.setCancelled(false);
            } else if (isValidBackupMenuInteraction) {
                if (staff.hasPermission("inventoryrestore.restore")) {
                    e.setCancelled(false);
                } else {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                }
            }
        }
    }

    private void enderChestBackupMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (!e.getView().getTitle().equals(InventoryName.ENDER_CHEST_BACKUP.getName()))
            return;

        if (e.getRawSlot() >= (InventoryName.ENDER_CHEST_BACKUP.getSize() - 9) && e.getRawSlot() < InventoryName.ENDER_CHEST_BACKUP.getSize()) {
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(nbt.getString("uuid")));
            LogType logType = LogType.valueOf(nbt.getString("logType"));
            Long timestamp = nbt.getLong("timestamp");

            // Back / pagination (arrow from main backup, banner for multi-page ender)
            if (icon.getType().equals(Buttons.getNavBackIcon())
                    || icon.getType().equals(Buttons.getPageSelectorIcon())) {

                //Player has selected a page icon
                int page = nbt.getInt("page");

                //Selected to go back to main menu
                if (page == 0) {

                    // Run all data retrieval operations async to avoid tick lag
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Init from MySQL or, if YAML, init & load config file
                            PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                            // Get data if using MySQL
                            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                                try {
                                    data.getAllBackupData().get();
                                } catch (ExecutionException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            // Get location of where the backup was made from data
                            String location = data.getWorld() + "," + data.getX() + "," + data.getY() + "," + data.getZ();

                            // Create inventory
                            MainInventoryBackupMenu menu = new MainInventoryBackupMenu(staff, data, location);

                            // Display inventory to player
                            Future<InventoryView> inventoryViewFuture = main.getServer().getScheduler().callSyncMethod(main,
                                    () -> staff.openInventory(menu.getInventory()));
                            //If the backup file is invalid it will return null, we want to catch it here
                            try {
                                inventoryViewFuture.get();
                                // Start placing items in the inventory async
                                menu.showBackupItems();
                            } catch (NullPointerException | ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(main);

                } else {

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Init from MySQL or, if YAML, init & load config file
                            PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                            // Get data if using MySQL
                            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                                try {
                                    data.getAllBackupData().get();
                                } catch (ExecutionException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            // Create Inventory
                            EnderChestBackupMenu menu = new EnderChestBackupMenu(staff, data, page);

                            // Open inventory sync (compressed code)
                            Future<Void> futureOpenInv = main.getServer().getScheduler().callSyncMethod(main,
                                    () -> {
                                        staff.openInventory(menu.getInventory());
                                        return null;
                                    });
                            try {
                                futureOpenInv.get();
                            } catch (ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }

                            // Place items async
                            menu.showEnderChestItems();
                        }
                    }.runTaskAsynchronously(this.main);
                }
            }

            //Clicked icon to overwrite player ender chest with backup data
            else if (icon.getType().equals(Buttons.getRestoreAllInventoryIcon())) {
                // Perm check
                if (!staff.hasPermission("inventoryrestore.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                if (offlinePlayer.isOnline()) {
                    Player player = (Player) offlinePlayer;

                    // Run all data retrieval operations async to avoid tick lag
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Init from MySQL or, if YAML, init & load config file
                            PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                            // Get from MySQL
                            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                                try {
                                    data.getAllBackupData().get();
                                } catch (ExecutionException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            // Display inventory to player
                            Future<Void> inventoryReplaceFuture = main.getServer().getScheduler().callSyncMethod(main,
                                    () -> {
                                        ItemStack[] enderChest = data.getEnderChest();
                                        if (enderChest == null) enderChest = new ItemStack[0];
                                        player.getEnderChest().setContents(enderChest);
                                        return null;
                                    });

                            //If the backup file is invalid it will return null, we want to catch it here
                            try {
                                inventoryReplaceFuture.get();
                            } catch (NullPointerException | ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(main);

                    if (SoundData.isInventoryRestoreEnabled())
                        player.playSound(player.getLocation(), SoundData.getInventoryRestored(), 1, 1); 

                    player.sendMessage(MessageData.getPluginPrefix() + MessageData.getEnderChestRestoredPlayer(staff.getName()));
                    if (!staff.getUniqueId().equals(player.getUniqueId()))
                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getEnderChestRestored(offlinePlayer.getName()));
                } else {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getEnderChestNotOnline(offlinePlayer.getName()));
                }
            }
        } else {
            int slotIndex = e.getRawSlot();
            int topInvSize = e.getView().getTopInventory().getSize();
            boolean clickIsWithinPlayerInventory = slotIndex >= topInvSize;

            if (clickIsWithinPlayerInventory && !e.isShiftClick()) {
                e.setCancelled(false);
            } else if (slotIndex < topInvSize - 9) {
                // Perm check
                if (!staff.hasPermission("inventoryrestore.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }
                e.setCancelled(false);
            }
        }
    }

    private void exportStorageMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (e.getRawSlot() >= 0 && e.getRawSlot() < InventoryName.EXPORT_STORAGE.getSize()) {
            if (icon == null) return;

            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            String modeStr = nbt.getString(ExportStorageMenu.NBT_EXPORT_MODE);
            if (modeStr == null || modeStr.isEmpty()) return;

            if (!staff.hasPermission("inventoryrestore.restore")) {
                staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                return;
            }

            UUID targetUuid = UUID.fromString(nbt.getString("uuid"));
            LogType logType = LogType.valueOf(nbt.getString("logType"));
            long timestamp = nbt.getLong("timestamp");

            if ("BACK".equals(modeStr)) {
                staff.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUuid);
                        PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);
                        if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                            try {
                                data.getAllBackupData().get();
                            } catch (ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                        String location = data.getWorld() + "," + data.getX() + "," + data.getY() + "," + data.getZ();
                        MainInventoryBackupMenu menu = new MainInventoryBackupMenu(staff, data, location);
                        try {
                            Future<InventoryView> future = main.getServer().getScheduler().callSyncMethod(main,
                                    () -> staff.openInventory(menu.getInventory()));
                            future.get();
                            menu.showBackupItems();
                        } catch (NullPointerException | ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(main);
                return;
            }

            final ExportStorageHelper.Mode mode;
            try {
                mode = ExportStorageHelper.Mode.valueOf(modeStr);
            } catch (IllegalArgumentException ex) {
                return;
            }

            if (main.getVersion().lessThan(MCVersion.v1_11.toBukkitVersion())) {
                if (mode == ExportStorageHelper.Mode.SHULKER
                        || mode == ExportStorageHelper.Mode.BARREL
                        || mode == ExportStorageHelper.Mode.CHEST) {
                    return;
                }
            }

            staff.closeInventory();
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerData data = new PlayerData(Bukkit.getOfflinePlayer(targetUuid), logType, timestamp);
                    if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                        try {
                            data.getAllBackupData().get();
                        } catch (ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    ItemStack[] mainInv = data.getMainInventory();
                    ItemStack[] armour = data.getArmour();
                    ItemStack offhand = data.getOffhand();
                    Bukkit.getScheduler().runTask(main, () ->
                            ExportStorageHelper.giveExport(staff, mode, mainInv, armour, offhand));
                }
            }.runTaskAsynchronously(main);
        } else {
            if (e.getRawSlot() >= e.getInventory().getSize() && !e.isShiftClick()) {
                e.setCancelled(false);
            }
        }
    }

    private void overwriteWarningMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (icon == null || !icon.hasItemMeta()) return;
        if (e.getRawSlot() < 45) return; // Only handle bottom row buttons

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
        String action = nbt.getString("action");
        if (action == null || action.isEmpty()) return;
        OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(nbt.getString("uuid")));
        LogType logType = LogType.valueOf(nbt.getString("logType"));
        Long timestamp = nbt.getLong("timestamp");

        if ("back".equals(action)) {
            staff.closeInventory();
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                PlayerData data = new PlayerData(target, logType, timestamp);
                if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                    try { data.getAllBackupData().get(); } catch (Exception ex) { ex.printStackTrace(); }
                }
                String location = data.getWorld() + "," + data.getX() + "," + data.getY() + "," + data.getZ();
                MainInventoryBackupMenu menu = new MainInventoryBackupMenu(staff, data, location);
                MainInventoryBackupMenu finalMenu = menu;
                Bukkit.getScheduler().runTask(main, () -> {
                    staff.openInventory(finalMenu.getInventory());
                    finalMenu.showBackupItems();
                });
            });
        } else if ("confirm".equals(action)) {
            staff.closeInventory();
            boolean refund = "1".equals(nbt.getString("refund"));
            performFullRestore(staff, target, logType, timestamp, refund);
        }
    }

    private void performFullRestore(Player staff, OfflinePlayer target, LogType logType, Long timestamp, boolean refund) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData data = new PlayerData(target, logType, timestamp);
                if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                    try { data.getAllBackupData().get(); } catch (Exception ex) { ex.printStackTrace(); }
                }

                if (target.isOnline()) {
                    Player player = (Player) target;
                    ItemStack[] inv = data.getMainInventory();
                    ItemStack[] armour = data.getArmour();
                    ItemStack offhand = data.getOffhand();
                    Bukkit.getScheduler().runTask(main, () -> {
                        if (inv != null) player.getInventory().setContents(inv);
                        if (armour != null && armour.length > 0)
                            player.getInventory().setArmorContents(java.util.Arrays.copyOf(armour, 4));
                        // Always restore offhand with inventory (set or clear to match backup)
                        player.getInventory().setItemInOffHand(offhand != null && !offhand.getType().isAir() ? offhand : new ItemStack(Material.AIR));
                        ItemStack[] ender = data.getEnderChest();
                        if (ender != null) player.getEnderChest().setContents(ender);
                        player.setFoodLevel(data.getFoodLevel());
                        player.setSaturation(data.getSaturation());
                        RestoreInventory.setTotalExperience(player, data.getXP());
                        if (SoundData.isInventoryRestoreEnabled())
                            player.playSound(player.getLocation(), SoundData.getInventoryRestored(), 1, 1);
                        player.sendMessage(MessageData.getPluginPrefix() + MessageData.getMainInventoryRestoredPlayer(staff.getName()));
                        if (!staff.getUniqueId().equals(player.getUniqueId()))
                            staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getMainInventoryRestored(target.getName()));
                        long backupTs = timestamp != null ? timestamp : 0L;
                        RestoreStatsManager.recordFullRestore(
                                target.getUniqueId(), staff.getName(), refund, logType, backupTs);
                        if (refund) {
                            DiscordRefundWebhook.sendAsync(staff.getName(), target.getName(), logType, backupTs);
                        }
                    });
                } else {
                    OfflineRestoreManager.scheduleRestore(target, logType, timestamp);
                    staff.sendMessage(MessageData.getPluginPrefix() + "Restore scheduled for " + target.getName() + " - will apply when they join.");
                    long backupTs = timestamp != null ? timestamp : 0L;
                    RestoreStatsManager.recordFullRestore(
                            target.getUniqueId(), staff.getName(), refund, logType, backupTs);
                    if (refund) {
                        DiscordRefundWebhook.sendAsync(staff.getName(), target.getName(), logType, backupTs);
                    }
                }
            }
        }.runTaskAsynchronously(main);
    }

}