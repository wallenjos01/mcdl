package org.wallentines.mcdl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mcdl.MinecraftVersion;
import org.wallentines.mcdl.Task;
import org.wallentines.mdcfg.ConfigSection;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class FabricUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("FabricUtil");
    private static final String FABRIC_INSTALLER_URL = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/%s/fabric-installer-%s.jar";
    private static final String FABRIC_REPO = "https://maven.fabricmc.net/";
    private static final String FABRIC_NAMESPACE = "net.fabricmc";
    private static final String FABRIC_INSTALLER_ID = "fabric-installer";

    public static Task.Result downloadFabricInstaller(String version, File output) {
        return MavenUtil.downloadArtifact(FABRIC_REPO, new MavenUtil.ArtifactSpec(FABRIC_NAMESPACE, FABRIC_INSTALLER_ID, version), output);
    }

    public static Task.Result installFabric(File installerJar, File serverJar, MinecraftVersion version, String javaCommand) {

        List<String> command = Arrays.asList(
                "server",
                "-mcversion", version.getVersionId()
        );
        if(version.isSnapshot()) {
            command.add("-snapshot");
        }

        // Execute jarfile
        JarUtil.executeJarFile(javaCommand, installerJar, null, command.toArray(new String[0]), null);

        // Configure launcher
        try(FileOutputStream os = new FileOutputStream("fabric-server-launcher.properties")) {

            os.write(("serverJar=" + serverJar.getPath()).getBytes(StandardCharsets.UTF_8));

        } catch (IOException ex) {

            LOGGER.warn("An IOExecption occurred while reading fabric-server-launcher", ex);
            return Task.Result.error("Unable to configure Fabric server launcher!");
        }

        return Task.Result.success();
    }

    public static String getLatestInstallerVersion() {

        String out = MavenUtil.getLatestVersion(FABRIC_REPO, new MavenUtil.ArtifactSpec(FABRIC_NAMESPACE, FABRIC_INSTALLER_ID, null));
        if(out == null) {
            LOGGER.warn("Unable to determine latest Fabric installer version! Defaulting to 1.0.0");
            return "1.0.0";
        }
        return out;
    }

    public static final Task INSTALL_FABRIC = queue -> {

        ConfigSection config = queue.getConfig();

        String installerVersion = config.getString("fabricInstallerVersion");
        String serverVersion = config.getString("version");
        String javaCommand = config.getString("javaCommand");
        File installerJar = new File(config.getString("fabricInstallerJarName"));
        File serverJar = new File(config.getString("jarName"));
        boolean keep = config.getBoolean("keepFabricInstaller");

        if(!serverJar.exists()) {
            return Task.Result.error("Unable to install Fabric! Server jar is missing!");
        }

        queue.getContext().set("fabric", true);

        MinecraftVersion ver = VanillaUtil.downloadVersion(serverVersion);
        if(ver == null) {
            return Task.Result.error("Unable to find Minecraft version " + serverVersion);
        }

        if(installerVersion.equals("latest")) {
            installerVersion = getLatestInstallerVersion();
        }

        queue.getLogger().info(String.format("Downloading Fabric installer version %s...", installerVersion));
        Task.Result dlRes = downloadFabricInstaller(installerVersion, installerJar);
        if(dlRes.isError()) return dlRes;

        queue.getLogger().info("Installing Fabric...");
        Task.Result inRes = installFabric(installerJar, serverJar, ver, javaCommand);
        if(inRes.isError()) return inRes;

        queue.getLogger().info("Cleaning up...");
        if(!keep && !installerJar.delete()) {
            queue.getLogger().warn("Warning: Unable to delete Fabric installer!");
        }

        return Task.Result.success();
    };



}
