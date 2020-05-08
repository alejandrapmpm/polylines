package com.polylines.particlereading;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomParticleReader implements ParticleReader {

    public final List<Integer> values;
    private final Random random;
    private static final int BOUND = 200;

    public RandomParticleReader(Random random) {
        this.random = random;
        values = new ArrayList<>();
    }


    @Override
    public void run() {
        values.add(generateRandomInt());
    }

    @Override
    public void clearPreviousReadings() {
        values.clear();
    }

    @Override
    public List<Integer> getReadings() {
        return values;
    }

    private int generateRandomInt() {
        return random.nextInt(BOUND);
    }
}
