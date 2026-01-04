package com.security.jwt.security.services;

import com.security.jwt.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * USER DETAILS IMPLEMENTATION
 *
 * This class is an ADAPTER between our User entity and Spring Security
 *
 * Why do we need this adapter?
 * - Spring Security expects UserDetails interface
 * - Our User entity doesn't implement UserDetails
 * - This class wraps User and implements UserDetails
 * - Allows Spring Security to work with our custom User entity
 *
 * ADAPTER PATTERN:
 * - Design pattern that makes incompatible interfaces work together
 * - Our User entity → UserDetailsImpl → Spring Security's UserDetails
 *
 * What is UserDetails?
 * - Core Spring Security interface
 * - Represents an authenticated user
 * - Contains authentication and authorization information
 * - Methods: getUsername(), getPassword(), getAuthorities(), etc.
 */
public class UserDetailsImpl implements UserDetails {

    /*
     * Serialization ID
     * Required when implementing Serializable (UserDetails extends it)
     */
    private static final long serialVersionUID = 1L;

    /*
     * USER INFORMATION FIELDS
     *
     * These mirror the fields in our User entity
     * We copy data from User to UserDetailsImpl
     */
    private Long id;
    private String username;
    private String email;

    /*
     * PASSWORD FIELD
     *
     * @JsonIgnore: Prevents password from being serialized to JSON
     *
     * Why JsonIgnore?
     * - Security: Never send password (even hashed) to client
     * - If UserDetailsImpl is accidentally returned in API response
     * - Password won't be included in JSON
     *
     * Critical Security Practice:
     * - Never expose passwords in API responses
     * - Never log passwords
     * - Never display passwords in UI
     */
    @JsonIgnore
    private String password;

    /*
     * AUTHORITIES (Roles/Permissions)
     *
     * Collection<? extends GrantedAuthority>:
     * - Spring Security's way of representing roles and permissions
     * - Each GrantedAuthority is a role like "ROLE_ADMIN"
     * - Used for authorization decisions
     *
     * GrantedAuthority vs Role:
     * - GrantedAuthority: Generic Spring Security term
     * - Can represent roles: "ROLE_ADMIN"
     * - Can represent permissions: "READ_PRIVILEGES", "WRITE_PRIVILEGES"
     * - In our case, we use it for roles
     */
    private Collection<? extends GrantedAuthority> authorities;

    /*
     * CONSTRUCTOR
     *
     * Creates UserDetailsImpl from our fields
     * Called by the static build() method below
     */
    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * BUILD METHOD - Factory Pattern
     *
     * Converts User entity to UserDetailsImpl
     * This is a static factory method
     *
     * Factory Pattern Benefits:
     * - Encapsulates object creation logic
     * - Clear method name describes what it does
     * - Can add validation or transformation logic
     *
     * Process:
     * 1. Extract roles from User entity
     * 2. Convert roles to GrantedAuthority objects
     * 3. Create UserDetailsImpl with converted data
     *
     * @param user - Our User entity from database
     * @return UserDetailsImpl ready for Spring Security
     */
    public static UserDetailsImpl build(User user) {
        /*
         * CONVERT ROLES TO GRANTED AUTHORITIES
         *
         * Stream API transformation:
         * 1. user.getRoles(): Get Set<Role> from user
         * 2. .stream(): Convert Set to Stream for processing
         * 3. .map(): Transform each Role to SimpleGrantedAuthority
         * 4. .collect(): Collect results into a List
         *
         * SimpleGrantedAuthority:
         * - Spring Security's basic implementation of GrantedAuthority
         * - Just wraps a string (e.g., "ROLE_ADMIN")
         * - Used in authorization checks
         *
         * getName().name():
         * - role.getName() returns ERole enum
         * - .name() converts enum to String ("ROLE_ADMIN")
         */
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        /*
         * CREATE AND RETURN UserDetailsImpl
         *
         * Copy all relevant data from User to UserDetailsImpl
         * Spring Security will use this object for authentication
         */
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    /*
     * ============================================
     * USERDETAILS INTERFACE METHODS
     * ============================================
     *
     * These methods are required by Spring Security's UserDetails interface
     * Spring Security calls these during authentication and authorization
     */

    /**
     * GET AUTHORITIES
     *
     * Returns user's roles/permissions
     * Called by Spring Security for authorization decisions
     *
     * Used in:
     * - @PreAuthorize annotations
     * - hasRole() checks in security config
     * - Manual authorization checks in code
     *
     * @return Collection of granted authorities (roles)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * GET PASSWORD
     *
     * Returns the hashed password
     * Called during authentication to verify credentials
     *
     * Authentication Process:
     * 1. User submits username and plain password
     * 2. Spring Security loads UserDetails by username
     * 3. Compares submitted password with this hashed password
     * 4. Uses BCryptPasswordEncoder.matches(plain, hashed)
     *
     * @return Hashed password from database
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * GET USERNAME
     *
     * Returns the username (unique identifier)
     * Called to identify the user
     *
     * Note: Spring Security uses "username" as generic term
     * - Could be actual username
     * - Could be email
     * - Could be phone number
     * - Just needs to be unique identifier
     *
     * @return Username string
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * IS ACCOUNT NON EXPIRED
     *
     * Indicates whether the user's account has expired
     *
     * Use case:
     * - Trial accounts that expire after 30 days
     * - Temporary accounts for contractors
     * - Accounts that need renewal
     *
     * Currently we return true (account never expires)
     * To implement expiration:
     * - Add expirationDate field to User entity
     * - Return expirationDate.isAfter(now)
     *
     * @return true if account is not expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * IS ACCOUNT NON LOCKED
     *
     * Indicates whether the user is locked or unlocked
     *
     * Use case:
     * - Lock account after too many failed login attempts
     * - Lock suspended accounts
     * - Lock accounts pending verification
     *
     * Currently we return true (account is never locked)
     * To implement locking:
     * - Add locked boolean field to User entity
     * - Add failedLoginAttempts counter
     * - Lock after X failed attempts
     *
     * @return true if account is not locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * IS CREDENTIALS NON EXPIRED
     *
     * Indicates whether the user's credentials (password) has expired
     *
     * Use case:
     * - Force password change every 90 days
     * - Security compliance requirements
     * - Password reset required flags
     *
     * Currently we return true (credentials never expire)
     * To implement expiration:
     * - Add passwordChangedAt field to User entity
     * - Return passwordChangedAt.plusDays(90).isAfter(now)
     *
     * @return true if credentials are not expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * IS ENABLED
     *
     * Indicates whether the user is enabled or disabled
     *
     * Use case:
     * - Disable accounts pending email verification
     * - Disable deactivated accounts
     * - Soft delete (disable instead of delete)
     *
     * Currently we return true (user is always enabled)
     * To implement:
     * - Add enabled boolean field to User entity
     * - Set false until email verified
     * - Set false for deactivated accounts
     *
     * @return true if user is enabled
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /*
     * ============================================
     * UTILITY METHODS
     * ============================================
     */

    /**
     * EQUALS METHOD
     *
     * Compares two UserDetailsImpl objects for equality
     * Important for collections, caching, and session management
     *
     * We compare by ID only:
     * - Two UserDetailsImpl objects are equal if they have same user ID
     * - Even if other fields differ (e.g., after user updates profile)
     *
     * @param o - Object to compare
     * @return true if objects represent same user
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    /*
     * ============================================
     * ADDITIONAL GETTERS
     * ============================================
     *
     * These are not part of UserDetails interface
     * But useful for our application
     */

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
