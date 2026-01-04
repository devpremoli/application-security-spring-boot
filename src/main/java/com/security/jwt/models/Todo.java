package com.security.jwt.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * TODO ENTITY
 *
 * Represents a todo item in the application
 * Demonstrates CRUD operations and user-specific data
 *
 * Each todo belongs to a user (many-to-one relationship)
 *
 * Learning concepts:
 * - JPA entity mapping
 * - Validation annotations
 * - Relationships (ManyToOne with User)
 * - Timestamp generation
 * - User-specific data filtering
 */
@Entity
@Table(name = "todos")
public class Todo {

    /**
     * PRIMARY KEY
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * TODO TITLE
     *
     * @NotBlank: Required, cannot be empty or whitespace
     * @Size: Length constraints
     */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * TODO DESCRIPTION
     *
     * Optional field for detailed description
     * @Size: Max 500 characters
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    /**
     * COMPLETION STATUS
     *
     * Tracks whether todo is completed
     * Default: false (not completed)
     */
    @Column(nullable = false)
    private Boolean completed = false;

    /**
     * OWNER/USER RELATIONSHIP
     *
     * @ManyToOne: Many todos belong to one user
     * @JoinColumn: Foreign key column name
     * FetchType.LAZY: Load user only when accessed
     *
     * Why ManyToOne?
     * - One user can have many todos
     * - Each todo belongs to one user
     *
     * This enables user-specific todo filtering:
     * - User only sees their own todos
     * - Cannot access other users' todos
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * CREATED TIMESTAMP
     *
     * @CreationTimestamp: Automatically set when entity is created
     * Hibernate annotation
     * Value set once and never changed
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * UPDATED TIMESTAMP
     *
     * @UpdateTimestamp: Automatically updated on entity modification
     * Tracks when todo was last modified
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * CONSTRUCTORS
     */

    public Todo() {
        // Default constructor required by JPA
    }

    public Todo(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
        this.completed = false;
    }

    /**
     * GETTERS AND SETTERS
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
