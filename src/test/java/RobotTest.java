import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import model.GeoPoint;
import service.RobotMovementService;

public class RobotTest {

    //ToDO handle empty or wrong polyline
    @Test
    public void whenRobotServiceIsCreated_DecodePathIsCalled(){

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        when(encoder.decodePath()).thenReturn(singletonList(new LatLng(41.84888, -87.63860)));

        new RobotMovementService(encoder);

        verify(encoder).decodePath();
    }

    @Test
    public void whenRobotServiceIsCreated_ARobotIsCreatedWithCurrentPositionBeingTheFirstOneEncoded(){

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);

        List<LatLng> points = asList(new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831));
        when(encoder.decodePath()).thenReturn(points);

        RobotMovementService robotMovementService = new RobotMovementService(encoder);

        assertEquals(new GeoPoint(41.84888, -87.63860), robotMovementService.robot.currentPosition);
    }

    @Test
    public void moveRobotWhenMetersToMoveAreLessThanDistanceToNextGeoPoint_ThenTheRobotMovesToAnIntermediateGeoPoint(){

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
    public void moveRobotWhenMetersToMoveAreFurtherThanNextGeoPoint_AndNoGeoPointsLeft_ThenRobotEndsInLastGeoPoint(){

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
}
