package service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import clock.Clock;
import model.GeoPoint;
import model.Robot;
import utilities.DistanceCalculator;

public class RobotMovementService {

    public final Robot robot;
    private final ParticleReader particleReader;
    public int nextPosition;
    public double metersMoved;

    public RobotMovementService(EncodedPolyline encoder, double meters, Clock clock, ParticleReader particleReader) {
        this.robot = new Robot(map(encoder.decodePath()));
        this.nextPosition = 1;
        this.particleReader = particleReader;
        this.metersMoved = 0;
        clock.addTask(() -> moveRobot(meters));
    }

    private List<GeoPoint> map(List<LatLng> decodePath) {
        return decodePath.stream().map(this::newGeoPoint).collect(Collectors.toList());
    }

    private <R> GeoPoint newGeoPoint(LatLng latLng) {
        return new GeoPoint(latLng.lat, latLng.lng);
    }

    public void moveRobot(double remainingMeters) {
       // System.out.println("Im moving: " + LocalDateTime.now());
        if (notYetAtTheEnd(robot.currentPosition)) {
            GeoPoint to = robot.journey.get(nextPosition);
            while (remainingMeters > 0 && notYetAtTheEnd(robot.currentPosition)) {
                double distanceBetweenGeoPoints = DistanceCalculator.distance(robot.currentPosition, to);
                if (distanceBetweenGeoPoints > remainingMeters) {
                    moveToIntermediateGeoPoint(remainingMeters, to);
                    metersMoved += remainingMeters;
                    remainingMeters = 0;
                } else if (distanceBetweenGeoPoints <= remainingMeters) {
                    moveToNextGeoPoint();
                    metersMoved += distanceBetweenGeoPoints;
                    remainingMeters -= distanceBetweenGeoPoints;
                    if (notYetAtTheEnd(robot.currentPosition)) {
                        to = robot.journey.get(nextPosition);
                    }
                }
                if (metersMoved >= 100) {
                    particleReader.run();
                    metersMoved = 0;
                }
            }
            //System.out.println("I've finished moving " + robot.currentPosition + " to: " + to);
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
