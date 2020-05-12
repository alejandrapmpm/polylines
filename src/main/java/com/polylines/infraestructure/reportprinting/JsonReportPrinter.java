package com.polylines.infraestructure.reportprinting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polylines.domain.reporting.Report;
import com.polylines.domain.reporting.ReportPrinter;

public class JsonReportPrinter implements ReportPrinter {

    @Override
    public void print(Report report) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(report);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
