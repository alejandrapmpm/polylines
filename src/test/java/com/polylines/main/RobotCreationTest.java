package com.polylines.main;

import static com.polylines.domain.robot.GeoPointBuilder.aGeoPoint;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.polylines.domain.robot.Robot;
import com.polylines.domain.robot.RobotValidationException;
import com.polylines.infraestructure.GeoPointMapper;

public class RobotCreationTest {

    private EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);
    private static final double METERS_TO_MOVE = 50;

    @Test
    public void whenRobotServiceIsCreated_robotIsCreatedWithCurrentPositionAsTheFirstOneOfThePointsEncoded()
            throws RobotValidationException {

        when(encoder.decodePath()).thenReturn(asList(
                new LatLng(41.84888, -87.63860),
                new LatLng(41.84856, -87.63831)));

        Robot robot = new Robot(GeoPointMapper.map(encoder.decodePath()), METERS_TO_MOVE);

        assertEquals(aGeoPoint(41.84888, -87.63860), robot.getCurrentPosition());
    }

    @Test(expected = RobotValidationException.class)
    public void whenThereIsOnlyOneGeoPoint_robotStaysInCurrentInitialPosition() throws RobotValidationException {

        new Robot(singletonList(aGeoPoint(41.84888, -87.63860)), METERS_TO_MOVE);
    }

    @Test(expected = RobotValidationException.class)
    public void whenEmptyGeoPoints_robotStaysInCurrentInitialPosition() throws RobotValidationException {

        new Robot(emptyList(), METERS_TO_MOVE);
    }
}
