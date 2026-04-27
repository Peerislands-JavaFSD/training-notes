## 1. Introduction to Spring Data JPA

Spring Data JPA simplifies the implementation of data access layers by providing:

* **Predefined repository interfaces** for CRUD operations
* **Query generation from method names**
* **Support for JPQL and native queries**
* **Integration with Spring Boot auto-configuration**

It abstracts most boilerplate code like DAO implementations.

---

## 2. JPARepository vs CRUDRepository

Spring Data JPA provides several repository interfaces:

| Interface                      | Description                                               | Features                                                                                       |
| ------------------------------ | --------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| **CrudRepository**             | Basic CRUD operations                                     | `save()`, `findById()`, `findAll()`, `delete()`, `count()`                                     |
| **JpaRepository**              | Extends `CrudRepository` and `PagingAndSortingRepository` | Adds JPA-specific methods: `flush()`, `saveAndFlush()`, `deleteInBatch()`, pagination, sorting |
| **PagingAndSortingRepository** | Extends `CrudRepository`                                  | Adds `findAll(Pageable pageable)` and sorting features                                         |

**Recommendation:** Use `JpaRepository` in most cases because it provides full CRUD plus pagination and JPA-specific methods.

---

### 2.1 Example: Repository Definitions

```java
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCrudRepository extends CrudRepository<User, Long> {
}

public interface UserJpaRepository extends JpaRepository<User, Long> {
}
```

Both repositories can perform CRUD, but `JpaRepository` adds advanced operations like batch deletes, flush, and pagination.

---

## 3. Property Expressions in Spring Data JPA

Spring Data JPA allows **query methods** derived from method names, known as **property expressions**. The framework parses method names and generates SQL automatically.

### 3.1 Basic Syntax

* Format: `findBy<PropertyName><Operator>`
* Examples:

    * `findByUsername(String username)`
    * `findByAgeGreaterThan(int age)`
    * `findByEmailContaining(String keyword)`

---

### 3.2 Supported Keywords / Operators

| Keyword         | Description                 |
| --------------- | --------------------------- |
| `Is` / `Equals` | Exact match                 |
| `Not`           | Negation                    |
| `Like`          | SQL LIKE operator           |
| `Containing`    | LIKE %value%                |
| `StartingWith`  | LIKE value%                 |
| `EndingWith`    | LIKE %value                 |
| `GreaterThan`   | `>` comparison              |
| `LessThan`      | `<` comparison              |
| `Between`       | Between two values          |
| `OrderBy`       | Sort results                |
| `In`            | SQL IN clause               |
| `IgnoreCase`    | Case-insensitive comparison |
| `And` / `Or`    | Combine multiple conditions |

---

### 3.3 Example: User Repository with Property Expressions

```java
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // Exact match
    List<User> findByUsername(String username);

    // Greater than
    List<User> findByAgeGreaterThan(int age);

    // Between two values
    List<User> findByAgeBetween(int startAge, int endAge);

    // Like / Contains
    List<User> findByEmailContaining(String keyword);

    // Case-insensitive search
    List<User> findByUsernameIgnoreCase(String username);

    // Combining multiple conditions
    List<User> findByUsernameAndAge(String username, int age);

    // Sorting
    List<User> findByAgeGreaterThanOrderByAgeDesc(int age);
}
```

**Key Points:**

* Property expressions eliminate manual JPQL queries for common patterns.
* You can chain conditions with `And` and `Or`.
* Sorting can be applied directly in method names or via `Sort` parameter.

---

## 4. Using Native Queries

Sometimes property expressions are not enough. For complex queries, you can use **JPQL** or **native SQL queries**.

### 4.1 JPQL Example

```java
import org.springframework.data.jpa.repository.Query;

@Query("SELECT u FROM User u WHERE u.age > ?1")
List<User> findUsersOlderThan(int age);
```

### 4.2 Native SQL Example

```java
@Query(value = "SELECT * FROM users WHERE email LIKE %?1%", nativeQuery = true)
List<User> findUsersByEmailKeyword(String keyword);
```

**Notes:**

* `nativeQuery = true` enables raw SQL execution.
* Ensure table and column names match your database.
* Use parameters (`?1`, `?2`) for dynamic values.

---

## 5. Common JPA Annotations

### 5.1 `@Entity`

Marks a class as a JPA entity.

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
}
```

### 5.2 `@Table`

Optional: Specify table name.

```java
@Table(name = "users")
```

### 5.3 `@Id` and `@GeneratedValue`

Marks primary key and auto-generation strategy.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

### 5.4 `@Column`

Customize column mapping.

```java
@Column(name = "user_name", nullable = false, length = 50)
private String username;
```

### 5.5 `@ManyToOne` / `@OneToMany` / `@ManyToMany` / `@OneToOne`

Define relationships between entities.

```java
@ManyToOne
@JoinColumn(name = "role_id")
private Role role;

@OneToMany(mappedBy = "user")
private List<Order> orders;
```

### 5.6 `@JoinColumn`

Specifies foreign key column for a relationship.

---

## 6. Example: Complete User Entity

```java
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;
}
```

---

## 7. Key Takeaways

1. **JPARepository** extends **CrudRepository** with pagination, sorting, and batch operations.
2. **Property expressions** allow generating queries from method names, supporting a wide range of operators (`And`, `Or`, `Like`, `Between`, `OrderBy`, `In`, etc.).
3. **Native queries** are useful for complex SQL not supported by method naming.
4. **Common JPA annotations** define entity mapping, relationships, primary keys, and table/column metadata.
5. Spring Data JPA reduces boilerplate code, improves readability, and accelerates development.
