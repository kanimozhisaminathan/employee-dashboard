package com.example.demo1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(
        name = "employee_alerts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_alert_event",
                columnNames = {"employee_id", "alert_type", "event_date", "alert_date"}
        )
)
public class EmployeeAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 32)
    private AlertType alertType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "alert_date", nullable = false)
    private LocalDate alertDate;

    @Column(name = "manager_id", length = 32)
    private String managerId;

    @Column(name = "hr_id", length = 32)
    private String hrId;

    @Column(name = "message", nullable = false)
    private String message;

    protected EmployeeAlert() {
    }

    public EmployeeAlert(Employee employee, AlertType alertType, LocalDate eventDate, LocalDate alertDate,
                         String managerId, String hrId, String message) {
        this.employee = employee;
        this.alertType = alertType;
        this.eventDate = eventDate;
        this.alertDate = alertDate;
        this.managerId = managerId;
        this.hrId = hrId;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public LocalDate getAlertDate() {
        return alertDate;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getHrId() {
        return hrId;
    }

    public String getMessage() {
        return message;
    }
}
