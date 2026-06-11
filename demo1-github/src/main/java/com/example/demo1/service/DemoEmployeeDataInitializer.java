package com.example.demo1.service;

import com.example.demo1.dto.WeeklyEmployeeEventDto;
import com.example.demo1.model.Employee;
import com.example.demo1.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DemoEmployeeDataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final WeeklyEmployeeEventService weeklyEmployeeEventService;
    private final String employeeSourceUrl;
    private final String demoEmployeeEmail;

    public DemoEmployeeDataInitializer(EmployeeRepository employeeRepository,
                                       WeeklyEmployeeEventService weeklyEmployeeEventService,
                                       @Value("${employee.source.url:}") String employeeSourceUrl,
                                       @Value("${app.demo.employee-email:mohanapriya.saravanan2007@gmail.com}") String demoEmployeeEmail) {
        this.employeeRepository = employeeRepository;
        this.weeklyEmployeeEventService = weeklyEmployeeEventService;
        this.employeeSourceUrl = employeeSourceUrl;
        this.demoEmployeeEmail = demoEmployeeEmail;
    }

    @Override
    public void run(String... args) {
        seedEmployees();
        printCurrentWeekEvents(LocalDate.now());
    }

    private void seedEmployees() {
        if (employeeRepository.count() > 0) {
            return;
        }

        if (employeeSourceUrl != null && !employeeSourceUrl.isBlank()) {
            return;
        }

        employeeRepository.saveAll(List.of(
                new Employee("MGR100", "Meera Manager", demoEmployeeEmail, LocalDate.of(1983, 3, 10),
                        LocalDate.of(2014, 1, 6), "HR100", "HR100"),
                new Employee("MGR101", "Ravi Approver", demoEmployeeEmail, LocalDate.of(1980, 9, 22),
                        LocalDate.of(2012, 2, 13), "HR101", "HR101"),
                new Employee("HR100", "Harini HR", demoEmployeeEmail, LocalDate.of(1978, 11, 2),
                        LocalDate.of(2010, 4, 1), null, null),
                new Employee("HR101", "Karthik HR", demoEmployeeEmail, LocalDate.of(1976, 12, 12),
                        LocalDate.of(2009, 8, 3), null, null),
                new Employee("EMP501", "Priya Raman", demoEmployeeEmail, LocalDate.of(1995, 5, 25),
                        LocalDate.of(2021, 7, 15), "MGR100", "HR100"),
                new Employee("EMP502", "Arun Kumar", demoEmployeeEmail, LocalDate.of(1990, 8, 12),
                        LocalDate.of(2020, 5, 26), "MGR100", "HR100"),
                new Employee("EMP503", "Sneha Iyer", demoEmployeeEmail, LocalDate.of(1997, 5, 27),
                        LocalDate.of(2022, 5, 27), "MGR101", "HR101"),
                new Employee("EMP504", "Vikram Shah", demoEmployeeEmail, LocalDate.of(1989, 5, 31),
                        LocalDate.of(2019, 10, 4), "MGR101", "HR101"),
                new Employee("EMP505", "Nisha Rao", demoEmployeeEmail, LocalDate.of(1994, 1, 9),
                        LocalDate.of(2023, 5, 30), "MGR100", "HR100"),
                new Employee("EMP506", "Outside Week", demoEmployeeEmail, LocalDate.of(1992, 6, 10),
                        LocalDate.of(2020, 6, 15), "MGR101", "HR101")
        ));

        employeeRepository.saveAll(List.of(
                new Employee("EMP601", "Asha Menon", demoEmployeeEmail, LocalDate.of(1992, 6, 1),
                        LocalDate.of(2019, 6, 6), "MGR100", "HR100"),
                new Employee("EMP602", "Rahul Das", demoEmployeeEmail, LocalDate.of(1991, 6, 3),
                        LocalDate.of(2018, 6, 7), "MGR100", "HR100"),
                new Employee("EMP603", "Nandini Rao", demoEmployeeEmail, LocalDate.of(1993, 6, 5),
                        LocalDate.of(2017, 6, 8), "MGR101", "HR101"),
                new Employee("EMP604", "Vivek Iyer", demoEmployeeEmail, LocalDate.of(1988, 6, 7),
                        LocalDate.of(2015, 6, 9), "MGR101", "HR101"),
                new Employee("EMP605", "Priyanka Shah", demoEmployeeEmail, LocalDate.of(1995, 6, 9),
                        LocalDate.of(2021, 6, 10), "MGR100", "HR100"),
                new Employee("EMP606", "Arjun Patel", demoEmployeeEmail, LocalDate.of(1990, 6, 11),
                        LocalDate.of(2016, 6, 12), "MGR100", "HR100"),
                new Employee("EMP607", "Sneha Kulkarni", demoEmployeeEmail, LocalDate.of(1994, 6, 14),
                        LocalDate.of(2020, 6, 15), "MGR101", "HR101"),
                new Employee("EMP608", "Karthik Reddy", demoEmployeeEmail, LocalDate.of(1989, 6, 18),
                        LocalDate.of(2014, 6, 19), "MGR101", "HR101"),
                new Employee("EMP609", "Meera Nair", demoEmployeeEmail, LocalDate.of(1996, 6, 22),
                        LocalDate.of(2017, 6, 23), "MGR100", "HR100"),
                new Employee("EMP610", "Farhan Khan", demoEmployeeEmail, LocalDate.of(1997, 6, 27),
                        LocalDate.of(2018, 6, 28), "MGR101", "HR101")
        ));
    }

    private void printCurrentWeekEvents(LocalDate referenceDate) {
        System.out.println("Employees with birthday or work anniversary in current week:");
        for (WeeklyEmployeeEventDto event : weeklyEmployeeEventService.currentWeekEvents(referenceDate)) {
            System.out.printf(
                    "%s | %s | %s | Event Date: %s | Approver: %s (%s) | HR: %s (%s)%n",
                    event.employeeId(),
                    event.employeeName(),
                    event.eventType(),
                    event.eventDate(),
                    event.approverName(),
                    event.approverId(),
                    event.hrName(),
                    event.hrId()
            );
        }
    }
}
