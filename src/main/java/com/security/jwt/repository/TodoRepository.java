package com.security.jwt.repository;

import com.security.jwt.models.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO REPOSITORY
 *
 * Data access layer for Todo entities
 * Demonstrates user-specific data filtering
 *
 * Learning concepts:
 * - Spring Data JPA query methods
 * - Method naming conventions
 * - User-specific data access
 * - Optional return types
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    /**
     * FIND ALL TODOS BY USER ID
     *
     * Query method - Spring Data generates implementation
     * Generated SQL: SELECT * FROM todos WHERE user_id = ?
     *
     * This ensures users only see their own todos
     *
     * Method naming convention:
     * - findBy: SELECT query
     * - UserId: Property path (user.id)
     * - Spring traverses user relationship to get user.id
     *
     * @param userId - The user's ID
     * @return List of todos belonging to the user
     */
    List<Todo> findByUserId(Long userId);

    /**
     * FIND TODO BY ID AND USER ID
     *
     * Ensures user can only access their own todos
     * Prevents unauthorized access to other users' todos
     *
     * Generated SQL:
     * SELECT * FROM todos WHERE id = ? AND user_id = ?
     *
     * Why Optional?
     * - Todo might not exist
     * - Todo might belong to different user
     * - Optional forces explicit handling
     *
     * Usage:
     * Optional<Todo> todoOpt = todoRepository.findByIdAndUserId(id, userId);
     * if (todoOpt.isPresent()) {
     *     Todo todo = todoOpt.get();
     *     // Process todo
     * } else {
     *     // Not found or access denied
     * }
     *
     * @param id - Todo ID
     * @param userId - User ID
     * @return Optional containing todo if found and owned by user
     */
    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    /**
     * DELETE TODO BY ID AND USER ID
     *
     * Deletes todo only if it belongs to the user
     * Security measure: Prevents deleting other users' todos
     *
     * Generated SQL:
     * DELETE FROM todos WHERE id = ? AND user_id = ?
     *
     * Returns number of deleted records:
     * - 1 if todo was deleted
     * - 0 if todo not found or belongs to different user
     *
     * Usage in service:
     * Long count = todoRepository.deleteByIdAndUserId(id, userId);
     * if (count == 0) {
     *     throw new NotFoundException("Todo not found");
     * }
     *
     * @param id - Todo ID
     * @param userId - User ID
     * @return Number of deleted records
     */
    Long deleteByIdAndUserId(Long id, Long userId);

    /**
     * COUNT TODOS BY USER ID
     *
     * Count how many todos a user has
     *
     * Generated SQL:
     * SELECT COUNT(*) FROM todos WHERE user_id = ?
     *
     * Use cases:
     * - Display total todo count
     * - Limit number of todos per user
     * - Statistics/analytics
     *
     * @param userId - User ID
     * @return Number of todos
     */
    Long countByUserId(Long userId);

    /**
     * FIND COMPLETED TODOS BY USER ID
     *
     * Get only completed todos for a user
     *
     * Generated SQL:
     * SELECT * FROM todos WHERE user_id = ? AND completed = true
     *
     * Multiple property query:
     * - findBy: SELECT
     * - UserId: WHERE user_id = ?
     * - And: SQL AND
     * - Completed: AND completed = ?
     *
     * @param userId - User ID
     * @param completed - Completion status (true/false)
     * @return List of todos matching criteria
     */
    List<Todo> findByUserIdAndCompleted(Long userId, Boolean completed);
}

/*
 * ============================================
 * SECURITY CONSIDERATIONS
 * ============================================
 *
 * Why include userId in every query?
 *
 * Problem without userId:
 * - User could access any todo by guessing IDs
 * - Example: GET /api/todos/5 might return another user's todo
 * - Security vulnerability!
 *
 * Solution with userId:
 * - Always filter by userId
 * - User can only access their own data
 * - Even if they know another user's todo ID, query returns empty
 *
 * Example attack scenario prevented:
 * 1. Attacker creates account and creates todo (id=1)
 * 2. Attacker knows victim has todo (id=100)
 * 3. Attacker tries: GET /api/todos/100
 * 4. Without userId filter: Returns victim's todo ❌
 * 5. With userId filter: Returns 404 Not Found ✅
 *
 * ============================================
 * ALTERNATIVE QUERY METHODS
 * ============================================
 *
 * Custom queries with @Query annotation:
 *
 * @Query("SELECT t FROM Todo t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
 * List<Todo> findUserTodosOrderedByDate(@Param("userId") Long userId);
 *
 * @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.title LIKE %:keyword%")
 * List<Todo> searchUserTodos(@Param("userId") Long userId, @Param("keyword") String keyword);
 *
 * Native SQL query:
 * @Query(value = "SELECT * FROM todos WHERE user_id = ?1 LIMIT 10", nativeQuery = true)
 * List<Todo> findTop10ByUserId(Long userId);
 *
 * But for simple queries, method naming is cleaner!
 */
