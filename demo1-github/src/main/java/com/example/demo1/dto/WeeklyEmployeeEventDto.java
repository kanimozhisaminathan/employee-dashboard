package com.example.demo1.dto;

import com.example.demo1.model.AlertType;

import java.time.LocalDate;

public record WeeklyEmployeeEventDto(
        String employeeId,
        String employeeName,
        String employeeEmail,
        AlertType eventType,
        LocalDate eventDate,
        Integer anniversaryYears,
        String approverId,
        String approverName,
        String approverEmail,
        String hrId,
        String hrName,
        String hrEmail
) {
}
