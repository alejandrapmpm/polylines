package com.polylines.domain;

import com.polylines.domain.observers.Observer;

public interface Observable {

    void registerObserver(Observer o);
    void notifyAllObservers();
}
