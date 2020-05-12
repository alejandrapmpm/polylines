package com.polylines.application.generatereport;

import java.util.Date;
import com.polylines.domain.particlesreading.ParticleReader;
import com.polylines.domain.reporting.Location;
import com.polylines.domain.reporting.Report;
import com.polylines.domain.reporting.ReportPrinter;
import com.polylines.domain.robot.Robot;

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
        printer.print(report);
        particleReader.clearPreviousReadings();
        return report;
    }

    private Report buildReport() {
        return new Report(
                getCurrentTimestamp(),
                getRobotLocation(),
                getLevel(),
                Robot.SOURCE);
    }

    private long getCurrentTimestamp() {
        return new Date().getTime();
    }

    private Location getRobotLocation() {
        return new Location(robot.getCurrentPosition().lat, robot.getCurrentPosition().lng);
    }

    private Report.Level getLevel() {
        double average = particleReader.getReadings().stream()
                .mapToInt(i -> i)
                .average()
                .orElse(0);
        if (average <= 50) {
            return Report.Level.Good;
        } else if (average <= 100) {
            return Report.Level.Moderate;
        } else if (average <= 150) {
            return Report.Level.USG;
        } else {
            return Report.Level.Unhealthy;
        }
    }
}
