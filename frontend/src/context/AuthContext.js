/**
 * AUTHENTICATION CONTEXT
 *
 * This file manages authentication state for the entire application
 *
 * KEY LEARNING CONCEPTS:
 * 1. React Context API - Global state management
 * 2. Custom hooks - useAuth hook
 * 3. LocalStorage - Persisting authentication
 * 4. Protected routes - Role-based access
 * 5. State management - Login/logout flow
 *
 * ============================================
 * WHAT IS REACT CONTEXT?
 * ============================================
 *
 * Context provides a way to pass data through the component tree
 * without having to pass props down manually at every level
 *
 * Problem without Context:
 * App → Header → UserMenu → UserName
 *   ↓ (pass user prop through each component)
 *
 * Solution with Context:
 * App (provides user) → Any component can access user
 *
 * Use cases:
 * - Authentication state (current user, isLoggedIn)
 * - Theme (dark/light mode)
 * - Language/locale
 * - Shopping cart
 */

import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI, getErrorMessage } from '../services/api';

/**
 * CREATE CONTEXT
 *
 * Creates a new context object
 * This will hold our authentication state and functions
 *
 * Initially undefined, will be set by AuthProvider
 */
const AuthContext = createContext(undefined);

/**
 * AUTH PROVIDER COMPONENT
 *
 * Wraps the entire application
 * Provides authentication state and functions to all children
 *
 * Usage in App.js:
 * <AuthProvider>
 *   <App />
 * </AuthProvider>
 *
 * Any component inside can access auth with useAuth() hook
 */
export const AuthProvider = ({ children }) => {
  /**
   * ============================================
   * STATE MANAGEMENT
   * ============================================
   */

  /**
   * USER STATE
   *
   * Stores current logged-in user information
   * null when not logged in
   *
   * User object structure:
   * {
   *   id: 1,
   *   username: "john",
   *   email: "john@example.com",
   *   roles: ["ROLE_USER", "ROLE_ADMIN"]
   * }
   *
   * useState hook:
   * - Returns [value, setValue] pair
   * - value: current state
   * - setValue: function to update state
   * - null: initial value
   */
  const [user, setUser] = useState(null);

  /**
   * LOADING STATE
   *
   * Tracks whether an auth operation is in progress
   * Used to:
   * - Show loading spinners
   * - Disable buttons during login/signup
   * - Prevent duplicate submissions
   */
  const [loading, setLoading] = useState(false);

  /**
   * ERROR STATE
   *
   * Stores error messages from auth operations
   * null when no error
   * String message when error occurs
   */
  const [error, setError] = useState(null);

  /**
   * ============================================
   * INITIALIZATION
   * ============================================
   *
   * useEffect runs when component mounts
   * Used to restore user from localStorage
   *
   * Why restore from localStorage?
   * - User refreshes page
   * - User closes and reopens browser
   * - Want to stay logged in
   *
   * Without this: User would be logged out on every page refresh!
   */
  useEffect(() => {
    /**
     * LOAD USER FROM LOCALSTORAGE
     *
     * localStorage persists data across browser sessions
     * Data is stored as strings, need to parse JSON
     */
    const loadUser = () => {
      try {
        // Get user JSON string from localStorage
        const storedUser = localStorage.getItem('user');

        if (storedUser) {
          // Parse JSON string to object
          const userData = JSON.parse(storedUser);
          setUser(userData);
          console.log('👤 User restored from localStorage:', userData.username);
        }
      } catch (error) {
        // Invalid JSON in localStorage, clear it
        console.error('Failed to parse stored user:', error);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      }
    };

    loadUser();
  }, []); // Empty dependency array = run once on mount

  /**
   * ============================================
   * LOGIN FUNCTION
   * ============================================
   *
   * Authenticates user with backend
   * On success: Stores token and user data
   *
   * Flow:
   * 1. User submits login form
   * 2. Component calls login(username, password)
   * 3. API request to /api/auth/signin
   * 4. Backend validates credentials
   * 5. Backend returns JWT token + user info
   * 6. Store token and user in localStorage
   * 7. Update user state
   * 8. User is now logged in!
   */
  const login = async (username, password) => {
    try {
      // Set loading state (show spinner)
      setLoading(true);
      // Clear any previous errors
      setError(null);

      console.log('🔐 Attempting login for:', username);

      /**
       * API CALL
       *
       * authAPI.login() from services/api.js
       * Makes POST request to /api/auth/signin
       *
       * await: Wait for promise to resolve
       * Returns response object with data
       */
      const response = await authAPI.login(username, password);

      /**
       * EXTRACT RESPONSE DATA
       *
       * Response structure (from Spring Boot):
       * {
       *   data: {
       *     token: "eyJhbGc...",
       *     type: "Bearer",
       *     id: 1,
       *     username: "john",
       *     email: "john@example.com",
       *     roles: ["ROLE_USER"]
       *   }
       * }
       */
      const { token, id, username: userName, email, roles } = response.data;

      /**
       * STORE TOKEN IN LOCALSTORAGE
       *
       * localStorage.setItem(key, value)
       * - Stores data as strings
       * - Persists across browser sessions
       * - Accessible on all pages of same origin
       *
       * Token is used by API interceptor
       * Added to Authorization header on every request
       */
      localStorage.setItem('token', token);

      /**
       * CREATE USER OBJECT
       *
       * Store only necessary user info
       * Don't store sensitive data in localStorage!
       * Never store passwords in localStorage!
       */
      const userData = {
        id,
        username: userName,
        email,
        roles,
      };

      /**
       * STORE USER IN LOCALSTORAGE
       *
       * JSON.stringify(): Convert object to JSON string
       * localStorage only stores strings
       */
      localStorage.setItem('user', JSON.stringify(userData));

      /**
       * UPDATE STATE
       *
       * setUser() triggers re-render
       * All components using useAuth() will see updated user
       * Protected routes will now allow access
       */
      setUser(userData);

      console.log('✅ Login successful:', userData.username);
      console.log('   Roles:', userData.roles);

      // Return true to indicate success
      return true;

    } catch (err) {
      /**
       * ERROR HANDLING
       *
       * Common login errors:
       * - 401: Invalid credentials (wrong username/password)
       * - 400: Validation error (missing fields)
       * - 500: Server error
       * - Network error: Server not running
       */

      const errorMessage = getErrorMessage(err);
      console.error('❌ Login failed:', errorMessage);

      // Set error state (component can display it)
      setError(errorMessage);

      // Return false to indicate failure
      return false;

    } finally {
      /**
       * FINALLY BLOCK
       *
       * Always runs, regardless of success or error
       * Used to clean up loading state
       *
       * Why? Don't want loading spinner stuck on screen
       */
      setLoading(false);
    }
  };

  /**
   * ============================================
   * SIGNUP FUNCTION
   * ============================================
   *
   * Registers new user account
   * Similar to login but doesn't automatically log in
   */
  const signup = async (username, email, password) => {
    try {
      setLoading(true);
      setError(null);

      console.log('📝 Attempting signup for:', username);

      /**
       * API CALL
       *
       * authAPI.signup() from services/api.js
       * Makes POST request to /api/auth/signup
       *
       * Request body:
       * {
       *   "username": "john",
       *   "email": "john@example.com",
       *   "password": "password123"
       * }
       */
      const response = await authAPI.signup(username, email, password);

      console.log('✅ Signup successful:', response.data.message);

      /**
       * AFTER SIGNUP
       *
       * Option 1: Automatically log in user
       * return await login(username, password);
       *
       * Option 2: Redirect to login page (current approach)
       * Let user login manually
       * More explicit, user knows they need to login
       */

      return true;

    } catch (err) {
      /**
       * SIGNUP ERROR HANDLING
       *
       * Common errors:
       * - 400: Username/email already taken
       * - 400: Validation error (weak password, invalid email)
       * - 500: Server error
       */
      const errorMessage = getErrorMessage(err);
      console.error('❌ Signup failed:', errorMessage);
      setError(errorMessage);
      return false;

    } finally {
      setLoading(false);
    }
  };

  /**
   * ============================================
   * LOGOUT FUNCTION
   * ============================================
   *
   * Logs out current user
   * Clears all authentication data
   */
  const logout = () => {
    console.log('👋 Logging out user:', user?.username);

    /**
     * CLEAR LOCALSTORAGE
     *
     * Remove both token and user data
     * Important: Don't leave stale data in localStorage!
     */
    localStorage.removeItem('token');
    localStorage.removeItem('user');

    /**
     * CLEAR STATE
     *
     * setUser(null) triggers re-render
     * Protected routes will redirect to login
     * User-specific UI will hide
     */
    setUser(null);
    setError(null);

    console.log('✅ Logout successful');
  };

  /**
   * ============================================
   * HELPER FUNCTIONS
   * ============================================
   */

  /**
   * CHECK IF USER IS LOGGED IN
   *
   * Simple boolean check
   * Used in components to conditionally render content
   */
  const isAuthenticated = () => {
    return user !== null;
  };

  /**
   * CHECK IF USER HAS SPECIFIC ROLE
   *
   * @param {string} role - Role to check (e.g., "ROLE_ADMIN")
   * @returns {boolean} - true if user has the role
   *
   * Usage:
   * if (hasRole("ROLE_ADMIN")) {
   *   // Show admin panel
   * }
   */
  const hasRole = (role) => {
    if (!user || !user.roles) {
      return false;
    }
    return user.roles.includes(role);
  };

  /**
   * CHECK IF USER HAS ANY OF THE SPECIFIED ROLES
   *
   * @param {Array<string>} roles - Array of roles to check
   * @returns {boolean} - true if user has at least one role
   *
   * Usage:
   * if (hasAnyRole(["ROLE_ADMIN", "ROLE_MODERATOR"])) {
   *   // Show moderator features
   * }
   */
  const hasAnyRole = (roles) => {
    if (!user || !user.roles) {
      return false;
    }
    return roles.some(role => user.roles.includes(role));
  };

  /**
   * ============================================
   * CONTEXT VALUE
   * ============================================
   *
   * Object containing all state and functions
   * Provided to all children components
   *
   * Components access these via useAuth() hook
   */
  const value = {
    // State
    user,              // Current user object or null
    loading,           // Is auth operation in progress?
    error,             // Current error message or null

    // Functions
    login,             // Login function
    signup,            // Signup function
    logout,            // Logout function
    isAuthenticated,   // Check if logged in
    hasRole,           // Check specific role
    hasAnyRole,        // Check any of roles
    setError,          // Manually set error (for component use)
  };

  /**
   * PROVIDE CONTEXT
   *
   * AuthContext.Provider makes value available to all children
   *
   * {children} is the entire app or portion wrapped by AuthProvider
   */
  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * ============================================
 * CUSTOM HOOK: useAuth
 * ============================================
 *
 * Custom hook to access auth context
 * Simplifies component code
 *
 * Without custom hook:
 * const authContext = useContext(AuthContext);
 * const user = authContext.user;
 * const login = authContext.login;
 *
 * With custom hook:
 * const { user, login } = useAuth();
 *
 * Much cleaner!
 */
export const useAuth = () => {
  const context = useContext(AuthContext);

  /**
   * ERROR CHECK
   *
   * If useAuth() is called outside AuthProvider,
   * context will be undefined
   *
   * This error helps catch setup mistakes
   */
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
};

/**
 * ============================================
 * USAGE EXAMPLES
 * ============================================
 *
 * Example 1: Setup (in index.js or App.js)
 *
 * import { AuthProvider } from './context/AuthContext';
 *
 * ReactDOM.render(
 *   <AuthProvider>
 *     <App />
 *   </AuthProvider>,
 *   document.getElementById('root')
 * );
 *
 * ---
 *
 * Example 2: Use in Login component
 *
 * import { useAuth } from './context/AuthContext';
 *
 * function Login() {
 *   const { login, loading, error } = useAuth();
 *
 *   const handleSubmit = async (e) => {
 *     e.preventDefault();
 *     const success = await login(username, password);
 *     if (success) {
 *       navigate('/dashboard');
 *     }
 *   };
 *
 *   return (
 *     <form onSubmit={handleSubmit}>
 *       {error && <div className="error">{error}</div>}
 *       <input type="text" ... />
 *       <button disabled={loading}>
 *         {loading ? 'Logging in...' : 'Login'}
 *       </button>
 *     </form>
 *   );
 * }
 *
 * ---
 *
 * Example 3: Conditional rendering based on auth
 *
 * function Header() {
 *   const { user, logout, isAuthenticated } = useAuth();
 *
 *   return (
 *     <header>
 *       {isAuthenticated() ? (
 *         <>
 *           <span>Welcome, {user.username}!</span>
 *           <button onClick={logout}>Logout</button>
 *         </>
 *       ) : (
 *         <Link to="/login">Login</Link>
 *       )}
 *     </header>
 *   );
 * }
 *
 * ---
 *
 * Example 4: Role-based rendering
 *
 * function AdminPanel() {
 *   const { hasRole } = useAuth();
 *
 *   if (!hasRole('ROLE_ADMIN')) {
 *     return <div>Access Denied</div>;
 *   }
 *
 *   return <div>Admin Panel Content</div>;
 * }
 */
