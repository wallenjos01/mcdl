package org.wallentines.mcdl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mcdl.Task;
import org.wallentines.mdcfg.ConfigSection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger("FileUtil");

    public static boolean verifyData(File f, String sha1) {

        if(!f.exists()) return false;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            FileInputStream fis = new FileInputStream(f);

            DigestInputStream dis = new DigestInputStream(fis, sha);

            byte[] buffer = new byte[1024];
            while(dis.read(buffer) > 0) {
                // Read all data
            }

            fis.close();

            return sha1.equals(toHexString(sha.digest()));

        } catch (NoSuchAlgorithmException ex) {

            LOGGER.warn("SHA-1 Algorithm could not be loaded! File will not be verified!");
            return true;

        } catch (IOException ex) {

            LOGGER.error("Unable to open file for verification!");
            return false;
        }
    }

    public static boolean recursiveDelete(File f) {

        if(!f.exists()) return true;
        if(f.isDirectory()) {

            File[] files = f.listFiles();
            if(files != null) for(File sub : files) {
                if(!recursiveDelete(sub)) return false;
            }
            return true;

        } else {

            return f.delete();
        }
    }

    public static boolean copyFile(File src, File dst) {

        if(!src.exists()) return false;
        if(src.equals(dst)) return true;
        if(src.isDirectory()) {

            File[] files = src.listFiles();
            if(files != null) for(File sub : files) {
                if(!copyFile(sub, new File(dst, sub.getName()))) return false;
            }
            return true;

        } else {

            try(FileInputStream is = new FileInputStream(src); FileOutputStream os = new FileOutputStream(dst)) {

                byte[] buffer = new byte[1024];
                int read;
                while((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                return true;

            } catch (IOException ex) {

                return false;
            }
        }
    }

    public static File getWorkingDir(ConfigSection sec) {

        File out;

        String workingDir = sec.getOrDefault("serverWorkingDir", (String) null);
        if(workingDir == null) {
            out = new File(System.getProperty("user.dir"));
        } else {
            out = new File(workingDir);
        }

        if(!out.exists() && !out.mkdirs()) {
            LOGGER.error("Unable to create working directory! (" + out.getAbsolutePath() + ")");
        }

        return out;
    }

    public static String toHexString(byte[] data) {

        StringBuilder out = new StringBuilder();
        for (byte b : data) {
            int value = b & 0xFF;
            if (value < 16) {
                out.append("0");
            }
            out.append(Integer.toHexString(value));
        }

        return out.toString();
    }

    public static final Task GENERATE_LAUNCH_SCRIPTS = queue -> {

        if(!queue.getConfig().getBoolean("generateScripts")) return Task.Result.success();

        String java = queue.getConfig().getString("javaCommand");
        File jar = new File(queue.getConfig().getString("jarName"));

        File workingDir = FileUtil.getWorkingDir(queue.getConfig());

        StringBuilder cmd = new StringBuilder(java);
        if(queue.getContext().getOrDefault("fabric", Boolean.FALSE)) {
            cmd.append(" -Dfabric.gameJarPath=").append(jar.getAbsolutePath());
            jar = new File(jar.getParent(), "fabric-server-launch.jar");
        }
        cmd.append(" -jar ").append(workingDir.toPath().toAbsolutePath().relativize(jar.toPath().toAbsolutePath())).append(" nogui");

        if(System.getProperty("os.name").startsWith("Windows")) {

            queue.getLogger().info("Generating launch scripts for Windows...");

            // Windows
            File batch = new File("start.bat");
            try(FileOutputStream fos = new FileOutputStream(batch)) {

                fos.write("@echo off\n".getBytes(StandardCharsets.UTF_8));
                fos.write(("cd " + workingDir.getAbsolutePath() + "\n").getBytes(StandardCharsets.UTF_8));
                fos.write(cmd.toString().getBytes(StandardCharsets.UTF_8));

            } catch (IOException ex) {
                return Task.Result.error("Unable to write launch scripts!");
            }
        } else {

            queue.getLogger().info("Generating launch scripts for Linux...");

            // Unix
            File shell = new File("start.sh");
            try(FileOutputStream fos = new FileOutputStream(shell)) {

                fos.write("#!/bin/sh\n".getBytes(StandardCharsets.UTF_8));
                fos.write(("cd " + workingDir.getAbsolutePath() + "\n").getBytes(StandardCharsets.UTF_8));
                fos.write(cmd.toString().getBytes(StandardCharsets.UTF_8));

                fos.close();
                if(!shell.setExecutable(true)) {
                    queue.getLogger().warn("Unable to mark " + shell.getName() + " as executable!");
                }

            } catch (IOException ex) {
                return Task.Result.error("Unable to write launch scripts!");
            }
        }

        return Task.Result.success();

    };

}
