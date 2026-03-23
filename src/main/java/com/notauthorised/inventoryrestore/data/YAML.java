package com.notauthorised.inventoryrestore.data;

import com.notauthorised.inventoryrestore.InventoryRestore;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.config.MessageData;
import com.notauthorised.inventoryrestore.gui.InventoryName;
import com.notauthorised.inventoryrestore.inventory.RestoreInventory;
import com.notauthorised.inventoryrestore.inventory.SaveInventory;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class YAML {

    private final UUID uuid;
    private final Long timestamp;
    private final File playerBackupFolder;
    private final File backupFile;
    private final YamlConfiguration data;

    private String mainInventory;
    private String armour;
    private String enderChest;
    private String offhand;
    private float xp;
    private double health;
    private int hunger;
    private float saturation;
    private String world;
    private double x;
    private double y;
    private double z;
    private LogType logType;
    private String packageVersion;
    private String deathReason;

    private static final String backupFolderName = "backups";

    public YAML(UUID uuid, LogType logType, Long timestampIn) {
        this.uuid = uuid;
        this.logType = logType;
        this.timestamp = timestampIn;
        this.playerBackupFolder = getPlayerBackupLocation(logType, uuid);
        this.backupFile = new File (playerBackupFolder, timestamp + ".yml");
        this.data = YamlConfiguration.loadConfiguration(backupFile);
    }

    public static void createStorageFolders() {        
        //Create folder for where player inventories will be saved
        File savesFolder = new File(ConfigData.getFolderLocation().getAbsoluteFile(), backupFolderName);
        if(!savesFolder.exists())
            savesFolder.mkdir();

        //Create folder for joins
        File joinsFolder = new File(savesFolder, "joins");
        if(!joinsFolder.exists())
            joinsFolder.mkdir();

        //Create folder for quits
        File quitsFolder = new File(savesFolder, "quits");
        if(!quitsFolder.exists())
            quitsFolder.mkdir();

        //Create folder for deaths
        File deathsFolder = new File(savesFolder, "deaths");
        if(!deathsFolder.exists())
            deathsFolder.mkdir();

        //Create folder for world changes
        File worldChangesFolder = new File(savesFolder, "worldChanges");
        if(!worldChangesFolder.exists())
            worldChangesFolder.mkdir();

        //Create folder for force saves
        File forceSavesFolder = new File(savesFolder, "force");
        if(!forceSavesFolder.exists())
            forceSavesFolder.mkdir();

        //Create folder for autosaves (temp - deleted on shutdown)
        File autosavesFolder = new File(savesFolder, "autosaves");
        if(!autosavesFolder.exists())
            autosavesFolder.mkdir();

        //Create folder for crash recovery
        File crashesFolder = new File(savesFolder, "crashes");
        if(!crashesFolder.exists())
            crashesFolder.mkdir();
    }

    private static File getRootBackupsFolder() {
        return new File(ConfigData.getFolderLocation(), backupFolderName);
    }

    private static File getBackupFolderForLogType(LogType backupLogType) {
        File backupLocation = getRootBackupsFolder();

        if (backupLogType == LogType.JOIN) {
            backupLocation = new File(backupLocation, "joins");
        } else if (backupLogType == LogType.QUIT) {
            backupLocation = new File(backupLocation, "quits");
        } else if (backupLogType == LogType.DEATH) {
            backupLocation = new File(backupLocation, "deaths");
        } else if (backupLogType == LogType.WORLD_CHANGE) {
            backupLocation = new File(backupLocation, "worldChanges");
        } else if (backupLogType == LogType.FORCE) {
            backupLocation = new File(backupLocation, "force");
        } else if (backupLogType == LogType.CRASH) {
            backupLocation = new File(backupLocation, "crashes");
        }

        return backupLocation;
    }

    private static File getPlayerBackupLocation(LogType backupLogType, UUID playerUUID) {
        return new File(getBackupFolderForLogType(backupLogType), playerUUID.toString());
    }

    public boolean doesBackupTypeExist() {
        return getAmountOfBackups() > 0;
    }

    public int getAmountOfBackups() {         
        if (!playerBackupFolder.exists()) return 0;

        String[] filesArr = playerBackupFolder.list();
        if (filesArr == null) return 0;

        return filesArr.length;
    }

    public List<Long> getSelectedPageTimestamps(int pageNumber) {
        List<Long> allTimeStamps = new ArrayList<>();

        if (!playerBackupFolder.exists())
            return allTimeStamps;

        long currTime = System.currentTimeMillis();

        for (File file : playerBackupFolder.listFiles()) {
            if (file.isDirectory())
                continue;

            int pos = file.getName().lastIndexOf('.');
            String fileName = file.getName().substring(0, pos);

            if (fileName.isEmpty() || !StringUtils.isNumeric(fileName))
                continue;

            long timestamp = Long.parseLong(fileName);
            // Make sure that the file hasn't been created in the last 1s: we could still be writing to it
            if (currTime - timestamp > 1000) allTimeStamps.add(timestamp);
        }

        //Set timestamps in order
        Collections.sort(allTimeStamps, Collections.reverseOrder());

        //Number of backups that will be on the page
        int backups = InventoryName.ROLLBACK_LIST.getSize() - 9;

        //Return all timestamps if list is the same size or less than the page max size
        if (allTimeStamps.size() <= backups)
            return allTimeStamps;

        List<Long> requiredTimestamps = new ArrayList<>();
        for (int i = (backups * (pageNumber - 1)); i < ((backups * (pageNumber - 1)) + backups); i++) {            
            if (i < allTimeStamps.size()) {
                requiredTimestamps.add(allTimeStamps.get(i));
            } else {
                break;
            }
        }

        return requiredTimestamps;
    }

    public void purgeExcessSaves(int deleteAmount) {
        List<Long> timeSaved = new ArrayList<>();

        File[] backupFiles = playerBackupFolder.listFiles();
        if (backupFiles == null) return;

        for (File file : backupFiles) {
            if (file.isDirectory())
                continue;

            int pos = file.getName().lastIndexOf('.');
            String fileName = file.getName().substring(0, pos);

            if (!StringUtils.isNumeric(fileName))
                continue;

            long saveTimeStamp;
            try {
                saveTimeStamp = Long.parseLong(fileName);
            } catch (NumberFormatException ex) {
                continue;
            }

            // In the case the backup was created less than a second ago, ignore it to avoid data corruption
            if (System.currentTimeMillis() - saveTimeStamp < 1000) {
                continue;
            }

            timeSaved.add(saveTimeStamp);
        }


        for (int i = 0; i < deleteAmount; i++) {
            if (timeSaved.isEmpty()) break;

            Long deleteTimestamp = Collections.min(timeSaved);
            timeSaved.remove(deleteTimestamp);

            try {
                Files.deleteIfExists(new File (playerBackupFolder, deleteTimestamp + ".yml").toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setMainInventory(ItemStack[] items) {
        this.mainInventory = SaveInventory.toBase64(items);
    }

    public void setArmour(ItemStack[] items) {
        this.armour = SaveInventory.toBase64(items);
    }

    public void setEnderChest(ItemStack[] items) {
        this.enderChest = SaveInventory.toBase64(items);
    }

    public void setOffhand(ItemStack item) {
        this.offhand = item != null ? SaveInventory.toBase64(new ItemStack[]{item}) : null;
    }

    public void setXP(float xp) {
        this.xp = xp;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void setFoodLevel(int foodLevel) {
        this.hunger = foodLevel;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public void setVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public void setDeathReason(String deathReason) {
        this.deathReason = deathReason;
    }

    public ItemStack[] getMainInventory() {
        String base64 = data.getString("inventory");
        return RestoreInventory.getInventoryItems(getVersion(), base64);
    }

    public ItemStack[] getArmour() {
        String base64 = data.getString("armour");
        return RestoreInventory.getInventoryItems(getVersion(), base64);
    }

    public ItemStack[] getEnderChest() {
        String base64 = data.getString("enderchest");
        return RestoreInventory.getInventoryItems(getVersion(), base64);
    }

    public ItemStack getOffhand() {
        String base64 = data.getString("offhand");
        if (base64 == null || base64.isEmpty()) return null;
        ItemStack[] arr = RestoreInventory.getInventoryItems(getVersion(), base64);
        return arr != null && arr.length > 0 ? arr[0] : null;
    }

    public float getXP() {
        return Float.parseFloat(data.getString("xp"));
    }

    public double getHealth() {
        return data.getDouble("health");
    }

    public int getFoodLevel() {
        return data.getInt("hunger");
    }

    public float getSaturation() {
        return Float.parseFloat(data.getString("saturation"));
    }

    public String getWorld() {
        return data.getString("location.world");
    }

    public double getX() {
        return data.getDouble("location.x");
    }

    public double getY() {
        return data.getDouble("location.y");
    }

    public double getZ() {
        return data.getDouble("location.z");
    }

    public LogType getSaveType() {
        LogType logType = null;

        try {
            logType = LogType.valueOf(data.getString("logType").toUpperCase());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return logType;
    }

    public String getVersion() {
        return data.getString("version");
    }

    public String getDeathReason() {
        return data.getString("deathReason");
    }

    public void saveData() {
        data.set("inventory", mainInventory);
        data.set("armour", armour);
        data.set("enderchest", enderChest);
        data.set("offhand", offhand);
        data.set("xp", xp);
        data.set("health", health);
        data.set("hunger", hunger);
        data.set("saturation", saturation);
        data.set("location.world", world);
        data.set("location.x", x);
        data.set("location.y", y);
        data.set("location.z", z);
        data.set("logType", logType.name());
        data.set("version", packageVersion);
        data.set("deathReason", deathReason);

        try {
            data.save(backupFile);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static String getBackupFolderName() {
        return backupFolderName;
    }

    private static File getAutosavesFolder() {
        return new File(getRootBackupsFolder(), "autosaves");
    }

    /**
     * Ensure autosave and crash folders exist (called on enable regardless of YAML/MySQL).
     */
    public static void ensureAutosaveFoldersExist() {
        File root = getRootBackupsFolder();
        if (!root.exists()) root.mkdirs();
        File autosavesFolder = getAutosavesFolder();
        if (!autosavesFolder.exists()) autosavesFolder.mkdirs();
        File crashesFolder = new File(root, "crashes");
        if (!crashesFolder.exists()) crashesFolder.mkdirs();
    }

    /**
     * Save a snapshot to the autosave folder (one file per player, overwritten each run).
     * Called every 30 seconds for online players.
     */
    public static void saveAutosave(UUID uuid, SaveInventory.PlayerDataSnapshot snapshot) {
        if (snapshot == null) return;
        ensureAutosaveFoldersExist();
        File autosavesFolder = getAutosavesFolder();
        File playerFile = new File(autosavesFolder, uuid.toString() + ".yml");
        YamlConfiguration data = new YamlConfiguration();
        long timestamp = System.currentTimeMillis();
        data.set("inventory", snapshot.finalMainInvContents != null ? SaveInventory.toBase64(snapshot.finalMainInvContents) : null);
        data.set("armour", snapshot.finalMainInvArmor != null ? SaveInventory.toBase64(snapshot.finalMainInvArmor) : null);
        data.set("enderchest", snapshot.finalEnderInvContents != null ? SaveInventory.toBase64(snapshot.finalEnderInvContents) : null);
        data.set("offhand", snapshot.finalOffhand != null ? SaveInventory.toBase64(new org.bukkit.inventory.ItemStack[]{snapshot.finalOffhand}) : null);
        data.set("xp", snapshot.totalXp);
        data.set("health", snapshot.health);
        data.set("hunger", snapshot.foodLevel);
        data.set("saturation", snapshot.saturation);
        data.set("location.world", snapshot.worldName);
        data.set("location.x", snapshot.locX);
        data.set("location.y", snapshot.locY);
        data.set("location.z", snapshot.locZ);
        data.set("logType", LogType.CRASH.name());
        data.set("version", com.notauthorised.inventoryrestore.InventoryRollback.getPackageVersion());
        data.set("deathReason", "AUTOSAVE");
        try {
            data.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * On startup: if autosave files exist, server crashed. Move them to crashes folder.
     */
    public static void processCrashRecovery() {
        File autosavesFolder = getAutosavesFolder();
        if (!autosavesFolder.exists()) return;
        File[] files = autosavesFolder.listFiles();
        if (files == null || files.length == 0) return;

        InventoryRestore.getInstance().getLogger().info("Crash detected - recovering autosave data to crashes...");
        File crashesRoot = new File(getRootBackupsFolder(), "crashes");
        if (!crashesRoot.exists()) crashesRoot.mkdirs();

        for (File f : files) {
            if (!f.isFile() || !f.getName().endsWith(".yml")) continue;
            String uuidStr = f.getName().substring(0, f.getName().length() - 4);
            try {
                UUID uuid = UUID.fromString(uuidStr);
                File crashPlayerFolder = new File(crashesRoot, uuid.toString());
                if (!crashPlayerFolder.exists()) crashPlayerFolder.mkdirs();
                long timestamp = System.currentTimeMillis();
                File dest = new File(crashPlayerFolder, timestamp + ".yml");
                Files.copy(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                f.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * On planned shutdown: delete all autosave files.
     */
    public static void deleteAutosaveFolder() {
        File autosavesFolder = getAutosavesFolder();
        if (!autosavesFolder.exists()) return;
        File[] files = autosavesFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                try {
                    f.delete();
                } catch (Exception ignored) {}
            }
        }
    }

    public static void convertOldBackupData() {
        List<File> backupLocations = new ArrayList<>();
        backupLocations.add(new File(ConfigData.getFolderLocation(), "saves/deaths"));
        backupLocations.add(new File(ConfigData.getFolderLocation(), "saves/joins"));
        backupLocations.add(new File(ConfigData.getFolderLocation(), "saves/quits"));
        backupLocations.add(new File(ConfigData.getFolderLocation(), "saves/worldChanges"));
        backupLocations.add(new File(ConfigData.getFolderLocation(), "saves/force"));

        List<LogType> logTypeFiles = new ArrayList<>();
        int logTypeNumber = 0;
        logTypeFiles.add(LogType.DEATH);
        logTypeFiles.add(LogType.JOIN);
        logTypeFiles.add(LogType.QUIT);
        logTypeFiles.add(LogType.WORLD_CHANGE);
        logTypeFiles.add(LogType.FORCE);

        for (File backupFolders : backupLocations) {
            if (!backupFolders.exists()) {
                InventoryRestore.getInstance().getConsoleSender().sendMessage(MessageData.getPluginPrefix() + ChatColor.RED + "Backup folder does not exist at " + backupFolders.getAbsolutePath());
                logTypeNumber++;
                continue;
            }

            List<File> backupFiles = new ArrayList<>();

            //Add all YAML files to list
            for (File file : backupFolders.listFiles()) {
                if (file.isFile() && file.getName().substring(file.getName().indexOf('.')).equals(".yml")) {
                    backupFiles.add(file);
                }
            }

            LogType log = logTypeFiles.get(logTypeNumber);
            InventoryRestore.getInstance().getConsoleSender().sendMessage(MessageData.getPluginPrefix() + "Converting the backup location " + log.name());

            for (File backup : backupFiles) {
                YamlConfiguration data = new YamlConfiguration();
                
                try {
                    data.load(backup);
                } catch (InvalidConfigurationException | IOException e) {
                    InventoryRestore.getInstance().getConsoleSender().sendMessage(MessageData.getPluginPrefix() + ChatColor.RED + "Error converting backup file at " + backup.getAbsolutePath() + " - Invalid YAML format possibly from corruption.");
                    continue;
                }

                for (String time : data.getConfigurationSection("data").getKeys(false)) {
                    try {
                        Long timestamp = Long.parseLong(time);
                        UUID uuid = UUID.fromString(backup.getName().substring(0, backup.getName().length() - 4)); 

                        YAML yaml = new YAML(uuid, log, timestamp);

                        String packageVersion = data.getString("data." + timestamp + ".version");

                        yaml.setMainInventory(RestoreInventory.getInventoryItems(packageVersion, data.getString("data." + timestamp + ".inventory")));
                        yaml.setArmour(RestoreInventory.getInventoryItems(packageVersion, data.getString("data." + timestamp + ".armour")));
                        yaml.setEnderChest(RestoreInventory.getInventoryItems(packageVersion, data.getString("data." + timestamp + ".enderchest")));                    
                        yaml.setXP(Float.parseFloat(data.getString("data." + timestamp + ".xp")));
                        yaml.setHealth(data.getDouble("data." + timestamp + ".health"));
                        yaml.setFoodLevel(data.getInt("data." + timestamp + ".hunger"));
                        yaml.setSaturation(Float.parseFloat(data.getString("data." + timestamp + ".saturation")));
                        yaml.setWorld(data.getString("data." + timestamp + ".location.world"));
                        yaml.setX(data.getDouble("data." + timestamp + ".location.x"));
                        yaml.setY(data.getDouble("data." + timestamp + ".location.y"));
                        yaml.setZ(data.getDouble("data." + timestamp + ".location.z"));

                        String lt = data.getString("data." + timestamp + ".logType");
                        LogType logType = null;
                        if (lt.equalsIgnoreCase("WORLDCHANGE")) {
                            logType = LogType.WORLD_CHANGE;
                        } else {
                            logType = LogType.valueOf(lt);
                        }
                        yaml.setLogType(logType);

                        yaml.setVersion(packageVersion);
                        yaml.setDeathReason(data.getString("data." + timestamp + ".deathReason"));

                        yaml.saveData();
                    } catch (Exception e) {
                        InventoryRestore.getInstance().getConsoleSender().sendMessage(MessageData.getPluginPrefix() + ChatColor.RED + "Error converting backup file at " + backup.getAbsolutePath() + " on timestamp " + time);
                    }
                }
            }

            logTypeNumber++;

        }

        InventoryRestore.getInstance().getConsoleSender().sendMessage(MessageData.getPluginPrefix() + ChatColor.GREEN + "Conversion completed!");
    }

}
