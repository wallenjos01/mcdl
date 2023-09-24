package org.wallentines.mcdl;

import org.wallentines.mdcfg.ConfigSection;

public class Main {

    public static void main(String[] args) {

        ArgumentParser parser = new ArgumentParser()
                // Globals
                .addOption("type", 't', "vanilla")
                .addOption("javaCommand", "java")
                .addOption("version", 'v', "latest")
                .addOption("jarName", "minecraft_server.jar")
                .addOption("serverWorkingDir")
                .addFlag("acceptEula", 'e')
                .addFlag("skipFirstLaunch", 's')
                .addFlag("generateScripts", 'g')
                // Fabric
                .addOption("fabricInstallerVersion", "latest")
                .addOption("fabricInstallerJarName", "fabric-installer.jar")
                .addFlag("keepFabricInstaller")
                // Spigot
                .addOption("spigotBuildFolder", "BuildTools")
                .addFlag("keepSpigotBuildTools")
                // Paper
                .addOption("paperBuild", "latest")
                // Velocity
                .addOption("velocityBuild", "latest")
                // Custom
                .addOption("customJarPath")
                .addOption("customJarUrl");

        ArgumentParser.ParseResult result = parser.parse(args);
        if(result.isError()) {
            System.out.println(result.getError());
            return;
        }

        ConfigSection sec = result.getOutput().toConfigSection();

        String type = sec.getString("type");
        Installer installer = Installer.byId(type);
        if(installer == null) {
            System.out.println("Unknown installer type " + type);
            return;
        }

        installer.run(sec);

    }


}
