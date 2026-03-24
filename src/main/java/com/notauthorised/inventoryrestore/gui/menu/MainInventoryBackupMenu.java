package com.notauthorised.inventoryrestore.gui.menu;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.tcoded.lightlibs.bukkitversion.MCVersion;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.notauthorised.inventoryrestore.data.BackupActivityTracker;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.PlayerData;
import com.notauthorised.inventoryrestore.gui.Buttons;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainInventoryBackupMenu {

	public static final int EXPORT_STORAGE_BUTTON_SLOT = 47;
	public static final int ACTIVITY_BOOK_SLOT = 53;
	public static final String NBT_OPEN_BACKUP_ACTIVITY = "openBackupActivity";

	/** Main grid + armor/offhand column (not the bottom button row). */
	public static boolean isEditablePreviewSlot(int rawSlot) {
		return (rawSlot >= 0 && rawSlot <= 35) || (rawSlot >= 40 && rawSlot <= 44);
	}
	private final InventoryRestore main;

	private final Player staff;
	private final UUID playerUUID;
	private final LogType logType;
	private final Long timestamp;
	private final ItemStack[] mainInventory;
	private final ItemStack[] armor;
	private final ItemStack[] enderChest;
	private final String location;
	private final double health;
	private final int hunger;
	private final float saturation;
	private final float xp;
	
    private final Buttons buttons;
    private Inventory inventory;

	private int mainInvLen;
	
	public MainInventoryBackupMenu(Player staff, PlayerData data, String location) {
		this.main = InventoryRestore.getInstance();

		this.staff = staff;
		this.playerUUID = data.getOfflinePlayer().getUniqueId();
		this.logType = data.getLogType();
		this.timestamp = data.getTimestamp();
		this.mainInventory = data.getMainInventory();
		this.armor = data.getArmour();
	    this.enderChest = data.getEnderChest();
		this.location = location;
		this.health = data.getHealth();
		this.hunger = data.getFoodLevel();
		this.saturation = data.getSaturation();
		this.xp = data.getXP();
		
		this.buttons = new Buttons(playerUUID);

		this.mainInvLen = mainInventory == null ? 0 : mainInventory.length;
		
		createInventory();
	}
	
	public void createInventory() {
	    inventory = Bukkit.createInventory(staff, InventoryName.MAIN_BACKUP.getSize(), InventoryName.MAIN_BACKUP.getName());
	    
	    //Add back button
        inventory.setItem(45, buttons.inventoryMenuBackButton(MessageData.getBackButton(), logType, timestamp));

		// Export backup to storage (shulker, barrel, chest, bundle, drop)
		if (main.getVersion().greaterOrEqThan(MCVersion.v1_11.toBukkitVersion()))
			inventory.setItem(EXPORT_STORAGE_BUTTON_SLOT, buttons.giveExportStorageButton(logType, timestamp));

		// Add restore all player inventory button
		if (ConfigData.isRestoreToPlayerButton())
			inventory.setItem(48, buttons.restoreAllInventory(logType, timestamp));
		else
			inventory.setItem(48, buttons.restoreAllInventoryDisabled(logType, timestamp));

		//Add teleport back button
		inventory.setItem(49, buttons.enderPearlButton(logType, location));

		//Add Enderchest icon
		inventory.setItem(50, buttons.enderChestButton(logType, timestamp, enderChest));

		//Add hunger icon (health restore removed)
		inventory.setItem(51, buttons.hungerButton(logType, hunger, saturation));

		//Add Experience Bottle
		inventory.setItem(52, buttons.experiencePotion(logType, xp));

		int viewCount = BackupActivityTracker.countViews(playerUUID, logType, timestamp);
		int restoreCount = BackupActivityTracker.countRestores(playerUUID, logType, timestamp);
		ItemStack activityBook = new ItemStack(Material.WRITTEN_BOOK);
		ItemMeta actMeta = activityBook.getItemMeta();
		if (actMeta != null) {
			actMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Backup activity");
			List<String> actLore = new ArrayList<>();
			actLore.add(ChatColor.GRAY + "Full restores: " + ChatColor.WHITE + restoreCount);
			actLore.add(ChatColor.GRAY + "Times viewed: " + ChatColor.WHITE + viewCount);
			actLore.add(ChatColor.DARK_GRAY + "Click: who opened / who restored");
			actMeta.setLore(actLore);
			activityBook.setItemMeta(actMeta);
		}
		CustomDataItemEditor actEd = CustomDataItemEditor.editItem(activityBook);
		actEd.setString(NBT_OPEN_BACKUP_ACTIVITY, "1");
		actEd.setString("uuid", playerUUID.toString());
		actEd.setString("logType", logType.name());
		actEd.setLong("timestamp", timestamp);
		inventory.setItem(ACTIVITY_BOOK_SLOT, actEd.setItemData());
	}
	
	public Inventory getInventory() {
	    return this.inventory;
	}
		
	public void showBackupItems() {
		// Make sure we are not running this on the main thread
		assert !Bukkit.isPrimaryThread();

		int item = 0;
		int position = 0;

		//If the backup file is invalid it will return null, we want to catch it here
		try {
    		// Add items, 5 per tick
			new BukkitRunnable() {

				boolean processedHotbar;
				int menuPos = 27;
				int backupPos = 0;
				final int max = Math.min(mainInvLen, 36); // excluded

				@Override
				public void run() {
					for (int i = 0; i < 6; i++) {
						// If hit max item position, stop
						if (backupPos >= max) {
							this.cancel();
							return;
						}

						ItemStack itemStack = mainInventory[backupPos];
						if (itemStack != null) {
							inventory.setItem(menuPos, itemStack);
						}

						// Move to next menu slot
						menuPos++;
						// We were incrementing the hotbar position (bottom of the UI) first. Once we reach that
						// slot, we move back to the top of the UI for the rest of the inventory
						if (menuPos >= 36 && !processedHotbar) {
							menuPos = 0;
						}

						// Move to next item stack
						backupPos++;
					}
				}
			}.runTaskTimer(main, 0, 1);
		} catch (Exception ex) {
			ex.printStackTrace();
			staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getErrorInventory());
		    return;
		}

		item = 36;
		position = 44;
		
		//Add armor
		if (armor != null && armor.length > 0) {
			try {
				for (int i = 0; i < armor.length; i++) {
					// Place item safely
					final int finalPos = position;
					final int finalItem = i;
					Future<Void> placeItemFuture = main.getServer().getScheduler().callSyncMethod(main,
							() -> {
								inventory.setItem(finalPos, armor[finalItem]);
								return null;
							});
					placeItemFuture.get();
					position--;
				}
			} catch (ExecutionException | InterruptedException ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				for (; item < mainInvLen; item++) {
					if (mainInventory[item] != null) {
						// Place item safely
						final int finalPos = position;
						final int finalItem = item;
						Future<Void> placeItemFuture = main.getServer().getScheduler().callSyncMethod(main,
								() -> {
									inventory.setItem(finalPos, mainInventory[finalItem]);
									return null;
								});
						placeItemFuture.get();
						position--;
					}
				}
			} catch (ExecutionException | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
		
}
