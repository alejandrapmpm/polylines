package model;

import java.util.List;

public class Robot {
    public final List<GeoPoint> journey;
    public GeoPoint currentPosition;
    public static final String source = "ROBOT";
    public final double speed;

    public Robot(List<GeoPoint> journey, double speed) {
        this.journey = journey;
        this.speed = speed;
        currentPosition = journey.get(0);
    }

    public boolean notArrivedYet(){
        return !currentPosition.equals(journey.get(journey.size() - 1));
    }
}
