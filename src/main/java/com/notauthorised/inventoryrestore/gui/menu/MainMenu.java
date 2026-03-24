package com.notauthorised.inventoryrestore.gui.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.gui.Buttons;
import com.notauthorised.inventoryrestore.gui.InventoryName;

import java.util.Collections;

public class MainMenu {

    private Player staff;

    private int pagesRequired;
    private int pageNumber;
    private Buttons buttons;

    private int startSelection;
    private int playerHeadLoops;

    private List<Player> onlinePlayers;
    private Inventory inventory;

    /** When true (e.g. /restore), heads use each player's username; when false (e.g. /refund), a generic label is used. */
    private final boolean showUsernameOnHeads;

    public MainMenu(Player staff, int pageNumber) {
        this(staff, pageNumber, true);
    }

    public MainMenu(Player staff, int pageNumber, boolean showUsernameOnHeads) {
        this.staff = staff;
        this.pageNumber = pageNumber;
        this.showUsernameOnHeads = showUsernameOnHeads;
        this.buttons = new Buttons(staff.getUniqueId());

        getPlayerHeadData();
        createInventory();
    }

    public void createInventory() {
        inventory = Bukkit.createInventory(staff, InventoryName.MAIN_MENU.getSize(), InventoryName.MAIN_MENU.getName());

        List<String> lore = new ArrayList<>(); 
        if (pageNumber > 1) {
            lore.add("Page " + (pageNumber - 1));
            ItemStack previousPage = buttons.backButton(MessageData.getPreviousPageButton(), LogType.UNKNOWN, pageNumber - 1, lore);

            inventory.setItem(InventoryName.MAIN_MENU.getSize() - 8, previousPage);
            lore.clear();
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void getMainMenu() {        
        int selection = startSelection;
        for (int i = 0; i < playerHeadLoops; i++) {
            Player player = onlinePlayers.get(selection);
            Buttons playerButton = new Buttons(player);

            String titleOverride = showUsernameOnHeads
                    ? null
                    : ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Select";
            inventory.setItem(i, playerButton.playerHead(
                    Collections.singletonList(ChatColor.DARK_GRAY + "View inventory backups"),
                    true,
                    titleOverride));
            selection++;
        }

        List<String> lore = new ArrayList<>();
        if (pageNumber < pagesRequired) {
            lore.add("Page " + (pageNumber + 1));
            ItemStack nextPage = buttons.nextButton(MessageData.getNextPageButton(), LogType.UNKNOWN, pageNumber + 1, lore);

            inventory.setItem(InventoryName.MAIN_MENU.getSize() - 2, nextPage);
            lore.clear();
        }
    }

    public void getPlayerHeadData() {
        //Get current online players
        onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        //Check how many online players there are in total
        int playersOnline = onlinePlayers.size();

        //How many rows are required
        int spaceRequired = InventoryName.MAIN_MENU.getSize() - 9;

        //How many pages are required
        pagesRequired = (int) Math.ceil(playersOnline / (double) spaceRequired);

        //Check if pageNumber supplied is greater then pagesRequired, if true set to last page
        if (pageNumber > pagesRequired) {
            pageNumber = pagesRequired;
        } else if (pageNumber <= 0) {
            pageNumber = 1;
        }

        //Get the amount of players that will show on this page
        startSelection = (((InventoryName.MAIN_MENU.getSize() - 9) * pageNumber) - (InventoryName.MAIN_MENU.getSize() - 9));

        int variance = playersOnline - (spaceRequired + startSelection);

        if (variance > 0) {
            playerHeadLoops = spaceRequired;
        } else {
            playerHeadLoops = spaceRequired + variance;
        }
    }

}
