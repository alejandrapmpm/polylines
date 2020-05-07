package com.polylines.model;

import com.polylines.observers.Observer;

public interface Observable {

    void registerObserver(Observer o);
    void notifyAllObservers();
}
