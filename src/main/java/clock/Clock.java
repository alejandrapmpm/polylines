package clock;

public interface Clock {
    void addTask(Task task);
    void start();

    interface Task {
        void perform();
    }
}