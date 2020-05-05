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
import clock.ManualScheduler;
import model.GeoPoint;
import model.Level;
import model.Robot;
import reporting.model.Report;
import reporting.printer.JsonReportPrinter;
import reporting.service.ReportGeneratorService;
import app.ParticleReader;
import app.RobotApplication;
import utilities.DistanceCalculator;
import utilities.GeoPointMapper;

public class RobotTest {

    private ManualScheduler robotScheduler;
    private ParticleReader particleReader;
    private EncodedPolyline encoder;
    private static final double METERS_TO_MOVE = 50;
    private static final String ROBOT_SOURCE_NAME = "ROBOT";
    private final GeoPointMapper mapper = new GeoPointMapper();


    @Before
    public void setup(){
        robotScheduler = new ManualScheduler();
        encoder = Mockito.mock(EncodedPolyline.class);
        particleReader = new ParticleReader();
    }
    //ToDO handle empty or wrong polyline

    @Test
    public void whenRobotServiceIsCreated_robotIsCreatedWithCurrentPositionAsTheFirstOneOfThePointsEncoded(){

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        Robot robot = new Robot(mapper.map(encoder.decodePath()), METERS_TO_MOVE);

        assertEquals(41.84888, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63860, robot.currentPosition.lng, 0.00001);
    }

    @Test
    public void whenThereIsOnlyOneGeoPoint_robotStaysInCurrentInitialPosition(){

        when(encoder.decodePath()).thenReturn(singletonList(new LatLng(41.84888, -87.63860)));

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, METERS_TO_MOVE);

        assertEquals(41.84888, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63860, robot.currentPosition.lng, 0.00001);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(41.84888, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63860, robot.currentPosition.lng, 0.00001);
    }

    @Test
    public void whenMetersToMoveAreLessThanDistanceToNextGeoPoint_robotEndsInAnIntermediateGeoPoint(){

        //these two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 20);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(41.84873, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63846, robot.currentPosition.lng, 0.00001);
        assertEquals(1, app.nextPosition);
    }

    @Test
    public void whenMetersToMoveAreFurtherThanNextGeoPoint_andNoGeoPointsLeft_robotEndsInLastGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@";

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 43d);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(41.84856, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63831, robot.currentPosition.lng, 0.00001);
    }

    @Test
    public void whenMetersToMoveAreExactlyTheDistanceBetweenTwoGeoPoints_robotEndsInTheSecondGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@";

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        double distance = DistanceCalculator.calculate(
                new GeoPoint(41.84888, -87.63860),
                new GeoPoint(41.84856, -87.63831)); //TODO - Revisit calculation for easier use when mocking

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, distance);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(41.84856, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63831, robot.currentPosition.lng, 0.00001);
    }

    @Test
    public void whenMetersToMoveIsGreaterThanTheSumOfAllDistancesInTheJourney_robotMovesEndsInLastGeoPoint(){

        /*  String polyline = "orl~Ff|{uO~@y@}A_AEsE";
            Distance is: 42.93135105797141
            Distance is: 58.59883353809855
            Distance is: 87.86280526271533
            */
        mockPolylineDecoding();

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 1000);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(41.84906, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63693, robot.currentPosition.lng, 0.00001);
    }

    @Test
    public void movingTwiceTheRobot_shouldMoveRobotAlongPolyline(){

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
        Robot robot = new Robot(journey, 40);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(41.84858, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63832, robot.currentPosition.lng, 0.00001);
        assertEquals(1, app.nextPosition);

        app.moveRobot();

        assertEquals(41.84885, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.63810, robot.currentPosition.lng, 0.00001);
        assertEquals(2, app.nextPosition);
    }

    @Test
    public void whenRobotSchedulerFires_robotShouldMove(){

        mockDecodeLongPolyline();

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, METERS_TO_MOVE);
        RobotApplication app = new RobotApplication(robot, particleReader);

        robotScheduler.addTask(app::moveRobot);

        fire(robotScheduler);

        assertEquals(41.87752, robot.currentPosition.lat, 0.00001);
        assertEquals(-87.65967, robot.currentPosition.lng, 0.00001);

        assertEquals(1, app.nextPosition);
    }

    @Test
    public void after100Meters_shouldReadParticles(){

        mockPolylineDecoding();
        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey,  100);
        RobotApplication app = new RobotApplication(robot, particleReader);
        robotScheduler.addTask(app::moveRobot);

        fire(robotScheduler);

        assertEquals(1, particleReader.values.size());
    }

    @Test
    public void whenReportingSchedulerFires_shouldGenerateAndPrintToConsoleAReportOfParticles() throws IOException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);
        mockPolylineDecoding();

        ParticleReader spyParticleReader = Mockito.spy(particleReader);
        Mockito.doReturn(51).when(spyParticleReader).generateRandomInt();

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 300);
        RobotApplication app = new RobotApplication(robot, spyParticleReader);

        ManualScheduler reportingScheduler = new ManualScheduler();
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, spyParticleReader, new JsonReportPrinter());
        reportingScheduler.addTask(reportGeneratorService::generate);

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

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 100);

        RobotApplication app = new RobotApplication(robot, spyParticleReader);

        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, spyParticleReader, new JsonReportPrinter());

        app.moveRobot(); // The particles reader generates 51 - which is Moderate level

        Report report = reportGeneratorService.generate();

        assertEquals(Level.Moderate, report.level);

        app.moveRobot(); // The particles reader generates 101 - which is USG level

        report = reportGeneratorService.generate();

        assertEquals(Level.USG, report.level);
    }

    @Test
    public void whenReportingSchedulerFiresAndNoParticlesRead_AverageIsStillZero() throws IOException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);

        mockDecodeLongPolyline();

        ParticleReader spyParticleReader = Mockito.spy(particleReader);

        GeoPointMapper mapper = new GeoPointMapper();
        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 99);

        RobotApplication app = new RobotApplication(robot, spyParticleReader);

        ManualScheduler reportingScheduler = new ManualScheduler();
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, new JsonReportPrinter());
        reportingScheduler.addTask(reportGeneratorService::generate);

        app.moveRobot();
        fire(reportingScheduler);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Level.Good, report.level);
        verify(spyParticleReader, never()).run();

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
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

    private void fire(ManualScheduler timer) {
        timer.start();
        timer.elapseTime();
    }
}
