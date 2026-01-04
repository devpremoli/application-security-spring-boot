package com.security.jwt;

import com.security.jwt.models.ERole;
import com.security.jwt.models.Role;
import com.security.jwt.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * SPRING BOOT JWT SECURITY APPLICATION
 *
 * This is the main entry point for the Spring Boot application
 * It starts the application and initializes all components
 *
 * @SpringBootApplication:
 * - Combines three annotations:
 *   1. @Configuration: Marks this as a configuration class
 *   2. @EnableAutoConfiguration: Enables Spring Boot's auto-configuration
 *   3. @ComponentScan: Scans for components in this package and sub-packages
 *
 * What does auto-configuration do?
 * - Automatically configures Spring beans based on classpath
 * - Detects H2 database → Configures DataSource
 * - Detects Spring Security → Configures security
 * - Detects Spring Web → Configures web server
 * - Saves tons of manual configuration!
 *
 * Component scanning:
 * - Finds all @Component, @Service, @Repository, @Controller
 * - Registers them as Spring beans
 * - Makes them available for dependency injection
 *
 * Package structure requirement:
 * - This class must be in root package (com.security.jwt)
 * - All other classes in sub-packages (com.security.jwt.controllers, etc.)
 * - Spring scans this package and all children
 */
@SpringBootApplication
public class SpringBootJwtSecurityApplication {

    /**
     * MAIN METHOD
     *
     * The entry point when you run the application
     * Standard Java main method
     *
     * SpringApplication.run():
     * - Starts Spring Boot application
     * - Creates ApplicationContext (Spring container)
     * - Scans for components
     * - Configures beans
     * - Starts embedded Tomcat server
     * - Application ready to receive requests
     *
     * @param args - Command line arguments (not used in this app)
     */
    public static void main(String[] args) {
        /*
         * START APPLICATION
         *
         * Parameters:
         * 1. SpringBootJwtSecurityApplication.class - The main application class
         * 2. args - Command line arguments to pass through
         *
         * What happens during startup:
         * 1. Creates Spring ApplicationContext
         * 2. Registers configuration classes
         * 3. Scans for components (@Component, @Service, etc.)
         * 4. Processes @Bean methods
         * 5. Injects dependencies
         * 6. Runs CommandLineRunner beans (see below)
         * 7. Starts embedded Tomcat on port 8080
         * 8. Application is ready!
         *
         * Console output shows:
         * - Spring Boot banner
         * - Starting SpringBootJwtSecurityApplication
         * - Database initialization
         * - Server startup on port 8080
         * - "Started SpringBootJwtSecurityApplication in X seconds"
         */
        SpringApplication.run(SpringBootJwtSecurityApplication.class, args);
    }

    /**
     * INITIALIZE ROLES IN DATABASE
     *
     * CommandLineRunner is a Spring Boot interface
     * - Has one method: run(String... args)
     * - Executed after Spring application context is loaded
     * - Perfect for initialization tasks
     *
     * @Bean:
     * - Tells Spring to manage this as a bean
     * - Spring calls this method at startup
     * - Returned CommandLineRunner is executed automatically
     *
     * Why initialize roles here?
     * - Roles are reference data (don't change often)
     * - Need to exist before users can sign up
     * - Simple way to ensure roles exist
     *
     * Alternative approaches:
     * 1. SQL script (data.sql) - Good for production
     * 2. Database migration tool (Flyway/Liquibase) - Best for production
     * 3. This approach - Good for learning/development
     *
     * @param roleRepository - Injected by Spring automatically
     * @return CommandLineRunner that initializes roles
     */
    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository) {
        /*
         * LAMBDA EXPRESSION
         *
         * args -> { ... } is a lambda that implements CommandLineRunner.run()
         * - args: Command line arguments (we don't use them)
         * - { ... }: The code to execute
         *
         * This is equivalent to:
         * return new CommandLineRunner() {
         *     @Override
         *     public void run(String... args) throws Exception {
         *         // initialization code
         *     }
         * };
         */
        return args -> {
            /*
             * CHECK IF ROLES ALREADY EXIST
             *
             * roleRepository.count():
             * - Counts total rows in roles table
             * - Returns 0 if table is empty
             * - Returns > 0 if roles exist
             *
             * Why check before inserting?
             * - Avoid duplicate role entries
             * - Application might restart multiple times
             * - H2 in-memory DB recreates on restart, but file-based DB persists
             * - Safe to run multiple times
             *
             * With ddl-auto=create-drop:
             * - Database is recreated each time
             * - This check is redundant but doesn't hurt
             * - Good practice if you change to ddl-auto=update
             */
            if (roleRepository.count() == 0) {
                /*
                 * CREATE AND SAVE ROLES
                 *
                 * Three standard roles for our application:
                 * 1. ROLE_USER - Default role for registered users
                 * 2. ROLE_MODERATOR - For content moderators
                 * 3. ROLE_ADMIN - For administrators
                 */

                /*
                 * USER ROLE
                 *
                 * new Role(ERole.ROLE_USER):
                 * - Creates new Role entity
                 * - Sets name to ROLE_USER enum
                 *
                 * roleRepository.save():
                 * - Persists to database
                 * - Executes: INSERT INTO roles (name) VALUES ('ROLE_USER')
                 * - Returns saved entity with generated ID
                 *
                 * Resulting database row:
                 * id | name
                 * 1  | ROLE_USER
                 */
                System.out.println("Initializing roles...");
                roleRepository.save(new Role(ERole.ROLE_USER));

                /*
                 * MODERATOR ROLE
                 *
                 * Resulting database row:
                 * id | name
                 * 2  | ROLE_MODERATOR
                 */
                roleRepository.save(new Role(ERole.ROLE_MODERATOR));

                /*
                 * ADMIN ROLE
                 *
                 * Resulting database row:
                 * id | name
                 * 3  | ROLE_ADMIN
                 */
                roleRepository.save(new Role(ERole.ROLE_ADMIN));

                /*
                 * LOG SUCCESS
                 *
                 * Confirmation message in console
                 * Helpful to know initialization completed
                 */
                System.out.println("Roles initialized successfully!");

                /*
                 * Final database state:
                 * roles table:
                 * id | name
                 * ---|----------------
                 * 1  | ROLE_USER
                 * 2  | ROLE_MODERATOR
                 * 3  | ROLE_ADMIN
                 *
                 * Now users can sign up and be assigned these roles!
                 */
            } else {
                /*
                 * ROLES ALREADY EXIST
                 *
                 * This happens if:
                 * - Using persistent database (not in-memory)
                 * - Application restarted
                 * - Database wasn't dropped
                 */
                System.out.println("Roles already exist, skipping initialization.");
            }
        };
    }
}

/*
 * ============================================
 * APPLICATION STARTUP SEQUENCE
 * ============================================
 *
 * When you run this application, here's what happens:
 *
 * 1. JVM STARTS
 * - Java Virtual Machine launches
 * - Executes main() method
 *
 * 2. SPRING BOOT INITIALIZATION
 * - SpringApplication.run() called
 * - Spring Boot banner displayed
 * - Application context created
 *
 * 3. AUTO-CONFIGURATION
 * - Spring Boot detects dependencies:
 *   - spring-boot-starter-web → Configure web server
 *   - spring-boot-starter-data-jpa → Configure JPA
 *   - spring-boot-starter-security → Configure security
 *   - h2 → Configure H2 database
 *
 * 4. COMPONENT SCANNING
 * - Scans com.security.jwt package and sub-packages
 * - Finds all components:
 *   - @Repository: UserRepository, RoleRepository
 *   - @Service: UserDetailsServiceImpl
 *   - @Component: JwtUtils, AuthTokenFilter, AuthEntryPointJwt
 *   - @RestController: AuthController, TestController
 *   - @Configuration: WebSecurityConfig
 *
 * 5. BEAN CREATION
 * - Creates beans in dependency order
 * - Example order:
 *   1. PasswordEncoder (no dependencies)
 *   2. Repositories (need DataSource)
 *   3. UserDetailsService (needs UserRepository)
 *   4. AuthenticationProvider (needs UserDetailsService, PasswordEncoder)
 *   5. Security filters and configurations
 *   6. Controllers
 *
 * 6. DATABASE INITIALIZATION
 * - JPA creates tables from entities:
 *   CREATE TABLE users (...)
 *   CREATE TABLE roles (...)
 *   CREATE TABLE user_roles (...)
 * - SQL logged if spring.jpa.show-sql=true
 *
 * 7. COMMAND LINE RUNNERS
 * - Executes initDatabase() bean
 * - Inserts default roles
 * - Logs: "Roles initialized successfully!"
 *
 * 8. EMBEDDED SERVER START
 * - Tomcat server starts on port 8080
 * - Logs: "Tomcat started on port(s): 8080"
 *
 * 9. APPLICATION READY
 * - Logs: "Started SpringBootJwtSecurityApplication in X seconds"
 * - API endpoints ready to receive requests
 * - H2 console available at http://localhost:8080/h2-console
 *
 * 10. WAITING FOR REQUESTS
 * - Application runs indefinitely
 * - Listens for HTTP requests
 * - Processes authentication and authorization
 *
 * ============================================
 * CONSOLE OUTPUT EXAMPLE
 * ============================================
 *
 *   .   ____          _            __ _ _
 *  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
 * ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 *  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
 *   '  |____| .__|_| |_|_| |_\__, | / / / /
 *  =========|_|==============|___/=/_/_/_/
 *  :: Spring Boot ::                (v3.2.1)
 *
 * 2024-01-15 10:30:45.123  INFO --- [main] c.s.j.SpringBootJwtSecurityApplication   : Starting SpringBootJwtSecurityApplication
 * 2024-01-15 10:30:46.456  INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
 * 2024-01-15 10:30:47.789  INFO --- [main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo
 * 2024-01-15 10:30:48.012  INFO --- [main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.3.1
 * 2024-01-15 10:30:49.345  INFO --- [main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000490: Using JtaPlatform implementation
 * Hibernate: create table roles (...)
 * Hibernate: create table users (...)
 * Hibernate: create table user_roles (...)
 * Initializing roles...
 * Hibernate: insert into roles (name) values (?)
 * Hibernate: insert into roles (name) values (?)
 * Hibernate: insert into roles (name) values (?)
 * Roles initialized successfully!
 * 2024-01-15 10:30:50.678  INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
 * 2024-01-15 10:30:50.789  INFO --- [main] c.s.j.SpringBootJwtSecurityApplication   : Started SpringBootJwtSecurityApplication in 5.666 seconds
 *
 * ============================================
 * HOW TO RUN THE APPLICATION
 * ============================================
 *
 * Method 1: Using Maven
 * mvn spring-boot:run
 *
 * Method 2: Using IDE
 * - Run SpringBootJwtSecurityApplication.java
 * - Right-click → Run 'SpringBootJwtSecurityApplication.main()'
 *
 * Method 3: Using JAR
 * mvn clean package
 * java -jar target/spring-boot-jwt-security-1.0.0.jar
 *
 * ============================================
 * ACCESSING THE APPLICATION
 * ============================================
 *
 * API Base URL: http://localhost:8080
 *
 * Public endpoints:
 * - POST http://localhost:8080/api/auth/signup
 * - POST http://localhost:8080/api/auth/signin
 * - GET  http://localhost:8080/api/test/all
 *
 * Protected endpoints (require JWT):
 * - GET  http://localhost:8080/api/test/user
 * - GET  http://localhost:8080/api/test/mod
 * - GET  http://localhost:8080/api/test/admin
 *
 * H2 Database Console:
 * - URL: http://localhost:8080/h2-console
 * - JDBC URL: jdbc:h2:mem:securitydb
 * - Username: sa
 * - Password: (leave empty)
 *
 * ============================================
 * TROUBLESHOOTING
 * ============================================
 *
 * Error: "Port 8080 already in use"
 * Solution: Change port in application.properties:
 * server.port=8081
 *
 * Error: "Role is not found" during signup
 * Solution: Ensure initDatabase() ran successfully
 * Check console for "Roles initialized successfully!"
 *
 * Error: "Failed to configure a DataSource"
 * Solution: Ensure H2 dependency in pom.xml
 *
 * Error: "Invalid JWT token"
 * Solution: Check jwt.secret in application.properties
 * Must be at least 256 bits (32 bytes) for HS256
 */
