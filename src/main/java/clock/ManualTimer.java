package clock;

import java.util.ArrayList;
import java.util.List;

public class ManualTimer implements Timer {

    private final List<Task> tasks = new ArrayList<>();

    @Override
    public void addTask(Task task) {
        tasks.add(task);
    }

    @Override
    public void start() {
        // Nothing to do
    }

    public void elapseTime(){
        tasks.forEach(Task::perform);
    }
}