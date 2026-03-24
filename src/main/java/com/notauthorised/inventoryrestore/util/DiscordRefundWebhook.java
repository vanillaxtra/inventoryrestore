package com.notauthorised.inventoryrestore.util;

import com.notauthorised.inventoryrestore.InventoryRollback;
import com.notauthorised.inventoryrestore.config.ConfigData;
import com.notauthorised.inventoryrestore.data.LogType;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Optional Discord webhook when a full restore is confirmed via the refund flow.
 */
public final class DiscordRefundWebhook {

    private DiscordRefundWebhook() {}

    public static void sendAsync(String staffName, String targetName, LogType logType, long backupTimestamp) {
        if (!ConfigData.isRefundWebhookEnabled()) return;
        String url = ConfigData.getRefundWebhookUrl();
        if (url == null || url.isEmpty()) return;

        InventoryRollback.getInstance().getServer().getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), () -> {
            try {
                String desc = "**Target:** `" + esc(targetName) + "`\n**Staff:** `" + esc(staffName)
                        + "`\n**Backup type:** `" + logType.name() + "`\n**Backup time (epoch ms):** `" + backupTimestamp + "`";
                String json = "{\"embeds\":[{\"title\":\"Inventory refund / restore\",\"description\":\"" + jsonEscape(desc) + "\",\"color\":3447003}]}";

                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes);
                }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                InventoryRollback.getInstance().getLogger().log(Level.WARNING, "Refund webhook request failed", e);
            }
        });
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("`", "'");
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
