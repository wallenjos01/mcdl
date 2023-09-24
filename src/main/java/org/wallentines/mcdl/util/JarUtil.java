package org.wallentines.mcdl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mcdl.Task;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

public class JarUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger("JarUtil");

    public static Task.Result executeJarFile(String javaCmd, File jarfile, File workingDir, String[] args, String[] jvmArgs) {

        jarfile = jarfile.getAbsoluteFile();

        if(javaCmd.equals("native")) {
            return executeJarFileNative(jarfile, args);
        }

        if(workingDir != null) {
            jarfile = workingDir.toPath().toAbsolutePath().relativize(jarfile.toPath().toAbsolutePath()).toFile();
        }

        List<String> command = new ArrayList<>();
        command.add(javaCmd);
        if(jvmArgs != null) {
            command.addAll(Arrays.asList(jvmArgs));
        }
        command.add("-jar");
        command.add(jarfile.getPath());
        if(args != null) {
            command.addAll(Arrays.asList(args));
        }

        ProcessBuilder builder = new ProcessBuilder(command).inheritIO();
        if(workingDir != null) {
            builder.directory(workingDir);
        }

        try {
            builder.start().waitFor();
        } catch (IOException | InterruptedException ex) {

            File parent = jarfile.getParentFile();

            if (parent != null) {
                LOGGER.warn("JAR Parent file: " + parent.getAbsolutePath());
                LOGGER.warn("Files in directory: ");

                if (parent.isDirectory()) for (File f : parent.listFiles()) {
                    LOGGER.warn(" - " + f.getAbsolutePath());
                }
            }

            LOGGER.error("Unable to execute command " + String.join(" ", builder.command()) + "!");
            return Task.Result.error("Unable to execute " + jarfile.getPath() + "! " + ex.getMessage());
        }

        return Task.Result.success();
    }

    public static Task.Result executeJarFileNative(File jarfile, String[] args) {

        try(JarFile file = new JarFile(jarfile)) {

            String mainClass = file.getManifest().getMainAttributes().getValue("Main-Class");

            try(URLClassLoader loader = new URLClassLoader(new URL[] { jarfile.toURI().toURL() })) {
                Class<?> clazz = loader.loadClass(mainClass);
                Method main = clazz.getDeclaredMethod("main", String.class.arrayType());

                // Execute installer
                main.invoke(clazz, (Object) args);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException ex) {

                LOGGER.error("An error occurred while loading a jar file! ", ex);
                return Task.Result.error("Unable to find entry point in file " + jarfile.getAbsolutePath() + "! " + ex.getMessage());
            }

        } catch (IOException ex) {

            LOGGER.error("An error occurred while loading a jar file! ", ex);
            return Task.Result.error("Unable to find jarfile " + jarfile.getAbsolutePath() + "! " + ex.getMessage());
        }

        return Task.Result.success();
    }


}
