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
    public int nextPosition;

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

    public void moveRobot(double metersToMove) {
        if (notYetAtTheEnd(robot.currentPosition)) {
            GeoPoint to = robot.journey.get(nextPosition);
            while (metersToMove > 0 && notYetAtTheEnd(robot.currentPosition)) {
                double distanceBetweenGeoPoints = DistanceCalculator.distance(robot.currentPosition, to);
                if (distanceBetweenGeoPoints > metersToMove) {
                    moveToIntermediateGeoPoint(metersToMove, to);
                    metersToMove = 0;
                } else if (distanceBetweenGeoPoints <= metersToMove) {
                    moveToNextGeoPoint();
                    metersToMove -= distanceBetweenGeoPoints;
                    if (notYetAtTheEnd(robot.currentPosition)) {
                        to = robot.journey.get(nextPosition);
                    }
                }
            }
            System.out.println("I've finished moving " + robot.currentPosition + " to: " + to);
        }
    }

    private void moveToIntermediateGeoPoint(double metersToMove, GeoPoint to) {
        robot.currentPosition = DistanceCalculator.moveGeoPointSomeMeters(robot.currentPosition, to, metersToMove);
    }

    private void moveToNextGeoPoint() {
        robot.currentPosition = robot.journey.get(nextPosition);
        nextPosition++;
    }

    private boolean notYetAtTheEnd(GeoPoint from) {
        return from != robot.journey.get(robot.journey.size() - 1);
    }
}
