import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import clock.Timer;
import clock.RealTimer;
import reporting.service.ReportGeneratorService;
import reporting.task.ReportGeneratorTask;
import reporting.printer.JsonReportPrinter;
import service.ParticleReader;
import service.RobotMovementService;
import service.task.RobotMovementTask;

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
        //robotMovementService.moveRobot(40);
        Timer robotScheduler = new RealTimer(1000, TimeUnit.MILLISECONDS);
        ParticleReader particleReader = new ParticleReader();
        RobotMovementService robotMovementService = new RobotMovementService(encoder, 50d, particleReader);
        RobotMovementTask robotMovementTask = new RobotMovementTask(robotScheduler, robotMovementService);
        robotScheduler.start();

        Timer reportingScheduler = new RealTimer(5000, TimeUnit.MILLISECONDS);
        ReportGeneratorService reportGenerator = new ReportGeneratorService(robotMovementService.robot, particleReader, new JsonReportPrinter());
        ReportGeneratorTask reportTask = new ReportGeneratorTask(reportingScheduler, reportGenerator);
        reportingScheduler.start();
    }
}
