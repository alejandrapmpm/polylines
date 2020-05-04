package reporting;

import model.Level;

public class Report {

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
