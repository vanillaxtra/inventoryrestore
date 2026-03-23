package com.notauthorised.inventoryrestore.commands;

import com.notauthorised.inventoryrestore.InventoryRestore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class IRPCommand {

    public InventoryRestore main;

    public IRPCommand(InventoryRestore mainIn) {
        this.main = mainIn;
    }

    public abstract void onCommand(CommandSender sender, Command cmd, String label, String[] args);

}
