package com.example.demo1.repository;

import com.example.demo1.model.AlertType;
import com.example.demo1.model.EmployeeAlert;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeAlertRepository extends JpaRepository<EmployeeAlert, Long> {

    boolean existsByEmployeeEmployeeIdAndAlertTypeAndEventDateAndAlertDate(
            String employeeId,
            AlertType alertType,
            LocalDate eventDate,
            LocalDate alertDate
    );

    @Override
    @EntityGraph(attributePaths = "employee")
    List<EmployeeAlert> findAll();

    @EntityGraph(attributePaths = "employee")
    List<EmployeeAlert> findByAlertDateOrderByEmployeeEmployeeNameAsc(LocalDate alertDate);
}
