package org.wallentines.mcdl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.ConfigSection;


public class TaskQueue {

    private final TaskList prefab;
    private final Logger logger = LoggerFactory.getLogger("Installer");
    private final ConfigSection config;
    private final ConfigSection context = new ConfigSection();


    public TaskQueue(TaskList prefab, ConfigSection config) {
        this.prefab = prefab;
        this.config = config;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigSection getConfig() {
        return config;
    }

    public ConfigSection getContext() {
        return context;
    }

    public void run() {

        for(Task t : prefab.getTasks()) {
            Task.Result res = t.run(this);
            if(res.isError()) {
                logger.error(res.getErrorMessage());
                return;
            }
        }
        logger.info("Installation completed successfully!");
    }
}
