import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import clock.Clock;
import clock.RealClock;
import reporting.ReportGenerator;
import service.ParticleReader;
import service.RobotMovementService;

public class Main {

    public static void main(String [] args){
        String polyline = "orl~Ff|{uO~@y@}A_AEsE";
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
        Clock timer = new RealClock(1000, TimeUnit.MILLISECONDS);
        ParticleReader particleReader = new ParticleReader();
        RobotMovementService robotMovementService = new RobotMovementService(encoder, 50d, timer, particleReader);
        timer.start();

        Clock reportTimer = new RealClock(5000, TimeUnit.MILLISECONDS);
        ReportGenerator reportGenerator = new ReportGenerator(robotMovementService.robot, particleReader, reportTimer);
        reportTimer.start();
    }
}
