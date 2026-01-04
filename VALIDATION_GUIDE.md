# Complete Guide to @Valid and Bean Validation in Spring Boot

A comprehensive guide to understanding and using `@Valid` annotation and Bean Validation API in Spring Boot applications.

## Table of Contents

- [What is @Valid?](#what-is-valid)
- [Bean Validation API (JSR-380)](#bean-validation-api-jsr-380)
- [How Validation Works](#how-validation-works)
- [Standard Validation Annotations](#standard-validation-annotations)
- [Custom Validators](#custom-validators)
- [Error Handling](#error-handling)
- [Validation Groups](#validation-groups)
- [Best Practices](#best-practices)

---

## What is @Valid?

`@Valid` is an annotation that triggers validation on objects in Spring Boot applications.

### Key Concepts

**Bean Validation**
- Standardized way to validate Java objects
- Part of Jakarta EE (formerly Java EE)
- Specification: JSR-380 (Bean Validation 2.0)
- Implementation: Hibernate Validator

**Where @Valid is Used**
```java
// In Controller - Validates request body
@PostMapping("/signup")
public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
    // If validation fails, method is never called
    // Spring returns 400 Bad Request automatically
}

// In Service - Validates method parameters
@Service
public class UserService {
    public void createUser(@Valid User user) {
        // Validation happens before method executes
    }
}

// In Entity - Validates before saving to database
@Entity
public class User {
    @NotBlank
    private String username;
    // Validation happens on save/update
}
```

### Why Use @Valid?

**1. Security**
```java
// Without validation
@PostMapping("/signup")
public void signup(@RequestBody SignupRequest request) {
    // What if username is null?
    // What if email is empty?
    // What if password is 1 character?
    userService.save(request); // Potential security risk!
}

// With validation
@PostMapping("/signup")
public void signup(@Valid @RequestBody SignupRequest request) {
    // Guaranteed:
    // - username is not null/empty
    // - email is valid format
    // - password meets requirements
    userService.save(request); // Safe!
}
```

**2. Data Integrity**
- Prevents invalid data from entering your system
- Enforces business rules at the API boundary
- Reduces database errors

**3. Better Error Messages**
- Automatic validation error responses
- Clear messages for API clients
- Consistent error format

---

## Bean Validation API (JSR-380)

### Architecture

```
┌─────────────────────────────────────────┐
│     Client sends JSON request           │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Spring MVC - Jackson Deserialization   │
│  Converts JSON → Java Object            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  @Valid Annotation Detected              │
│  Spring triggers validation              │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Hibernate Validator (Implementation)   │
│  - Checks all constraints                │
│  - Collects violations                   │
└──────────────┬──────────────────────────┘
               │
        ┌──────┴──────┐
        │             │
   ✓ Valid      ✗ Invalid
        │             │
        ▼             ▼
    Controller   MethodArgumentNotValidException
                      │
                      ▼
              400 Bad Request
              with error details
```

### Dependencies

In our `pom.xml`:
```xml
<!-- Already included in spring-boot-starter-web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

This includes:
- Jakarta Bean Validation API
- Hibernate Validator (reference implementation)
- Spring Boot auto-configuration

---

## How Validation Works

### Step-by-Step Process

#### 1. Client Request
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "",
  "email": "invalid-email",
  "password": "123"
}
```

#### 2. Spring MVC Deserializes JSON
```java
// Jackson converts JSON to SignupRequest object
SignupRequest request = new SignupRequest();
request.setUsername("");           // Empty!
request.setEmail("invalid-email"); // Invalid format!
request.setPassword("123");        // Too short!
```

#### 3. @Valid Triggers Validation
```java
@PostMapping("/signup")
public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
    // @Valid annotation triggers validation BEFORE this line executes
}
```

#### 4. Hibernate Validator Checks Constraints
```java
public class SignupRequest {
    @NotBlank  // Check: Is it blank? → YES! Violation!
    private String username;

    @Email     // Check: Is it valid email? → NO! Violation!
    private String email;

    @Size(min=6, max=40)  // Check: Length 6-40? → NO (length=3)! Violation!
    private String password;
}
```

#### 5. Validation Result

**If all constraints pass:**
- Controller method executes normally
- Returns success response

**If any constraint fails:**
- Throws `MethodArgumentNotValidException`
- Spring returns 400 Bad Request
- Includes validation error details

#### 6. Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "username",
      "message": "must not be blank"
    },
    {
      "field": "email",
      "message": "must be a well-formed email address"
    },
    {
      "field": "password",
      "message": "size must be between 6 and 40"
    }
  ]
}
```

---

## Standard Validation Annotations

### 1. Null/Empty Checks

#### @NotNull
```java
@NotNull
private String username;
```
- **Checks:** Value is not null
- **Allows:** Empty strings, whitespace
- **Use case:** Required fields that can be empty

**Examples:**
```java
null         → ✗ Violation
""           → ✓ Valid
"   "        → ✓ Valid
"john"       → ✓ Valid
```

#### @NotEmpty
```java
@NotEmpty
private String username;
```
- **Checks:** Not null AND not empty
- **Allows:** Whitespace-only strings
- **Use case:** String/Collection must have content

**Examples:**
```java
null         → ✗ Violation
""           → ✗ Violation
"   "        → ✓ Valid (not empty, just whitespace)
"john"       → ✓ Valid
```

#### @NotBlank
```java
@NotBlank
private String username;
```
- **Checks:** Not null, not empty, not whitespace
- **Use case:** Strings that must have actual content
- **Best for:** User input validation

**Examples:**
```java
null         → ✗ Violation
""           → ✗ Violation
"   "        → ✗ Violation (only whitespace)
"john"       → ✓ Valid
```

**Comparison:**
```java
public class Example {
    @NotNull
    private String field1;   // null → ✗, "" → ✓, "  " → ✓

    @NotEmpty
    private String field2;   // null → ✗, "" → ✗, "  " → ✓

    @NotBlank
    private String field3;   // null → ✗, "" → ✗, "  " → ✗
}
```

### 2. Size Constraints

#### @Size
```java
@Size(min = 3, max = 20)
private String username;
```
- **Works on:** String, Collection, Map, Array
- **Parameters:**
  - `min`: Minimum size (inclusive)
  - `max`: Maximum size (inclusive)

**Examples:**
```java
@Size(min = 3, max = 20)
private String username;

"ab"        → ✗ Violation (length 2 < 3)
"abc"       → ✓ Valid (length 3)
"12345678901234567890"  → ✓ Valid (length 20)
"123456789012345678901" → ✗ Violation (length 21 > 20)
```

#### @Length (Hibernate Validator specific)
```java
@Length(min = 3, max = 20)
private String username;
```
- Similar to @Size but only for Strings
- Part of Hibernate Validator, not standard JSR-380

### 3. Numeric Constraints

#### @Min / @Max
```java
@Min(18)
private Integer age;

@Max(100)
private Integer discount;
```
- **Works on:** Integer, Long, Short, Byte, BigInteger, BigDecimal
- **Check:** Value >= min or <= max

**Examples:**
```java
@Min(18)
private Integer age;

17  → ✗ Violation
18  → ✓ Valid
19  → ✓ Valid
```

#### @DecimalMin / @DecimalMax
```java
@DecimalMin(value = "0.0", inclusive = false)
private BigDecimal price;

@DecimalMax(value = "999.99", inclusive = true)
private BigDecimal price;
```
- **Works on:** BigDecimal, BigInteger, String, numeric primitives
- **Parameter:**
  - `inclusive`: Whether the boundary is inclusive

**Examples:**
```java
@DecimalMin(value = "0.0", inclusive = false)
private BigDecimal price;

0.0   → ✗ Violation (not inclusive)
0.01  → ✓ Valid
```

#### @Positive / @PositiveOrZero
```java
@Positive
private Integer quantity;

@PositiveOrZero
private Integer stock;
```
- **@Positive:** Value > 0
- **@PositiveOrZero:** Value >= 0

#### @Negative / @NegativeOrZero
```java
@Negative
private Integer debt;

@NegativeOrZero
private Integer balance;
```
- **@Negative:** Value < 0
- **@NegativeOrZero:** Value <= 0

### 4. Pattern Matching

#### @Pattern
```java
@Pattern(regexp = "^[A-Za-z0-9]+$")
private String username;
```
- **Uses:** Regular expressions
- **Parameters:**
  - `regexp`: Regular expression pattern
  - `message`: Custom error message

**Examples:**
```java
// Alphanumeric only
@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Username must be alphanumeric")
private String username;

"john123"   → ✓ Valid
"john_123"  → ✗ Violation (underscore not allowed)
"john 123"  → ✗ Violation (space not allowed)

// Phone number (US format)
@Pattern(regexp = "^\\d{3}-\\d{3}-\\d{4}$")
private String phone;

"123-456-7890"  → ✓ Valid
"1234567890"    → ✗ Violation (no dashes)

// Password strength
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain uppercase, lowercase, number, and special character"
)
private String password;
```

### 5. Email Validation

#### @Email
```java
@Email
private String email;
```
- **Checks:** Valid email format
- **Standard:** RFC 5322
- **Parameters:**
  - `regexp`: Additional pattern (optional)
  - `message`: Custom message

**Examples:**
```java
@Email
private String email;

"user@example.com"      → ✓ Valid
"user.name@example.com" → ✓ Valid
"user@sub.example.com"  → ✓ Valid
"user"                  → ✗ Violation
"user@"                 → ✗ Violation
"@example.com"          → ✗ Violation

// With additional pattern
@Email(regexp = ".*@company\\.com$")
private String corporateEmail;

"user@company.com"   → ✓ Valid
"user@gmail.com"     → ✗ Violation (not @company.com)
```

### 6. Boolean Constraints

#### @AssertTrue / @AssertFalse
```java
@AssertTrue
private Boolean termsAccepted;

@AssertFalse
private Boolean banned;
```
- **@AssertTrue:** Must be true
- **@AssertFalse:** Must be false
- **Note:** null values are considered valid!

**Examples:**
```java
@AssertTrue
private Boolean termsAccepted;

true   → ✓ Valid
false  → ✗ Violation
null   → ✓ Valid (null is allowed!)

// To require non-null true:
@NotNull
@AssertTrue
private Boolean termsAccepted;
```

### 7. Date/Time Constraints

#### @Past / @PastOrPresent
```java
@Past
private LocalDate birthDate;

@PastOrPresent
private LocalDate registrationDate;
```
- **@Past:** Date must be in the past
- **@PastOrPresent:** Date in past or today

#### @Future / @FutureOrPresent
```java
@Future
private LocalDate expiryDate;

@FutureOrPresent
private LocalDate scheduledDate;
```
- **@Future:** Date must be in the future
- **@FutureOrPresent:** Date in future or today

**Examples:**
```java
@Past
private LocalDate birthDate;

LocalDate.of(1990, 1, 1)  → ✓ Valid (in the past)
LocalDate.now()           → ✗ Violation (not in past)
LocalDate.of(2030, 1, 1)  → ✗ Violation (in future)
```

---

## Custom Validators

### Creating Custom Annotations

Sometimes standard validators aren't enough. Here's how to create custom ones:

#### Example 1: Password Strength Validator

**Step 1: Create Annotation**
```java
package com.security.jwt.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "Password must be strong";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minLength() default 8;

    boolean requireUppercase() default true;

    boolean requireLowercase() default true;

    boolean requireDigit() default true;

    boolean requireSpecial() default true;
}
```

**Step 2: Create Validator**
```java
package com.security.jwt.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator
        implements ConstraintValidator<StrongPassword, String> {

    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecial;

    @Override
    public void initialize(StrongPassword annotation) {
        this.minLength = annotation.minLength();
        this.requireUppercase = annotation.requireUppercase();
        this.requireLowercase = annotation.requireLowercase();
        this.requireDigit = annotation.requireDigit();
        this.requireSpecial = annotation.requireSpecial();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true; // Use @NotNull for null check
        }

        // Check minimum length
        if (password.length() < minLength) {
            return false;
        }

        // Check uppercase
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check lowercase
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            return false;
        }

        // Check digit
        if (requireDigit && !password.matches(".*\\d.*")) {
            return false;
        }

        // Check special character
        if (requireSpecial && !password.matches(".*[@$!%*?&].*")) {
            return false;
        }

        return true;
    }
}
```

**Step 3: Use the Annotation**
```java
public class SignupRequest {
    @NotBlank
    @StrongPassword(
        minLength = 8,
        message = "Password must be at least 8 characters with uppercase, lowercase, digit, and special character"
    )
    private String password;
}
```

#### Example 2: Unique Email Validator

**Annotation:**
```java
@Documented
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

**Validator:**
```java
@Component
public class UniqueEmailValidator
        implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;
        }
        return !userRepository.existsByEmail(email);
    }
}
```

**Usage:**
```java
public class SignupRequest {
    @Email
    @UniqueEmail(message = "This email is already registered")
    private String email;
}
```

---

## Error Handling

### Default Error Response

Spring Boot's default validation error response:
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/auth/signup"
}
```

Not very helpful! Let's customize it.

### Custom Exception Handler

```java
package com.security.jwt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        // Extract all field errors
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
```

**Better Error Response:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "username": "must not be blank",
    "email": "must be a well-formed email address",
    "password": "size must be between 6 and 40"
  }
}
```

---

## Validation Groups

Validation groups allow different validation rules for different scenarios.

### Use Case

```java
public class User {
    @Null(groups = Create.class)  // Must be null when creating
    @NotNull(groups = Update.class) // Must not be null when updating
    private Long id;

    @NotBlank(groups = {Create.class, Update.class})
    private String username;

    @NotBlank(groups = Create.class)  // Required only on create
    private String password;

    // Validation groups
    public interface Create {}
    public interface Update {}
}
```

### Usage in Controller

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(
        @Validated(User.Create.class) @RequestBody User user) {
    // Validates with Create.class rules
}

@PutMapping("/users/{id}")
public ResponseEntity<?> updateUser(
        @PathVariable Long id,
        @Validated(User.Update.class) @RequestBody User user) {
    // Validates with Update.class rules
}
```

---

## Best Practices

### 1. Validate at API Boundary

```java
// ✓ GOOD: Validate in controller
@PostMapping("/signup")
public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
    userService.createUser(request);
}

// ✗ BAD: No validation, data could be invalid
@PostMapping("/signup")
public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
    userService.createUser(request); // What if request is invalid?
}
```

### 2. Use Specific Annotations

```java
// ✓ GOOD: Use most specific annotation
@NotBlank  // Checks null, empty, and whitespace
private String username;

// ✗ LESS GOOD: Too lenient
@NotNull   // Only checks null, allows empty/whitespace
private String username;
```

### 3. Provide Clear Messages

```java
// ✓ GOOD: Clear, actionable message
@Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
private String password;

// ✗ BAD: Generic message (default)
@Size(min = 6, max = 40)
private String password;  // Message: "size must be between 6 and 40"
```

### 4. Combine Annotations

```java
// ✓ GOOD: Multiple constraints
@NotBlank(message = "Username is required")
@Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Username must be alphanumeric")
private String username;
```

### 5. Don't Validate Everything

```java
// ✗ OVERKILL: Too many constraints
@NotNull
@Size(min = 1, max = 1)
@Pattern(regexp = "^[YN]$")
private String flag;

// ✓ BETTER: Simple and clear
@NotNull
@Pattern(regexp = "^[YN]$", message = "Must be Y or N")
private String flag;
```

---

## Summary

### Quick Reference

| Annotation | Use Case | Example |
|------------|----------|---------|
| `@NotNull` | Required field | ID, foreign keys |
| `@NotEmpty` | Non-empty collection | List of items |
| `@NotBlank` | Non-blank string | Username, email |
| `@Size` | Length constraint | Password, description |
| `@Min/@Max` | Numeric range | Age, quantity |
| `@Email` | Email format | Email address |
| `@Pattern` | Regex match | Phone, postal code |
| `@Past/@Future` | Date validation | Birth date, expiry |

### @Valid vs @Validated

- **@Valid**: JSR-380 standard, triggers validation
- **@Validated**: Spring-specific, supports validation groups

```java
// Standard validation
public void method(@Valid User user) { }

// With validation groups
public void method(@Validated(Create.class) User user) { }
```

---

**Happy Validating!** 🎯

For more examples, check the code in `SignupRequest.java` and `LoginRequest.java` in our JWT Security application!
