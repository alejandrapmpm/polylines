package com.polylines.domain.particlesreading;

import java.util.List;
public interface ParticleReader {

    void run();

    void clearPreviousReadings();

    List<Integer> getReadings();
}
