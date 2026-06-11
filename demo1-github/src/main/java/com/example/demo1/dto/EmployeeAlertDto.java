package com.example.demo1.dto;

import com.example.demo1.model.AlertType;
import com.example.demo1.model.EmployeeAlert;

import java.time.LocalDate;

public record EmployeeAlertDto(
        Long id,
        String employeeId,
        String employeeName,
        AlertType alertType,
        LocalDate eventDate,
        LocalDate alertDate,
        String managerId,
        String hrId,
        String message
) {
    public static EmployeeAlertDto from(EmployeeAlert alert) {
        return new EmployeeAlertDto(
                alert.getId(),
                alert.getEmployee().getEmployeeId(),
                alert.getEmployee().getEmployeeName(),
                alert.getAlertType(),
                alert.getEventDate(),
                alert.getAlertDate(),
                alert.getManagerId(),
                alert.getHrId(),
                alert.getMessage()
        );
    }
}
