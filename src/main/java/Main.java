import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import clock.Timer;
import clock.RealTimer;
import model.GeoPoint;
import model.Robot;
import reporting.service.ReportGeneratorService;
import reporting.task.ReportGeneratorTask;
import reporting.printer.JsonReportPrinter;
import app.ParticleReader;
import app.RobotApplication;
import app.task.RobotMovementTask;
import utilities.GeoPointMapper;

public class Main {

    public static void main(String [] args){
        String polyline = "{gr~F`b`vO`mIcgHusDz|U";
        /*
        *   Distance is: 42.93135105797141
            Distance is: 58.59883353809855
            Distance is: 87.86280526271533
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
        );*/
        EncodedPolyline encoder = new EncodedPolyline(polyline);
        List<GeoPoint> journey = generateGeoPoints(encoder);

        ParticleReader particleReader = new ParticleReader();

        Robot robot = new Robot(journey, 50d);
        RobotApplication app = new RobotApplication(robot, particleReader);

        Timer robotScheduler = new RealTimer(1000, TimeUnit.MILLISECONDS);
        RobotMovementTask robotMovementTask = new RobotMovementTask(robotScheduler, app);

        Timer reportingScheduler = new RealTimer(5000, TimeUnit.MILLISECONDS);
        ReportGeneratorService reportGenerator = new ReportGeneratorService(robot, particleReader, new JsonReportPrinter());
        ReportGeneratorTask reportTask = new ReportGeneratorTask(reportingScheduler, reportGenerator);

        robotScheduler.start();
        reportingScheduler.start();
    }

    private static List<GeoPoint> generateGeoPoints(EncodedPolyline encoder) {
        GeoPointMapper mapper = new GeoPointMapper();
        return mapper.map(encoder.decodePath());
    }
}
