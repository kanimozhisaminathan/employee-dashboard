package com.example.demo1.controller;

import com.example.demo1.dto.EmployeeAlertDto;
import com.example.demo1.dto.EmployeeDto;
import com.example.demo1.dto.EmailSendResultDto;
import com.example.demo1.dto.TestMailRequest;
import com.example.demo1.dto.TestMailResult;
import com.example.demo1.dto.WeeklyEmployeeEventDto;
import com.example.demo1.service.EmployeeAlertService;
import com.example.demo1.service.EmployeeEmailService;
import com.example.demo1.service.EmployeeImportService;
import com.example.demo1.service.SourceEmployeeClient;
import com.example.demo1.service.TestMailService;
import com.example.demo1.service.WeeklyEmployeeEventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class EmployeeController {

    private final EmployeeImportService employeeImportService;
    private final EmployeeAlertService employeeAlertService;
    private final EmployeeEmailService employeeEmailService;
    private final SourceEmployeeClient sourceEmployeeClient;
    private final TestMailService testMailService;
    private final WeeklyEmployeeEventService weeklyEmployeeEventService;

    public EmployeeController(EmployeeImportService employeeImportService,
                              EmployeeAlertService employeeAlertService,
                              EmployeeEmailService employeeEmailService,
                              SourceEmployeeClient sourceEmployeeClient,
                              TestMailService testMailService,
                              WeeklyEmployeeEventService weeklyEmployeeEventService) {
        this.employeeImportService = employeeImportService;
        this.employeeAlertService = employeeAlertService;
        this.employeeEmailService = employeeEmailService;
        this.sourceEmployeeClient = sourceEmployeeClient;
        this.testMailService = testMailService;
        this.weeklyEmployeeEventService = weeklyEmployeeEventService;
    }

    @GetMapping("/employees")
    public List<EmployeeDto> getEmployees() {
        return employeeImportService.findAllDtos();
    }

    @PostMapping(value = "/employees/import", consumes = MediaType.TEXT_PLAIN_VALUE)
    public List<EmployeeDto> importEmployees(@RequestBody String csv) {
        return employeeImportService.toDtos(employeeImportService.importCsv(csv));
    }

    @PostMapping("/employees/import/source")
    public List<EmployeeDto> importEmployeesFromSource() {
        return employeeImportService.toDtos(employeeImportService.importCsv(sourceEmployeeClient.fetchEmployeeCsv()));
    }

    @PostMapping("/employee-alerts/run")
    public List<EmployeeAlertDto> runAlerts(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        LocalDate alertDate = date == null ? LocalDate.now() : date;
        return employeeAlertService.generateAlerts(alertDate).stream()
                .map(EmployeeAlertDto::from)
                .toList();
    }

    @GetMapping("/employee-alerts")
    public List<EmployeeAlertDto> getAlerts(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        if (date == null) {
            return employeeAlertService.findAll().stream()
                    .map(EmployeeAlertDto::from)
                    .toList();
        }
        return employeeAlertService.alertsForDate(date).stream()
                .map(EmployeeAlertDto::from)
                .toList();
    }

    @GetMapping("/employees/current-week-events")
    public List<WeeklyEmployeeEventDto> getCurrentWeekEmployeeEvents(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        LocalDate referenceDate = date == null ? LocalDate.now() : date;
        return weeklyEmployeeEventService.currentWeekEvents(referenceDate);
    }

    @PostMapping("/employees/current-week-events/email")
    public List<EmailSendResultDto> emailCurrentWeekEmployeeEvents(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        LocalDate referenceDate = date == null ? LocalDate.now() : date;
        return employeeEmailService.sendCurrentWeekEventEmails(referenceDate);
    }

    @PostMapping("/mail/test")
    public TestMailResult sendTestMail(@RequestBody TestMailRequest request) {
        return testMailService.send(request);
    }
}
