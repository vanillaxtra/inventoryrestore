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
 * Per-player restore/refund counts, per-backup refund tallies, and refund event log.
 */
public final class RestoreStatsManager {

    private static File file;
    private static FileConfiguration yaml;

    private RestoreStatsManager() {}

    public static final class RefundEvent {
        public final long eventTime;
        public final String staffName;
        public final LogType logType;
        public final long backupTimestamp;

        public RefundEvent(long eventTime, String staffName, LogType logType, long backupTimestamp) {
            this.eventTime = eventTime;
            this.staffName = staffName;
            this.logType = logType;
            this.backupTimestamp = backupTimestamp;
        }

        static RefundEvent parse(String line) {
            if (line == null || line.isEmpty()) return null;
            String[] p = line.split("\\|", 4);
            if (p.length != 4) return null;
            try {
                return new RefundEvent(Long.parseLong(p[0]), p[1], LogType.valueOf(p[2]), Long.parseLong(p[3]));
            } catch (Exception e) {
                return null;
            }
        }

        static String encode(long eventTime, String staffName, LogType logType, long backupTimestamp) {
            String s = staffName == null ? "unknown" : staffName.replace("|", "_");
            return eventTime + "|" + s + "|" + logType.name() + "|" + backupTimestamp;
        }
    }

    public static void init() {
        file = new File(InventoryRollback.getInstance().getDataFolder(), "restore-stats.yml");
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                InventoryRollback.getInstance().getLogger().log(Level.WARNING, "Could not create restore-stats.yml", e);
            }
        }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    private static ConfigurationSection section(UUID uuid) {
        String key = uuid.toString();
        ConfigurationSection s = yaml.getConfigurationSection(key);
        if (s == null) {
            yaml.createSection(key);
            s = yaml.getConfigurationSection(key);
        }
        return s;
    }

    /** Log types can contain underscores (e.g. WORLD_CHANGE); use a delimiter that cannot appear in names. */
    private static String backupKey(LogType logType, long backupTimestamp) {
        return logType.name() + "@" + backupTimestamp;
    }

    public static final class BackupRefundSummary {
        public final LogType logType;
        public final long backupTimestamp;
        public final int refundCount;
        public final long lastRefundTime;
        public final String lastStaff;

        public BackupRefundSummary(LogType logType, long backupTimestamp, int refundCount,
                                   long lastRefundTime, String lastStaff) {
            this.logType = logType;
            this.backupTimestamp = backupTimestamp;
            this.refundCount = refundCount;
            this.lastRefundTime = lastRefundTime;
            this.lastStaff = lastStaff;
        }
    }

    public static List<BackupRefundSummary> listBackupRefundSummaries(UUID uuid, int max) {
        if (yaml == null) init();
        ConfigurationSection s = section(uuid);
        ConfigurationSection per = s.getConfigurationSection("per-backup-refunds");
        if (per == null) return new ArrayList<>();
        List<BackupRefundSummary> out = new ArrayList<>();
        for (String key : per.getKeys(false)) {
            int count = per.getInt(key, 0);
            if (count < 1) continue;
            int at = key.lastIndexOf('@');
            if (at <= 0) continue;
            LogType lt;
            long bkTs;
            try {
                lt = LogType.valueOf(key.substring(0, at));
                bkTs = Long.parseLong(key.substring(at + 1));
            } catch (Exception e) {
                continue;
            }
            long lastT = 0;
            String lastSt = "";
            for (RefundEvent ev : listRefundEvents(uuid, 200)) {
                if (ev.logType == lt && ev.backupTimestamp == bkTs) {
                    if (ev.eventTime >= lastT) {
                        lastT = ev.eventTime;
                        lastSt = ev.staffName;
                    }
                }
            }
            out.add(new BackupRefundSummary(lt, bkTs, count, lastT, lastSt));
        }
        out.sort((a, b) -> Long.compare(b.lastRefundTime, a.lastRefundTime));
        if (out.size() > max) {
            return new ArrayList<>(out.subList(0, max));
        }
        return out;
    }

    public static void recordFullRestore(UUID targetUuid, String staffName, boolean refund,
                                         LogType logType, long backupTimestamp) {
        if (yaml == null) init();
        ConfigurationSection s = section(targetUuid);
        String prefix = refund ? "refund." : "restore.";
        int n = s.getInt(prefix + "count", 0) + 1;
        s.set(prefix + "count", n);
        s.set(prefix + "last_time", System.currentTimeMillis());
        s.set(prefix + "last_staff", staffName != null ? staffName : "unknown");

        if (refund) {
            String bk = backupKey(logType, backupTimestamp);
            int per = s.getInt("per-backup-refunds." + bk, 0) + 1;
            s.set("per-backup-refunds." + bk, per);

            List<String> events = s.getStringList("refund-events");
            if (!(events instanceof ArrayList)) {
                events = new ArrayList<>(events);
            }
            events.add(0, RefundEvent.encode(System.currentTimeMillis(), staffName, logType, backupTimestamp));
            while (events.size() > 100) {
                events.remove(events.size() - 1);
            }
            s.set("refund-events", events);
        }
        save();
    }

    public static int getBackupRefundCount(UUID uuid, LogType logType, long backupTimestamp) {
        if (yaml == null) init();
        return section(uuid).getInt("per-backup-refunds." + backupKey(logType, backupTimestamp), 0);
    }

    public static List<RefundEvent> listRefundEvents(UUID uuid, int max) {
        if (yaml == null) init();
        List<String> raw = section(uuid).getStringList("refund-events");
        List<RefundEvent> out = new ArrayList<>();
        for (String line : raw) {
            RefundEvent e = RefundEvent.parse(line);
            if (e != null) out.add(e);
            if (out.size() >= max) break;
        }
        return out;
    }

    public static int getRestoreCount(UUID uuid) {
        if (yaml == null) init();
        return section(uuid).getInt("restore.count", 0);
    }

    public static int getRefundCount(UUID uuid) {
        if (yaml == null) init();
        return section(uuid).getInt("refund.count", 0);
    }

    public static long getLastRestoreTime(UUID uuid) {
        if (yaml == null) init();
        return section(uuid).getLong("restore.last_time", 0);
    }

    public static long getLastRefundTime(UUID uuid) {
        if (yaml == null) init();
        return section(uuid).getLong("refund.last_time", 0);
    }

    public static String getLastRestoreStaff(UUID uuid) {
        if (yaml == null) init();
        return section(uuid).getString("restore.last_staff", "");
    }

    public static String getLastRefundStaff(UUID uuid) {
        if (yaml == null) init();
        return section(uuid).getString("refund.last_staff", "");
    }

    private static void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            InventoryRollback.getInstance().getLogger().log(Level.WARNING, "Could not save restore-stats.yml", e);
        }
    }
}
