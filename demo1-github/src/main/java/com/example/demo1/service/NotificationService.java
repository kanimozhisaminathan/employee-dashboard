package com.example.demo1.service;

import com.example.demo1.model.EmployeeAlert;
import com.example.demo1.model.Employee;
import com.example.demo1.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final EmployeeRepository employeeRepository;
    private final String fromAddress;

    public NotificationService(JavaMailSender mailSender,
                               EmployeeRepository employeeRepository,
                               @Value("${app.mail.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.employeeRepository = employeeRepository;
        this.fromAddress = fromAddress;
    }

    public void notifyManagerAndHr(EmployeeAlert alert) {
        if (fromAddress == null || fromAddress.isBlank()) {
            LOGGER.warn("Skipping alert email for {} because app.mail.from is not configured.",
                    alert.getEmployee().getEmployeeId());
            return;
        }

        Set<String> recipients = recipients(alert);
        if (recipients.isEmpty()) {
            LOGGER.warn("Skipping alert email for {} because manager and HR emails are missing.",
                    alert.getEmployee().getEmployeeId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipients.toArray(String[]::new));
            message.setSubject(subjectFor(alert));
            message.setText(bodyFor(alert, recipients));
            mailSender.send(message);
            LOGGER.info(
                    "Alert emailed for employee={} to recipients={}",
                    alert.getEmployee().getEmployeeId(),
                    recipients
            );
        } catch (MailException ex) {
            LOGGER.warn(
                    "Failed to email alert for employee={} to manager/hr: {}",
                    alert.getEmployee().getEmployeeId(),
                    detailedMessage(ex)
            );
        }
    }

    private Set<String> recipients(EmployeeAlert alert) {
        Set<String> recipients = new LinkedHashSet<>();
        addRecipient(recipients, alert.getEmployee().getEmail());
        addRecipient(recipients, emailForEmployee(alert.getManagerId()));
        addRecipient(recipients, emailForEmployee(alert.getHrId()));
        return recipients;
    }

    private String emailForEmployee(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            return null;
        }
        return employeeRepository.findById(employeeId)
                .map(Employee::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .orElse(null);
    }

    private void addRecipient(Set<String> recipients, String email) {
        if (email != null && !email.isBlank()) {
            recipients.add(email.trim());
        }
    }

    private String subjectFor(EmployeeAlert alert) {
        return switch (alert.getAlertType()) {
            case BIRTHDAY -> "Birthday Alert: " + alert.getEmployee().getEmployeeName();
            case WORK_ANNIVERSARY -> "Work Anniversary Alert: " + alert.getEmployee().getEmployeeName();
        };
    }

    private String bodyFor(EmployeeAlert alert, Set<String> recipients) {
        String eventLabel = alert.getAlertType() == null ? "Employee Alert" : alert.getAlertType().name().replace('_', ' ');
        return """
                Hello,

                An employee alert has been generated.

                Employee: %s (%s)
                Event: %s
                Event Date: %s
                Manager ID: %s
                HR ID: %s
                Recipients: %s

                Message: %s

                Regards,
                Employee Alert System
                """.formatted(
                alert.getEmployee().getEmployeeName(),
                alert.getEmployee().getEmployeeId(),
                eventLabel,
                alert.getEventDate(),
                safe(alert.getManagerId()),
                safe(alert.getHrId()),
                String.join(", ", recipients),
                alert.getMessage()
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
