import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import TodoList from '../components/todos/TodoList';
import './Dashboard.css';

/**
 * DASHBOARD PAGE
 *
 * Main page after login
 * Protected route - only accessible to authenticated users
 *
 * Demonstrates:
 * 1. Accessing user data from auth context
 * 2. Navigation (logout redirects to home)
 * 3. Displaying user-specific information
 * 4. Composing components (includes TodoList)
 * 5. Role-based UI (show/hide based on roles)
 *
 * KEY CONCEPTS:
 * - useAuth: Access global auth state
 * - useNavigate: Programmatic navigation
 * - Component composition: TodoList rendered inside Dashboard
 */

const Dashboard = () => {
    // ============================================
    // HOOKS
    // ============================================

    /**
     * AUTH CONTEXT
     *
     * Access current user data and logout function
     */
    const { user, logout } = useAuth();

    /**
     * NAVIGATION
     *
     * For redirecting after logout
     */
    const navigate = useNavigate();

    // ============================================
    // EVENT HANDLERS
    // ============================================

    /**
     * HANDLE LOGOUT
     *
     * Process:
     * 1. Call logout() from AuthContext
     * 2. AuthContext clears localStorage
     * 3. AuthContext sets user to null
     * 4. Navigate to home page
     *
     * After logout:
     * - User cannot access this page anymore
     * - ProtectedRoute will redirect to login
     */
    const handleLogout = () => {
        logout();
        navigate('/');
    };

    // ============================================
    // RENDER
    // ============================================

    return (
        <div className="dashboard">
            {/* Header / Navbar */}
            <header className="dashboard-header">
                <div className="header-content">
                    {/* App Title */}
                    <div className="header-left">
                        <h2 className="app-title">📝 Todo App</h2>
                    </div>

                    {/* User Info & Logout */}
                    <div className="header-right">
                        {/* User Info */}
                        {/*
                            DISPLAY USER DATA

                            user object from AuthContext:
                            {
                              id: 1,
                              username: "john",
                              email: "john@example.com",
                              roles: ["user", "admin"]
                            }

                            Displays username and email
                        */}
                        <div className="user-info">
                            <div className="user-details">
                                <span className="user-name">{user?.username}</span>
                                <span className="user-email">{user?.email}</span>
                            </div>

                            {/* User Roles */}
                            {/*
                                CONDITIONAL RENDERING - ROLES

                                Only show if user has roles
                                Maps through roles array to create badges
                            */}
                            {user?.roles && user.roles.length > 0 && (
                                <div className="user-roles">
                                    {user.roles.map(role => (
                                        <span key={role} className="role-badge">
                                            {role}
                                        </span>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Logout Button */}
                        <button onClick={handleLogout} className="btn btn-logout">
                            Logout
                        </button>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="dashboard-main">
                {/* Welcome Section */}
                <section className="welcome-section">
                    <h1 className="welcome-title">
                        Welcome back, {user?.username}! 👋
                    </h1>
                    <p className="welcome-subtitle">
                        Manage your todos and stay organized.
                    </p>
                </section>

                {/* Todo List Component */}
                {/*
                    COMPONENT COMPOSITION

                    TodoList is a child component
                    - Handles all todo CRUD operations
                    - Manages its own state
                    - Self-contained functionality

                    Dashboard just renders it
                    - Dashboard doesn't need to know todo logic
                    - Clean separation of concerns
                */}
                <TodoList />
            </main>

            {/* Footer */}
            <footer className="dashboard-footer">
                <p>
                    Spring Boot JWT Security Demo • Built with React & Spring Boot
                </p>
            </footer>
        </div>
    );
};

export default Dashboard;

/*
 * ============================================
 * PAGE LIFECYCLE
 * ============================================
 *
 * 1. User logs in successfully
 * 2. Login component calls navigate('/dashboard')
 * 3. React Router navigates to /dashboard
 * 4. App.js renders Dashboard inside ProtectedRoute
 * 5. ProtectedRoute checks isAuthenticated()
 * 6. User is logged in → allows access
 * 7. Dashboard component renders
 * 8. useAuth() retrieves user from context
 * 9. User info displays in header
 * 10. TodoList component mounts
 * 11. TodoList fetches todos from API
 * 12. Todos display on page
 *
 * ============================================
 * LOGOUT FLOW
 * ============================================
 *
 * 1. User clicks "Logout" button
 * 2. handleLogout() called
 * 3. logout() from AuthContext runs:
 *    - localStorage.removeItem('token')
 *    - localStorage.removeItem('user')
 *    - setUser(null)
 * 4. navigate('/') redirects to home
 * 5. Home page renders
 * 6. If user tries to access /dashboard:
 *    - ProtectedRoute checks isAuthenticated()
 *    - Returns false (user is null)
 *    - Redirects to /login
 *
 * ============================================
 * USER DATA FLOW
 * ============================================
 *
 * AuthContext (Provider):
 * - Stores user state
 * - Provides user data to all children
 *
 * Dashboard (Consumer):
 * - Calls useAuth() hook
 * - Receives current user object
 * - Displays user.username, user.email, user.roles
 *
 * Data doesn't need to be passed through props:
 * - AuthContext wraps entire app
 * - Any component can access user data
 * - No prop drilling needed
 *
 * ============================================
 * COMPONENT HIERARCHY
 * ============================================
 *
 * Dashboard
 * ├── Header
 * │   ├── App Title
 * │   └── User Info
 * │       ├── Username
 * │       ├── Email
 * │       ├── Roles
 * │       └── Logout Button
 * ├── Main Content
 * │   ├── Welcome Section
 * │   └── TodoList
 * │       ├── TodoForm
 * │       └── TodoItem (multiple)
 * └── Footer
 *
 * ============================================
 * ROLE-BASED UI
 * ============================================
 *
 * Current: Shows all roles as badges
 *
 * Example: Admin-only features
 *
 * import { useAuth } from '../context/AuthContext';
 *
 * const { hasRole } = useAuth();
 *
 * {hasRole('admin') && (
 *   <Link to="/admin">
 *     <button>Admin Panel</button>
 *   </Link>
 * )}
 *
 * Only admins see the admin panel link
 *
 * ============================================
 * PROTECTED DATA
 * ============================================
 *
 * TodoList fetches user-specific todos:
 * - GET /api/todos with Authorization header
 * - Backend (TodoController.getAllTodos):
 *   - Extracts user from JWT token
 *   - Queries: todoRepository.findByUserId(userId)
 *   - Returns only current user's todos
 *
 * Security:
 * - User A cannot see User B's todos
 * - Even if User A knows todo ID
 * - Backend enforces data isolation
 *
 * ============================================
 * NAVIGATION PATTERNS
 * ============================================
 *
 * navigate('/') - Basic navigation
 * navigate('/dashboard', { replace: true }) - Replace history
 * navigate(-1) - Go back
 * navigate(1) - Go forward
 *
 * With state:
 * navigate('/dashboard', { state: { message: 'Welcome!' } })
 *
 * Access in target component:
 * const location = useLocation();
 * const message = location.state?.message;
 *
 * ============================================
 * RESPONSIVE DESIGN
 * ============================================
 *
 * Dashboard.css includes mobile styles:
 * - Stacks header elements vertically on small screens
 * - Adjusts font sizes
 * - Optimizes spacing
 *
 * Media query: @media (max-width: 768px)
 *
 * ============================================
 * PERFORMANCE CONSIDERATIONS
 * ============================================
 *
 * Current approach: Simple and straightforward
 *
 * For optimization:
 * 1. Code splitting:
 *    - Lazy load TodoList
 *    - const TodoList = lazy(() => import('./components/todos/TodoList'))
 *    - Reduces initial bundle size
 *
 * 2. Memoization:
 *    - useMemo for computed values
 *    - useCallback for handlers
 *    - Prevents unnecessary re-renders
 *
 * 3. Virtualization:
 *    - For large todo lists (100+)
 *    - react-window or react-virtualized
 *    - Only render visible items
 *
 * ============================================
 * ACCESSIBILITY
 * ============================================
 *
 * Current implementation:
 * - Semantic HTML (header, main, footer)
 * - Clear heading hierarchy (h1, h2)
 * - Descriptive button text
 *
 * Improvements:
 * - Skip to main content link
 * - ARIA labels for user info
 * - Keyboard navigation
 * - Focus management
 * - Screen reader announcements
 *
 * ============================================
 * ERROR HANDLING
 * ============================================
 *
 * Current: TodoList handles its own errors
 *
 * Additional error handling:
 * - Error boundary for unexpected errors
 * - Retry mechanism for failed requests
 * - Offline detection
 * - Session expiration handling
 *
 * Example: Session expired
 * - Todo API call returns 401
 * - Axios interceptor catches error
 * - Redirects to login
 * - Shows "Session expired" message
 */
