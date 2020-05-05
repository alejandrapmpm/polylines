package reporting.service;

import java.util.Date;
import model.Level;
import model.Robot;
import reporting.model.Location;
import reporting.model.Report;
import reporting.printer.ReportPrinter;
import app.ParticleReader;

public class ReportGeneratorService {

    private final Robot robot;
    private final ParticleReader particleReader;
    private final ReportPrinter printer;

    public ReportGeneratorService(Robot robot, ParticleReader particleReader, ReportPrinter printer) {
        this.robot = robot;
        this.particleReader = particleReader;
        this.printer = printer;
    }

    public Report generate() {
        Report report = buildReport();
        printer.printReport(report);
        particleReader.removePreviousReadings();
        return report;
    }

    private Report buildReport() {
        return new Report(
                getCurrentTimestamp(),
                getRobotLocation(),
                getLevel(),
                robot.source);
    }

    private long getCurrentTimestamp() {
        return new Date().getTime();
    }

    private Location getRobotLocation() {
        return new Location(robot.currentPosition.lat, robot.currentPosition.lng);
    }

    private Level getLevel() {
        double average = particleReader.values.stream()
                .mapToInt(i -> i)
                .average()
                .orElse(0d);
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
