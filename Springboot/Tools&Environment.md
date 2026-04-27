

# **SPRING BOOT – LOMBOK, DEVTOOLS & ENVIRONMENTS (DETAILED LEARNING MATERIAL)**

---

## **1. Lombok in Spring Boot**

Lombok is a compile-time code generation library that helps eliminate boilerplate code in Java classes. Spring Boot projects often use Lombok because it improves readability and reduces repetitive code such as getters, setters, constructors, and loggers.

---

# **1.1 Why Lombok is Needed**

Traditional Java classes contain repetitive patterns:

* Getters & Setters
* Multiple Constructors
* toString()
* equals() and hashCode()
* Builder pattern
* Logger setup

These patterns reduce productivity and clutter your code. Lombok solves this by generating these elements during compilation.

---

# **1.2 How Lombok Works Internally**

Lombok uses:

* **Annotation Processing Tool (APT)** to modify the AST (Abstract Syntax Tree).
* Injects methods into .class files while compiling.
* IDEs must support annotation processing to show generated methods in autocompletion.

**Important:** IntelliJ users must enable:

```
Settings → Build Tools → Annotation Processing → Enable Annotation Processing
```

---

# **1.3 Common Lombok Annotations**

### ### **a) @Getter and @Setter**

Generates getter and setter methods.

```java
@Getter
@Setter
public class User {
    private String name;
    private int age;
}
```

---

### **b) @NoArgsConstructor, @AllArgsConstructor, @RequiredArgsConstructor**

Constructor generation:

```java
@NoArgsConstructor      // Default constructor
@AllArgsConstructor     // Constructor with all fields
@RequiredArgsConstructor // Constructor for final fields
```

---

### **c) @Data**

A combination annotation that generates:

* Getters + Setters
* toString()
* equals() and hashCode()
* RequiredArgsConstructor

```java
@Data
public class Employee {
    private int id;
    private String department;
}
```

---

### **d) @Builder**

Implements the Builder Pattern.

```java
@Builder
public class Order {
    private int id;
    private String product;
}
```

Usage:

```java
Order order = Order.builder()
        .id(1)
        .product("Laptop")
        .build();
```

---

### **e) @Value (Immutable Type)**

Makes class immutable.

```java
@Value
public class Currency {
    String code;
    double value;
}
```

Generates:

* private final fields
* constructor
* getters
* no setters
* toString, equals, hashCode

---

### **f) @Slf4j**

Logger setup:

```java
@Slf4j
public class PaymentService {
    public void pay() {
        log.info("Payment started");
    }
}
```

---

# **1.4 Lombok Best Practices**

✔ Use `@Data` only for pure DTO/POJO classes.
✔ Avoid using `@Data` for JPA entities — prefer `@Getter` + `@Setter`.
✔ Use `@Builder` for object creation in controllers/services.
✔ Use `@Value` for value objects that must be immutable.
✔ Don’t overuse Lombok for public APIs — prefer explicit methods for clarity.

---

# **1.5 Common Lombok Problems**

| Problem                              | Cause                          | Fix                          |
| ------------------------------------ | ------------------------------ | ---------------------------- |
| Lombok annotations not taking effect | Annotation processing disabled | Enable annotation processing |
| IDE shows errors but code compiles   | IDE plugin not installed       | Install Lombok plugin        |
| @Builder with JPA entity fails       | JPA requires no-arg ctor       | Add @NoArgsConstructor       |

---

---

# ## **2. Spring Boot DevTools**

Spring Boot DevTools enhances **development speed** by monitoring file changes and automatically restarting or refreshing.

---

# **2.1 What DevTools Provides**

### **a) Automatic Restart**

When compiled classes or properties change, the app restarts automatically with a lightweight restart mechanism.

### **b) LiveReload Support**

When static resources (HTML, CSS, JS) change, the browser refreshes automatically (when LiveReload plugin is installed).

### **c) Logging & Template Cache Disabled**

Useful for:

* Thymeleaf
* Freemarker
* Mustache

### **d) Fast Start via ClassLoader Partitioning**

DevTools uses two classloaders:

* **Base ClassLoader** → unchanged libraries
* **Restart ClassLoader** → your application code

Only the restart classloader is discarded on reload → giving faster restarts.

---

# **2.2 DevTools Dependency**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

# **2.3 DevTools Configurations**

### Disable restart:

```
spring.devtools.restart.enabled=false
```

### Exclude specific folders:

```
spring.devtools.restart.exclude=static/**,public/**,templates/**
```

### Disable caching:

```
spring.thymeleaf.cache=false
```

---

# **2.4 DevTools in Production**

Spring Boot automatically disables DevTools features in packaged JAR deployments.

DevTools should **never** be included in production classpath.

---

# **2.5 Common DevTools Issues and Fixes**

| Issue                  | Cause                                  | Fix                       |
| ---------------------- | -------------------------------------- | ------------------------- |
| App not restarting     | IDE not compiling on save              | Enable auto-build         |
| Browser not refreshing | LiveReload plugin missing              | Install browser extension |
| Restart slow           | Heavy libraries in restart classloader | Add excludes              |

---

---

# ## **3. Spring Boot Environments (Profiles)**

Spring Environments allow different configurations based on where the application runs:

* Development
* Testing
* UAT / Staging
* Production

Profiles help isolate settings like:

* Database credentials
* Logging level
* External service URLs
* Caching behavior
* Feature toggles

---

# **3.1 Understanding Spring Profiles**

Spring profile determines which configuration is active at runtime.
Profiles control:

* Which properties file is read
* Which beans get created
* Which integrations activate

---

# **3.2 How Profiles Work**

Spring locates files following this naming pattern:

```
application-{profile}.properties
application-{profile}.yml
```

Examples:

* application-dev.properties
* application-test.properties
* application-prod.properties

---

# **3.3 Activating Profiles**

### **1. application.properties**

```
spring.profiles.active=dev
```

### **2. Command Line**

```
java -jar app.jar --spring.profiles.active=prod
```

### **3. Environment Variable**

```
SPRING_PROFILES_ACTIVE=test
```

### **4. Programmatically**

```java
SpringApplication app = new SpringApplication(MyApp.class);
app.setAdditionalProfiles("dev");
app.run(args);
```

---

# **3.4 Example Property Files**

### **application-dev.properties**

```
server.port=8081
logging.level.root=DEBUG
app.mode=development
```

### **application-prod.properties**

```
server.port=8080
logging.level.root=INFO
app.mode=production
```

---

# **3.5 @Profile Annotation**

Controls bean creation based on active environment.

```java
@Service
@Profile("dev")
public class DevDataService implements DataService {}
```

```java
@Service
@Profile("prod")
public class ProdDataService implements DataService {}
```

Only one of these will load depending on the active profile.

---

# **3.6 Profile-Specific Beans**

### Example: Different Cache for Environments

```java
@Configuration
public class CacheConfig {

    @Bean
    @Profile("dev")
    public Cache simpleCache() {
        return new InMemoryCache();
    }

    @Bean
    @Profile("prod")
    public Cache redisCache() {
        return new RedisCache();
    }
}
```

---

# **3.7 Multi-Profile Activation**

Profiles can be grouped:

### application-dev.properties

```
spring.profiles.include=common
```

### application-common.properties

```
logging.pattern.console=%d{HH:mm:ss} - %msg%n
```

---

# **3.8 Profile-Specific YAML Structure**

```yaml
spring:
  profiles:
    active: dev

---

spring:
  config:
    activate:
      on-profile: dev
server:
  port: 8081

---

spring:
  config:
    activate:
      on-profile: prod
server:
  port: 8080
```

---

# **3.9 Best Practices for Environments**

✔ Keep secrets OUT of property files → use environment variables.
✔ Use dev profile for H2 database & verbose logs.
✔ Use prod profile for external DB, optimized threads, and caching.
✔ Do not commit production keys in Git.
✔ Keep environment switching automated (CI/CD pipelines).

---

# **COMPREHENSIVE SUMMARY**

| Topic                     | What You Learned                                                                                   |
| ------------------------- | -------------------------------------------------------------------------------------------------- |
| **Lombok**                | Removes boilerplate code using annotations; supports builders, logging, immutability               |
| **DevTools**              | Auto-restart, LiveReload, classloader optimizations; speeds development                            |
| **Environments/Profiles** | Manage different settings per environment; use @Profile, property files, and environment variables |

---
