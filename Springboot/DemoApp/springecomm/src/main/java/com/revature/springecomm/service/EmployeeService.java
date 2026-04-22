package com.revature.springecomm.service;

import com.revature.springecomm.models.Employee;
import com.revature.springecomm.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository repository){
        this.employeeRepository = repository;
    }


    public Employee createEmployee(Employee e){

        return employeeRepository.save(e);
    }

    public List<Employee> getAllEmployees(){
        return employeeRepository.findAll();
    }
    //optional
    public Employee getEmployeeById(Long id){
        return employeeRepository.findById(id).orElseThrow(()->new RuntimeException("Employee not found"));
    }

    public Employee updateEmployee(Long id, Employee emp){
        Employee existing = getEmployeeById(id);

        existing.setDepartment(emp.getDepartment());
        existing.setFirstName(emp.getFirstName());
        existing.setLastName(emp.getLastName());
        existing.setDepartment(emp.getDepartment());
        existing.setEmail(emp.getEmail());

        return employeeRepository.save(existing);
    }

    public void deleteEmployee(Long id){
        Employee existing = getEmployeeById(id);
        employeeRepository.delete(existing);
    }

    public List<Employee> searchEmployee(String department ,double salary){
        return employeeRepository.findByDepartmentAndSalaryGreaterThanEqual(department, salary);
    }

    public Page<Employee> getEmployeePage(){
        Page<Employee> employees = employeeRepository.findAll(PageRequest.of(1, 10));
        return  employees;
    }
}
