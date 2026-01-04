package com.security.jwt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GLOBAL EXCEPTION HANDLER
 *
 * Handles validation errors across the entire application
 *
 * @RestControllerAdvice:
 * - Applies to all @RestController classes
 * - Catches exceptions globally
 * - Returns JSON responses (not HTML error pages)
 *
 * Why do we need this?
 * - Spring's default error response is not user-friendly
 * - We want consistent error format across all endpoints
 * - We want detailed validation error information
 *
 * ============================================
 * DEFAULT VS CUSTOM ERROR RESPONSE
 * ============================================
 *
 * DEFAULT (without this handler):
 * {
 *   "timestamp": "2024-01-15T10:30:00.000+00:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "path": "/api/auth/signup"
 * }
 *
 * CUSTOM (with this handler):
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "Input validation failed for signup request",
 *   "errors": {
 *     "username": "must not be blank",
 *     "email": "must be a well-formed email address",
 *     "password": "Password does not meet strength requirements"
 *   }
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * HANDLE METHOD ARGUMENT NOT VALID EXCEPTION
     *
     * This exception is thrown when @Valid fails on @RequestBody
     *
     * When does this occur?
     * 1. Client sends JSON request
     * 2. Spring deserializes to Java object
     * 3. @Valid annotation triggers validation
     * 4. Validation fails
     * 5. Spring throws MethodArgumentNotValidException
     * 6. This method catches it and formats error response
     *
     * Example scenario:
     * @PostMapping("/signup")
     * public void signup(@Valid @RequestBody SignupRequest request) {
     *     // If validation fails, this method is never called
     *     // Instead, handleValidationExceptions() is called
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        /*
         * BUILD ERROR RESPONSE
         *
         * We'll create a structured error response with:
         * 1. Timestamp: When the error occurred
         * 2. Status: HTTP status code (400 Bad Request)
         * 3. Error: Error type ("Validation Failed")
         * 4. Message: Overall error message
         * 5. Errors: Map of field → error message
         */
        Map<String, Object> response = new HashMap<>();

        /*
         * EXTRACT FIELD ERRORS
         *
         * MethodArgumentNotValidException contains BindingResult
         * BindingResult contains all validation errors
         *
         * We extract each error and build a map:
         * {
         *   "username": "must not be blank",
         *   "email": "must be a well-formed email address"
         * }
         */
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            /*
             * FIELD ERROR
             *
             * Each ObjectError can be cast to FieldError
             * FieldError contains:
             * - field: The field name ("username", "email", etc.)
             * - defaultMessage: The error message from annotation
             * - rejectedValue: The invalid value that was provided
             */
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            /*
             * HANDLE DUPLICATE ERRORS
             *
             * If a field has multiple validation annotations,
             * there might be multiple errors for the same field
             *
             * Example:
             * @NotBlank
             * @Size(min = 3, max = 20)
             * private String username;
             *
             * Input: ""
             * - Violates @NotBlank
             * - Also violates @Size
             *
             * We'll collect all errors for the field:
             */
            if (errors.containsKey(fieldName)) {
                // Field already has an error, append this one
                String existing = errors.get(fieldName);
                errors.put(fieldName, existing + "; " + errorMessage);
            } else {
                // First error for this field
                errors.put(fieldName, errorMessage);
            }
        });

        /*
         * BUILD RESPONSE BODY
         *
         * Create a comprehensive error response
         */

        // Timestamp: When the error occurred
        response.put("timestamp", LocalDateTime.now().toString());

        // Status: HTTP status code
        response.put("status", HttpStatus.BAD_REQUEST.value());

        // Error: Error category
        response.put("error", "Validation Failed");

        // Message: Overall description
        response.put("message", "Input validation failed. Please check your request.");

        // Errors: Field-level errors
        response.put("errors", errors);

        /*
         * OPTIONAL: Add more context
         *
         * You can add additional information:
         */
        response.put("path", ex.getParameter().getExecutable().getName());

        /*
         * RETURN RESPONSE
         *
         * Return 400 Bad Request with error details
         */
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE CONSTRAINT VIOLATION EXCEPTION
     *
     * This exception is thrown when validation fails on:
     * - Method parameters (not @RequestBody)
     * - Return values
     * - Path variables with @Valid
     *
     * Example:
     * @GetMapping("/users/{id}")
     * public User getUser(@PathVariable @Min(1) Long id) {
     *     // If id < 1, ConstraintViolationException is thrown
     * }
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, Object> response = new HashMap<>();

        /*
         * EXTRACT CONSTRAINT VIOLATIONS
         *
         * ConstraintViolationException contains Set<ConstraintViolation>
         * Each violation has:
         * - propertyPath: The parameter/field path
         * - message: The error message
         * - invalidValue: The value that caused violation
         */
        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Constraint Violation");
        response.put("message", "Validation failed on method parameters");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * ALTERNATIVE: DETAILED ERROR RESPONSE
     *
     * This version provides more detailed information
     * Useful for debugging and development
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptionsDetailed(
            MethodArgumentNotValidException ex) {

        /*
         * Create detailed error response with:
         * - List of field errors with rejected values
         * - Global errors (object-level constraints)
         */
        List<FieldValidationError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldValidationError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .collect(Collectors.toList());

        /*
         * GLOBAL ERRORS
         *
         * These are object-level validation errors
         * Example: @AssertTrue on a method that validates multiple fields
         */
        List<String> globalErrors = ex.getBindingResult()
                .getGlobalErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());

        ValidationErrorResponse response = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Input validation failed. Please check your request.",
                fieldErrors,
                globalErrors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * VALIDATION ERROR RESPONSE DTO
     *
     * Structured error response for better client handling
     */
    public static class ValidationErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private List<FieldValidationError> fieldErrors;
        private List<String> globalErrors;

        public ValidationErrorResponse(LocalDateTime timestamp, int status, String error,
                                       String message, List<FieldValidationError> fieldErrors,
                                       List<String> globalErrors) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.fieldErrors = fieldErrors;
            this.globalErrors = globalErrors;
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public List<FieldValidationError> getFieldErrors() { return fieldErrors; }
        public List<String> getGlobalErrors() { return globalErrors; }
    }

    /**
     * FIELD VALIDATION ERROR DTO
     *
     * Details about a single field's validation error
     */
    public static class FieldValidationError {
        private String field;
        private String message;
        private Object rejectedValue;

        public FieldValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        // Getters
        public String getField() { return field; }
        public String getMessage() { return message; }
        public Object getRejectedValue() { return rejectedValue; }
    }
}

/*
 * ============================================
 * ERROR RESPONSE EXAMPLES
 * ============================================
 *
 * Example 1: Simple error response (first handler)
 *
 * Request:
 * POST /api/auth/signup
 * {
 *   "username": "",
 *   "email": "invalid",
 *   "password": "123"
 * }
 *
 * Response:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "Input validation failed. Please check your request.",
 *   "errors": {
 *     "username": "must not be blank",
 *     "email": "must be a well-formed email address",
 *     "password": "size must be between 6 and 40"
 *   }
 * }
 *
 * ---
 *
 * Example 2: Detailed error response (second handler)
 *
 * Response:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "Input validation failed. Please check your request.",
 *   "fieldErrors": [
 *     {
 *       "field": "username",
 *       "message": "must not be blank",
 *       "rejectedValue": ""
 *     },
 *     {
 *       "field": "email",
 *       "message": "must be a well-formed email address",
 *       "rejectedValue": "invalid"
 *     },
 *     {
 *       "field": "password",
 *       "message": "size must be between 6 and 40",
 *       "rejectedValue": "123"
 *     }
 *   ],
 *   "globalErrors": []
 * }
 *
 * ============================================
 * CLIENT-SIDE HANDLING
 * ============================================
 *
 * JavaScript example:
 *
 * fetch('/api/auth/signup', {
 *   method: 'POST',
 *   headers: { 'Content-Type': 'application/json' },
 *   body: JSON.stringify({ username: '', email: 'bad', password: '123' })
 * })
 * .then(response => {
 *   if (response.status === 400) {
 *     return response.json().then(error => {
 *       // Display field errors
 *       Object.keys(error.errors).forEach(field => {
 *         console.log(`${field}: ${error.errors[field]}`);
 *         // Show error next to form field
 *         document.getElementById(`${field}-error`).textContent = error.errors[field];
 *       });
 *     });
 *   }
 *   return response.json();
 * });
 *
 * ============================================
 * CUSTOMIZATION OPTIONS
 * ============================================
 *
 * 1. Internationalization (i18n):
 *
 * @ExceptionHandler(MethodArgumentNotValidException.class)
 * public ResponseEntity<?> handle(MethodArgumentNotValidException ex, Locale locale) {
 *     // Use MessageSource to get localized messages
 *     String message = messageSource.getMessage("validation.failed", null, locale);
 *     // ...
 * }
 *
 * ---
 *
 * 2. Logging validation errors:
 *
 * @ExceptionHandler(MethodArgumentNotValidException.class)
 * public ResponseEntity<?> handle(MethodArgumentNotValidException ex) {
 *     logger.warn("Validation failed: {}", ex.getBindingResult().getAllErrors());
 *     // ...
 * }
 *
 * ---
 *
 * 3. Different error formats for different endpoints:
 *
 * @RestControllerAdvice(basePackages = "com.security.jwt.controllers.api")
 * public class ApiExceptionHandler { ... }
 *
 * @RestControllerAdvice(basePackages = "com.security.jwt.controllers.admin")
 * public class AdminExceptionHandler { ... }
 */
