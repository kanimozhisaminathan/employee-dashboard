package com.example.demo1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @Column(name = "employee_id", length = 32)
    private String employeeId;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Column(name = "email")
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "hiring_date")
    private LocalDate hiringDate;

    @Column(name = "manager_id", length = 32)
    private String managerId;

    @Column(name = "hr_id", length = 32)
    private String hrId;

    protected Employee() {
    }

    public Employee(String employeeId, String employeeName, LocalDate dateOfBirth, LocalDate hiringDate,
                    String managerId, String hrId) {
        this(employeeId, employeeName, null, dateOfBirth, hiringDate, managerId, hrId);
    }

    public Employee(String employeeId, String employeeName, String email, LocalDate dateOfBirth, LocalDate hiringDate,
                    String managerId, String hrId) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.hiringDate = hiringDate;
        this.managerId = managerId;
        this.hrId = hrId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDate getHiringDate() {
        return hiringDate;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getHrId() {
        return hrId;
    }

    public void updateFrom(Employee other) {
        this.employeeName = other.employeeName;
        this.email = other.email;
        this.dateOfBirth = other.dateOfBirth;
        this.hiringDate = other.hiringDate;
        this.managerId = other.managerId;
        this.hrId = other.hrId;
    }
}
