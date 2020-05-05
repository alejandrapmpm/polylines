package app;

import model.GeoPoint;
import model.Robot;
import utilities.DistanceCalculator;

public class RobotApplication {

    private final Robot robot;
    private final ParticleReader particleReader;
    private double travelledMeters = 0;
    public int nextPosition = 1;

    public RobotApplication(Robot robot, ParticleReader particleReader) {
        this.robot = robot;
        this.particleReader = particleReader;
    }

    public void moveRobot() {
        double remainingMeters = robot.speed;
        if (robot.notArrivedYet()) {
            GeoPoint to = robot.journey.get(nextPosition);
            while (remainingMeters > 0 && robot.notArrivedYet()) {
                double distance = DistanceCalculator.calculate(robot.currentPosition, to);
                //System.out.println("Im moving: " + distance + " from " + robot.currentPosition+ " " + to);
                if (distance > remainingMeters) {
                    robot.currentPosition = newGeoPoint(remainingMeters, robot.currentPosition, to);
                    travelledMeters += remainingMeters;
                    remainingMeters = 0;
                } else if (distance <= remainingMeters) {
                    moveToNextGeoPoint();
                    travelledMeters += distance;
                    remainingMeters -= distance;
                    if (robot.notArrivedYet()) {
                        to = robot.journey.get(nextPosition);
                    }
                }
                if (travelledMeters >= 100) {
                    particleReader.run();
                    travelledMeters = 0;
                }
            }
            //System.out.println("I've finished moving " + robot.currentPosition + " to: " + to);
        }
    }

    private GeoPoint newGeoPoint(double meters, GeoPoint from, GeoPoint to) {
        double radio = meters / DistanceCalculator.calculate(from, to);
        double newLat = from.lat + (to.lat - from.lat) * radio;
        double newLng = from.lng + (to.lng - from.lng) * radio;
        return new GeoPoint(newLat, newLng);
    }

    private void moveToNextGeoPoint() {
        robot.currentPosition = robot.journey.get(nextPosition);
        nextPosition++;
    }
}
