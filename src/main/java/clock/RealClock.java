package clock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RealClock implements Clock{
    private final long period;
    private final TimeUnit periodTimeUnit;
    private final List<Task> tasks = new ArrayList<>();
    private final ScheduledExecutorService timerService = Executors.newSingleThreadScheduledExecutor();

    public RealClock(long period, TimeUnit periodTimeUnit) {
        this.period = period;
        this.periodTimeUnit = periodTimeUnit;
    }

    @Override
    public void addTask(Task task) {
        tasks.add(task);
    }

    @Override
    public void start() {
        timerService.scheduleAtFixedRate(this::performTask, period, period, periodTimeUnit);
    }

    private void performTask() {
        tasks.forEach(Task::perform);
    }
}