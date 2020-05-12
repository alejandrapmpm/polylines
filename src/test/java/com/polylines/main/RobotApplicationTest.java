package com.polylines.main;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.polylines.application.moverobot.RobotPollutionCollector;
import com.polylines.domain.robot.RobotValidationException;
import com.polylines.domain.robot.GeoPoint;
import com.polylines.domain.robot.Robot;
import com.polylines.domain.observers.Observer;
import com.polylines.domain.observers.SchedulerObserver;
import com.polylines.application.readparticles.RandomParticleReader;
import com.polylines.infraestructure.reportprinting.JsonReportPrinter;
import com.polylines.application.generatereport.ReportGeneratorService;
import com.polylines.application.scheduler.ManualScheduler;
import com.polylines.infraestructure.DistanceCalculator;
import com.polylines.infraestructure.GeoPointMapper;

public class RobotApplicationTest {

    private static final JsonReportPrinter jsonPrinter = new JsonReportPrinter();
    private ManualScheduler robotScheduler;
    private ManualScheduler reportingScheduler;
    private RandomParticleReader particleReader;
    private EncodedPolyline encoder;
    private Random mockRandom = mock(Random.class);
    private static final double METERS_TO_MOVE = 50;

    @Before
    public void setup() {
        robotScheduler = new ManualScheduler();
        reportingScheduler = new ManualScheduler();
        encoder = mock(EncodedPolyline.class);
        particleReader = new RandomParticleReader(mockRandom);
    }

    @Test
    public void whenMetersToMoveAreLessThanDistanceToNextGeoPoint_robotEndsInAnIntermediateGeoPoint()
            throws RobotValidationException {

        mockPolylineWithOnlyTwoPoints7000MetersApart();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 200);
        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

        app.moveRobot();

        GeoPoint initialPosition = journey.get(0);
        assertNotEquals(initialPosition, robot.getCurrentPosition());
        assertEquals(1, app.getNextPosition());
    }

    @Test
    public void whenMetersToMoveAreFurtherThanNextGeoPoint_andNoGeoPointsLeft_robotEndsInLastGeoPoint()
            throws RobotValidationException {

        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 210);
        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

        app.moveRobot();

        GeoPoint lastPosition = journey.get(journey.size() - 1);
        assertEquals(lastPosition, robot.getCurrentPosition());
        assertTrue(robot.atTheEndOfJourney());
    }

    @Test
    public void whenMetersToMoveAreExactlyTheDistanceBetweenTwoGeoPoints_robotEndsInTheSecondGeoPoint()
            throws RobotValidationException {

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        double distance = DistanceCalculator.calculate(
                new GeoPoint(41.84888, -87.63860),
                new GeoPoint(41.84856, -87.63831)); //TODO - Need a better way to mock distances

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, distance);
        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

        app.moveRobot();

        GeoPoint secondPoint = journey.get(1);
        assertEquals(secondPoint, robot.getCurrentPosition());
        assertEquals(2, app.getNextPosition());
    }

    @Test
    public void whenMetersToMoveIsGreaterThanTheSumOfAllDistancesInTheJourney_robotMovesEndsInLastGeoPoint()
            throws RobotValidationException {

        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 1000);
        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

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
        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

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
        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

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
        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

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
    public void whenRobotHasArrivedToLastPosition_robotSchedulerAndReportingSchedulerShouldStop()
            throws RobotValidationException {

        mockPolyLineWithFourPointsWithATotalDistanceOf200Meters();

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 250);

        ManualScheduler spyRobotScheduler = Mockito.spy(robotScheduler);
        ManualScheduler spyReportingScheduler = Mockito.spy(reportingScheduler);

        registerObservers(robot, spyRobotScheduler, spyReportingScheduler);

        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);
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

    private void fire(ManualScheduler timer) {
        timer.start();
        timer.elapseTime();
    }
}
