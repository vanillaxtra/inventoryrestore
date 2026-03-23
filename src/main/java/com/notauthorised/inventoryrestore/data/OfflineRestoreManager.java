package com.notauthorised.inventoryrestore.data;

import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.inventory.RestoreInventory;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Manages deferred restores for offline players.
 * When staff restores an offline player, we save the backup ref here.
 * On next login, the restore is applied.
 */
public class OfflineRestoreManager {

    private static final String FOLDER = "pending_restores";

    private static File getPendingFile(UUID uuid) {
        File dir = new File(ConfigData.getFolderLocation(), FOLDER);
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, uuid.toString() + ".yml");
    }

    public static void scheduleRestore(OfflinePlayer target, LogType logType, Long timestamp) {
        File f = getPendingFile(target.getUniqueId());
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("logType", logType.name());
        cfg.set("timestamp", timestamp);
        try {
            cfg.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasPendingRestore(UUID uuid) {
        return getPendingFile(uuid).exists();
    }

    /**
     * Apply pending restore and delete the file. Returns true if a restore was applied.
     */
    public static boolean applyPendingRestore(Player player) {
        File f = getPendingFile(player.getUniqueId());
        if (!f.exists()) return false;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        LogType logType = LogType.valueOf(cfg.getString("logType"));
        Long timestamp = cfg.getLong("timestamp");

        try {
            PlayerData data = new PlayerData(player, logType, timestamp);
            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                try {
                    data.getAllBackupData().get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    f.delete();
                    return false;
                }
            }

            // Apply inventory, ender chest, XP, hunger (no health)
            ItemStack[] inv = data.getMainInventory();
            ItemStack[] armour = data.getArmour();
            ItemStack offhand = data.getOffhand();
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            f.delete();
        }
        return true;
    }
}
