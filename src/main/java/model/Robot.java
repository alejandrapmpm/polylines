package model;

import java.util.List;
public class Robot {
    public final List<GeoPoint> journey;
    public GeoPoint currentPosition;


    public Robot(List<GeoPoint> journey) {
        this.journey = journey;
        currentPosition = journey.get(0);
    }
}
