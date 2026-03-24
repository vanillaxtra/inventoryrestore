package com.notauthorised.inventoryrestore.commands.subcommands;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.commands.IRPCommand;
import com.notauthorised.inventoryrestore.config.MessageData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpSubCmd extends IRPCommand {

    public HelpSubCmd(InventoryRestore mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("inventoryrestore.help")) {
            this.sendHelp(sender);
        } else {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
        }
        return;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(
                MessageData.getPluginPrefix() + ChatColor.GRAY + "InventoryRestore - by notauthorised\n" +
                        ChatColor.WHITE + "  Available Commands:\n" +
                        ChatColor.WHITE + "    /ir restore [player]" + ChatColor.GRAY + " - Open rollback GUI (tab completes names)\n" +
                        ChatColor.WHITE + "    /restore [player]" + ChatColor.GRAY + " - Shortcut for restore GUI\n" +
                        ChatColor.WHITE + "    /ir refund [player]" + ChatColor.GRAY + " - Refund flow + ledger (webhook if configured)\n" +
                        ChatColor.WHITE + "    /refund [player]" + ChatColor.GRAY + " - Shortcut for refund GUI\n" +
                        ChatColor.WHITE + "    /ir forcebackup <all/player> [player]" + ChatColor.GRAY + " - Create a forced save of a player's inventory\n" +
                        ChatColor.WHITE + "    /ir enable" + ChatColor.GRAY + " - Enable the plugin\n" +
                        ChatColor.WHITE + "    /ir disable" + ChatColor.GRAY + " - Disable the plugin\n" +
                        ChatColor.WHITE + "    /ir reload" + ChatColor.GRAY + " - Reload the plugin\n" +
                        ChatColor.WHITE + "    /ir help" + ChatColor.GRAY + " - Get this message\n" +
                        ChatColor.WHITE + "    /ir version" + ChatColor.GRAY + " - Get plugin info & version\n");
    }

}
