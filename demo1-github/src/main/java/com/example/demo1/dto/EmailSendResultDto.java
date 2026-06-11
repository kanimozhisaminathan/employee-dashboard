package com.example.demo1.dto;

import com.example.demo1.model.AlertType;

public record EmailSendResultDto(
        String employeeId,
        String employeeName,
        String employeeEmail,
        AlertType eventType,
        boolean sent,
        String message
) {
}
