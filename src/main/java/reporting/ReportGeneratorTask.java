package reporting;

import clock.Timer;

public class ReportGeneratorTask {

    public ReportGeneratorTask(Timer timer, ReportGeneratorService reportGeneratorService) {
        timer.addTask(reportGeneratorService::generate);
    }

}
