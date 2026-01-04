package com.security.jwt.security.jwt;

import com.security.jwt.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT AUTHENTICATION FILTER
 *
 * This is the HEART of JWT authentication!
 * It runs on EVERY HTTP request to validate JWT tokens
 *
 * What is a Filter?
 * - Component that intercepts HTTP requests/responses
 * - Part of the servlet specification
 * - Can modify request, response, or block request
 * - Runs before controllers
 *
 * Filter Chain:
 * Request → Filter 1 → Filter 2 → Filter 3 → Controller
 * Response ← Filter 1 ← Filter 2 ← Filter 3 ← Controller
 *
 * OncePerRequestFilter:
 * - Spring class that guarantees filter executes once per request
 * - Prevents duplicate executions
 * - Handles error dispatches correctly
 *
 * This Filter's Job:
 * 1. Extract JWT token from Authorization header
 * 2. Validate the token
 * 3. Extract username from token
 * 4. Load user details from database
 * 5. Set authentication in SecurityContext
 * 6. Let request continue to controller
 *
 * @Component: Makes this a Spring-managed bean
 */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    /*
     * DEPENDENCIES
     *
     * Injected by Spring using @Autowired
     */

    @Autowired
    private JwtUtils jwtUtils; // For parsing and validating JWT tokens

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // For loading user from database

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    /**
     * DO FILTER INTERNAL
     *
     * This method is called for EVERY HTTP request
     * It's the main logic of the filter
     *
     * Request Flow:
     * 1. Parse Authorization header
     * 2. Extract JWT token
     * 3. Validate token
     * 4. Extract username
     * 5. Load user details
     * 6. Create authentication
     * 7. Set in SecurityContext
     * 8. Continue filter chain
     *
     * @param request - The incoming HTTP request
     * @param response - The HTTP response
     * @param filterChain - The chain of filters to execute
     * @throws ServletException - If servlet error occurs
     * @throws IOException - If I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            /*
             * STEP 1: EXTRACT JWT TOKEN FROM REQUEST
             *
             * parseJwt() method (defined below) does:
             * 1. Gets "Authorization" header from request
             * 2. Checks if it starts with "Bearer "
             * 3. Extracts the token part (after "Bearer ")
             * 4. Returns token string or null if not found
             *
             * Expected header format:
             * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
             */
            String jwt = parseJwt(request);

            /*
             * STEP 2: VALIDATE TOKEN AND PROCESS
             *
             * Only proceed if:
             * - jwt is not null (token was provided)
             * - jwt is valid (not tampered, not expired)
             *
             * If token is null:
             * - User didn't provide Authorization header
             * - Might be accessing public endpoint
             * - Skip authentication, let Spring Security handle it
             *
             * If token is invalid:
             * - validateJwtToken() returns false
             * - Skip authentication
             * - If endpoint is protected, AuthEntryPointJwt handles error
             */
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                /*
                 * STEP 3: EXTRACT USERNAME FROM TOKEN
                 *
                 * jwtUtils.getUsernameFromJwtToken():
                 * - Parses the JWT token
                 * - Extracts the "subject" claim
                 * - Returns username string
                 *
                 * The username is what we set when generating the token
                 * See JwtUtils.generateJwtToken() where we set .subject(username)
                 */
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                /*
                 * STEP 4: LOAD USER DETAILS FROM DATABASE
                 *
                 * Why load from database if we have token?
                 * - Token might be old, user data might have changed
                 * - User roles might have been updated
                 * - User might have been disabled/locked
                 * - Get fresh, current user information
                 *
                 * userDetailsService.loadUserByUsername():
                 * - Queries database for user
                 * - Loads user with current roles
                 * - Returns UserDetailsImpl
                 *
                 * This is the same service used during login
                 * See UserDetailsServiceImpl.loadUserByUsername()
                 */
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                /*
                 * STEP 5: CREATE AUTHENTICATION OBJECT
                 *
                 * UsernamePasswordAuthenticationToken:
                 * - Spring Security's authentication object
                 * - Represents an authenticated user
                 * - Contains: principal, credentials, authorities
                 *
                 * Constructor parameters:
                 * 1. userDetails: The authenticated user (principal)
                 * 2. null: Credentials (we don't need password after authentication)
                 * 3. userDetails.getAuthorities(): User's roles/permissions
                 *
                 * Why null for credentials?
                 * - Password is only needed during login
                 * - After authentication, we don't need it
                 * - Security: Don't keep password in memory
                 *
                 * Principal vs Credentials:
                 * - Principal: Who you are (user object)
                 * - Credentials: What you know (password)
                 * - After login, we only need principal
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                /*
                 * STEP 6: SET AUTHENTICATION DETAILS
                 *
                 * Adds extra details about the authentication
                 * - IP address of client
                 * - Session ID
                 * - Other request details
                 *
                 * WebAuthenticationDetailsSource:
                 * - Builds WebAuthenticationDetails from request
                 * - Extracts IP, session ID, etc.
                 *
                 * Why add details?
                 * - Useful for audit logging
                 * - Security monitoring
                 * - Tracking user sessions
                 */
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                /*
                 * STEP 7: SET AUTHENTICATION IN SECURITY CONTEXT
                 *
                 * THIS IS THE CRITICAL STEP!
                 *
                 * SecurityContextHolder:
                 * - Thread-local storage for security information
                 * - Each request has its own SecurityContext
                 * - Accessible anywhere in the request thread
                 *
                 * getContext():
                 * - Gets the SecurityContext for current thread/request
                 *
                 * setAuthentication():
                 * - Stores the authentication object in context
                 * - Marks the request as authenticated
                 * - Spring Security uses this for authorization
                 *
                 * After this line:
                 * - User is considered authenticated
                 * - Can access protected endpoints (if authorized)
                 * - Can be retrieved anywhere: SecurityContextHolder.getContext().getAuthentication()
                 *
                 * Controllers can access authenticated user:
                 * @GetMapping("/me")
                 * public String getCurrentUser() {
                 *     UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder
                 *         .getContext().getAuthentication().getPrincipal();
                 *     return user.getUsername();
                 * }
                 *
                 * Or using @AuthenticationPrincipal:
                 * @GetMapping("/me")
                 * public String getCurrentUser(@AuthenticationPrincipal UserDetailsImpl user) {
                 *     return user.getUsername();
                 * }
                 */
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            /*
             * EXCEPTION HANDLING
             *
             * If any error occurs during authentication:
             * - Log the error
             * - Don't set authentication
             * - Let request continue
             *
             * Spring Security will see no authentication and:
             * - Block access to protected endpoints
             * - Call AuthEntryPointJwt
             * - Return 401 Unauthorized
             *
             * Why not throw exception?
             * - We want request to continue
             * - Let Spring Security's normal flow handle it
             * - Throwing here would cause 500 error instead of 401
             *
             * Common exceptions:
             * - UsernameNotFoundException: User deleted from database
             * - ExpiredJwtException: Token expired
             * - SignatureException: Invalid token signature
             */
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        /*
         * STEP 8: CONTINUE FILTER CHAIN
         *
         * THIS IS CRUCIAL! Don't forget this line!
         *
         * filterChain.doFilter():
         * - Passes request to next filter in chain
         * - Eventually reaches the controller
         * - Without this, request is blocked
         *
         * Filter chain continues to:
         * 1. Other security filters
         * 2. Spring MVC DispatcherServlet
         * 3. Controller
         * 4. Back through filters
         * 5. Response to client
         *
         * Even if authentication failed, we continue:
         * - Request might be to public endpoint (no auth needed)
         * - Spring Security will handle authorization
         * - If protected and not authenticated, returns 401
         */
        filterChain.doFilter(request, response);
    }

    /**
     * PARSE JWT TOKEN FROM REQUEST
     *
     * Extracts JWT token from the Authorization header
     *
     * Expected header format:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *
     * Process:
     * 1. Get "Authorization" header
     * 2. Check if it exists and starts with "Bearer "
     * 3. Extract token (everything after "Bearer ")
     * 4. Return token string
     *
     * @param request - The HTTP request
     * @return JWT token string, or null if not found
     */
    private String parseJwt(HttpServletRequest request) {
        /*
         * GET AUTHORIZATION HEADER
         *
         * request.getHeader("Authorization"):
         * - Retrieves the value of Authorization header
         * - Returns null if header doesn't exist
         *
         * Example values:
         * - "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
         * - "Basic dXNlcjpwYXNzd29yZA==" (different auth scheme)
         * - null (no header)
         */
        String headerAuth = request.getHeader("Authorization");

        /*
         * VALIDATE AND EXTRACT TOKEN
         *
         * StringUtils.hasText():
         * - Checks if string is not null, not empty, not whitespace
         * - Returns true only if string has actual content
         *
         * headerAuth.startsWith("Bearer "):
         * - Checks if header uses Bearer authentication scheme
         * - "Bearer " is 7 characters (note the space!)
         *
         * headerAuth.substring(7):
         * - Extracts everything after "Bearer " (index 7 onwards)
         * - This is the actual JWT token
         *
         * Example:
         * Input: "Bearer eyJhbGciOiJIUzI1..."
         * Output: "eyJhbGciOiJIUzI1..."
         *
         * Why check hasText?
         * - Prevents null pointer exception
         * - Prevents processing empty strings
         *
         * Why check startsWith?
         * - Ensures correct authentication scheme
         * - Could be "Basic" or other schemes
         * - We only support Bearer tokens
         */
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix
        }

        // If header doesn't exist or doesn't start with "Bearer ", return null
        return null;
    }
}

/*
 * ============================================
 * REQUEST FLOW EXAMPLE
 * ============================================
 *
 * Example: User accessing protected endpoint
 *
 * Client Request:
 * GET /api/admin/users
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
 *
 * Filter Execution:
 * 1. doFilterInternal() is called
 * 2. parseJwt() extracts token: "eyJhbGci..."
 * 3. jwtUtils.validateJwtToken() validates: true
 * 4. jwtUtils.getUsernameFromJwtToken() extracts: "john"
 * 5. userDetailsService.loadUserByUsername("john") queries database
 * 6. Returns UserDetailsImpl with roles: [ROLE_USER, ROLE_ADMIN]
 * 7. Creates UsernamePasswordAuthenticationToken
 * 8. Sets in SecurityContextHolder
 * 9. filterChain.doFilter() continues to controller
 * 10. Controller accesses SecurityContext to get user
 * 11. Checks if user has required role
 * 12. Returns response
 *
 * ============================================
 * PUBLIC ENDPOINT EXAMPLE
 * ============================================
 *
 * Client Request:
 * POST /api/auth/signin
 * (No Authorization header)
 *
 * Filter Execution:
 * 1. doFilterInternal() is called
 * 2. parseJwt() returns null (no header)
 * 3. if (jwt != null) is false
 * 4. Skip authentication setup
 * 5. filterChain.doFilter() continues
 * 6. Controller handles login
 * 7. Returns JWT token
 *
 * No authentication needed because /api/auth/** is configured as public in SecurityConfig
 *
 * ============================================
 * SECURITY NOTES
 * ============================================
 *
 * 1. Token Storage:
 *    - Client should store token securely
 *    - LocalStorage: Easy but vulnerable to XSS
 *    - HttpOnly Cookie: More secure, immune to XSS
 *    - Memory: Most secure but lost on refresh
 *
 * 2. Token Expiration:
 *    - Always set reasonable expiration time
 *    - Force re-authentication periodically
 *    - Implement refresh token for better UX
 *
 * 3. HTTPS:
 *    - ALWAYS use HTTPS in production
 *    - JWT in Authorization header can be intercepted
 *    - HTTPS encrypts the entire request
 *
 * 4. Token Revocation:
 *    - JWT tokens can't be revoked (stateless)
 *    - Solutions:
 *      a) Short expiration times
 *      b) Maintain blacklist of revoked tokens
 *      c) Use token versioning
 *      d) Check user status on each request
 *
 * 5. Sensitive Operations:
 *    - For critical operations (change password, delete account)
 *    - Require re-authentication
 *    - Don't rely only on JWT token
 */
