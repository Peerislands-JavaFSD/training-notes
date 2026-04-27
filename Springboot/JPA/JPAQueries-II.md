# JPA Named Queries and Native Queries

## 1. Introduction

In JPA, queries can be written in multiple ways:

* Derived queries (method name queries)
* JPQL queries
* **Named Queries**
* **Native Queries**

This tutorial focuses on **Named Queries** and **Native Queries**, explaining when, why, and how to use them effectively.

---

## 2. What Are Named Queries?

A **Named Query** is a **static, predefined query** defined at the entity level and identified by a unique name.

* Written using **JPQL** (or native SQL in special cases)
* Parsed and validated at application startup
* Reusable across repositories and services

---

## 3. Why Use Named Queries?

### Advantages

* Centralized query definitions
* Early validation at startup
* Improved readability
* Reusability across repositories
* Better maintainability for complex queries

### Limitations

* Static in nature
* Less flexible than dynamic JPQL
* Not ideal for frequently changing queries

---

## 4. Types of Named Queries

1. `@NamedQuery` (JPQL)
2. `@NamedNativeQuery` (SQL)

---

## 5. Named Query Using JPQL

### Entity Example

```java
@Entity
@Table(name = "employees")
@NamedQuery(
    name = "Employee.findByDepartment",
    query = "SELECT e FROM Employee e WHERE e.department = :dept"
)
public class Employee {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String department;
    private Double salary;

    // getters and setters
}
```

### Key Points

* Query uses **entity name and fields**, not table/column names
* `:dept` is a named parameter
* Query is validated at startup

---

## 6. Using Named Query in Repository

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(name = "Employee.findByDepartment")
    List<Employee> findEmployeesByDepartment(
            @Param("dept") String department);
}
```

---

## 7. Executing Named Query via EntityManager

```java
TypedQuery<Employee> query =
        entityManager.createNamedQuery(
                "Employee.findByDepartment", Employee.class);

query.setParameter("dept", "IT");

List<Employee> employees = query.getResultList();
```

---

## 8. Multiple Named Queries

```java
@NamedQueries({
    @NamedQuery(
        name = "Employee.findHighSalary",
        query = "SELECT e FROM Employee e WHERE e.salary > :salary"
    ),
    @NamedQuery(
        name = "Employee.findByName",
        query = "SELECT e FROM Employee e WHERE e.name = :name"
    )
})
```

---

## 9. Named Native Query

### What Is a Native Query?

A **Native Query** is written in **database-specific SQL** and executed directly against the database.

---

## 10. When to Use Native Queries?

* Complex joins or subqueries not easily expressible in JPQL
* Database-specific features
* Performance tuning
* Legacy database integration

---

## 11. Defining Named Native Query

```java
@Entity
@Table(name = "employees")
@NamedNativeQuery(
    name = "Employee.findByDeptNative",
    query = "SELECT * FROM employees WHERE department = :dept",
    resultClass = Employee.class
)
public class Employee {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String department;
    private Double salary;
}
```

---

## 12. Using Named Native Query

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(name = "Employee.findByDeptNative", nativeQuery = true)
    List<Employee> findByDepartmentNative(
            @Param("dept") String department);
}
```

---

## 13. Native Query Using `@Query` Annotation

```java
@Query(
    value = "SELECT * FROM employees WHERE salary > ?1",
    nativeQuery = true
)
List<Employee> findEmployeesWithSalaryAbove(Double salary);
```

---

## 14. Mapping Native Query Results to DTO

### DTO

```java
public class EmployeeSalaryDto {
    private String name;
    private Double salary;
}
```

### Native Query with Projection

```java
@Query(
    value = """
        SELECT name, salary
        FROM employees
        WHERE salary > :salary
    """,
    nativeQuery = true
)
List<Object[]> findEmployeeSalaryRaw(@Param("salary") Double salary);
```

### Manual Mapping

```java
return result.stream()
    .map(row -> new EmployeeSalaryDto(
        (String) row[0],
        (Double) row[1]
    ))
    .toList();
```

---

## 15. Named Native Query with `@SqlResultSetMapping`

```java
@SqlResultSetMapping(
    name = "EmployeeSalaryMapping",
    classes = @ConstructorResult(
        targetClass = EmployeeSalaryDto.class,
        columns = {
            @ColumnResult(name = "name"),
            @ColumnResult(name = "salary")
        }
    )
)
@NamedNativeQuery(
    name = "Employee.salaryReport",
    query = "SELECT name, salary FROM employees",
    resultSetMapping = "EmployeeSalaryMapping"
)
```

---

## 16. Named Query vs JPQL @Query

| Aspect      | Named Query  | JPQL @Query      |
| ----------- | ------------ | ---------------- |
| Defined     | Entity level | Repository level |
| Validation  | Startup      | Runtime          |
| Reuse       | High         | Medium           |
| Flexibility | Low          | High             |

---

## 17. Native Query vs JPQL

| Aspect        | JPQL     | Native SQL |
| ------------- | -------- | ---------- |
| Portability   | High     | Low        |
| DB dependency | No       | Yes        |
| Performance   | Moderate | High       |
| Complex SQL   | Limited  | Full       |

---

## 18. Common Mistakes

* Using table names in JPQL
* Overusing native queries unnecessarily
* Not validating column mappings
* Ignoring SQL injection risks

---

## 19. Best Practices

1. Prefer JPQL or derived queries first
2. Use Named Queries for reusable logic
3. Use Native Queries only when required
4. Always use parameter binding
5. Document complex native queries

---

## 20. Interview Questions

**Q: When are named queries validated?**
At application startup.

**Q: Can named queries be dynamic?**
No, they are static.

**Q: Can native queries return entities?**
Yes, using `resultClass`.

**Q: Why avoid native queries?**
They reduce portability and increase maintenance cost.

---

## 21. Summary

* Named Queries provide reusable, validated JPQL queries
* Native Queries offer full SQL power when needed
* Choose the right approach based on complexity and portability
* Follow best practices for maintainable data access

---
