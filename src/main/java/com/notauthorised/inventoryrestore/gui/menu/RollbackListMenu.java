package com.notauthorised.inventoryrestore.gui.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.PlayerData;
import com.notauthorised.inventoryrestore.gui.Buttons;
import com.notauthorised.inventoryrestore.gui.GuiDecorItems;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import com.notauthorised.inventoryrestore.util.RelativeTime;

public class RollbackListMenu {

    private int pageNumber;

    private Player staff;
    private UUID playerUUID;
    private LogType logType;
    
    private Buttons buttons;
    private Inventory inventory;

    public RollbackListMenu(Player staff, OfflinePlayer player, LogType logType, int pageNumberIn) {
        this.staff = staff;
        this.playerUUID = player.getUniqueId();
        this.logType = logType;
        this.pageNumber = pageNumberIn;
        this.buttons = new Buttons(playerUUID);
        
        createInventory();
    }
    
    public void createInventory() {
        inventory = Bukkit.createInventory(staff, InventoryName.ROLLBACK_LIST.getSize(), InventoryName.ROLLBACK_LIST.getName());
        
        List<String> lore = new ArrayList<>();  
        if (pageNumber == 1) {
            ItemStack mainMenu = buttons.mainMenuNavButton(MessageData.getMainMenuButton(), logType);
            inventory.setItem(InventoryName.ROLLBACK_LIST.getSize() - 8, mainMenu);
        }       

        if (pageNumber > 1) {
            lore.add("Page " + (pageNumber - 1));
            ItemStack previousPage = buttons.backButton(MessageData.getPreviousPageButton(), logType, pageNumber - 1, lore);

            inventory.setItem(InventoryName.ROLLBACK_LIST.getSize() - 8, previousPage);
            lore.clear();
        }
    }
    
    public Inventory getInventory() {
        return this.inventory;
    }

    public void showBackups() {
        PlayerData playerData = new PlayerData(playerUUID, logType, null);

        //Check how many backups there are in total
        int backups = playerData.getAmountOfBackups();

        //How many rows are required
        int spaceRequired = InventoryName.ROLLBACK_LIST.getSize() - 9;

        //How many pages are required
        int pagesRequired = (int) Math.ceil(backups / (double) spaceRequired);

        //Check if pageNumber supplied is greater than pagesRequired, if true set to last page
        if (pageNumber > pagesRequired) {
            pageNumber = pagesRequired;
        } else if (pageNumber <= 0) {
            pageNumber = 1;
        }

        int backupsAlreadyPassed = spaceRequired * (pageNumber - 1);
        int backupsOnCurrentPage = Math.min(backups, Math.min(spaceRequired, backups - backupsAlreadyPassed));
        List<Long> timeStamps = playerData.getSelectedPageTimestamps(pageNumber);

        int position = 0;
        for (int i = 0; i < backupsOnCurrentPage; i++) {
            try {
                Long timestamp = timeStamps.get(i);
                playerData = new PlayerData(playerUUID, logType, timestamp);

                playerData.getRollbackMenuData();

                String when = PlayerData.getTime(timestamp);
                String displayName = MessageData.getDeathTime(when + ChatColor.DARK_GRAY + " (" + RelativeTime.ago(timestamp) + ")");

                List<String> lore = new ArrayList<>();

                String deathReason = playerData.getDeathReason();
                String reasonText = deathReason != null && !deathReason.isEmpty()
                        ? deathReason
                        : (logType == LogType.DEATH ? "Unknown" : logType.name());
                lore.add(MessageData.getDeathReason(reasonText));
                for (int start = 45; start < reasonText.length(); start += 45) {
                    int end = Math.min(reasonText.length(), start + 45);
                    lore.add(ChatColor.GRAY + reasonText.substring(start, end));
                }

                String world = playerData.getWorld();
                double x = playerData.getX();
                double y = playerData.getY();
                double z = playerData.getZ();
                String location = world + "," + x + "," + y + "," + z;

                if (world != null && !world.isEmpty())
                    lore.add(MessageData.getDeathLocationWorld(world));

                ItemStack item = buttons.createInventoryButton(new ItemStack(Material.CHEST), logType, location, timestamp, displayName, lore);

                inventory.setItem(position, item);

            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            position++;
        }

        List<String> lore = new ArrayList<>();
        if (pageNumber < pagesRequired) {
            lore.add("Page " + (pageNumber + 1));
            ItemStack nextPage = buttons.nextButton(MessageData.getNextPageButton(), logType, pageNumber + 1, lore);

            inventory.setItem(position + 7, nextPage);
            lore.clear();
        }

        int sz = inventory.getSize();
        ItemStack gap = GuiDecorItems.grayGap();
        for (int s = sz - 9; s < sz; s++) {
            ItemStack cur = inventory.getItem(s);
            if (cur == null || cur.getType().isAir()) {
                inventory.setItem(s, gap.clone());
            }
        }
    }

}
