package com.reliaquest.api.controller;

import com.reliaquest.api.model.EmployeeModel;
import java.util.List;
import com.reliaquest.api.Interfaces.IEmployeeService;
import com.reliaquest.api.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/employees")
public class EmployeeController implements IEmployeeController<EmployeeModel, EmployeeModel> {

    @Autowired
    private IEmployeeService employeeService;

    private IEmployeeService empSvc() {
        if (employeeService == null)
            employeeService = new EmployeeService();
        return employeeService;
    }

    @Override
    @GetMapping()
    public ResponseEntity<List<EmployeeModel>> getAllEmployees() {
        return ResponseEntity.ok(empSvc().getAllEmployees());
    }

    @Override
    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<EmployeeModel>> getEmployeesByNameSearch(@PathVariable String searchString) {
        return ResponseEntity.ok(empSvc().getEmployeesByNameSearch(searchString));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeModel> getEmployeeById(@PathVariable String id) {
        EmployeeModel emp = empSvc().getEmployeeById(id);
        return ResponseEntity.ok(emp);
    }

    @Override
    @GetMapping("/highestSalary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(empSvc().getHighestSalaryOfEmployees());
    }

    @Override
    @GetMapping("/topTenHighestEarningEmployeeNames")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(empSvc().getTopTenHighestEarningEmployeeNames());
    }

    @Override
    @PostMapping()
    public ResponseEntity<EmployeeModel> createEmployee(@RequestBody EmployeeModel employeeInput) {
        String createdId = empSvc().createEmployee(employeeInput);
        EmployeeModel result = new EmployeeModel();
        try {
            result.setId(UUID.fromString(createdId));
        } catch (Exception ex) {
            // ignore parse errors; return model with null id
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        String name = empSvc().deleteEmployeeById(id);
        return ResponseEntity.ok(name);
    }
}
