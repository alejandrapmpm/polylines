package clock;

import java.util.ArrayList;
import java.util.List;

public class ManualClock implements Clock{
    private final List<Task> tasks = new ArrayList<>();
    @Override
    public void addTask(Task task) {
        tasks.add(task);
    }

    @Override
    public void start() {
        // Ignored
    }

    public void elapseTime(){
        tasks.forEach(Task::perform);
    }
}