package com.security.jwt.payload.response;

import java.util.List;

/**
 * JWT RESPONSE DTO
 *
 * This DTO represents the response after successful authentication
 * Sent back to client after login is successful
 *
 * Response JSON format:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "type": "Bearer",
 *   "id": 1,
 *   "username": "john",
 *   "email": "john@example.com",
 *   "roles": ["ROLE_USER", "ROLE_ADMIN"]
 * }
 *
 * What should the client do with this response?
 * 1. Store the token securely (localStorage, sessionStorage, or memory)
 * 2. Include token in Authorization header for subsequent requests:
 *    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * 3. Display user info (username, email)
 * 4. Show/hide UI elements based on roles
 */
public class JwtResponse {

    /*
     * TOKEN - The JWT access token
     *
     * Structure of a JWT token (3 parts separated by dots):
     * 1. Header: {"alg": "HS256", "typ": "JWT"}
     * 2. Payload: {"sub": "john", "iat": 1234567890, "exp": 1234654290, ...}
     * 3. Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
     *
     * The token contains:
     * - User identification (username)
     * - Issued at time (iat)
     * - Expiration time (exp)
     * - Signature to prevent tampering
     *
     * Token lifecycle:
     * 1. Server generates token after successful login
     * 2. Client stores token
     * 3. Client includes token in every request
     * 4. Server validates token on each request
     * 5. Token expires after configured time
     * 6. User must login again to get new token
     */
    private String token;

    /*
     * TYPE - Token type (always "Bearer")
     *
     * "Bearer" is the authentication scheme
     * - Defined in OAuth 2.0 specification
     * - Means "bearer of this token has access"
     * - No other credentials required beyond the token
     *
     * Full Authorization header format:
     * Authorization: Bearer <token>
     *
     * Other authentication schemes:
     * - Basic: Base64 encoded username:password
     * - Digest: MD5 hash of credentials
     * - Bearer: Token-based (what we use)
     */
    private String type = "Bearer";

    /*
     * USER ID
     * Useful for client-side operations and user identification
     */
    private Long id;

    /*
     * USERNAME
     * Display name for the user in the UI
     */
    private String username;

    /*
     * EMAIL
     * User's email address
     */
    private String email;

    /*
     * ROLES - List of user's roles
     *
     * Why List instead of Set?
     * - JSON arrays serialize from List more predictably
     * - Order might matter in some UIs
     * - Most JSON libraries work better with List
     *
     * Client can use roles to:
     * - Show/hide menu items
     * - Enable/disable features
     * - Redirect to appropriate pages
     *
     * Example UI logic:
     * if (roles.includes("ROLE_ADMIN")) {
     *   showAdminPanel();
     * }
     *
     * IMPORTANT: Never trust client-side role checks for security!
     * - Server must ALWAYS validate permissions
     * - Client-side checks are only for UX
     * - Malicious user can modify client-side code
     */
    private List<String> roles;

    /*
     * CONSTRUCTOR
     *
     * Takes all user information and token to create response
     */
    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    /*
     * GETTERS AND SETTERS
     */

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
