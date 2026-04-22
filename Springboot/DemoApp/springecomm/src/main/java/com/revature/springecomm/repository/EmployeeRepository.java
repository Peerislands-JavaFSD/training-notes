package com.revature.springecomm.repository;

import com.revature.springecomm.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
// component

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    //property expression
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartment(String dept);
    List<Employee> findBySalaryGreaterThanEqual(double salary);
    boolean existsByEmail(String email);
    List<Employee> findByFirstNameOrLastName(String firtName, String LastName);
    List<Employee> findByFirstNameLike(String firstName);
    List<Employee> findByEmailIsNotNull();
    List<Employee> findByEmailOrderBySalaryAsc(String email);
    List<Employee> findByDepartmentAndSalaryGreaterThanEqual(String department, double salary);

    //jpql
    @Query("SELECT e FROM Employee e WHERE e.department = :dept ORDER BY e.salary DESC")
    List<Employee> findByDepartmentOrderBySalary(@Param("dept") String department);

    @Query("SELECT e FROM Employee e INNER JOIN e.department d WHERE e.id = :id")
    List<Employee> getEmployeeWithId(@Param("id") Long id);
//
//    //native
//    @Query(value = "SELECT * FROM employees WHERE department = :dept ORDER BY salary DESC", nativeQuery = true)
//



}
