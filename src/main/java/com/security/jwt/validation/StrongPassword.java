package com.security.jwt.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * CUSTOM VALIDATION ANNOTATION: @StrongPassword
 *
 * This demonstrates how to create a custom validation annotation
 *
 * Use case:
 * - Enforce strong password requirements
 * - More flexible than @Pattern
 * - Can have multiple configurable requirements
 *
 * Usage:
 * @StrongPassword(minLength = 8, requireUppercase = true)
 * private String password;
 *
 * ============================================
 * ANNOTATION STRUCTURE EXPLAINED
 * ============================================
 */

/**
 * @Documented
 * - Include this annotation in JavaDoc
 * - Helps IDEs show validation info
 */
@Documented

/**
 * @Constraint
 * - Marks this as a validation constraint
 * - validatedBy: Specifies the validator class
 * - The validator class implements ConstraintValidator interface
 */
@Constraint(validatedBy = StrongPasswordValidator.class)

/**
 * @Target
 * - Where this annotation can be used
 * - ElementType.FIELD: On class fields
 * - ElementType.PARAMETER: On method parameters
 * - ElementType.ANNOTATION_TYPE: On other annotations (for composition)
 *
 * Example targets:
 * - FIELD: private String password;
 * - PARAMETER: public void method(@StrongPassword String pwd) { }
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})

/**
 * @Retention
 * - When the annotation is available
 * - RetentionPolicy.RUNTIME: Available at runtime
 * - Required for validation to work (validator needs to see it at runtime)
 */
@Retention(RetentionPolicy.RUNTIME)

public @interface StrongPassword {

    /**
     * MESSAGE - Error message when validation fails
     *
     * Default message if validation fails
     * Can be overridden when using the annotation:
     *
     * @StrongPassword(message = "Custom error message")
     * private String password;
     *
     * Message can use placeholders:
     * "Password must be at least {minLength} characters"
     */
    String message() default "Password does not meet strength requirements";

    /**
     * GROUPS - Validation groups
     *
     * Allows grouping validations for different scenarios
     * See VALIDATION_GUIDE.md for details on validation groups
     *
     * Example:
     * @StrongPassword(groups = Create.class)
     * private String password;
     */
    Class<?>[] groups() default {};

    /**
     * PAYLOAD - Additional metadata
     *
     * Can be used to assign severity or additional info
     * Rarely used in basic validations
     *
     * Example use case:
     * - Severity levels (warning vs error)
     * - Client-side validation hints
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * CUSTOM PARAMETERS
     *
     * These make the annotation configurable
     * Can be set when using the annotation
     */

    /**
     * Minimum password length
     *
     * Usage:
     * @StrongPassword(minLength = 10)
     * private String password;
     */
    int minLength() default 8;

    /**
     * Require at least one uppercase letter
     *
     * Usage:
     * @StrongPassword(requireUppercase = false)
     * private String password;
     */
    boolean requireUppercase() default true;

    /**
     * Require at least one lowercase letter
     */
    boolean requireLowercase() default true;

    /**
     * Require at least one digit
     */
    boolean requireDigit() default true;

    /**
     * Require at least one special character
     *
     * Special characters: @$!%*?&
     */
    boolean requireSpecial() default true;
}

/*
 * ============================================
 * USAGE EXAMPLES
 * ============================================
 *
 * Example 1: Default settings (all requirements enabled)
 *
 * @StrongPassword
 * private String password;
 *
 * Requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character
 *
 * ---
 *
 * Example 2: Custom length, no special character required
 *
 * @StrongPassword(
 *     minLength = 12,
 *     requireSpecial = false,
 *     message = "Password must be at least 12 characters with upper, lower, and digit"
 * )
 * private String password;
 *
 * ---
 *
 * Example 3: Basic password (only length check)
 *
 * @StrongPassword(
 *     minLength = 6,
 *     requireUppercase = false,
 *     requireLowercase = false,
 *     requireDigit = false,
 *     requireSpecial = false
 * )
 * private String basicPassword;
 *
 * ---
 *
 * Example 4: Combined with other annotations
 *
 * @NotBlank(message = "Password is required")
 * @StrongPassword(
 *     minLength = 10,
 *     message = "Password must meet security requirements"
 * )
 * private String securePassword;
 *
 * ============================================
 * VALIDATION EXAMPLES
 * ============================================
 *
 * Input              | Result | Reason
 * -------------------|--------|---------------------------
 * "Pass123!"         | ✓ PASS | Meets all requirements
 * "password"         | ✗ FAIL | No uppercase, digit, special
 * "PASSWORD123!"     | ✗ FAIL | No lowercase
 * "Pass123"          | ✗ FAIL | No special character
 * "Pass!"            | ✗ FAIL | Too short, no digit
 * "P@ss1"            | ✗ FAIL | Too short (< 8 characters)
 * "MyP@ssw0rd"       | ✓ PASS | Meets all requirements
 * "Admin@123"        | ✓ PASS | Meets all requirements
 */
