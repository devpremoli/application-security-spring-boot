package com.security.jwt.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * USER ENTITY
 *
 * This class represents a user in our application and maps to a database table.
 * It uses JPA (Java Persistence API) annotations to define how it should be stored.
 *
 * Key Concepts:
 * - Entity: A domain object that is persisted to the database
 * - Table: The actual database table where data is stored
 * - Each instance of this class represents one row in the 'users' table
 */
@Entity
@Table(name = "users",
       uniqueConstraints = {
           /*
            * UNIQUE CONSTRAINTS
            * Ensures that certain fields must be unique across all rows
            * - No two users can have the same username
            * - No two users can have the same email
            * - Database will reject inserts/updates that violate this
            */
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
public class User {

    /*
     * PRIMARY KEY - ID
     *
     * @Id: Marks this field as the primary key (unique identifier for each row)
     * @GeneratedValue: Database automatically generates the value
     * - GenerationType.IDENTITY: Uses database auto-increment feature
     * - Each new user gets the next available ID automatically
     *
     * Why use Long instead of int?
     * - Long can hold much larger numbers (2^63 vs 2^31)
     * - Prevents ID overflow in large applications
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * USERNAME FIELD
     *
     * Validation Annotations:
     * @NotBlank: Field cannot be null, empty, or just whitespace
     * @Size: Restricts the length of the string
     * - min = 3: Username must be at least 3 characters
     * - max = 20: Username cannot exceed 20 characters
     *
     * These validations happen before data reaches the database
     * Spring automatically validates when @Valid is used in controllers
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    /*
     * EMAIL FIELD
     *
     * @Email: Validates that the string matches email format
     * - Checks for pattern like: user@domain.com
     * - Uses standard email regex validation
     *
     * @Size: Email length validation
     * - max = 50: Prevents extremely long emails
     */
    @NotBlank(message = "Email is required")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    @Email(message = "Email must be valid")
    private String email;

    /*
     * PASSWORD FIELD
     *
     * IMPORTANT SECURITY NOTES:
     * - This stores the HASHED password, NOT the plain text
     * - We use BCrypt to hash passwords before saving
     * - BCrypt is a one-way hash (cannot be decrypted)
     * - Even if database is compromised, passwords are safe
     *
     * @Size(max = 120): BCrypt hashes are 60 chars, but we allow 120 for flexibility
     *
     * Password Hashing Process:
     * 1. User provides plain password: "myPassword123"
     * 2. BCrypt generates salt and hashes: "$2a$10$abc...xyz" (60 chars)
     * 3. We store only the hash in database
     * 4. To verify: BCrypt.matches(plainPassword, hashedPassword)
     */
    @NotBlank(message = "Password is required")
    @Size(max = 120, message = "Password hash cannot exceed 120 characters")
    private String password;

    /*
     * ROLES - MANY-TO-MANY RELATIONSHIP
     *
     * Why Many-to-Many?
     * - One user can have multiple roles (e.g., USER and ADMIN)
     * - One role can be assigned to multiple users
     *
     * @ManyToMany: Defines the relationship type
     *
     * @JoinTable: Creates a join table to manage the relationship
     * - Join table name: "user_roles"
     * - Columns:
     *   - user_id: References the user (foreign key to users.id)
     *   - role_id: References the role (foreign key to roles.id)
     *
     * FetchType.LAZY vs EAGER:
     * - LAZY: Roles are loaded only when accessed (better performance)
     * - EAGER: Roles are loaded immediately with user (can cause N+1 problem)
     *
     * We use LAZY to avoid loading roles unless needed
     *
     * Example Database Structure:
     * users table: id | username | email | password
     * roles table: id | name
     * user_roles table: user_id | role_id
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /*
     * CONSTRUCTORS
     *
     * Why do we need multiple constructors?
     * - Default constructor: Required by JPA/Hibernate
     * - Parameterized constructor: Convenient for creating users
     */

    /**
     * Default constructor required by JPA
     * JPA uses reflection to create instances
     */
    public User() {
    }

    /**
     * Parameterized constructor for easy user creation
     * Note: ID is not included because it's auto-generated
     * Note: Roles are not included because they're set separately
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /*
     * GETTERS AND SETTERS
     *
     * Why do we need these?
     * - Encapsulation: Hides internal implementation
     * - JPA needs them to access/modify fields
     * - Allows validation logic if needed
     *
     * Note: We could use Lombok's @Data annotation to auto-generate these
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
