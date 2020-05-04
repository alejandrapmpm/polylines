package reporting;

import java.util.Date;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import clock.Clock;
import model.Robot;
import service.ParticleReader;
public class ReportGenerator {
    private final Robot robot;
    private final ParticleReader particleReader;

    public ReportGenerator(Robot robot, ParticleReader particleReader, Clock reportTimer) {

        this.robot = robot;
        this.particleReader = particleReader;
        reportTimer.addTask(this::run);

    }
    public void run() {
        Integer sum = particleReader.values.stream().reduce(0, Integer::sum);
        int size = particleReader.values.size();
            double average = size > 0 ? sum / size: 0; //TODO
            Report report = new Report(
                    new Date().getTime(),
                    new Location(robot.currentPosition.lat, robot.currentPosition.lng),
                    "LEVEL",
                    "ROBOT");
            ObjectMapper mapper = new ObjectMapper();
            try {
                String json = mapper.writeValueAsString(report);
                System.out.println(json);
                //System.out.println(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
    }
}
