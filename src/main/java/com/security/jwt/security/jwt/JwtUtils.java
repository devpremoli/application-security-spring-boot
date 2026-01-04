package com.security.jwt.security.jwt;

import com.security.jwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT UTILITY CLASS
 *
 * This is the core JWT component responsible for:
 * 1. Generating JWT tokens after successful authentication
 * 2. Validating JWT tokens from incoming requests
 * 3. Extracting information (username) from tokens
 *
 * @Component: Makes this a Spring-managed bean
 * - Spring creates single instance (singleton)
 * - Can be injected into other components
 * - Properties are automatically injected from application.properties
 */
@Component
public class JwtUtils {

    /*
     * LOGGER
     *
     * SLF4J (Simple Logging Facade for Java) logger
     * - Used to log important events, errors, and debug information
     * - Logs go to console and/or files based on configuration
     * - Different levels: TRACE, DEBUG, INFO, WARN, ERROR
     *
     * Why logging is important:
     * - Debugging: See what's happening in production
     * - Security auditing: Track authentication attempts
     * - Monitoring: Detect suspicious activities
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /*
     * JWT SECRET KEY
     *
     * @Value: Injects value from application.properties
     * - ${jwt.secret} maps to the property with that name
     * - This is the secret key used to sign and verify tokens
     *
     * CRITICAL SECURITY CONCEPT:
     * - The secret key is like a password for JWT tokens
     * - Anyone with this key can create valid tokens
     * - Anyone with this key can read token contents
     * - MUST be kept secret and secure
     *
     * Production best practices:
     * - NEVER hardcode in source code
     * - NEVER commit to version control
     * - Use environment variables: ${JWT_SECRET}
     * - Or use secret management: AWS Secrets Manager, HashiCorp Vault, etc.
     * - Rotate keys periodically
     * - Use different keys for dev/staging/production
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /*
     * JWT EXPIRATION TIME (in milliseconds)
     *
     * @Value: Injected from application.properties
     * - 86400000 ms = 24 hours
     *
     * Token expiration considerations:
     * - Short expiration (15-60 min): More secure, less convenient
     * - Long expiration (days/weeks): More convenient, less secure
     * - Balance based on your security requirements
     *
     * What happens when token expires?
     * - validateJwtToken() returns false
     * - User must login again to get new token
     * - Or implement refresh token mechanism
     *
     * Refresh Token Pattern (advanced):
     * - Access token: Short-lived (15 min), used for API calls
     * - Refresh token: Long-lived (days), used to get new access token
     * - More secure than long-lived access tokens
     */
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * GENERATE JWT TOKEN
     *
     * Called after successful authentication to create a token
     *
     * Process:
     * 1. Extract username from authenticated user
     * 2. Create JWT with username as subject
     * 3. Set issued time (now)
     * 4. Set expiration time (now + jwtExpirationMs)
     * 5. Sign with secret key using HMAC-SHA256
     *
     * @param authentication - Contains authenticated user details
     * @return JWT token as a String
     */
    public String generateJwtToken(Authentication authentication) {
        /*
         * EXTRACT USER DETAILS
         *
         * authentication.getPrincipal() returns the authenticated user
         * - After successful login, this is a UserDetailsImpl object
         * - We cast it to access our custom fields
         */
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        /*
         * BUILD JWT TOKEN
         *
         * Jwts.builder(): Creates a JWT builder
         *
         * .subject(): The "subject" claim (who the token is about)
         * - We use username as subject
         * - Subject is a standard JWT claim
         * - Retrieved later with getSubject()
         *
         * .issuedAt(): The "iat" (issued at) claim
         * - Timestamp when token was created
         * - new Date() = current time
         * - Useful for token age validation
         *
         * .expiration(): The "exp" (expiration) claim
         * - Timestamp when token expires
         * - After this time, token is invalid
         * - Checked automatically by parser
         *
         * .signWith(): Signs the token with secret key
         * - getSigningKey() creates cryptographic key from secret
         * - Uses HMAC-SHA256 algorithm by default
         * - Signature prevents token tampering
         *
         * .compact(): Serializes to string
         * - Creates the final token string
         * - Format: header.payload.signature
         */
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * GET SIGNING KEY
     *
     * Converts the secret string into a cryptographic key
     *
     * Process:
     * 1. Decode base64 secret string to bytes
     * 2. Create HMAC key from bytes
     *
     * Why use HMAC-SHA256?
     * - Symmetric algorithm: Same key for signing and verification
     * - Fast and efficient
     * - Secure for most applications
     *
     * Alternative algorithms:
     * - RS256: Asymmetric (public/private key pair)
     * - ES256: Elliptic Curve (smaller keys, faster)
     *
     * @return SecretKey for signing/verifying tokens
     */
    private SecretKey getSigningKey() {
        /*
         * Decoders.BASE64.decode(): Decodes base64 string to bytes
         * - Our secret is stored as base64 string
         * - Must convert to bytes for cryptographic operations
         *
         * Keys.hmacShaKeyFor(): Creates HMAC key from bytes
         * - Automatically chooses SHA-256, SHA-384, or SHA-512
         * - Based on key length (256 bits = SHA-256)
         */
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * GET USERNAME FROM JWT TOKEN
     *
     * Extracts the username (subject) from a token
     * Used to identify which user is making the request
     *
     * Process:
     * 1. Parse and verify token signature
     * 2. Extract claims (payload)
     * 3. Return subject claim (username)
     *
     * @param token - The JWT token string
     * @return Username from token's subject claim
     */
    public String getUsernameFromJwtToken(String token) {
        /*
         * Jwts.parser(): Creates a JWT parser
         *
         * .verifyWith(): Sets the key to verify signature
         * - Ensures token was signed with our secret key
         * - Detects if token was tampered with
         *
         * .build(): Builds the parser
         *
         * .parseSignedClaims(): Parses and validates token
         * - Verifies signature
         * - Checks expiration
         * - Throws exception if invalid
         *
         * .getPayload(): Gets the claims (payload)
         *
         * .getSubject(): Gets the subject claim (username)
         */
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * VALIDATE JWT TOKEN
     *
     * Checks if a token is valid and can be trusted
     *
     * Validation checks:
     * 1. Signature is valid (token not tampered)
     * 2. Token not expired
     * 3. Token format is correct
     * 4. Claims are valid
     *
     * @param authToken - The JWT token to validate
     * @return true if valid, false if invalid
     */
    public boolean validateJwtToken(String authToken) {
        try {
            /*
             * PARSE AND VALIDATE TOKEN
             *
             * This single call performs all validations:
             * - Signature verification
             * - Expiration check
             * - Format validation
             *
             * If any validation fails, an exception is thrown
             */
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);

            // If we reach here, token is valid
            return true;

        } catch (MalformedJwtException e) {
            /*
             * MALFORMED JWT
             *
             * Token format is invalid
             * - Not three parts separated by dots
             * - Base64 decoding failed
             * - JSON parsing failed
             *
             * Example: "abc.def" (missing signature part)
             */
            logger.error("Invalid JWT token: {}", e.getMessage());

        } catch (ExpiredJwtException e) {
            /*
             * EXPIRED JWT
             *
             * Token has passed its expiration time
             * - exp claim is before current time
             * - Most common validation failure
             *
             * What to do:
             * - User must login again
             * - Or use refresh token to get new access token
             */
            logger.error("JWT token is expired: {}", e.getMessage());

        } catch (UnsupportedJwtException e) {
            /*
             * UNSUPPORTED JWT
             *
             * Token uses unsupported features
             * - Unsupported algorithm
             * - Unsupported format
             *
             * Example: Token signed with RS256 but we expect HS256
             */
            logger.error("JWT token is unsupported: {}", e.getMessage());

        } catch (IllegalArgumentException e) {
            /*
             * ILLEGAL ARGUMENT
             *
             * Token string is invalid
             * - Empty string
             * - Null value
             * - Invalid characters
             */
            logger.error("JWT claims string is empty: {}", e.getMessage());

        } catch (io.jsonwebtoken.security.SecurityException e) {
            /*
             * SECURITY EXCEPTION
             *
             * Signature validation failed
             * - Token was tampered with
             * - Signed with different secret key
             * - CRITICAL SECURITY ISSUE!
             *
             * This means someone tried to create or modify a token
             * Should be logged and investigated
             */
            logger.error("Invalid JWT signature: {}", e.getMessage());
        }

        // If any exception occurred, token is invalid
        return false;
    }
}
