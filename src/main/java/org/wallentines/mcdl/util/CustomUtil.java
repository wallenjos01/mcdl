package org.wallentines.mcdl.util;

import org.wallentines.mcdl.Task;
import org.wallentines.mdcfg.ConfigSection;

import java.io.File;

public class CustomUtil {

    public static final Task COPY_CUSTOM_JAR = queue -> {

        ConfigSection config = queue.getConfig();
        String location = config.getString("customJarPath");
        String url = config.getString("customJarUrl");
        File out = new File(config.getString("jarName"));

        if(location != null) {
            queue.getLogger().info("Copying custom jar...");
            FileUtil.copyFile(new File(location), out);

        } else if(url != null) {
            queue.getLogger().info("Downloading custom jar...");
            if(!DownloadUtil.downloadBytes(url, out)) {
                return Task.Result.error("Unable to download file!");
            }
        } else {
            return Task.Result.error("Custom jars must specify either customJarPath or customJarUrl!");
        }

        return Task.Result.success();

    };


}
