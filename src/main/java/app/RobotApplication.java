package app;

import model.GeoPoint;
import model.Robot;
import scheduler.Scheduler;
import utilities.DistanceCalculator;

public class RobotApplication {

    private final Robot robot;
    private final ParticleReader particleReader;
    private final Scheduler robotScheduler;
    private final Scheduler reportingScheduler;
    private double travelledMeters = 0;
    public int nextPosition = 1;

    public RobotApplication(Robot robot, ParticleReader particleReader, Scheduler robotScheduler, Scheduler reportingScheduler) {
        this.robot = robot;
        this.particleReader = particleReader;
        this.robotScheduler = robotScheduler;
        this.reportingScheduler = reportingScheduler;
    }

    public void moveRobot() {
        double remainingMeters = robot.speed;
        GeoPoint to = robot.journey.get(nextPosition);

        while (remainingMeters > 0 && !robot.atTheEndOfJourney()) {
            double distance = DistanceCalculator.calculate(robot.currentPosition, to);
            if (distance > remainingMeters) {
                moveToAnIntermediateStop(remainingMeters, to);
                remainingMeters = 0;
            } else {
                to = moveToNextStopAndRecalculate(to, distance);
                remainingMeters -= distance;
            }
            if (travelledMeters >= 100) {
                readParticlesInTheAir();
            }
        }
        if (robot.atTheEndOfJourney()) {
            stopRobotAndReportingSchedulers();
        }
    }

    private GeoPoint moveToNextStopAndRecalculate(GeoPoint to, double distance) {
        robot.currentPosition = moveToNextGeoPoint();
        travelledMeters += distance;
        if (!robot.atTheEndOfJourney()) {
            to = robot.journey.get(nextPosition);
        }
        return to;
    }

    private void moveToAnIntermediateStop(double remainingMeters, GeoPoint to) {
        robot.currentPosition = newGeoPoint(remainingMeters, robot.currentPosition, to);
        travelledMeters += remainingMeters;
    }

    private void readParticlesInTheAir() {
        particleReader.run();
        travelledMeters = 0;
    }

    private GeoPoint newGeoPoint(double meters, GeoPoint from, GeoPoint to) {
        double radio = meters / DistanceCalculator.calculate(from, to);
        double newLat = from.lat + (to.lat - from.lat) * radio;
        double newLng = from.lng + (to.lng - from.lng) * radio;
        return new GeoPoint(newLat, newLng);
    }

    private GeoPoint moveToNextGeoPoint() {
        robot.currentPosition = robot.journey.get(nextPosition);
        nextPosition++;
        return robot.currentPosition;
    }

    private void stopRobotAndReportingSchedulers() {
        robotScheduler.stop();
        reportingScheduler.stop();
    }
}
