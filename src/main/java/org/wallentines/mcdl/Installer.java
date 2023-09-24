package org.wallentines.mcdl;

import org.wallentines.mcdl.util.*;
import org.wallentines.mdcfg.ConfigSection;

public enum Installer {

    VANILLA( "vanilla", new TaskList.Builder().then(VanillaUtil.DOWNLOAD_VANILLA).then(VanillaUtil.FIRST_LAUNCH).then(VanillaUtil.CONFIGURE).then(FileUtil.GENERATE_LAUNCH_SCRIPTS).build()),
    FABRIC("fabric", new TaskList.Builder().then(VanillaUtil.DOWNLOAD_VANILLA).then(FabricUtil.INSTALL_FABRIC).then(VanillaUtil.FIRST_LAUNCH).then(VanillaUtil.CONFIGURE).then(FileUtil.GENERATE_LAUNCH_SCRIPTS).build()),
    SPIGOT("spigot", new TaskList.Builder().then(SpigotUtils.BUILD_SPIGOT).then(VanillaUtil.FIRST_LAUNCH).then(VanillaUtil.CONFIGURE).then(FileUtil.GENERATE_LAUNCH_SCRIPTS).build()),
    PAPER("paper", new TaskList.Builder().then(PaperUtils.DOWNLOAD_PAPER).then(VanillaUtil.FIRST_LAUNCH).then(VanillaUtil.CONFIGURE).then(FileUtil.GENERATE_LAUNCH_SCRIPTS).build()),
    VELOCITY("velocity", new TaskList.Builder().then(PaperUtils.DOWNLOAD_VELOCITY).then(FileUtil.GENERATE_LAUNCH_SCRIPTS).build()),
    CUSTOM("custom", new TaskList.Builder().then(CustomUtil.COPY_CUSTOM_JAR).then(VanillaUtil.FIRST_LAUNCH).then(VanillaUtil.CONFIGURE).then(FileUtil.GENERATE_LAUNCH_SCRIPTS).build());

    private final TaskList prefab;
    private final String id;

    Installer(String id, TaskList prefab) {
        this.id = id;
        this.prefab = prefab;
    }

    public void run(ConfigSection config) {
        TaskQueue queue = prefab.createInstance(config);
        queue.run();
    }

    public String getId() {
        return id;
    }

    public static Installer byId(String id) {
        for(Installer ins : values()) {
            if(ins.id.equals(id)) {
                return ins;
            }
        }
        return null;
    }
}
