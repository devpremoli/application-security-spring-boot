package com.security.jwt.security.services;

import com.security.jwt.models.User;
import com.security.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * USER DETAILS SERVICE IMPLEMENTATION
 *
 * This is a critical component in Spring Security's authentication process
 * It loads user data from the database during authentication
 *
 * What is UserDetailsService?
 * - Core Spring Security interface
 * - Has one method: loadUserByUsername()
 * - Called during authentication to fetch user from database
 *
 * Authentication Flow:
 * 1. User submits username and password
 * 2. Spring Security calls loadUserByUsername()
 * 3. This method queries database for user
 * 4. Converts User entity to UserDetails
 * 5. Spring Security compares passwords
 * 6. If match, authentication succeeds
 *
 * @Service:
 * - Marks this as a Spring service component
 * - Makes it a Spring-managed bean
 * - Can be injected into other components
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /*
     * USER REPOSITORY
     *
     * @Autowired: Dependency Injection
     * - Spring automatically provides UserRepository instance
     * - No need for manual instantiation
     * - Spring manages the lifecycle
     *
     * Constructor injection (alternative):
     * private final UserRepository userRepository;
     * public UserDetailsServiceImpl(UserRepository userRepository) {
     *     this.userRepository = userRepository;
     * }
     *
     * Constructor injection is preferred over @Autowired:
     * - Makes dependencies explicit
     * - Allows final fields (immutability)
     * - Easier to test
     * - But @Autowired is simpler and widely used
     */
    @Autowired
    UserRepository userRepository;

    /**
     * LOAD USER BY USERNAME
     *
     * This method is called by Spring Security during authentication
     *
     * When is this called?
     * 1. User login: When authenticating username/password
     * 2. JWT validation: When loading user from token
     * 3. Session restoration: When loading user from session
     *
     * @Transactional:
     * - Ensures database operations are in a transaction
     * - If exception occurs, transaction rolls back
     * - Required for lazy loading of relationships (roles)
     *
     * Why @Transactional?
     * - User entity has lazy-loaded roles (FetchType.LAZY)
     * - Without transaction, accessing roles throws LazyInitializationException
     * - Transaction keeps database connection open until method completes
     *
     * Process:
     * 1. Query database for user by username
     * 2. If not found, throw UsernameNotFoundException
     * 3. If found, convert User to UserDetailsImpl
     * 4. Return UserDetails to Spring Security
     * 5. Spring Security uses it to verify credentials
     *
     * @param username - The username to look up
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /*
         * QUERY DATABASE FOR USER
         *
         * userRepository.findByUsername(username):
         * - Executes: SELECT * FROM users WHERE username = ?
         * - Returns Optional<User>
         *
         * .orElseThrow():
         * - If user found: Returns the User
         * - If not found: Throws the exception
         *
         * This is a clean way to handle Optional without if/else
         *
         * Alternative verbose approach:
         * Optional<User> userOpt = userRepository.findByUsername(username);
         * if (userOpt.isPresent()) {
         *     User user = userOpt.get();
         * } else {
         *     throw new UsernameNotFoundException("User Not Found with username: " + username);
         * }
         */
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        /*
         * CONVERT TO USERDETAILS
         *
         * UserDetailsImpl.build(user):
         * - Static factory method
         * - Converts our User entity to UserDetailsImpl
         * - Extracts and converts roles to GrantedAuthorities
         * - Returns object that Spring Security can use
         *
         * Why not return User directly?
         * - User entity doesn't implement UserDetails interface
         * - Spring Security requires UserDetails
         * - UserDetailsImpl is the adapter
         *
         * What Spring Security does with this:
         * 1. Gets password: userDetails.getPassword()
         * 2. Compares with submitted password using BCrypt
         * 3. If match, creates Authentication object
         * 4. Sets Authentication in SecurityContext
         * 5. User is now authenticated!
         */
        return UserDetailsImpl.build(user);
    }
}

/*
 * ============================================
 * AUTHENTICATION FLOW IN DETAIL
 * ============================================
 *
 * Let's trace what happens when user logs in with username "john" and password "password123"
 *
 * STEP 1: Client Request
 * POST /api/auth/signin
 * {
 *   "username": "john",
 *   "password": "password123"
 * }
 *
 * STEP 2: AuthController receives request
 * - Validates LoginRequest DTO
 * - Creates UsernamePasswordAuthenticationToken
 * - Calls AuthenticationManager.authenticate()
 *
 * STEP 3: AuthenticationManager processes
 * - Delegates to AuthenticationProvider (DaoAuthenticationProvider)
 * - Provider calls UserDetailsService.loadUserByUsername("john")
 *
 * STEP 4: This class (UserDetailsServiceImpl)
 * - Queries database: SELECT * FROM users WHERE username = 'john'
 * - Finds user with hashed password: "$2a$10$abc...xyz"
 * - Converts to UserDetailsImpl
 * - Returns UserDetailsImpl to Provider
 *
 * STEP 5: DaoAuthenticationProvider validates
 * - Gets hashed password from UserDetails: "$2a$10$abc...xyz"
 * - Compares with submitted password using BCrypt
 * - BCryptPasswordEncoder.matches("password123", "$2a$10$abc...xyz")
 * - If true: Authentication successful!
 * - If false: Throws BadCredentialsException
 *
 * STEP 6: AuthController handles result
 * - If successful: Generate JWT token
 * - Extract user info (id, username, email, roles)
 * - Return JwtResponse with token and user info
 *
 * STEP 7: Client stores token
 * - Save token in localStorage or memory
 * - Include in future requests: Authorization: Bearer <token>
 *
 * STEP 8: Subsequent requests with JWT
 * - Client sends: Authorization: Bearer eyJhbG...
 * - JwtAuthenticationFilter validates token
 * - Extracts username from token
 * - Calls loadUserByUsername() again to get fresh user data
 * - Sets authentication in SecurityContext
 * - Request proceeds to controller
 *
 * ============================================
 * ERROR SCENARIOS
 * ============================================
 *
 * Scenario 1: User not found
 * - Database query returns empty
 * - orElseThrow() throws UsernameNotFoundException
 * - Spring Security catches it
 * - Returns 401 Unauthorized to client
 *
 * Scenario 2: Wrong password
 * - User found and returned
 * - BCrypt comparison fails
 * - DaoAuthenticationProvider throws BadCredentialsException
 * - Returns 401 Unauthorized to client
 *
 * Scenario 3: Account locked/disabled
 * - User found and returned
 * - UserDetailsImpl.isEnabled() returns false
 * - Spring Security throws DisabledException
 * - Returns 401 Unauthorized to client
 *
 * Scenario 4: Database error
 * - Database connection fails
 * - Repository throws exception
 * - Spring translates to DataAccessException
 * - Returns 500 Internal Server Error to client
 */
