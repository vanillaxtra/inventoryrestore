package com.notauthorised.inventoryrestore.data;

import com.notauthorised.inventoryrestore.InventoryRollback;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.inventory.RestoreInventory;
import com.notauthorised.inventoryrestore.util.serialization.ItemStackSerialization;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Deferred full restores for offline players. Stored under the plugin data folder so pending
 * restores survive restarts; the pending file is removed only after a successful apply.
 */
public class OfflineRestoreManager {

    private static final String FOLDER = "pending_restores";

    private static File primaryDir() {
        File dir = new File(InventoryRollback.getInstance().getDataFolder(), FOLDER);
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return dir;
    }

    private static File primaryFile(UUID uuid) {
        return new File(primaryDir(), uuid + ".yml");
    }

    private static File legacyFile(UUID uuid) {
        return new File(new File(ConfigData.getFolderLocation(), FOLDER), uuid + ".yml");
    }

    /**
     * Pending files live in plugins/.../pending_restores/ (survives restarts). If an older build
     * left a file under the configured backup folder, it is moved here on first access.
     */
    private static File resolvePendingFile(UUID uuid) {
        File p = primaryFile(uuid);
        if (p.exists()) return p;
        File leg = legacyFile(uuid);
        if (!leg.exists()) return p;
        try {
            Files.move(leg.toPath(), p.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            InventoryRollback.getInstance().getLogger().log(Level.WARNING,
                    "Could not move pending restore to plugin folder, using legacy path: " + e.getMessage());
            return leg;
        }
        return p;
    }

    public static void scheduleRestore(OfflinePlayer target, LogType logType, Long timestamp) {
        scheduleRestore(target, logType, timestamp, null, null, null);
    }

    /**
     * @param guiMain36 main inventory from the staff backup GUI (may be edited); null = use backup file on join
     * @param guiArmor4 four armor slots (boots..helmet); null = use backup file
     * @param guiOffhand resolved offhand; null treated as empty when using GUI snapshot
     */
    public static void scheduleRestore(OfflinePlayer target, LogType logType, Long timestamp,
                                       ItemStack[] guiMain36, ItemStack[] guiArmor4, ItemStack guiOffhand) {
        File f = primaryFile(target.getUniqueId());
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("logType", logType.name());
        cfg.set("timestamp", timestamp);
        if (guiMain36 != null && guiArmor4 != null) {
            cfg.set("useGuiSnapshot", true);
            cfg.set("guiInv", ItemStackSerialization.serialize(guiMain36));
            cfg.set("guiArmor", ItemStackSerialization.serialize(guiArmor4));
            cfg.set("guiOffhand", ItemStackSerialization.serialize(new ItemStack[]{
                    guiOffhand != null && !guiOffhand.getType().isAir() ? guiOffhand : null
            }));
        }
        try {
            cfg.save(f);
        } catch (IOException e) {
            InventoryRollback.getInstance().getLogger().log(Level.SEVERE, "Could not save pending offline restore", e);
        }
    }

    public static boolean hasPendingRestore(UUID uuid) {
        File p = primaryFile(uuid);
        if (p.exists()) return true;
        return legacyFile(uuid).exists();
    }

    /**
     * Apply pending restore and delete the file only on full success. Returns false if missing or failed
     * (file is kept so it can be retried after e.g. a bad MySQL moment or restart).
     */
    public static boolean applyPendingRestore(Player player) {
        File f = resolvePendingFile(player.getUniqueId());
        if (!f.exists()) return false;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        try {
            LogType logType = LogType.valueOf(cfg.getString("logType"));
            Long timestamp = cfg.getLong("timestamp");

            PlayerData data = new PlayerData(player, logType, timestamp);
            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                data.getAllBackupData().get();
            }

            ItemStack[] inv;
            ItemStack[] armour;
            ItemStack offhand;
            String ver = InventoryRollback.getPackageVersion();
            if (cfg.getBoolean("useGuiSnapshot", false)) {
                inv = ItemStackSerialization.deserializeData(ver, cfg.getString("guiInv")).getItems();
                armour = ItemStackSerialization.deserializeData(ver, cfg.getString("guiArmor")).getItems();
                ItemStack[] offArr = ItemStackSerialization.deserializeData(ver, cfg.getString("guiOffhand")).getItems();
                offhand = (offArr != null && offArr.length > 0) ? offArr[0] : null;
                if (inv == null) inv = data.getMainInventory();
                if (armour == null) armour = data.getArmour();
                if (offhand == null) offhand = data.getOffhand();
            } else {
                inv = data.getMainInventory();
                armour = data.getArmour();
                offhand = data.getOffhand();
            }
            if (inv != null) player.getInventory().setContents(inv);
            if (cfg.getBoolean("useGuiSnapshot", false)) {
                player.getInventory().setArmorContents(java.util.Arrays.copyOf(
                        armour != null ? armour : new ItemStack[0], 4));
            } else if (armour != null && armour.length > 0) {
                player.getInventory().setArmorContents(java.util.Arrays.copyOf(armour, 4));
            }
            player.getInventory().setItemInOffHand(offhand != null && !offhand.getType().isAir() ? offhand : new ItemStack(Material.AIR));
            ItemStack[] ender = data.getEnderChest();
            if (ender != null) player.getEnderChest().setContents(ender);
            player.setFoodLevel(data.getFoodLevel());
            player.setSaturation(data.getSaturation());
            RestoreInventory.setTotalExperience(player, data.getXP());

            if (!f.delete()) {
                InventoryRollback.getInstance().getLogger().warning("Pending restore applied but could not delete: " + f.getAbsolutePath());
            }
            return true;
        } catch (Exception e) {
            InventoryRollback.getInstance().getLogger().log(Level.WARNING,
                    "Pending restore failed for " + player.getName() + " — will retry on next login if file remains", e);
            return false;
        }
    }
}
