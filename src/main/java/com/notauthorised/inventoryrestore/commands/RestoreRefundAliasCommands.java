package com.notauthorised.inventoryrestore.commands;

import com.notauthorised.inventoryrestore.InventoryRollback;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.data.RestoreSession;
import com.notauthorised.inventoryrestore.gui.menu.MainMenu;
import com.notauthorised.inventoryrestore.gui.menu.PlayerMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * /restore [player] and /refund [player] shortcuts.
 */
public class RestoreRefundAliasCommands {

    public static final class RestoreAlias implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPlayerOnlyError());
                return true;
            }
            if (!sender.hasPermission("inventoryrestore.viewbackups")) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                return true;
            }
            if (!ConfigData.isEnabled()) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPluginDisabled());
                return true;
            }
            Player staff = (Player) sender;
            if (args.length == 0) {
                RestoreSession.clear(staff.getUniqueId());
                MainMenu menu = new MainMenu(staff, 1);
                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getMainMenu);
                return true;
            }
            OfflinePlayer target = resolve(staff, args[0]);
            if (target == null) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getError());
                return true;
            }
            RestoreSession.setRefundContext(staff.getUniqueId(), false);
            PlayerMenu menu = new PlayerMenu(staff, target, false);
            staff.openInventory(menu.getInventory());
            Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getPlayerMenu);
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length != 1 || !sender.hasPermission("inventoryrestore.viewbackups")) {
                return List.of();
            }
            return matchOfflineNames(args[0]);
        }
    }

    public static final class RefundAlias implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPlayerOnlyError());
                return true;
            }
            if (!sender.hasPermission("inventoryrestore.refund")) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                return true;
            }
            if (!ConfigData.isEnabled()) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPluginDisabled());
                return true;
            }
            Player staff = (Player) sender;
            if (args.length == 0) {
                RestoreSession.setRefundContext(staff.getUniqueId(), true);
                MainMenu menu = new MainMenu(staff, 1, false);
                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getMainMenu);
                return true;
            }
            OfflinePlayer target = resolve(staff, args[0]);
            if (target == null) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getError());
                return true;
            }
            RestoreSession.setRefundContext(staff.getUniqueId(), true);
            PlayerMenu menu = new PlayerMenu(staff, target, true);
            staff.openInventory(menu.getInventory());
            Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getPlayerMenu);
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length != 1 || !sender.hasPermission("inventoryrestore.refund")) {
                return List.of();
            }
            return matchOfflineNames(args[0]);
        }
    }

    private static OfflinePlayer resolve(CommandSender sender, String token) {
        if (token.length() == 36 || token.length() == 32) {
            String fixed = token;
            if (token.length() == 32) {
                fixed = token.substring(0, 8) + "-" + token.substring(8, 12) + "-"
                        + token.substring(12, 16) + "-" + token.substring(16, 20) + "-" + token.substring(20);
            }
            try {
                return Bukkit.getOfflinePlayer(UUID.fromString(fixed));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return Bukkit.getOfflinePlayer(token);
    }

    /** Tab-complete helper for /ir restore|refund & shortcut commands. */
    public static List<String> matchOfflineNames(String prefix) {
        String p = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            String name = op.getName();
            if (name == null) continue;
            if (name.toLowerCase(Locale.ROOT).startsWith(p)) {
                out.add(name);
            }
            if (out.size() >= 48) break;
        }
        return out;
    }
}
