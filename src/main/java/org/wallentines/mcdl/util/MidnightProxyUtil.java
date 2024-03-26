package org.wallentines.mcdl.util;

import org.wallentines.mcdl.MinecraftVersion;
import org.wallentines.mcdl.Task;
import org.wallentines.mdcfg.ConfigSection;

import java.io.File;

public class MidnightProxyUtil {


    private static final String REPO = "https://maven.wallentines.org/releases";
    private static final String NAMESPACE = "org.wallentines";
    private static final String INSTALLER_ID = "midnightproxy-proxy";

    public static Task.Result downloadMidnightProxy(String version, File output) {
        return MavenUtil.downloadArtifact(REPO, new MavenUtil.ArtifactSpec(NAMESPACE, INSTALLER_ID, version), output);
    }
    public static String getLatestProxyVersion() {

        return MavenUtil.getLatestVersion(REPO, new MavenUtil.ArtifactSpec(NAMESPACE, INSTALLER_ID, null));
    }

    public static final Task DOWNLOAD_MIDNIGHTPROXY = queue -> {

        ConfigSection config = queue.getConfig();

        String serverVersion = config.getString("version");
        File serverJar = new File(config.getString("jarName"));

        if(serverVersion.equals("latest")) {
            serverVersion = getLatestProxyVersion();
        }

        queue.getLogger().info(String.format("Downloading MidnightProxy version %s...", serverVersion));
        Task.Result dlRes = downloadMidnightProxy(serverVersion, serverJar);
        if(dlRes.isError()) return dlRes;

        return Task.Result.success();
    };

}
