package com.polylines.application.scheduler;

public interface Scheduler {

    void addTask(Task task);
    void start();
    void stop();

    interface Task {
        void perform();
    }

}