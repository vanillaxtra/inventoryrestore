package com.notauthorised.inventoryrestore.gui;

import com.notauthorised.inventoryrestore.config.ConfigData;

public enum InventoryName {
	
    MAIN_MENU("Inventory Restore", 36),
	PLAYER_MENU("Player Data", 27),
	REFUND_MENU("Refund", 27),
	REFUND_HISTORY("Refund history", 36),
	ROLLBACK_LIST("Rollbacks", ConfigData.getBackupLinesVisible() * 9 + 9),
	MAIN_BACKUP("Main Inventory Backup", 54),
    ENDER_CHEST_BACKUP("Ender Chest Backup", 36),
	OVERWRITE_WARNING("Overwrite Warning", 54),
	EXPORT_STORAGE("Export to Storage", 27);
	
	private final String menuName;
	private final int size;
	
	private InventoryName(String name, int size) {
		this.menuName = name;
		this.size = size;
	}
	
	public String getName() {
		return this.menuName;
	}
	
	public int getSize() {
	    return this.size;
	}

}
