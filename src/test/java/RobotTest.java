import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import model.GeoPoint;
import service.RobotMovementService;
import utilities.DistanceCalculator;

public class RobotTest {

    //ToDO handle empty or wrong polyline
    @Test
    public void whenRobotServiceIsCreated_decodePathIsCalled(){

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        when(encoder.decodePath()).thenReturn(singletonList(new LatLng(41.84888, -87.63860)));

        new RobotMovementService(encoder);

        verify(encoder).decodePath();
    }

    @Test
    public void whenRobotServiceIsCreated_robotIsCreatedWithCurrentPositionAsTheFirstOneOfThePointsEncoded(){

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831));
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder);

        assertEquals(new GeoPoint(41.84888, -87.63860), robotMovementService.robot.currentPosition);
    }

    @Test
    public void whenThereIsOnlyOneGeoPoint_robotEndsInThatOne(){

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        List<LatLng> points = singletonList(new LatLng(41.84888, -87.63860));
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder);
        robotMovementService.moveRobot(1);

        assertEquals(new GeoPoint(41.84888, -87.63860),
                robotMovementService.robot.currentPosition);
    }


    @Test
    public void whenMetersToMoveAreLessThanDistanceToNextGeoPoint_robotEndsInAnIntermediateGeoPoint(){

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //this two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder);
        robotMovementService.moveRobot(20);

        assertEquals(new GeoPoint(41.84873092479406, -87.63846490059463),
                robotMovementService.robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveAreFurtherThanNextGeoPoint_andNoGeoPointsLeft_robotEndsInLastGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@";

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //these two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder);
        robotMovementService.moveRobot(43);

        assertEquals(new GeoPoint(41.84856, -87.63831),
                robotMovementService.robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveAreExactlyTheDistanceBetweenTwoGeoPoints_robotEndsInTheSecondGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@";

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)); //these two points are 42.9 meters far from each other
        when(encoder.decodePath()).thenReturn(points);

        double distance = DistanceCalculator.distance(new GeoPoint(41.84888, -87.63860),
                new GeoPoint(41.84856, -87.63831)); //Not sure how I feel about this- Maybe its easier to mock this utilityg

        RobotMovementService robotMovementService = new RobotMovementService(encoder);
        robotMovementService.moveRobot(distance);

        assertEquals(new GeoPoint(41.84856, -87.63831),
                robotMovementService.robot.currentPosition);
    }

    @Test
    public void whenMetersToMoveIsGreaterThanAllTheDistancesBetweenPoints_moveRobotAlongAllPoints_andEndsInLastGeoPoint(){

        //String polyline = "orl~Ff|{uO~@y@}A_AEsE";
        //[GeoPoint{lat=41.84888, lng=-87.63860000000001},
        // GeoPoint{lat=41.848560000000006, lng=-87.63831},
        // GeoPoint{lat=41.849030000000006, lng=-87.63799},
        // GeoPoint{lat=41.84906, lng=-87.63693}]
        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        List<LatLng> points = asList(
                new LatLng(41.84888, -87.63860000000001),
                new LatLng(41.84856, -87.63831),
                new LatLng(41.84903, -87.63799),
                new LatLng(41.84906, -87.63693)
                );
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder);
        robotMovementService.moveRobot(1000);

        assertEquals(new GeoPoint(41.84906, -87.63693),
                robotMovementService.robot.currentPosition);
        assertTrue(robotMovementService.robot.journey.stream().allMatch(GeoPoint::isVisited));
    }
}
