package com.notauthorised.inventoryrestore;

import com.notauthorised.inventoryrestore.UpdateChecker.UpdateResult;
import com.notauthorised.inventoryrestore.commands.Commands;
import com.notauthorised.inventoryrestore.commands.RestoreRefundAliasCommands;
import com.notauthorised.inventoryrestore.data.RestoreStatsManager;
import com.notauthorised.inventoryrestore.util.TimeZoneUtil;
import com.notauthorised.inventoryrestore.util.test.SelfTestSerialization;
import com.tcoded.lightlibs.bukkitversion.BukkitVersion;
import com.tcoded.lightlibs.bukkitversion.MCVersion;
import io.papermc.lib.PaperLib;
import com.notauthorised.inventoryrestore.InventoryRollback;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.data.LogType;
import com.notauthorised.inventoryrestore.data.YAML;
import com.notauthorised.inventoryrestore.inventory.SaveInventory;
import com.notauthorised.inventoryrestore.listeners.ClickGUI;
import com.notauthorised.inventoryrestore.listeners.EventLogs;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class InventoryRestore extends InventoryRollback {

    private static InventoryRestore instance;

    private TimeZoneUtil timeZoneUtil = null;
    private AutoSaveManager autoSaveManager = null;

    private ConfigData configData;
    private BukkitVersion version = BukkitVersion.v1_13_R1;

    private AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public static InventoryRestore getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        InventoryRollback.setInstance(instance);

        // Load Utils
        this.timeZoneUtil = new TimeZoneUtil();

        // Load Config
        configData = new ConfigData();
        configData.setVariables(); // requires TimeZoneUtil

        RestoreStatsManager.init();

        // Init NMS
        String serverVersion = this.getServer().getVersion();
        getLogger().info("Attempting support for version: " + serverVersion);
        MCVersion mcVersion = MCVersion.fromServerVersion(serverVersion);
        BukkitVersion nmsVersion = mcVersion.toBukkitVersion();
        if (nmsVersion == null) {
            getLogger().severe(MessageData.getPluginPrefix() + "\n" +
                    " ** WARNING! InventoryRestore may not be compatible with this version of Minecraft. **\n" +
                    " ** Please fully test the plugin before using on your server as features may be broken. **\n" +
                    MessageData.getPluginPrefix()
            );
            setPackageVersion(BukkitVersion.getLatest().name());
        } else {
            setVersion(nmsVersion);
            InventoryRollback.setPackageVersion(nmsVersion.name());
        }
        getLogger().info("Using CraftBukkit version: " + getPackageVersion());

        // Storage Init & Update checker
        super.startupTasks();

        // Ensure autosave/crash folders exist (even when using MySQL)
        YAML.ensureAutosaveFoldersExist();

        // Crash detection: if autosaves exist, server crashed - move to crashes
        YAML.processCrashRecovery();

        // Autosave scheduler
        if (ConfigData.isAutosaveEnabled()) {
            autoSaveManager = new AutoSaveManager(this);
            autoSaveManager.start();
        }

        // bStats
        if (ConfigData.isbStatsEnabled()) initBStats();

        // Commands
        Commands cmds = new Commands(this);
        PluginCommand plCmd = getCommand("inventoryrestore");
        if (plCmd != null) {
            plCmd.setExecutor(cmds);
            plCmd.setTabCompleter(cmds);
        }
        RestoreRefundAliasCommands.RestoreAlias restoreAlias = new RestoreRefundAliasCommands.RestoreAlias();
        PluginCommand restoreCmd = getCommand("restore");
        if (restoreCmd != null) {
            restoreCmd.setExecutor(restoreAlias);
            restoreCmd.setTabCompleter(restoreAlias);
        }
        RestoreRefundAliasCommands.RefundAlias refundAlias = new RestoreRefundAliasCommands.RefundAlias();
        PluginCommand refundCmd = getCommand("refund");
        if (refundCmd != null) {
            refundCmd.setExecutor(refundAlias);
            refundCmd.setTabCompleter(refundAlias);
        }

        // Events
        getServer().getPluginManager().registerEvents(new ClickGUI(), this);
        getServer().getPluginManager().registerEvents(new EventLogs(), this);
        // Run after all plugin enable
        getServer().getScheduler().runTask(this, EventLogs::patchLowestHandlers);

        // PaperLib
        if (!PaperLib.isPaper()) {
            this.getLogger().info("----------------------------------------");
            this.getLogger().info("We recommend updating your server to use Paper :)");
            this.getLogger().info("Paper significantly reduces lag spikes among other benefits.");
            this.getLogger().info("Learn more at: https://papermc.io/");
            this.getLogger().info("----------------------------------------");
        }

        // Run self-tests
        SelfTestSerialization.runTests();
    }

    @Override
    public void onDisable() {
        // Signal to the plugin that new tasks cannot be scheduled
        getLogger().info("Setting shutdown state");
        shuttingDown.set(true);

        // Stop autosave
        if (autoSaveManager != null) {
            autoSaveManager.stop();
            autoSaveManager = null;
        }

        // Save all inventories
        getLogger().info("Saving player inventories...");
        for (Player player : this.getServer().getOnlinePlayers()) {
            if (player.hasPermission("inventoryrestore.leavesave")) {
                new SaveInventory(player, LogType.QUIT, null, null)
                        .snapshotAndSave(player.getInventory(), player.getEnderChest(), false);
            }
        }
        getLogger().info("Done saving player inventories!");

        // Delete autosaves on planned shutdown
        YAML.deleteAutosaveFolder();

        // Unregister event listeners
        HandlerList.unregisterAll(this);

        // Cancel tasks
        this.getServer().getScheduler().cancelTasks(this);

        // Clear instance references
        instance = null;
        super.onDisable();

        getLogger().info("Plugin is disabled!");
    }

    public void setVersion(BukkitVersion versionName) {
        version = versionName;
    }

    public boolean isCompatibleCb(String cbVersion) {
        for (BukkitVersion v : BukkitVersion.values()) {
            if (v.name().equalsIgnoreCase(cbVersion)) {
                this.setVersion(v);
                return true;
            }
        }

        return false;
    }

    public void checkUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), () -> {
            InventoryRestore.getInstance().getConsoleSender().sendMessage(MessageData.getPluginPrefix() + "Checking for updates...");

            final UpdateChecker.UpdateResult result = new UpdateChecker(getInstance(), 85811).getResult();

            switch (result) {
                case FAIL_SPIGOT:
                    getConsoleSender().sendMessage(MessageData.getPluginPrefix() + ChatColor.GOLD + "Warning: Could not contact Spigot to check if an update is available.");
                    break;
                case UPDATE_AVAILABLE:
                    getConsoleSender().sendMessage(ChatColor.AQUA + "===============================================================================");
                    getConsoleSender().sendMessage(ChatColor.AQUA + "An update to InventoryRestore is available!");
                    getConsoleSender().sendMessage(ChatColor.AQUA + "Download at https://www.spigotmc.org/resources/inventoryrestore.85811/");
                    getConsoleSender().sendMessage(ChatColor.AQUA + "===============================================================================");
                    break;
                case NO_UPDATE:
                    getConsoleSender().sendMessage(MessageData.getPluginPrefix() + ChatColor.RESET + "You are running the latest version.");
                    break;
            }
        });
    }

    public void initBStats() {
        bStats();
    }

    @Override
    public void bStats() {
        Metrics metrics = new Metrics(this,  	9437);

        if (ConfigData.isbStatsEnabled())
            InventoryRestore.getInstance().getConsoleSender().sendMessage(MessageData.getPluginPrefix() + "bStats are enabled");

        metrics.addCustomChart(new SimplePie("database_type", () -> ConfigData.getSaveType().getName()));

        metrics.addCustomChart(new SimplePie("restore_to_player_enabled", () -> {
            if (ConfigData.isRestoreToPlayerButton()) {
                return "Enabled";
            } else {
                return "Disabled";
            }
        }));

        metrics.addCustomChart(new SimplePie("save_location", () -> {
            if (ConfigData.getFolderLocation() == InventoryRollback.getInstance().getDataFolder()) {
                return "Default";
            } else {
                return "Not Default";
            }
        }));

        metrics.addCustomChart(new SimplePie("storage_type", () -> {
            if (ConfigData.isMySQLEnabled()) {
                return "MySQL";
            } else {
                return "YAML";
            }
        }));

        metrics.addCustomChart(new SimplePie("time_zone", () -> {
            return ConfigData.getTimeZone().getID();
        }));

        metrics.addCustomChart(new SimplePie("allow_other_plugins_edit_death_inventory", () -> {
            return String.valueOf(ConfigData.isAllowOtherPluginEditDeathInventory());
        }));

        metrics.addCustomChart(new SimplePie("custom_online_mode", () -> {
            boolean vanillaOnline = this.getServer().getOnlineMode();
            boolean spigotProxyMode = false;

            boolean legacyPaperProxyEnabled = false;
            boolean legacyPaperProxyMode = false;

            boolean modernPaperProxyEnabled = false;
            boolean modernPaperProxyMode = false;

            File mainFolder = new File(System.getProperty("user.dir"));

            File spigotConfig = new File(mainFolder, "spigot.yml");
            if (spigotConfig.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(spigotConfig);
                spigotProxyMode = config.getBoolean("settings.bungeecord", false);
            }

            File legacyPaperConfig = new File(mainFolder, "paper.yml");
            if (legacyPaperConfig.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(legacyPaperConfig);
                legacyPaperProxyEnabled = config.getBoolean("settings.velocity-support.enabled", false);
                legacyPaperProxyMode = config.getBoolean("settings.velocity-support.online-mode", false);
            }

            File modernPaperConfig = new File(mainFolder, "config/paper-global.yml");
            if (modernPaperConfig.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(modernPaperConfig);
                modernPaperProxyEnabled = config.getBoolean("proxies.velocity.enabled", false);
                modernPaperProxyMode = config.getBoolean("proxies.velocity.online-mode", false);
            }

            if (modernPaperProxyEnabled) {
                if (modernPaperProxyMode) return "Modern Paper Proxy - Online";
                return "Modern Paper Proxy - Offline";
            }

            if (legacyPaperProxyEnabled) {
                if (legacyPaperProxyMode) return "Legacy Paper Proxy - Online";
                return "Legacy Paper Proxy - Offline";
            }

            if (spigotProxyMode) {
                if (vanillaOnline) return "Bungeecord - Online";
                return "Bungeecord - Offline";
            }

            if (vanillaOnline) return "Vanilla - Online";
            return "Vanilla - Offline";
        }));
    }

    // GETTERS

    public boolean isShuttingDown() {
        return shuttingDown.get();
    }

    public BukkitVersion getVersion() {
        return version;
    }

    public ConsoleCommandSender getConsoleSender() {
        return this.getServer().getConsoleSender();
    }

    public TimeZoneUtil getTimeZoneUtil() {
        return this.timeZoneUtil;
    }

    public ConfigData getConfigData() {
        return configData;
    }
}
