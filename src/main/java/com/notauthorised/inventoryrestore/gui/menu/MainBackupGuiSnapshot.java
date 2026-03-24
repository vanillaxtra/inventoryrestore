package com.notauthorised.inventoryrestore.gui.menu;

import com.notauthorised.inventoryrestore.customdata.CustomDataItemEditor;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Live contents of the main backup GUI (what the staff sees), mapped back to player inventory layout.
 * Does not persist to stored backup data.
 */
public final class MainBackupGuiSnapshot {

    private final UUID targetUuid;
    private final LogType logType;
    private final long timestamp;
    private final ItemStack[] mainContents;
    private final ItemStack[] armorContents;
    private final boolean useDiskOffhand;
    private final ItemStack offhandFromGui;

    private MainBackupGuiSnapshot(UUID targetUuid, LogType logType, long timestamp,
                                ItemStack[] mainContents, ItemStack[] armorContents,
                                boolean useDiskOffhand, ItemStack offhandFromGui) {
        this.targetUuid = targetUuid;
        this.logType = logType;
        this.timestamp = timestamp;
        this.mainContents = mainContents;
        this.armorContents = armorContents;
        this.useDiskOffhand = useDiskOffhand;
        this.offhandFromGui = offhandFromGui;
    }

    public boolean matches(UUID targetUuid, LogType logType, long timestamp) {
        return this.targetUuid.equals(targetUuid)
                && this.logType == logType
                && this.timestamp == timestamp;
    }

    public ItemStack[] getMainContents() {
        return mainContents;
    }

    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    /**
     * Offhand is not filled from backup in the GUI; use stored backup offhand unless staff put an item in slot 40.
     */
    public ItemStack resolveOffhand(ItemStack diskOffhand) {
        if (useDiskOffhand) return diskOffhand;
        return offhandFromGui;
    }

    /**
     * Reads the open main backup menu and clones item areas (not the button row).
     * Validates against the back-button NBT so the wrong menu cannot be captured.
     */
    public static MainBackupGuiSnapshot tryCapture(InventoryView view, UUID expectedTarget,
                                                   LogType expectedLogType, long expectedTimestamp) {
        if (view == null || !InventoryName.MAIN_BACKUP.getName().equals(view.getTitle()))
            return null;
        Inventory top = view.getTopInventory();
        if (top.getSize() != InventoryName.MAIN_BACKUP.getSize())
            return null;

        ItemStack back = top.getItem(45);
        if (back == null || !back.hasItemMeta())
            return null;
        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(back);
        if (!nbt.hasUUID())
            return null;
        UUID uuid = UUID.fromString(nbt.getString("uuid"));
        LogType lt = LogType.valueOf(nbt.getString("logType"));
        long ts = nbt.getLong("timestamp");
        if (!uuid.equals(expectedTarget) || lt != expectedLogType || ts != expectedTimestamp)
            return null;

        // Inverse of MainInventoryBackupMenu.showBackupItems: backup index 0..8 -> GUI 27..35, 9..35 -> GUI 0..26
        ItemStack[] main = new ItemStack[36];
        for (int gui = 0; gui <= 26; gui++) {
            main[gui + 9] = cloneOrNull(top.getItem(gui));
        }
        for (int gui = 27; gui <= 35; gui++) {
            main[gui - 27] = cloneOrNull(top.getItem(gui));
        }

        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            int guiSlot = 44 - i;
            armor[i] = cloneOrNull(top.getItem(guiSlot));
        }

        ItemStack rawOff = top.getItem(40);
        boolean useDisk = rawOff == null || rawOff.getType().isAir();
        ItemStack offClone = useDisk ? null : rawOff.clone();

        return new MainBackupGuiSnapshot(expectedTarget, expectedLogType, expectedTimestamp,
                main, armor, useDisk, offClone);
    }

    private static ItemStack cloneOrNull(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        return item.clone();
    }
}
