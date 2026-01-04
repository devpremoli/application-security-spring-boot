import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

/**
 * SIGNUP COMPONENT
 *
 * Demonstrates:
 * 1. Multi-field form handling
 * 2. Form validation (client-side)
 * 3. Password confirmation matching
 * 4. Field-level error display
 * 5. Role selection with checkboxes
 * 6. Complex state management
 * 7. Backend validation error handling
 *
 * KEY DIFFERENCES FROM LOGIN:
 * - More form fields (username, email, password, confirmPassword, roles)
 * - Client-side validation (email format, password match, password strength)
 * - Checkbox handling for role selection
 * - Field-specific error messages
 */

const Signup = () => {
    // ============================================
    // STATE MANAGEMENT
    // ============================================

    /**
     * FORM DATA STATE
     *
     * More fields than Login component:
     * - username: User's chosen username
     * - email: User's email address
     * - password: User's password
     * - confirmPassword: Password confirmation (not sent to backend)
     * - roles: Array of selected roles ['user', 'admin']
     */
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        roles: ['user'] // Default role
    });

    /**
     * VALIDATION ERRORS STATE
     *
     * Object storing field-specific errors
     * Example:
     * {
     *   username: 'Username is required',
     *   email: 'Invalid email format',
     *   password: 'Password must be at least 8 characters'
     * }
     *
     * Allows showing error under each field
     */
    const [errors, setErrors] = useState({});

    /**
     * GENERAL ERROR STATE
     *
     * For errors not tied to specific field
     * Example: "Username already exists"
     */
    const [error, setError] = useState('');

    /**
     * SUCCESS MESSAGE STATE
     *
     * Shows confirmation message before redirect
     */
    const [success, setSuccess] = useState('');

    /**
     * LOADING STATE
     */
    const [loading, setLoading] = useState(false);

    // ============================================
    // HOOKS
    // ============================================

    const { signup } = useAuth();
    const navigate = useNavigate();

    // ============================================
    // VALIDATION FUNCTIONS
    // ============================================

    /**
     * VALIDATE EMAIL FORMAT
     *
     * Uses regular expression to check email format
     *
     * Regex explanation:
     * ^[^\s@]+   - Start with one or more non-whitespace, non-@ characters
     * @          - Must have @ symbol
     * [^\s@]+    - Domain name (non-whitespace, non-@)
     * \.         - Must have dot
     * [^\s@]+$   - Top-level domain (com, org, etc.)
     *
     * Valid: john@example.com, user.name@domain.co.uk
     * Invalid: john@, @example.com, john example@test.com
     */
    const isValidEmail = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    };

    /**
     * VALIDATE PASSWORD STRENGTH
     *
     * Checks if password meets security requirements
     *
     * Requirements:
     * - At least 8 characters
     * - Contains uppercase letter
     * - Contains lowercase letter
     * - Contains number
     * - Contains special character (@$!%*?&)
     *
     * Returns:
     * - null if valid
     * - Error message if invalid
     */
    const validatePassword = (password) => {
        if (password.length < 8) {
            return 'Password must be at least 8 characters';
        }
        if (!/[A-Z]/.test(password)) {
            return 'Password must contain at least one uppercase letter';
        }
        if (!/[a-z]/.test(password)) {
            return 'Password must contain at least one lowercase letter';
        }
        if (!/[0-9]/.test(password)) {
            return 'Password must contain at least one number';
        }
        if (!/[@$!%*?&]/.test(password)) {
            return 'Password must contain at least one special character (@$!%*?&)';
        }
        return null;
    };

    /**
     * VALIDATE ALL FIELDS
     *
     * Runs all validation checks before submission
     *
     * Process:
     * 1. Create empty errors object
     * 2. Check each field
     * 3. Add error messages for invalid fields
     * 4. Return errors object
     *
     * Form is valid if returned object is empty
     */
    const validateForm = () => {
        const newErrors = {};

        // Username validation
        if (!formData.username.trim()) {
            newErrors.username = 'Username is required';
        } else if (formData.username.length < 3) {
            newErrors.username = 'Username must be at least 3 characters';
        } else if (formData.username.length > 20) {
            newErrors.username = 'Username cannot exceed 20 characters';
        }

        // Email validation
        if (!formData.email.trim()) {
            newErrors.email = 'Email is required';
        } else if (!isValidEmail(formData.email)) {
            newErrors.email = 'Invalid email format';
        }

        // Password validation
        const passwordError = validatePassword(formData.password);
        if (passwordError) {
            newErrors.password = passwordError;
        }

        // Confirm password validation
        if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }

        // Roles validation
        if (formData.roles.length === 0) {
            newErrors.roles = 'Please select at least one role';
        }

        return newErrors;
    };

    // ============================================
    // EVENT HANDLERS
    // ============================================

    /**
     * HANDLE TEXT INPUT CHANGE
     *
     * Same as Login component
     * Updates formData when user types
     */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });

        // Clear field-specific error when user starts typing
        if (errors[name]) {
            setErrors({
                ...errors,
                [name]: ''
            });
        }
    };

    /**
     * HANDLE ROLE CHECKBOX CHANGE
     *
     * Manages role selection checkboxes
     *
     * How it works:
     * 1. Get current roles array from state
     * 2. Check if checkbox was checked or unchecked
     * 3. If checked: Add role to array
     * 4. If unchecked: Remove role from array
     * 5. Update state with new roles array
     *
     * Example:
     * Current roles: ['user']
     * User checks 'admin' checkbox
     * New roles: ['user', 'admin']
     */
    const handleRoleChange = (e) => {
        const { value, checked } = e.target;
        let newRoles = [...formData.roles]; // Copy current roles

        if (checked) {
            // Add role if checked
            if (!newRoles.includes(value)) {
                newRoles.push(value);
            }
        } else {
            // Remove role if unchecked
            newRoles = newRoles.filter(role => role !== value);
        }

        setFormData({
            ...formData,
            roles: newRoles
        });
    };

    /**
     * HANDLE FORM SUBMIT
     *
     * Process:
     * 1. Prevent default form submission
     * 2. Clear previous errors/messages
     * 3. Validate form (client-side)
     * 4. If validation fails: Show errors, stop
     * 5. If validation passes: Submit to backend
     * 6. Handle backend response
     * 7. Show success and redirect OR show backend errors
     */
    const handleSubmit = async (e) => {
        e.preventDefault();

        // Clear previous messages
        setError('');
        setSuccess('');

        // Client-side validation
        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            // Validation failed - show errors
            setErrors(validationErrors);
            return;
        }

        // Clear all errors
        setErrors({});
        setLoading(true);

        try {
            // Prepare data for backend (exclude confirmPassword)
            const signupData = {
                username: formData.username,
                email: formData.email,
                password: formData.password,
                roles: formData.roles
            };

            // Call signup function from AuthContext
            const success = await signup(signupData);

            if (success) {
                // Signup successful
                setSuccess('Account created successfully! Redirecting to login...');

                // Redirect to login after 2 seconds
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            }
        } catch (err) {
            console.error('Signup error:', err);

            /**
             * BACKEND ERROR HANDLING
             *
             * Backend can return two types of errors:
             *
             * 1. Validation errors (400 Bad Request)
             *    Response body:
             *    {
             *      "errors": {
             *        "username": "Username is already taken",
             *        "email": "Email is already registered"
             *      }
             *    }
             *
             * 2. General errors
             *    Response body:
             *    {
             *      "message": "Registration failed"
             *    }
             */
            if (err.response && err.response.data) {
                const responseData = err.response.data;

                // Check for field-specific validation errors
                if (responseData.errors) {
                    setErrors(responseData.errors);
                } else if (responseData.message) {
                    setError(responseData.message);
                } else {
                    setError('Signup failed. Please try again.');
                }
            } else if (err.request) {
                setError('Unable to connect to server. Please try again.');
            } else {
                setError('Signup failed. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    // ============================================
    // RENDER
    // ============================================

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2 className="auth-title">Create an Account</h2>
                <p className="auth-subtitle">
                    Join us today! Fill in your details below.
                </p>

                {/* General Error Display */}
                {error && (
                    <div className="error-message">
                        <span className="error-icon">⚠️</span>
                        {error}
                    </div>
                )}

                {/* Success Message */}
                {success && (
                    <div className="success-message">
                        <span className="success-icon">✓</span>
                        {success}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="auth-form">

                    {/* Username Field */}
                    <div className="form-group">
                        <label htmlFor="username" className="form-label">
                            Username *
                        </label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            className={`form-input ${errors.username ? 'input-error' : ''}`}
                            placeholder="Choose a username"
                            disabled={loading}
                        />
                        {/*
                            FIELD-SPECIFIC ERROR

                            Shows error message under specific field
                            Only renders if errors.username exists
                        */}
                        {errors.username && (
                            <span className="field-error">{errors.username}</span>
                        )}
                    </div>

                    {/* Email Field */}
                    <div className="form-group">
                        <label htmlFor="email" className="form-label">
                            Email *
                        </label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            className={`form-input ${errors.email ? 'input-error' : ''}`}
                            placeholder="Enter your email"
                            disabled={loading}
                        />
                        {errors.email && (
                            <span className="field-error">{errors.email}</span>
                        )}
                    </div>

                    {/* Password Field */}
                    <div className="form-group">
                        <label htmlFor="password" className="form-label">
                            Password *
                        </label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            className={`form-input ${errors.password ? 'input-error' : ''}`}
                            placeholder="Create a strong password"
                            disabled={loading}
                        />
                        {errors.password && (
                            <span className="field-error">{errors.password}</span>
                        )}
                        {/* Password hint */}
                        <small className="form-hint">
                            Must be 8+ characters with uppercase, lowercase, number, and special character
                        </small>
                    </div>

                    {/* Confirm Password Field */}
                    <div className="form-group">
                        <label htmlFor="confirmPassword" className="form-label">
                            Confirm Password *
                        </label>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            className={`form-input ${errors.confirmPassword ? 'input-error' : ''}`}
                            placeholder="Re-enter your password"
                            disabled={loading}
                        />
                        {errors.confirmPassword && (
                            <span className="field-error">{errors.confirmPassword}</span>
                        )}
                    </div>

                    {/* Role Selection */}
                    {/*
                        CHECKBOX GROUP

                        Allows selecting multiple roles
                        Each checkbox has:
                        - value: Role name ('user', 'admin')
                        - checked: Whether role is in formData.roles array
                        - onChange: Calls handleRoleChange

                        checked={formData.roles.includes('user')}
                        - Returns true if 'user' is in roles array
                        - Checkbox appears checked
                    */}
                    <div className="form-group">
                        <label className="form-label">Roles *</label>
                        <div className="checkbox-group">
                            <label className="checkbox-label">
                                <input
                                    type="checkbox"
                                    value="user"
                                    checked={formData.roles.includes('user')}
                                    onChange={handleRoleChange}
                                    disabled={loading}
                                />
                                <span>User</span>
                            </label>
                            <label className="checkbox-label">
                                <input
                                    type="checkbox"
                                    value="admin"
                                    checked={formData.roles.includes('admin')}
                                    onChange={handleRoleChange}
                                    disabled={loading}
                                />
                                <span>Admin</span>
                            </label>
                        </div>
                        {errors.roles && (
                            <span className="field-error">{errors.roles}</span>
                        )}
                    </div>

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={loading}
                        className={`btn btn-primary ${loading ? 'btn-loading' : ''}`}
                    >
                        {loading ? 'Creating Account...' : 'Sign Up'}
                    </button>

                    {/* Login Link */}
                    <div className="auth-footer">
                        <p>
                            Already have an account?{' '}
                            <Link to="/login" className="auth-link">
                                Login here
                            </Link>
                        </p>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Signup;

/*
 * ============================================
 * VALIDATION FLOW
 * ============================================
 *
 * Client-Side Validation (this component):
 * 1. User fills form
 * 2. User clicks "Sign Up"
 * 3. validateForm() runs
 * 4. If errors found: Display errors, stop submission
 * 5. If no errors: Send request to backend
 *
 * Backend Validation (Spring Boot):
 * 6. Backend receives request
 * 7. @Valid annotation triggers Bean Validation
 * 8. Checks @NotBlank, @Email, @Size, etc.
 * 9. If validation fails: Returns 400 with error details
 * 10. If validation passes: Creates user
 *
 * Frontend Error Handling:
 * 11. Receives 400 response
 * 12. Extracts error details from response.data.errors
 * 13. Sets errors state
 * 14. Component re-renders showing errors
 *
 * ============================================
 * STATE MANAGEMENT EXAMPLE
 * ============================================
 *
 * Initial State:
 * formData: {
 *   username: '',
 *   email: '',
 *   password: '',
 *   confirmPassword: '',
 *   roles: ['user']
 * }
 *
 * After User Types:
 * formData: {
 *   username: 'johndoe',
 *   email: 'john@example.com',
 *   password: 'MyPass123!',
 *   confirmPassword: 'MyPass123!',
 *   roles: ['user', 'admin']
 * }
 *
 * Data Sent to Backend:
 * {
 *   username: 'johndoe',
 *   email: 'john@example.com',
 *   password: 'MyPass123!',
 *   roles: ['user', 'admin']
 * }
 * Note: confirmPassword NOT sent (only used for client-side validation)
 *
 * ============================================
 * ERROR HANDLING EXAMPLES
 * ============================================
 *
 * Example 1: Username Too Short
 * User enters: "jo"
 * Validation error: "Username must be at least 3 characters"
 * Result: Error shown under username field, form not submitted
 *
 * Example 2: Invalid Email
 * User enters: "invalid-email"
 * Validation error: "Invalid email format"
 * Result: Error shown under email field
 *
 * Example 3: Weak Password
 * User enters: "password"
 * Validation error: "Password must contain at least one uppercase letter"
 * Result: Error shown under password field
 *
 * Example 4: Passwords Don't Match
 * Password: "MyPass123!"
 * Confirm: "MyPass123"
 * Validation error: "Passwords do not match"
 * Result: Error shown under confirm password field
 *
 * Example 5: Username Already Exists (Backend Error)
 * User enters: username "admin" (already exists)
 * Backend returns: 400 Bad Request
 * Response: { "errors": { "username": "Username is already taken" } }
 * Result: Backend error shown under username field
 *
 * ============================================
 * CHECKBOX HANDLING EXPLAINED
 * ============================================
 *
 * Initial state:
 * roles: ['user']
 *
 * User checks "admin" checkbox:
 * 1. handleRoleChange called with value='admin', checked=true
 * 2. Create copy: newRoles = ['user']
 * 3. checked is true, so add 'admin'
 * 4. newRoles becomes ['user', 'admin']
 * 5. setFormData updates roles
 * 6. Component re-renders
 * 7. Both checkboxes now checked
 *
 * User unchecks "user" checkbox:
 * 1. handleRoleChange called with value='user', checked=false
 * 2. Create copy: newRoles = ['user', 'admin']
 * 3. checked is false, so filter out 'user'
 * 4. newRoles becomes ['admin']
 * 5. setFormData updates roles
 * 6. Component re-renders
 * 7. Only "admin" checkbox checked
 */
