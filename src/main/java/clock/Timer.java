package clock;

public interface Timer {

    void addTask(Task task);
    void start();

    interface Task {
        void perform();
    }

}