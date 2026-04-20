
## 1. What is Dependency Injection (DI)?

**Dependency Injection (DI)** is a design principle in which an object’s dependencies are **provided from the outside**, rather than the object creating them itself.

In simple terms:

* A class **does not create** the objects it depends on
* Dependencies are **injected** by a container or framework

This leads to:

* Loose coupling
* Better testability
* Easier maintenance
* Improved flexibility

### Without DI (Tight Coupling)

```java
class OrderService {
    private PaymentService paymentService = new PaymentService();
}
```

### With DI (Loose Coupling)

```java
class OrderService {
    private PaymentService paymentService;

    OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

---

## 2. What is Inversion of Control (IoC)?

**Inversion of Control (IoC)** is a **broader design principle** where the control of:

* Object creation
* Object lifecycle
* Dependency management

is **inverted** from the application code to a **container or framework**.

### Traditional Control

* Application creates objects
* Application manages lifecycle

### IoC Control

* Framework creates objects
* Framework manages lifecycle
* Framework injects dependencies

> **DI is the most common way to achieve IoC**

---

## 3. Relationship Between IoC and DI

| Concept          | Description                         |
| ---------------- | ----------------------------------- |
| IoC              | Principle (what is inverted)        |
| DI               | Pattern (how inversion is achieved) |
| Spring Container | Implements IoC using DI             |

**IoC = What**
**DI = How**

---

## 4. Types of Dependency Injection

### 1. Constructor Injection (Recommended)

Dependencies are provided through the constructor.

```java
class UserService {
    private final UserRepository repo;

    UserService(UserRepository repo) {
        this.repo = repo;
    }
}
```

**Advantages**

* Ensures immutability
* Mandatory dependencies are enforced
* Best for unit testing
* Preferred by Spring

---

### 2. Setter Injection

Dependencies are injected using setter methods.

```java
class UserService {
    private UserRepository repo;

    void setRepo(UserRepository repo) {
        this.repo = repo;
    }
}
```

**Advantages**

* Useful for optional dependencies
* Allows reconfiguration

**Disadvantages**

* Dependency may be unset
* Less safe than constructor injection

---

### 3. Field Injection (Not Recommended)

Dependencies are injected directly into fields.

```java
class UserService {
    @Autowired
    private UserRepository repo;
}
```

**Disadvantages**

* Breaks encapsulation
* Hard to unit test
* Hidden dependencies

**Usage**

* Suitable only for quick prototypes or tests

---

## 5. How Spring Implements IoC and DI

### Spring IoC Container Responsibilities

* Instantiate beans
* Resolve dependencies
* Inject dependencies
* Manage bean lifecycle
* Apply AOP proxies

### Bean Definition Sources

* Annotations (`@Component`, `@Service`)
* Java Config (`@Bean`)
* XML (legacy)

---

## 6. Real-World Analogy

**Without DI**
A person cooks everything themselves.

**With DI**
Food is delivered by a service.

* Person → Business logic
* Food service → Spring container
* Food → Dependency

---

## 7. DI Benefits Summary

* Loose coupling
* Easier unit testing
* Better readability
* Faster development
* Supports SOLID principles (especially DIP)

---

## 8. Summary

> **Dependency Injection** is a design pattern where dependencies are supplied externally, and **IoC** is the principle where the framework controls object creation and lifecycle instead of the application.

---
