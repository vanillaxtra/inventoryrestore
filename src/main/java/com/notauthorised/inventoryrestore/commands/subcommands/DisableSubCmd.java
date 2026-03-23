package com.notauthorised.inventoryrestore.commands.subcommands;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.commands.IRPCommand;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.config.MessageData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DisableSubCmd extends IRPCommand {

    public DisableSubCmd(InventoryRestore mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("inventoryrestore.disable")) {
            ConfigData.setEnabled(false);
            main.getConfigData().saveConfig();

            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPluginDisabled());
        } else {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
        }
    }

}
