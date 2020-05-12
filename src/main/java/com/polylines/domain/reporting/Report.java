package com.polylines.domain.reporting;

public class Report {

    public enum Level {
        Good,
        Moderate,
        USG,
        Unhealthy;
    }

    private long timestamp;
    private Location location;
    private Level level;
    private String source;

    private Report() {
    }

    public Report(long timestamp, Location location, Level level, String source) {
        this.timestamp = timestamp;
        this.location = location;
        this.level = level;
        this.source = source;
    }

    public Location getLocation() {
        return location;
    }

    public Level getLevel() {
        return level;
    }

    public String getSource() {
        return source;
    }
}
