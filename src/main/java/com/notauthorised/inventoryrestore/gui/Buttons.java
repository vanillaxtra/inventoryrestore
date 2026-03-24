package com.notauthorised.inventoryrestore.gui;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.tcoded.lightlibs.bukkitversion.BukkitVersion;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.inventory.RestoreInventory;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Buttons {

    private UUID uuid;

    private static final Material death =
            InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.SKELETON_SKULL : Material.getMaterial("SKULL_ITEM");

    private static final Material join =
            InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.OAK_SAPLING : Material.getMaterial("SAPLING");

    private static final Material quit =
            InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.RED_BED : Material.getMaterial("BED");

    private static final Material worldChange = Material.COMPASS;

    private static final Material forceSave = Material.DIAMOND;


    private static final Material pageSelector =
            InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.WHITE_BANNER : Material.getMaterial("BANNER");

    private static final Material teleport = Material.ENDER_PEARL;

    private static final Material enderChest = Material.ENDER_CHEST;

    private static final Material hunger = Material.COOKED_BEEF;

    private static final Material experience =
            InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.EXPERIENCE_BOTTLE : Material.getMaterial("EXP_BOTTLE");

    private static final Material restoreAllInventory = Material.CLOCK;

    private static final Material restoreAllInventoryDisabled = Material.RED_CONCRETE;

    private static final Material exportStorageIcon =
            InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_11_R1) ?
                    Material.CHEST : Material.BARRIER;


    public Buttons(UUID uuid) {
        this.uuid = uuid;
    }
    
    public Buttons(OfflinePlayer player) {
        this.uuid = player.getUniqueId();
    }
    
    public static Material getDeathLogIcon() {
        return death;
    }
    
    public static Material getJoinLogIcon() {
        return join;
    }
    
    public static Material getQuitLogIcon() {
        return quit;
    }
    
    public static Material getWorldChangeLogIcon() {
        return worldChange;
    }
    
    public static Material getForceSaveLogIcon() {
        return forceSave;
    }

    public static Material getPageSelectorIcon() {
        return pageSelector;
    }

    public static Material getTeleportLocationIcon() {
        return teleport;
    }

    public static Material getEnderChestIcon() {
        return enderChest;
    }

    public static Material getHungerIcon() {
        return hunger;
    }

    public static Material getExperienceIcon() {
        return experience;
    }

    public static Material getRestoreAllInventoryIcon() {
        return restoreAllInventory;
    }

    public static Material getRestoreAllInventoryDisabledIcon() {
        return restoreAllInventoryDisabled;
    }

    public static Material getExportStorageIcon() {
        return exportStorageIcon;
    }

    /** @deprecated use {@link #getExportStorageIcon()} */
    @Deprecated
    public static Material getGiveShulkerBoxIcon() {
        return exportStorageIcon;
    }

    public static Material getNavBackIcon() {
        return Material.ARROW;
    }

    public ItemStack nextButton(String displayName, LogType logType, int page, List<String> lore) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(true);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        meta.setLore(lore);

        button.setItemMeta(meta);

        CustomDataItemEditor editor = CustomDataItemEditor.editItem(button);

        editor.setString("uuid", uuid.toString());
        editor.setString("logType", logType.name());
        editor.setInt("page", page);
        button = editor.setItemData();

        return button;
    }

    public ItemStack backButton(String displayName, LogType logType, int page, List<String> lore) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(false);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        if (lore != null) {
            meta.setLore(lore);
        }

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setInt("page", page);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack enderChestNextButton(String displayName, LogType logType, int page, Long timestamp, List<String> lore) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(true);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        meta.setLore(lore);

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setInt("page", page);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack enderChestBackButton(String displayName, LogType logType, int page, Long timestamp, List<String> lore) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(false);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        if (lore != null) {
            meta.setLore(lore);
        }

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setInt("page", page);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack mainMenuBackButton(String displayName) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(false);

        assert meta != null;
        meta.setPatterns(patterns);

//        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", uuid.toString());
        button = nbt.setItemData();

        return button;
    }

    public ItemStack inventoryMenuBackButton(String displayName, LogType logType, Long timestamp) {
        ItemStack button = new ItemStack(getNavBackIcon());
        ItemMeta meta = button.getItemMeta();
        assert meta != null;
        if (displayName != null) {
            meta.setDisplayName(displayName);
        }
        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setInt("page", 0);

        button = nbt.setItemData();

        return button;
    }

    /** Arrow button: rollback list page 0 → player menu / main listing */
    public ItemStack mainMenuNavButton(String displayName, LogType logType) {
        ItemStack button = new ItemStack(getNavBackIcon());
        ItemMeta meta = button.getItemMeta();
        assert meta != null;
        if (displayName != null) {
            meta.setDisplayName(displayName);
        }
        button.setItemMeta(meta);

        CustomDataItemEditor editor = CustomDataItemEditor.editItem(button);
        editor.setString("uuid", uuid.toString());
        editor.setString("logType", logType.name());
        editor.setInt("page", 0);
        return editor.setItemData();
    }

    public ItemStack createInventoryButton(ItemStack item, LogType logType, String location, Long time, String displayName, List<String> lore) {    	
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {
            meta.setLore(lore);
        }

        meta.setDisplayName(displayName);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", time);
        nbt.setString("location", location);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createDeathLogButton(LogType logType, List<String> lore) {    	
        ItemStack item = new ItemStack(getDeathLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(ChatColor.RED + "Deaths");

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createJoinLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(getJoinLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(ChatColor.GREEN + "Joins");

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createQuitLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(getQuitLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(ChatColor.GOLD + "Quits");

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createWorldChangeLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(getWorldChangeLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "World Changes");

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createForceSaveLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(getForceSaveLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(ChatColor.AQUA + "Force Saves");

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createCrashLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(Material.TNT);        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(ChatColor.DARK_RED + "Crashes");

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack playerHead(List<String> lore, boolean setSkin) {
        return playerHead(lore, setSkin, null);
    }

    /**
     * @param displayNameOverride if non-null, used as the item title instead of the player's username.
     */
    public ItemStack playerHead(List<String> lore, boolean setSkin, String displayNameOverride) {
        if (uuid == null)
            return null;

        ItemStack skull;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1)) {
            skull = new ItemStack(Material.PLAYER_HEAD);
        } else {
            Constructor<?> itemStackConstructor;

            try {
                itemStackConstructor = Class.forName("org.bukkit.inventory.ItemStack").getConstructor(Material.class, int.class, short.class);
                skull = (ItemStack) itemStackConstructor.newInstance(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
                return null;
            }
        }

        assert skull != null;
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        assert skullMeta != null;
        if (setSkin) {
            try {
                if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1)) {
                    skullMeta.setOwningPlayer(player);
                } else {
                    Method method = skullMeta.getClass().getMethod("setOwner", String.class);
                    method.setAccessible(true);
                    method.invoke(skullMeta, player.getName());
                    method.setAccessible(false);
                }
            } catch (IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (displayNameOverride != null) {
            skullMeta.setDisplayName(displayNameOverride);
        } else if (setSkin) {
            skullMeta.setDisplayName(ChatColor.RESET + player.getName());
        }

        if (lore != null) {
            skullMeta.setLore(lore);
        }

        skull.setItemMeta(skullMeta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(skull);

        nbt.setString("uuid", uuid + "");
        skull = nbt.setItemData();

        return skull;
    }

    /**
     * Player summary in the center of the player-data chest; clicking does not open another menu.
     */
    public ItemStack playerInfoSkull(List<String> lore, boolean setSkin) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        List<String> lines = new ArrayList<>();
        String name = player.getName();
        if (name != null) {
            lines.add(ChatColor.GRAY + "Name: " + ChatColor.WHITE + name);
        }
        if (lore != null) {
            lines.addAll(lore);
        }
        ItemStack skull = playerHead(lines, setSkin, ChatColor.AQUA + "" + ChatColor.BOLD + "Player");
        if (skull == null) return null;
        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(skull);
        nbt.setString("staticInfo", "1");
        return nbt.setItemData();
    }

    public ItemStack enderPearlButton(LogType logType, String location) {    	
        ItemStack item = new ItemStack(getTeleportLocationIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getDeathLocation());

        List<String> lore = new ArrayList<>();
        if (location != null) {
            String[] loc = location.split(",");
            lore.add(ChatColor.GOLD + "World: " + ChatColor.WHITE + loc[0]);
            lore.add(ChatColor.GOLD + "X: " + ChatColor.WHITE + loc[1]);
            lore.add(ChatColor.GOLD + "Y: " + ChatColor.WHITE + loc[2]);
            lore.add(ChatColor.GOLD + "Z: " + ChatColor.WHITE + loc[3]);

            meta.setLore(lore);
        } else {
            lore.add(ChatColor.WHITE + "No location saved");
        }

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setString("location", location);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack enderChestButton(LogType logType, Long timestamp, ItemStack[] enderChest) {    	
        ItemStack item = new ItemStack(getEnderChestIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getEnderChestRestoreButton());

        List<String> lore = new ArrayList<>();

        if (enderChest != null && enderChest.length > 1)
            lore.add(ChatColor.WHITE + "Items in Ender Chest");
        else {
            lore.add(ChatColor.WHITE + "Empty");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack hungerButton(LogType logType, int hunger, float saturation) {    	
        ItemStack item = new ItemStack(getHungerIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getHungerRestoreButton());

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setInt("hunger", hunger);
        nbt.setFloat("saturation", saturation);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack experiencePotion(LogType logType, float xp) {    	
        ItemStack item = new ItemStack(getExperienceIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getExperienceRestoreButton());

        List<String> lore = new ArrayList<>();
        lore.add(MessageData.getExperienceRestoreLevel((int) RestoreInventory.getLevel(xp)));
        meta.setLore(lore);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setFloat("xp", xp);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack restoreAllInventory(LogType logType, Long timestamp) {       
        ItemStack item = new ItemStack(getRestoreAllInventoryIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getMainInventoryRestoreButton());

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack restoreAllInventoryDisabled(LogType logType, Long timestamp) {
        ItemStack item = new ItemStack(getRestoreAllInventoryDisabledIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        String[] nameParts = MessageData.getMainInventoryDisabledButton().split("\\\\n");
        String titlePart = nameParts[0];
        ArrayList<String> loreParts = new ArrayList<>();

        meta.setDisplayName(titlePart);
        for (int i = 1; i < nameParts.length; i ++) {
            loreParts.add(nameParts[i]);
        }
        meta.setLore(loreParts);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    private static @NotNull List<Pattern> createBannerPatterns(boolean isNext) {
        List<Pattern> patterns = new ArrayList<>();
        if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) {
            patterns.add(new Pattern(DyeColor.BLACK, PatternType.BASE));
            patterns.add(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS));
            if (isNext) patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL));
            else patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL_RIGHT));
            patterns.add(new Pattern(DyeColor.GRAY, PatternType.BORDER));

        } else {
            patterns.add(new Pattern(DyeColor.BLACK, PatternType.BASE));
            patterns.add(new Pattern(DyeColor.WHITE, PatternType.valueOf("RHOMBUS_MIDDLE")));
            if (isNext) patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL));
            else patterns.add(new Pattern(DyeColor.BLACK, PatternType.valueOf("HALF_VERTICAL_MIRROR")));
            patterns.add(new Pattern(DyeColor.GRAY, PatternType.BORDER));
        }
        return patterns;
    }

    public ItemStack giveExportStorageButton(LogType logType, Long timestamp) {
        ItemStack item = new ItemStack(getExportStorageIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        String[] nameParts = MessageData.getExportStorageButton().split("\\\\n");
        String titlePart = nameParts[0];
        ArrayList<String> loreParts = new ArrayList<>();

        meta.setDisplayName(titlePart);
        for (int i = 1; i < nameParts.length; i ++) {
            loreParts.add(nameParts[i]);
        }
        meta.setLore(loreParts);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    /** @deprecated use {@link #giveExportStorageButton(LogType, Long)} */
    @Deprecated
    public ItemStack giveShulkerBox(LogType logType, Long timestamp) {
        return giveExportStorageButton(logType, timestamp);
    }

}
