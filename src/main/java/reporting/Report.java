package reporting;

public class Report {

    public long timestamp;
    public Location location;
    public String level;
    public String source;

    public Report() {
    }

    public Report(long timestamp, Location location, String level, String source) {
        this.timestamp = timestamp;
        this.location = location;
        this.level = level;
        this.source = source;
    }
}
