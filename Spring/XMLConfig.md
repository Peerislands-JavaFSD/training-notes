
# XML-Based Configuration for Dependency Injection (Spring)

## 1. What is XML-Based Configuration?

**XML-based configuration** is the traditional way of configuring Spring applications, where:

* Beans
* Dependencies
* Scopes
* Lifecycle callbacks

are defined in an **XML file** instead of Java code or annotations.

Although modern projects prefer Java/Annotation configuration, **XML is still important** for:

* Legacy enterprise applications
* Externalized configuration
* Interview questions
* Clear separation of configuration from code

---

## 2. When XML Configuration Is Used

* Old Spring applications (pre-Spring Boot)
* Large enterprise systems with strict config separation
* When source code modification is restricted
* For infrastructure-level configuration

---

## 3. Basic Project Structure

```
src
 ├── main
 │   ├── java
 │   │   └── com.example.app
 │   │       ├── service
 │   │       │   └── OrderService.java
 │   │       └── repository
 │   │           └── OrderRepository.java
 │   └── resources
 │       └── applicationContext.xml
 └── Main.java
```

---

## 4. Spring XML Configuration File

### applicationContext.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

</beans>
```

This file defines the **Spring IoC container configuration**.

---

## 5. Defining a Simple Bean

### Java Class

```java
public class OrderRepository {
}
```

### XML Bean Definition

```xml
<bean id="orderRepository"
      class="com.example.app.repository.OrderRepository"/>
```

* `id` → Bean name
* `class` → Fully qualified class name

---

## 6. Constructor Injection Using XML

### Service Class

```java
public class OrderService {

    private OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

### XML Configuration

```xml
<bean id="orderService"
      class="com.example.app.service.OrderService">
    <constructor-arg ref="orderRepository"/>
</bean>
```

**How it works**

* Spring creates `OrderRepository`
* Injects it into `OrderService` constructor

---

## 7. Setter Injection Using XML

### Service Class

```java
public class PaymentService {

    private PaymentGateway gateway;

    public void setGateway(PaymentGateway gateway) {
        this.gateway = gateway;
    }
}
```

### XML Configuration

```xml
<bean id="paymentService"
      class="com.example.app.service.PaymentService">
    <property name="gateway" ref="paymentGateway"/>
</bean>
```

---

## 8. Injecting Primitive and String Values

### Class

```java
public class EmailService {

    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
```

### XML Configuration

```xml
<bean id="emailService"
      class="com.example.app.service.EmailService">
    <property name="host" value="smtp.gmail.com"/>
    <property name="port" value="587"/>
</bean>
```

---

## 9. Injecting Collections

### Class

```java
public class ReportService {

    private List<String> formats;

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }
}
```

### XML Configuration

```xml
<bean id="reportService"
      class="com.example.app.service.ReportService">
    <property name="formats">
        <list>
            <value>PDF</value>
            <value>EXCEL</value>
            <value>CSV</value>
        </list>
    </property>
</bean>
```

Supported collections:

* `<list>`
* `<set>`
* `<map>`
* `<props>`

---

## 10. Bean Scope Configuration

```xml
<bean id="cacheManager"
      class="com.example.app.CacheManager"
      scope="prototype"/>
```

Common scopes:

* `singleton` (default)
* `prototype`
* `request`
* `session`

---


## 11. Using `ApplicationContext`

```java
public class Main {
    public static void main(String[] args) {

        ApplicationContext context =
            new ClassPathXmlApplicationContext("applicationContext.xml");

        OrderService service = context.getBean("orderService", OrderService.class);
    }
}
```

---

## 12. XML vs Java Config (Quick Comparison)

| Aspect       | XML    | Java Config |
| ------------ | ------ | ----------- |
| Verbosity    | High   | Low         |
| Type Safety  | No     | Yes         |
| Refactoring  | Hard   | Easy        |
| Industry Use | Legacy | Modern      |

---

## 13. Common XML Configuration Mistakes

* Wrong fully qualified class names
* Missing setter methods
* Circular dependencies
* Incorrect schema declarations
* Overusing autowire

---

## 14.Summary

> XML-based configuration defines Spring beans and their dependencies declaratively in XML, providing clear separation between configuration and application code, but with higher verbosity and lower type safety compared to Java-based configuration.

---
