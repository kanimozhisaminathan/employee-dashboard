package com.example.demo1.service;

import com.example.demo1.dto.TestMailRequest;
import com.example.demo1.dto.TestMailResult;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class TestMailService {

    public TestMailResult send(TestMailRequest request) {
        if (isBlank(request.from()) || isBlank(password(request)) || isBlank(request.to())) {
            return new TestMailResult(false, "from, password/appPassword, and to are required.");
        }

        TestMailResult startTlsResult = sendWithStartTls(request);
        if (startTlsResult.sent()) {
            return startTlsResult;
        }

        TestMailResult sslResult = sendWithSsl(request);
        if (sslResult.sent()) {
            return sslResult;
        }

        return new TestMailResult(false, "STARTTLS failed: " + startTlsResult.message()
                + " | SSL failed: " + sslResult.message());
    }

    private TestMailResult sendWithStartTls(TestMailRequest request) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host(request));
        sender.setPort(port(request, 587));
        sender.setUsername(username(request));
        sender.setPassword(password(request));

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        return send(request, sender, "STARTTLS");
    }

    private TestMailResult sendWithSsl(TestMailRequest request) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host(request));
        sender.setPort(465);
        sender.setUsername(username(request));
        sender.setPassword(password(request));

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.trust", host(request));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        return send(request, sender, "SSL");
    }

    private TestMailResult send(TestMailRequest request, JavaMailSenderImpl sender, String mode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(request.from().trim());
            message.setTo(request.to().trim());
            message.setSubject("Employee Alert Test Email");
            message.setText("""
                    Hello,

                    This is a test email from the Employee Alert Management application.

                    If you received this email, Gmail SMTP is configured correctly.

                    Regards,
                    Employee Alert Team
                    """);
            sender.send(message);
            return new TestMailResult(true, "Test email sent successfully using " + mode + ".");
        } catch (MailException ex) {
            return new TestMailResult(false, detailedMessage(ex));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String host(TestMailRequest request) {
        return isBlank(request.host()) ? "smtp.gmail.com" : request.host().trim();
    }

    private int port(TestMailRequest request, int defaultPort) {
        return request.port() == null ? defaultPort : request.port();
    }

    private String username(TestMailRequest request) {
        return isBlank(request.username()) ? request.from().trim() : request.username().trim();
    }

    private String password(TestMailRequest request) {
        String value = isBlank(request.password()) ? request.appPassword() : request.password();
        return value == null ? null : value.replace(" ", "").trim();
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
