package com.security.jwt.repository;

import com.security.jwt.models.ERole;
import com.security.jwt.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ROLE REPOSITORY
 *
 * Repository interface for accessing Role entities from database
 * Similar to UserRepository but for roles
 *
 * Spring Data JPA automatically implements this interface
 * No need to write implementation code
 *
 * @Repository: Marks this as a Spring Data repository
 * JpaRepository<Role, Integer>:
 * - Entity type: Role
 * - ID type: Integer
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * FIND ROLE BY NAME
     *
     * Query Method - Spring Data automatically implements this
     *
     * How it works:
     * - Spring parses method name: "findBy" + "Name"
     * - Generates SQL: SELECT * FROM roles WHERE name = ?
     * - Maps result to Role entity
     * - Returns Optional<Role>
     *
     * Return type: Optional<Role>
     * - Optional because role might not exist in database
     * - Better than returning null
     * - Forces explicit handling of "not found" case
     *
     * Usage in signup process:
     * 1. User signs up with roles: ["admin", "user"]
     * 2. For each role string, we need to find Role entity
     * 3. roleRepository.findByName(ERole.ROLE_ADMIN)
     * 4. Get Role entity from database
     * 5. Add to user's roles collection
     *
     * Example:
     * Optional<Role> roleOpt = roleRepository.findByName(ERole.ROLE_USER);
     * if (roleOpt.isPresent()) {
     *     Role role = roleOpt.get();
     *     user.getRoles().add(role);
     * } else {
     *     throw new RuntimeException("Role not found");
     * }
     *
     * Or using orElseThrow:
     * Role role = roleRepository.findByName(ERole.ROLE_USER)
     *     .orElseThrow(() -> new RuntimeException("Role not found"));
     *
     * @param name - The role name enum (ROLE_USER, ROLE_ADMIN, etc.)
     * @return Optional containing Role if found, empty if not found
     */
    Optional<Role> findByName(ERole name);

    /*
     * ============================================
     * INHERITED METHODS
     * ============================================
     *
     * From JpaRepository, we automatically get:
     *
     * - save(Role role): Insert or update role
     * - findById(Integer id): Find role by ID
     * - findAll(): Get all roles
     * - deleteById(Integer id): Delete role by ID
     * - count(): Count total roles
     * - existsById(Integer id): Check if role exists
     *
     * These are used to initialize roles in database
     * See CommandLineRunner in main application class
     */

    /*
     * ============================================
     * DATABASE INITIALIZATION
     * ============================================
     *
     * Roles are typically pre-populated in database
     * We need to insert them when application starts
     *
     * Two approaches:
     *
     * 1. Using CommandLineRunner (recommended for learning):
     * @Bean
     * CommandLineRunner initRoles(RoleRepository roleRepository) {
     *     return args -> {
     *         if (roleRepository.count() == 0) {
     *             roleRepository.save(new Role(ERole.ROLE_USER));
     *             roleRepository.save(new Role(ERole.ROLE_MODERATOR));
     *             roleRepository.save(new Role(ERole.ROLE_ADMIN));
     *         }
     *     };
     * }
     *
     * 2. Using data.sql file (Spring Boot feature):
     * - Create src/main/resources/data.sql
     * - Add SQL statements:
     *   INSERT INTO roles(name) VALUES('ROLE_USER');
     *   INSERT INTO roles(name) VALUES('ROLE_MODERATOR');
     *   INSERT INTO roles(name) VALUES('ROLE_ADMIN');
     *
     * We'll use approach 1 in our main application class
     */
}
