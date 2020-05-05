import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import app.ParticleReader;
import app.RobotApplication;
import scheduler.RealScheduler;
import scheduler.Scheduler;
import model.GeoPoint;
import model.Robot;
import reporting.printer.JsonReportPrinter;
import reporting.service.ReportGeneratorService;
import utilities.GeoPointMapper;

public class Main {

    public static void main(String [] args){
        String polyline = "mpjyHx`i@VjAVKnAh@BHHX@LZR@Bj@Ml@WWc@]w@bAyAfBmCb@o@pLeQfCsDVa@@ODQR}AJ{A?{BGuAD_@FKb@MTUX]Le@^kBVcAVo@Ta@|EaFh@m@FWaA{DCo@q@mCm@cC{A_GWeA}@sGSeAcA_EOSMa@}A_GsAwFkAiEoAaFaBoEGo@]_AIWW{AQyAUyBQqAI_BFkEd@aHZcDlAyJLaBPqDDeD?mBEiA}@F]yKWqGSkICmCIeZIuZi@_Sw@{WgAoXS{DOcAWq@KQGIFQDGn@Y`@MJEFIHyAVQVOJGHgFRJBBCCSKBcAKoACyA?m@^yVJmLJ{FGGWq@e@eBIe@Ei@?q@Bk@Hs@Le@Rk@gCuIkJcZsDwLd@g@Oe@o@mB{BgHQYq@qBQYOMSMGBUBGCYc@E_@H]DWJST?JFFHBDNBJ?LED?LBv@WfAc@@EDGNK|@e@hAa@`Bk@b@OEk@Go@IeACoA@a@PyB`@yDDc@e@K{Bi@oA_@w@]m@_@]QkBoAwC{BmAeAo@s@uAoB_AaBmAwCa@mAo@iCgAwFg@iDq@}G[uEU_GBuP@cICmA?eI?qCB{FBkCI}BOyCMiAGcAC{AN{YFqD^}FR}CNu@JcAHu@b@_E`@}DVsB^mBTsAQKkCmAg@[YQOIOvAi@[m@e@s@g@GKCKAEJIn@g@GYGIc@ScBoAf@{A`@uAlBfAG`@";
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

        Robot robot = new Robot(journey, 50);
        RobotApplication app = new RobotApplication(robot, particleReader);

        Scheduler robotScheduler = new RealScheduler(1000,0,  TimeUnit.MILLISECONDS);
        robotScheduler.addTask(app::moveRobot);

        Scheduler reportingScheduler = new RealScheduler(15, 15, TimeUnit.MINUTES);
        ReportGeneratorService reportGenerator = new ReportGeneratorService(robot, particleReader, new JsonReportPrinter());
        reportingScheduler.addTask(reportGenerator::generate);

        robotScheduler.start();
        reportingScheduler.start();

        shutdownSchedulerIfRobotFinishedJourney(robot, robotScheduler, reportingScheduler);
    }

    private static void shutdownSchedulerIfRobotFinishedJourney(Robot robot, Scheduler robotScheduler, Scheduler reportingScheduler) {
        if(!robot.notArrivedYet()){
            robotScheduler.stop();
            reportingScheduler.stop();
        }
    }

    private static List<GeoPoint> generateGeoPoints(EncodedPolyline encoder) {
        GeoPointMapper mapper = new GeoPointMapper();
        return mapper.map(encoder.decodePath());
    }
}
