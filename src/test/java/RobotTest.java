import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import clock.ManualTimer;
import model.GeoPoint;
import model.Level;
import model.Robot;
import reporting.model.Report;
import reporting.printer.JsonReportPrinter;
import reporting.service.ReportGeneratorService;
import reporting.task.ReportGeneratorTask;
import app.ParticleReader;
import app.RobotApplication;
import app.task.RobotMovementTask;
import utilities.DistanceCalculator;
import utilities.GeoPointMapper;

public class RobotTest {

    private ManualTimer robotScheduler;
    private ParticleReader particleReader;
    private EncodedPolyline encoder;
    private static final double METERS_TO_MOVE = 50d;
    private static final String ROBOT_SOURCE_NAME = "ROBOT";
    private final GeoPointMapper mapper = new GeoPointMapper();


    @Before
    public void setup(){
        robotScheduler = new ManualTimer();
        encoder = Mockito.mock(EncodedPolyline.class);
        particleReader = new ParticleReader();
    }
    //ToDO handle empty or wrong polyline

    @Test
    public void whenRobotServiceIsCreated_robotIsCreatedWithCurrentPositionAsTheFirstOneOfThePointsEncoded(){

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831));
        when(encoder.decodePath()).thenReturn(points);

        Robot robot = new Robot(mapper.map(encoder.decodePath()), METERS_TO_MOVE);

        assertEquals(new GeoPoint(41.84888, -87.63860), robot.currentPosition);
    }

    @Test
    public void whenThereIsOnlyOneGeoPoint_robotEndsInThatOne(){

        List<LatLng> points = singletonList(new LatLng(41.84888, -87.63860));
        when(encoder.decodePath()).thenReturn(points);

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, METERS_TO_MOVE);

        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(new GeoPoint(41.84888, -87.63860),
                robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveAreLessThanDistanceToNextGeoPoint_robotEndsInAnIntermediateGeoPoint(){

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //this two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 20d);

        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(new GeoPoint(41.84873092479406, -87.63846490059463),
                robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveAreFurtherThanNextGeoPoint_andNoGeoPointsLeft_robotEndsInLastGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@";

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //these two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 43d);

        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(new GeoPoint(41.84856, -87.63831),
                robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveAreExactlyTheDistanceBetweenTwoGeoPoints_robotEndsInTheSecondGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@";

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //these two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        double distance = DistanceCalculator.calculate(new GeoPoint(41.84888, -87.63860),
                new GeoPoint(41.84856, -87.63831)); //TODO - Revisit calculation for easier use when mocking

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, distance);

        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(new GeoPoint(41.84856, -87.63831),
                robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveIsGreaterThanAllTheDistancesBetweenPoints_moveRobotAlongAllPoints_andEndsInLastGeoPoint(){

/*          String polyline = "orl~Ff|{uO~@y@}A_AEsE";
            Distance is: 42.93135105797141
            Distance is: 58.59883353809855
            Distance is: 87.86280526271533*/
        mockPolylineDecoding();

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 1000d);

        RobotApplication app = new RobotApplication(robot, particleReader);
        app.moveRobot();

        assertEquals(new GeoPoint(41.84906, -87.63693),
                robot.currentPosition);
    }

    @Test
    public void movingTwiceTheRobot_shouldKeepState(){

        /*  String polyline = "orl~Ff|{uO~@y@}A_AEsE";
            Distance is: 42.93135105797141
            Distance is: 58.59883353809855
            Distance is: 87.86280526271533
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
            */

        mockPolylineDecoding();

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 40d);

        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(new GeoPoint(41.84858184958813, -87.63832980118924),
                robot.currentPosition);
        assertEquals(1, app.nextPosition);

        app.moveRobot();

        assertEquals(new GeoPoint(41.848857314177536, -87.63810757332594),
                robot.currentPosition);
        assertEquals(2, app.nextPosition);
    }

    @Test
    public void whenTimerFinishes_robotShouldHaveMoved(){

        mockDecodeLongPolyline();
        List<GeoPoint> journey = mapper.map(encoder.decodePath());

        Robot robot = new Robot(journey, METERS_TO_MOVE);
        RobotApplication app = new RobotApplication(robot, particleReader);
        RobotMovementTask task = new RobotMovementTask(robotScheduler, app);

        fire(robotScheduler);

        assertEquals(new GeoPoint(41.87752475866416, -87.65967737260071), robot.currentPosition);
        assertEquals(1, app.nextPosition);

        GeoPoint origin = new GeoPoint(41.87790000, -87.66001000);
        double distance = DistanceCalculator.calculate(robot.currentPosition, origin);
        assertEquals(METERS_TO_MOVE, distance, 0.1);

    }

    @Test
    public void after100Meters_shouldReadParticles(){

        mockPolylineDecoding();
        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey,  100d);
        RobotApplication app = new RobotApplication(robot, particleReader);

        RobotMovementTask task = new RobotMovementTask(robotScheduler, app);

        fire(robotScheduler);

        assertEquals(1, particleReader.values.size());
    }

    @Test
    public void whenReportingSchedulerFinishes_shouldGenerateAndPrintToConsoleAReportOfParticles() throws IOException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);

        mockPolylineDecoding();

        ParticleReader spyParticleReader = Mockito.spy(particleReader);
        Mockito.doReturn(51).when(spyParticleReader).generateRandomInt();

        GeoPointMapper mapper = new GeoPointMapper();
        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 300d);

        RobotApplication app = new RobotApplication(robot, spyParticleReader);

        ManualTimer reportingScheduler = new ManualTimer();
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, spyParticleReader, new JsonReportPrinter());
        new ReportGeneratorTask(reportingScheduler, reportGeneratorService);

        app.moveRobot();
        fire(reportingScheduler);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Level.Moderate, report.level);
        assertEquals(ROBOT_SOURCE_NAME, report.source);
        assertEquals(41.84906d, report.location.lat, 0.00001);
        assertEquals(-87.63693d, report.location.lng, 0.00001);

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
    }

    @Test
    public void generatingAReport_ShouldNotTakeIntoAccountPreviousReadings(){

        mockDecodeLongPolyline();

        ParticleReader spyParticleReader = Mockito.spy(particleReader);
        Mockito.doReturn(51).doReturn(101).when(spyParticleReader).generateRandomInt();

        GeoPointMapper mapper = new GeoPointMapper();
        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 100d);

        RobotApplication app = new RobotApplication(robot, spyParticleReader);

        ManualTimer reportTimer = new ManualTimer();
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, spyParticleReader, new JsonReportPrinter());
        new ReportGeneratorTask(reportTimer, reportGeneratorService);

        app.moveRobot();
        Report report = reportGeneratorService.generate();
        assertEquals(Level.Moderate, report.level);

        app.moveRobot();
        report = reportGeneratorService.generate();
        assertEquals(Level.USG, report.level);

    }

    private void mockPolylineDecoding() {
        List<LatLng> points = asList(
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
        );
        when(encoder.decodePath()).thenReturn(points);
    }

    private void mockDecodeLongPolyline() {
        //7122 meters apart
        List<LatLng> points = asList(
                new LatLng(41.87790000,-87.66001000),
                new LatLng(41.82445000,-87.61263000)
        );
        when(encoder.decodePath()).thenReturn(points);
    }

    @Test
    public void whenNoParticlesRead_AverageIsStillZero() throws IOException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);

        List<LatLng> points = singletonList(new LatLng(41.84888, -87.63860000000001));
        when(encoder.decodePath()).thenReturn(points);

        ParticleReader spyParticleReader = Mockito.spy(particleReader);

        GeoPointMapper mapper = new GeoPointMapper();
        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 2d);

        RobotApplication app = new RobotApplication(robot, spyParticleReader);

        ManualTimer reportTimer = new ManualTimer();

        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, new JsonReportPrinter());
        ReportGeneratorTask reportGeneratorTask = new ReportGeneratorTask(reportTimer, reportGeneratorService);

        fire(reportTimer);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Level.Good, report.level);
        verify(spyParticleReader, never()).run();

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
    }

    private Report mapWrittenOutputToReport(ByteArrayOutputStream outContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(outContent.toString(), Report.class);
    }

    private void leaveSystemOutAsItWasBefore(PrintStream originalOut, PrintStream originalErr) {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void mockSystemOut(ByteArrayOutputStream outContent, ByteArrayOutputStream errContent) {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    private void fire(ManualTimer timer) {
        timer.start();
        timer.elapseTime();
    }
}
