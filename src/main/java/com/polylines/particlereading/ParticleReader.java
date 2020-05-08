package com.polylines.particlereading;

import java.util.List;
public interface ParticleReader {

    void run();

    void clearPreviousReadings();

    List<Integer> getReadings();
}
