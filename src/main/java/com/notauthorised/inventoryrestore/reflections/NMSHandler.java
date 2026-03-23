package com.notauthorised.inventoryrestore.reflections;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.tcoded.lightlibs.bukkitversion.BukkitVersion;
import com.notauthorised.inventoryrestore.InventoryRollback;
import org.bukkit.Bukkit;

public class NMSHandler {

    public Class<?> getNMSClass(String name) {
        Class<?> c = null;

        try {
            if (InventoryRestore.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_17_R1)) {
                c = Class.forName("net.minecraft." + name);
            } else {
                c = Class.forName("net.minecraft.server." + InventoryRollback.getPackageVersion() + "." + name);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return c;
    }

    public Class<?> getCraftBukkitClass(String name) {
        Class<?> c = null;

        try {
            c = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return c;
    }
}