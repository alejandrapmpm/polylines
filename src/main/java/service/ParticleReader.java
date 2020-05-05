package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleReader {

    public List<Integer> values = new ArrayList<>();
    private final Random random;

    public ParticleReader() {
        random = new Random();
    }

    public void run() {
        values.add(generateRandomInt());
        //System.out.println("Generating particula:" + values);
    }

    public int generateRandomInt() {
        return random.nextInt(200);
    }

    public void removePreviousReadings() {
        values.clear();
    }
}
