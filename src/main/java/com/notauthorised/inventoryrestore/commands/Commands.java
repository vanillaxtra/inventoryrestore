package com.notauthorised.inventoryrestore.commands;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.commands.subcommands.*;
import com.notauthorised.inventoryrestore.config.MessageData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class Commands implements CommandExecutor, TabCompleter {

    private InventoryRestore main;

    private String[] defaultOptions = new String[] {"restore", "refund", "forcebackup", "enable", "disable", "reload", "version", "import", "help"};
    private String[] backupOptions = new String[] {"all", "player"};
    private String[] importOptions = new String[] {"confirm"};

    private HashMap<String, IRPCommand> subCommands = new HashMap<>();

    public Commands(InventoryRestore mainIn) {
        this.main = mainIn;
        this.subCommands.put("restore", new RestoreSubCmd(mainIn));
        this.subCommands.put("refund", new RefundSubCmd(mainIn));
        this.subCommands.put("enable", new EnableSubCmd(mainIn));
        this.subCommands.put("disable", new DisableSubCmd(mainIn));
        this.subCommands.put("reload", new ReloadSubCmd(mainIn));
        this.subCommands.put("version", new VersionSubCmd(mainIn));
        this.subCommands.put("forcebackup", new ForceBackupSubCmd(mainIn));
        this.subCommands.put("import", new ImportSubCmd(mainIn));
        this.subCommands.put("help", new HelpSubCmd(mainIn));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("ir") ||
                label.equalsIgnoreCase("inventoryrestore")
        ) {
            if (args.length == 0) {
                ((HelpSubCmd) this.subCommands.get("help")).sendHelp(sender);
                return true;
            }
            IRPCommand irpCmd = this.subCommands.get(args[0]);
            if (irpCmd != null) {
                irpCmd.onCommand(sender, cmd, label, args);
                return true;
            }
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getError());
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String name, String[] args) {
        if (args.length == 1) {
            ArrayList<String> suggestions = new ArrayList<>();
            for (String option : this.defaultOptions) {
                if (option.startsWith(args[0].toLowerCase()) && commandSender.hasPermission("inventoryrestore." + option))
                    suggestions.add(option);
            }
            return suggestions;
        } else if (args.length == 2) {
            String[] opts;

            if ((args[0].equalsIgnoreCase("forcebackup") ||
                    args[0].equalsIgnoreCase("forcesave")) &&
                    commandSender.hasPermission("inventoryrestore.forcebackup")
            ) {
                opts = this.backupOptions;

            } else if (args[0].equalsIgnoreCase("import") &&
                    (ImportSubCmd.shouldShowConfirmOption() || args[1].toLowerCase().startsWith("c")) &&
                    commandSender.hasPermission("inventoryrestore.import")
            ) {
                opts = this.importOptions;

            } else if ((args[0].equalsIgnoreCase("restore") && commandSender.hasPermission("inventoryrestore.viewbackups"))
                    || (args[0].equalsIgnoreCase("refund") && commandSender.hasPermission("inventoryrestore.refund"))) {
                return RestoreRefundAliasCommands.matchOfflineNames(args[1]);

            } else {
                opts = null;
            }

            if (opts == null) return null;

            ArrayList<String> suggestions = new ArrayList<>();
            for (String option : opts) {
                if (option.startsWith(args[1].toLowerCase()))
                    suggestions.add(option);
            }
            return suggestions;
        }
        return null;
    }
}
