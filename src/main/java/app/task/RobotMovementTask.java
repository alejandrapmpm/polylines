package app.task;

import clock.Timer;
import app.RobotApplication;

public class RobotMovementTask {

    public RobotMovementTask(Timer timer, RobotApplication robotApp) {
        timer.addTask(robotApp::moveRobot);
    }
}
