package com.notauthorised.inventoryrestore;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UpdateChecker {

    private static final String MODRINTH_SLUG = "inventoryrestore";
    private static final String VERSIONS_URL = "https://api.modrinth.com/v2/project/" + MODRINTH_SLUG + "/version";

    private static final Pattern VERSION_OBJECT = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");

    private final String currentVersion;
    private String availableVersion;

    private UpdateResult result = UpdateResult.FAIL_SPIGOT;

    public enum UpdateResult {
        NO_UPDATE,
        FAIL_SPIGOT,
        UPDATE_AVAILABLE
    }

    public UpdateChecker(JavaPlugin plugin) {
        this.currentVersion = normalizeVersion(plugin.getDescription().getVersion());
        String ua = "InventoryRestore/" + (currentVersion.isEmpty() ? "unknown" : currentVersion);
        try {
            List<String> published = fetchVersionNumbers(ua);
            if (published.isEmpty()) {
                result = UpdateResult.FAIL_SPIGOT;
                return;
            }
            String best = null;
            String bestNorm = null;
            for (String v : published) {
                String norm = normalizeVersion(v);
                if (norm.isEmpty()) continue;
                if (bestNorm == null || compareVersions(norm, bestNorm) > 0) {
                    best = v;
                    bestNorm = norm;
                }
            }
            if (best == null || bestNorm.isEmpty()) {
                result = UpdateResult.FAIL_SPIGOT;
                return;
            }
            this.availableVersion = bestNorm;
            if (compareVersions(this.availableVersion, this.currentVersion) > 0) {
                result = UpdateResult.UPDATE_AVAILABLE;
            } else {
                result = UpdateResult.NO_UPDATE;
            }
        } catch (IOException ignored) {
            result = UpdateResult.FAIL_SPIGOT;
        }
    }

    private List<String> fetchVersionNumbers(String userAgent) throws IOException {
        HttpURLConnection con = open(VERSIONS_URL, userAgent);
        int code = con.getResponseCode();
        String body = readBody(con);
        if (code != 200 || body.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>();
        Matcher m = VERSION_OBJECT.matcher(body);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    private static HttpURLConnection open(String urlStr, String userAgent) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(urlStr).openConnection();
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", userAgent);
        con.setConnectTimeout(10_000);
        con.setReadTimeout(10_000);
        con.setInstanceFollowRedirects(true);
        return con;
    }

    private static String readBody(HttpURLConnection con) throws IOException {
        InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            in = con.getErrorStream();
            if (in == null) {
                return "";
            }
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static int compareVersions(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) {
            return a.compareTo(b);
        }
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int n = Math.max(pa.length, pb.length);
        for (int i = 0; i < n; i++) {
            int na = i < pa.length ? parseIntSafe(pa[i]) : 0;
            int nb = i < pb.length ? parseIntSafe(pb[i]) : 0;
            if (na != nb) {
                return Integer.compare(na, nb);
            }
        }
        return 0;
    }

    private static int parseIntSafe(String segment) {
        String digits = segment.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public UpdateResult getResult() {
        return this.result;
    }

    public String getVersion() {
        return this.availableVersion != null ? this.availableVersion : "";
    }

    private static String normalizeVersion(String version) {
        if (version == null) {
            return "";
        }
        return version.replaceAll("[^0-9.]", "");
    }
}
