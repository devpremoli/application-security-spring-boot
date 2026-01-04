package com.security.jwt.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TEST CONTROLLER
 *
 * This controller demonstrates role-based access control using Spring Security
 * It provides test endpoints with different authorization levels
 *
 * Purpose:
 * - Learn how to protect endpoints by role
 * - Understand @PreAuthorize annotation
 * - Test JWT authentication with different user roles
 *
 * Endpoints:
 * 1. /api/test/all - Public (no authentication needed)
 * 2. /api/test/user - Requires ROLE_USER (any authenticated user)
 * 3. /api/test/mod - Requires ROLE_MODERATOR
 * 4. /api/test/admin - Requires ROLE_ADMIN
 *
 * @RestController: All methods return data (JSON)
 * @RequestMapping: Base path for all endpoints
 * @CrossOrigin: Allow cross-origin requests
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * PUBLIC ENDPOINT
     *
     * Accessible by anyone, no authentication required
     *
     * Endpoint: GET /api/test/all
     *
     * Use cases:
     * - Testing if API is running
     * - Public information
     * - Health check
     *
     * How to test:
     * curl http://localhost:8080/api/test/all
     *
     * Expected response:
     * "Public Content."
     *
     * No JWT token needed!
     */
    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    /**
     * USER ENDPOINT
     *
     * Requires authentication (any logged-in user)
     * Accessible by users with ROLE_USER, ROLE_MODERATOR, or ROLE_ADMIN
     *
     * Endpoint: GET /api/test/user
     *
     * @PreAuthorize("hasRole('USER')"):
     * - Method-level security annotation
     * - Checks if authenticated user has ROLE_USER
     * - Spring Security automatically adds "ROLE_" prefix
     * - So hasRole('USER') actually checks for 'ROLE_USER'
     *
     * What happens if user doesn't have the role?
     * 1. Spring Security blocks the request
     * 2. Returns 403 Forbidden
     * 3. User is authenticated but not authorized
     *
     * How to test:
     * 1. Login and get JWT token
     * 2. Send request with token:
     *    curl -H "Authorization: Bearer <token>" http://localhost:8080/api/test/user
     *
     * Expected response (if user has ROLE_USER):
     * "User Content."
     *
     * Expected response (if user doesn't have ROLE_USER):
     * 403 Forbidden
     *
     * @PreAuthorize vs @Secured vs URL-based security:
     * - @PreAuthorize: Most flexible, supports SpEL expressions
     * - @Secured: Simpler, just role names
     * - URL-based (in WebSecurityConfig): Applies to all matching URLs
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userAccess() {
        return "User Content.";
    }

    /**
     * MODERATOR ENDPOINT
     *
     * Requires ROLE_MODERATOR or ROLE_ADMIN
     *
     * Endpoint: GET /api/test/mod
     *
     * @PreAuthorize("hasRole('MODERATOR')"):
     * - Checks for ROLE_MODERATOR
     * - ROLE_ADMIN users can also access (we'll see why below)
     *
     * Role hierarchy concept:
     * - In many applications, ADMIN has all MODERATOR permissions
     * - We can configure role hierarchy in SecurityConfig
     * - For simplicity, we're not implementing hierarchy here
     * - Admin would need both ADMIN and MODERATOR roles
     *
     * Alternative with multiple roles:
     * @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
     * - This allows both MODERATOR and ADMIN roles
     * - More flexible
     * - Explicit permission declaration
     *
     * How to test:
     * 1. Create user with MODERATOR role
     * 2. Login and get JWT token
     * 3. Send request:
     *    curl -H "Authorization: Bearer <token>" http://localhost:8080/api/test/mod
     *
     * Expected response (with ROLE_MODERATOR):
     * "Moderator Board."
     *
     * Expected response (without ROLE_MODERATOR):
     * 403 Forbidden
     */
    @GetMapping("/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public String moderatorAccess() {
        return "Moderator Board.";
    }

    /**
     * ADMIN ENDPOINT
     *
     * Requires ROLE_ADMIN (highest privilege)
     *
     * Endpoint: GET /api/test/admin
     *
     * @PreAuthorize("hasRole('ADMIN')"):
     * - Checks for ROLE_ADMIN
     * - Only admin users can access
     * - Most restrictive endpoint
     *
     * Use cases:
     * - System configuration
     * - User management
     * - Sensitive operations
     * - Administrative tasks
     *
     * How to test:
     * 1. Create user with ADMIN role
     * 2. Login and get JWT token
     * 3. Send request:
     *    curl -H "Authorization: Bearer <token>" http://localhost:8080/api/test/admin
     *
     * Expected response (with ROLE_ADMIN):
     * "Admin Board."
     *
     * Expected response (without ROLE_ADMIN):
     * 403 Forbidden
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }
}

/*
 * ============================================
 * ADVANCED @PreAuthorize EXAMPLES
 * ============================================
 *
 * @PreAuthorize supports powerful SpEL (Spring Expression Language) expressions
 * Here are some examples:
 *
 * 1. MULTIPLE ROLES (OR):
 * @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
 * - User must have ADMIN OR MODERATOR role
 *
 * 2. MULTIPLE ROLES (AND):
 * @PreAuthorize("hasRole('USER') and hasRole('PREMIUM')")
 * - User must have BOTH USER AND PREMIUM roles
 *
 * 3. COMPLEX CONDITIONS:
 * @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == authentication.principal.id)")
 * - Admin can access anyone's data
 * - Regular user can only access their own data
 *
 * 4. METHOD PARAMETER ACCESS:
 * @PreAuthorize("#username == authentication.principal.username")
 * public void updateProfile(String username, ProfileData data)
 * - User can only update their own profile
 *
 * 5. CUSTOM SECURITY EXPRESSIONS:
 * @PreAuthorize("@customSecurityService.canAccess(#resourceId)")
 * - Delegate to custom Spring bean for complex logic
 *
 * 6. NOT CONDITIONS:
 * @PreAuthorize("!hasRole('BANNED')")
 * - Allow all except banned users
 *
 * ============================================
 * TESTING SCENARIOS
 * ============================================
 *
 * Scenario 1: Regular User
 * - Create user with default role (ROLE_USER)
 * - Can access: /api/test/all, /api/test/user
 * - Cannot access: /api/test/mod, /api/test/admin
 *
 * Scenario 2: Moderator
 * - Create user with ROLE_MODERATOR
 * - Can access: /api/test/all, /api/test/mod
 * - Cannot access: /api/test/user (doesn't have ROLE_USER)
 * - Cannot access: /api/test/admin (doesn't have ROLE_ADMIN)
 * - Solution: Assign multiple roles [ROLE_USER, ROLE_MODERATOR]
 *
 * Scenario 3: Admin
 * - Create user with ROLE_ADMIN
 * - Can access: /api/test/all, /api/test/admin
 * - Cannot access: /api/test/user, /api/test/mod (doesn't have those roles)
 * - Solution: Assign multiple roles [ROLE_USER, ROLE_MODERATOR, ROLE_ADMIN]
 *
 * Scenario 4: Multiple Roles
 * - Create user with [ROLE_USER, ROLE_MODERATOR, ROLE_ADMIN]
 * - Can access: All endpoints
 * - This is typical for admin users
 *
 * ============================================
 * CURL COMMAND EXAMPLES
 * ============================================
 *
 * Test 1: Public endpoint (no auth)
 * curl http://localhost:8080/api/test/all
 *
 * Test 2: Register new user
 * curl -X POST http://localhost:8080/api/auth/signup \
 *   -H "Content-Type: application/json" \
 *   -d '{"username":"john","email":"john@example.com","password":"password123"}'
 *
 * Test 3: Login
 * curl -X POST http://localhost:8080/api/auth/signin \
 *   -H "Content-Type: application/json" \
 *   -d '{"username":"john","password":"password123"}'
 *
 * (Save the token from response)
 *
 * Test 4: Access user endpoint (with token)
 * curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
 *   http://localhost:8080/api/test/user
 *
 * Test 5: Try to access admin endpoint (should fail)
 * curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
 *   http://localhost:8080/api/test/admin
 *
 * Test 6: Register admin user
 * curl -X POST http://localhost:8080/api/auth/signup \
 *   -H "Content-Type: application/json" \
 *   -d '{"username":"admin","email":"admin@example.com","password":"admin123","roles":["admin","user"]}'
 *
 * Test 7: Login as admin
 * curl -X POST http://localhost:8080/api/auth/signin \
 *   -H "Content-Type: application/json" \
 *   -d '{"username":"admin","password":"admin123"}'
 *
 * Test 8: Access admin endpoint (with admin token)
 * curl -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
 *   http://localhost:8080/api/test/admin
 *
 * ============================================
 * COMMON ERRORS AND SOLUTIONS
 * ============================================
 *
 * Error 1: 401 Unauthorized
 * - Cause: No JWT token or invalid token
 * - Solution: Include valid token in Authorization header
 * - Format: Authorization: Bearer <token>
 *
 * Error 2: 403 Forbidden
 * - Cause: User authenticated but doesn't have required role
 * - Solution: Assign appropriate role to user
 * - Example: User trying to access /api/test/admin without ROLE_ADMIN
 *
 * Error 3: "Access Denied" message
 * - Cause: @PreAuthorize condition failed
 * - Solution: Check user's roles match the requirement
 * - Debug: Log authentication.authorities to see actual roles
 *
 * Error 4: "Anonymous User"
 * - Cause: JWT token not being processed
 * - Solution: Check Authorization header format
 * - Must be: "Bearer " + token (note the space!)
 *
 * Error 5: Token expired
 * - Cause: JWT token passed expiration time
 * - Solution: Login again to get new token
 * - Or implement refresh token mechanism
 */
