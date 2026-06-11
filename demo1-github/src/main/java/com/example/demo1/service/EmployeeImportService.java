package com.example.demo1.service;

import com.example.demo1.dto.EmployeeDto;
import com.example.demo1.model.Employee;
import com.example.demo1.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmployeeImportService {

    private static final int MINIMUM_COLUMNS = 6;

    private final EmployeeRepository employeeRepository;

    public EmployeeImportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public List<Employee> importCsv(String csv) {
        List<Employee> imported = new ArrayList<>();
        String[] lines = csv.split("\\R");

        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.isEmpty() || isHeader(line)) {
                continue;
            }

            String[] columns = line.split(",", -1);
            if (columns.length < MINIMUM_COLUMNS) {
                throw new IllegalArgumentException("Invalid employee CSV at line " + (index + 1));
            }

            String employeeId = blankToNull(columns[0]);
            if (employeeId == null) {
                continue;
            }

            Employee parsed = new Employee(
                    employeeId,
                    columns[1].trim(),
                    email(columns),
                    parseDate(columns[2]),
                    parseDate(columns[3]),
                    blankToNull(columns[4]),
                    blankToNull(columns[5])
            );

            Employee employee = employeeRepository.findById(employeeId)
                    .map(existing -> {
                        existing.updateFrom(parsed);
                        return existing;
                    })
                    .orElse(parsed);

            imported.add(employeeRepository.save(employee));
        }

        return imported;
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> findAllDtos() {
        return toDtos(employeeRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> toDtos(List<Employee> employees) {
        Map<String, Employee> employeesById = employeeRepository.findAll().stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, Function.identity()));

        return employees.stream()
                .map(employee -> new EmployeeDto(
                        employee.getEmployeeId(),
                        employee.getEmployeeName(),
                        employee.getEmail(),
                        employee.getDateOfBirth(),
                        employee.getHiringDate(),
                        employee.getManagerId(),
                        nameOf(employeesById, employee.getManagerId()),
                        employee.getHrId(),
                        nameOf(employeesById, employee.getHrId())
                ))
                .toList();
    }

    private String nameOf(Map<String, Employee> employeesById, String employeeId) {
        if (employeeId == null) {
            return null;
        }
        Employee employee = employeesById.get(employeeId);
        return employee == null ? null : employee.getEmployeeName();
    }

    private boolean isHeader(String line) {
        return line.toLowerCase().startsWith("employee_id,employee_name");
    }

    private String email(String[] columns) {
        return columns.length > 6 ? blankToNull(columns[6]) : null;
    }

    private LocalDate parseDate(String value) {
        String normalized = value.trim();
        return normalized.isEmpty() ? null : LocalDate.parse(normalized);
    }

    private String blankToNull(String value) {
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
