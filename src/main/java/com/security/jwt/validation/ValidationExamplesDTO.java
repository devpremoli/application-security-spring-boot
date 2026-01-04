package com.security.jwt.validation;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * COMPREHENSIVE VALIDATION EXAMPLES
 *
 * This class demonstrates ALL standard Bean Validation annotations
 * Each field shows a different validation constraint with detailed comments
 *
 * Use this as a reference for learning all validation options
 */
public class ValidationExamplesDTO {

    /*
     * ============================================
     * NULL/EMPTY CHECKS
     * ============================================
     */

    /**
     * @NotNull - Value cannot be null
     *
     * ALLOWS:
     * - Empty strings: ""
     * - Whitespace strings: "   "
     * - Empty collections: []
     *
     * REJECTS:
     * - null
     *
     * USE WHEN: You need the field to exist but can be empty
     */
    @NotNull(message = "Field1 cannot be null")
    private String field1;

    /**
     * @NotEmpty - Value cannot be null or empty
     *
     * ALLOWS:
     * - Whitespace strings: "   " (has content, even if just spaces)
     * - Non-empty collections
     *
     * REJECTS:
     * - null
     * - Empty string: ""
     * - Empty collection: []
     *
     * USE WHEN: String or collection must have some content
     */
    @NotEmpty(message = "Field2 cannot be null or empty")
    private String field2;

    /**
     * @NotBlank - Value cannot be null, empty, or only whitespace
     *
     * This is the STRICTEST null/empty check
     *
     * ALLOWS:
     * - "john" (valid content)
     * - " john " (has content after trim)
     *
     * REJECTS:
     * - null
     * - Empty string: ""
     * - Whitespace only: "   "
     * - Tab/newline only: "\t\n"
     *
     * USE WHEN: User input that must have meaningful content
     * BEST FOR: usernames, passwords, names, etc.
     */
    @NotBlank(message = "Username is required and cannot be blank")
    private String username;

    /**
     * COMPARISON: NotNull vs NotEmpty vs NotBlank
     *
     * Input     | @NotNull | @NotEmpty | @NotBlank
     * ----------|----------|-----------|----------
     * null      | ✗ FAIL   | ✗ FAIL    | ✗ FAIL
     * ""        | ✓ PASS   | ✗ FAIL    | ✗ FAIL
     * "   "     | ✓ PASS   | ✓ PASS    | ✗ FAIL
     * "john"    | ✓ PASS   | ✓ PASS    | ✓ PASS
     */

    /*
     * ============================================
     * SIZE CONSTRAINTS
     * ============================================
     */

    /**
     * @Size - Validates size/length of strings, collections, arrays, maps
     *
     * Parameters:
     * - min: minimum size (inclusive)
     * - max: maximum size (inclusive)
     *
     * Works on:
     * - String: character count
     * - Collection: element count
     * - Array: element count
     * - Map: entry count
     */
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String usernameWithSize;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * @Size on Collections
     *
     * Example: User must select 1-5 interests
     */
    @NotNull
    @Size(min = 1, max = 5, message = "Please select 1 to 5 interests")
    private List<String> interests;

    /*
     * ============================================
     * NUMERIC CONSTRAINTS
     * ============================================
     */

    /**
     * @Min - Minimum value (inclusive)
     *
     * Works on:
     * - Integer, Long, Short, Byte
     * - BigInteger, BigDecimal
     * - float, double
     *
     * Use for: age, quantity, rating, etc.
     */
    @NotNull
    @Min(value = 18, message = "Age must be at least 18")
    private Integer age;

    /**
     * @Max - Maximum value (inclusive)
     *
     * Example: Discount percentage must be 0-100
     */
    @NotNull
    @Min(value = 0, message = "Discount cannot be negative")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    private Integer discountPercentage;

    /**
     * @DecimalMin / @DecimalMax - For decimal precision
     *
     * Useful for:
     * - Prices (must be > 0)
     * - Rates
     * - Percentages
     *
     * Parameters:
     * - value: String representation of number
     * - inclusive: whether boundary is included (default true)
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed $999,999.99")
    private BigDecimal price;

    /**
     * @Positive - Value must be > 0
     *
     * Simpler than @Min(1) for positive numbers
     */
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    /**
     * @PositiveOrZero - Value must be >= 0
     *
     * Use for: stock count, balance, etc.
     */
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    /**
     * @Negative - Value must be < 0
     *
     * Use for: debts, temperature below zero, etc.
     */
    @Negative(message = "Debt must be negative")
    private Integer debt;

    /**
     * @NegativeOrZero - Value must be <= 0
     */
    @NegativeOrZero(message = "Balance must be zero or negative")
    private Integer negativeBalance;

    /*
     * ============================================
     * STRING PATTERN MATCHING
     * ============================================
     */

    /**
     * @Pattern - Validates against regular expression
     *
     * Parameters:
     * - regexp: regular expression pattern
     * - flags: regex flags (optional)
     * - message: error message
     *
     * Common patterns:
     * - Alphanumeric: ^[A-Za-z0-9]+$
     * - Letters only: ^[A-Za-z]+$
     * - Phone: ^\d{3}-\d{3}-\d{4}$
     * - Postal code: ^\d{5}(-\d{4})?$
     */

    // Alphanumeric only
    @Pattern(
        regexp = "^[A-Za-z0-9]+$",
        message = "Username can only contain letters and numbers"
    )
    private String alphanumericUsername;

    // Phone number (US format: 123-456-7890)
    @Pattern(
        regexp = "^\\d{3}-\\d{3}-\\d{4}$",
        message = "Phone must be in format: XXX-XXX-XXXX"
    )
    private String phoneNumber;

    // Postal code (US: 12345 or 12345-6789)
    @Pattern(
        regexp = "^\\d{5}(-\\d{4})?$",
        message = "Invalid postal code format"
    )
    private String postalCode;

    // Strong password requirements
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, " +
                  "one lowercase letter, one number, and one special character"
    )
    private String strongPassword;

    // URL format
    @Pattern(
        regexp = "^https?://.*$",
        message = "URL must start with http:// or https://"
    )
    private String website;

    /*
     * ============================================
     * EMAIL VALIDATION
     * ============================================
     */

    /**
     * @Email - Validates email format according to RFC 5322
     *
     * Checks:
     * - Local part (before @)
     * - @ symbol
     * - Domain part (after @)
     *
     * Valid examples:
     * - user@example.com
     * - user.name@example.com
     * - user+tag@example.co.uk
     *
     * Invalid examples:
     * - user
     * - user@
     * - @example.com
     * - user @example.com (space)
     */
    @Email(message = "Email must be valid")
    private String email;

    /**
     * @Email with custom pattern
     *
     * Example: Corporate email that must end with @company.com
     */
    @Email(
        regexp = ".*@company\\.com$",
        message = "Must be a valid company email address (@company.com)"
    )
    private String corporateEmail;

    /*
     * ============================================
     * BOOLEAN CONSTRAINTS
     * ============================================
     */

    /**
     * @AssertTrue - Value must be true
     *
     * Use cases:
     * - Terms and conditions acceptance
     * - Confirmation checkboxes
     * - Agreement flags
     *
     * IMPORTANT: null is considered valid!
     * Combine with @NotNull if you want to enforce non-null true
     */
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean termsAccepted;

    /**
     * @AssertTrue with @NotNull
     *
     * Ensures value is not null AND true
     */
    @NotNull(message = "Privacy policy acceptance is required")
    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean privacyAccepted;

    /**
     * @AssertFalse - Value must be false
     *
     * Use cases:
     * - User not banned
     * - Account not suspended
     * - Feature not disabled
     */
    @AssertFalse(message = "Account cannot be banned")
    private Boolean banned;

    /*
     * ============================================
     * DATE/TIME CONSTRAINTS
     * ============================================
     */

    /**
     * @Past - Date must be in the past
     *
     * Works on:
     * - Date
     * - LocalDate, LocalDateTime
     * - Instant, ZonedDateTime
     *
     * Use cases:
     * - Birth date
     * - Registration date
     * - Past events
     */
    @NotNull
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    /**
     * @PastOrPresent - Date in past or today
     *
     * Use cases:
     * - Event start date
     * - Transaction date
     */
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate eventDate;

    /**
     * @Future - Date must be in the future
     *
     * Use cases:
     * - Appointment date
     * - Expiry date
     * - Scheduled tasks
     */
    @NotNull
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    /**
     * @FutureOrPresent - Date in future or today
     *
     * Use cases:
     * - Delivery date (can be today)
     * - Start date (can start today)
     */
    @FutureOrPresent(message = "Delivery date must be today or in the future")
    private LocalDate deliveryDate;

    /*
     * ============================================
     * COLLECTION CONSTRAINTS
     * ============================================
     */

    /**
     * Validating collection elements
     *
     * @Valid applies validation to each element in the collection
     */
    @NotNull
    @Size(min = 1, message = "At least one address is required")
    @Valid  // Validates each Address object in the list
    private List<Address> addresses;

    /**
     * Nested object validation
     */
    public static class Address {
        @NotBlank(message = "Street is required")
        private String street;

        @NotBlank(message = "City is required")
        private String city;

        @Pattern(regexp = "^\\d{5}$", message = "ZIP code must be 5 digits")
        private String zipCode;

        // Getters and setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    }

    /*
     * ============================================
     * COMBINING MULTIPLE CONSTRAINTS
     * ============================================
     */

    /**
     * Multiple constraints on one field
     *
     * All constraints must pass for validation to succeed
     * Constraints are evaluated in order
     */
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(
        regexp = "^[A-Za-z ]+$",
        message = "Name can only contain letters and spaces"
    )
    private String fullName;

    /**
     * Complex validation example: Credit card
     */
    @NotBlank(message = "Credit card number is required")
    @Pattern(
        regexp = "^\\d{16}$",
        message = "Credit card must be 16 digits"
    )
    // In real application, also validate with Luhn algorithm
    private String creditCardNumber;

    /*
     * ============================================
     * GETTERS AND SETTERS
     * ============================================
     */

    // Generated getters and setters for all fields
    // In real application, use Lombok's @Data to auto-generate

    public String getField1() { return field1; }
    public void setField1(String field1) { this.field1 = field1; }

    public String getField2() { return field2; }
    public void setField2(String field2) { this.field2 = field2; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUsernameWithSize() { return usernameWithSize; }
    public void setUsernameWithSize(String usernameWithSize) { this.usernameWithSize = usernameWithSize; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Integer discountPercentage) { this.discountPercentage = discountPercentage; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getDebt() { return debt; }
    public void setDebt(Integer debt) { this.debt = debt; }

    public Integer getNegativeBalance() { return negativeBalance; }
    public void setNegativeBalance(Integer negativeBalance) { this.negativeBalance = negativeBalance; }

    public String getAlphanumericUsername() { return alphanumericUsername; }
    public void setAlphanumericUsername(String alphanumericUsername) { this.alphanumericUsername = alphanumericUsername; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getStrongPassword() { return strongPassword; }
    public void setStrongPassword(String strongPassword) { this.strongPassword = strongPassword; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCorporateEmail() { return corporateEmail; }
    public void setCorporateEmail(String corporateEmail) { this.corporateEmail = corporateEmail; }

    public Boolean getTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(Boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public Boolean getPrivacyAccepted() { return privacyAccepted; }
    public void setPrivacyAccepted(Boolean privacyAccepted) { this.privacyAccepted = privacyAccepted; }

    public Boolean getBanned() { return banned; }
    public void setBanned(Boolean banned) { this.banned = banned; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCreditCardNumber() { return creditCardNumber; }
    public void setCreditCardNumber(String creditCardNumber) { this.creditCardNumber = creditCardNumber; }
}

/*
 * ============================================
 * TESTING VALIDATION
 * ============================================
 *
 * To test these validations, create a controller:
 *
 * @RestController
 * @RequestMapping("/api/validation-test")
 * public class ValidationTestController {
 *
 *     @PostMapping("/test")
 *     public ResponseEntity<?> testValidation(
 *             @Valid @RequestBody ValidationExamplesDTO dto) {
 *         return ResponseEntity.ok("All validations passed!");
 *     }
 * }
 *
 * Then test with curl:
 *
 * curl -X POST http://localhost:8080/api/validation-test/test \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "username": "",
 *     "email": "invalid-email",
 *     "age": 15
 *   }'
 *
 * Response will show all validation errors!
 */
