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

/**
 * Compares the running plugin version to the latest GitHub release (or newest tag).
 * API: <a href="https://docs.github.com/en/rest/releases/releases">GitHub Releases REST</a>
 */
public class UpdateChecker {

    private static final String GITHUB_OWNER = "vanillaxtra";
    private static final String GITHUB_REPO = "inventoryrestore";

    private static final Pattern TAG_NAME = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    /** Tag listing: each tag object includes "name":"…" */
    private static final Pattern TAG_OBJECT_NAME = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

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
            String remote = fetchLatestReleaseTag(ua);
            if (remote == null || remote.isEmpty()) {
                remote = fetchBestTagFromApi(ua);
            }
            if (remote == null || remote.isEmpty()) {
                result = UpdateResult.FAIL_SPIGOT;
                return;
            }
            this.availableVersion = normalizeVersion(remote);
            if (this.availableVersion.isEmpty()) {
                result = UpdateResult.FAIL_SPIGOT;
                return;
            }
            if (compareVersions(this.availableVersion, this.currentVersion) > 0) {
                result = UpdateResult.UPDATE_AVAILABLE;
            } else {
                result = UpdateResult.NO_UPDATE;
            }
        } catch (IOException ignored) {
            result = UpdateResult.FAIL_SPIGOT;
        }
    }

    private String fetchLatestReleaseTag(String userAgent) throws IOException {
        String url = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases/latest";
        HttpURLConnection con = open(url, userAgent);
        int code = con.getResponseCode();
        String body = readBody(con);
        if (code == 404) {
            return null;
        }
        if (code != 200) {
            return null;
        }
        Matcher m = TAG_NAME.matcher(body);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String fetchBestTagFromApi(String userAgent) throws IOException {
        String url = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/tags?per_page=100";
        HttpURLConnection con = open(url, userAgent);
        if (con.getResponseCode() != 200) {
            return null;
        }
        String body = readBody(con);
        List<String> names = new ArrayList<>();
        Matcher m = TAG_OBJECT_NAME.matcher(body);
        while (m.find()) {
            names.add(m.group(1));
        }
        if (names.isEmpty()) {
            return null;
        }
        String best = null;
        String bestNorm = null;
        for (String name : names) {
            String norm = normalizeVersion(name);
            if (norm.isEmpty()) {
                continue;
            }
            if (bestNorm == null || compareVersions(norm, bestNorm) > 0) {
                best = name;
                bestNorm = norm;
            }
        }
        return best;
    }

    private static HttpURLConnection open(String urlStr, String userAgent) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(urlStr).openConnection();
        con.setRequestProperty("Accept", "application/vnd.github+json");
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

    /**
     * @return positive if a &gt; b, 0 if equal, negative if a &lt; b
     */
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
