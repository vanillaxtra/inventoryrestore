package com.notauthorised.inventoryrestore.util;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.tcoded.lightlibs.bukkitversion.BukkitVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Export backup inventory to shulkers, barrels, chests, bundles, or dropped items.
 */
public final class ExportStorageHelper {

    public enum Mode {
        SHULKER,
        BARREL,
        CHEST,
        BUNDLE,
        DROP
    }

    private ExportStorageHelper() {}

    public static void giveExport(Player staff, Mode mode, ItemStack[] mainInventory, ItemStack[] armour, ItemStack offhand) {
        if (mainInventory == null) mainInventory = new ItemStack[0];
        ItemStack[] extraItems = armour;
        if (extraItems == null || extraItems.length == 0) {
            int extraLen = Math.max(0, mainInventory.length - 36);
            extraItems = new ItemStack[extraLen];
            if (extraLen > 0) System.arraycopy(mainInventory, 36, extraItems, 0, extraLen);
        }

        ItemStack[] hotBar = new ItemStack[9];
        for (int i = 0; i < Math.min(9, mainInventory.length); i++) {
            hotBar[i] = mainInventory[i];
        }

        ItemStack[] invContents;
        if (mainInventory.length > 9) {
            int end = Math.min(mainInventory.length, 45);
            invContents = java.util.Arrays.copyOfRange(mainInventory, 9, end);
        } else {
            invContents = new ItemStack[0];
        }

        if (mode == Mode.DROP) {
            dropAll(staff, hotBar, extraItems, invContents, offhand);
            return;
        }

        if (mode == Mode.BUNDLE) {
            giveBundles(staff, hotBar, extraItems, invContents, offhand);
            return;
        }

        Material containerMat = mode == Mode.SHULKER ? Material.SHULKER_BOX
                : mode == Mode.BARREL ? Material.BARREL
                : Material.CHEST;

        ItemStack[] first = new ItemStack[27];
        ItemStack[] second = new ItemStack[27];
        System.arraycopy(hotBar, 0, first, 0, Math.min(hotBar.length, 9));
        System.arraycopy(extraItems, 0, first, 9, Math.min(extraItems.length, 18));
        System.arraycopy(invContents, 0, second, 0, Math.min(invContents.length, 27));

        ItemStack a = createFilledContainer(containerMat, first);
        ItemStack b = createFilledContainer(containerMat, second);
        if (a != null) staff.getInventory().addItem(a);
        if (b != null) staff.getInventory().addItem(b);
    }

    private static void dropAll(Player staff, ItemStack[] hotBar, ItemStack[] extra, ItemStack[] inv, ItemStack offhand) {
        World w = staff.getWorld();
        Location loc = staff.getLocation().add(0, 0.2, 0);
        for (ItemStack s : hotBar) dropOne(w, loc, s);
        for (ItemStack s : extra) dropOne(w, loc, s);
        for (ItemStack s : inv) dropOne(w, loc, s);
        dropOne(w, loc, offhand);
    }

    private static void dropOne(World w, Location loc, ItemStack s) {
        if (s == null || s.getType().isAir()) return;
        w.dropItemNaturally(loc, s.clone());
    }

    private static void giveBundles(Player staff, ItemStack[] hotBar, ItemStack[] extra, ItemStack[] inv, ItemStack offhand) {
        List<ItemStack> flat = new ArrayList<>();
        for (ItemStack s : hotBar) if (s != null && !s.getType().isAir()) flat.add(s.clone());
        for (ItemStack s : extra) if (s != null && !s.getType().isAir()) flat.add(s.clone());
        for (ItemStack s : inv) if (s != null && !s.getType().isAir()) flat.add(s.clone());
        if (offhand != null && !offhand.getType().isAir()) flat.add(offhand.clone());

        if (flat.isEmpty()) return;

        InventoryRestore plugin = InventoryRestore.getInstance();
        if (!plugin.getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) {
            World w = staff.getWorld();
            Location loc = staff.getLocation().add(0, 0.2, 0);
            for (ItemStack s : flat) w.dropItemNaturally(loc, s);
            return;
        }

        while (!flat.isEmpty()) {
            ItemStack bundleItem = new ItemStack(Material.BUNDLE);
            BundleMeta meta = (BundleMeta) bundleItem.getItemMeta();
            if (meta == null) {
                dropFlat(staff, flat);
                return;
            }
            boolean putAnything = false;
            while (!flat.isEmpty()) {
                ItemStack head = flat.get(0);
                int maxTry = Math.min(head.getAmount(), Math.max(1, head.getMaxStackSize()));
                boolean addedChunk = false;
                for (int n = maxTry; n >= 1; n--) {
                    int countBefore = bundleItemCount(meta);
                    ItemStack chunk = head.clone();
                    chunk.setAmount(n);
                    meta.addItem(chunk);
                    int delta = bundleItemCount(meta) - countBefore;
                    if (delta > 0) {
                        head.setAmount(head.getAmount() - delta);
                        addedChunk = true;
                        putAnything = true;
                        if (head.getAmount() <= 0) {
                            flat.remove(0);
                        }
                        break;
                    }
                }
                if (!addedChunk) {
                    break;
                }
            }
            if (!putAnything) {
                dropFlat(staff, flat);
                return;
            }
            bundleItem.setItemMeta(meta);
            staff.getInventory().addItem(bundleItem);
        }
    }

    private static int bundleItemCount(BundleMeta meta) {
        int t = 0;
        for (ItemStack x : meta.getItems()) {
            if (x != null) t += x.getAmount();
        }
        return t;
    }

    private static void dropFlat(Player staff, List<ItemStack> flat) {
        World w = staff.getWorld();
        Location loc = staff.getLocation().add(0, 0.2, 0);
        for (ItemStack s : flat) w.dropItemNaturally(loc, s);
        flat.clear();
    }

    private static ItemStack createFilledContainer(Material mat, ItemStack[] contents27) {
        ItemStack item = new ItemStack(mat);
        if (!(item.getItemMeta() instanceof BlockStateMeta)) return null;
        BlockStateMeta blockMeta = (BlockStateMeta) item.getItemMeta();

        if (mat == Material.SHULKER_BOX && blockMeta.getBlockState() instanceof ShulkerBox) {
            ShulkerBox box = (ShulkerBox) blockMeta.getBlockState();
            box.getInventory().setContents(contents27);
            blockMeta.setBlockState(box);
            item.setItemMeta(blockMeta);
            return item;
        }
        if (mat == Material.BARREL && blockMeta.getBlockState() instanceof Barrel) {
            Barrel barrel = (Barrel) blockMeta.getBlockState();
            barrel.getInventory().setContents(contents27);
            blockMeta.setBlockState(barrel);
            item.setItemMeta(blockMeta);
            return item;
        }
        if (mat == Material.CHEST && blockMeta.getBlockState() instanceof Chest) {
            Chest chest = (Chest) blockMeta.getBlockState();
            chest.getInventory().setContents(contents27);
            blockMeta.setBlockState(chest);
            item.setItemMeta(blockMeta);
            return item;
        }
        return null;
    }
}
