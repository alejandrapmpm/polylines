package clock;

public interface Scheduler {

    void addTask(Task task);
    void start();

    interface Task {
        void perform();
    }

}