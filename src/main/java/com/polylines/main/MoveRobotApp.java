package com.polylines.main;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import com.polylines.application.moverobot.RobotPollutionCollector;
import com.polylines.domain.robot.RobotValidationException;
import com.polylines.domain.robot.GeoPoint;
import com.polylines.domain.robot.Robot;
import com.polylines.domain.observers.Observer;
import com.polylines.domain.observers.SchedulerObserver;
import com.polylines.domain.particlesreading.ParticleReader;
import com.polylines.application.readparticles.RandomParticleReader;
import com.polylines.infraestructure.reportprinting.JsonReportPrinter;
import com.polylines.application.generatereport.ReportGeneratorService;
import com.polylines.application.scheduler.RealScheduler;
import com.polylines.application.scheduler.Scheduler;
import com.polylines.infraestructure.GeoPointMapper;

public class MoveRobotApp {

    private static final double SPEED = 2;
    private static ParticleReader particleReader = new RandomParticleReader(new Random());
    private static JsonReportPrinter jsonReportPrinter = new JsonReportPrinter();

    public static void main(String[] args) throws RobotValidationException {

        String polyline = args[0];
        Robot robot = new Robot(generateJourney(new EncodedPolyline(polyline)), SPEED);

        Scheduler robotScheduler = new RealScheduler(1, 0, TimeUnit.SECONDS);
        Scheduler reportingScheduler = new RealScheduler(15, 15, TimeUnit.MINUTES);

        robot = addSchedulersAsObservers(robot, robotScheduler, reportingScheduler);

        ReportGeneratorService reportGenerator = new ReportGeneratorService(robot, particleReader, jsonReportPrinter);
        reportingScheduler.addTask(reportGenerator::generate);

        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);
        robotScheduler.addTask(app::moveRobot);

        launchApplication(robotScheduler, reportingScheduler);
    }

    private static void launchApplication(Scheduler robotScheduler, Scheduler reportingScheduler) {
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

    private static List<GeoPoint> generateJourney(EncodedPolyline encoder) {
        return GeoPointMapper.map(encoder.decodePath());
    }
}
