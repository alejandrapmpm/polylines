package com.polylines.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RealScheduler implements Scheduler {

    private final long period;
    private final long initialDelay;
    private final TimeUnit periodTimeUnit;
    private final List<Task> tasks;
    private final ScheduledExecutorService scheduler;

    public RealScheduler(long period, long initialDelay, TimeUnit periodTimeUnit) {
        this.period = period;
        this.initialDelay = initialDelay;
        this.periodTimeUnit = periodTimeUnit;
        this.tasks = new ArrayList<>();
        this.scheduler =  Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void addTask(Task task) {
        tasks.add(task);
    }

    @Override
    public void start() {
        scheduler.scheduleAtFixedRate(this::performTask, initialDelay, period, periodTimeUnit);
    }
    @Override
    public void stop() {
        scheduler.shutdown();
    }

    private void performTask() {
        tasks.forEach(Task::perform);
    }
}