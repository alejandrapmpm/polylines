package reporting.model;

public class Report {

    public enum Level {
        Good,
        Moderate,
        USG,
        Unhealthy;
    }

    //TODO make fields final but need to get rid of the default constructor
    public long timestamp;
    public Location location;
    public Level level;
    public String source;

    public Report() {
    }

    public Report(long timestamp, Location location, Level level, String source) {
        this.timestamp = timestamp;
        this.location = location;
        this.level = level;
        this.source = source;
    }
}
