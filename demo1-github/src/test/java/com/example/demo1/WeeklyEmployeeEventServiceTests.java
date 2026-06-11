package com.example.demo1;

import com.example.demo1.dto.WeeklyEmployeeEventDto;
import com.example.demo1.model.AlertType;
import com.example.demo1.repository.EmployeeAlertRepository;
import com.example.demo1.repository.EmployeeRepository;
import com.example.demo1.service.EmployeeImportService;
import com.example.demo1.service.WeeklyEmployeeEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WeeklyEmployeeEventServiceTests {

    @Autowired
    private EmployeeImportService employeeImportService;

    @Autowired
    private WeeklyEmployeeEventService weeklyEmployeeEventService;

    @Autowired
    private EmployeeAlertRepository employeeAlertRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void resetData() {
        employeeAlertRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    void printsEmployeesWithBirthdayOrHiringDateInCurrentWeekWithApproverAndHrNames() {
        employeeImportService.importCsv("""
                Employee_ID,Employee_Name,Date_of_Birth,Hiring_Date,Approver_ID,HR_ID
                MGR100,Meera Manager,1983-03-10,2014-01-06,HR100,HR100
                HR100,Harini HR,1978-11-02,2010-04-01,,
                EMP501,Priya Raman,1995-05-18,2021-07-15,MGR100,HR100
                EMP502,Arun Kumar,1990-08-12,2020-05-19,MGR100,HR100
                EMP503,Sneha Iyer,1997-05-21,2022-05-21,MGR100,HR100
                EMP504,Outside Week,1992-06-10,2020-06-15,MGR100,HR100
                """);

        List<WeeklyEmployeeEventDto> events =
                weeklyEmployeeEventService.currentWeekEvents(LocalDate.of(2026, 5, 21));

        assertThat(events)
                .extracting(WeeklyEmployeeEventDto::employeeName)
                .contains("Priya Raman", "Arun Kumar", "Sneha Iyer")
                .doesNotContain("Outside Week");
        assertThat(events)
                .filteredOn(event -> event.employeeName().equals("Priya Raman"))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.eventType()).isEqualTo(AlertType.BIRTHDAY);
                    assertThat(event.approverName()).isEqualTo("Meera Manager");
                    assertThat(event.hrName()).isEqualTo("Harini HR");
                });
        assertThat(events)
                .filteredOn(event -> event.employeeName().equals("Sneha Iyer"))
                .extracting(WeeklyEmployeeEventDto::eventType)
                .containsExactlyInAnyOrder(AlertType.BIRTHDAY, AlertType.WORK_ANNIVERSARY);
    }
}
