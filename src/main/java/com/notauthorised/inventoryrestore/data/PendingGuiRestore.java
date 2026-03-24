package com.notauthorised.inventoryrestore.data;

import com.notauthorised.inventoryrestore.gui.menu.MainBackupGuiSnapshot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds a staff member's captured main-backup GUI layout between "Restore" and overwrite confirm
 * (or until applied), so restores match on-screen edits without writing back to stored backups.
 */
public final class PendingGuiRestore {

    private static final Map<UUID, MainBackupGuiSnapshot> BY_STAFF = new ConcurrentHashMap<>();

    private PendingGuiRestore() {}

    public static void store(UUID staffId, MainBackupGuiSnapshot snapshot) {
        if (snapshot == null) {
            BY_STAFF.remove(staffId);
        } else {
            BY_STAFF.put(staffId, snapshot);
        }
    }

    public static void clear(UUID staffId) {
        BY_STAFF.remove(staffId);
    }

    /**
     * If the staff has a pending GUI snapshot for this exact backup, removes and returns it.
     * If the snapshot does not match, it is put back and this returns null.
     */
    public static MainBackupGuiSnapshot takeIfMatches(UUID staffId, UUID targetUuid, LogType logType, long timestamp) {
        MainBackupGuiSnapshot s = BY_STAFF.remove(staffId);
        if (s == null) return null;
        if (!s.matches(targetUuid, logType, timestamp)) {
            BY_STAFF.put(staffId, s);
            return null;
        }
        return s;
    }
}
