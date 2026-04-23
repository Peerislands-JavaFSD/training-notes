package com.revature.springecomm.models;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class EmployeeValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Employee.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Employee emp = (Employee) target;

        if(emp.getFirstName() == null || emp.getFirstName().isEmpty()){
            errors.rejectValue("fistName", "firstName.empty", "First name is required");
        }

        if(emp.getSalary() < 10000) {
            errors.rejectValue("salary", "salary.invalid", "Salary must be greadter than 10000");
        }


    }
}
