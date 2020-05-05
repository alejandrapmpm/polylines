package scheduler;

public interface Scheduler {

    void addTask(Task task);
    void start();
    void stop();

    interface Task {
        void perform();
    }

}