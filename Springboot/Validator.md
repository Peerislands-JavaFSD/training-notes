## 1. Introduction to Validation in Spring Boot

Validation ensures that the data received by your application meets specific criteria before processing.
Spring Boot provides several options for validation:

1. **JSR-380 (Bean Validation)** using annotations (`@NotNull`, `@Size`, `@Email`)
2. **Custom validation** using `Validator` interface

The `Validator` interface is useful when:

* Built-in annotations are not enough
* Complex or conditional validation logic is required
* You want to validate across multiple fields

---

## 2. `Validator` Interface Overview

The `org.springframework.validation.Validator` interface has two main methods:

```java
public interface Validator {
    boolean supports(Class<?> clazz); // Checks if the validator supports the given class
    void validate(Object target, Errors errors); // Contains validation logic
}
```

**Explanation:**

* **supports(Class<?> clazz)**
  Returns `true` if the validator can validate the given class.

* **validate(Object target, Errors errors)**
  Contains the validation rules.
  Use `errors.rejectValue(field, errorCode, message)` to report validation errors.

---

## 3. Example: Custom User Validation

### 3.1 User DTO

```java
public class UserDTO {
    private String username;
    private String email;
    private int age;

    // getters and setters
}
```

---

### 3.2 Custom Validator

```java
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDTO user = (UserDTO) target;

        // Username must not be null or empty
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            errors.rejectValue("username", "username.empty", "Username cannot be empty");
        }

        // Email must contain @
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            errors.rejectValue("email", "email.invalid", "Email must be valid");
        }

        // Age must be >= 18
        if (user.getAge() < 18) {
            errors.rejectValue("age", "age.invalid", "Age must be 18 or older");
        }
    }
}
```

**Explanation:**

* `supports()` ensures only `UserDTO` instances are validated
* `validate()` checks each field and reports errors via `Errors` object

---

## 4. Using the Validator in Controller

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserValidator userValidator;

    @Autowired
    public UserController(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody UserDTO user, BindingResult result) {
        userValidator.validate(user, result);

        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            result.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append("; "));
            return "Validation failed: " + errors.toString();
        }

        return "User is valid and registered successfully";
    }
}
```

**Explanation:**

* `BindingResult` captures validation errors
* If errors exist, the API returns a readable message
* Otherwise, user passes validation

---

## 5. Using Validator with Spring's `@InitBinder`

You can automatically apply a validator to certain request objects:

```java
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserValidator userValidator;

    public UserController(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userValidator);
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody UserDTO user, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            result.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append("; "));
            return "Validation failed: " + errors.toString();
        }

        return "User is valid and registered successfully";
    }
}
```

**Explanation:**

* `@InitBinder` registers the validator for all incoming requests of type `UserDTO`
* Eliminates manual calls to `validator.validate()` in controller methods

---

## 6. Combining `Validator` with JSR-380 Annotations

You can use both:

```java
import jakarta.validation.constraints.*;

public class UserDTO {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Email(message = "Email should be valid")
    private String email;

    @Min(value = 18, message = "Age must be 18 or older")
    private int age;
}
```

* Annotation-based validation handles simple rules
* `Validator` interface handles complex or conditional rules

---

## 7. Common Use Cases for Custom Validator

1. Cross-field validation (e.g., password confirmation)
2. Complex business rules (e.g., email must belong to a domain)
3. Conditional validation based on other fields
4. Validating collections of objects

---

## 8. Best Practices

1. Keep validators **small and focused**
2. Use **annotation-based validation** for simple checks
3. Use **custom validator** for complex logic
4. Register validators with `@InitBinder` or inject manually
5. Always check `BindingResult` before proceeding with business logic

---

## 9. Summary

* `Validator` interface allows **custom, reusable, and complex validation** in Spring Boot
* `supports()` method specifies the object type
* `validate()` contains the logic and populates `Errors` object
* Integration can be done manually in controller or via `@InitBinder`
* Combines well with JSR-380 annotations for full validation coverage

---
