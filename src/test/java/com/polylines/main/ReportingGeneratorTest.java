package com.polylines.main;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.polylines.app.RobotPollutionCollector;
import com.polylines.exception.RobotValidationException;
import com.polylines.model.GeoPoint;
import com.polylines.model.Robot;
import com.polylines.particlereading.ParticleReader;
import com.polylines.particlereading.RandomParticleReader;
import com.polylines.reporting.model.Report;
import com.polylines.reporting.printer.JsonReportPrinter;
import com.polylines.reporting.service.ReportGeneratorService;
import com.polylines.scheduler.ManualScheduler;
import com.polylines.utilities.GeoPointMapper;

public class ReportingGeneratorTest {

    private static final JsonReportPrinter jsonPrinter = new JsonReportPrinter();
    private ParticleReader particleReader;
    private EncodedPolyline encoder;
    private Random mockRandom = Mockito.mock(Random.class);
    private static final String ROBOT_SOURCE_NAME = "ROBOT";

    @Before
    public void setup() {
        encoder = Mockito.mock(EncodedPolyline.class);
        particleReader = new RandomParticleReader(mockRandom);
    }

    @Test
    public void whenReportingSchedulerFires_shouldGenerateAndPrintToConsoleAReportOfParticles()
            throws IOException, RobotValidationException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);
        mockPolylineWithOnlyTwoPoints7000MetersApart();

        when(mockRandom.nextInt(eq(200))).thenReturn(51);

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 100);
        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

        ManualScheduler reportingScheduler = new ManualScheduler();
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, jsonPrinter);
        reportingScheduler.addTask(reportGeneratorService::generate);

        app.moveRobot();

        fire(reportingScheduler);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Report.Level.Moderate, report.getLevel());
        assertEquals(ROBOT_SOURCE_NAME, report.getSource());
        assertEquals(41.87714, report.getLocation().getLat(), 0.00001);
        assertEquals(-87.65934, report.getLocation().getLng(), 0.00001);

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
    }

    @Test
    public void generatingAReport_shouldNotTakeIntoAccountPreviousReadings() throws RobotValidationException {

        mockPolylineWithOnlyTwoPoints7000MetersApart();

        when(mockRandom.nextInt(eq(200))).thenReturn(50).thenReturn(100).thenReturn(150).thenReturn(250);

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 100);

        RobotPollutionCollector app = new RobotPollutionCollector(robot, particleReader);

        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, jsonPrinter);

        app.moveRobot(); // The particles reader generates 50 - which is Moderate level

        Report report = reportGeneratorService.generate();

        assertEquals(Report.Level.Good, report.getLevel());

        app.moveRobot(); // The particles reader generates 100 - which is Moderate level

        report = reportGeneratorService.generate();

        assertEquals(Report.Level.Moderate, report.getLevel());

        app.moveRobot(); // The particles reader generates 150 - which is USG level

        report = reportGeneratorService.generate();

        assertEquals(Report.Level.USG, report.getLevel());

        app.moveRobot(); // The particles reader generates 250 - which is Unhealthy level

        report = reportGeneratorService.generate();

        assertEquals(Report.Level.Unhealthy, report.getLevel());
    }

    @Test
    public void whenReportingSchedulerFiresAndNoParticlesRead_reportIsGeneratedButAverageIsStillZero()
            throws IOException, RobotValidationException {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        mockSystemOut(outContent, errContent);

        mockPolylineWithOnlyTwoPoints7000MetersApart();

        ParticleReader spyParticleReader = Mockito.spy(particleReader);

        List<GeoPoint> journey = GeoPointMapper.map(encoder.decodePath());
        Robot robot = new Robot(journey, 99);

        RobotPollutionCollector app = new RobotPollutionCollector(robot, spyParticleReader);

        ManualScheduler reportingScheduler = new ManualScheduler();
        ReportGeneratorService reportGeneratorService = new ReportGeneratorService(robot, particleReader, jsonPrinter);
        reportingScheduler.addTask(reportGeneratorService::generate);

        app.moveRobot();
        fire(reportingScheduler);

        Report report = mapWrittenOutputToReport(outContent);

        assertEquals(Report.Level.Good, report.getLevel());
        verify(spyParticleReader, never()).run();

        leaveSystemOutAsItWasBefore(originalOut, originalErr);
    }

    private void mockSystemOut(ByteArrayOutputStream outContent, ByteArrayOutputStream errContent) {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    private Report mapWrittenOutputToReport(ByteArrayOutputStream outContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(outContent.toString(), Report.class);
    }

    private void leaveSystemOutAsItWasBefore(PrintStream originalOut, PrintStream originalErr) {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void fire(ManualScheduler timer) {
        timer.start();
        timer.elapseTime();
    }

    private void mockPolylineWithOnlyTwoPoints7000MetersApart() {
        //7122 meters apart
        List<LatLng> points = asList(
                new LatLng(41.87790000, -87.66001000),
                new LatLng(41.82445000, -87.61263000)
        );
        when(encoder.decodePath()).thenReturn(points);
    }

}
