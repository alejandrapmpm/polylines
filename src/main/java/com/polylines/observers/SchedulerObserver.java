package com.polylines.observers;

import com.polylines.scheduler.Scheduler;

public class SchedulerObserver implements Observer {

    private final Scheduler scheduler;

    public SchedulerObserver(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void update(){
        System.out.println("Stoping schedulersssss");
        scheduler.stop();
    }
}
