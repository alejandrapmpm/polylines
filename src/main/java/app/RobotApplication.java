package app;

import model.GeoPoint;
import model.Robot;
import utilities.DistanceCalculator;

public class RobotApplication {

    private final Robot robot;
    private final ParticleReader particleReader;
    public int nextPosition = 1;
    public double metersAlreadyMoved = 0;

    public RobotApplication(Robot robot, ParticleReader particleReader) {
        this.robot = robot;
        this.particleReader = particleReader;
    }

    public void moveRobot() {
        double remainingMeters = robot.speed;
        if (!robot.atTheEndOfTheJourney()) {
            GeoPoint to = robot.journey.get(nextPosition);
            while (remainingMeters > 0 && !robot.atTheEndOfTheJourney()) {
                double distanceBetweenGeoPoints = DistanceCalculator.calculate(robot.currentPosition, to);
                //System.out.println("Im moving: " + distanceBetweenGeoPoints + " from " + robot.currentPosition+ " " + to);
                if (distanceBetweenGeoPoints > remainingMeters) {
                    robot.currentPosition = getNewGeoPoint(remainingMeters, robot.currentPosition, to);
                    metersAlreadyMoved += remainingMeters;
                    remainingMeters = 0;
                } else if (distanceBetweenGeoPoints <= remainingMeters) {
                    moveToNextGeoPoint();
                    metersAlreadyMoved += distanceBetweenGeoPoints;
                    remainingMeters -= distanceBetweenGeoPoints;
                    if (!robot.atTheEndOfTheJourney()) {
                        to = robot.journey.get(nextPosition);
                    }
                }
                if (metersAlreadyMoved >= 100) {
                    particleReader.run();
                    metersAlreadyMoved = 0;
                }
            }
            //System.out.println("I've finished moving " + robot.currentPosition + " to: " + to);
        }
    }

    private GeoPoint getNewGeoPoint(double meters, GeoPoint from, GeoPoint to) {
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
