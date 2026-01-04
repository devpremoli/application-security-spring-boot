package com.security.jwt.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * STRONG PASSWORD VALIDATOR
 *
 * This class implements the validation logic for @StrongPassword annotation
 *
 * Implements ConstraintValidator<A, T>:
 * - A: The annotation type (@StrongPassword)
 * - T: The type being validated (String in this case)
 *
 * ============================================
 * HOW VALIDATOR WORKS
 * ============================================
 *
 * 1. Spring finds @StrongPassword annotation
 * 2. Looks up validator: StrongPasswordValidator
 * 3. Calls initialize() with annotation parameters
 * 4. Calls isValid() with the actual value
 * 5. Returns true (valid) or false (invalid)
 */
public class StrongPasswordValidator
        implements ConstraintValidator<StrongPassword, String> {

    /**
     * CONFIGURATION FIELDS
     *
     * These store the parameters from the annotation
     * Set in initialize() method
     */
    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecial;

    /**
     * INITIALIZE METHOD
     *
     * Called once when validator is created
     * Extracts configuration from the annotation
     *
     * @param annotation - The @StrongPassword annotation instance
     *                     Contains all the parameters (minLength, etc.)
     *
     * This method is called BEFORE any validation happens
     * Use it to:
     * - Read annotation parameters
     * - Set up validation rules
     * - Initialize any resources
     */
    @Override
    public void initialize(StrongPassword annotation) {
        /*
         * EXTRACT ANNOTATION PARAMETERS
         *
         * annotation.minLength() gets the minLength parameter value
         * Example:
         * @StrongPassword(minLength = 10) → minLength = 10
         * @StrongPassword() → minLength = 8 (default)
         */
        this.minLength = annotation.minLength();
        this.requireUppercase = annotation.requireUppercase();
        this.requireLowercase = annotation.requireLowercase();
        this.requireDigit = annotation.requireDigit();
        this.requireSpecial = annotation.requireSpecial();

        /*
         * OPTIONAL: Validation logic for parameters
         *
         * You could validate the annotation parameters themselves:
         */
        if (minLength < 1) {
            throw new IllegalArgumentException(
                "minLength must be at least 1"
            );
        }
    }

    /**
     * IS VALID METHOD
     *
     * This is where the actual validation happens
     * Called for each value that needs to be validated
     *
     * @param password - The actual password value to validate
     * @param context  - Validation context (for advanced use)
     *
     * @return true if password is valid, false otherwise
     *
     * IMPORTANT: null values
     * - Returning true for null means "null is valid"
     * - If you want to reject null, use @NotNull separately
     * - This follows Single Responsibility Principle
     *   - @NotNull checks if value exists
     *   - @StrongPassword checks if value meets requirements
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        /*
         * STEP 1: NULL CHECK
         *
         * null is considered valid by this validator
         * Why? Because checking for null is @NotNull's job
         *
         * If you want to require non-null password:
         * @NotNull
         * @StrongPassword
         * private String password;
         *
         * This follows Separation of Concerns:
         * - @NotNull: Ensures value is present
         * - @StrongPassword: Ensures value meets requirements
         */
        if (password == null) {
            return true;  // Let @NotNull handle null check
        }

        /*
         * STEP 2: LENGTH CHECK
         *
         * Verify password meets minimum length requirement
         *
         * Example:
         * minLength = 8
         * password = "Pass123!" (length 8) → PASS
         * password = "Pass12!" (length 7) → FAIL
         */
        if (password.length() < minLength) {
            /*
             * OPTIONAL: Custom error message
             *
             * You can customize the error message using context:
             */
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Password must be at least %d characters long", minLength)
            ).addConstraintViolation();

            return false;  // Validation failed
        }

        /*
         * STEP 3: UPPERCASE CHECK
         *
         * If required, ensure at least one uppercase letter (A-Z)
         *
         * Regex: .*[A-Z].*
         * - .*: Any characters (zero or more)
         * - [A-Z]: One uppercase letter
         * - .*: Any characters (zero or more)
         *
         * Examples:
         * "Password" → contains P → PASS
         * "password" → no uppercase → FAIL
         */
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            return false;
        }

        /*
         * STEP 4: LOWERCASE CHECK
         *
         * If required, ensure at least one lowercase letter (a-z)
         *
         * Regex: .*[a-z].*
         *
         * Examples:
         * "Password" → contains a,s,s,w,o,r,d → PASS
         * "PASSWORD" → no lowercase → FAIL
         */
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            return false;
        }

        /*
         * STEP 5: DIGIT CHECK
         *
         * If required, ensure at least one digit (0-9)
         *
         * Regex: .*\\d.*
         * - \\d: One digit (0-9)
         *
         * Examples:
         * "Password1" → contains 1 → PASS
         * "Password" → no digit → FAIL
         */
        if (requireDigit && !password.matches(".*\\d.*")) {
            return false;
        }

        /*
         * STEP 6: SPECIAL CHARACTER CHECK
         *
         * If required, ensure at least one special character
         *
         * Regex: .*[@$!%*?&].*
         * - [@$!%*?&]: One of these special characters
         *
         * You can customize the allowed special characters:
         * - Change to: .*[!@#$%^&*()_+\\-=\\[\\]{};':"\\\\|,.<>\\/?].*
         * - For all common special characters
         *
         * Examples:
         * "Password1!" → contains ! → PASS
         * "Password1" → no special char → FAIL
         */
        if (requireSpecial && !password.matches(".*[@$!%*?&].*")) {
            return false;
        }

        /*
         * STEP 7: ALL CHECKS PASSED
         *
         * If we reach here, password meets all requirements
         */
        return true;
    }
}

/*
 * ============================================
 * VALIDATION FLOW EXAMPLE
 * ============================================
 *
 * Example annotation usage:
 * @StrongPassword(minLength = 8, requireSpecial = true)
 * private String password;
 *
 * Input value: "Pass123"
 *
 * Flow:
 * 1. initialize() called once
 *    - minLength = 8
 *    - requireUppercase = true (default)
 *    - requireLowercase = true (default)
 *    - requireDigit = true (default)
 *    - requireSpecial = true
 *
 * 2. isValid("Pass123", context) called
 *    - Null check: "Pass123" is not null → Continue
 *    - Length check: 7 < 8 → FAIL (too short)
 *    - Returns false immediately
 *
 * 3. Spring returns validation error:
 *    "Password must be at least 8 characters long"
 *
 * ---
 *
 * Input value: "Pass1234"
 *
 * Flow:
 * 1. isValid("Pass1234", context) called
 *    - Null check: not null → Continue
 *    - Length check: 8 >= 8 → PASS
 *    - Uppercase check: Contains P → PASS
 *    - Lowercase check: Contains a,s,s → PASS
 *    - Digit check: Contains 1,2,3,4 → PASS
 *    - Special check: No special character → FAIL
 *    - Returns false
 *
 * 2. Spring returns validation error:
 *    "Password does not meet strength requirements"
 *
 * ---
 *
 * Input value: "Pass123!"
 *
 * Flow:
 * 1. isValid("Pass123!", context) called
 *    - All checks: PASS
 *    - Returns true
 *
 * 2. Validation succeeds, controller method executes
 *
 * ============================================
 * ADVANCED CUSTOMIZATIONS
 * ============================================
 *
 * 1. Custom error messages for each requirement:
 *
 * if (!password.matches(".*[A-Z].*")) {
 *     context.disableDefaultConstraintViolation();
 *     context.buildConstraintViolationWithTemplate(
 *         "Password must contain at least one uppercase letter"
 *     ).addConstraintViolation();
 *     return false;
 * }
 *
 * ---
 *
 * 2. Multiple validation messages:
 *
 * List<String> violations = new ArrayList<>();
 * if (password.length() < minLength) {
 *     violations.add("Too short");
 * }
 * if (requireUppercase && !password.matches(".*[A-Z].*")) {
 *     violations.add("Missing uppercase");
 * }
 *
 * if (!violations.isEmpty()) {
 *     context.disableDefaultConstraintViolation();
 *     context.buildConstraintViolationWithTemplate(
 *         String.join(", ", violations)
 *     ).addConstraintViolation();
 *     return false;
 * }
 *
 * ---
 *
 * 3. Password strength scoring:
 *
 * int score = 0;
 * if (password.length() >= minLength) score++;
 * if (password.matches(".*[A-Z].*")) score++;
 * if (password.matches(".*[a-z].*")) score++;
 * if (password.matches(".*\\d.*")) score++;
 * if (password.matches(".*[@$!%*?&].*")) score++;
 *
 * // Require minimum score
 * return score >= 4; // At least 4 out of 5 requirements
 *
 * ============================================
 * TESTING THE VALIDATOR
 * ============================================
 *
 * Unit test example:
 *
 * @Test
 * void testPasswordValidation() {
 *     StrongPasswordValidator validator = new StrongPasswordValidator();
 *     StrongPassword annotation = createAnnotation();
 *     validator.initialize(annotation);
 *
 *     assertTrue(validator.isValid("Pass123!", null));
 *     assertFalse(validator.isValid("pass123!", null)); // No uppercase
 *     assertFalse(validator.isValid("Pass123", null));  // No special
 *     assertFalse(validator.isValid("Pass!", null));    // No digit
 * }
 */
