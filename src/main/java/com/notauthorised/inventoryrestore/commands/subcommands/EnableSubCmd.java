package com.notauthorised.inventoryrestore.commands.subcommands;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.commands.IRPCommand;
import com.notauthorised.inventoryrestore.config.MessageData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EnableSubCmd extends IRPCommand {

    public EnableSubCmd(InventoryRestore mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("inventoryrestore.enable")) {
            main.getConfigData().setEnabled(true);
            main.getConfigData().saveConfig();

            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPluginEnabled());
        } else {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
        }
        return;
    }

}
