package com.polylines.main;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
    private Random mockRandom = Mockito.mock(Random.class);
    private static final double METERS_TO_MOVE = 50;
    private static final String ROBOT_SOURCE_NAME = "ROBOT";

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

        Robot robot = new Robot(GeoPointMapper.map(encoder.decodePath()), METERS_TO_MOVE);

        assertEquals(new GeoPoint(41.84888, -87.63860), robot.getCurrentPosition());
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

        mockPolylineWithOnlyTwoPoints7000MetersApart();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 200);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        GeoPoint initialPosition = journey.get(0);
        assertNotEquals(initialPosition, robot.getCurrentPosition());
        assertEquals(1, app.getNextPosition());
    }

    @Test
    public void whenMetersToMoveAreFurtherThanNextGeoPoint_andNoGeoPointsLeft_robotEndsInLastGeoPoint() throws RobotValidationException {

        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 210);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        GeoPoint lastPosition = journey.get(journey.size() - 1);
        assertEquals(lastPosition, robot.getCurrentPosition());
        assertTrue(robot.atTheEndOfJourney());
    }

    @Test
    public void whenMetersToMoveAreExactlyTheDistanceBetweenTwoGeoPoints_robotEndsInTheSecondGeoPoint() throws RobotValidationException {

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        double distance = DistanceCalculator.calculate(
                new GeoPoint(41.84888, -87.63860),
                new GeoPoint(41.84856, -87.63831)); //TODO - Need a better way to mock distances

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, distance);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        GeoPoint secondPoint = journey.get(1);
        assertEquals(secondPoint, robot.getCurrentPosition());
        assertEquals(2, app.getNextPosition());
    }

    @Test
    public void whenMetersToMoveIsGreaterThanTheSumOfAllDistancesInTheJourney_robotMovesEndsInLastGeoPoint() throws RobotValidationException {

        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 1000);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        GeoPoint lastPosition = journey.get(journey.size() - 1);
        assertEquals(lastPosition, robot.getCurrentPosition());
        assertTrue(robot.atTheEndOfJourney());
    }

    @Test
    public void movingTwiceTheRobot_shouldMoveRobotAlongPolyline() throws RobotValidationException {

        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 40);
        RobotApplication app = new RobotApplication(robot, particleReader);

        app.moveRobot();

        assertEquals(new GeoPoint(41.84858184958813, -87.63832980118924), robot.getCurrentPosition());
        assertEquals(1, app.getNextPosition());

        app.moveRobot();

        assertEquals(new GeoPoint(41.848857314177536, -87.63810757332594), robot.getCurrentPosition());
        assertEquals(2, app.getNextPosition());
    }

    @Test
    public void whenRobotSchedulerFires_robotShouldMoveFromCurrentPositionToAnotherPoint() throws RobotValidationException {

        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, METERS_TO_MOVE);
        RobotApplication app = new RobotApplication(robot, particleReader);

        robotScheduler.addTask(app::moveRobot);

        fire(robotScheduler);

        assertNotEquals(journey.get(0), robot.getCurrentPosition());
        assertEquals(2, app.getNextPosition());
        assertFalse(robot.atTheEndOfJourney());
    }

    @Test
    public void each100Meters_shouldReadParticles() throws RobotValidationException {

        mockPolylineWithOnlyTwoPoints7000MetersApart();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 40);
        RobotApplication app = new RobotApplication(robot, particleReader);

        //Moving 120 meters in total
        app.moveRobot();
        app.moveRobot();
        app.moveRobot();

        assertEquals(1, particleReader.values.size());

        //Moving 60 meters
        app.moveRobot();
        app.moveRobot();

        assertEquals(2, particleReader.values.size());
    }

    @Test
    public void whenReportingSchedulerFires_shouldGenerateAndPrintToConsoleAReportOfParticles()
            throws IOException, RobotValidationException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);
        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        when(mockRandom.nextInt(eq(200))).thenReturn(51);

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 100);
        RobotApplication app = new RobotApplication(robot, particleReader);

        ManualScheduler reportingScheduler = new ManualScheduler();
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, jsonPrinter);
        reportingScheduler.addTask(reportGeneratorService::generate);

        app.moveRobot();

        fire(reportingScheduler);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Report.Level.Moderate, report.getLevel());
        assertEquals(ROBOT_SOURCE_NAME, report.getSource());
        assertEquals(41.84901, report.getLocation().getLat(), 0.00001);
        assertEquals(-87.63799, report.getLocation().getLng(), 0.00001);

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
    }

    @Test
    public void generatingAReport_shouldNotTakeIntoAccountPreviousReadings() throws RobotValidationException {

        mockPolylineWithOnlyTwoPoints7000MetersApart();

        when(mockRandom.nextInt(eq(200))).thenReturn(50).thenReturn(100).thenReturn(150).thenReturn(250);

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
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
    public void whenReportingSchedulerFiresAndNoParticlesRead_reportIsGeneratedButAverageIsStillZero()
            throws IOException, RobotValidationException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);

        mockPolylineWithOnlyTwoPoints7000MetersApart();

        ParticleReader spyParticleReader = Mockito.spy(particleReader);

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
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
    public void whenRobotHasArrivedToLastPosition_robotSchedulerAndReportingSchedulerShouldStop() throws RobotValidationException {

        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 250);

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

    private void mockPolyLineWithFourPointsWithATotalDistanceOf200Meters() {
         /*     String polyline = "orl~Ff|{uO~@y@}A_AEsE";
                Distance is: 42.93135105797141
                Distance is: 58.59883353809855
                Distance is: 87.86280526271533
                Point 1: 41.84888, -87.63860000000001),
                Point 2: 41.84856, -87.63831),
                Point 3: 41.84903, -87.63799),
                Point 4: (41.84906, -87.63693)
         */
        List<LatLng> points = asList(
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
        );
        when(encoder.decodePath()).thenReturn(points);
    }

    private void mockPolylineWithOnlyTwoPoints7000MetersApart() {
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
