package com.polylines.app;

import com.polylines.model.GeoPoint;
import com.polylines.model.Robot;
import com.polylines.particlereading.ParticleReader;
import com.polylines.utilities.DistanceCalculator;

public class RobotPollutionCollector {

    private final Robot robot;
    private final ParticleReader particleReader;
    private double travelledMeters;
    private int nextPosition;

    public RobotPollutionCollector(Robot robot, ParticleReader particleReader) {
        this.robot = robot;
        this.particleReader = particleReader;
        this.travelledMeters = 0;
        this.nextPosition = 1;
    }

    public void moveRobot() {
        double metersToMove = robot.speed;
        GeoPoint to = robot.journey.get(nextPosition);

        while (robotCanMove(metersToMove)) {
            double distance = DistanceCalculator.calculate(robot.getCurrentPosition(), to);
            if (distance > metersToMove) {
                moveToAnIntermediateStop(metersToMove, to);
                metersToMove = 0;
            } else {
                to = moveToNextStopAndRecalculate(to, distance);
                metersToMove -= distance;
            }
            if (shouldReadParticlesLevel()) {
                readParticlesInTheAirAndReset();
            }
        }
    }

    private boolean robotCanMove(double remainingMeters) {
        return remainingMeters > 0 && !robot.atTheEndOfJourney();
    }

    private boolean shouldReadParticlesLevel() {
        return travelledMeters >= 100;
    }

    private GeoPoint moveToNextStopAndRecalculate(GeoPoint to, double distance) {
        robot.setCurrentPosition(robot.journey.get(nextPosition));
        nextPosition++;
        travelledMeters += distance;
        if (!robot.atTheEndOfJourney()) {
            to = robot.journey.get(nextPosition);
        }
        return to;
    }

    private void moveToAnIntermediateStop(double remainingMeters, GeoPoint to) {
        robot.setCurrentPosition(newGeoPoint(remainingMeters, robot.getCurrentPosition(), to));
        travelledMeters += remainingMeters;
    }

    private void readParticlesInTheAirAndReset() {
        particleReader.run();
        travelledMeters = travelledMeters - 100;
    }

    private GeoPoint newGeoPoint(double meters, GeoPoint from, GeoPoint to) {
        double radio = meters / DistanceCalculator.calculate(from, to);
        double newLat = from.lat + (to.lat - from.lat) * radio;
        double newLng = from.lng + (to.lng - from.lng) * radio;
        return new GeoPoint(newLat, newLng);
    }

    public int getNextPosition() {
        return nextPosition;
    }
}
