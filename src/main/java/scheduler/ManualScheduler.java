package scheduler;

import java.util.ArrayList;
import java.util.List;

//This class is for testing purposes
public class ManualScheduler implements Scheduler {

    private final List<Task> tasks = new ArrayList<>();

    @Override
    public void addTask(Task task) {
        tasks.add(task);
    }

    @Override
    public void start() {
        // Nothing to do
    }

    @Override
    public void stop() {
        //Nothing to do
    }

    public void elapseTime(){
        tasks.forEach(Task::perform);
    }
}