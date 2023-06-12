package org.wallentines.mcdl.util;

import org.wallentines.mcdl.Task;
import org.wallentines.mdcfg.ConfigSection;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotUtils {

    public static final String BUILDTOOLS_URL = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar";

    public static Task.Result downloadBuildTools(File buildToolsFolder) {

        if(!buildToolsFolder.exists() && !buildToolsFolder.mkdirs()) {
            return Task.Result.error("Unable to create a folder for BuildTools!");
        }

        File out = new File(buildToolsFolder, "BuildTools.jar");
        if(!DownloadUtil.downloadBytes(BUILDTOOLS_URL, out)) {
            return Task.Result.error("Unable to download BuildTools.jar!");
        }

        return Task.Result.success();
    }

    public static Task.Result buildSpigot(File buildToolsFolder, String version, String javaCommand) {

        File jar = new File(buildToolsFolder, "BuildTools.jar");
        if(!jar.exists()) {
            return Task.Result.error("Unable to locate BuildTools!");
        }

        return JarUtil.executeJarFile(javaCommand, jar, buildToolsFolder, new String[]{ "--rev", version }, null);
    }

    public static final Task BUILD_SPIGOT = queue -> {

        ConfigSection config = queue.getConfig();
        File buildFolder = new File(config.getString("spigotBuildFolder"));
        File jar = new File(config.getString("jarName"));
        String version = config.getString("version");

        queue.getLogger().info("Downloading Spigot BuildTools...");
        Task.Result dl = downloadBuildTools(buildFolder);
        if(dl.isError()) return dl;

        queue.getLogger().info("Building Spigot...");
        Task.Result build = buildSpigot(buildFolder, version, config.getString("javaCommand"));
        if(build.isError()) return build;

        queue.getLogger().info("Copying Spigot...");
        File spigotJar = new File(buildFolder, String.format("spigot-%s.jar",version));
        if(version.equals("latest")) {

            Pattern pattern = Pattern.compile("spigot-([0-9])\\.([0-9]+)\\.?([0-9])*\\.jar");

            long biggest = 0L;
            for (File f : Objects.requireNonNull(buildFolder.listFiles())) {

                Matcher matcher = pattern.matcher(f.getName());
                if (!matcher.matches()) continue;

                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                int patch = 0;
                if (matcher.groupCount() > 3) {
                    patch = Integer.parseInt(matcher.group(3));
                }

                long ver = (long) major << 48 + (long) minor << 24 + patch;
                if(ver > biggest) {
                    biggest = ver;
                    spigotJar = f;
                }
            }
        }

        if(!FileUtil.copyFile(spigotJar, jar)) {
            return Task.Result.error("Unable to copy built jar!");
        }

        if(!config.getBoolean("keepSpigotBuildTools")) {
            if(!FileUtil.recursiveDelete(buildFolder)) {
                queue.getLogger().warn("Unable to delete BuildTools!");
            }
        }

        return Task.Result.success();
    };

}
