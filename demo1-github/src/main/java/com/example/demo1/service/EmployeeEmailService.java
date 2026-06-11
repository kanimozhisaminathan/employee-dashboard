package com.example.demo1.service;

import com.example.demo1.dto.EmailSendResultDto;
import com.example.demo1.dto.WeeklyEmployeeEventDto;
import com.example.demo1.model.AlertType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeEmailService {

    private final JavaMailSender mailSender;
    private final WeeklyEmployeeEventService weeklyEmployeeEventService;
    private final String fromAddress;

    public EmployeeEmailService(JavaMailSender mailSender,
                                WeeklyEmployeeEventService weeklyEmployeeEventService,
                                @Value("${app.mail.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.weeklyEmployeeEventService = weeklyEmployeeEventService;
        this.fromAddress = fromAddress;
    }

    public List<EmailSendResultDto> sendCurrentWeekEventEmails(LocalDate referenceDate) {
        return weeklyEmployeeEventService.currentWeekEvents(referenceDate).stream()
                .map(this::sendEmail)
                .toList();
    }

    private EmailSendResultDto sendEmail(WeeklyEmployeeEventDto event) {
        if (fromAddress == null || fromAddress.isBlank()) {
            return failed(event, "Configure app.mail.from with your sender email address.");
        }

        Set<String> recipients = recipients(event);
        if (recipients.isEmpty()) {
            return failed(event, "Manager and HR email addresses are missing for this event.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipients.toArray(String[]::new));
            message.setSubject(subjectFor(event));
            message.setText(bodyFor(event, recipients));
            mailSender.send(message);
            return new EmailSendResultDto(
                    event.employeeId(),
                    event.employeeName(),
                    event.employeeEmail(),
                    event.eventType(),
                    true,
                    successMessage(event)
            );
        } catch (MailException ex) {
            return failed(event, detailedMessage(ex));
        }
    }

    private EmailSendResultDto failed(WeeklyEmployeeEventDto event, String reason) {
        return new EmailSendResultDto(
                event.employeeId(),
                event.employeeName(),
                event.employeeEmail(),
                event.eventType(),
                false,
                reason
        );
    }

    private Set<String> recipients(WeeklyEmployeeEventDto event) {
        Set<String> recipients = new LinkedHashSet<>();
        addRecipient(recipients, event.employeeEmail());
        addRecipient(recipients, event.approverEmail());
        addRecipient(recipients, event.hrEmail());
        return recipients;
    }

    private void addRecipient(Set<String> recipients, String email) {
        if (email != null && !email.isBlank()) {
            recipients.add(email.trim());
        }
    }

    private String successMessage(WeeklyEmployeeEventDto event) {
        return event.eventType() == AlertType.BIRTHDAY
                ? "Birthday alert emailed to employee, manager and HR."
                : "Work anniversary alert emailed to employee, manager and HR.";
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

    private String subjectFor(WeeklyEmployeeEventDto event) {
        return event.eventType() == AlertType.BIRTHDAY
                ? "Birthday Alert: " + event.employeeName()
                : "Work Anniversary Alert: " + event.employeeName();
    }

    private String bodyFor(WeeklyEmployeeEventDto event, Set<String> recipients) {
        String eventLabel = event.eventType() == AlertType.BIRTHDAY
                ? "Birthday"
                : anniversaryLabel(event.anniversaryYears());
        return """
                Hello,

                An employee alert has been generated.

                Employee: %s (%s)
                Event: %s
                Event Date: %s
                Manager: %s (%s)
                HR: %s (%s)
                Recipients: %s

                Message: %s

                Regards,
                Employee Alert System
                """.formatted(
                event.employeeName(),
                event.employeeId(),
                eventLabel,
                event.eventDate(),
                safe(event.approverName()),
                safe(event.approverId()),
                safe(event.hrName()),
                safe(event.hrId()),
                String.join(", ", recipients),
                event.eventType() == AlertType.BIRTHDAY
                        ? "Today is " + event.employeeName() + "'s Birthday."
                        : "Today is " + event.employeeName() + "'s " + anniversaryLabel(event.anniversaryYears()) + " Work Anniversary."
        );
    }

    private String anniversaryLabel(Integer years) {
        if (years == null) {
            return "Work Anniversary";
        }
        return switch (years) {
            case 1 -> "1st";
            case 5 -> "5th";
            case 10 -> "10th";
            default -> years + "th";
        };
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
