package com.notauthorised.inventoryrestore.commands.subcommands;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.commands.IRPCommand;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.config.MessageData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadSubCmd extends IRPCommand {

    public ReloadSubCmd(InventoryRestore mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("inventoryrestore.reload")) {
            ConfigData config = main.getConfigData();
            config.generateConfigFile();
            config.setVariables();
            main.startupTasks();

            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPluginReload());
        } else {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
        }
    }

}
