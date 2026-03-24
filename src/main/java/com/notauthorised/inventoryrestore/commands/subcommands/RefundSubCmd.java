package com.notauthorised.inventoryrestore.commands.subcommands;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.commands.IRPCommand;
import com.notauthorised.inventoryrestore.InventoryRollback;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.data.RestoreSession;
import com.notauthorised.inventoryrestore.gui.menu.MainMenu;
import com.notauthorised.inventoryrestore.gui.menu.PlayerMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RefundSubCmd extends IRPCommand {

    public RefundSubCmd(InventoryRestore mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPlayerOnlyError());
            return;
        }
        if (!sender.hasPermission("inventoryrestore.refund")) {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
            return;
        }
        if (!ConfigData.isEnabled()) {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPluginDisabled());
            return;
        }
        Player staff = (Player) sender;
        openRefundMenu(sender, staff, args);
    }

    @SuppressWarnings("deprecation")
    private void openRefundMenu(CommandSender sender, Player staff, String[] args) {
        if (args.length <= 1) {
            RestoreSession.setRefundContext(staff.getUniqueId(), true);
            MainMenu menu = new MainMenu(staff, 1, false);
            staff.openInventory(menu.getInventory());
            Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getMainMenu);
            return;
        }

        String nameOrUuid = args[1];
        OfflinePlayer target = resolveTarget(sender, nameOrUuid);
        if (target == null) return;

        RestoreSession.setRefundContext(staff.getUniqueId(), true);
        PlayerMenu menu = new PlayerMenu(staff, target, true);
        staff.openInventory(menu.getInventory());
        Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getPlayerMenu);
    }

    private OfflinePlayer resolveTarget(CommandSender sender, String uuidStr) {
        if (uuidStr.length() == 36 || uuidStr.length() == 32) {
            String fixed = uuidStr;
            if (uuidStr.length() == 32) {
                fixed = uuidStr.substring(0, 8) + "-" + uuidStr.substring(8, 12) + "-"
                        + uuidStr.substring(12, 16) + "-" + uuidStr.substring(16, 20) + "-" + uuidStr.substring(20);
            }
            try {
                return Bukkit.getOfflinePlayer(UUID.fromString(fixed));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getError());
                return null;
            }
        }
        return Bukkit.getOfflinePlayer(uuidStr);
    }
}
