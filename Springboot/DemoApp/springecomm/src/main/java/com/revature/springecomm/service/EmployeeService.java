package com.revature.springecomm.service;

import com.revature.springecomm.models.Employee;
import com.revature.springecomm.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        //DML
        //DML
        return employeeRepository.findById(id).orElseThrow(()->new RuntimeException("Employee not found"));
    }

    //NESTED => Savepoint, NEVER => No trx, MANDATORY => trx, NOT_SUPPORTED, SUPPORT, REQUIRES_NEW, REQUIRED
    @Transactional(propagation = Propagation.NESTED)
    public Employee updateEmployee(Long id, Employee emp){
        Employee existing = getEmployeeById(id);//findById
        for(int i = 0; i < 100; )
        existing.setDepartment(emp.getDepartment());
        existing.setFirstName(emp.getFirstName());
        existing.setLastName(emp.getLastName());
        existing.setDepartment(emp.getDepartment());
        existing.setEmail(emp.getEmail());
        //DML
//        throw new RuntimeException();

        return employeeRepository.save(existing);
    }
    //ACID->
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
