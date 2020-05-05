package reporting;

import java.util.Date;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import clock.Timer;
import model.Level;
import model.Robot;
import reporting.printer.ReportPrinter;
import service.ParticleReader;

public class ReportGenerator {
    private final Robot robot;
    private final ParticleReader particleReader;
    private final ReportPrinter printer;

    public ReportGenerator(Robot robot, ParticleReader particleReader, Timer reportTimer, ReportPrinter printer) {

        this.robot = robot;
        this.particleReader = particleReader;
        this.printer = printer;
        reportTimer.addTask(this::generate);
    }

    private void generate() {

        Report report = buildReport();
        printer.printReport(report);
    }

    private Report buildReport() {
        Integer sum = particleReader.values.stream().reduce(0, Integer::sum);
        int size = particleReader.values.size();
        int average = size > 0 ? sum / size : 0;
        return new Report(
                    new Date().getTime(),
                    new Location(robot.currentPosition.lat, robot.currentPosition.lng),
                    getLevel(average),
                    robot.source);
    }

    private Level getLevel(int average) {
        if (average >= 0 && average <= 50) {
            return Level.Good;
        } else if (average <= 100) {
            return Level.Moderate;
        } else if (average <= 150) {
            return Level.USG;
        } else {
            return Level.Unhealthy;
        }
    }
}
