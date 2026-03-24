package com.notauthorised.inventoryrestore.config;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.InventoryRollback;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;

public class ConfigData {

    private File configurationFile;
    private FileConfiguration configuration;
    private static final String configurationFileName = "config.yml";

    public ConfigData() {
        generateConfigFile();
    }

    public void generateConfigFile() {
        getConfigurationFile();
        if(!configurationFile.exists()) {
            InventoryRollback.getInstance().saveResource(configurationFileName, false);
            getConfigurationFile();
        }
        getConfigData();
    }

    private void getConfigurationFile() {
        configurationFile = new File(InventoryRollback.getInstance().getDataFolder(), configurationFileName);
    }

    private void getConfigData() {
        configuration = YamlConfiguration.loadConfiguration(configurationFile);
    }

    public boolean saveConfig() {
        try {
            configuration.save(configurationFile);
        } catch (IOException e) {
            InventoryRollback.getInstance().getLogger().log(Level.SEVERE, "Could not save data to config file", e);
            return false;
        }

        saveChanges = false;

        return true;
    }

    public enum SaveType {
        YAML("YAML"),
        MYSQL("MySQL");
        
        private final String name;

        SaveType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return this.name;
        }
    }

    private static boolean pluginEnabled;

    private static SaveType saveType = SaveType.YAML;
    private static File folderLocation = InventoryRollback.getInstance().getDataFolder();   

    private static boolean mysqlEnabled;
    private static String mysqlHost;
    private static int mysqlPort;
    private static String mysqlDatabase;
    private static String mysqlTablePrefix;
    private static String mysqlUsername;
    private static String mysqlPassword;
    private static boolean mysqlUseSSL;
    private static boolean mysqlVerifyCertificate;
    private static boolean mysqlPubKeyRetrieval;
    private static int mysqlPoolMaximumPoolSize;
    private static int mysqlPoolMinimumIdle;
    private static int mysqlPoolConnectionTimeout;
    private static boolean mysqlCachePrepStmts;
    private static int mysqlPrepStmtCacheSize;
    private static int mysqlPrepStmtCacheSqlLimit;

    private static boolean allowOtherPluginEditDeathInventory;
    private static boolean restoreToPlayerButton;
    private static int backupLinesVisible;

    private static boolean saveEmptyInventories;

    private static boolean autosaveEnabled;
    private static int autosaveIntervalSeconds;

    private static int maxSavesJoin;
    private static int maxSavesQuit;
    private static int maxSavesDeath;
    private static int maxSavesWorldChange;
    private static int maxSavesForce;
    private static int maxSavesCrash;

    private static long timeZoneOffsetMillis;
    private static TimeZone timeZone;
    private static String timeZoneName;
    private static SimpleDateFormat timeFormat;

    private static boolean updateChecker;
    private static boolean bStatsEnabled;
    private static boolean debugEnabled;

    private static boolean refundWebhookEnabled;
    private static String refundWebhookUrl;

    /** Materials never written into backups (main, armour, offhand, ender). */
    private static Set<Material> backupIgnoredMaterials = Collections.emptySet();

    public void setVariables() {		
        setEnabled((boolean) getDefaultValue("enabled", true));

        String folder = (String) getDefaultValue("folder-location", "DEFAULT");
        if (folder.equalsIgnoreCase("DEFAULT") || folder.isEmpty()) {
            setFolderLocation(InventoryRollback.getInstance().getDataFolder());
        } else {
            try {
                setFolderLocation(new File(folder));
            } catch (NullPointerException e) {
                InventoryRollback.getInstance().getLogger().log(Level.WARNING, "Could not save set data folder to \"" + folder + "\". Setting to default location in plugin folder.", e);
                setFolderLocation(InventoryRollback.getInstance().getDataFolder());
            }
        }

        setMySQLEnabled((boolean) getDefaultValue("mysql.enabled", false));
        if (isMySQLEnabled())
            setSaveType(SaveType.MYSQL);
        else
            setSaveType(SaveType.YAML);

        setMySQLHost((String) getDefaultValue("mysql.details.host", "127.0.0.1"));
        setMySQLPort((int) getDefaultValue("mysql.details.port", 3306));
        setMySQLDatabase((String) getDefaultValue("mysql.details.database", "inventory_rollback"));
        setMySQLTablePrefix((String) getDefaultValue("mysql.details.table-prefix", "backup_"));
        setMySQLUsername((String) getDefaultValue("mysql.details.username", "username"));
        setMySQLPassword((String) getDefaultValue("mysql.details.password", "password"));
        setMySQLUseSSL((boolean) getDefaultValue("mysql.details.use-SSL", true));
        setMySQLVerifyCertificate((boolean) getDefaultValue("mysql.details.verifyCertificate", true));
        setMysqlPubKeyRetrievalAllowed((boolean) getDefaultValue("mysql.details.allowPubKeyRetrieval", false));
        setMySQLPoolMaximumPoolSize((int) getDefaultValue("mysql.pool.maximum-pool-size", 10));
        setMySQLPoolMinimumIdle((int) getDefaultValue("mysql.pool.minimum-idle", 2));
        setMySQLPoolConnectionTimeout((int) getDefaultValue("mysql.pool.connection-timeout", 30000));
        setMySQLCachePrepStmts((boolean) getDefaultValue("mysql.pool.cache-prep-stmts", true));
        setMySQLPrepStmtCacheSize((int) getDefaultValue("mysql.pool.prep-stmt-cache-size", 250));
        setMySQLPrepStmtCacheSqlLimit((int) getDefaultValue("mysql.pool.prep-stmt-cache-sql-limit", 2048));

        setAllowOtherPluginEditDeathInventory((boolean) getDefaultValue("allow-other-plugins-edit-death-inventory", false));
        setRestoreToPlayerButton((boolean) getDefaultValue("restore-to-player-button", true));
        setBackupLinesVisible((int) getDefaultValue("backup-lines-visible", 1));
        setSaveEmptyInventories((boolean) getDefaultValue("save-empty-inventories", true));

        setAutosaveEnabled((boolean) getDefaultValue("autosave-enabled", true));
        setAutosaveIntervalSeconds((int) getDefaultValue("autosave-interval-seconds", 30));

        setMaxSavesJoin((int) getDefaultValue("max-saves.join", 10));
        setMaxSavesQuit((int) getDefaultValue("max-saves.quit", 10));	
        setMaxSavesDeath((int) getDefaultValue("max-saves.death", 50));
        setMaxSavesWorldChange((int) getDefaultValue("max-saves.world-change", 10));	
        setMaxSavesForce((int) getDefaultValue("max-saves.force", 10));
        setMaxSavesCrash((int) getDefaultValue("max-saves.crash", 20));

        setTimeZone((String) getDefaultValue("time-zone", "GMT"));
        setTimeFormat((String) getDefaultValue("time-format", "dd/MM/yyyy HH:mm:ss a"));

        setUpdateChecker((boolean) getDefaultValue("update-checker", true));
        setbStatsEnabled((boolean) getDefaultValue("bStats", true));
        setDebugEnabled((boolean) getDefaultValue("debug", false));

        setRefundWebhookEnabled((boolean) getDefaultValue("refund-webhook.enabled", false));
        setRefundWebhookUrl((String) getDefaultValue("refund-webhook.url", ""));

        reloadBackupIgnoreMaterials();

        if (saveChanges())
            saveConfig();
    }

    private void reloadBackupIgnoreMaterials() {
        List<String> raw = configuration.getStringList("backup-ignore-materials");
        if (!configuration.contains("backup-ignore-materials")) {
            configuration.set("backup-ignore-materials", Collections.emptyList());
            saveChanges = true;
            raw = Collections.emptyList();
        }
        Set<Material> mats = new HashSet<>();
        for (String name : raw) {
            if (name == null || name.trim().isEmpty()) continue;
            try {
                mats.add(Material.valueOf(name.trim().toUpperCase(Locale.ROOT).replace(' ', '_')));
            } catch (IllegalArgumentException ex) {
                InventoryRollback.getInstance().getLogger().warning("[InventoryRestore] Unknown material in backup-ignore-materials: " + name);
            }
        }
        backupIgnoredMaterials = Collections.unmodifiableSet(mats);
    }

    public static boolean isBackupIgnoredMaterial(Material material) {
        if (material == null) return false;
        return backupIgnoredMaterials.contains(material);
    }

    public static void setEnabled(boolean enabled) {        
        pluginEnabled = enabled;
    }

    public static void setSaveType(SaveType value) {
        saveType = value;
    }

    public static void setFolderLocation(File folder) {
        folderLocation = folder;
    }

    public static void setMySQLEnabled(boolean value) {
        mysqlEnabled = value;
    }

    public static void setMySQLHost(String value) {
        mysqlHost = value;
    }

    public static void setMySQLPort(int value) {
        mysqlPort = value;
    }

    public static void setMySQLDatabase(String value) {
        mysqlDatabase = value;
    }

    public static void setMySQLTablePrefix(String value) {
        mysqlTablePrefix = value;
    }

    public static void setMySQLUsername(String value) {
        mysqlUsername = value;
    }

    public static void setMySQLPassword(String value) {
        mysqlPassword = value;
    }

    public static void setMySQLUseSSL(boolean value) {
        mysqlUseSSL = value;
    }

    public static void setMySQLVerifyCertificate(boolean value) {
        mysqlVerifyCertificate = value;
    }

    public static void setMysqlPubKeyRetrievalAllowed(boolean value) {
        mysqlPubKeyRetrieval = value;
    }

    public static void setMySQLPoolMaximumPoolSize(int value) {
        mysqlPoolMaximumPoolSize = value;
    }

    public static void setMySQLPoolMinimumIdle(int value) {
        mysqlPoolMinimumIdle = value;
    }

    public static void setMySQLPoolConnectionTimeout(int value) {
        mysqlPoolConnectionTimeout = value;
    }

    public static void setMySQLCachePrepStmts(boolean value) {
        mysqlCachePrepStmts = value;
    }

    public static void setMySQLPrepStmtCacheSize(int value) {
        mysqlPrepStmtCacheSize = value;
    }

    public static void setMySQLPrepStmtCacheSqlLimit(int value) {
        mysqlPrepStmtCacheSqlLimit = value;
    }

    public static void setRestoreToPlayerButton(boolean value) {
        restoreToPlayerButton = value;
    }

    public static void setAllowOtherPluginEditDeathInventory(boolean value) {
        allowOtherPluginEditDeathInventory = value;
    }

    public static void setSaveEmptyInventories(boolean value) {
        saveEmptyInventories = value;
    }

    public static void setAutosaveEnabled(boolean value) {
        autosaveEnabled = value;
    }

    public static void setAutosaveIntervalSeconds(int value) {
        autosaveIntervalSeconds = value;
    }

    public static boolean isAutosaveEnabled() {
        return autosaveEnabled;
    }

    public static int getAutosaveIntervalSeconds() {
        return autosaveIntervalSeconds;
    }

    public static void setBackupLinesVisible(int value) {
        if (value <= 0) {
            backupLinesVisible = 1;
        } else if (value > 5) {
            backupLinesVisible = 5;
        } else {
            backupLinesVisible = value;
        }
    }

    public static void setMaxSavesJoin(int value) {
        maxSavesJoin = value;
    }

    public static void setMaxSavesQuit(int value) {
        maxSavesQuit = value;
    }

    public static void setMaxSavesDeath(int value) {
        maxSavesDeath = value;
    }

    public static void setMaxSavesWorldChange(int value) {
        maxSavesWorldChange = value;
    }

    public static void setMaxSavesForce(int value) {
        maxSavesForce = value;
    }

    public static void setMaxSavesCrash(int value) {
        maxSavesCrash = value;
    }

    public static void setTimeZone(String zone) {
        try {
            // Allow UTC offsets
            if (zone.length() > 3 && zone.startsWith("UTC")) {
                zone = "GMT" + zone.substring(3);
            }

            timeZone = TimeZone.getTimeZone(zone);
            timeZoneName = zone;
            timeZoneOffsetMillis = InventoryRestore.getInstance().getTimeZoneUtil().getMillisOffsetAtTimeZone(zone);
        } catch (IllegalArgumentException | NullPointerException ex) {
            ex.printStackTrace();
            timeZoneOffsetMillis = 0L;
            InventoryRollback.getInstance().getLogger().log(Level.WARNING, ("Time zone \"" + zone + "\" in config.yml is invalid. Defaulting to \"UTC\""));
        }
    }

    public static void setTimeFormat(String format) {
        try {
            timeFormat = new SimpleDateFormat(format);
        } catch (IllegalArgumentException e) {
            timeFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a z");
            InventoryRollback.getInstance().getLogger().log(Level.WARNING, ("Time zone format \"" + format + "\" in config.yml is not valid. Defaulting to \"dd/MM/yyyy hh:mm:ss a z\""));
        }
    }

    public static void setUpdateChecker(boolean enabled) {
        updateChecker = enabled;
    }

    public static void setbStatsEnabled(boolean enabled) {
        bStatsEnabled = enabled;
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static boolean isEnabled() {
        return pluginEnabled;
    }

    public static SaveType getSaveType() {
        return saveType;
    }

    public static File getFolderLocation() {
        return folderLocation;
    }

    public static boolean isMySQLEnabled() {
        return mysqlEnabled;
    }

    public static String getMySQLHost() {
        return mysqlHost;
    }

    public static int getMySQLPort() {
        return mysqlPort;
    }

    public static String getMySQLDatabase() {
        return mysqlDatabase;
    }

    public static String getMySQLTablePrefix() {
        return mysqlTablePrefix;
    }

    public static String getMySQLUsername() {
        return mysqlUsername;
    }

    public static String getMySQLPassword() {
        return mysqlPassword;
    }

    public static boolean isMySQLUseSSL() {
        return mysqlUseSSL;
    }

    public static boolean isMySQLVerifyCertificate() {
        return mysqlVerifyCertificate;
    }

    public static boolean isMySQLPubKeyRetrievalAllowed() {
        return mysqlPubKeyRetrieval;
    }

    public static int getMySQLPoolMaximumPoolSize() {
        return mysqlPoolMaximumPoolSize;
    }

    public static int getMySQLPoolMinimumIdle() {
        return mysqlPoolMinimumIdle;
    }

    public static int getMySQLPoolConnectionTimeout() {
        return mysqlPoolConnectionTimeout;
    }

    public static boolean isMySQLCachePrepStmts() {
        return mysqlCachePrepStmts;
    }

    public static int getMySQLPrepStmtCacheSize() {
        return mysqlPrepStmtCacheSize;
    }

    public static int getMySQLPrepStmtCacheSqlLimit() {
        return mysqlPrepStmtCacheSqlLimit;
    }

    public static boolean isRestoreToPlayerButton() {
        return restoreToPlayerButton;
    }

    public static boolean isAllowOtherPluginEditDeathInventory() {
        return allowOtherPluginEditDeathInventory;
    }

    public static boolean isSaveEmptyInventories() {
        return saveEmptyInventories;
    }

    public static int getBackupLinesVisible() {
        return backupLinesVisible;
    }

    public static int getMaxSavesJoin() {
        return maxSavesJoin;
    }

    public static int getMaxSavesQuit() {
        return maxSavesQuit;
    }

    public static int getMaxSavesDeath() {
        return maxSavesDeath;
    }

    public static int getMaxSavesWorldChange() {
        return maxSavesWorldChange;
    }

    public static int getMaxSavesForce() {
        return maxSavesForce;
    }

    public static int getMaxSavesCrash() {
        return maxSavesCrash;
    }

    public static long getTimeZoneOffsetMillis() {
        return timeZoneOffsetMillis;
    }

    public static TimeZone getTimeZone() {
        return timeZone;
    }

    public static SimpleDateFormat getTimeFormat() {
        return timeFormat;
    }

    public static boolean isUpdateCheckerEnabled() {
        return updateChecker;
    }

    public static boolean isbStatsEnabled() {
        return bStatsEnabled;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static boolean isRefundWebhookEnabled() {
        return refundWebhookEnabled;
    }

    public static String getRefundWebhookUrl() {
        return refundWebhookUrl;
    }

    private static void setRefundWebhookEnabled(boolean v) {
        refundWebhookEnabled = v;
    }

    private static void setRefundWebhookUrl(String v) {
        refundWebhookUrl = v != null ? v : "";
    }

    private boolean saveChanges = false;
    public Object getDefaultValue(String path, Object defaultValue) {
        Object obj = configuration.get(path);

        if (obj == null) {
            obj = defaultValue;

            configuration.set(path, defaultValue);
            saveChanges = true;
        }

        return obj;
    }

    private boolean saveChanges() {
        return saveChanges;
    }

}