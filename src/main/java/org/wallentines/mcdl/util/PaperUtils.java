package org.wallentines.mcdl.util;

import org.wallentines.mcdl.Task;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.Serializer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public class PaperUtils {

    public static final String VERSIONS_URL = "https://api.papermc.io/v2/projects/%s/";
    public static final String BUILDS_URL = VERSIONS_URL + "versions/%s/";
    public static final String DOWNLOAD_URL = BUILDS_URL + "builds/%s/downloads/%s-%s-%s.jar";

    private static Task.Result downloadFromPaperMC(String project, String version, String build, File output) {

        if(version.equals("latest")) {
            try {
                URLConnection conn = new URL(String.format(VERSIONS_URL, project)).openConnection();
                ConfigSection sec = JSONCodec.loadConfig(conn.getInputStream()).asSection();

                ConfigList lst = sec.getList("versions");
                version = lst.get(lst.size() - 1, Serializer.STRING);

            } catch (IOException | IllegalStateException ex) {

                return Task.Result.error("Unable to find " + project + " versions! " + ex.getMessage());
            }
        }

        if(build.equals("latest")) {
            try {
                URLConnection conn = new URL(String.format(BUILDS_URL, project, version)).openConnection();
                ConfigSection sec = JSONCodec.loadConfig(conn.getInputStream()).asSection();

                ConfigList lst = sec.getList("builds");
                build = Objects.toString(lst.get(lst.size() - 1, Serializer.INT));

            } catch (IOException | IllegalStateException ex) {

                return Task.Result.error("Unable to find " + project + " builds for version " + version + "! " + ex.getMessage());
            }
        }

        if(!DownloadUtil.downloadBytes(String.format(DOWNLOAD_URL, project, version, build, project, version, build), output)) {
            return Task.Result.error("Unable to download " + project + "!");
        }

        return Task.Result.success();

    }

    public static Task.Result downloadPaper(String version, String build, File output) {

        return downloadFromPaperMC("paper", version, build, output);
    }

    public static Task.Result downloadVelocity(String version, String build, File output) {

        return downloadFromPaperMC("velocity", version, build, output);
    }

    public static final Task DOWNLOAD_PAPER = queue -> {

        ConfigSection config = queue.getConfig();
        String version = config.getString("version");
        String build = config.getString("paperBuild");
        File out = new File(config.getString("jarName"));

        queue.getLogger().info("Downloading Paper...");
        return downloadPaper(version, build, out);
    };

    public static final Task DOWNLOAD_VELOCITY = queue -> {

        ConfigSection config = queue.getConfig();
        String version = config.getString("version");
        String build = config.getString("velocityBuild");
        File out = new File(config.getString("jarName"));

        queue.getLogger().info("Downloading Velocity...");
        return downloadVelocity(version, build, out);
    };

}
