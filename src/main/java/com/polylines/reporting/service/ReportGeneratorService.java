package com.polylines.reporting.service;

import java.util.Date;
import com.polylines.app.ParticleReader;
import com.polylines.model.Robot;
import com.polylines.reporting.model.Location;
import com.polylines.reporting.model.Report;
import com.polylines.reporting.printer.ReportPrinter;

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
        particleReader.removePreviousReadings();
        return report;
    }

    private Report buildReport() {
        return new Report(
                getCurrentTimestamp(),
                getRobotLocation(),
                getLevel(),
                Robot.source);
    }

    private long getCurrentTimestamp() {
        return new Date().getTime();
    }

    private Location getRobotLocation() {
        return new Location(robot.getCurrentPosition().lat, robot.getCurrentPosition().lng);
    }

    private Report.Level getLevel() {
        double average = particleReader.values.stream()
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
