package com.security.jwt.models;

import jakarta.persistence.*;

/**
 * ROLE ENTITY
 *
 * This class represents a user role (e.g., USER, ADMIN, MODERATOR)
 * It's used for authorization - determining what users can do
 *
 * Difference between Authentication and Authorization:
 * - Authentication: Who are you? (Login with username/password)
 * - Authorization: What can you do? (Roles and permissions)
 *
 * Example:
 * - All users are authenticated after login
 * - Only users with ADMIN role can access admin endpoints
 */
@Entity
@Table(name = "roles")
public class Role {

    /*
     * PRIMARY KEY - ID
     *
     * Auto-incremented unique identifier for each role
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * ROLE NAME
     *
     * @Enumerated(EnumType.STRING):
     * - Stores the enum value as a string in the database
     * - Alternative: EnumType.ORDINAL stores as integer (0, 1, 2...)
     *
     * Why STRING over ORDINAL?
     * - More readable in database: "ROLE_ADMIN" vs "2"
     * - Safer: Adding new enum values won't break existing data
     * - ORDINAL issue: If you reorder enum, data becomes incorrect
     *
     * @Column(length = 20):
     * - Limits VARCHAR size in database
     * - "ROLE_MODERATOR" is 14 chars, so 20 is safe
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    /*
     * CONSTRUCTORS
     */

    /**
     * Default constructor required by JPA
     */
    public Role() {
    }

    /**
     * Parameterized constructor for easy role creation
     */
    public Role(ERole name) {
        this.name = name;
    }

    /*
     * GETTERS AND SETTERS
     */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ERole getName() {
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }
}
