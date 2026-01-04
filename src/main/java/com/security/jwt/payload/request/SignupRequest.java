package com.security.jwt.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * SIGNUP REQUEST DTO
 *
 * This DTO represents the registration/signup request body:
 * {
 *   "username": "john",
 *   "email": "john@example.com",
 *   "password": "myPassword123",
 *   "roles": ["admin", "user"]  // Optional
 * }
 *
 * The signup process:
 * 1. Client sends this JSON to /api/auth/signup
 * 2. Spring validates using @Valid annotation
 * 3. If valid, controller creates User entity
 * 4. Password is hashed with BCrypt before saving
 * 5. Roles are assigned (default is USER if not specified)
 * 6. User is saved to database
 */
public class SignupRequest {

    /*
     * USERNAME FIELD
     *
     * Validation:
     * - @NotBlank: Required, cannot be empty or whitespace
     * - @Size(min=3, max=20): Length restrictions
     *
     * Why validate here AND in entity?
     * - DTO validation: Provides immediate feedback to API client
     * - Entity validation: Protects database integrity
     * - Defense in depth: Multiple layers of validation
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    /*
     * EMAIL FIELD
     *
     * @Email: Validates email format using regex
     * - Must match pattern: someone@domain.extension
     * - Examples: user@example.com, test.user@company.co.uk
     */
    @NotBlank(message = "Email is required")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    @Email(message = "Email must be valid")
    private String email;

    /*
     * PASSWORD FIELD
     *
     * @Size(min=6, max=40): Password length requirements
     * - min=6: Prevents weak passwords (in production, use stronger rules)
     * - max=40: Limit on input (stored hash is always same length)
     *
     * SECURITY BEST PRACTICES:
     * In production, also validate:
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one number
     * - At least one special character
     * - Not in common password list
     *
     * Example with custom validator:
     * @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;

    /*
     * ROLES FIELD (Optional)
     *
     * Set<String>: Collection of role names (e.g., ["admin", "moderator"])
     *
     * Why Set instead of List?
     * - Set: No duplicates allowed (user can't have "admin" role twice)
     * - Set: Order doesn't matter for roles
     *
     * Optional field:
     * - If null or empty: User gets default ROLE_USER
     * - If provided: Assign specified roles (admin can assign any role)
     *
     * Example scenarios:
     * 1. Normal signup: roles = null → Gets ROLE_USER
     * 2. Admin creating user: roles = ["moderator"] → Gets ROLE_MODERATOR
     * 3. Admin creating admin: roles = ["admin", "user"] → Gets both roles
     *
     * SECURITY NOTE:
     * In production, regular users shouldn't be able to assign roles!
     * Only admins should have this capability. Implement proper authorization.
     */
    private Set<String> roles;

    /*
     * GETTERS AND SETTERS
     */

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
