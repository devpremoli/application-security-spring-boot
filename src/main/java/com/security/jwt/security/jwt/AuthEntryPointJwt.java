package com.security.jwt.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AUTHENTICATION ENTRY POINT
 *
 * This class handles authentication failures for JWT-based security
 * It's the "gatekeeper" that intercepts unauthorized access attempts
 *
 * What is AuthenticationEntryPoint?
 * - Spring Security interface for handling authentication failures
 * - Called when user tries to access protected resource without authentication
 * - Called when authentication fails (invalid/expired JWT)
 *
 * Why do we need this?
 * - Default Spring Security behavior returns HTML login page
 * - We're building a REST API, not a web application
 * - We need to return JSON error response instead of HTML
 *
 * When is this called?
 * 1. User accesses protected endpoint without JWT token
 * 2. User provides invalid JWT token
 * 3. JWT token has expired
 * 4. JWT token signature is invalid
 * 5. User provides wrong username/password
 *
 * @Component: Makes this a Spring-managed bean
 * - Can be injected into SecurityConfig
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    /*
     * LOGGER
     *
     * For logging authentication failures
     * Important for:
     * - Security monitoring
     * - Debugging authentication issues
     * - Detecting brute force attacks
     * - Audit trails
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    /**
     * COMMENCE - Handle Authentication Error
     *
     * This method is automatically called by Spring Security when authentication fails
     *
     * Parameters explained:
     * @param request - The HTTP request that failed authentication
     *                  Contains: URL, headers, parameters, etc.
     *                  Useful for logging which endpoint was accessed
     *
     * @param response - The HTTP response to send back to client
     *                   We use this to set:
     *                   - Status code (401 Unauthorized)
     *                   - Content type (application/json)
     *                   - Response body (error message)
     *
     * @param authException - The exception that caused authentication failure
     *                        Contains the error message
     *                        Examples:
     *                        - "Full authentication is required"
     *                        - "Bad credentials"
     *                        - "JWT token has expired"
     *
     * @throws IOException - If writing response fails
     * @throws ServletException - If servlet error occurs
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        /*
         * LOG THE UNAUTHORIZED ACCESS ATTEMPT
         *
         * logger.error(): Logs at ERROR level
         *
         * What we log:
         * - Error message from exception
         * - Helps identify authentication issues
         * - Important for security monitoring
         *
         * In production, you might also log:
         * - request.getRequestURI() - Which endpoint was accessed
         * - request.getRemoteAddr() - IP address of requester
         * - request.getHeader("User-Agent") - Client information
         * - Timestamp (automatically added by logger)
         *
         * Example log output:
         * 2024-01-15 10:30:45 ERROR AuthEntryPointJwt - Unauthorized error: Full authentication is required to access this resource
         */
        logger.error("Unauthorized error: {}", authException.getMessage());

        /*
         * SET RESPONSE CONTENT TYPE
         *
         * MediaType.APPLICATION_JSON_VALUE = "application/json"
         *
         * Why set content type?
         * - Tells client the response is JSON
         * - Client can parse response correctly
         * - Standard REST API practice
         *
         * Without this:
         * - Client might interpret as plain text
         * - Some clients might not parse properly
         */
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        /*
         * SET HTTP STATUS CODE
         *
         * HttpServletResponse.SC_UNAUTHORIZED = 401
         *
         * HTTP Status Codes explained:
         * - 200 OK: Request succeeded
         * - 400 Bad Request: Invalid request format
         * - 401 Unauthorized: Authentication required/failed
         * - 403 Forbidden: Authenticated but not authorized
         * - 404 Not Found: Resource doesn't exist
         * - 500 Internal Server Error: Server error
         *
         * Why 401 Unauthorized?
         * - User didn't provide valid credentials
         * - Standard HTTP status for authentication failure
         * - Clients expect 401 for auth failures
         *
         * 401 vs 403:
         * - 401: "Who are you?" - Authentication failure
         * - 403: "I know who you are, but you can't access this" - Authorization failure
         *
         * Example:
         * - User not logged in → 401
         * - User logged in but not admin → 403
         */
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        /*
         * BUILD ERROR RESPONSE BODY
         *
         * We create a JSON object with error details
         * This provides useful information to the client
         */
        final Map<String, Object> body = new HashMap<>();

        /*
         * STATUS CODE
         * Include the HTTP status code in response body
         * Redundant but some clients find it useful
         */
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        /*
         * ERROR TYPE
         * Categorizes the error
         * Helps client handle different error types
         */
        body.put("error", "Unauthorized");

        /*
         * ERROR MESSAGE
         * The actual error message from the exception
         * Examples:
         * - "Full authentication is required to access this resource"
         * - "Bad credentials"
         * - "JWT token has expired"
         *
         * authException.getMessage():
         * - Gets the message from the thrown exception
         * - Provides specific reason for failure
         */
        body.put("message", authException.getMessage());

        /*
         * REQUEST PATH
         * The URL that the user tried to access
         * Helpful for debugging
         *
         * request.getServletPath():
         * - Gets the path portion of the URL
         * - Example: "/api/admin/users"
         * - Doesn't include domain or query parameters
         */
        body.put("path", request.getServletPath());

        /*
         * WRITE JSON RESPONSE
         *
         * ObjectMapper: Jackson library class for JSON conversion
         * - Converts Java objects to JSON
         * - Converts JSON to Java objects
         *
         * ObjectMapper().writeValue():
         * - First parameter: Where to write (response output stream)
         * - Second parameter: What to write (our body Map)
         * - Automatically converts Map to JSON
         *
         * response.getOutputStream():
         * - Gets the output stream to write response
         * - Data written here goes back to client
         *
         * Result sent to client:
         * {
         *   "status": 401,
         *   "error": "Unauthorized",
         *   "message": "Full authentication is required to access this resource",
         *   "path": "/api/admin/users"
         * }
         */
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}

/*
 * ============================================
 * EXAMPLE SCENARIOS
 * ============================================
 *
 * Scenario 1: User tries to access protected endpoint without token
 *
 * Request:
 * GET /api/admin/users
 * (No Authorization header)
 *
 * Response:
 * HTTP/1.1 401 Unauthorized
 * Content-Type: application/json
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Full authentication is required to access this resource",
 *   "path": "/api/admin/users"
 * }
 *
 * ---
 *
 * Scenario 2: User provides expired JWT token
 *
 * Request:
 * GET /api/admin/users
 * Authorization: Bearer eyJhbGc... (expired token)
 *
 * Flow:
 * 1. JwtAuthenticationFilter extracts token
 * 2. JwtUtils.validateJwtToken() returns false (expired)
 * 3. Filter doesn't set authentication
 * 4. Request reaches protected endpoint
 * 5. Spring Security detects no authentication
 * 6. Calls AuthEntryPointJwt.commence()
 *
 * Response:
 * HTTP/1.1 401 Unauthorized
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "JWT token has expired",
 *   "path": "/api/admin/users"
 * }
 *
 * ---
 *
 * Scenario 3: User provides invalid JWT token
 *
 * Request:
 * GET /api/admin/users
 * Authorization: Bearer invalid.token.here
 *
 * Same flow as Scenario 2, but different error message
 *
 * Response:
 * HTTP/1.1 401 Unauthorized
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Invalid JWT signature",
 *   "path": "/api/admin/users"
 * }
 *
 * ============================================
 * CLIENT-SIDE HANDLING
 * ============================================
 *
 * When client receives 401 response:
 * 1. Clear stored JWT token (it's invalid)
 * 2. Redirect to login page
 * 3. Show error message to user
 * 4. Prompt for re-authentication
 *
 * Example JavaScript:
 * fetch('/api/admin/users', {
 *   headers: {
 *     'Authorization': 'Bearer ' + token
 *   }
 * })
 * .then(response => {
 *   if (response.status === 401) {
 *     // Token invalid or expired
 *     localStorage.removeItem('token');
 *     window.location.href = '/login';
 *   }
 *   return response.json();
 * })
 * .then(data => {
 *   // Handle successful response
 * });
 */
