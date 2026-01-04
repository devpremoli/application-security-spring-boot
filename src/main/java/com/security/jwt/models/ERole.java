package com.security.jwt.models;

/**
 * ROLE ENUMERATION
 *
 * An enum is a special Java type that represents a fixed set of constants
 * Here we define all possible roles in our application
 *
 * Why use an enum instead of String?
 * - Type safety: Compiler catches typos (can't accidentally use "ADMIN" vs "ROLE_ADMIN")
 * - Limited values: Only these specific roles can exist
 * - IDE support: Auto-completion shows available roles
 * - Refactoring: Easy to rename roles across entire codebase
 *
 * Naming Convention: ROLE_*
 * - Spring Security convention is to prefix roles with "ROLE_"
 * - When using @PreAuthorize("hasRole('ADMIN')"), Spring adds "ROLE_" prefix
 * - When using @PreAuthorize("hasAuthority('ROLE_ADMIN')"), use full name
 *
 * Common Role Hierarchy:
 * - ROLE_USER: Basic authenticated user, can access standard features
 * - ROLE_MODERATOR: Can moderate content, manage users (but not system config)
 * - ROLE_ADMIN: Full system access, can manage everything
 */
public enum ERole {
    /**
     * USER ROLE
     * - Default role for registered users
     * - Can access standard application features
     * - Cannot access admin or moderator features
     */
    ROLE_USER,

    /**
     * MODERATOR ROLE
     * - Elevated privileges for content moderation
     * - Can manage user content, handle reports, etc.
     * - Typical use: Community moderators, support staff
     */
    ROLE_MODERATOR,

    /**
     * ADMIN ROLE
     * - Highest level of access
     * - Can manage users, roles, system configuration
     * - Should be given only to trusted personnel
     * - Typical use: System administrators
     */
    ROLE_ADMIN
}

/*
 * USAGE EXAMPLES:
 *
 * 1. In Controller - Method Level Security:
 *    @PreAuthorize("hasRole('ADMIN')")
 *    public String adminOnly() { ... }
 *
 * 2. In Controller - Multiple Roles:
 *    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
 *    public String moderatorOrAdmin() { ... }
 *
 * 3. In Security Config - URL Based:
 *    http.authorizeRequests()
 *        .antMatchers("/api/admin/**").hasRole("ADMIN")
 *        .antMatchers("/api/mod/**").hasAnyRole("MODERATOR", "ADMIN")
 *
 * 4. Checking in Code:
 *    boolean isAdmin = userDetails.getAuthorities().stream()
 *        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
 */
