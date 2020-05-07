package com.polylines.main;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import com.polylines.app.ParticleReader;
import com.polylines.app.RobotApplication;
import com.polylines.exception.RobotValidationException;
import com.polylines.model.GeoPoint;
import com.polylines.model.Robot;
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
        String polyline = args[0];

        List<GeoPoint> journey = generateGeoPoints(new EncodedPolyline(polyline));

        Scheduler robotScheduler = new RealScheduler(1, 0, TimeUnit.SECONDS);
        Scheduler reportingScheduler = new RealScheduler(15, 15, TimeUnit.MINUTES);

        Robot robot = new Robot(journey, SPEED);

        ReportGeneratorService reportGenerator = new ReportGeneratorService(robot, particleReader, jsonReportPrinter);
        reportingScheduler.addTask(reportGenerator::generate);

        RobotApplication app = new RobotApplication(robot, particleReader, robotScheduler, reportingScheduler);
        robotScheduler.addTask(app::moveRobot);

        robotScheduler.start();
        reportingScheduler.start();
    }

    private static List<GeoPoint> generateGeoPoints(EncodedPolyline encoder) {
        GeoPointMapper mapper = new GeoPointMapper();
        return mapper.map(encoder.decodePath());
    }
}
