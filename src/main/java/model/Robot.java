package model;

import java.util.List;
public class Robot {
    public final List<GeoPoint> journey;
    public GeoPoint currentPosition;
    public final String source;
    public double speed;

    public Robot(List<GeoPoint> journey, double speed) {
        this.journey = journey;
        this.speed = speed;
        this.source = "ROBOT";
        currentPosition = journey.get(0);
    }

    public boolean atTheEndOfTheJourney(){
        return currentPosition.equals(journey.get(journey.size() - 1));
    }
}
