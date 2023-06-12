package org.wallentines.mcdl.util;

import org.wallentines.mcdl.MinecraftVersion;
import org.wallentines.mcdl.Task;
import org.wallentines.mdcfg.ConfigSection;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class FabricUtil {

    private static final String FABRIC_INSTALLER_URL = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/%s/fabric-installer-%s.jar";

    public static Task.Result downloadFabricInstaller(String version, File output) {

        String actualUrl = String.format(FABRIC_INSTALLER_URL, version, version);
        if(!DownloadUtil.downloadBytes(actualUrl, output)) {
            return Task.Result.error("Unable to download Fabric installer!");
        }
        return Task.Result.success();
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

            ex.printStackTrace();
            return Task.Result.error("Unable to configure Fabric server launcher!");
        }

        return Task.Result.success();
    }

    public static String getLatestInstallerVersion() {

        String url = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/maven-metadata.xml";

        try {
            URL actualUrl = new URL(url);
            URLConnection conn = actualUrl.openConnection();

            SAXParser parser = SAXParserFactory.newDefaultInstance().newSAXParser();
            LatestVersionHandler handler = new LatestVersionHandler();
            parser.parse(conn.getInputStream(), handler);

            return handler.latestVersion;

        } catch (IOException | ParserConfigurationException | SAXException ex) {
            ex.printStackTrace();
            return "0.11.2";
        }

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

    private static class LatestVersionHandler extends DefaultHandler {
        private String latestVersion = null;
        private final Stack<String> tags = new Stack<>();
        private final StringBuilder currentValue = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            tags.push(qName);
            currentValue.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            String tag = tags.pop();
            if(!tag.equals(qName)) throw new SAXException("Corrupted stack!");

            if(tag.equals("latest") && tags.peek().equals("versioning") && tags.size() == 2) {
                latestVersion = currentValue.toString();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {

            currentValue.append(ch, start, length);
        }
    }

}
