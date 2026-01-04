package com.security.jwt.controllers;

import com.security.jwt.models.ERole;
import com.security.jwt.models.Role;
import com.security.jwt.models.User;
import com.security.jwt.payload.request.LoginRequest;
import com.security.jwt.payload.request.SignupRequest;
import com.security.jwt.payload.response.JwtResponse;
import com.security.jwt.payload.response.MessageResponse;
import com.security.jwt.repository.RoleRepository;
import com.security.jwt.repository.UserRepository;
import com.security.jwt.security.jwt.JwtUtils;
import com.security.jwt.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AUTHENTICATION CONTROLLER
 *
 * This controller handles user authentication and registration
 * Provides endpoints for:
 * - User signup (registration)
 * - User signin (login)
 *
 * @RestController:
 * - Combines @Controller and @ResponseBody
 * - All methods return data (not views)
 * - Responses automatically converted to JSON
 *
 * @RequestMapping("/api/auth"):
 * - Base URL for all endpoints in this controller
 * - All methods start with /api/auth
 * - Example: /api/auth/signin, /api/auth/signup
 *
 * @CrossOrigin:
 * - Allows requests from different domains (CORS)
 * - origins = "*": Allow from any domain
 * - maxAge: Cache preflight request for 3600 seconds
 *
 * CORS (Cross-Origin Resource Sharing):
 * - Browser security feature
 * - Blocks requests from different domains by default
 * - Example: Frontend on localhost:3000, API on localhost:8080
 * - Without @CrossOrigin, browser blocks the request
 *
 * In production:
 * - Don't use "*", specify exact frontend domain
 * - Example: origins = "https://yourfrontend.com"
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /*
     * INJECTED DEPENDENCIES
     *
     * @Autowired: Spring automatically provides these instances
     */

    @Autowired
    AuthenticationManager authenticationManager; // For authenticating users

    @Autowired
    UserRepository userRepository; // For database operations on users

    @Autowired
    RoleRepository roleRepository; // For database operations on roles

    @Autowired
    PasswordEncoder encoder; // For hashing passwords (BCrypt)

    @Autowired
    JwtUtils jwtUtils; // For generating JWT tokens

    /**
     * SIGNIN ENDPOINT - User Login
     *
     * Authenticates user and returns JWT token
     *
     * Endpoint: POST /api/auth/signin
     *
     * Request Body:
     * {
     *   "username": "john",
     *   "password": "password123"
     * }
     *
     * Success Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "id": 1,
     *   "username": "john",
     *   "email": "john@example.com",
     *   "roles": ["ROLE_USER", "ROLE_ADMIN"]
     * }
     *
     * Error Response (401 Unauthorized):
     * - Wrong username or password
     *
     * @PostMapping:
     * - Maps HTTP POST requests
     * - Path: /api/auth/signin
     *
     * @param loginRequest - The login credentials from request body
     * @return ResponseEntity with JwtResponse or error
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        /*
         * ============================================
         * STEP 1: AUTHENTICATE USER
         * ============================================
         *
         * authenticationManager.authenticate():
         * - Main authentication method
         * - Validates username and password
         * - Returns Authentication object if successful
         * - Throws exception if authentication fails
         *
         * UsernamePasswordAuthenticationToken:
         * - Represents username/password authentication
         * - Constructor: (username, password)
         * - Before authentication: Contains credentials
         * - After authentication: Contains principal + authorities
         *
         * What happens inside authenticate()?
         * 1. AuthenticationManager delegates to DaoAuthenticationProvider
         * 2. Provider calls UserDetailsService.loadUserByUsername()
         * 3. Gets UserDetails from database
         * 4. Compares passwords using PasswordEncoder.matches()
         * 5. If match: Creates authenticated Authentication object
         * 6. If no match: Throws BadCredentialsException
         *
         * Exceptions that can be thrown:
         * - BadCredentialsException: Wrong password
         * - UsernameNotFoundException: User doesn't exist
         * - DisabledException: Account disabled
         * - LockedException: Account locked
         * - AccountExpiredException: Account expired
         *
         * Spring automatically converts these to 401 Unauthorized
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        /*
         * ============================================
         * STEP 2: SET AUTHENTICATION IN CONTEXT
         * ============================================
         *
         * SecurityContextHolder.getContext().setAuthentication():
         * - Stores authentication in SecurityContext
         * - Makes user "logged in" for this request
         * - Accessible anywhere in the application
         *
         * Why set in context?
         * - Not strictly necessary for JWT
         * - Useful if we need to access user in this request
         * - Good practice, maintains consistency
         *
         * Note: This is request-scoped, cleared after response
         * For subsequent requests, AuthTokenFilter sets authentication
         */
        SecurityContextHolder.getContext().setAuthentication(authentication);

        /*
         * ============================================
         * STEP 3: GENERATE JWT TOKEN
         * ============================================
         *
         * jwtUtils.generateJwtToken():
         * - Creates JWT token from authentication
         * - Extracts username from authentication.getPrincipal()
         * - Sets expiration time
         * - Signs with secret key
         * - Returns token string
         *
         * Token structure:
         * Header: {"alg": "HS256", "typ": "JWT"}
         * Payload: {"sub": "john", "iat": 1234567890, "exp": 1234654290}
         * Signature: HMACSHA256(header + payload, secret)
         *
         * Result: "eyJhbGc...xyz" (base64 encoded)
         */
        String jwt = jwtUtils.generateJwtToken(authentication);

        /*
         * ============================================
         * STEP 4: EXTRACT USER DETAILS
         * ============================================
         *
         * authentication.getPrincipal():
         * - Returns the authenticated user
         * - Type is Object, need to cast
         * - After successful authentication, it's UserDetailsImpl
         *
         * (UserDetailsImpl) cast:
         * - Convert Object to UserDetailsImpl
         * - Safe because we know authentication succeeded
         * - UserDetailsImpl was created by our UserDetailsService
         */
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        /*
         * ============================================
         * STEP 5: EXTRACT ROLES
         * ============================================
         *
         * userDetails.getAuthorities():
         * - Returns Collection<? extends GrantedAuthority>
         * - Each GrantedAuthority represents a role
         * - Example: [ROLE_USER, ROLE_ADMIN]
         *
         * Stream transformation:
         * 1. .stream(): Convert collection to stream
         * 2. .map(GrantedAuthority::getAuthority): Extract role string
         * 3. .collect(Collectors.toList()): Collect to List
         *
         * Result: List<String> with role names
         * - ["ROLE_USER", "ROLE_ADMIN"]
         *
         * Why extract as strings?
         * - JwtResponse expects List<String>
         * - Easier to serialize to JSON
         * - Client can use role strings directly
         */
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        /*
         * ============================================
         * STEP 6: BUILD AND RETURN RESPONSE
         * ============================================
         *
         * ResponseEntity.ok():
         * - HTTP 200 OK status
         * - Indicates successful authentication
         *
         * new JwtResponse():
         * - Create response DTO with all user info
         * - Includes JWT token
         * - Includes user details (id, username, email)
         * - Includes roles
         *
         * Spring automatically converts to JSON:
         * {
         *   "token": "eyJhbGc...",
         *   "type": "Bearer",
         *   "id": 1,
         *   "username": "john",
         *   "email": "john@example.com",
         *   "roles": ["ROLE_USER"]
         * }
         *
         * Client should:
         * 1. Store token securely
         * 2. Include in future requests: Authorization: Bearer <token>
         * 3. Use user info to personalize UI
         * 4. Use roles to show/hide features
         */
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles
        ));
    }

    /**
     * SIGNUP ENDPOINT - User Registration
     *
     * Creates a new user account
     *
     * Endpoint: POST /api/auth/signup
     *
     * Request Body:
     * {
     *   "username": "john",
     *   "email": "john@example.com",
     *   "password": "password123",
     *   "roles": ["admin", "user"]  // Optional, defaults to ["user"]
     * }
     *
     * Success Response (200 OK):
     * {
     *   "message": "User registered successfully!"
     * }
     *
     * Error Response (400 Bad Request):
     * {
     *   "message": "Error: Username is already taken!"
     * }
     * OR
     * {
     *   "message": "Error: Email is already in use!"
     * }
     *
     * @param signUpRequest - The registration data from request body
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        /*
         * ============================================
         * STEP 1: VALIDATE USERNAME UNIQUENESS
         * ============================================
         *
         * userRepository.existsByUsername():
         * - Checks if username already exists in database
         * - Returns true if found, false if not found
         * - Uses database query: SELECT COUNT(*) WHERE username = ?
         *
         * Why check this?
         * - Username must be unique (database constraint)
         * - Better to return friendly error than database exception
         * - User gets immediate feedback
         *
         * If username exists:
         * - Return 400 Bad Request
         * - Include error message
         * - User can try different username
         */
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        /*
         * ============================================
         * STEP 2: VALIDATE EMAIL UNIQUENESS
         * ============================================
         *
         * Similar to username check, but for email
         *
         * Why check email?
         * - Email should be unique (one account per email)
         * - Prevents duplicate accounts
         * - Needed for password reset functionality
         *
         * Database constraint:
         * - @UniqueConstraint in User entity
         * - Database enforces uniqueness
         * - But better to check before insert attempt
         */
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        /*
         * ============================================
         * STEP 3: CREATE NEW USER ENTITY
         * ============================================
         *
         * new User():
         * - Create new user entity
         * - Uses parameterized constructor
         *
         * encoder.encode():
         * - Hash the password using BCrypt
         * - NEVER store plain text passwords!
         *
         * Password hashing:
         * Input: "password123"
         * Output: "$2a$10$abc...xyz" (60 characters)
         *
         * BCrypt properties:
         * - One-way: Can't decrypt hash to get password
         * - Salted: Each hash is unique even for same password
         * - Slow: Resistant to brute force attacks
         *
         * Example:
         * Same password "password123" creates different hashes:
         * - User 1: "$2a$10$abc...xyz"
         * - User 2: "$2a$10$def...uvw"
         *
         * This is because BCrypt generates random salt for each encoding
         */
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword())
        );

        /*
         * ============================================
         * STEP 4: ASSIGN ROLES TO USER
         * ============================================
         *
         * Get roles from request or use default
         */
        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        /*
         * DETERMINE WHICH ROLES TO ASSIGN
         *
         * If roles not specified in request:
         * - Assign default ROLE_USER
         * - Every new user gets USER role
         *
         * If roles specified:
         * - Assign requested roles
         * - Usually only admin can assign roles
         * - In production, add role assignment authorization!
         */
        if (strRoles == null) {
            /*
             * DEFAULT ROLE ASSIGNMENT
             *
             * roleRepository.findByName(ERole.ROLE_USER):
             * - Query database for USER role
             * - Returns Optional<Role>
             *
             * .orElseThrow():
             * - If role found: Return the Role
             * - If not found: Throw RuntimeException
             *
             * Why might role not be found?
             * - Database not initialized with roles
             * - Need to run initialization code
             * - See main application class for role initialization
             *
             * roles.add():
             * - Add role to user's role collection
             * - Creates entry in user_roles join table
             */
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            /*
             * CUSTOM ROLE ASSIGNMENT
             *
             * Loop through requested roles
             * For each role string, find and assign corresponding Role entity
             *
             * SECURITY WARNING:
             * In production, regular users shouldn't be able to assign roles!
             * Only admins should assign roles
             * Add authorization check here:
             * if (!currentUser.hasRole("ADMIN")) {
             *     throw new ForbiddenException("Only admins can assign roles");
             * }
             */
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        /*
                         * ADMIN ROLE
                         *
                         * Find ROLE_ADMIN in database
                         * Add to user's roles
                         *
                         * Admin capabilities (defined in controllers):
                         * - Manage all users
                         * - Access admin endpoints
                         * - View system statistics
                         * - etc.
                         */
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;

                    case "mod":
                        /*
                         * MODERATOR ROLE
                         *
                         * Moderator capabilities:
                         * - Moderate content
                         * - Manage user posts
                         * - Handle reports
                         * - Less access than admin
                         */
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;

                    default:
                        /*
                         * DEFAULT TO USER ROLE
                         *
                         * If role string doesn't match "admin" or "mod"
                         * Or if user explicitly requests "user" role
                         * Assign ROLE_USER
                         */
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        /*
         * ============================================
         * STEP 5: SET ROLES AND SAVE USER
         * ============================================
         *
         * user.setRoles(roles):
         * - Assign collected roles to user
         * - Sets the many-to-many relationship
         *
         * userRepository.save(user):
         * - Persist user to database
         * - Executes INSERT statement
         * - Also creates entries in user_roles join table
         * - ID is auto-generated
         *
         * SQL executed:
         * INSERT INTO users (username, email, password) VALUES (?, ?, ?);
         * INSERT INTO user_roles (user_id, role_id) VALUES (?, ?);
         * (One INSERT into user_roles for each role)
         *
         * Transaction handling:
         * - Spring automatically wraps in transaction
         * - If any error occurs, entire operation rolls back
         * - Database remains consistent
         */
        user.setRoles(roles);
        userRepository.save(user);

        /*
         * ============================================
         * STEP 6: RETURN SUCCESS RESPONSE
         * ============================================
         *
         * ResponseEntity.ok():
         * - HTTP 200 OK status
         * - Indicates successful registration
         *
         * MessageResponse:
         * - Simple DTO with success message
         * - Converted to JSON: {"message": "User registered successfully!"}
         *
         * User can now:
         * 1. Login with credentials
         * 2. Get JWT token
         * 3. Access protected endpoints
         */
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}

/*
 * ============================================
 * USAGE EXAMPLES
 * ============================================
 *
 * EXAMPLE 1: User Registration (Default Role)
 *
 * Request:
 * POST http://localhost:8080/api/auth/signup
 * Content-Type: application/json
 *
 * {
 *   "username": "john",
 *   "email": "john@example.com",
 *   "password": "password123"
 * }
 *
 * Response:
 * HTTP/1.1 200 OK
 * {
 *   "message": "User registered successfully!"
 * }
 *
 * Database state:
 * users table: id=1, username=john, email=john@example.com, password=$2a$10$...
 * user_roles table: user_id=1, role_id=1 (ROLE_USER)
 *
 * ---
 *
 * EXAMPLE 2: User Login
 *
 * Request:
 * POST http://localhost:8080/api/auth/signin
 * Content-Type: application/json
 *
 * {
 *   "username": "john",
 *   "password": "password123"
 * }
 *
 * Response:
 * HTTP/1.1 200 OK
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huIiwiaWF0IjoxNjM5NTg1MjAwLCJleHAiOjE2Mzk2NzE2MDB9.xyz",
 *   "type": "Bearer",
 *   "id": 1,
 *   "username": "john",
 *   "email": "john@example.com",
 *   "roles": ["ROLE_USER"]
 * }
 *
 * ---
 *
 * EXAMPLE 3: Registration with Admin Role
 *
 * Request:
 * POST http://localhost:8080/api/auth/signup
 * Content-Type: application/json
 *
 * {
 *   "username": "admin",
 *   "email": "admin@example.com",
 *   "password": "admin123",
 *   "roles": ["admin", "user"]
 * }
 *
 * Response:
 * HTTP/1.1 200 OK
 * {
 *   "message": "User registered successfully!"
 * }
 *
 * Database state:
 * users table: id=2, username=admin, email=admin@example.com, password=$2a$10$...
 * user_roles table:
 *   user_id=2, role_id=1 (ROLE_USER)
 *   user_id=2, role_id=3 (ROLE_ADMIN)
 *
 * ---
 *
 * EXAMPLE 4: Duplicate Username Error
 *
 * Request:
 * POST http://localhost:8080/api/auth/signup
 * {
 *   "username": "john",  // Already exists
 *   "email": "different@example.com",
 *   "password": "password123"
 * }
 *
 * Response:
 * HTTP/1.1 400 Bad Request
 * {
 *   "message": "Error: Username is already taken!"
 * }
 *
 * ---
 *
 * EXAMPLE 5: Invalid Login Credentials
 *
 * Request:
 * POST http://localhost:8080/api/auth/signin
 * {
 *   "username": "john",
 *   "password": "wrongpassword"
 * }
 *
 * Response:
 * HTTP/1.1 401 Unauthorized
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Bad credentials",
 *   "path": "/api/auth/signin"
 * }
 *
 * (Error response comes from AuthEntryPointJwt)
 */
