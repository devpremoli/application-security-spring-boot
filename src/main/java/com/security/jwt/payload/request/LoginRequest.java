package com.security.jwt.payload.request;

import jakarta.validation.constraints.NotBlank;

/**
 * LOGIN REQUEST DTO (Data Transfer Object)
 *
 * What is a DTO?
 * - A simple object that carries data between processes
 * - Used to transfer data from client to server
 * - Separates API structure from database structure
 *
 * Why use DTOs instead of entities directly?
 * 1. Security: Don't expose internal entity structure
 * 2. Flexibility: API can have different fields than database
 * 3. Validation: Apply different validation rules than entity
 * 4. Decoupling: API changes don't force database changes
 *
 * This DTO represents the login request body:
 * {
 *   "username": "john",
 *   "password": "myPassword123"
 * }
 *
 * Spring automatically converts JSON to this object (deserialization)
 */
public class LoginRequest {

    /*
     * USERNAME FIELD
     *
     * @NotBlank: Ensures username is provided
     * - Combines @NotNull, @NotEmpty, and no whitespace
     * - Validation happens in controller with @Valid annotation
     */
    @NotBlank(message = "Username is required")
    private String username;

    /*
     * PASSWORD FIELD
     *
     * @NotBlank: Ensures password is provided
     *
     * SECURITY NOTE:
     * - This is the PLAIN TEXT password from user input
     * - It's only in memory temporarily during authentication
     * - Never logged or stored in plain text
     * - Immediately compared with hashed password using BCrypt
     */
    @NotBlank(message = "Password is required")
    private String password;

    /*
     * GETTERS AND SETTERS
     *
     * Required for:
     * - Jackson (JSON library) to deserialize JSON into this object
     * - Spring validation to access values
     *
     * Note: Could use Lombok's @Data to auto-generate
     */

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
