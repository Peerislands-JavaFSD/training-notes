## 1. Introduction to Transactions

A **transaction** is a sequence of operations performed as a single logical unit of work. Transactions must satisfy **ACID properties**:

1. **Atomicity** – all operations succeed or none
2. **Consistency** – database remains consistent before and after transaction
3. **Isolation** – concurrent transactions do not interfere
4. **Durability** – once committed, changes are permanent

Spring manages transactions declaratively using `@Transactional`, removing the need to manage `EntityManager` manually.

---

## 2. Enabling Transaction Management

Spring Boot automatically enables transaction management when using `spring-boot-starter-data-jpa`.
For explicit configuration:

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {
}
```

---

## 3. Using @Transactional

`@Transactional` is used to define the **scope of a single database transaction**.

### 3.1 Basic Usage

```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void createUser(User user) {
        repo.save(user);
        // Additional database operations in the same transaction
    }
}
```

**Explanation:**

* If all operations succeed, transaction is committed.
* If an exception occurs, transaction is rolled back automatically.

---

### 3.2 Rollback Behavior

By default:

* **Unchecked exceptions (RuntimeException)** trigger rollback
* **Checked exceptions** do not trigger rollback

You can customize:

```java
@Transactional(rollbackFor = Exception.class)
public void createUserWithCheckedException(User user) throws Exception {
    repo.save(user);
    throw new Exception("Checked exception");
}
```

---

## 4. Transaction Propagation

Propagation defines how **existing transactions are handled** when a transactional method is called from another transactional method.

### 4.1 Propagation Types

| Propagation          | Description                                                            |
| -------------------- | ---------------------------------------------------------------------- |
| `REQUIRED` (default) | Join existing transaction if present, else create new                  |
| `REQUIRES_NEW`       | Suspend existing transaction and create a new one                      |
| `SUPPORTS`           | Join existing transaction if present, else execute non-transactionally |
| `NOT_SUPPORTED`      | Execute non-transactionally, suspend existing transaction              |
| `MANDATORY`          | Must run inside a transaction; throws exception if none exists         |
| `NEVER`              | Must run outside a transaction; throws exception if transaction exists |
| `NESTED`             | Executes within a nested transaction (requires savepoints)             |

---

### 4.2 Example: Propagation in Service Methods

```java
@Service
public class OuterService {

    private final InnerService innerService;
    private final UserRepository repo;

    public OuterService(InnerService innerService, UserRepository repo) {
        this.innerService = innerService;
        this.repo = repo;
    }

    @Transactional
    public void outerMethod() {
        User user1 = new User("Alice", 25);
        repo.save(user1);

        // Call inner method
        innerService.innerMethod();
    }
}

@Service
public class InnerService {

    private final UserRepository repo;

    public InnerService(UserRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void innerMethod() {
        User user2 = new User("Bob", 30);
        repo.save(user2);
        // Even if outerMethod fails, this transaction is committed independently
    }
}
```

**Explanation:**

* `outerMethod` uses default `REQUIRED` propagation.
* `innerMethod` uses `REQUIRES_NEW` – executes in a separate transaction.
* If `outerMethod` fails, `innerMethod` changes are still committed.

---

## 5. Transaction Timeout and Read-Only

### 5.1 Setting Timeout

```java
@Transactional(timeout = 5) // 5 seconds
public void processData() {
    // Operations
}
```

If the transaction takes longer than 5 seconds, it rolls back automatically.

### 5.2 Read-Only Transactions

```java
@Transactional(readOnly = true)
public List<User> getAllUsers() {
    return repo.findAll();
}
```

* Optimizes database for read operations
* Avoids unnecessary flushes

---

## 6. Exception Handling in Transactions

* Runtime exceptions trigger rollback by default
* Checked exceptions do not, unless specified in `rollbackFor`
* Nested transactions can use savepoints to rollback partially

---

## 7. Programmatic Transaction Management

Besides declarative `@Transactional`, Spring allows **programmatic transactions**:

```java
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class ProgrammaticService {

    private final PlatformTransactionManager transactionManager;

    public ProgrammaticService(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void executeProgrammatic() {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // Database operations
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
        }
    }
}
```

**Note:** Declarative `@Transactional` is preferred in most cases.

---

## 8. Best Practices

1. Keep transactional methods in **service layer**, not controller layer.
2. Avoid long-running transactions.
3. Use **REQUIRES_NEW** carefully to isolate critical operations.
4. Use **readOnly = true** for queries to improve performance.
5. Handle exceptions and rollback properly.

---

## 9. Summary

1. **Transactions** ensure ACID properties in your application.
2. `@Transactional` provides declarative transaction management.
3. **Propagation** determines how nested or new transactions behave.
4. `rollbackFor` and `readOnly` customize transaction behavior.
5. Programmatic transaction management is available but declarative is preferred.

