import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

/**
 * LOGIN COMPONENT
 *
 * Demonstrates:
 * 1. Form handling in React
 * 2. State management with useState hook
 * 3. API request submission
 * 4. Error handling and display
 * 5. Form validation
 * 6. Navigation after successful login
 * 7. Context usage (useAuth)
 *
 * KEY LEARNING CONCEPTS:
 * - useState: Managing component state (form inputs, errors, loading)
 * - useNavigate: Programmatic navigation after login
 * - useAuth: Accessing global authentication context
 * - Event handlers: onChange, onSubmit
 * - Async/await: Handling API calls
 * - Conditional rendering: Showing errors, loading states
 */

const Login = () => {
    // ============================================
    // STATE MANAGEMENT
    // ============================================

    /**
     * FORM DATA STATE
     *
     * Stores the username and password input values
     *
     * useState returns:
     * - formData: Current state value
     * - setFormData: Function to update state
     *
     * Initial state: { username: '', password: '' }
     */
    const [formData, setFormData] = useState({
        username: '',
        password: ''
    });

    /**
     * ERROR STATE
     *
     * Stores error message from failed login attempts
     * Examples:
     * - "Invalid username or password"
     * - "Network error"
     * - Validation errors from backend
     */
    const [error, setError] = useState('');

    /**
     * LOADING STATE
     *
     * Indicates when API request is in progress
     * Used to:
     * - Disable submit button during request
     * - Show loading spinner
     * - Prevent duplicate submissions
     */
    const [loading, setLoading] = useState(false);

    // ============================================
    // HOOKS
    // ============================================

    /**
     * AUTH CONTEXT
     *
     * Access global authentication functions
     * Provided by AuthProvider in App.js
     *
     * Available functions:
     * - login(username, password)
     * - logout()
     * - isAuthenticated()
     */
    const { login } = useAuth();

    /**
     * NAVIGATION HOOK
     *
     * Used for programmatic navigation
     * After successful login, redirect to dashboard
     *
     * Usage:
     * navigate('/dashboard') - Navigate to dashboard
     * navigate(-1) - Go back
     */
    const navigate = useNavigate();

    // ============================================
    // EVENT HANDLERS
    // ============================================

    /**
     * HANDLE INPUT CHANGE
     *
     * Called when user types in username or password field
     * Updates formData state with new value
     *
     * How it works:
     * 1. Extract name and value from event target
     *    - name: 'username' or 'password' (from input's name attribute)
     *    - value: What user typed
     *
     * 2. Use spread operator to keep existing fields
     *    { ...formData } creates copy of current formData
     *
     * 3. Override specific field with new value
     *    [name]: value - Uses computed property name
     *
     * Example:
     * User types "john" in username field
     * name = 'username', value = 'john'
     * setFormData({ ...formData, username: 'john' })
     * Result: { username: 'john', password: '' }
     */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    };

    /**
     * HANDLE FORM SUBMIT
     *
     * Called when user clicks "Login" button or presses Enter
     * Performs login API request and handles response
     *
     * Process:
     * 1. Prevent default form submission (page reload)
     * 2. Clear any previous errors
     * 3. Validate input (client-side)
     * 4. Set loading state (disable button, show spinner)
     * 5. Call login API through context
     * 6. Handle success: Navigate to dashboard
     * 7. Handle error: Display error message
     * 8. Reset loading state
     *
     * Error Handling:
     * - Network errors: "Unable to connect to server"
     * - 401 Unauthorized: "Invalid username or password"
     * - Validation errors: Display specific field errors
     * - Unexpected errors: Generic error message
     */
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent page reload

        // Clear previous errors
        setError('');

        // Client-side validation
        if (!formData.username || !formData.password) {
            setError('Please enter both username and password');
            return;
        }

        // Start loading
        setLoading(true);

        try {
            // Call login function from AuthContext
            // This will:
            // 1. Send POST request to /api/auth/signin
            // 2. Store JWT token in localStorage
            // 3. Update user state in context
            const success = await login(formData.username, formData.password);

            if (success) {
                // Login successful - redirect to dashboard
                navigate('/dashboard');
            }
        } catch (err) {
            // Login failed - display error
            console.error('Login error:', err);

            // Extract error message
            if (err.response) {
                // Server responded with error status
                setError(err.response.data.message || 'Invalid username or password');
            } else if (err.request) {
                // Request made but no response (network error)
                setError('Unable to connect to server. Please try again.');
            } else {
                // Other errors
                setError('Login failed. Please try again.');
            }
        } finally {
            // Reset loading state (runs whether success or error)
            setLoading(false);
        }
    };

    // ============================================
    // RENDER
    // ============================================

    return (
        <div className="auth-container">
            <div className="auth-card">
                {/* Header */}
                <h2 className="auth-title">Login to Your Account</h2>
                <p className="auth-subtitle">
                    Welcome back! Please enter your credentials.
                </p>

                {/* Error Display */}
                {/*
                    CONDITIONAL RENDERING

                    Only show error box if error exists
                    && operator: Renders right side if left side is truthy

                    If error is empty string: Nothing renders
                    If error has message: Error box renders
                */}
                {error && (
                    <div className="error-message">
                        <span className="error-icon">⚠️</span>
                        {error}
                    </div>
                )}

                {/* Login Form */}
                {/*
                    FORM ELEMENT

                    onSubmit: Called when form is submitted
                    - User clicks submit button
                    - User presses Enter in input field

                    preventDefault() stops default behavior (page reload)
                */}
                <form onSubmit={handleSubmit} className="auth-form">

                    {/* Username Field */}
                    <div className="form-group">
                        <label htmlFor="username" className="form-label">
                            Username
                        </label>
                        {/*
                            INPUT ELEMENT

                            type="text": Regular text input
                            id="username": Links to label's htmlFor
                            name="username": Used in handleChange to identify field
                            value={formData.username}: Controlled component
                            onChange={handleChange}: Updates state on every keystroke
                            required: HTML5 validation (browser-level)
                            disabled={loading}: Disable during API request

                            CONTROLLED COMPONENT:
                            - React state is "single source of truth"
                            - Input value always reflects state
                            - Every change updates state via onChange
                        */}
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            className="form-input"
                            placeholder="Enter your username"
                            required
                            disabled={loading}
                        />
                    </div>

                    {/* Password Field */}
                    <div className="form-group">
                        <label htmlFor="password" className="form-label">
                            Password
                        </label>
                        {/*
                            PASSWORD INPUT

                            type="password": Masks input (shows dots/asterisks)
                            Other attributes same as username field
                        */}
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            className="form-input"
                            placeholder="Enter your password"
                            required
                            disabled={loading}
                        />
                    </div>

                    {/* Submit Button */}
                    {/*
                        BUTTON ELEMENT

                        type="submit": Triggers form onSubmit
                        disabled={loading}: Prevent duplicate submissions
                        className: Dynamic class based on loading state

                        Button content shows:
                        - "Logging in..." when loading is true
                        - "Login" when loading is false
                    */}
                    <button
                        type="submit"
                        disabled={loading}
                        className={`btn btn-primary ${loading ? 'btn-loading' : ''}`}
                    >
                        {loading ? 'Logging in...' : 'Login'}
                    </button>

                    {/* Sign Up Link */}
                    {/*
                        REACT ROUTER LINK

                        <Link> component from react-router-dom
                        Navigates without page reload (SPA navigation)

                        Difference from <a> tag:
                        - <a href="/signup">: Full page reload
                        - <Link to="/signup">: Client-side navigation
                    */}
                    <div className="auth-footer">
                        <p>
                            Don't have an account?{' '}
                            <Link to="/signup" className="auth-link">
                                Sign up here
                            </Link>
                        </p>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Login;

/*
 * ============================================
 * COMPONENT LIFECYCLE
 * ============================================
 *
 * 1. Initial Render:
 *    - Component function executes
 *    - useState initializes state variables
 *    - useAuth accesses context
 *    - JSX is returned and rendered to DOM
 *
 * 2. User Interaction:
 *    - User types in username field
 *    - onChange triggers handleChange
 *    - setFormData updates state
 *    - Component re-renders with new formData
 *    - Input shows updated value
 *
 * 3. Form Submission:
 *    - User clicks Login button
 *    - onSubmit triggers handleSubmit
 *    - setLoading(true) causes re-render (button disabled, shows "Logging in...")
 *    - API request sent
 *    - On success: navigate('/dashboard') redirects user
 *    - On error: setError causes re-render (error message appears)
 *    - setLoading(false) causes final re-render (button enabled again)
 *
 * ============================================
 * STATE FLOW DIAGRAM
 * ============================================
 *
 *     User Types "john"
 *            ↓
 *      onChange event
 *            ↓
 *      handleChange(e)
 *            ↓
 *   setFormData({ username: 'john', password: '' })
 *            ↓
 *      State Updated
 *            ↓
 *     Component Re-renders
 *            ↓
 *   Input shows "john"
 *
 * ============================================
 * API FLOW
 * ============================================
 *
 * 1. User submits form with username: "john", password: "pass123"
 *
 * 2. handleSubmit calls: login('john', 'pass123')
 *
 * 3. AuthContext.login does:
 *    - Calls authAPI.login('john', 'pass123')
 *    - API client sends POST /api/auth/signin
 *    - Request body: { "username": "john", "password": "pass123" }
 *    - Request headers: { "Content-Type": "application/json" }
 *
 * 4. Backend (Spring Boot) receives request:
 *    - AuthController.authenticateUser()
 *    - Validates credentials with BCrypt
 *    - Generates JWT token with JwtUtils
 *    - Returns: { "token": "eyJhbG...", "id": 1, "username": "john", "email": "john@example.com", "roles": ["ROLE_USER"] }
 *
 * 5. AuthContext.login processes response:
 *    - Stores token in localStorage
 *    - Stores user data in localStorage
 *    - Updates user state
 *    - Returns true (success)
 *
 * 6. handleSubmit receives true:
 *    - Calls navigate('/dashboard')
 *    - User redirected to dashboard
 *
 * 7. Future requests automatically include token:
 *    - Axios interceptor reads token from localStorage
 *    - Adds "Authorization: Bearer eyJhbG..." header
 *    - Backend validates token with JwtUtils
 *    - Request succeeds if token valid
 *
 * ============================================
 * TESTING EXAMPLES
 * ============================================
 *
 * Test 1: Successful Login
 * - Enter username: john
 * - Enter password: password123
 * - Click Login
 * - Expected: Redirect to /dashboard
 *
 * Test 2: Invalid Credentials
 * - Enter username: john
 * - Enter password: wrongpassword
 * - Click Login
 * - Expected: Error message "Invalid username or password"
 *
 * Test 3: Empty Fields
 * - Leave username empty
 * - Leave password empty
 * - Click Login
 * - Expected: Error message "Please enter both username and password"
 *
 * Test 4: Network Error
 * - Stop backend server
 * - Enter valid credentials
 * - Click Login
 * - Expected: Error message "Unable to connect to server"
 *
 * ============================================
 * COMMON ISSUES AND SOLUTIONS
 * ============================================
 *
 * Issue 1: "Cannot read property 'username' of undefined"
 * - Cause: formData not initialized
 * - Solution: Ensure useState has initial object
 *
 * Issue 2: Input doesn't update when typing
 * - Cause: Missing value or onChange
 * - Solution: Add both value={formData.username} and onChange={handleChange}
 *
 * Issue 3: Form reloads page on submit
 * - Cause: Missing e.preventDefault()
 * - Solution: Add e.preventDefault() at start of handleSubmit
 *
 * Issue 4: Button stays disabled after error
 * - Cause: setLoading(false) not called
 * - Solution: Use finally block to ensure setLoading(false) always runs
 *
 * Issue 5: Error message doesn't clear
 * - Cause: Not clearing error on new submit
 * - Solution: Add setError('') at start of handleSubmit
 */
