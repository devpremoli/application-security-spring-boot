package com.security.jwt.security;

import com.security.jwt.security.jwt.AuthEntryPointJwt;
import com.security.jwt.security.jwt.AuthTokenFilter;
import com.security.jwt.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * WEB SECURITY CONFIGURATION
 *
 * This is the CENTRAL configuration class for Spring Security
 * It configures ALL security aspects of the application
 *
 * Key responsibilities:
 * 1. Configure authentication (how users log in)
 * 2. Configure authorization (who can access what)
 * 3. Configure password encoding
 * 4. Configure JWT filter
 * 5. Configure exception handling
 * 6. Configure CORS and CSRF
 *
 * @Configuration:
 * - Marks this as a Spring configuration class
 * - Methods annotated with @Bean create Spring-managed beans
 * - Spring processes this class at startup
 *
 * @EnableWebSecurity:
 * - Enables Spring Security's web security support
 * - Activates @Configuration for Spring Security
 * - Allows customizing WebSecurityConfigurerAdapter
 *
 * @EnableMethodSecurity:
 * - Enables method-level security annotations
 * - Allows using @PreAuthorize, @PostAuthorize, @Secured
 * - Example: @PreAuthorize("hasRole('ADMIN')")
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    /*
     * INJECTED DEPENDENCIES
     *
     * Spring automatically injects these components
     */

    @Autowired
    UserDetailsServiceImpl userDetailsService; // Loads user from database

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler; // Handles auth errors

    /**
     * AUTHENTICATION JWT TOKEN FILTER BEAN
     *
     * Creates and configures the JWT authentication filter
     *
     * @Bean:
     * - Tells Spring to manage this object
     * - Creates a singleton instance
     * - Makes it available for dependency injection
     *
     * This filter will:
     * - Run on every request
     * - Validate JWT tokens
     * - Set authentication in SecurityContext
     *
     * @return AuthTokenFilter instance
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * DAO AUTHENTICATION PROVIDER
     *
     * Configures how Spring Security authenticates users
     *
     * DaoAuthenticationProvider:
     * - "DAO" = Data Access Object
     * - Authenticates users from database
     * - Uses UserDetailsService to load user
     * - Uses PasswordEncoder to verify password
     *
     * Authentication process:
     * 1. User submits username and password
     * 2. Provider calls userDetailsService.loadUserByUsername()
     * 3. Gets UserDetails with hashed password
     * 4. Compares submitted password with hash using passwordEncoder
     * 5. If match, authentication succeeds
     * 6. If no match, throws BadCredentialsException
     *
     * Why use DAO provider?
     * - Works with database storage
     * - Separates authentication logic from data access
     * - Supports password encoding
     * - Standard Spring Security approach
     *
     * @return Configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        /*
         * SET USER DETAILS SERVICE
         *
         * Tells provider how to load user data
         * - userDetailsService will query database
         * - Returns UserDetailsImpl with user info
         */
        authProvider.setUserDetailsService(userDetailsService);

        /*
         * SET PASSWORD ENCODER
         *
         * Tells provider how to verify passwords
         * - Uses BCrypt to compare hashed passwords
         * - BCrypt is one-way encryption
         * - Same algorithm used when creating user
         */
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * AUTHENTICATION MANAGER BEAN
     *
     * The main authentication manager used throughout the application
     *
     * What is AuthenticationManager?
     * - Core Spring Security interface
     * - Has one method: authenticate(Authentication)
     * - Processes authentication requests
     * - Returns authenticated user or throws exception
     *
     * When is it used?
     * - During login in AuthController
     * - authenticationManager.authenticate(credentials)
     * - Validates username and password
     * - Returns Authentication object if successful
     *
     * Why expose as @Bean?
     * - Allows injecting into controllers
     * - AuthController needs it for login
     * - By default, it's not exposed
     *
     * @param authConfig - Spring's authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        /*
         * GET AUTHENTICATION MANAGER
         *
         * authConfig.getAuthenticationManager():
         * - Gets the default authentication manager
         * - Configured with our authenticationProvider()
         * - Ready to authenticate users
         */
        return authConfig.getAuthenticationManager();
    }

    /**
     * PASSWORD ENCODER BEAN
     *
     * Configures how passwords are hashed and verified
     *
     * BCryptPasswordEncoder:
     * - Industry-standard password hashing algorithm
     * - Based on Blowfish cipher
     * - Includes salt automatically (prevents rainbow table attacks)
     * - Adaptive: Can increase cost factor as computers get faster
     *
     * How BCrypt works:
     * 1. Encoding: Takes plain password, generates salt, creates hash
     *    - Input: "myPassword123"
     *    - Output: "$2a$10$abc...xyz" (60 chars)
     *
     * 2. Verification: Takes plain password and hash, checks match
     *    - matches("myPassword123", "$2a$10$abc...xyz")
     *    - Returns true if password matches, false otherwise
     *
     * BCrypt hash format:
     * $2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQR
     * │ │ │  │                                                    │
     * │ │ │  │                                                    └─ Hash (31 chars)
     * │ │ │  └────────────────────────────────────────────────────── Salt (22 chars)
     * │ │ └───────────────────────────────────────────────────────── Cost factor (10 = 2^10 iterations)
     * │ └─────────────────────────────────────────────────────────── BCrypt version
     * └───────────────────────────────────────────────────────────── Algorithm identifier
     *
     * Why BCrypt over others?
     * - MD5/SHA: Too fast, vulnerable to brute force
     * - Plain text: Obviously terrible, never use!
     * - BCrypt: Slow by design, resistant to brute force
     *
     * Cost factor (10):
     * - Higher = slower = more secure
     * - 10 is good balance (2^10 = 1024 iterations)
     * - Can increase to 12-14 for higher security
     * - Don't go too high or login becomes slow
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SECURITY FILTER CHAIN
     *
     * This is the MAIN security configuration!
     * It defines:
     * - Which URLs are public vs protected
     * - What security filters to apply
     * - How to handle errors
     * - Session management
     * - CORS and CSRF settings
     *
     * @param http - HttpSecurity builder for configuration
     * @return SecurityFilterChain - The configured filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /*
         * ============================================
         * CSRF CONFIGURATION
         * ============================================
         *
         * CSRF = Cross-Site Request Forgery
         *
         * What is CSRF?
         * - Attack where malicious site sends request to our API
         * - Uses victim's cookies/session
         * - Example: Evil site submits form to our API
         *
         * Traditional web apps:
         * - Use cookies for authentication
         * - CSRF protection required
         * - Spring generates CSRF token
         * - Must include token in forms
         *
         * JWT-based APIs:
         * - Don't use cookies (token in header)
         * - CSRF attack can't access Authorization header
         * - CSRF protection not needed
         * - We disable it for simplicity
         *
         * IMPORTANT: If you use cookies for JWT storage, ENABLE CSRF!
         */
        http.csrf(csrf -> csrf.disable());

        /*
         * ============================================
         * EXCEPTION HANDLING
         * ============================================
         *
         * Configure how authentication errors are handled
         *
         * exceptionHandling():
         * - Customizes exception handling
         *
         * authenticationEntryPoint(unauthorizedHandler):
         * - Sets our custom entry point
         * - Calls AuthEntryPointJwt when auth fails
         * - Returns JSON error instead of HTML
         *
         * Without this:
         * - Spring returns HTML login page
         * - Not suitable for REST API
         *
         * With this:
         * - Returns JSON error response
         * - Status 401 Unauthorized
         * - Client can parse and handle
         */
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(unauthorizedHandler)
        );

        /*
         * ============================================
         * SESSION MANAGEMENT
         * ============================================
         *
         * Configure session policy for stateless JWT authentication
         *
         * What are sessions?
         * - Server stores user data between requests
         * - Session ID stored in cookie
         * - Server looks up session on each request
         *
         * Stateful (traditional):
         * - User logs in → Server creates session
         * - Session stored in memory or database
         * - Cookie contains session ID
         * - Scalability issues (server must remember all sessions)
         *
         * Stateless (JWT):
         * - User logs in → Server creates JWT token
         * - Token contains all necessary info
         * - No server-side session storage
         * - Scales easily (no session synchronization needed)
         *
         * SessionCreationPolicy.STATELESS:
         * - Never create HTTP session
         * - Never store authentication in session
         * - Every request must have JWT token
         * - Server doesn't remember previous requests
         *
         * Benefits of stateless:
         * - Easy to scale horizontally
         * - No session synchronization needed
         * - Works well with microservices
         * - Simpler deployment
         *
         * Trade-offs:
         * - Can't easily revoke tokens (until expiration)
         * - Token size larger than session ID
         * - Must include token in every request
         */
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        /*
         * ============================================
         * AUTHORIZATION RULES
         * ============================================
         *
         * Define which URLs require authentication
         *
         * This is WHERE you specify:
         * - Public endpoints (no auth needed)
         * - Protected endpoints (auth required)
         * - Role-based access (admin only, etc.)
         */
        http.authorizeHttpRequests(auth -> auth
                /*
                 * PUBLIC ENDPOINTS
                 *
                 * requestMatchers().permitAll():
                 * - These URLs are accessible without authentication
                 * - Anyone can access them
                 * - No JWT token required
                 *
                 * /api/auth/**:
                 * - Signup: POST /api/auth/signup
                 * - Login: POST /api/auth/signin
                 * - Password reset, etc.
                 *
                 * /** wildcard: Matches any path after /api/auth/
                 *
                 * Why public?
                 * - Can't login if login endpoint requires auth!
                 * - New users need signup without account
                 */
                .requestMatchers("/api/auth/**").permitAll()

                /*
                 * H2 CONSOLE (Development only!)
                 *
                 * /h2-console/**:
                 * - H2 database web console
                 * - Useful for viewing database during development
                 *
                 * SECURITY WARNING:
                 * - NEVER enable in production!
                 * - Exposes entire database
                 * - Only for local development
                 *
                 * In production:
                 * - Remove this line
                 * - Or protect with admin role
                 * - Or disable H2 console entirely
                 */
                .requestMatchers("/h2-console/**").permitAll()

                /*
                 * TEST ENDPOINTS (Optional)
                 *
                 * /api/test/**:
                 * - Test endpoints for learning
                 * - Example: /api/test/all (public)
                 * - Example: /api/test/user (requires auth)
                 *
                 * In production:
                 * - Remove test endpoints
                 * - Or protect appropriately
                 */
                .requestMatchers("/api/test/**").permitAll()

                /*
                 * ALL OTHER REQUESTS
                 *
                 * anyRequest().authenticated():
                 * - Any URL not matched above
                 * - Requires authentication
                 * - Must have valid JWT token
                 *
                 * Examples of protected URLs:
                 * - /api/user/profile
                 * - /api/admin/users
                 * - /api/products
                 * - Anything not explicitly made public
                 *
                 * How it works:
                 * 1. Request comes in
                 * 2. AuthTokenFilter validates JWT
                 * 3. Sets authentication in SecurityContext
                 * 4. Spring Security checks authentication
                 * 5. If authenticated, allow request
                 * 6. If not, call AuthEntryPointJwt (401 error)
                 */
                .anyRequest().authenticated()
        );

        /*
         * ============================================
         * H2 CONSOLE CONFIGURATION
         * ============================================
         *
         * H2 console uses frames, need to allow them
         *
         * What are frames?
         * - HTML iframes
         * - H2 console UI uses frames
         *
         * Why disable frameOptions?
         * - Spring Security blocks frames by default (prevents clickjacking)
         * - H2 console won't work without frames
         * - We allow frames only for H2 console
         *
         * Security note:
         * - Clickjacking: Attacker tricks user to click invisible iframe
         * - Not a concern for H2 console in development
         * - Don't expose H2 console in production anyway
         */
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
        );

        /*
         * ============================================
         * AUTHENTICATION PROVIDER
         * ============================================
         *
         * Register our custom authentication provider
         *
         * authenticationProvider():
         * - Our DaoAuthenticationProvider bean
         * - Uses UserDetailsService and PasswordEncoder
         * - Handles username/password authentication
         */
        http.authenticationProvider(authenticationProvider());

        /*
         * ============================================
         * JWT FILTER REGISTRATION
         * ============================================
         *
         * Add JWT filter to the filter chain
         *
         * addFilterBefore():
         * - Adds our filter before another filter
         *
         * authenticationJwtTokenFilter():
         * - Our custom JWT filter
         * - Validates tokens
         * - Sets authentication
         *
         * UsernamePasswordAuthenticationFilter.class:
         * - Standard Spring Security filter
         * - Handles form-based login
         * - We don't use it, but use as reference point
         *
         * Why add before?
         * - JWT filter runs first
         * - Sets authentication if token valid
         * - Then other security filters can check auth
         *
         * Filter chain order:
         * 1. JWT Filter (our custom filter)
         * 2. UsernamePasswordAuthenticationFilter
         * 3. Other security filters
         * 4. Controller
         */
        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);

        /*
         * BUILD AND RETURN
         *
         * http.build():
         * - Builds the SecurityFilterChain
         * - Applies all configurations
         * - Returns configured chain
         */
        return http.build();
    }
}

/*
 * ============================================
 * SECURITY FLOW SUMMARY
 * ============================================
 *
 * LOGIN FLOW:
 * 1. User POSTs credentials to /api/auth/signin
 * 2. Matches /api/auth/** → permitAll() → No authentication needed
 * 3. AuthController receives request
 * 4. Calls authenticationManager.authenticate()
 * 5. AuthenticationManager uses authenticationProvider()
 * 6. DaoAuthenticationProvider calls userDetailsService.loadUserByUsername()
 * 7. Gets user from database
 * 8. Compares password using passwordEncoder
 * 9. If match, returns Authentication object
 * 10. AuthController generates JWT token
 * 11. Returns token to client
 *
 * ACCESSING PROTECTED ENDPOINT:
 * 1. User GETs /api/user/profile with JWT in header
 * 2. AuthTokenFilter intercepts request
 * 3. Extracts and validates JWT token
 * 4. Loads user from database
 * 5. Sets authentication in SecurityContext
 * 6. Continues filter chain
 * 7. Spring Security checks authorization
 * 8. /api/user/profile requires authenticated() → Check passes
 * 9. Request reaches controller
 * 10. Controller processes and returns response
 *
 * ACCESSING WITHOUT TOKEN:
 * 1. User GETs /api/user/profile without JWT
 * 2. AuthTokenFilter intercepts request
 * 3. No token found, doesn't set authentication
 * 4. Continues filter chain
 * 5. Spring Security checks authorization
 * 6. /api/user/profile requires authenticated() → No authentication
 * 7. Calls AuthEntryPointJwt
 * 8. Returns 401 Unauthorized JSON error
 *
 * ============================================
 * ROLE-BASED ACCESS EXAMPLES
 * ============================================
 *
 * You can further customize authorization:
 *
 * 1. Role-based URL access:
 * .requestMatchers("/api/admin/**").hasRole("ADMIN")
 * .requestMatchers("/api/mod/**").hasAnyRole("MODERATOR", "ADMIN")
 *
 * 2. Method-level security (in controllers):
 * @PreAuthorize("hasRole('ADMIN')")
 * public String adminOnly() { ... }
 *
 * @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
 * public String moderatorOrAdmin() { ... }
 *
 * @PreAuthorize("hasRole('USER') and #id == authentication.principal.id")
 * public String ownProfileOnly(@PathVariable Long id) { ... }
 *
 * 3. HTTP method specific:
 * .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
 * .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
 *
 * We'll use method-level security in our test controller!
 */
