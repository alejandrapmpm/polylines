package com.polylines.main;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.polylines.app.ParticleReader;
import com.polylines.app.RobotApplication;
import com.polylines.exception.RobotValidationException;
import com.polylines.model.GeoPoint;
import com.polylines.model.Robot;
import com.polylines.observers.Observer;
import com.polylines.observers.SchedulerObserver;
import com.polylines.reporting.model.Report;
import com.polylines.reporting.printer.JsonReportPrinter;
import com.polylines.reporting.service.ReportGeneratorService;
import com.polylines.scheduler.ManualScheduler;
import com.polylines.utilities.DistanceCalculator;
import com.polylines.utilities.GeoPointMapper;

public class RobotTest {

    private static final JsonReportPrinter jsonPrinter = new JsonReportPrinter();
    private ManualScheduler robotScheduler;
    private ManualScheduler reportingScheduler;
    private ParticleReader particleReader;
    private EncodedPolyline encoder;
    private static final double METERS_TO_MOVE = 50;
    private static final String ROBOT_SOURCE_NAME = "ROBOT";
    private final GeoPointMapper mapper = new GeoPointMapper();
    private Random mockRandom = Mockito.mock(Random.class);

    @Before
    public void setup() {
        robotScheduler = new ManualScheduler();
        reportingScheduler = new ManualScheduler();
        encoder = Mockito.mock(EncodedPolyline.class);
        particleReader = new ParticleReader(mockRandom);
    }

    @Test
    public void whenRobotServiceIsCreated_robotIsCreatedWithCurrentPositionAsTheFirstOneOfThePointsEncoded() throws RobotValidationException {

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        Robot robot = new Robot(mapper.map(encoder.decodePath()), METERS_TO_MOVE);

        assertEquals(41.84888, robot.getCurrentPosition().lat, 0.00001);
        assertEquals(-87.63860, robot.getCurrentPosition().lng, 0.00001);
    }

    @Test(expected = RobotValidationException.class)
    public void whenThereIsOnlyOneGeoPoint_robotStaysInCurrentInitialPosition() throws RobotValidationException {

        new Robot(singletonList(new GeoPoint(41.84888, -87.63860)), METERS_TO_MOVE);
    }

    @Test(expected = RobotValidationException.class)
    public void whenEmptyGeoPoints_robotStaysInCurrentInitialPosition() throws RobotValidationException {

        new Robot(emptyList(), METERS_TO_MOVE);
    }

    @Test
    public void whenMetersToMoveAreLessThanDistanceToNextGeoPoint_robotEndsInAnIntermediateGeoPoint() throws RobotValidationException {

        //these two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 20);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(41.84873, robot.getCurrentPosition().lat, 0.00001);
        assertEquals(-87.63846, robot.getCurrentPosition().lng, 0.00001);
        assertEquals(1, app.nextPosition);
    }

    @Test
    public void whenMetersToMoveAreFurtherThanNextGeoPoint_andNoGeoPointsLeft_robotEndsInLastGeoPoint() throws RobotValidationException {

        //String polyline = "orl~Ff|{uO~@y@";

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 43);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(41.84856, robot.getCurrentPosition().lat, 0.00001);
        assertEquals(-87.63831, robot.getCurrentPosition().lng, 0.00001);
    }

    @Test
    public void whenMetersToMoveAreExactlyTheDistanceBetweenTwoGeoPoints_robotEndsInTheSecondGeoPoint() throws RobotValidationException {

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

        assertEquals(41.84856, robot.getCurrentPosition().lat, 0.00001);
        assertEquals(-87.63831, robot.getCurrentPosition().lng, 0.00001);
    }

    @Test
    public void whenMetersToMoveIsGreaterThanTheSumOfAllDistancesInTheJourney_robotMovesEndsInLastGeoPoint() throws RobotValidationException {

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

        assertEquals(41.84906, robot.getCurrentPosition().lat, 0.00001);
        assertEquals(-87.63693, robot.getCurrentPosition().lng, 0.00001);
    }

    @Test
    public void movingTwiceTheRobot_shouldMoveRobotAlongPolyline() throws RobotValidationException {

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

        assertEquals(41.84858, robot.getCurrentPosition().lat, 0.00001);
        assertEquals(-87.63832, robot.getCurrentPosition().lng, 0.00001);
        assertEquals(1, app.nextPosition);

        app.moveRobot();

        assertEquals(41.84885, robot.getCurrentPosition().lat, 0.00001);
        assertEquals(-87.63810, robot.getCurrentPosition().lng, 0.00001);
        assertEquals(2, app.nextPosition);
    }

    @Test
    public void whenRobotSchedulerFires_robotShouldMove() throws RobotValidationException {

        mockDecodeLongPolyline();

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, METERS_TO_MOVE);
        RobotApplication app = new RobotApplication(robot, particleReader);

        robotScheduler.addTask(app::moveRobot);

        fire(robotScheduler);

        assertEquals(41.87752, robot.getCurrentPosition().lat, 0.00001);
        assertEquals(-87.65967, robot.getCurrentPosition().lng, 0.00001);

        assertEquals(1, app.nextPosition);
    }

    @Test
    public void after100Meters_shouldReadParticles() throws RobotValidationException {

        mockPolylineDecoding();
        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 100);
        RobotApplication app = new RobotApplication(robot, particleReader);
        robotScheduler.addTask(app::moveRobot);

        Integer expectedRandom = 50;
        when(mockRandom.nextInt(eq(200))).thenReturn(expectedRandom);

        fire(robotScheduler);

        assertEquals(1, particleReader.values.size());
        assertEquals(expectedRandom, particleReader.values.get(0));
    }

    @Test
    public void whenReportingSchedulerFires_shouldGenerateAndPrintToConsoleAReportOfParticles() throws IOException, RobotValidationException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);
        mockPolylineDecoding();

        when(mockRandom.nextInt(eq(200))).thenReturn(51);

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 300);
        RobotApplication app = new RobotApplication(robot, particleReader);

        ManualScheduler reportingScheduler = new ManualScheduler();
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, jsonPrinter);
        reportingScheduler.addTask(reportGeneratorService::generate);

        app.moveRobot();

        fire(reportingScheduler);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Report.Level.Moderate, report.getLevel());
        assertEquals(ROBOT_SOURCE_NAME, report.getSource());
        assertEquals(41.84906d, report.getLocation().getLat(), 0.00001);
        assertEquals(-87.63693d, report.getLocation().getLng(), 0.00001);

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
    }

    @Test
    public void generatingAReport_ShouldNotTakeIntoAccountPreviousReadings() throws RobotValidationException {

        mockDecodeLongPolyline();

        when(mockRandom.nextInt(eq(200))).thenReturn(50).thenReturn(100).thenReturn(150).thenReturn(250);

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 100);

        RobotApplication app = new RobotApplication(robot, particleReader);

        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, jsonPrinter);

        app.moveRobot(); // The particles reader generates 50 - which is Moderate level

        Report report = reportGeneratorService.generate();

        assertEquals(Report.Level.Good, report.getLevel());

        app.moveRobot(); // The particles reader generates 100 - which is Moderate level

        report = reportGeneratorService.generate();

        assertEquals(Report.Level.Moderate, report.getLevel());

        app.moveRobot(); // The particles reader generates 150 - which is USG level

        report = reportGeneratorService.generate();

        assertEquals(Report.Level.USG, report.getLevel());

        app.moveRobot(); // The particles reader generates 250 - which is Unhealthy level

        report = reportGeneratorService.generate();

        assertEquals(Report.Level.Unhealthy, report.getLevel());
    }

    @Test
    public void whenReportingSchedulerFiresAndNoParticlesRead_AverageIsStillZero() throws IOException, RobotValidationException {

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
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, jsonPrinter);
        reportingScheduler.addTask(reportGeneratorService::generate);

        app.moveRobot();
        fire(reportingScheduler);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Report.Level.Good, report.getLevel());
        verify(spyParticleReader, never()).run();

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
    }

    @Test
    public void whenRobotHasArrivedToLastPosition_RobotSchedulerAndReportingSchedulerShouldStop() throws RobotValidationException {

        //String polyline = "orl~Ff|{uO~@y@";

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        List<GeoPoint> journey = mapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 43);

        ManualScheduler spyRobotScheduler = Mockito.spy(robotScheduler);
        ManualScheduler spyReportingScheduler = Mockito.spy(reportingScheduler);

        registerObservers(robot, spyRobotScheduler, spyReportingScheduler);

        RobotApplication app = new RobotApplication(robot, particleReader);
        spyRobotScheduler.addTask(app::moveRobot);

        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, jsonPrinter);
        spyReportingScheduler.addTask(reportGeneratorService::generate);

        fire(spyRobotScheduler);
        fire(reportingScheduler);

        assertTrue(robot.atTheEndOfJourney());

        verify(spyRobotScheduler).stop();
        verify(spyReportingScheduler).stop();
    }

    private void registerObservers(Robot robot, ManualScheduler spyRobotScheduler, ManualScheduler spyReportingScheduler) {
        Observer robotObserver = new SchedulerObserver(spyRobotScheduler);
        Observer reportingObserver = new SchedulerObserver(spyReportingScheduler);
        robot.registerObserver(robotObserver);
        robot.registerObserver(reportingObserver);
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
                new LatLng(41.87790000, -87.66001000),
                new LatLng(41.82445000, -87.61263000)
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
