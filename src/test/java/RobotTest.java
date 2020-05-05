import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
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
import reporting.Report;
import reporting.ReportGenerator;
import reporting.printer.ConsoleReportPrinter;
import service.ParticleReader;
import service.RobotMovementService;
import utilities.DistanceCalculator;

public class RobotTest {

    private ManualTimer robotTimer;
    private ParticleReader particleReader;
    private EncodedPolyline encoder;
    private static final double METERS_TO_MOVE = 50d;

    @Before
    public void setup(){
        robotTimer = new ManualTimer();
        encoder = Mockito.mock(EncodedPolyline.class);
        particleReader = new ParticleReader();
    }
    //ToDO handle empty or wrong polyline
    @Test
    public void whenRobotServiceIsCreated_decodePathIsCalled(){

        when(encoder.decodePath()).thenReturn(singletonList(new LatLng(41.84888, -87.63860)));

        new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);

        verify(encoder).decodePath();
    }

    @Test
    public void whenRobotServiceIsCreated_robotIsCreatedWithCurrentPositionAsTheFirstOneOfThePointsEncoded(){

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831));
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);

        assertEquals(new GeoPoint(41.84888, -87.63860), robotMovementService.robot.currentPosition);
    }

    @Test
    public void whenThereIsOnlyOneGeoPoint_robotEndsInThatOne(){

        List<LatLng> points = singletonList(new LatLng(41.84888, -87.63860));
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);
        robotMovementService.moveRobot(1);

        assertEquals(new GeoPoint(41.84888, -87.63860),
                robotMovementService.robot.currentPosition);
    }


    @Test
    public void whenMetersToMoveAreLessThanDistanceToNextGeoPoint_robotEndsInAnIntermediateGeoPoint(){

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //this two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);
        robotMovementService.moveRobot(20);

        assertEquals(new GeoPoint(41.84873092479406, -87.63846490059463),
                robotMovementService.robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveAreFurtherThanNextGeoPoint_andNoGeoPointsLeft_robotEndsInLastGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@";

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //these two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);
        robotMovementService.moveRobot(43);

        assertEquals(new GeoPoint(41.84856, -87.63831),
                robotMovementService.robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveAreExactlyTheDistanceBetweenTwoGeoPoints_robotEndsInTheSecondGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@";

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //these two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        double distance = DistanceCalculator.calculate(new GeoPoint(41.84888, -87.63860),
                new GeoPoint(41.84856, -87.63831)); //Not sure how I feel about this- Maybe its easier to mock this utilityg

        RobotMovementService robotMovementService = new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);
        robotMovementService.moveRobot(distance);

        assertEquals(new GeoPoint(41.84856, -87.63831),
                robotMovementService.robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveIsGreaterThanAllTheDistancesBetweenPoints_moveRobotAlongAllPoints_andEndsInLastGeoPoint(){

/*          String polyline = "orl~Ff|{uO~@y@}A_AEsE";
            Distance is: 42.93135105797141
            Distance is: 58.59883353809855
            Distance is: 87.86280526271533*/
        List<LatLng> points = asList(
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
                );
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);
        robotMovementService.moveRobot(1000);

        assertEquals(new GeoPoint(41.84906, -87.63693),
                robotMovementService.robot.currentPosition);
    }

    @Test
    public void movingTwiceTheRobot_shouldKeepState(){

        /*  String polyline = "orl~Ff|{uO~@y@}A_AEsE";
            Distance is: 42.93135105797141
            Distance is: 58.59883353809855
            Distance is: 87.86280526271533*/

        List<LatLng> points = asList(
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
        );
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);
        robotMovementService.moveRobot(40);

        assertEquals(new GeoPoint(41.84858184958813, -87.63832980118924),
                robotMovementService.robot.currentPosition);
        assertEquals(1, robotMovementService.nextPosition);

        robotMovementService.moveRobot(100);

        assertEquals(new GeoPoint(41.84904313518723, -87.6375258900511),
                robotMovementService.robot.currentPosition);
        assertEquals(3, robotMovementService.nextPosition);
    }

    @Test
    public void whenTimerFinishes_robotShouldHavePerformedTheTask(){

        /*String polyline = "orl~Ff|{uO~@y@}A_AEsE";
        *   Distance is: 42.93135105797141
            Distance is: 58.59883353809855
            Distance is: 87.86280526271533
        * */
        List<LatLng> points = asList(
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
        );
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder, METERS_TO_MOVE, robotTimer, particleReader);

        fireTimer(robotTimer);

        assertEquals(new GeoPoint(41.84861669507057, -87.63827139910089),
                robotMovementService.robot.currentPosition);
        assertEquals(2, robotMovementService.nextPosition);

    }

    @Test
    public void every100MetersShouldReadParticles(){

        /*String polyline = "orl~Ff|{uO~@y@}A_AEsE";
        *   Distance is: 42.93135105797141
            Distance is: 58.59883353809855
            Distance is: 87.86280526271533
        * */
        List<LatLng> points = asList(
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
        );
        when(encoder.decodePath()).thenReturn(points);


        RobotMovementService robotMovementService = new RobotMovementService(encoder, 300d, robotTimer, particleReader);

        fireTimer(robotTimer);

        assertEquals(1, particleReader.values.size());
        System.out.println("Delta moved: " + robotMovementService.metersMoved);
    }

    @Test
    public void every15MinShouldGenerateAReportOfParticles() throws IOException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);

        List<LatLng> points = asList(
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
        );
        when(encoder.decodePath()).thenReturn(points);

        ParticleReader spyParticleReader = Mockito.spy(particleReader);
        Mockito.doReturn(51).when(spyParticleReader).generateRandomInt();

        RobotMovementService robotMovementService = new RobotMovementService(encoder, 300d, robotTimer, spyParticleReader);
        ManualTimer reportTimer = new ManualTimer();

        new ReportGenerator(robotMovementService.robot, spyParticleReader, reportTimer, new ConsoleReportPrinter());

        fireTimer(robotTimer);
        fireTimer(reportTimer);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Level.Moderate, report.level);
        assertEquals("ROBOT", report.source);
        assertEquals(41.84906d, report.location.lat, 0.00001);
        assertEquals(-87.63693d, report.location.lng, 0.00001);

        assertEquals(1, particleReader.values.size());

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
    }

    @Test
    public void whenNoParticlesRead_AverageIsStillZero() throws IOException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);

        List<LatLng> points = singletonList(
                new LatLng(41.84888, -87.63860000000001)
        );
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder, 2d, robotTimer, new ParticleReader());

        ManualTimer reportTimer = new ManualTimer();
        new ReportGenerator(robotMovementService.robot, particleReader, reportTimer, new ConsoleReportPrinter());

        fireTimer(reportTimer);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Level.Good, report.level);
        assertEquals("ROBOT", report.source);
        assertEquals(41.84888d, report.location.lat, 0.00001);
        assertEquals(-87.63860d, report.location.lng, 0.00001);

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

    private void fireTimer(ManualTimer reportTimer) {
        reportTimer.start();
        reportTimer.elapseTime();
    }
}
