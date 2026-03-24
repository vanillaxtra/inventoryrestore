package com.notauthorised.inventoryrestore.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks whether the staff member opened menus via /refund (for webhooks / stats).
 */
public final class RestoreSession {

    private static final Map<UUID, Boolean> refundMode = new ConcurrentHashMap<>();

    private RestoreSession() {}

    public static void setRefundContext(UUID staffId, boolean refund) {
        if (refund) {
            refundMode.put(staffId, true);
        } else {
            refundMode.remove(staffId);
        }
    }

    public static boolean isRefundContext(UUID staffId) {
        return Boolean.TRUE.equals(refundMode.get(staffId));
    }

    public static void clear(UUID staffId) {
        refundMode.remove(staffId);
    }
}
