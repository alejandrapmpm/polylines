package model;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import exception.RobotValidationException;

public class Robot {

    public final List<GeoPoint> journey;
    public GeoPoint currentPosition;
    public final double speed;
    public static final String source = "ROBOT";
    private static final String NOT_VALID_JOURNEY = "The journey should have more than one point.";

    public Robot(List<GeoPoint> journey, double speed) throws RobotValidationException {
        if (CollectionUtils.isEmpty(journey) || journey.size() == 1) {
            throw new RobotValidationException(NOT_VALID_JOURNEY);
        }
        this.journey = journey;
        this.speed = speed;
        currentPosition = journey.get(0);
    }

    public boolean atTheEndOfJourney() {
        GeoPoint lastPoint = journey.get(journey.size() - 1);
        return currentPosition.equals(lastPoint);
    }
}
