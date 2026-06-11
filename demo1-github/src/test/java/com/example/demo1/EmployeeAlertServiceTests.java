package com.example.demo1;

import com.example.demo1.model.AlertType;
import com.example.demo1.model.EmployeeAlert;
import com.example.demo1.repository.EmployeeAlertRepository;
import com.example.demo1.repository.EmployeeRepository;
import com.example.demo1.service.EmployeeAlertService;
import com.example.demo1.service.EmployeeImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.mock;

@SpringBootTest(properties = "app.mail.from=test@example.com")
class EmployeeAlertServiceTests {

    @Autowired
    private EmployeeImportService employeeImportService;

    @Autowired
    private EmployeeAlertService employeeAlertService;

    @Autowired
    private EmployeeAlertRepository employeeAlertRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JavaMailSender mailSender;

    @BeforeEach
    void resetData() {
        employeeAlertRepository.deleteAll();
        employeeRepository.deleteAll();
        reset(mailSender);
    }

    @Test
    void createsBirthdayAndAnniversaryAlertsForMatchingDate() {
        employeeImportService.importCsv("""
                Employee_ID,Employee_Name,Date_of_Birth,Hiring_Date,Manager_ID,HR_ID,Email
                EMP01,Priya Raman,1995-05-21,2021-05-21,MGR01,HR01,priya.roman@example.com
                MGR01,Manager One,1980-01-01,2010-01-01,HR01,HR01,manager.one@example.com
                HR01,HR One,1975-01-01,2008-01-01,,,hr.one@example.com
                """);

        List<EmployeeAlert> alerts = employeeAlertService.generateAlerts(LocalDate.of(2026, 5, 21));

        assertThat(alerts)
                .extracting(EmployeeAlert::getAlertType)
                .containsExactlyInAnyOrder(AlertType.BIRTHDAY, AlertType.WORK_ANNIVERSARY);
        assertThat(alerts)
                .extracting(EmployeeAlert::getMessage)
                .contains("Today is Priya Raman's 5th Work Anniversary.");

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, org.mockito.Mockito.times(2)).send(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues())
                .extracting(SimpleMailMessage::getTo)
                .allSatisfy(recipients -> assertThat(recipients)
                        .containsExactly("priya.roman@example.com", "manager.one@example.com", "hr.one@example.com"));
    }

    @Test
    void sendsWeekendEventsOnPreviousFridayOnlyOnce() {
        employeeImportService.importCsv("""
                Employee_ID,Employee_Name,Date_of_Birth,Hiring_Date,Manager_ID,HR_ID
                EMP02,Arun Kumar,1990-05-23,2020-05-24,MGR01,HR01
                """);

        LocalDate friday = LocalDate.of(2026, 5, 22);
        List<EmployeeAlert> firstRun = employeeAlertService.generateAlerts(friday);
        List<EmployeeAlert> secondRun = employeeAlertService.generateAlerts(friday);
        List<EmployeeAlert> saturdayRun = employeeAlertService.generateAlerts(LocalDate.of(2026, 5, 23));

        assertThat(firstRun)
                .extracting(EmployeeAlert::getEventDate)
                .containsExactlyInAnyOrder(LocalDate.of(2026, 5, 23), LocalDate.of(2026, 5, 24));
        assertThat(secondRun).isEmpty();
        assertThat(saturdayRun).isEmpty();
        verifyNoInteractions(mailSender);
    }

    @TestConfiguration
    static class MailTestConfiguration {

        @Bean
        @Primary
        JavaMailSender mailSender() {
            return mock(JavaMailSender.class);
        }
    }
}
