# **1. Why Exception Handling?**

* To handle errors gracefully and consistently.
* To return meaningful HTTP responses (status codes + messages) instead of stack traces.
* To separate error-handling logic from business logic.

---

# **2. Types of Exception Handling**

### **A. Controller-Level Handling**

Use `@ExceptionHandler` inside a controller:

```java
@RestController
@RequestMapping("/students")
public class StudentController {

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable int id) {
        throw new StudentNotFoundException("Student not found");
    }

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<String> handleNotFound(StudentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
```

---

### **B. Global Handling**

Use `@RestControllerAdvice` to handle exceptions across all controllers:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(StudentNotFoundException ex) {
        Map<String, String> error = Map.of("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
```

---

# **3. Best Practices**

* Always use meaningful HTTP status codes: 404, 400, 500, etc.
* Return structured error responses (JSON) instead of plain strings.
* Use global exception handling (`@RestControllerAdvice`) for consistency.
* Don’t expose stack traces in production.

---

# **4. Summary**

Spring Boot exception handling allows you to:

1. Catch exceptions at the controller or global level.
2. Customize HTTP responses and error messages.
3. Keep your API robust, consistent, and user-friendly.

---
