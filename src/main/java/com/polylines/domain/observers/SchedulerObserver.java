package com.polylines.domain.observers;

import com.polylines.application.scheduler.Scheduler;

public class SchedulerObserver implements Observer {

    private final Scheduler scheduler;

    public SchedulerObserver(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void update(){
        scheduler.stop();
    }
}
