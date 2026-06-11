package com.example.demo1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmployeeAlertJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeAlertJob.class);

    private final EmployeeImportService employeeImportService;
    private final EmployeeAlertService employeeAlertService;
    private final SourceEmployeeClient sourceEmployeeClient;
    private final String sourceUrl;

    public EmployeeAlertJob(EmployeeImportService employeeImportService,
                            EmployeeAlertService employeeAlertService,
                            SourceEmployeeClient sourceEmployeeClient,
                            @Value("${employee.source.url:}") String sourceUrl) {
        this.employeeImportService = employeeImportService;
        this.employeeAlertService = employeeAlertService;
        this.sourceEmployeeClient = sourceEmployeeClient;
        this.sourceUrl = sourceUrl;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        runForDate(LocalDate.now(), "startup");
    }

    @Scheduled(cron = "${employee.alert.cron:0 0 8 * * *}")
    public void runDaily() {
        runForDate(LocalDate.now(), "scheduled");
    }

    private void runForDate(LocalDate date, String trigger) {
        try {
            refreshSourceEmployeesIfConfigured();
            int generated = employeeAlertService.generateAlerts(date).size();
            LOGGER.info("Employee alert job ran on {} via {} and generated {} alerts.", date, trigger, generated);
        } catch (Exception ex) {
            LOGGER.warn("Employee alert job failed on {} via {}: {}", date, trigger, detailedMessage(ex));
        }
    }

    private void refreshSourceEmployeesIfConfigured() {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return;
        }

        String csv = sourceEmployeeClient.fetchEmployeeCsv();
        if (csv == null || csv.isBlank()) {
            LOGGER.warn("Source employee feed returned no data.");
            return;
        }

        employeeImportService.importCsv(csv);
    }

    private String detailedMessage(Exception ex) {
        StringBuilder message = new StringBuilder();
        Throwable current = ex;
        while (current != null) {
            if (!message.isEmpty()) {
                message.append(" | ");
            }
            message.append(current.getMessage());
            current = current.getCause();
        }
        return message.toString();
    }
}
