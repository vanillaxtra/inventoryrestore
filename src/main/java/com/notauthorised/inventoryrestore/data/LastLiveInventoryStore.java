package com.notauthorised.inventoryrestore.data;

import com.notauthorised.inventoryrestore.InventoryRollback;
import com.notauthorised.inventoryrestore.util.serialization.ItemStackSerialization;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Saves main inventory (36), armor, and offhand when a player quits so staff can preview it while they are offline.
 */
public final class LastLiveInventoryStore {

    private static final String FOLDER = "last-live-inventory";

    private LastLiveInventoryStore() {}

    private static File file(UUID uuid) {
        File dir = new File(InventoryRollback.getInstance().getDataFolder(), FOLDER);
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return new File(dir, uuid + ".yml");
    }

    public static void save(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack off = player.getInventory().getItemInOffHand();

        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("savedAt", System.currentTimeMillis());
        cfg.set("inv", ItemStackSerialization.serialize(contents));
        cfg.set("armor", ItemStackSerialization.serialize(armor));
        cfg.set("offhand", ItemStackSerialization.serialize(new ItemStack[]{
                off != null && !off.getType().isAir() ? off : null
        }));
        try {
            cfg.save(file(player.getUniqueId()));
        } catch (IOException e) {
            InventoryRollback.getInstance().getLogger().log(Level.WARNING, "Could not save last-live inventory", e);
        }
    }

    public static Loaded load(UUID uuid) {
        File f = file(uuid);
        if (!f.exists()) return null;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        String invB64 = cfg.getString("inv");
        if (invB64 == null) return null;
        String ver = InventoryRollback.getPackageVersion();
        ItemStack[] inv = ItemStackSerialization.deserializeData(ver, invB64).getItems();
        String armorB64 = cfg.getString("armor");
        ItemStack[] armor = armorB64 != null
                ? ItemStackSerialization.deserializeData(ver, armorB64).getItems()
                : null;
        ItemStack offhand = null;
        String offB64 = cfg.getString("offhand");
        if (offB64 != null) {
            ItemStack[] o = ItemStackSerialization.deserializeData(ver, offB64).getItems();
            if (o != null && o.length > 0) offhand = o[0];
        }
        return new Loaded(inv, armor, offhand, cfg.getLong("savedAt"));
    }

    public static final class Loaded {
        public final ItemStack[] contents36;
        public final ItemStack[] armor;
        public final ItemStack offhand;
        public final long savedAt;

        public Loaded(ItemStack[] contents36, ItemStack[] armor, ItemStack offhand, long savedAt) {
            this.contents36 = contents36;
            this.armor = armor;
            this.offhand = offhand;
            this.savedAt = savedAt;
        }

        /** True when main, armor, and off-hand are all empty (used to skip overwrite warning). */
        public boolean isCompletelyEmpty() {
            if (contents36 != null) {
                for (ItemStack s : contents36) {
                    if (s != null && !s.getType().isAir()) return false;
                }
            }
            if (armor != null) {
                for (ItemStack s : armor) {
                    if (s != null && !s.getType().isAir()) return false;
                }
            }
            return offhand == null || offhand.getType().isAir();
        }
    }
}
