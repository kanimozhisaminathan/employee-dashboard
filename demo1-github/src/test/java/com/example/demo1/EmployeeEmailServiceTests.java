package com.example.demo1;

import com.example.demo1.dto.EmailSendResultDto;
import com.example.demo1.repository.EmployeeAlertRepository;
import com.example.demo1.repository.EmployeeRepository;
import com.example.demo1.service.EmployeeEmailService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@SpringBootTest(properties = "app.mail.from=test@example.com")
class EmployeeEmailServiceTests {

    @Autowired
    private EmployeeImportService employeeImportService;

    @Autowired
    private EmployeeEmailService employeeEmailService;

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
    void sendsBirthdayAndAnniversaryAlertsToManagerAndHr() {
        employeeImportService.importCsv("""
                Employee_ID,Employee_Name,Date_of_Birth,Hiring_Date,Manager_ID,HR_ID,Email
                EMP01,Priya Raman,1995-05-21,2021-05-21,MGR01,HR01,priya.roman@example.com
                MGR01,Manager One,1980-01-01,2010-01-01,HR01,HR01,manager.one@example.com
                HR01,HR One,1975-01-01,2008-01-01,,,hr.one@example.com
                """);

        List<EmailSendResultDto> results = employeeEmailService.sendCurrentWeekEventEmails(LocalDate.of(2026, 5, 21));

        assertThat(results)
                .extracting(EmailSendResultDto::eventType)
                .containsExactlyInAnyOrder(
                        com.example.demo1.model.AlertType.BIRTHDAY,
                        com.example.demo1.model.AlertType.WORK_ANNIVERSARY
                );
        assertThat(results).allMatch(EmailSendResultDto::sent);

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues())
                .extracting(SimpleMailMessage::getTo)
                .allSatisfy(recipients -> assertThat(recipients)
                        .containsExactly("priya.roman@example.com", "manager.one@example.com", "hr.one@example.com"));
        assertThat(messageCaptor.getAllValues().get(0).getSubject()).contains("Birthday Alert");
        assertThat(messageCaptor.getAllValues().get(1).getSubject()).contains("Work Anniversary Alert");
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
