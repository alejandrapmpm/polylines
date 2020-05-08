package com.polylines.main;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import com.polylines.particlereading.ParticleReader;
import com.polylines.app.RobotPollutionCollector;
import com.polylines.exception.RobotValidationException;
import com.polylines.model.GeoPoint;
import com.polylines.model.Robot;
import com.polylines.observers.Observer;
import com.polylines.observers.SchedulerObserver;
import com.polylines.reporting.printer.JsonReportPrinter;
import com.polylines.reporting.service.ReportGeneratorService;
import com.polylines.scheduler.RealScheduler;
import com.polylines.scheduler.Scheduler;
import com.polylines.utilities.GeoPointMapper;

public class MoveRobotApp {

    private static final int SPEED = 2;
    private static ParticleReader particleReader = new ParticleReader(new Random());
    private static JsonReportPrinter jsonReportPrinter = new JsonReportPrinter();

    public static void main(String[] args) throws RobotValidationException {

        List<GeoPoint> journey = generateGeoPoints(new EncodedPolyline(args[0]));

        Scheduler robotScheduler = new RealScheduler(1, 0, TimeUnit.SECONDS);
        Scheduler reportingScheduler = new RealScheduler(15, 15, TimeUnit.MINUTES);

        Robot robot = new Robot(journey, SPEED);
        robot = addSchedulersAsObservers(robot, robotScheduler, reportingScheduler);

        ReportGeneratorService reportGenerator = new ReportGeneratorService(robot, particleReader, jsonReportPrinter);
        reportingScheduler.addTask(reportGenerator::generate);

        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);
        robotScheduler.addTask(app::moveRobot);

        robotScheduler.start();
        reportingScheduler.start();
    }

    private static Robot addSchedulersAsObservers(Robot robot, Scheduler robotScheduler, Scheduler reportingScheduler) {
        Observer robotSchedulerObserver = new SchedulerObserver(robotScheduler);
        Observer reportingSchedulerObserver = new SchedulerObserver(reportingScheduler);
        robot.registerObserver(robotSchedulerObserver);
        robot.registerObserver(reportingSchedulerObserver);
        return robot;
    }

    private static List<GeoPoint> generateGeoPoints(EncodedPolyline encoder) {
        return GeoPointMapper.map(encoder.decodePath());
    }
}
