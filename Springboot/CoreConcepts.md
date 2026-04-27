

# **1. Spring Boot – Introduction**

Spring Boot is a framework built on top of the core Spring Framework that makes it **easier and faster** to build production-ready Java applications.

Traditional Spring applications required a lot of manual configuration (XML files, bean definitions, dependency management). Spring Boot solves this by:

### ✔ Minimizing configuration

### ✔ Providing defaults for common patterns

### ✔ Embedding a server (Tomcat/Jetty/Netty)

### ✔ Offering production-ready features (metrics, health checks, logging)

---

# **2. Why Spring Boot?**

### **a) Less Boilerplate**

No need to write web.xml, server.xml, or multiple configuration files.

### **b) Auto-configured Web Server**

Spring Boot provides an **embedded Tomcat** (default) so the application runs directly using:

```
java -jar app.jar
```

### **c) Opinionated Defaults**

Spring Boot provides sensible defaults for:

* JSON serialization
* Logging
* Dependency versions
* Build plugins
* Application properties

### **d) Production Ready**

Includes Actuator for:

* health checks
* environment info
* metrics
* thread dumps
* readiness/liveness probes

### **e) Easy Integration**

Integrates seamlessly with:

* Spring Data JPA
* Spring Security
* Spring MVC
* Spring Cloud
* Messaging brokers (Kafka/RabbitMQ)
* Databases

---

# **3. Key Features of Spring Boot**

| Feature                  | Description                                                    |
| ------------------------ | -------------------------------------------------------------- |
| **Auto-Configuration**   | Automatically configures beans based on classpath & properties |
| **Starter Dependencies** | Predefined dependency bundles for common use cases             |
| **Embedded Servers**     | Tomcat, Jetty, Netty                                           |
| **Spring Boot CLI**      | Command-line support for Groovy scripts                        |
| **Spring Actuator**      | Monitoring/management endpoints                                |

---

# **4. Spring Boot Starter Dependencies (MOST COMMON)**

Starter dependencies are **curated dependency bundles** that simplify adding libraries.

###  Web Applications

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Contains:

* Spring MVC
* Jackson for JSON
* Embedded Tomcat

---

###  Data Access (SQL)

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

Contains:

* Hibernate
* Spring Data JPA
* Transaction management

---

###  Databases (H2)

```xml
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>
```

---

###  Validation

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Uses Hibernate Validator → @Valid, @NotNull, @Size, etc.

---

###  Security

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Contains:

* Authentication/Authorization filters
* BCryptPasswordEncoder

---

###  Actuator

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

###  Test

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```

Contains:

* JUnit 5
* Mockito
* Spring Test

---

# **5. Spring Initializr (start.spring.io)**

Spring Initializr is an online project generator for Spring Boot.

### Steps:

### **1. Choose Project Type**

* Maven project (most common)
* Gradle project

### **2. Choose Language**

* Java (default)
* Kotlin
* Groovy

### **3. Spring Boot Version**

Choose stable version (e.g., 3.2.x)

### **4. Project Metadata**

* Group: `com.example`
* Artifact: `demo`
* Name: `DemoApplication`
* Packaging: `Jar`
* Java Version: 17/21

### **5. Select Dependencies**

Common dependencies:

* Spring Web
* Spring Data JPA
* H2 Database
* Validation
* Lombok
* Actuator

### **6. Generate the Project**

Download the ZIP → Unzip → Import in IntelliJ/Eclipse/VS Code.

Initializr ensures:

* Correct folder structure
* Maven/Gradle configuration
* Spring Boot starter dependencies
* Application class with `@SpringBootApplication`

---

# **6. Spring Boot Auto-Configuration (Core Concept)**

Auto-configuration is the heart of Spring Boot.

### **What is Auto-Configuration?**

Spring Boot automatically configures components based on:

1. **Classpath dependencies**
2. **Default configurations**
3. **Your custom application.properties**

Example:
If `spring-boot-starter-web` is added:

* Spring Boot detects Spring MVC
* Configures DispatcherServlet
* Configures ObjectMapper
* Sets up default error handling
* Starts embedded Tomcat

You don’t configure any of this manually.

---

## **6.1 How does Auto-Configuration work?**

Enabled using:

```java
@SpringBootApplication
```

This annotation =
`@Configuration + @EnableAutoConfiguration + @ComponentScan`

### ✔ `@EnableAutoConfiguration`

Tells Spring Boot to search for auto-config classes located under:

```
META-INF/spring/org.springframework.boot.autoconfigure.*
```

### ✔ Condition Annotations

Auto-config uses “Conditional” logic:

| Annotation                  | Meaning                                               |
| --------------------------- | ----------------------------------------------------- |
| `@ConditionalOnClass`       | Apply config only if class exists in classpath        |
| `@ConditionalOnMissingBean` | Apply config only if developer has NOT created a bean |
| `@ConditionalOnProperty`    | Enable based on application.properties                |

Example:
JacksonAutoConfiguration activates only when:

* `ObjectMapper` is available
* No custom ObjectMapper bean exists

---

## **6.2 Customizing Auto-Configuration**

Spring Boot allows overriding defaults:

Example: Custom port:

```
server.port=8085
```

Custom datasource:

```
spring.datasource.url=jdbc:mysql://localhost:3306/db
spring.datasource.username=root
spring.datasource.password=pass
```

---

## **6.3 Disabling Auto-Configuration**

```java
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
```

---

# **7. Folder Structure (Standard)**

```
src/main/java/
   com.example.demo/
       DemoApplication.java
       controller/
       service/
       repository/
       entity/

src/main/resources/
   application.properties
   static/
   templates/

pom.xml
```

---


### **Spring Boot Provides:**

* Fast development
* Minimal configuration
* Auto-configured components
* Starter dependencies
* Embedded server
* Spring Initializr for project generation
* Production-ready features

---
