package com.notauthorised.inventoryrestore.data;

import com.notauthorised.inventoryrestore.InventoryRollback;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Tracks who opened a backup (view) and who ran a full restore, per backup snapshot.
 */
public final class BackupActivityTracker {

    private static File file;
    private static FileConfiguration yaml;

    private BackupActivityTracker() {}

    public static final class ActivityEntry {
        public final long time;
        public final String staffName;
        public final boolean restore; // false = view

        public ActivityEntry(long time, String staffName, boolean restore) {
            this.time = time;
            this.staffName = staffName;
            this.restore = restore;
        }

        static String encode(long time, String staff, boolean restore) {
            String s = staff == null ? "unknown" : staff.replace("|", "_");
            return time + "|" + s + "|" + (restore ? "RESTORE" : "VIEW");
        }

        static ActivityEntry parse(String line) {
            if (line == null || line.isEmpty()) return null;
            String[] p = line.split("\\|", 3);
            if (p.length != 3) return null;
            try {
                long t = Long.parseLong(p[0]);
                boolean rest = "RESTORE".equalsIgnoreCase(p[2]);
                return new ActivityEntry(t, p[1], rest);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static void init() {
        file = new File(InventoryRollback.getInstance().getDataFolder(), "backup-activity.yml");
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                InventoryRollback.getInstance().getLogger().log(Level.WARNING, "Could not create backup-activity.yml", e);
            }
        }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    private static String backupKey(LogType logType, long backupTimestamp) {
        return logType.name() + "@" + backupTimestamp;
    }

    private static ConfigurationSection playerSection(UUID targetUuid) {
        String key = targetUuid.toString();
        ConfigurationSection s = yaml.getConfigurationSection(key);
        if (s == null) {
            yaml.createSection(key);
            s = yaml.getConfigurationSection(key);
        }
        return s;
    }

    private static List<String> eventList(UUID targetUuid, LogType logType, long backupTimestamp) {
        ConfigurationSection s = playerSection(targetUuid);
        String path = "backups." + backupKey(logType, backupTimestamp) + ".events";
        List<String> list = s.getStringList(path);
        return list instanceof ArrayList ? list : new ArrayList<>(list);
    }

    public static synchronized void recordView(String staffName, UUID targetUuid, LogType logType, long backupTimestamp) {
        if (yaml == null) init();
        List<String> list = eventList(targetUuid, logType, backupTimestamp);
        list.add(0, ActivityEntry.encode(System.currentTimeMillis(), staffName, false));
        while (list.size() > 200) {
            list.remove(list.size() - 1);
        }
        playerSection(targetUuid).set("backups." + backupKey(logType, backupTimestamp) + ".events", list);
        save();
    }

    public static synchronized void recordRestore(String staffName, UUID targetUuid, LogType logType, long backupTimestamp) {
        if (yaml == null) init();
        List<String> list = eventList(targetUuid, logType, backupTimestamp);
        list.add(0, ActivityEntry.encode(System.currentTimeMillis(), staffName, true));
        while (list.size() > 200) {
            list.remove(list.size() - 1);
        }
        playerSection(targetUuid).set("backups." + backupKey(logType, backupTimestamp) + ".events", list);
        save();
    }

    public static List<ActivityEntry> listEvents(UUID targetUuid, LogType logType, long backupTimestamp, int max) {
        if (yaml == null) init();
        List<String> raw = eventList(targetUuid, logType, backupTimestamp);
        List<ActivityEntry> out = new ArrayList<>();
        for (String line : raw) {
            ActivityEntry e = ActivityEntry.parse(line);
            if (e != null) out.add(e);
            if (out.size() >= max) break;
        }
        return out;
    }

    public static int countViews(UUID targetUuid, LogType logType, long backupTimestamp) {
        int n = 0;
        for (ActivityEntry e : listEvents(targetUuid, logType, backupTimestamp, 500)) {
            if (!e.restore) n++;
        }
        return n;
    }

    public static int countRestores(UUID targetUuid, LogType logType, long backupTimestamp) {
        int n = 0;
        for (ActivityEntry e : listEvents(targetUuid, logType, backupTimestamp, 500)) {
            if (e.restore) n++;
        }
        return n;
    }

    private static void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            InventoryRollback.getInstance().getLogger().log(Level.WARNING, "Could not save backup-activity.yml", e);
        }
    }
}
