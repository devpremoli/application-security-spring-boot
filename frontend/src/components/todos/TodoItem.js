import React, { useState } from 'react';
import './Todo.css';

/**
 * TODO ITEM COMPONENT
 *
 * Displays a single todo item with edit and delete functionality
 *
 * Demonstrates:
 * 1. Toggle between view and edit modes
 * 2. Inline editing
 * 3. Conditional styling based on completion status
 * 4. Confirmation dialogs for destructive actions
 * 5. Optimistic UI updates
 * 6. Date formatting
 * 7. Multiple event handlers
 *
 * PROPS:
 * - todo: Todo object { id, title, description, completed, createdAt, updatedAt }
 * - onUpdate: Function to call when todo is updated
 * - onDelete: Function to call when todo is deleted
 * - onToggle: Function to call when completion is toggled
 */

const TodoItem = ({ todo, onUpdate, onDelete, onToggle }) => {
    // ============================================
    // STATE MANAGEMENT
    // ============================================

    /**
     * EDIT MODE STATE
     *
     * true: Show edit form
     * false: Show todo display
     */
    const [isEditing, setIsEditing] = useState(false);

    /**
     * EDIT FORM DATA
     *
     * Temporary storage for edits
     * Only applied when user saves
     */
    const [editData, setEditData] = useState({
        title: todo.title,
        description: todo.description || ''
    });

    /**
     * LOADING STATE
     *
     * Shows spinner during API operations
     */
    const [loading, setLoading] = useState(false);

    /**
     * ERROR STATE
     *
     * Displays errors from failed operations
     */
    const [error, setError] = useState('');

    // ============================================
    // EVENT HANDLERS - DISPLAY MODE
    // ============================================

    /**
     * HANDLE TOGGLE COMPLETION
     *
     * Marks todo as completed/not completed
     * Uses checkbox input
     *
     * Process:
     * 1. User clicks checkbox
     * 2. Call onToggle (parent function)
     * 3. Parent sends PATCH /api/todos/{id}/toggle
     * 4. Backend toggles completed field
     * 5. Parent updates state
     * 6. This component re-renders with new todo prop
     * 7. Checkbox reflects new state
     *
     * Note: We don't update local state
     * - Parent owns the data
     * - Parent will pass updated todo via props
     * - React will re-render this component
     */
    const handleToggle = async () => {
        setLoading(true);
        setError('');

        try {
            await onToggle(todo.id);
        } catch (err) {
            console.error('Error toggling todo:', err);
            setError('Failed to update status');
        } finally {
            setLoading(false);
        }
    };

    /**
     * START EDITING
     *
     * Switches to edit mode
     * Initializes edit form with current values
     */
    const startEditing = () => {
        setEditData({
            title: todo.title,
            description: todo.description || ''
        });
        setIsEditing(true);
        setError('');
    };

    /**
     * HANDLE DELETE
     *
     * Deletes the todo
     * Shows confirmation dialog first
     *
     * Confirmation Dialog:
     * - Browser built-in confirm()
     * - Returns true if user clicks OK
     * - Returns false if user clicks Cancel
     *
     * Better UX alternatives:
     * - Custom modal component
     * - Undo functionality
     * - "Trash" with restore option
     */
    const handleDelete = async () => {
        // Confirm deletion
        const confirmed = window.confirm(`Delete "${todo.title}"?`);
        if (!confirmed) return;

        setLoading(true);
        setError('');

        try {
            await onDelete(todo.id);
            // Parent will remove from list, this component will unmount
        } catch (err) {
            console.error('Error deleting todo:', err);
            setError('Failed to delete todo');
            setLoading(false);
        }
    };

    // ============================================
    // EVENT HANDLERS - EDIT MODE
    // ============================================

    /**
     * HANDLE EDIT CHANGE
     *
     * Updates edit form data when user types
     */
    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setEditData({
            ...editData,
            [name]: value
        });
    };

    /**
     * SAVE EDIT
     *
     * Saves edited todo
     *
     * Process:
     * 1. Validate input
     * 2. Call onUpdate (parent function)
     * 3. Parent sends PUT /api/todos/{id}
     * 4. Backend updates database
     * 5. Parent updates state
     * 6. This component receives new todo prop
     * 7. Exit edit mode
     */
    const saveEdit = async () => {
        // Validation
        if (!editData.title.trim()) {
            setError('Title is required');
            return;
        }

        if (editData.title.length > 100) {
            setError('Title cannot exceed 100 characters');
            return;
        }

        if (editData.description.length > 500) {
            setError('Description cannot exceed 500 characters');
            return;
        }

        setLoading(true);
        setError('');

        try {
            // Call parent update function
            await onUpdate(todo.id, {
                title: editData.title.trim(),
                description: editData.description.trim(),
                completed: todo.completed // Keep current completion status
            });

            // Success - exit edit mode
            setIsEditing(false);
        } catch (err) {
            console.error('Error updating todo:', err);

            // Handle backend validation errors
            if (err.response && err.response.data) {
                setError(err.response.data.message || 'Failed to update todo');
            } else {
                setError('Failed to update todo');
            }
        } finally {
            setLoading(false);
        }
    };

    /**
     * CANCEL EDIT
     *
     * Discards changes and exits edit mode
     * Resets edit data to original values
     */
    const cancelEdit = () => {
        setIsEditing(false);
        setEditData({
            title: todo.title,
            description: todo.description || ''
        });
        setError('');
    };

    // ============================================
    // UTILITY FUNCTIONS
    // ============================================

    /**
     * FORMAT DATE
     *
     * Converts ISO date string to readable format
     *
     * Input: "2024-01-15T10:30:00"
     * Output: "Jan 15, 2024"
     *
     * Uses JavaScript Date and Intl.DateTimeFormat
     */
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        }).format(date);
    };

    // ============================================
    // RENDER - EDIT MODE
    // ============================================

    /**
     * EDIT MODE VIEW
     *
     * Shows when isEditing is true
     * Inline edit form
     */
    if (isEditing) {
        return (
            <div className="todo-item editing">
                {/* Error Display */}
                {error && (
                    <div className="error-message-small">
                        {error}
                    </div>
                )}

                {/* Edit Form */}
                <div className="todo-edit-form">
                    {/* Title Input */}
                    <input
                        type="text"
                        name="title"
                        value={editData.title}
                        onChange={handleEditChange}
                        className="edit-input"
                        placeholder="Todo title"
                        disabled={loading}
                        autoFocus // Auto-focus when entering edit mode
                    />

                    {/* Description Textarea */}
                    <textarea
                        name="description"
                        value={editData.description}
                        onChange={handleEditChange}
                        className="edit-textarea"
                        placeholder="Description (optional)"
                        rows="2"
                        disabled={loading}
                    />

                    {/* Action Buttons */}
                    <div className="edit-actions">
                        <button
                            onClick={saveEdit}
                            disabled={loading}
                            className="btn btn-save"
                        >
                            {loading ? 'Saving...' : 'Save'}
                        </button>
                        <button
                            onClick={cancelEdit}
                            disabled={loading}
                            className="btn btn-cancel"
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    // ============================================
    // RENDER - DISPLAY MODE
    // ============================================

    /**
     * DISPLAY MODE VIEW
     *
     * Shows when isEditing is false
     * Todo information with action buttons
     */
    return (
        <div className={`todo-item ${todo.completed ? 'completed' : ''} ${loading ? 'loading' : ''}`}>
            {/* Error Display */}
            {error && (
                <div className="error-message-small">
                    {error}
                </div>
            )}

            {/* Todo Content */}
            <div className="todo-content">
                {/* Checkbox */}
                {/*
                    COMPLETION CHECKBOX

                    checked: Reflects todo.completed
                    onChange: Calls handleToggle
                    disabled: Disabled during loading

                    CSS:
                    - Styled checkbox (custom appearance)
                    - Checkmark icon when checked
                */}
                <input
                    type="checkbox"
                    checked={todo.completed}
                    onChange={handleToggle}
                    disabled={loading}
                    className="todo-checkbox"
                    aria-label={`Mark "${todo.title}" as ${todo.completed ? 'incomplete' : 'complete'}`}
                />

                {/* Todo Details */}
                <div className="todo-details">
                    {/* Title */}
                    {/*
                        DYNAMIC STYLING

                        CSS applies strikethrough when todo has 'completed' class
                        Applied based on todo.completed

                        .todo-item.completed .todo-title {
                          text-decoration: line-through;
                          opacity: 0.6;
                        }
                    */}
                    <h3 className="todo-title">{todo.title}</h3>

                    {/* Description (if exists) */}
                    {/*
                        CONDITIONAL RENDERING

                        Only show if description exists and isn't empty
                        && operator: Renders right side if left side is truthy
                    */}
                    {todo.description && (
                        <p className="todo-description">{todo.description}</p>
                    )}

                    {/* Metadata */}
                    <div className="todo-meta">
                        {/* Created Date */}
                        <span className="todo-date">
                            Created: {formatDate(todo.createdAt)}
                        </span>

                        {/* Updated Date (if different from created) */}
                        {/*
                            SHOW UPDATED DATE

                            Only show if todo was updated after creation
                            Compares timestamps
                        */}
                        {todo.updatedAt !== todo.createdAt && (
                            <span className="todo-date">
                                Updated: {formatDate(todo.updatedAt)}
                            </span>
                        )}
                    </div>
                </div>
            </div>

            {/* Action Buttons */}
            <div className="todo-actions">
                {/* Edit Button */}
                <button
                    onClick={startEditing}
                    disabled={loading}
                    className="btn-icon"
                    title="Edit"
                    aria-label={`Edit "${todo.title}"`}
                >
                    ✏️
                </button>

                {/* Delete Button */}
                <button
                    onClick={handleDelete}
                    disabled={loading}
                    className="btn-icon btn-delete"
                    title="Delete"
                    aria-label={`Delete "${todo.title}"`}
                >
                    🗑️
                </button>
            </div>

            {/* Loading Overlay */}
            {/*
                LOADING INDICATOR

                Shows semi-transparent overlay when loading
                Prevents clicks during operation
            */}
            {loading && (
                <div className="loading-overlay">
                    <div className="spinner-small"></div>
                </div>
            )}
        </div>
    );
};

export default TodoItem;

/*
 * ============================================
 * COMPONENT STATE MANAGEMENT
 * ============================================
 *
 * TodoItem has local state for:
 * - isEditing: UI state (doesn't affect other todos)
 * - editData: Temporary data (discarded if cancelled)
 * - loading: UI state for this todo only
 * - error: Error for this specific todo
 *
 * TodoItem does NOT store:
 * - todo data (comes from parent via props)
 * - todos list (parent manages)
 *
 * Why?
 * - Todo data is shared (needs to be in parent)
 * - Edit mode is local (only this component cares)
 * - Loading/error are local (specific to this instance)
 *
 * ============================================
 * PROPS EXPLAINED
 * ============================================
 *
 * Props are data passed from parent (TodoList)
 *
 * todo prop:
 * - Object with todo data
 * - Read-only (never modify directly)
 * - When parent updates, component re-renders with new todo
 *
 * Function props:
 * - onUpdate, onDelete, onToggle
 * - References to TodoList functions
 * - Calling them triggers parent state update
 * - Parent re-renders
 * - New todo prop passed to this component
 * - This component re-renders
 *
 * ============================================
 * EDIT FLOW
 * ============================================
 *
 * 1. User clicks "Edit" button
 *    - startEditing() called
 *    - setEditData copies todo data
 *    - setIsEditing(true)
 *    - Component re-renders in edit mode
 *
 * 2. User changes title
 *    - onChange triggers
 *    - handleEditChange updates editData
 *    - Component re-renders
 *    - Input shows new value
 *
 * 3. User clicks "Save"
 *    - saveEdit() called
 *    - Validates editData
 *    - Calls onUpdate(todo.id, editData)
 *    - onUpdate sends PUT /api/todos/{id}
 *    - Backend updates database
 *    - Backend returns updated todo
 *    - onUpdate updates TodoList state
 *    - TodoList re-renders
 *    - TodoItem receives new todo prop
 *    - TodoItem re-renders
 *    - setIsEditing(false)
 *    - Component shows display mode with updated data
 *
 * 4. Alternative: User clicks "Cancel"
 *    - cancelEdit() called
 *    - setIsEditing(false)
 *    - editData reset to original
 *    - Component shows display mode
 *    - No API call made
 *
 * ============================================
 * DELETE FLOW
 * ============================================
 *
 * 1. User clicks delete button
 *    - handleDelete() called
 *    - window.confirm() shows dialog
 *    - User clicks "OK" or "Cancel"
 *
 * 2. If Cancel: Nothing happens
 *
 * 3. If OK:
 *    - onDelete(todo.id) called
 *    - TodoList.handleDeleteTodo runs
 *    - DELETE /api/todos/{id} sent
 *    - Backend deletes todo
 *    - TodoList removes from todos array
 *    - TodoList re-renders
 *    - TodoItem unmounts (no longer in array)
 *    - Todo disappears from UI
 *
 * ============================================
 * TOGGLE COMPLETION FLOW
 * ============================================
 *
 * 1. User clicks checkbox
 *    - handleToggle() called
 *    - onToggle(todo.id) called
 *    - TodoList.handleToggleTodo runs
 *    - PATCH /api/todos/{id}/toggle sent
 *    - Backend toggles completed field
 *    - Backend returns updated todo
 *    - TodoList updates todo in array
 *    - TodoList re-renders
 *    - TodoItem receives updated todo prop
 *    - TodoItem re-renders
 *    - Checkbox shows new state
 *    - CSS applies/removes strikethrough
 *
 * ============================================
 * STYLING BASED ON STATE
 * ============================================
 *
 * Dynamic class names:
 *
 * className={`todo-item ${todo.completed ? 'completed' : ''} ${loading ? 'loading' : ''}`}
 *
 * Possible results:
 * - "todo-item" (not completed, not loading)
 * - "todo-item completed" (completed, not loading)
 * - "todo-item loading" (not completed, loading)
 * - "todo-item completed loading" (both)
 *
 * CSS then styles based on classes:
 * - .todo-item.completed .todo-title { text-decoration: line-through; }
 * - .todo-item.loading { opacity: 0.7; }
 *
 * ============================================
 * ACCESSIBILITY FEATURES
 * ============================================
 *
 * Current:
 * - aria-label on checkbox and buttons
 * - title attributes for tooltips
 * - Semantic HTML (h3 for title, button for actions)
 * - Keyboard accessible (tab, enter, space)
 *
 * Could add:
 * - aria-busy during loading
 * - aria-live for status updates
 * - Focus management (return focus after delete)
 * - Keyboard shortcuts (e for edit, d for delete)
 * - Screen reader announcements
 *
 * ============================================
 * PERFORMANCE CONSIDERATIONS
 * ============================================
 *
 * Current approach:
 * - Re-renders when parent updates
 * - Re-renders when local state changes
 *
 * Optimization with React.memo:
 *
 * export default React.memo(TodoItem, (prevProps, nextProps) => {
 *   return prevProps.todo.id === nextProps.todo.id &&
 *          prevProps.todo.completed === nextProps.todo.completed &&
 *          prevProps.todo.title === nextProps.todo.title &&
 *          prevProps.todo.description === nextProps.todo.description;
 * });
 *
 * Only re-render if todo actually changed
 * Useful with large todo lists (100+)
 *
 * ============================================
 * ERROR HANDLING
 * ============================================
 *
 * Each operation handles errors independently:
 *
 * - Toggle error: Shows inline, doesn't affect display
 * - Edit error: Shows in edit form, prevents save
 * - Delete error: Shows inline, keeps todo visible
 *
 * User can retry after error
 * Errors don't break the component
 * Error state clears on next action
 *
 * Alternative approaches:
 * - Toast notifications (global)
 * - Error boundary (catch React errors)
 * - Retry logic (automatic retries)
 * - Undo functionality (revert on error)
 */
