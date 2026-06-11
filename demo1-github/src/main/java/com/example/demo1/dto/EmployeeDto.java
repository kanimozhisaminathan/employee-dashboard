package com.example.demo1.dto;

import com.example.demo1.model.Employee;

import java.time.LocalDate;

public record EmployeeDto(
        String employeeId,
        String employeeName,
        String email,
        LocalDate dateOfBirth,
        LocalDate hiringDate,
        String managerId,
        String managerName,
        String hrId,
        String hrName
) {
    public static EmployeeDto from(Employee employee) {
        return new EmployeeDto(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getEmail(),
                employee.getDateOfBirth(),
                employee.getHiringDate(),
                employee.getManagerId(),
                null,
                employee.getHrId(),
                null
        );
    }
}
