package reporting;

import java.util.Date;
import model.Level;
import model.Robot;
import reporting.model.Location;
import reporting.model.Report;
import reporting.printer.ReportPrinter;
import service.ParticleReader;

public class ReportGeneratorService {

    private final Robot robot;
    private final ParticleReader particleReader;
    private final ReportPrinter printer;

    public ReportGeneratorService(Robot robot, ParticleReader particleReader, ReportPrinter printer) {
        this.robot = robot;
        this.particleReader = particleReader;
        this.printer = printer;
    }

    void generate() {
        printer.printReport(buildReport());
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
