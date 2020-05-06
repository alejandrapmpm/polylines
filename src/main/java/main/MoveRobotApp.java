package main;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import app.ParticleReader;
import app.RobotApplication;
import exception.RobotValidationException;
import model.GeoPoint;
import model.Robot;
import reporting.printer.JsonReportPrinter;
import reporting.service.ReportGeneratorService;
import scheduler.RealScheduler;
import scheduler.Scheduler;
import utilities.GeoPointMapper;

public class MoveRobotApp {

    private static final int SPEED = 2;
    private static ParticleReader particleReader = new ParticleReader();
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
