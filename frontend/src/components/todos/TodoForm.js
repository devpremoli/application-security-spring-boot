import React, { useState } from 'react';
import './Todo.css';

/**
 * TODO FORM COMPONENT
 *
 * Form for creating new todos
 *
 * Demonstrates:
 * 1. Controlled form components
 * 2. Form validation before submission
 * 3. Calling parent component functions via props
 * 4. Error handling and user feedback
 * 5. Resetting form after successful submission
 * 6. Character count display
 * 7. Textarea handling
 *
 * PROPS:
 * - onCreateTodo: Function to call when user submits form
 *   Receives: { title, description }
 *   Expected to return: Promise (resolves on success, rejects on error)
 */

const TodoForm = ({ onCreateTodo }) => {
    // ============================================
    // STATE MANAGEMENT
    // ============================================

    /**
     * FORM DATA STATE
     *
     * title: Todo title (required, max 100 chars)
     * description: Todo description (optional, max 500 chars)
     */
    const [formData, setFormData] = useState({
        title: '',
        description: ''
    });

    /**
     * VALIDATION ERRORS
     *
     * Field-specific error messages
     */
    const [errors, setErrors] = useState({});

    /**
     * SUBMITTING STATE
     *
     * True while API request is in progress
     * Prevents duplicate submissions
     */
    const [submitting, setSubmitting] = useState(false);

    /**
     * EXPANDED STATE
     *
     * Controls whether description field is visible
     * Collapsed by default, expands when user wants to add description
     */
    const [expanded, setExpanded] = useState(false);

    // ============================================
    // VALIDATION
    // ============================================

    /**
     * VALIDATE FORM
     *
     * Checks form data before submission
     * Matches backend validation rules
     *
     * Backend rules (from Todo.java):
     * - title: @NotBlank, @Size(max = 100)
     * - description: @Size(max = 500)
     *
     * Frontend validation should match backend
     * Provides immediate feedback to user
     * Reduces unnecessary API calls
     */
    const validateForm = () => {
        const newErrors = {};

        // Title validation
        if (!formData.title.trim()) {
            newErrors.title = 'Title is required';
        } else if (formData.title.length > 100) {
            newErrors.title = 'Title cannot exceed 100 characters';
        }

        // Description validation (optional field)
        if (formData.description && formData.description.length > 500) {
            newErrors.description = 'Description cannot exceed 500 characters';
        }

        return newErrors;
    };

    // ============================================
    // EVENT HANDLERS
    // ============================================

    /**
     * HANDLE INPUT CHANGE
     *
     * Updates form data when user types
     * Clears field-specific error when user starts typing
     */
    const handleChange = (e) => {
        const { name, value } = e.target;

        // Update form data
        setFormData({
            ...formData,
            [name]: value
        });

        // Clear error for this field
        if (errors[name]) {
            setErrors({
                ...errors,
                [name]: ''
            });
        }
    };

    /**
     * HANDLE FORM SUBMIT
     *
     * Process:
     * 1. Prevent default form submission
     * 2. Validate form data
     * 3. If invalid: Show errors, stop
     * 4. If valid: Call onCreateTodo (parent function)
     * 5. If success: Reset form
     * 6. If error: Show error message
     *
     * Parent Communication:
     * - Calls onCreateTodo prop with todo data
     * - onCreateTodo is handleCreateTodo from TodoList
     * - Returns promise that resolves/rejects
     * - We handle success/error here
     */
    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validate
        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        // Clear errors
        setErrors({});
        setSubmitting(true);

        try {
            /**
             * CALL PARENT FUNCTION
             *
             * onCreateTodo is passed from TodoList
             * It will:
             * 1. Send POST /api/todos
             * 2. Add todo to todos array
             * 3. Return success
             */
            await onCreateTodo({
                title: formData.title.trim(),
                description: formData.description.trim()
            });

            // Success - reset form
            setFormData({ title: '', description: '' });
            setExpanded(false); // Collapse description field

        } catch (err) {
            console.error('Error creating todo:', err);

            /**
             * HANDLE BACKEND ERRORS
             *
             * Backend might return validation errors
             * Example: Title already exists (custom validation)
             */
            if (err.response && err.response.data) {
                if (err.response.data.errors) {
                    // Field-specific errors
                    setErrors(err.response.data.errors);
                } else {
                    // General error
                    setErrors({ general: err.response.data.message || 'Failed to create todo' });
                }
            } else {
                setErrors({ general: 'Failed to create todo. Please try again.' });
            }
        } finally {
            setSubmitting(false);
        }
    };

    /**
     * TOGGLE EXPANDED
     *
     * Shows/hides description field
     * Improves UX by keeping form compact
     */
    const toggleExpanded = () => {
        setExpanded(!expanded);
    };

    // ============================================
    // RENDER
    // ============================================

    return (
        <div className="todo-form-container">
            {/* General Error */}
            {errors.general && (
                <div className="error-message">
                    {errors.general}
                </div>
            )}

            <form onSubmit={handleSubmit} className="todo-form">
                {/* Title Input */}
                <div className="form-row">
                    <div className="form-group flex-grow">
                        {/*
                            TITLE INPUT

                            placeholder: Hint text
                            value: Controlled by state
                            onChange: Updates state
                            className: Dynamic - adds error class if invalid
                            disabled: Disabled during submission
                        */}
                        <input
                            type="text"
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                            placeholder="What needs to be done?"
                            className={`todo-input ${errors.title ? 'input-error' : ''}`}
                            disabled={submitting}
                        />

                        {/* Character Counter */}
                        {/*
                            LIVE CHARACTER COUNT

                            Shows: 15 / 100
                            Updates as user types
                            Turns red when approaching limit

                            formData.title.length >= 90
                            - If 90+ characters, add 'warning' class
                            - CSS makes it red
                        */}
                        {formData.title && (
                            <small className={`char-count ${formData.title.length >= 90 ? 'warning' : ''}`}>
                                {formData.title.length} / 100
                            </small>
                        )}

                        {/* Error Message */}
                        {errors.title && (
                            <span className="field-error">{errors.title}</span>
                        )}
                    </div>

                    {/* Expand Button */}
                    {/*
                        TOGGLE DESCRIPTION BUTTON

                        Shows "+" when collapsed
                        Shows "-" when expanded
                        Clicking toggles description field visibility
                    */}
                    <button
                        type="button"
                        onClick={toggleExpanded}
                        className="btn-expand"
                        title={expanded ? 'Hide description' : 'Add description'}
                    >
                        {expanded ? '−' : '+'}
                    </button>

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={submitting || !formData.title.trim()}
                        className="btn btn-primary btn-add"
                    >
                        {submitting ? 'Adding...' : 'Add'}
                    </button>
                </div>

                {/* Description Field (Conditional) */}
                {/*
                    CONDITIONAL RENDERING

                    Only show description field if expanded is true

                    CSS Transition:
                    - Could add slide-down animation
                    - See Todo.css for animation styles
                */}
                {expanded && (
                    <div className="form-group">
                        {/*
                            TEXTAREA ELEMENT

                            Multi-line text input
                            Auto-resizes based on content

                            rows: Initial height
                            placeholder: Hint text
                        */}
                        <textarea
                            name="description"
                            value={formData.description}
                            onChange={handleChange}
                            placeholder="Add a description (optional)"
                            className={`todo-textarea ${errors.description ? 'input-error' : ''}`}
                            rows="3"
                            disabled={submitting}
                        />

                        {/* Character Counter */}
                        {formData.description && (
                            <small className={`char-count ${formData.description.length >= 450 ? 'warning' : ''}`}>
                                {formData.description.length} / 500
                            </small>
                        )}

                        {/* Error Message */}
                        {errors.description && (
                            <span className="field-error">{errors.description}</span>
                        )}
                    </div>
                )}
            </form>
        </div>
    );
};

export default TodoForm;

/*
 * ============================================
 * COMPONENT USAGE EXAMPLE
 * ============================================
 *
 * In TodoList.js:
 *
 * const handleCreateTodo = async (todoData) => {
 *     const response = await todoAPI.create(todoData);
 *     setTodos([response.data, ...todos]);
 *     return true;
 * };
 *
 * <TodoForm onCreateTodo={handleCreateTodo} />
 *
 * ============================================
 * CONTROLLED VS UNCONTROLLED COMPONENTS
 * ============================================
 *
 * This is a CONTROLLED component:
 * - Form inputs value is controlled by React state
 * - Every change updates state via onChange
 * - State is single source of truth
 *
 * Controlled:
 * <input value={formData.title} onChange={handleChange} />
 * - Value always matches state
 * - Can validate on every keystroke
 * - Can transform input (e.g., uppercase)
 *
 * Uncontrolled (not used here):
 * <input ref={inputRef} />
 * - DOM is source of truth
 * - Access value via ref
 * - Simpler but less control
 *
 * ============================================
 * FORM SUBMISSION FLOW
 * ============================================
 *
 * 1. User Types "Buy groceries"
 *    - onChange fires
 *    - handleChange updates formData.title
 *    - Component re-renders
 *    - Input shows "Buy groceries"
 *    - Character count shows "13 / 100"
 *
 * 2. User Clicks "+" Button
 *    - toggleExpanded called
 *    - setExpanded(true)
 *    - Component re-renders
 *    - Description field appears
 *
 * 3. User Types Description
 *    - onChange fires
 *    - handleChange updates formData.description
 *    - Component re-renders
 *    - Character count updates
 *
 * 4. User Clicks "Add"
 *    - onSubmit fires
 *    - handleSubmit runs
 *    - e.preventDefault() stops page reload
 *    - validateForm() checks data
 *    - onCreateTodo called (parent function)
 *    - TodoList.handleCreateTodo runs
 *    - POST /api/todos sent
 *    - Backend creates todo
 *    - TodoList adds to todos array
 *    - Promise resolves
 *    - Form resets (title and description cleared)
 *    - Description field collapses
 *
 * ============================================
 * VALIDATION FLOW
 * ============================================
 *
 * Frontend Validation (immediate feedback):
 * 1. User types 101 characters
 * 2. Character count turns red
 * 3. User clicks "Add"
 * 4. validateForm() runs
 * 5. Returns error: "Title cannot exceed 100 characters"
 * 6. setErrors updates state
 * 7. Error message appears under input
 * 8. Form not submitted to backend
 *
 * Backend Validation (server-side check):
 * 1. User bypasses frontend validation (rare)
 * 2. Request sent to backend
 * 3. @Valid triggers Bean Validation
 * 4. Returns 400 Bad Request
 * 5. Response: { "errors": { "title": "..." } }
 * 6. Catch block extracts errors
 * 7. setErrors updates state
 * 8. Error message appears
 *
 * ============================================
 * PARENT-CHILD COMMUNICATION
 * ============================================
 *
 * TodoList (Parent) ← → TodoForm (Child)
 *
 * Data Flow DOWN (via props):
 * - TodoList passes onCreateTodo function
 * - TodoForm receives as prop
 *
 * Events Flow UP (via callbacks):
 * - User submits form in TodoForm
 * - TodoForm calls onCreateTodo(data)
 * - onCreateTodo runs in TodoList context
 * - TodoList updates its state
 * - React re-renders TodoList
 * - TodoForm re-renders (receives fresh props)
 *
 * Why not store todos in TodoForm?
 * - TodoList needs to display all todos
 * - Multiple components need same data
 * - State should be in common parent
 * - "Lift state up" pattern
 *
 * ============================================
 * PERFORMANCE OPTIMIZATION IDEAS
 * ============================================
 *
 * Current: Re-renders on every keystroke
 *
 * Optimizations:
 * 1. Debounce validation
 *    - Don't validate until user stops typing
 *    - Wait 300ms after last keystroke
 *
 * 2. useCallback for handlers
 *    - Memoize handleChange, handleSubmit
 *    - Prevents unnecessary re-renders
 *
 * 3. React.memo for component
 *    - Only re-render if props change
 *    - onCreateTodo should be memoized too
 *
 * 4. Lazy load description field
 *    - Don't render until expanded
 *    - Use dynamic import
 *
 * For most apps, these aren't needed!
 * Premature optimization is root of all evil.
 *
 * ============================================
 * ACCESSIBILITY CONSIDERATIONS
 * ============================================
 *
 * Current implementation could be improved with:
 *
 * 1. Labels for screen readers
 *    <label htmlFor="title">Title</label>
 *
 * 2. ARIA attributes
 *    aria-label="Todo title"
 *    aria-invalid={!!errors.title}
 *    aria-describedby="title-error"
 *
 * 3. Error announcements
 *    <div role="alert" id="title-error">
 *      {errors.title}
 *    </div>
 *
 * 4. Keyboard navigation
 *    - Tab through inputs
 *    - Enter to submit
 *    - Escape to collapse
 *
 * 5. Focus management
 *    - Auto-focus title on mount
 *    - Focus first error after validation
 */
