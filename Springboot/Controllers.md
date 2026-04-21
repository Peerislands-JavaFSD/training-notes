# Spring Boot Controllers

*(Setup, Architecture, Annotations & Internals)*

---

## 1. What is a Controller in Spring Boot?

A **Controller** is the component responsible for:

* Receiving HTTP requests
* Delegating business logic
* Returning a response (view or data)

In Spring Boot, controllers sit at the **web layer** of the application.

---

## 2. Controller in the Spring Boot Architecture

```
Client (Browser / API Client)
        ↓
Embedded Server (Tomcat / Netty)
        ↓
DispatcherServlet  (Front Controller)
        ↓
Handler Mapping
        ↓
Controller
        ↓
Service Layer
        ↓
Repository / DB
```

> Controllers never talk directly to DB in a clean architecture.

---

## 3. Basic Project Setup (Controller Context)

### Required Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

This brings:

* Spring MVC
* Jackson
* Embedded Tomcat
* Validation support

---

## 4. DispatcherServlet (Important Theory)

* **Front Controller pattern**
* Single entry point for all HTTP requests
* Auto-configured by Spring Boot

You **do not define it manually** in Boot.

---

## 5. Types of Controllers in Spring Boot

### 5.1 `@Controller`

Used for **MVC applications** returning views (JSP/Thymeleaf).

### 5.2 `@RestController`

Used for **REST APIs**, returns JSON/XML.

```java
@RestController
public class SampleController {
}
```

> `@RestController = @Controller + @ResponseBody`

---

## 6. Controller Discovery (Component Scanning)

Spring Boot scans from the package where:

```java
@SpringBootApplication
```

is located.

Controllers **must be in the same package or sub-packages**.

---

## 7. Request Mapping Basics

### 7.1 `@RequestMapping` (Generic Mapping)

```java
@RequestMapping("/api")
@RestController
public class ApiController {
}
```

Acts as a **base URL**.

---

### 7.2 HTTP Method-Specific Mappings

| Annotation       | HTTP Method |
| ---------------- | ----------- |
| `@GetMapping`    | GET         |
| `@PostMapping`   | POST        |
| `@PutMapping`    | PUT         |
| `@PatchMapping`  | PATCH       |
| `@DeleteMapping` | DELETE      |

---

## 8. Basic Controller Example

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping
    public List<String> getUsers() {
        return List.of("A", "B");
    }
}
```

URL:

```
GET /users
```

---

## 9. Request Parameters Handling

### 9.1 `@PathVariable`

```java
@GetMapping("/{id}")
public String getUser(@PathVariable int id) {
    return "User " + id;
}
```

URL:

```
/users/10
```

---

### 9.2 `@RequestParam`

```java
@GetMapping("/search")
public String search(@RequestParam String name) {
    return name;
}
```

URL:

```
/users/search?name=John
```

---

### Optional Params

```java
@RequestParam(required = false)
```

---

## 10. Request Body Handling

### 10.1 `@RequestBody`

```java
@PostMapping
public User create(@RequestBody User user) {
    return user;
}
```

* JSON → Java Object
* Uses Jackson internally

---

### Validation

```java
@PostMapping
public User create(@Valid @RequestBody User user) {
    return user;
}
```

---

## 11. Response Handling

### 11.1 Default JSON Response

```java
@GetMapping
public User get() {
    return new User();
}
```

---

### 11.2 Custom HTTP Status

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public User create(@RequestBody User user) {
    return user;
}
```

---

### 11.3 `ResponseEntity`

```java
@GetMapping("/{id}")
public ResponseEntity<User> get(@PathVariable int id) {
    return ResponseEntity.ok(new User());
}
```

---

## 12. Content Negotiation (Theory)

Spring Boot selects response type based on:

* `Accept` header
* Message converters

Default:

* JSON (Jackson)

---

## 13. Exception Handling in Controllers

### 13.1 `@ExceptionHandler`

```java
@ExceptionHandler(RuntimeException.class)
public String handle(RuntimeException ex) {
    return ex.getMessage();
}
```

---

### 13.2 Global Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

---

## 14. Controller Advice (Cross-Cutting Web Concerns)

* Exception handling
* Data binding
* Response formatting

Applied **across all controllers**.

---

## 15. Binding & Conversion (Advanced Theory)

Spring automatically converts:

* Path variables
* Request params
* JSON payloads

Using:

* `Converter`
* `Formatter`
* `HttpMessageConverter`

---

## 16. Controller Lifecycle (Theory)

Controllers:

* Are **singleton beans**
* Created at application startup
* Must be **stateless**

Never store request-specific data in fields.

---

## 17. Common Controller Mistakes

❌ Business logic in controller
❌ DB access in controller
❌ Using `HttpServletRequest` directly
❌ Returning entities directly (in large apps)

---

## 18. MVC Controller vs REST Controller

| Aspect     | MVC           | REST              |
| ---------- | ------------- | ----------------- |
| Annotation | `@Controller` | `@RestController` |
| Output     | View          | JSON/XML          |
| Use case   | Web UI        | APIs              |

---

## 19. Controller Testing (Theory Only)

* Unit tests → `@WebMvcTest`
* Integration tests → `@SpringBootTest`

Controllers are usually tested **in isolation**.

---

## 20. Interview Summary (Controllers)

> Spring Boot controllers handle HTTP requests using annotation-based mappings, rely on DispatcherServlet as a front controller, convert request data via message converters, and return responses using content negotiation.

---
