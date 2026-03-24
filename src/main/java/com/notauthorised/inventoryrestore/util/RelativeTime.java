package com.notauthorised.inventoryrestore.util;

/**
 * Human-readable relative past times ("22 hours ago").
 */
public final class RelativeTime {

    private RelativeTime() {}

    public static String ago(long epochMs) {
        if (epochMs <= 0) return "unknown";
        long now = System.currentTimeMillis();
        long diff = now - epochMs;
        if (diff < 0) return "in the future?";
        if (diff < 10_000L) return "just now";
        if (diff < 60_000L) return (diff / 1000L) + " seconds ago";
        if (diff < 3_600_000L) return (diff / 60_000L) + " minutes ago";
        if (diff < 86_400_000L) return (diff / 3_600_000L) + " hours ago";
        if (diff < 604_800_000L) return (diff / 86_400_000L) + " days ago";
        if (diff < 2_629_800_000L) return (diff / 604_800_000L) + " weeks ago";
        long months = diff / 2_629_800_000L;
        if (months < 12) return months + " months ago";
        return (diff / 31_557_600_000L) + " years ago";
    }
}
