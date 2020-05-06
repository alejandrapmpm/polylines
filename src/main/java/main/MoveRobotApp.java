package main;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.maps.model.EncodedPolyline;
import app.ParticleReader;
import app.RobotApplication;
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

    public static void main(String [] args){
        String polyline = "mpjyHx`i@VjAVKnAh@BHHX@LZR@Bj@Ml@WWc@]w@bAyAfBmCb@o@pLeQfCsDVa@@ODQR}AJ{A?{BGuAD_@FKb@MTUX]Le@" +
                "^kBVcAVo@Ta@|EaFh@m@FWaA{DCo@q@mCm@cC{A_GWeA}@sGSeAcA_EOSMa@}A_GsAwFkAiEoAaFaBoEGo@]_AIWW{AQyAUyBQqAI_BF" +
                "kEd@aHZcDlAyJLaBPqDDeD?mBEiA}@F]yKWqGSkICmCIeZIuZi@_Sw@{WgAoXS{DOcAWq@KQGIFQDGn@Y`@MJEFIHyAVQVOJGHgFRJBB" +
                "CCSKBcAKoACyA?m@^yVJmLJ{FGGWq@e@eBIe@Ei@?q@Bk@Hs@Le@Rk@gCuIkJcZsDwLd@g@Oe@o@mB{BgHQYq@qBQYOMSMGBUBGCYc@E" +
                "_@H]DWJST?JFFHBDNBJ?LED?LBv@WfAc@@EDGNK|@e@hAa@`Bk@b@OEk@Go@IeACoA@a@PyB`@yDDc@e@K{Bi@oA_@w@]m@_@]QkBoAw" +
                "C{BmAeAo@s@uAoB_AaBmAwCa@mAo@iCgAwFg@iDq@}G[uEU_GBuP@cICmA?eI?qCB{FBkCI}BOyCMiAGcAC{AN{YFqD^}FR}CNu@JcAH" +
                "u@b@_E`@}DVsB^mBTsAQKkCmAg@[YQOIOvAi@[m@e@s@g@GKCKAEJIn@g@GYGIc@ScBoAf@{A`@uAlBfAG`@";

        EncodedPolyline encoder = new EncodedPolyline(polyline);
        List<GeoPoint> journey = generateGeoPoints(encoder);

        Scheduler robotScheduler = new RealScheduler(1,0,  TimeUnit.SECONDS);
        Scheduler reportingScheduler = new RealScheduler(15 , 15, TimeUnit.MINUTES);

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
