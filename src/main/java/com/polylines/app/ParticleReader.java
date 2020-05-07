package com.polylines.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleReader {

    public final List<Integer> values;
    private final Random random;

    public ParticleReader(Random random) {
        this.random = random;
        values = new ArrayList<>();
    }

    public void run() {
        values.add(generateRandomInt());
    }

    private int generateRandomInt() {
        return random.nextInt(200);
    }

    public void removePreviousReadings() {
        values.clear();
    }
}
