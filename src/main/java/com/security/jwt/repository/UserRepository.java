package com.security.jwt.repository;

import com.security.jwt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * USER REPOSITORY
 *
 * This interface provides database access for User entities
 * It's a Repository in the Repository Pattern
 *
 * REPOSITORY PATTERN:
 * - Abstracts data access logic
 * - Provides collection-like interface for domain objects
 * - Separates business logic from data access
 *
 * Spring Data JPA Magic:
 * - We just define the interface
 * - Spring automatically implements it at runtime!
 * - No need to write SQL or implementation code
 * - Implementation is generated based on method names
 *
 * @Repository:
 * - Marks this as a Spring Data repository
 * - Enables exception translation (SQL exceptions → Spring exceptions)
 * - Makes it a Spring-managed bean
 *
 * JpaRepository<User, Long>:
 * - Generic interface from Spring Data JPA
 * - First parameter: Entity type (User)
 * - Second parameter: ID type (Long)
 * - Provides CRUD operations automatically
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * FIND USER BY USERNAME
     *
     * Query Method - Spring Data magic!
     *
     * How it works:
     * 1. Spring parses the method name: "findBy" + "Username"
     * 2. Generates SQL: SELECT * FROM users WHERE username = ?
     * 3. Maps result to User entity
     * 4. Wraps in Optional to handle not found case
     *
     * Method naming convention:
     * - findBy: Indicates SELECT query
     * - Username: Property name from User entity (must match exactly!)
     * - Spring automatically creates the implementation
     *
     * Return type: Optional<User>
     * - Optional: Java 8 container that may or may not contain a value
     * - Avoids null pointer exceptions
     * - Explicit handling of "not found" case
     *
     * Usage:
     * Optional<User> userOpt = userRepository.findByUsername("john");
     * if (userOpt.isPresent()) {
     *     User user = userOpt.get();
     * } else {
     *     // User not found
     * }
     *
     * Or using lambda:
     * userRepository.findByUsername("john")
     *     .ifPresent(user -> doSomething(user));
     *
     * @param username - The username to search for
     * @return Optional containing User if found, empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * CHECK IF USERNAME EXISTS
     *
     * Query Method for existence check
     *
     * How it works:
     * 1. Spring parses: "existsBy" + "Username"
     * 2. Generates SQL: SELECT COUNT(*) FROM users WHERE username = ?
     * 3. Returns true if count > 0, false otherwise
     *
     * More efficient than findByUsername():
     * - Only counts, doesn't fetch all data
     * - Faster query execution
     * - Less memory usage
     *
     * Usage:
     * if (userRepository.existsByUsername("john")) {
     *     throw new RuntimeException("Username already taken!");
     * }
     *
     * @param username - The username to check
     * @return true if username exists, false otherwise
     */
    Boolean existsByUsername(String username);

    /**
     * CHECK IF EMAIL EXISTS
     *
     * Query Method for email existence check
     *
     * Similar to existsByUsername but checks email field
     *
     * Use case:
     * - During registration, ensure email is unique
     * - Prevent duplicate email addresses
     *
     * Generated SQL: SELECT COUNT(*) FROM users WHERE email = ?
     *
     * @param email - The email to check
     * @return true if email exists, false otherwise
     */
    Boolean existsByEmail(String email);

    /*
     * ============================================
     * INHERITED METHODS FROM JpaRepository
     * ============================================
     *
     * We automatically get these methods without writing code:
     *
     * SAVE/UPDATE:
     * - save(User user): Insert or update user
     * - saveAll(Iterable<User> users): Batch save
     *
     * FIND/READ:
     * - findById(Long id): Find user by ID
     * - findAll(): Get all users
     * - findAllById(Iterable<Long> ids): Find multiple by IDs
     *
     * DELETE:
     * - deleteById(Long id): Delete user by ID
     * - delete(User user): Delete user entity
     * - deleteAll(): Delete all users (DANGEROUS!)
     *
     * COUNT:
     * - count(): Count total users
     *
     * EXISTENCE:
     * - existsById(Long id): Check if user exists by ID
     *
     * PAGINATION:
     * - findAll(Pageable pageable): Paginated results
     * - findAll(Sort sort): Sorted results
     */

    /*
     * ============================================
     * CUSTOM QUERY EXAMPLES (Not implemented, just for learning)
     * ============================================
     *
     * If you need more complex queries, you can use @Query:
     *
     * 1. JPQL (Java Persistence Query Language):
     * @Query("SELECT u FROM User u WHERE u.email = ?1")
     * User findByEmailAddress(String email);
     *
     * 2. Native SQL:
     * @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
     * User findByEmailNative(String email);
     *
     * 3. With multiple parameters:
     * @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.email = ?2")
     * User findByUsernameAndEmail(String username, String email);
     *
     * 4. With named parameters:
     * @Query("SELECT u FROM User u WHERE u.username = :username")
     * User findByUsernameNamed(@Param("username") String username);
     *
     * 5. Update query:
     * @Modifying
     * @Query("UPDATE User u SET u.email = ?1 WHERE u.id = ?2")
     * int updateUserEmail(String email, Long id);
     *
     * But for simple queries, Spring Data's method naming is cleaner!
     */
}
