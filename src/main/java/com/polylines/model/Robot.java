package com.polylines.model;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import com.polylines.exception.RobotValidationException;
import com.polylines.observers.Observer;

public class Robot implements Observable {

    public final List<GeoPoint> journey;
    private final double speed;
    private GeoPoint currentPosition;
    private final List<Observer> observers;
    public static final String SOURCE = "ROBOT";
    private static final String NOT_VALID_JOURNEY = "The journey should have more than one point.";

    public Robot(List<GeoPoint> journey, double speed) throws RobotValidationException {
        if (isEmpty(journey) || journey.size() == 1) {
            throw new RobotValidationException(NOT_VALID_JOURNEY);
        }
        this.journey = journey;
        this.speed = speed;
        currentPosition = journey.get(0);
        observers = new ArrayList<>();
    }

    public boolean atTheEndOfJourney() {
        GeoPoint lastPoint = journey.get(journey.size() - 1);
        return currentPosition.equals(lastPoint);
    }

    public GeoPoint getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(GeoPoint currentPosition) {
        this.currentPosition = currentPosition;
        if(atTheEndOfJourney()){
            notifyAllObservers();
        }
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void notifyAllObservers() {
        observers.forEach(Observer::update);
    }

    public double getSpeed() {
        return speed;
    }
}
