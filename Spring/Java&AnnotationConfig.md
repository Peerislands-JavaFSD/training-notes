
# Java & Annotation-Based Configuration for Dependency Injection (Spring)

## 1. What is Java-Based Configuration?

**Java-based configuration** uses **Java classes and annotations** instead of XML to define:

* Beans
* Dependencies
* Wiring rules

Introduced in **Spring 3.x**, this approach is now the **default and recommended** way to configure Spring applications.

Key benefits:

* Type safety
* Refactoring support
* Less configuration errors
* IDE auto-completion

---

## 2. Core Annotations Used

| Annotation       | Purpose                                 |
| ---------------- | --------------------------------------- |
| `@Configuration` | Marks a class as a configuration source |
| `@Bean`          | Declares a Spring-managed bean          |
| `@ComponentScan` | Enables component scanning              |
| `@Component`     | Generic stereotype for beans            |
| `@Service`       | Business layer bean                     |
| `@Repository`    | Data access layer bean                  |
| `@Autowired`     | Injects dependencies                    |
| `@Qualifier`     | Resolves ambiguity                      |
| `@Primary`       | Default bean selection                  |

---

## 3. Basic Project Structure

```
com.example.app
 ├── config
 │   └── AppConfig.java
 ├── service
 │   └── OrderService.java
 ├── repository
 │   └── OrderRepository.java
 └── Main.java
```

---

## 4. Defining Beans Using `@Configuration` and `@Bean`

### Step 1: Create Configuration Class

```java
@Configuration
public class AppConfig {

    @Bean
    public OrderRepository orderRepository() {
        return new OrderRepository();
    }

    @Bean
    public OrderService orderService() {
        return new OrderService(orderRepository());
    }
}
```

### What Happens Internally?

* Spring creates this class as a proxy
* Ensures each `@Bean` method returns a **singleton** by default
* Manages dependencies automatically

---

## 5. Constructor Injection (Recommended)

### Service Class

```java
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public void placeOrder() {
        System.out.println("Order placed");
    }
}
```

### Repository Class

```java
public class OrderRepository {
}
```

---

## 6. Bootstrapping the Spring Context

```java
public class Main {
    public static void main(String[] args) {

        ApplicationContext context =
            new AnnotationConfigApplicationContext(AppConfig.class);

        OrderService service = context.getBean(OrderService.class);
        service.placeOrder();
    }
}
```

---

## 7. Component Scanning with Java Configuration

### Configuration Class

```java
@Configuration
@ComponentScan("com.example.app")
public class AppConfig {
}
```

### Repository

```java
@Repository
public class OrderRepository {
}
```

### Service

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    @Autowired
    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

> From Spring 4.3+, `@Autowired` on constructors is **optional** if only one constructor exists.

---

## 8. Setter Injection Using Java Config

```java
@Service
public class PaymentService {

    private PaymentGateway gateway;

    @Autowired
    public void setGateway(PaymentGateway gateway) {
        this.gateway = gateway;
    }
}
```

**Use Case**

* Optional dependencies
* Runtime reconfiguration

---

## 9. Field Injection (Not Recommended)

```java
@Service
public class EmailService {

    @Autowired
    private MailClient client;
}
```

**Problems**

* Hard to test
* Hidden dependencies
* Breaks immutability

---

## 10. Resolving Multiple Implementations

### Multiple Beans

```java
@Component
public class PaypalPayment implements PaymentGateway {
}

@Component
public class StripePayment implements PaymentGateway {
}
```

### Using `@Qualifier`

```java
@Service
public class PaymentService {

    private final PaymentGateway gateway;

    public PaymentService(@Qualifier("paypalPayment") PaymentGateway gateway) {
        this.gateway = gateway;
    }
}
```

---

## 11. Using `@Primary`

```java
@Component
@Primary
public class StripePayment implements PaymentGateway {
}
```

Spring selects this bean by default when multiple candidates exist.

---

## 12. Bean Scope in Java Configuration

```java
@Bean
@Scope("prototype")
public ReportGenerator reportGenerator() {
    return new ReportGenerator();
}
```

Common scopes:

* `singleton` (default)
* `prototype`
* `request`
* `session`

---


## 13. Java Config vs XML (Quick Comparison)

| Aspect         | Java Config | XML    |
| -------------- | ----------- | ------ |
| Type safety    | Yes         | No     |
| Refactoring    | Easy        | Hard   |
| Readability    | High        | Medium |
| Industry usage | High        | Legacy |

---

## 14. Best Practices Summary

* Prefer **constructor injection**
* Avoid field injection
* Use `@Configuration + @Bean` for external libraries
* Use `@ComponentScan` for application classes
* Use `@Qualifier` sparingly
* Keep configuration minimal

---

## 15. Summary

> Java-based configuration uses `@Configuration` and `@Bean` to define Spring beans in a type-safe way, enabling Dependency Injection without XML and promoting clean, testable code.

---
