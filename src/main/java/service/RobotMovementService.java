package service;

import java.util.List;
import java.util.stream.Collectors;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import model.GeoPoint;
import model.Robot;
import utilities.DistanceCalculator;

public class RobotMovementService {

    private EncodedPolyline encoder;
    public Robot robot;
    private int nextPosition;
    private List<GeoPoint> journey;

    public RobotMovementService(EncodedPolyline encodedPolyline) {
        this.encoder = encodedPolyline;
        journey = map(encoder.decodePath());
        this.robot = new Robot(journey.get(0));
        this.nextPosition = 1;
    }

    public List<GeoPoint> decode() {
        List<GeoPoint> points = map(encoder.decodePath());
        System.out.println("Decoded: " + points);
        return points;
    }

    private List<GeoPoint> map(List<LatLng> decodePath) {
        return decodePath.stream().map(this::newGeoPoint).collect(Collectors.toList());
    }

    private <R> GeoPoint newGeoPoint(LatLng latLng) {
        return new GeoPoint(latLng.lat, latLng.lng);
    }

    public void moveRobot(double meters) {
        GeoPoint to = journey.get(nextPosition);
        double trip = DistanceCalculator.distance(robot.currentPosition, to);
        System.out.println("Trip: " + trip);

        if (trip > meters) {
            robot.currentPosition = DistanceCalculator.moveGeoPointSomeMeters(robot.currentPosition, to, meters);
        } else if (trip < meters) {
            robot.currentPosition = journey.get(nextPosition);
        }
    }
}
