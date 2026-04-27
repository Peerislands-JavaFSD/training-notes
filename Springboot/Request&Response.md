
# **REQUEST & RESPONSE HANDLING **



# **1. Basic Overview of Spring Boot Request/Response Flow**

```
Client → HTTP Request → DispatcherServlet → HandlerMapping → Controller → Service → Response → Client
```

### Key components involved:

| Component                          | Responsibility                              |
| ---------------------------------- | ------------------------------------------- |
| **DispatcherServlet**              | Front controller that receives all requests |
| **HandlerMapping**                 | Finds which controller method to call       |
| **Controller**                     | Accepts request & returns response          |
| **ViewResolver / JSON Serializer** | Converts response into JSON                 |
| **HttpMessageConverters**          | Convert JSON ↔ Java objects                 |
| **ResponseEntity**                 | Full control over returned HTTP response    |

---

# **2. REST Controller Basics**

Use:

```java
@RestController
@RequestMapping("/api")
public class StudentController { }
```

* `@RestController` = `@Controller + @ResponseBody`

    * Every method returns JSON (or XML if configured)
* `@RequestMapping("/api")` = base URL prefix

---

# **3. Handling Different Request Types (GET, POST, PUT, DELETE)**

### **GET Request (Read data)**

```java
@GetMapping("/students")
public List<Student> getAllStudents() {
    return studentService.getAll();
}
```

### **POST Request (Create data)**

```java
@PostMapping("/students")
public Student createStudent(@RequestBody Student student) {
    return studentService.save(student);
}
```

### **PUT Request (Update whole object)**

```java
@PutMapping("/students/{id}")
public Student updateStudent(
        @PathVariable int id,
        @RequestBody Student student) {
    return studentService.update(id, student);
}
```

### **PATCH Request (Partial update)**

```java
@PatchMapping("/students/{id}")
public Student updateName(
        @PathVariable int id,
        @RequestBody Map<String, Object> updates) {
    return studentService.partialUpdate(id, updates);
}
```

### **DELETE Request (Remove data)**

```java
@DeleteMapping("/students/{id}")
public void deleteStudent(@PathVariable int id) {
    studentService.delete(id);
}
```

---

# **4. Path Variables and Query Parameters**

## **4.1 Path Variables**

Used when the value identifies a resource.

```
GET /students/10
```

```java
@GetMapping("/students/{id}")
public Student getById(@PathVariable int id) {
    return studentService.get(id);
}
```

**Custom variable name:**

```java
@GetMapping("/students/{studentId}")
public Student getById(@PathVariable("studentId") int id) {
    ...
}
```

---

## **4.2 Query Parameters**

Used for filtering/sorting/search.

```
GET /students?city=Chennai&sort=name
```

```java
@GetMapping("/students")
public List<Student> filterStudents(
        @RequestParam(required=false) String city,
        @RequestParam(defaultValue="id") String sort) {
    return studentService.filter(city, sort);
}
```

---

# **5. Handling Request Body (JSON → Java)**

Use **@RequestBody**.

### Example JSON:

```json
{
  "id": 1,
  "name": "John",
  "city": "Chennai"
}
```

### Controller:

```java
@PostMapping("/students")
public Student create(@RequestBody Student student) {
    return studentService.save(student);
}
```

Spring Boot uses **Jackson** to convert JSON → Java object.

---

# **6. Handling Headers**

### Read incoming headers:

```java
@GetMapping("/secure-data")
public String getSecureData(@RequestHeader("Authorization") String token) {
    return "Token received: " + token;
}
```

### Optional header:

```java
@GetMapping("/info")
public String info(@RequestHeader(value="app-version", required=false) String version) {
    return version != null ? version : "No version provided";
}
```

---

# **7. Handling Multi-Part Files**

```java
@PostMapping("/upload")
public String uploadFile(@RequestParam("file") MultipartFile file) {
    return file.getOriginalFilename();
}
```

---

# **8. Returning Custom Responses Using ResponseEntity**

`ResponseEntity` allows you to control:

* HTTP Status Code
* Headers
* Body

### Example: Return with status 201 CREATED

```java
@PostMapping("/students")
public ResponseEntity<Student> create(@RequestBody Student student) {
    Student saved = studentService.save(student);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

### Example: Custom Headers

```java
@GetMapping("/students")
public ResponseEntity<List<Student>> getStudents() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "100");

    return ResponseEntity.ok()
            .headers(headers)
            .body(studentService.getAll());
}
```

### Example: No content response

```java
@DeleteMapping("/students/{id}")
public ResponseEntity<Void> delete(@PathVariable int id) {
    studentService.delete(id);
    return ResponseEntity.noContent().build();
}
```

---

# **9. Validation in Request Handling**

Add dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Model:

```java
public class Student {

    @Min(1)
    private int id;

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    // getters, setters
}
```

### Apply validation:

```java
@PostMapping("/students")
public ResponseEntity<Student> create(
        @Valid @RequestBody Student student) {
    return ResponseEntity.ok(studentService.save(student));
}
```

### Handle validation errors: (Globally)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }
}
```

---

# **10. JSON Serialization & Deserialization**

Spring Boot automatically converts Java objects to JSON using **Jackson**.

### Custom JSON property names:

```java
public class Student {

    @JsonProperty("student_name")
    private String name;
}
```

### Ignore fields:

```java
@JsonIgnore
private String internalCode;
```

---

# **11. Exception Handling (Global Response Handling)**

Use `@RestControllerAdvice`.

### Example custom exception:

```java
public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(String msg) { super(msg); }
}
```

Throw in service:

```java
if(student == null) {
    throw new StudentNotFoundException("Student not found");
}
```

Global handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(StudentNotFoundException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

---

# **12. Best Practices for Request/Response Handling**

### **Do’s**

✔ Use meaningful URLs
✔ Use plural nouns for resources (`/students`)
✔ Return appropriate HTTP status codes
✔ Validate incoming data
✔ Send structured error responses
✔ Use ResponseEntity for full control
✔ Use DTOs for request/response mapping
✔ Use `PATCH` only for partial update
✔ Use global exception handling (`@RestControllerAdvice`)

---

### **Don’ts**

✘ Don’t expose database entities directly
✘ Don’t use 200 OK for errors
✘ Don’t mix business logic in controllers
✘ Don’t return stack traces
✘ Don’t accept input without validation

---

# **13. Full Working Example (Controller + DTO + ResponseEntity)**

### **Student DTO**

```java
public class StudentRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    // getters, setters
}
```

### **Student Response DTO**

```java
public class StudentResponse {
    private int id;
    private String name;
    private String city;
}
```

### **Controller**

```java
@RestController
@RequestMapping("/students")
public class StudentController {

    @PostMapping
    public ResponseEntity<StudentResponse> create(
            @Valid @RequestBody StudentRequest request) {

        StudentResponse response = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> get(@PathVariable int id) {
        return ResponseEntity.ok(service.get(id));
    }
}
```

---
