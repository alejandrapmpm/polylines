package reporting.task;

import clock.Timer;
import reporting.service.ReportGeneratorService;

public class ReportGeneratorTask {

    public ReportGeneratorTask(Timer timer, ReportGeneratorService reportGeneratorService) {
        timer.addTask(reportGeneratorService::generate);
    }

}
