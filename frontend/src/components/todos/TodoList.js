import React, { useState, useEffect } from 'react';
import { todoAPI } from '../../services/api';
import TodoForm from './TodoForm';
import TodoItem from './TodoItem';
import './Todo.css';

/**
 * TODO LIST COMPONENT
 *
 * Main component for displaying and managing todos
 *
 * Demonstrates:
 * 1. useEffect for data fetching on component mount
 * 2. CRUD operations (Create, Read, Update, Delete)
 * 3. State management for todo list
 * 4. Loading and error states
 * 5. Communication between parent and child components
 * 6. Filtering todos (all, completed, pending)
 * 7. Real-time UI updates after API calls
 *
 * KEY CONCEPTS:
 * - useEffect: Side effects (API calls) when component mounts
 * - Array methods: map, filter
 * - Conditional rendering: Loading, error, empty states
 * - Props passing: Sending data and functions to child components
 * - Callback functions: Child components calling parent functions
 */

const TodoList = () => {
    // ============================================
    // STATE MANAGEMENT
    // ============================================

    /**
     * TODOS STATE
     *
     * Array of todo objects
     * Each todo: { id, title, description, completed, createdAt, updatedAt }
     */
    const [todos, setTodos] = useState([]);

    /**
     * LOADING STATE
     *
     * True when fetching todos from backend
     * Shows loading spinner
     */
    const [loading, setLoading] = useState(true);

    /**
     * ERROR STATE
     *
     * Stores error messages from failed API calls
     */
    const [error, setError] = useState('');

    /**
     * FILTER STATE
     *
     * Controls which todos to display
     * Values: 'all', 'completed', 'pending'
     */
    const [filter, setFilter] = useState('all');

    // ============================================
    // LIFECYCLE - DATA FETCHING
    // ============================================

    /**
     * useEffect - FETCH TODOS ON MOUNT
     *
     * Runs when component first renders
     * Empty dependency array [] means "run once on mount"
     *
     * Process:
     * 1. Component renders (todos is empty array)
     * 2. useEffect runs after render
     * 3. fetchTodos() called
     * 4. API request sent
     * 5. Response received
     * 6. setTodos updates state
     * 7. Component re-renders with todos
     *
     * Dependency array explained:
     * - [] : Run once on mount
     * - [filter] : Run on mount and when filter changes
     * - No array : Run on every render (infinite loop!)
     */
    useEffect(() => {
        fetchTodos();
    }, []); // Empty array = run once on mount

    /**
     * FETCH TODOS FUNCTION
     *
     * Loads all todos from backend
     * Called on component mount
     * Can be called again to refresh list
     *
     * API Call:
     * GET /api/todos
     * Headers: Authorization: Bearer <token>
     * Response: [{ id: 1, title: "...", ... }, ...]
     */
    const fetchTodos = async () => {
        try {
            setLoading(true);
            setError('');

            // Call API (token added automatically by interceptor)
            const response = await todoAPI.getAll();

            // Update state with todos
            setTodos(response.data);
        } catch (err) {
            console.error('Error fetching todos:', err);

            // Handle different error types
            if (err.response) {
                // Backend returned error
                setError(err.response.data.message || 'Failed to load todos');
            } else if (err.request) {
                // Network error
                setError('Unable to connect to server');
            } else {
                // Other errors
                setError('Failed to load todos');
            }
        } finally {
            setLoading(false);
        }
    };

    // ============================================
    // CRUD OPERATIONS
    // ============================================

    /**
     * CREATE TODO
     *
     * Called by TodoForm when user creates new todo
     * Receives todo data from child component
     *
     * Process:
     * 1. Send POST request to backend
     * 2. Backend creates todo in database
     * 3. Backend returns created todo with ID
     * 4. Add new todo to local state
     * 5. UI updates immediately
     *
     * Optimistic Update Alternative:
     * Instead of waiting for backend response, could:
     * 1. Add todo to UI immediately
     * 2. Send request in background
     * 3. If fails, remove from UI
     */
    const handleCreateTodo = async (todoData) => {
        try {
            // Send POST request
            const response = await todoAPI.create(todoData);

            // Add new todo to beginning of array
            // spread operator creates new array (React needs new reference to detect change)
            setTodos([response.data, ...todos]);

            return true; // Success
        } catch (err) {
            console.error('Error creating todo:', err);
            throw err; // Re-throw so TodoForm can handle error
        }
    };

    /**
     * UPDATE TODO
     *
     * Called by TodoItem when user edits todo
     *
     * Process:
     * 1. Find todo in local state
     * 2. Send PUT request with updated data
     * 3. Backend updates database
     * 4. Update local state with response
     * 5. UI updates to show changes
     *
     * Array update pattern:
     * - Map through todos array
     * - For matching ID: Replace with updated todo
     * - For other IDs: Keep original
     */
    const handleUpdateTodo = async (id, updatedData) => {
        try {
            // Send PUT request
            const response = await todoAPI.update(id, updatedData);

            // Update todo in local state
            setTodos(todos.map(todo =>
                todo.id === id ? response.data : todo
            ));

            return true;
        } catch (err) {
            console.error('Error updating todo:', err);
            throw err;
        }
    };

    /**
     * TOGGLE TODO COMPLETION
     *
     * Marks todo as completed/not completed
     * Uses PATCH endpoint for partial update
     *
     * Process:
     * 1. Send PATCH /api/todos/{id}/toggle
     * 2. Backend toggles completed field
     * 3. Update local state
     */
    const handleToggleTodo = async (id) => {
        try {
            // Send PATCH request
            const response = await todoAPI.toggleComplete(id);

            // Update todo in local state
            setTodos(todos.map(todo =>
                todo.id === id ? response.data : todo
            ));

            return true;
        } catch (err) {
            console.error('Error toggling todo:', err);
            throw err;
        }
    };

    /**
     * DELETE TODO
     *
     * Removes todo from database and UI
     *
     * Process:
     * 1. Send DELETE request
     * 2. Backend deletes from database
     * 3. Remove from local state
     * 4. UI updates (todo disappears)
     *
     * Array deletion pattern:
     * - Filter array to exclude deleted item
     * - Returns new array without deleted todo
     */
    const handleDeleteTodo = async (id) => {
        try {
            // Send DELETE request
            await todoAPI.delete(id);

            // Remove todo from local state
            setTodos(todos.filter(todo => todo.id !== id));

            return true;
        } catch (err) {
            console.error('Error deleting todo:', err);
            throw err;
        }
    };

    // ============================================
    // FILTERING
    // ============================================

    /**
     * GET FILTERED TODOS
     *
     * Returns todos based on current filter
     *
     * Filter logic:
     * - 'all': Show all todos
     * - 'completed': Show only completed (completed === true)
     * - 'pending': Show only pending (completed === false)
     *
     * Array.filter():
     * - Creates new array with items that pass test
     * - Doesn't modify original array
     */
    const getFilteredTodos = () => {
        switch (filter) {
            case 'completed':
                return todos.filter(todo => todo.completed);
            case 'pending':
                return todos.filter(todo => !todo.completed);
            default:
                return todos;
        }
    };

    /**
     * GET TODO STATS
     *
     * Calculates counts for display
     * Used in filter buttons to show counts
     */
    const getTodoStats = () => {
        return {
            total: todos.length,
            completed: todos.filter(t => t.completed).length,
            pending: todos.filter(t => !t.completed).length
        };
    };

    // ============================================
    // RENDER
    // ============================================

    /**
     * LOADING STATE
     *
     * Show spinner while fetching todos
     * Prevents showing empty state during load
     */
    if (loading) {
        return (
            <div className="todo-container">
                <div className="loading-spinner">
                    <div className="spinner"></div>
                    <p>Loading todos...</p>
                </div>
            </div>
        );
    }

    /**
     * ERROR STATE
     *
     * Show error message if fetch failed
     * Provide retry button
     */
    if (error) {
        return (
            <div className="todo-container">
                <div className="error-container">
                    <p className="error-text">{error}</p>
                    <button onClick={fetchTodos} className="btn btn-primary">
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    // Get filtered todos and stats
    const filteredTodos = getFilteredTodos();
    const stats = getTodoStats();

    return (
        <div className="todo-container">
            {/* Header */}
            <div className="todo-header">
                <h1>My Todos</h1>
                <p className="todo-stats">
                    {stats.total} total • {stats.completed} completed • {stats.pending} pending
                </p>
            </div>

            {/* Todo Form (Create New) */}
            {/*
                PASSING FUNCTION AS PROP

                onCreateTodo={handleCreateTodo}
                - Pass function reference to child component
                - TodoForm can call this function
                - When called, handleCreateTodo runs in THIS component
                - Allows child to trigger parent's state update

                Why?
                - TodoForm knows how to collect input
                - TodoList manages the todos array
                - TodoForm tells TodoList "new todo created"
            */}
            <TodoForm onCreateTodo={handleCreateTodo} />

            {/* Filter Buttons */}
            {/*
                FILTER CONTROLS

                Active button has 'active' class
                filter === 'all' ? 'active' : ''
                - If current filter is 'all', add 'active' class
                - CSS styles active button differently
            */}
            <div className="filter-controls">
                <button
                    className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
                    onClick={() => setFilter('all')}
                >
                    All ({stats.total})
                </button>
                <button
                    className={`filter-btn ${filter === 'pending' ? 'active' : ''}`}
                    onClick={() => setFilter('pending')}
                >
                    Pending ({stats.pending})
                </button>
                <button
                    className={`filter-btn ${filter === 'completed' ? 'active' : ''}`}
                    onClick={() => setFilter('completed')}
                >
                    Completed ({stats.completed})
                </button>
            </div>

            {/* Todo List */}
            {/*
                CONDITIONAL RENDERING - EMPTY STATE

                filteredTodos.length === 0
                - If no todos match filter, show empty message
                - Otherwise, show todo list
            */}
            {filteredTodos.length === 0 ? (
                <div className="empty-state">
                    <p className="empty-icon">📝</p>
                    <p className="empty-text">
                        {filter === 'all' && 'No todos yet. Create one to get started!'}
                        {filter === 'completed' && 'No completed todos yet.'}
                        {filter === 'pending' && 'No pending todos. Great job!'}
                    </p>
                </div>
            ) : (
                <div className="todo-list">
                    {/*
                        ARRAY MAPPING TO COMPONENTS

                        todos.map(todo => <TodoItem ... />)
                        - Iterate through todos array
                        - Create TodoItem component for each todo
                        - Return array of components
                        - React renders each component

                        KEY PROP:
                        - key={todo.id} is REQUIRED
                        - Helps React identify which items changed
                        - Must be unique
                        - Never use array index as key

                        PROPS:
                        - todo={todo}: Pass entire todo object
                        - onUpdate={handleUpdateTodo}: Pass update function
                        - onDelete={handleDeleteTodo}: Pass delete function
                        - onToggle={handleToggleTodo}: Pass toggle function
                    */}
                    {filteredTodos.map(todo => (
                        <TodoItem
                            key={todo.id}
                            todo={todo}
                            onUpdate={handleUpdateTodo}
                            onDelete={handleDeleteTodo}
                            onToggle={handleToggleTodo}
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

export default TodoList;

/*
 * ============================================
 * DATA FLOW DIAGRAM
 * ============================================
 *
 * Component Mount:
 * 1. TodoList renders with empty todos
 * 2. useEffect triggers
 * 3. fetchTodos() called
 * 4. GET /api/todos request
 * 5. Backend returns todos array
 * 6. setTodos updates state
 * 7. Component re-renders with todos
 * 8. todos.map creates TodoItem for each
 *
 * Creating Todo:
 * 1. User types in TodoForm
 * 2. User clicks "Add Todo"
 * 3. TodoForm calls onCreateTodo (= handleCreateTodo)
 * 4. handleCreateTodo sends POST /api/todos
 * 5. Backend creates todo, returns with ID
 * 6. setTodos adds new todo to array
 * 7. TodoList re-renders
 * 8. New TodoItem appears at top
 *
 * Updating Todo:
 * 1. User clicks edit in TodoItem
 * 2. TodoItem shows edit form
 * 3. User changes title, clicks save
 * 4. TodoItem calls onUpdate (= handleUpdateTodo)
 * 5. handleUpdateTodo sends PUT /api/todos/{id}
 * 6. Backend updates todo
 * 7. setTodos updates todo in array
 * 8. TodoList re-renders
 * 9. TodoItem shows updated data
 *
 * Toggling Completion:
 * 1. User clicks checkbox in TodoItem
 * 2. TodoItem calls onToggle (= handleToggleTodo)
 * 3. handleToggleTodo sends PATCH /api/todos/{id}/toggle
 * 4. Backend toggles completed
 * 5. setTodos updates todo
 * 6. TodoList re-renders
 * 7. TodoItem shows checked/unchecked
 *
 * Deleting Todo:
 * 1. User clicks delete in TodoItem
 * 2. TodoItem calls onDelete (= handleDeleteTodo)
 * 3. handleDeleteTodo sends DELETE /api/todos/{id}
 * 4. Backend deletes todo
 * 5. setTodos removes todo from array
 * 6. TodoList re-renders
 * 7. TodoItem disappears
 *
 * ============================================
 * COMPONENT HIERARCHY
 * ============================================
 *
 * TodoList (this component)
 * ├── TodoForm
 * │   └── Handles creating new todos
 * └── TodoItem (multiple instances)
 *     └── Handles display and editing of one todo
 *
 * Data Flow:
 * - Props flow DOWN (TodoList → TodoItem)
 * - Events flow UP (TodoItem → TodoList via callbacks)
 *
 * ============================================
 * STATE MANAGEMENT PATTERN
 * ============================================
 *
 * "Lifting State Up"
 * - Todo list stored in TodoList (parent)
 * - TodoForm and TodoItem don't store todos
 * - Children receive data via props
 * - Children notify parent via callbacks
 * - Parent updates state
 * - React propagates changes to children
 *
 * Why?
 * - Single source of truth
 * - Easier to debug
 * - Consistent data across components
 * - Easier to add features (e.g., search, filters)
 *
 * ============================================
 * PERFORMANCE CONSIDERATIONS
 * ============================================
 *
 * Current approach:
 * - Fetch all todos on mount
 * - Store in state
 * - Filter in frontend
 *
 * For large todo lists (1000s of items):
 * - Add pagination (load 20 at a time)
 * - Backend filtering (GET /api/todos?completed=true)
 * - Virtual scrolling (only render visible items)
 * - useCallback to memoize functions
 * - React.memo for TodoItem to prevent unnecessary re-renders
 *
 * ============================================
 * ERROR HANDLING STRATEGIES
 * ============================================
 *
 * Current: Show error message, provide retry button
 *
 * Alternatives:
 * - Toast notifications (non-blocking)
 * - Optimistic updates (update UI, then sync with backend)
 * - Retry failed requests automatically
 * - Cache todos in localStorage (work offline)
 * - Show stale data with "refreshing..." indicator
 */
