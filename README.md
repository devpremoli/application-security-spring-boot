# Spring Boot JWT Security - Complete Learning Guide

A comprehensive Spring Boot application demonstrating **JWT (JSON Web Token) authentication** with **Spring Security**. This project is designed as a learning resource with extensive documentation and comments explaining every concept in detail.

## Table of Contents

- [Overview](#overview)
- [What You'll Learn](#what-youll-learn)
- [Technologies Used](#technologies-used)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [How JWT Authentication Works](#how-jwt-authentication-works)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Testing the Application](#testing-the-application)
- [Key Concepts Explained](#key-concepts-explained)
- [Security Best Practices](#security-best-practices)
- [Common Issues](#common-issues)

## Overview

This application implements a complete authentication and authorization system using:
- **JWT tokens** for stateless authentication
- **BCrypt** for password hashing
- **Role-based access control** (RBAC)
- **Spring Security** for protection

## What You'll Learn

After studying this project, you'll understand:

1. **JWT Fundamentals**
   - What JWT is and how it works
   - Token structure (header, payload, signature)
   - Token generation and validation
   - Token expiration and security

2. **Spring Security**
   - Authentication vs Authorization
   - Security filter chain
   - UserDetails and UserDetailsService
   - Password encoding with BCrypt
   - Method-level security with @PreAuthorize

3. **API Security**
   - Protecting REST endpoints
   - Role-based access control
   - Handling authentication errors
   - CORS configuration

4. **Best Practices**
   - Stateless authentication
   - Password hashing
   - Secret key management
   - Error handling

## Technologies Used

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming language |
| Spring Boot | 3.2.1 | Application framework |
| Spring Security | 6.x | Security framework |
| Spring Data JPA | 3.2.x | Database access |
| JWT (jjwt) | 0.12.3 | JWT token handling |
| H2 Database | 2.x | In-memory database |
| Maven | 3.x | Build tool |
| Lombok | 1.x | Reduce boilerplate code |

## Architecture

### Authentication Flow

```
1. User Registration (Signup)
   Client -> POST /api/auth/signup -> Server
   Server validates -> Hash password -> Save user -> Return success

2. User Login (Signin)
   Client -> POST /api/auth/signin -> Server
   Server validates credentials -> Generate JWT -> Return token + user info

3. Accessing Protected Resource
   Client -> GET /api/test/user + JWT Token -> Server
   Server validates JWT -> Extract user -> Check authorization -> Return resource
```

## Project Structure

```
src/main/java/com/security/jwt/
├── SpringBootJwtSecurityApplication.java    # Main application class
├── controllers/
│   ├── AuthController.java                  # Signup & Signin endpoints
│   └── TestController.java                  # Test endpoints (public, user, mod, admin)
├── models/
│   ├── User.java                            # User entity
│   ├── Role.java                            # Role entity
│   └── ERole.java                           # Role enum
├── payload/
│   ├── request/
│   │   ├── LoginRequest.java                # Login DTO
│   │   └── SignupRequest.java               # Signup DTO
│   └── response/
│       ├── JwtResponse.java                 # JWT response DTO
│       └── MessageResponse.java             # Simple message DTO
├── repository/
│   ├── UserRepository.java                  # User database access
│   └── RoleRepository.java                  # Role database access
└── security/
    ├── WebSecurityConfig.java               # Security configuration
    ├── jwt/
    │   ├── AuthEntryPointJwt.java          # Authentication error handler
    │   ├── AuthTokenFilter.java            # JWT validation filter
    │   └── JwtUtils.java                   # JWT utility methods
    └── services/
        ├── UserDetailsImpl.java            # UserDetails implementation
        └── UserDetailsServiceImpl.java     # Load user for authentication

src/main/resources/
└── application.properties                   # Application configuration
```

## How JWT Authentication Works

### What is JWT?

JWT (JSON Web Token) is a compact, URL-safe token format for securely transmitting information between parties. A JWT consists of three parts separated by dots:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
│                                     │                                    │
│          HEADER                     │         PAYLOAD                    │      SIGNATURE
```

**Header**: Algorithm and token type
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload**: User data and claims
```json
{
  "sub": "john",
  "iat": 1516239022,
  "exp": 1516325422
}
```

**Signature**: Verification signature
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd application-security-spring-boot
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify it's running**
   ```
   Application starts on http://localhost:8080
   You should see "Started SpringBootJwtSecurityApplication" in console
   ```

## API Endpoints

### Authentication Endpoints (Public)

#### 1. Register New User
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "message": "User registered successfully!"
}
```

#### 2. Login User
```http
POST /api/auth/signin
Content-Type: application/json

{
  "username": "john",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

### Test Endpoints

#### 1. Public Content (No Authentication)
```http
GET /api/test/all
```

**Response:** `"Public Content."`

#### 2. User Content (Requires ROLE_USER)
```http
GET /api/test/user
Authorization: Bearer <your-jwt-token>
```

**Response:** `"User Content."`

#### 3. Moderator Content (Requires ROLE_MODERATOR)
```http
GET /api/test/mod
Authorization: Bearer <your-jwt-token>
```

**Response:** `"Moderator Board."`

#### 4. Admin Content (Requires ROLE_ADMIN)
```http
GET /api/test/admin
Authorization: Bearer <your-jwt-token>
```

**Response:** `"Admin Board."`

## Testing the Application

### Using cURL

#### 1. Register a regular user
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"password123"}'
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123"}'
```

Save the token from the response!

#### 3. Access protected endpoint
```bash
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  http://localhost:8080/api/test/user
```

#### 4. Register admin user
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","password":"admin123","roles":["admin","user"]}'
```

### Using H2 Console

1. **Access H2 Console:**
   ```
   http://localhost:8080/h2-console
   ```

2. **Connection Settings:**
   - JDBC URL: `jdbc:h2:mem:securitydb`
   - Username: `sa`
   - Password: (leave empty)

3. **Useful Queries:**
   ```sql
   -- View all users
   SELECT * FROM users;

   -- View all roles
   SELECT * FROM roles;

   -- View user-role relationships
   SELECT u.username, r.name
   FROM users u
   JOIN user_roles ur ON u.id = ur.user_id
   JOIN roles r ON ur.role_id = r.id;
   ```

## Key Concepts Explained

### 1. Authentication vs Authorization

- **Authentication**: Who are you? (Login with username/password)
- **Authorization**: What can you do? (Role-based permissions)

### 2. Stateless Authentication

Traditional (Stateful):
- Server stores session data
- Cookie contains session ID
- Doesn't scale well

JWT (Stateless):
- No server-side session storage
- Token contains all necessary info
- Scales horizontally easily

### 3. Password Security

```java
// NEVER DO THIS:
user.setPassword(plainPassword); // ❌

// ALWAYS DO THIS:
user.setPassword(passwordEncoder.encode(plainPassword)); // ✅
```

### 4. Role-Based Access Control

```java
// Method-based (in Controller)
@PreAuthorize("hasRole('ADMIN')")
public String adminOnly() { ... }
```

## Security Best Practices

### 1. JWT Secret Key

**Production (Recommended):**
```properties
# Use environment variable
jwt.secret=${JWT_SECRET}
```

### 2. HTTPS Only

Always use HTTPS in production to encrypt token transmission.

### 3. Token Expiration

Current: 24 hours - Consider shorter for high-security apps.

## Common Issues

### Issue 1: Port Already in Use

**Solution:**
```properties
# In application.properties
server.port=8081
```

### Issue 2: Invalid JWT Token

**Solution:** Login again to get new token

### Issue 3: 403 Forbidden

**Cause:** User doesn't have required role

**Solution:** Check user's roles in database

## Code Highlights

Every file in this project contains extensive comments explaining:
- What the code does
- Why it's written that way
- How it fits into the bigger picture
- Common pitfalls and best practices

Start reading from:
1. `SpringBootJwtSecurityApplication.java` - Entry point
2. `WebSecurityConfig.java` - Security configuration
3. `AuthController.java` - Authentication logic
4. `AuthTokenFilter.java` - JWT validation

---

Happy Learning! 🚀

For questions or clarifications, study the code comments - they contain detailed explanations of every concept!