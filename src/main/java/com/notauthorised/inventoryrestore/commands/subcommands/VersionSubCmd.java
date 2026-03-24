package com.notauthorised.inventoryrestore.commands.subcommands;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.commands.IRPCommand;
import com.notauthorised.inventoryrestore.InventoryRollback;
import com.notauthorised.inventoryrestore.config.MessageData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class VersionSubCmd extends IRPCommand {

    public VersionSubCmd(InventoryRestore mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        StringBuilder strb = new StringBuilder(MessageData.getPluginPrefix());
        boolean hasVersionPerm = sender.hasPermission("inventoryrestore.version");

        strb.append("\n")
            .append(ChatColor.WHITE)
            .append("Plugin:").append("\n")
            .append(ChatColor.GRAY)
            .append("  Running InventoryRestore");
        // Can see version?
        if (hasVersionPerm) strb.append(" v").append(InventoryRollback.getPluginVersion());
        strb.append("\n");
        // Else show warning
        if (!hasVersionPerm)
            strb.append(ChatColor.GRAY)
                .append("  (Version not visible, lacking permission)")
                .append("\n");

        strb.append(ChatColor.WHITE)
            .append("Author:").append("\n")
            .append(ChatColor.GRAY)
            .append("  notauthorised").append("\n")
            .append("\n")
            .append(ChatColor.WHITE).append("Update link:").append("\n")
            .append(ChatColor.BLUE).append(ChatColor.ITALIC).append("  https://www.spigotmc.org/resources/inventoryrestore.85811/");


        // Send
        sender.sendMessage(strb.toString());
    }

}
