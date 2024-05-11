package org.wallentines.mcdl.util;

import org.wallentines.mcdl.MinecraftVersion;
import org.wallentines.mcdl.Task;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

import static org.wallentines.mcdl.util.DownloadUtil.downloadBytes;
import static org.wallentines.mcdl.util.DownloadUtil.downloadJSON;

public class VanillaUtil {

    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";


    public static MinecraftVersion downloadVersion(String version) {

        ConfigObject obj = downloadJSON(VERSION_MANIFEST_URL);
        if(obj == null) {
            System.out.println("Unable to download version manifest!");
            return null;
        }

        if(version.equals("latest")) {
            version = obj.asSection().getSection("latest").getString("release");
        } else if(version.equals("latest-snapshot")) {
            version = obj.asSection().getSection("latest").getString("snapshot");
        }

        for(ConfigObject ver : obj.asSection().getList("versions").values()) {

            if(!ver.isSection()) continue;

            String versionId = ver.asSection().getString("id");
            if(versionId.equals(version)) {

                return new MinecraftVersion(versionId,
                        ver.asSection().getString("type").equals("snapshot"),
                        ver.asSection().getString("url")
                );
            }
        }

        System.out.println("Unable to find requested Minecraft version " + version);
        return null;
    }

    public static boolean downloadVanillaJar(MinecraftVersion version, File output) {

        ConfigObject definition = downloadJSON(version.getDefinitionUrl());
        if(definition == null) {
            System.out.println("Unable to download version definition for version " + version);
            return false;
        }


        ConfigSection serverDl = definition.asSection().getSection("downloads").getSection("server");
        String sha = serverDl.getString("sha1");

        if(FileUtil.verifyData(output, sha)) {
            return true;
        }

        String serverUrl = serverDl.getString("url");

        if(!downloadBytes(serverUrl, output)) {
            System.out.println("Unable to download server jar!");
            return false;
        }

        return true;
    }

    public static final Task DOWNLOAD_VANILLA = queue -> {

        ConfigSection config = queue.getConfig();

        String version = config.getString("version");
        String jarName = config.getString("jarName");


        MinecraftVersion ver = downloadVersion(version);
        if(ver == null) {
            return Task.Result.error("Unable to find Minecraft version " + version);
        }

        queue.getLogger().info(String.format("Downloading Vanilla Server version %s...", ver.getVersionId()));

        if(!downloadVanillaJar(ver, new File(jarName))) {
            return Task.Result.error("Unable to download vanilla jar!");
        }

        return Task.Result.success();
    };

    public static final Task CONFIGURE = queue -> {

        ConfigSection config = queue.getConfig();

        // EULA
        if(config.getBoolean("acceptEula")) {

            queue.getLogger().info("Accepting EULA...");

            File eula = new File(FileUtil.getWorkingDir(queue.getConfig()), "eula.txt");
            try(FileOutputStream os = new FileOutputStream(eula)) {
                os.write("eula=true".getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                return Task.Result.error("Unable to accept eula! " + ex.getMessage());
            }
        }

        // Port
        if (config.has("port")) {

            int port;
            try {
                port = Integer.parseInt(config.getString("port"));
                if(port < 1 || port > Short.MAX_VALUE) {
                    return Task.Result.error("Server port out of range [1,65535]!");
                }
            } catch (NumberFormatException ex) {
                return Task.Result.error("Unable to parse port number!");
            }

            // Load server.properties
            Properties props = new Properties();
            File properties = new File(FileUtil.getWorkingDir(queue.getConfig()), "server.properties");
            if(properties.isFile()) {
                try(FileInputStream fis = new FileInputStream(properties)) {
                    props.load(fis);
                } catch (IOException ex) {
                    return Task.Result.error("Unable to load server.properties!" + ex.getMessage());
                }
            }

            props.setProperty("server-port", Objects.toString(port));

            // Save server.properties
            try(FileOutputStream os = new FileOutputStream(properties)) {
                props.store(os, "");
            } catch (IOException ex) {
                return Task.Result.error("Unable to write server.properties! " + ex.getMessage());
            }
        }

        return Task.Result.success();
    };

    public static final Task FIRST_LAUNCH = queue -> {

        if(queue.getConfig().getBoolean("skipFirstLaunch")) {
            return Task.Result.success();
        }

        File workingDir = FileUtil.getWorkingDir(queue.getConfig());
        File eula = new File(workingDir, "eula.txt");
        if(eula.exists() && !eula.delete()) {
            queue.getLogger().warn("Unable to delete eula.txt! First launch skipped!");
            return Task.Result.success();
        }

        queue.getLogger().info("Performing first launch...");
        String java = queue.getConfig().getString("javaCommand");
        File jar = new File(queue.getConfig().getString("jarName"));

        String[] jvmArgs = null;
        if(queue.getContext().getOrDefault("fabric", false)) {
            jvmArgs = new String[] { "-Dfabric.gameJarPath=" + jar.getAbsolutePath() };
            jar = new File(jar.getParent(), "fabric-server-launch.jar");
        }

        if(!jar.exists()) {
            queue.getLogger().warn("Unable to find launch jar! First launch skipped!");
            return Task.Result.success();
        }

        return JarUtil.executeJarFile(java, jar, workingDir, new String[] { "nogui" }, jvmArgs);
    };
}
