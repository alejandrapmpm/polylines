package com.polylines.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleReader {

    public final List<Integer> values = new ArrayList<>();
    private final Random random;

    public ParticleReader() {
        random = new Random();
    }

    public void run() {
        values.add(generateRandomInt());
    }

    public int generateRandomInt() {
        return random.nextInt(200);
    }

    public void removePreviousReadings() {
        values.clear();
    }
}
