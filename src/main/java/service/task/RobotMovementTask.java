package service.task;

import clock.Timer;
import service.RobotMovementService;

public class RobotMovementTask {

    public RobotMovementTask(Timer timer, RobotMovementService robotMovementService) {
        timer.addTask(robotMovementService::moveRobot);
    }
}
