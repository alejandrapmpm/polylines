package app;

import model.GeoPoint;
import model.Robot;
import utilities.DistanceCalculator;

public class RobotApplication {

    public final Robot robot;
    private final ParticleReader particleReader;
    public GeoPoint currentPosition;
    public int nextPosition = 1;
    public double metersMoved = 0;

    public RobotApplication(Robot robot, ParticleReader particleReader) {
        this.robot = robot;
        this.particleReader = particleReader;
        currentPosition = robot.journey.get(0);
    }

    public void moveRobot() {
        double remainingMeters = robot.speed;
        if (notYetAtTheEnd(robot.currentPosition)) {
            GeoPoint to = robot.journey.get(nextPosition);
            while (remainingMeters > 0 && notYetAtTheEnd(robot.currentPosition)) {
                double distanceBetweenGeoPoints = DistanceCalculator.calculate(robot.currentPosition, to);
                //System.out.println("Im moving: " + distanceBetweenGeoPoints + " from " + robot.currentPosition+ " " + to);
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
