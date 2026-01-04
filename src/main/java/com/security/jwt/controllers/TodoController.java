package com.security.jwt.controllers;

import com.security.jwt.models.Todo;
import com.security.jwt.models.User;
import com.security.jwt.repository.TodoRepository;
import com.security.jwt.repository.UserRepository;
import com.security.jwt.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TODO CONTROLLER
 *
 * REST API endpoints for managing todos
 * Demonstrates CRUD operations with user-specific data
 *
 * KEY LEARNING CONCEPTS:
 * 1. REST API design
 * 2. HTTP methods (GET, POST, PUT, DELETE, PATCH)
 * 3. Request/Response handling
 * 4. User authentication and authorization
 * 5. Data validation
 * 6. Error handling
 *
 * ALL ENDPOINTS REQUIRE AUTHENTICATION
 * - User must be logged in (valid JWT token)
 * - Each user can only access their own todos
 */
@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TodoController {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET CURRENT AUTHENTICATED USER
     *
     * Helper method used by all endpoints
     * Extracts user from Spring Security context
     *
     * How it works:
     * 1. User sends request with JWT token
     * 2. AuthTokenFilter validates token
     * 3. Filter sets authentication in SecurityContext
     * 4. We retrieve it here
     *
     * SecurityContextHolder:
     * - Thread-local storage for security info
     * - Accessible anywhere in request thread
     * - Set by AuthTokenFilter
     *
     * @return Current authenticated User entity
     * @throws RuntimeException if user not found (shouldn't happen)
     */
    private User getCurrentUser() {
        // Get authentication from context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Extract UserDetails (set by AuthTokenFilter)
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Load full User entity from database
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * GET ALL TODOS
     *
     * GET /api/todos
     * Headers: Authorization: Bearer <token>
     *
     * Returns all todos for the authenticated user
     * Other users' todos are NOT included
     *
     * Response: 200 OK
     * [
     *   {
     *     "id": 1,
     *     "title": "Learn React",
     *     "description": "Complete React tutorial",
     *     "completed": false,
     *     "createdAt": "2024-01-15T10:30:00",
     *     "updatedAt": "2024-01-15T10:30:00"
     *   },
     *   ...
     * ]
     *
     * Frontend usage:
     * const response = await todoAPI.getAll();
     * const todos = response.data;
     */
    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos() {
        // Get authenticated user
        User user = getCurrentUser();

        // Fetch only this user's todos
        List<Todo> todos = todoRepository.findByUserId(user.getId());

        // Return list of todos
        return ResponseEntity.ok(todos);
    }

    /**
     * GET SINGLE TODO
     *
     * GET /api/todos/{id}
     * Headers: Authorization: Bearer <token>
     *
     * Returns a specific todo if it belongs to the user
     *
     * Path variable:
     * - {id}: Todo ID from URL
     * - Example: /api/todos/5 → id = 5
     *
     * Security:
     * - Uses findByIdAndUserId()
     * - Ensures user can only access their own todo
     * - Returns 404 if todo doesn't exist or belongs to another user
     *
     * Response: 200 OK
     * {
     *   "id": 1,
     *   "title": "Learn React",
     *   ...
     * }
     *
     * Error: 404 Not Found
     * - Todo doesn't exist
     * - Todo belongs to different user
     */
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        User user = getCurrentUser();

        // Find todo by ID AND user ID (security check)
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        return ResponseEntity.ok(todo);
    }

    /**
     * CREATE TODO
     *
     * POST /api/todos
     * Headers: Authorization: Bearer <token>
     * Content-Type: application/json
     *
     * Request body:
     * {
     *   "title": "Learn Spring Boot",
     *   "description": "Complete Spring Boot tutorial",
     *   "completed": false
     * }
     *
     * @Valid annotation:
     * - Triggers Bean Validation
     * - Validates @NotBlank, @Size, etc.
     * - Returns 400 if validation fails
     * - See GlobalExceptionHandler for error format
     *
     * Process:
     * 1. Validate request body
     * 2. Get current user
     * 3. Set user as todo owner
     * 4. Set completed to false (new todos not completed)
     * 5. Save to database
     * 6. Return saved todo with generated ID
     *
     * Response: 201 Created
     * {
     *   "id": 5,
     *   "title": "Learn Spring Boot",
     *   "description": "Complete Spring Boot tutorial",
     *   "completed": false,
     *   "createdAt": "2024-01-15T10:30:00",
     *   "updatedAt": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody Todo todo) {
        User user = getCurrentUser();

        // Set the owner (current user)
        todo.setUser(user);

        // New todos start as not completed
        todo.setCompleted(false);

        // Save to database
        Todo savedTodo = todoRepository.save(todo);

        // Return 201 Created with saved todo
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTodo);
    }

    /**
     * UPDATE TODO
     *
     * PUT /api/todos/{id}
     * Headers: Authorization: Bearer <token>
     * Content-Type: application/json
     *
     * Request body:
     * {
     *   "title": "Updated title",
     *   "description": "Updated description",
     *   "completed": true
     * }
     *
     * PUT vs PATCH:
     * - PUT: Replace entire resource
     * - PATCH: Partial update
     * - We use PUT but only update specific fields
     *
     * Process:
     * 1. Find existing todo (with security check)
     * 2. Update fields from request body
     * 3. Save updated todo
     * 4. Return updated todo
     *
     * Security:
     * - findByIdAndUserId ensures user owns the todo
     * - User cannot update another user's todo
     *
     * Response: 200 OK
     * {
     *   "id": 1,
     *   "title": "Updated title",
     *   ...
     * }
     *
     * Error: 404 Not Found
     * - Todo doesn't exist or belongs to different user
     */
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody Todo todoDetails) {

        User user = getCurrentUser();

        // Find existing todo (security check)
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        // Update fields
        todo.setTitle(todoDetails.getTitle());
        todo.setDescription(todoDetails.getDescription());
        todo.setCompleted(todoDetails.getCompleted());

        // Save updated todo
        Todo updatedTodo = todoRepository.save(todo);

        return ResponseEntity.ok(updatedTodo);
    }

    /**
     * DELETE TODO
     *
     * DELETE /api/todos/{id}
     * Headers: Authorization: Bearer <token>
     *
     * Deletes a todo if it belongs to the user
     *
     * Process:
     * 1. Attempt to delete by ID and user ID
     * 2. Repository returns count of deleted records
     * 3. If 0, todo didn't exist or belongs to another user
     * 4. If 1, todo was deleted successfully
     *
     * Security:
     * - deleteByIdAndUserId ensures user can only delete their own todo
     *
     * Response: 200 OK (no body)
     * - Todo deleted successfully
     *
     * Response: 404 Not Found
     * - Todo doesn't exist or belongs to different user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        User user = getCurrentUser();

        // Attempt to delete (returns count of deleted records)
        Long deletedCount = todoRepository.deleteByIdAndUserId(id, user.getId());

        if (deletedCount == 0) {
            // Todo not found or doesn't belong to user
            return ResponseEntity.notFound().build();
        }

        // Successfully deleted
        return ResponseEntity.ok().build();
    }

    /**
     * TOGGLE TODO COMPLETION
     *
     * PATCH /api/todos/{id}/toggle
     * Headers: Authorization: Bearer <token>
     *
     * Convenience endpoint to toggle completion status
     * Saves frontend from fetching todo, updating field, and sending back
     *
     * Process:
     * 1. Find todo
     * 2. Toggle completed: true → false, false → true
     * 3. Save and return updated todo
     *
     * Frontend usage:
     * // Simply call toggle, no need to know current state
     * await todoAPI.toggleComplete(todoId);
     * // Todo completion is automatically toggled
     *
     * Response: 200 OK
     * {
     *   "id": 1,
     *   "title": "Learn React",
     *   "completed": true,  // Toggled!
     *   ...
     * }
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Todo> toggleTodoCompletion(@PathVariable Long id) {
        User user = getCurrentUser();

        // Find todo (security check)
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        // Toggle completion status
        todo.setCompleted(!todo.getCompleted());

        // Save updated todo
        Todo updatedTodo = todoRepository.save(todo);

        return ResponseEntity.ok(updatedTodo);
    }

    /**
     * GET COMPLETED TODOS
     *
     * GET /api/todos/completed
     *
     * Returns only completed todos for the user
     * Useful for showing completed/incomplete todos separately
     *
     * Query parameter alternative:
     * GET /api/todos?completed=true
     * Then use @RequestParam Boolean completed
     */
    @GetMapping("/completed")
    public ResponseEntity<List<Todo>> getCompletedTodos() {
        User user = getCurrentUser();
        List<Todo> completedTodos = todoRepository.findByUserIdAndCompleted(
                user.getId(), true);
        return ResponseEntity.ok(completedTodos);
    }

    /**
     * GET PENDING TODOS
     *
     * GET /api/todos/pending
     *
     * Returns only pending (not completed) todos
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Todo>> getPendingTodos() {
        User user = getCurrentUser();
        List<Todo> pendingTodos = todoRepository.findByUserIdAndCompleted(
                user.getId(), false);
        return ResponseEntity.ok(pendingTodos);
    }

    /**
     * GET TODO COUNT
     *
     * GET /api/todos/count
     *
     * Returns total number of todos for the user
     *
     * Response:
     * {
     *   "count": 15
     * }
     *
     * Useful for:
     * - Displaying stats
     * - Pagination
     * - Limiting todos per user
     */
    @GetMapping("/count")
    public ResponseEntity<?> getTodoCount() {
        User user = getCurrentUser();
        Long count = todoRepository.countByUserId(user.getId());
        return ResponseEntity.ok(new CountResponse(count));
    }

    // Helper class for count response
    static class CountResponse {
        private Long count;

        public CountResponse(Long count) {
            this.count = count;
        }

        public Long getCount() {
            return count;
        }
    }
}

/*
 * ============================================
 * TESTING THE API
 * ============================================
 *
 * 1. Get JWT token:
 * POST /api/auth/signin
 * { "username": "john", "password": "password123" }
 * Save the token from response
 *
 * 2. Create todo:
 * POST /api/todos
 * Headers: Authorization: Bearer <token>
 * Body: { "title": "Learn React", "description": "..." }
 *
 * 3. Get all todos:
 * GET /api/todos
 * Headers: Authorization: Bearer <token>
 *
 * 4. Update todo:
 * PUT /api/todos/1
 * Headers: Authorization: Bearer <token>
 * Body: { "title": "Updated", "description": "...", "completed": true }
 *
 * 5. Toggle completion:
 * PATCH /api/todos/1/toggle
 * Headers: Authorization: Bearer <token>
 *
 * 6. Delete todo:
 * DELETE /api/todos/1
 * Headers: Authorization: Bearer <token>
 *
 * ============================================
 * SECURITY NOTES
 * ============================================
 *
 * 1. All endpoints require authentication
 *    - Must include valid JWT token
 *    - AuthTokenFilter validates token
 *
 * 2. User-specific data filtering
 *    - findByUserId() ensures user isolation
 *    - User A cannot access User B's todos
 *
 * 3. Validation
 *    - @Valid ensures data quality
 *    - Prevents empty titles, too long descriptions
 *
 * 4. Error handling
 *    - GlobalExceptionHandler catches exceptions
 *    - Returns user-friendly error messages
 *
 * ============================================
 * POSSIBLE EXTENSIONS
 * ============================================
 *
 * 1. Pagination:
 * @GetMapping
 * public Page<Todo> getTodos(Pageable pageable) {
 *     return todoRepository.findByUserId(userId, pageable);
 * }
 *
 * 2. Sorting:
 * @GetMapping
 * public List<Todo> getTodos(@RequestParam String sortBy) {
 *     return todoRepository.findByUserIdOrderByCreatedAtDesc(userId);
 * }
 *
 * 3. Search:
 * @GetMapping("/search")
 * public List<Todo> search(@RequestParam String keyword) {
 *     return todoRepository.findByUserIdAndTitleContaining(userId, keyword);
 * }
 *
 * 4. Categories/Tags:
 * Add category field to Todo entity
 * Filter by category in queries
 *
 * 5. Due dates:
 * Add dueDate field
 * Query overdue todos
 * Sort by due date
 */
