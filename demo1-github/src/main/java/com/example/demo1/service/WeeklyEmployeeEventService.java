package com.example.demo1.service;

import com.example.demo1.dto.WeeklyEmployeeEventDto;
import com.example.demo1.model.AlertType;
import com.example.demo1.model.Employee;
import com.example.demo1.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WeeklyEmployeeEventService {

    private final EmployeeRepository employeeRepository;

    public WeeklyEmployeeEventService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<WeeklyEmployeeEventDto> currentWeekEvents(LocalDate referenceDate) {
        LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Employee> employees = employeeRepository.findAll();
        Map<String, Employee> employeesById = employees.stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, Function.identity()));

        List<WeeklyEmployeeEventDto> events = new ArrayList<>();
        for (Employee employee : employees) {
            addBirthdayEvent(employee, employeesById, weekStart, weekEnd, events);
            addWorkAnniversaryEvent(employee, employeesById, weekStart, weekEnd, events);
        }
        return events;
    }

    private void addBirthdayEvent(Employee employee, Map<String, Employee> employeesById,
                                  LocalDate weekStart, LocalDate weekEnd,
                                  List<WeeklyEmployeeEventDto> events) {
        LocalDate eventDate = eventDateForCurrentYear(employee.getDateOfBirth(), weekStart.getYear());
        if (eventDate != null && isBetweenInclusive(eventDate, weekStart, weekEnd)) {
            events.add(toDto(employee, employeesById, AlertType.BIRTHDAY, eventDate, null));
        }
    }

    private void addWorkAnniversaryEvent(Employee employee, Map<String, Employee> employeesById,
                                         LocalDate weekStart, LocalDate weekEnd,
                                         List<WeeklyEmployeeEventDto> events) {
        LocalDate eventDate = eventDateForCurrentYear(employee.getHiringDate(), weekStart.getYear());
        if (eventDate != null
                && employee.getHiringDate().isBefore(eventDate)
                && isBetweenInclusive(eventDate, weekStart, weekEnd)) {
            int years = Period.between(employee.getHiringDate(), eventDate).getYears();
            events.add(toDto(employee, employeesById, AlertType.WORK_ANNIVERSARY, eventDate, years));
        }
    }

    private WeeklyEmployeeEventDto toDto(Employee employee, Map<String, Employee> employeesById,
                                         AlertType eventType, LocalDate eventDate, Integer anniversaryYears) {
        return new WeeklyEmployeeEventDto(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getEmail(),
                eventType,
                eventDate,
                anniversaryYears,
                employee.getManagerId(),
                employeeName(employeesById, employee.getManagerId()),
                employeeEmail(employeesById, employee.getManagerId()),
                employee.getHrId(),
                employeeName(employeesById, employee.getHrId()),
                employeeEmail(employeesById, employee.getHrId())
        );
    }

    private LocalDate eventDateForCurrentYear(LocalDate date, int year) {
        if (date == null) {
            return null;
        }
        MonthDay monthDay = MonthDay.from(date);
        return monthDay.isValidYear(year) ? monthDay.atYear(year) : LocalDate.of(year, 2, 28);
    }

    private boolean isBetweenInclusive(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private String employeeName(Map<String, Employee> employeesById, String employeeId) {
        if (employeeId == null) {
            return null;
        }
        Employee employee = employeesById.get(employeeId);
        return employee == null ? null : employee.getEmployeeName();
    }

    private String employeeEmail(Map<String, Employee> employeesById, String employeeId) {
        if (employeeId == null) {
            return null;
        }
        Employee employee = employeesById.get(employeeId);
        return employee == null ? null : employee.getEmail();
    }
}
