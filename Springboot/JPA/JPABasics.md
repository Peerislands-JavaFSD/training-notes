# Spring Boot JPA Basics
---

## 1. What is JPA?

**JPA (Java Persistence API)** is a **specification** that defines how Java objects are mapped to relational database tables.

Key points:

* JPA is **not an implementation**
* It defines rules for:

    * Mapping Java classes to DB tables
    * Mapping fields to columns
    * Managing relationships
    * Querying data

### Popular JPA Implementations

* **Hibernate** (most commonly used)
* EclipseLink
* OpenJPA

👉 In Spring Boot, **Hibernate is the default JPA provider**.

---

## 2. Why Use JPA with Spring Boot?

Spring Boot + JPA gives:

* Minimal configuration
* Automatic EntityManager setup
* Transaction management
* Clean ORM abstraction

Without JPA:

* You manually write SQL
* You manually map ResultSet → objects

With JPA:

* Java objects ↔ database tables automatically

---

## 3. Basic JPA Terminology

| Term          | Meaning                       |
| ------------- | ----------------------------- |
| Entity        | Java class mapped to DB table |
| Persistence   | Storing Java objects in DB    |
| ORM           | Object Relational Mapping     |
| EntityManager | Core JPA interface            |
| Hibernate     | JPA implementation            |

---

## 4. JPA Entity Basics

### 4.1 What is an Entity?

An **Entity** is a **persistent Java class** mapped to a database table.

### Mandatory Rules for Entity

* Must be annotated with `@Entity`
* Must have a **primary key**
* Must have a **default constructor**
* Should be a POJO (no business logic)

---

## 5. Creating a Simple Entity

```java
@Entity
@Table(name = "users")
public class User {
}
```

### Explanation

* `@Entity` → Marks the class as a JPA entity
* `@Table` → Maps entity to a specific table name

If `@Table` is not used:

* Table name defaults to class name (`user`)

---

## 6. Primary Key Mapping (Most Important)

### 6.1 What is a Primary Key?

* Uniquely identifies each row
* Mandatory for every entity
* Immutable once persisted (best practice)

---

### 6.2 @Id Annotation

```java
@Id
private Long id;
```

* Marks the primary key field
* Exactly **one per entity**

---

## 7. Primary Key Generation Strategies

### 7.1 @GeneratedValue

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

### Common Strategies

| Strategy | Description                      |
| -------- | -------------------------------- |
| IDENTITY | DB auto-increment (MySQL)        |
| SEQUENCE | DB sequence (PostgreSQL, Oracle) |
| TABLE    | Table-based id generation        |
| AUTO     | Provider chooses best strategy   |

👉 **IDENTITY** is most commonly used in Spring Boot.

---

## 8. Column Mapping Basics

```java
@Column(nullable = false, unique = true)
private String email;
```

### Common @Column Attributes

| Attribute | Purpose             |
| --------- | ------------------- |
| nullable  | NOT NULL constraint |
| unique    | UNIQUE constraint   |
| length    | Column size         |
| name      | Custom column name  |

---

## 9. Complete Simple Entity Example

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // getters and setters
}
```

---

## 10. Foreign Key & Relationships (Core JPA Topic)

### 10.1 Why Relationships?

Real systems have:

* User → Orders
* Project → Tasks
* Customer → Address

JPA models these using **object references**, not foreign key IDs.

---

## 11. Relationship Types in JPA

| Relationship | Meaning      |
| ------------ | ------------ |
| @OneToOne    | One-to-One   |
| @OneToMany   | One-to-Many  |
| @ManyToOne   | Many-to-One  |
| @ManyToMany  | Many-to-Many |

---

## 12. @ManyToOne (Most Common)

### Example: Many Tasks → One User

```java
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
```

### Explanation

* `@ManyToOne` → Many tasks belong to one user
* `@JoinColumn` → Creates foreign key column `user_id`
* DB column: `tasks.user_id`

👉 This is the **owning side** of the relationship.

---

## 13. @OneToMany (Inverse Side)

```java
@Entity
@Table(name = "users")
public class User {

    @OneToMany(mappedBy = "user")
    private List<Task> tasks;
}
```

### Key Concepts

* `mappedBy` → Points to owning field
* No foreign key column here
* Avoids duplicate join columns

---

## 14. Bidirectional Relationship Summary

```
User (1) ←→ (Many) Task
```

* Foreign key exists in **Task table**
* `Task` = owning side
* `User` = inverse side

---

## 15. @OneToOne Relationship

```java
@OneToOne
@JoinColumn(name = "profile_id")
private Profile profile;
```

* One entity references one row
* Foreign key stored in owning entity

---

## 16. @ManyToMany (Use Carefully)

```java
@ManyToMany
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles;
```

⚠️ **Avoid ManyToMany in real projects**

* Hard to maintain
* Difficult to extend
* Prefer join entity instead

---

## 17. Enum Mapping

```java
@Enumerated(EnumType.STRING)
private Status status;
```

Why STRING?

* Safer
* Readable
* Order changes won’t break DB

---

## 18. Date & Time Mapping

```java
private LocalDate createdDate;
private LocalDateTime updatedAt;
```

Best practice:

* Use **java.time API**
* Avoid `java.util.Date`

---

## 19. Cascade Types (Important Concept)

```java
@OneToMany(cascade = CascadeType.ALL)
```

### Common Cascade Types

| Type    | Effect                     |
| ------- | -------------------------- |
| PERSIST | Save child automatically   |
| REMOVE  | Delete child automatically |
| ALL     | All operations             |

Use cascade **carefully**, especially REMOVE.

---

## 20. Fetch Types

```java
@ManyToOne(fetch = FetchType.LAZY)
```

### Fetch Types

| Type  | Behavior           |
| ----- | ------------------ |
| LAZY  | Load when accessed |
| EAGER | Load immediately   |

👉 Default:

* `ManyToOne` → EAGER
* `OneToMany` → LAZY

**Best practice:** Prefer LAZY.

---
