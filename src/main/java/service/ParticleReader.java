package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class ParticleReader {

    public List<Integer> values = new ArrayList<>();

    public void run() {
        values.add(generateRandomInt());
        //System.out.println("Generating particula:" + values);
    }

    private int generateRandomInt() {
        Random random = new Random();
        return random.nextInt(200);
    }
}
