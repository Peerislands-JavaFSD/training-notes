package com.revature.springecomm.controller;

import com.revature.springecomm.models.Employee;
import com.revature.springecomm.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }
    //MIME TYPES - content negotiation
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee){
        Employee created = employeeService.createEmployee(employee);
        return new ResponseEntity<>(created, HttpStatus.CREATED);//201
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(){
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployee(@RequestParam(required = false) String email,
                                                         @RequestParam(required = false, defaultValue = "1") double salary,
                                                         @RequestParam(required = false) String department,
                                                         @RequestParam(required = false)String sortBy,
                                                         @RequestParam(required = false, defaultValue = "asc") String sortOrder){
        return ResponseEntity.ok(employeeService.searchEmployee(department, salary));
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id){
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id,
                                                   @RequestBody Employee employee){
        Employee updated = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id){
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Succesfully deleted employee " + id);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> sendNullError(){
        Map<String, String> error = new HashMap<>();
        error.put("status", "500");
        error.put("message", "Please try again later");
        error.put("detail", "Internal server error");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
