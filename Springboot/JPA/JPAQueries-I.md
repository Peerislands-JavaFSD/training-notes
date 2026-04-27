# Spring Boot JPA Querying 

## 1. Understanding Querying in Spring Data JPA

Spring Data JPA sits on top of JPA/Hibernate and **generates JPQL automatically** based on:

* Repository method names (Property Expressions)
* Explicit JPQL written using `@Query`

The key idea:

> **You never write SQL directly**.
> You work with **entities and fields**, not tables and columns.

---

## 2. Reference Domain Model (Used Throughout)

### Employee Entity

```java
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String department;
    private Double salary;
    private Boolean active;
}
```

Important:

* Query methods use **field names**, not column names
* Case-sensitive with respect to Java fields

---

# PART A – PROPERTY EXPRESSIONS (DERIVED QUERY METHODS)

---

## 3. What Are Property Expressions?

Property expressions allow Spring Data JPA to:

* Read method names
* Parse property names
* Build JPQL automatically

Example:

```java
List<Employee> findByDepartment(String department);
```

Internally converted to:

```jpql
SELECT e FROM Employee e WHERE e.department = ?
```

---

## 4. Anatomy of a Derived Query Method

```text
<Prefix>By<Property><Condition><LogicalOperator><Property>
```

### Common Prefixes

| Prefix  | Meaning           |
| ------- | ----------------- |
| findBy  | Retrieve data     |
| getBy   | Same as find      |
| readBy  | Semantic clarity  |
| queryBy | Explicit querying |

---

## 5. Simple Property Queries

```java
List<Employee> findByDepartment(String department);

Employee findByEmail(String email);

List<Employee> findByActive(Boolean active);
```

Rules:

* Parameter order must match method name order
* Return type can be `List`, single entity, or `Optional`

---

## 6. Multiple Conditions (AND / OR)

```java
List<Employee> findByDepartmentAndActive(String dept, Boolean active);

List<Employee> findByDepartmentOrActive(String dept, Boolean active);
```

Generated JPQL:

```jpql
WHERE department = ? AND active = ?
```

---

## 7. Comparison Keywords (Extensive)

### Numeric Comparisons

```java
List<Employee> findBySalaryGreaterThan(Double salary);

List<Employee> findBySalaryGreaterThanEqual(Double salary);

List<Employee> findBySalaryLessThan(Double salary);

List<Employee> findBySalaryBetween(Double min, Double max);
```

---

### String Matching Queries

```java
List<Employee> findByNameLike(String pattern);

List<Employee> findByNameContaining(String text);

List<Employee> findByNameStartingWith(String prefix);

List<Employee> findByNameEndingWith(String suffix);
```

Example:

```java
findByNameContaining("an") → Anand, Sanjay
```

---

## 8. Case Handling

```java
List<Employee> findByDepartmentIgnoreCase(String department);

List<Employee> findByNameContainingIgnoreCase(String name);
```

Enterprise usage:

* Search APIs
* Case-insensitive filters

---

## 9. IN, NOT IN, NULL Checks

```java
List<Employee> findByDepartmentIn(List<String> departments);

List<Employee> findByDepartmentNotIn(List<String> departments);

List<Employee> findByEmailIsNull();

List<Employee> findByEmailIsNotNull();
```

---

## 10. Boolean & Flag-Based Queries

```java
List<Employee> findByActiveTrue();

List<Employee> findByActiveFalse();
```

Cleaner than:

```java
findByActive(true)
```

---

## 11. Ordering & Sorting (Static)

```java
List<Employee> findByDepartmentOrderBySalaryDesc(String dept);

List<Employee> findByActiveOrderByDepartmentAscSalaryDesc(Boolean active);
```

Note:

* Static sorting only
* Not flexible for APIs

---

## 12. Count, Exists & Top Queries

```java
long countByDepartment(String dept);

boolean existsByEmail(String email);

Employee findTopByDepartmentOrderBySalaryDesc(String dept);

List<Employee> findTop3ByDepartmentOrderBySalaryDesc(String dept);
```

Used for:

* Validation
* Ranking logic
* Dashboards

---

## 13. When Property Expressions Become a Problem

Example:

```java
findByDepartmentAndSalaryGreaterThanAndActiveAndNameContainingIgnoreCase
```

Problems:

* Hard to read
* Hard to maintain
* Business logic hidden in method name

 This is where **JPQL** is preferred.

---

# PART B – JPQL USING `@Query`

---

## 14. What Is JPQL?

JPQL (Java Persistence Query Language):

* Works on **entities**
* Uses **Java field names**
* Database independent

Syntax looks like SQL, but it is **NOT SQL**.

---

## 15. Basic JPQL with `@Query`

```java
@Query("SELECT e FROM Employee e WHERE e.department = :dept")
List<Employee> findEmployeesByDepartment(@Param("dept") String department);
```

Key points:

* `Employee` → entity class name
* `e.department` → Java field
* `:dept` → named parameter

---

## 16. Positional vs Named Parameters

### Positional

```java
@Query("SELECT e FROM Employee e WHERE e.department = ?1")
List<Employee> findByDept(String dept);
```

### Named (Recommended)

```java
@Query("SELECT e FROM Employee e WHERE e.department = :dept")
List<Employee> findByDept(@Param("dept") String dept);
```

---

## 17. Multiple Conditions in JPQL

```java
@Query("""
       SELECT e FROM Employee e
       WHERE e.department = :dept
       AND e.salary > :salary
       AND e.active = true
       """)
List<Employee> findQualifiedEmployees(
    @Param("dept") String dept,
    @Param("salary") Double salary
);
```

Benefits:

* Readable
* Business logic explicit

---

## 18. Dynamic Conditions Using NULL Checks

Used in **search APIs**.

```java
@Query("""
       SELECT e FROM Employee e
       WHERE (:dept IS NULL OR e.department = :dept)
       AND (:active IS NULL OR e.active = :active)
       """)
List<Employee> searchEmployees(
    @Param("dept") String dept,
    @Param("active") Boolean active
);
```

---

## 19. LIKE Queries in JPQL

```java
@Query("SELECT e FROM Employee e WHERE e.name LIKE %:name%")
List<Employee> searchByName(@Param("name") String name);
```

Case-insensitive version:

```java
@Query("""
       SELECT e FROM Employee e
       WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))
       """)
List<Employee> searchByNameIgnoreCase(@Param("name") String name);
```

---

## 20. Selecting Partial Data (Projections)

```java
@Query("SELECT e.name, e.email FROM Employee e WHERE e.active = true")
List<Object[]> findActiveEmployeeContacts();
```

Usage:

```java
for (Object[] row : results) {
    String name = (String) row[0];
    String email = (String) row[1];
}
```

---

## 21. Update Queries with JPQL

```java
@Modifying
@Transactional
@Query("""
       UPDATE Employee e
       SET e.salary = e.salary + :increment
       WHERE e.department = :dept
       """)
int incrementSalary(
    @Param("increment") Double increment,
    @Param("dept") String dept
);
```

Rules:

* `@Modifying` is mandatory
* Must be transactional
* Returns number of rows updated

---

## 22. Delete Queries with JPQL

```java
@Modifying
@Transactional
@Query("DELETE FROM Employee e WHERE e.active = false")
int deleteInactiveEmployees();
```

---

## 23. Property Expressions vs JPQL – Decision Guide

| Scenario            | Recommended          |
| ------------------- | -------------------- |
| Simple filters      | Property Expressions |
| Multiple conditions | JPQL                 |
| Readability matters | JPQL                 |
| Search APIs         | JPQL                 |
| Rapid CRUD          | Property Expressions |

---
