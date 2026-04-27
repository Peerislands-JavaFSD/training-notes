
# 1. Introduction to API Documentation

API documentation is an essential part of backend development. It helps developers understand:

* What endpoints are available
* What request data is required
* What responses look like
* What HTTP methods are used
* How to test APIs without external tools

Swagger (OpenAPI Specification) provides an interface to visualize and interact with REST APIs.

---

# 2. What is Swagger and OpenAPI

## 2.1 OpenAPI Specification (OAS)

OpenAPI is a standard format for describing REST APIs. It defines:

* Paths (endpoints)
* Request/response models
* Query parameters
* Error structures
* Authentication methods

A typical OpenAPI spec is written in YAML or JSON.

## 2.2 Swagger Ecosystem

Swagger is a set of tools built around the OpenAPI spec.

| Tool              | Purpose                                             |
| ----------------- | --------------------------------------------------- |
| Swagger UI        | Visual interface to explore API endpoints           |
| Swagger Editor    | Online editor for writing OpenAPI specs             |
| Swagger Codegen   | Generates server/client code                        |
| springdoc-openapi | Library that generates OpenAPI docs for Spring Boot |

For Spring Boot, we use **springdoc-openapi**.

---

# 3. Adding Swagger to a Spring Boot Project

There are two popular approaches:

1. Using springdoc-openapi (recommended for Spring Boot 2.x / 3.x)
2. Using springfox-swagger (older, not recommended anymore)

In modern Spring Boot, you should use **springdoc-openapi**.

---

# 3.1 Add Maven Dependency (Spring Boot 3.x and above)

In pom.xml:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

Spring Boot automatically configures the Swagger UI.

---

# 4. Accessing Swagger UI

Once dependency is added, run the application and open:

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

OpenAPI YAML (if enabled):

```
http://localhost:8080/v3/api-docs.yaml
```

---

# 5. Documenting APIs in Spring Boot

Swagger UI automatically scans:

* Controllers
* Request mappings
* DTOs
* Response bodies

However, adding annotations improves the clarity of documentation.

---

# 5.1 Basic Controller Example

```java
@RestController
@RequestMapping("/students")
public class StudentController {

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable int id) {
        return new Student(id, "John Doe", "Computer Science");
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return student;
    }
}
```

Swagger UI will automatically list these endpoints.

---

# 6. Enhancing Documentation with Annotations

springdoc-openapi supports OpenAPI annotations to describe APIs more clearly.

Import package:

```java
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.*;
```

---

# 6.1 Adding Tags

Tags group your API endpoints on Swagger UI.

```java
@RestController
@RequestMapping("/students")
@Tag(name = "Student APIs", description = "Operations related to students")
public class StudentController { }
```

---

# 6.2 Documenting an Endpoint Using @Operation

```java
@Operation(
    summary = "Fetch student by ID",
    description = "Returns the student details for the given ID"
)
@GetMapping("/{id}")
public Student getStudent(@PathVariable int id) {
    return new Student(id, "John Doe", "CS");
}
```

---

# 6.3 Documenting API Responses

```java
@Operation(summary = "Create a student")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Student created successfully",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Student.class))),
    @ApiResponse(responseCode = "400", description = "Invalid payload"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
@PostMapping
public Student createStudent(@RequestBody Student student) {
    return student;
}
```

This clearly explains:

* What is returned
* What status codes are possible
* What structure the response follows

---

# 6.4 Documenting Request Models with @Schema

### DTO example

```java
@Schema(description = "Student data model")
public class Student {

    @Schema(description = "Unique student identifier", example = "101")
    private int id;

    @Schema(description = "Full name of the student", example = "Alice Johnson")
    private String name;

    @Schema(description = "Student department", example = "Electrical")
    private String department;
}
```

Guidance:

* Use simple descriptions
* Provide example values
* Keep field names meaningful

---

# 7. Customizing OpenAPI Info Section

Create a configuration class.

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
            .info(new Info()
                .title("Student Management API")
                .version("1.0")
                .description("API documentation for managing students")
                .contact(new Contact().name("Training Team").email("support@example.com"))
            );
    }
}
```

This creates a clear header section in Swagger UI.

---

# 8. Enabling/Disabling Swagger Per Environment

You can enable Swagger only in dev environments.

### application-dev.properties

```
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
```

### application-prod.properties

```
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

This avoids exposing docs in production.

---

# 9. Common Swagger Configurations

### Change default Swagger UI URL

```
springdoc.swagger-ui.path=/docs
```

### Change OpenAPI JSON path

```
springdoc.api-docs.path=/api-docs
```

### Enable sorting

```
springdoc.swagger-ui.operationsSorter=alpha
springdoc.swagger-ui.tagsSorter=alpha
```

---

# 10. How Swagger Helps Beginners

Swagger provides the following benefits:

1. Helps visualize available APIs
2. Acts as interactive documentation
3. Works as a built-in API testing tool
4. Reduces dependency on Postman
5. Improves communication between frontend and backend teams
6. Helps in onboarding new developers
7. Ensures clarity during code reviews

---

# 11. Best Practices for Writing API Documentation

1. Provide short and clear summaries
2. Add meaningful descriptions
3. Use example values in schemas
4. Include all possible response status codes
5. Group endpoints logically using tags
6. Do not expose sensitive information
7. Disable Swagger in production
8. Maintain consistency in naming

---

# 12. Beginner-Friendly Checklist

| Requirement                                 | Status        |
| ------------------------------------------- | ------------- |
| Added springdoc-openapi dependency          | Required      |
| Accessed Swagger UI                         | Must know     |
| Used @Operation for endpoint clarity        | Recommended   |
| Used @ApiResponses for response structure   | Required      |
| Documented DTOs with @Schema                | Recommended   |
| Created custom OpenAPI info                 | Optional      |
| Implemented environment-based configuration | Good practice |

---

# 13. Summary

This learning material covered:

* The purpose of API documentation
* Swagger and OpenAPI basics
* Adding Swagger to Spring Boot
* Generating automatic documentation
* Enhancing docs with annotations
* Documenting DTOs
* Customizing UI and OpenAPI metadata
* Environment-specific enabling/disabling
* Best practices for beginners

