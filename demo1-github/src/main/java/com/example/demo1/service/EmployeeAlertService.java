package com.example.demo1.service;

import com.example.demo1.model.AlertType;
import com.example.demo1.model.Employee;
import com.example.demo1.model.EmployeeAlert;
import com.example.demo1.repository.EmployeeAlertRepository;
import com.example.demo1.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeAlertService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeAlertRepository alertRepository;
    private final NotificationService notificationService;

    public EmployeeAlertService(EmployeeRepository employeeRepository,
                                EmployeeAlertRepository alertRepository,
                                NotificationService notificationService) {
        this.employeeRepository = employeeRepository;
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public List<EmployeeAlert> generateAlerts(LocalDate alertDate) {
        List<EmployeeAlert> generated = new ArrayList<>();

        for (Employee employee : employeeRepository.findAll()) {
            addBirthdayAlert(employee, alertDate, generated);
            addWorkAnniversaryAlert(employee, alertDate, generated);
        }

        return generated;
    }

    @Transactional(readOnly = true)
    public List<EmployeeAlert> alertsForDate(LocalDate alertDate) {
        return alertRepository.findByAlertDateOrderByEmployeeEmployeeNameAsc(alertDate);
    }

    @Transactional(readOnly = true)
    public List<EmployeeAlert> findAll() {
        return alertRepository.findAll();
    }

    private void addBirthdayAlert(Employee employee, LocalDate alertDate, List<EmployeeAlert> generated) {
        if (employee.getDateOfBirth() == null) {
            return;
        }

        for (LocalDate eventDate : eventDatesToNotify(alertDate)) {
            if (MonthDay.from(employee.getDateOfBirth()).equals(MonthDay.from(eventDate))) {
                saveIfNew(employee, AlertType.BIRTHDAY, eventDate, alertDate,
                        "Today is " + employee.getEmployeeName() + "'s Birthday.", generated);
            }
        }
    }

    private void addWorkAnniversaryAlert(Employee employee, LocalDate alertDate, List<EmployeeAlert> generated) {
        if (employee.getHiringDate() == null) {
            return;
        }

        for (LocalDate eventDate : eventDatesToNotify(alertDate)) {
            if (MonthDay.from(employee.getHiringDate()).equals(MonthDay.from(eventDate))
                    && employee.getHiringDate().isBefore(eventDate)) {
                int years = Period.between(employee.getHiringDate(), eventDate).getYears();
                saveIfNew(employee, AlertType.WORK_ANNIVERSARY, eventDate, alertDate,
                        "Today is " + employee.getEmployeeName() + "'s " + ordinal(years) + " Work Anniversary.",
                        generated);
            }
        }
    }

    private List<LocalDate> eventDatesToNotify(LocalDate alertDate) {
        if (alertDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return List.of(alertDate, alertDate.plusDays(1), alertDate.plusDays(2));
        }
        if (alertDate.getDayOfWeek() == DayOfWeek.SATURDAY || alertDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }
        return List.of(alertDate);
    }

    private void saveIfNew(Employee employee, AlertType type, LocalDate eventDate, LocalDate alertDate,
                           String message, List<EmployeeAlert> generated) {
        boolean exists = alertRepository.existsByEmployeeEmployeeIdAndAlertTypeAndEventDateAndAlertDate(
                employee.getEmployeeId(),
                type,
                eventDate,
                alertDate
        );
        if (exists) {
            return;
        }

        EmployeeAlert alert = new EmployeeAlert(
                employee,
                type,
                eventDate,
                alertDate,
                employee.getManagerId(),
                employee.getHrId(),
                message
        );
        EmployeeAlert saved = alertRepository.save(alert);
        notificationService.notifyManagerAndHr(saved);
        generated.add(saved);
    }

    private String ordinal(int value) {
        if (value % 100 >= 11 && value % 100 <= 13) {
            return value + "th";
        }
        return switch (value % 10) {
            case 1 -> value + "st";
            case 2 -> value + "nd";
            case 3 -> value + "rd";
            default -> value + "th";
        };
    }
}
