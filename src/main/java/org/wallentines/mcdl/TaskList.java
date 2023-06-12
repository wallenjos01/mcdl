package org.wallentines.mcdl;

import org.wallentines.mdcfg.ConfigSection;

import java.util.ArrayList;
import java.util.List;

public class TaskList {

    private final List<Task> tasks;

    private TaskList(List<Task> tasks) {
        this.tasks = List.copyOf(tasks);
    }

    public TaskQueue createInstance(ConfigSection sec) {

        return new TaskQueue(this, sec);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    static class Builder {

        private final List<Task> tasks = new ArrayList<>();

        Builder then(Task task) {
            tasks.add(task);
            return this;
        }

        Builder then(Runnable runnable) {
            tasks.add(queue -> {
                try {
                    runnable.run();
                    return Task.Result.success();
                } catch (Throwable th) {
                    return Task.Result.error(th.getMessage());
                }
            });
            return this;
        }

        TaskList build() {
            return new TaskList(tasks);
        }
    }
}
