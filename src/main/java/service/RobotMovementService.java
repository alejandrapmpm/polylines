package service;

import java.util.List;
import java.util.stream.Collectors;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import model.GeoPoint;
import model.Robot;
import utilities.DistanceCalculator;

public class RobotMovementService {

    public Robot robot;
    private int nextPosition;

    public RobotMovementService(EncodedPolyline encodedPolyline) {
        this.robot = new Robot(map(encodedPolyline.decodePath()));
        this.nextPosition = 1;
    }

    private List<GeoPoint> map(List<LatLng> decodePath) {
        return decodePath.stream().map(this::newGeoPoint).collect(Collectors.toList());
    }

    private <R> GeoPoint newGeoPoint(LatLng latLng) {
        return new GeoPoint(latLng.lat, latLng.lng);
    }

    public void moveRobot(double meters) {
        GeoPoint from = robot.currentPosition;
        if (from != robot.journey.get(robot.journey.size() - 1)) {
            GeoPoint to = robot.journey.get(nextPosition);
            while (meters > 0 && nextPosition < robot.journey.size()) {
                double trip = DistanceCalculator.distance(robot.currentPosition, to);
                if (trip > meters) {
                    robot.currentPosition = DistanceCalculator.moveGeoPointSomeMeters(robot.currentPosition, to, meters);
                    meters = 0;
                } else if (trip <= meters) {
                    meters = meters - trip;
                    robot.currentPosition = robot.journey.get(nextPosition);
                    robot.journey.get(nextPosition).visited = true;
                    nextPosition++;
                    if (nextPosition < robot.journey.size()) {
                        to = robot.journey.get(nextPosition);
                    }
                }
            }
            System.out.println("I've finished moving " + robot.currentPosition + " to: " + to);
        }
    }
}
