package com.notauthorised.inventoryrestore;

import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.YAML;
import com.notauthorised.inventoryrestore.inventory.SaveInventory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AutoSaveManager {

    private final InventoryRestore plugin;
    private BukkitTask task;

    public AutoSaveManager(InventoryRestore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) return;

        int intervalTicks = Math.max(20, ConfigData.getAutosaveIntervalSeconds() * 20);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.isShuttingDown()) {
                    cancel();
                    return;
                }
                // Crash recovery: snapshot every online player (not gated on leavesave — that perm is for join/quit backups only).
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    SaveInventory saveInv = new SaveInventory(player, LogType.QUIT, null, null);
                    SaveInventory.PlayerDataSnapshot snapshot = saveInv.createSnapshot(player.getInventory(), player.getEnderChest());
                    if (snapshot != null) {
                        java.util.UUID uuid = player.getUniqueId();
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> YAML.saveAutosave(uuid, snapshot));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
