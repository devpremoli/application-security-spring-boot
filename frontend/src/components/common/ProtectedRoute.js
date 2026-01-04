import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/**
 * PROTECTED ROUTE COMPONENT
 *
 * Wrapper component that protects routes from unauthorized access
 *
 * Demonstrates:
 * 1. Route guards / authentication guards
 * 2. Conditional rendering based on auth state
 * 3. Programmatic navigation (redirect)
 * 4. Role-based access control (RBAC)
 * 5. Higher-order component pattern
 *
 * KEY LEARNING CONCEPTS:
 * - Authentication: Is user logged in?
 * - Authorization: Does user have required role?
 * - Redirect: Send unauthenticated users to login
 * - Children prop: Render wrapped component
 *
 * USAGE:
 * <ProtectedRoute>
 *   <Dashboard />
 * </ProtectedRoute>
 *
 * <ProtectedRoute requiredRole="admin">
 *   <AdminPanel />
 * </ProtectedRoute>
 */

const ProtectedRoute = ({ children, requiredRole }) => {
    // ============================================
    // AUTHENTICATION CHECK
    // ============================================

    /**
     * USE AUTH CONTEXT
     *
     * Get authentication state and functions
     * - isAuthenticated(): Returns true if user logged in
     * - hasRole(role): Returns true if user has specific role
     */
    const { isAuthenticated, hasRole } = useAuth();

    /**
     * CHECK AUTHENTICATION
     *
     * If user not logged in, redirect to login page
     *
     * <Navigate> component:
     * - From react-router-dom
     * - Redirects to specified path
     * - replace=true: Replace history entry (back button won't go to protected route)
     * - Alternative: <Redirect to="/login" /> in older React Router versions
     */
    if (!isAuthenticated()) {
        return <Navigate to="/login" replace />;
    }

    // ============================================
    // ROLE-BASED ACCESS CONTROL
    // ============================================

    /**
     * CHECK REQUIRED ROLE
     *
     * If route requires specific role:
     * - Check if user has that role
     * - If not, redirect to unauthorized page
     *
     * Example:
     * <ProtectedRoute requiredRole="admin">
     *   Only admins can access this
     * </ProtectedRoute>
     *
     * Optional feature - can be removed if not needed
     */
    if (requiredRole && !hasRole(requiredRole)) {
        return <Navigate to="/unauthorized" replace />;
    }

    // ============================================
    // RENDER PROTECTED CONTENT
    // ============================================

    /**
     * RENDER CHILDREN
     *
     * If all checks pass, render the protected component
     *
     * {children} is whatever was wrapped by ProtectedRoute:
     *
     * <ProtectedRoute>
     *   <Dashboard />      ← This is {children}
     * </ProtectedRoute>
     *
     * Result: <Dashboard /> renders
     */
    return children;
};

export default ProtectedRoute;

/*
 * ============================================
 * HOW IT WORKS
 * ============================================
 *
 * Flow for authenticated user:
 * 1. User navigates to /dashboard
 * 2. App.js renders: <ProtectedRoute><Dashboard /></ProtectedRoute>
 * 3. ProtectedRoute checks isAuthenticated()
 * 4. User is logged in → returns true
 * 5. ProtectedRoute returns {children} (Dashboard component)
 * 6. Dashboard renders
 *
 * Flow for unauthenticated user:
 * 1. User navigates to /dashboard
 * 2. App.js renders: <ProtectedRoute><Dashboard /></ProtectedRoute>
 * 3. ProtectedRoute checks isAuthenticated()
 * 4. User not logged in → returns false
 * 5. ProtectedRoute returns <Navigate to="/login" />
 * 6. React Router navigates to /login
 * 7. Login component renders
 *
 * ============================================
 * USAGE IN APP.JS
 * ============================================
 *
 * import ProtectedRoute from './components/common/ProtectedRoute';
 * import Dashboard from './pages/Dashboard';
 *
 * function App() {
 *   return (
 *     <Routes>
 *       <Route path="/login" element={<Login />} />
 *       <Route path="/dashboard" element={
 *         <ProtectedRoute>
 *           <Dashboard />
 *         </ProtectedRoute>
 *       } />
 *     </Routes>
 *   );
 * }
 *
 * ============================================
 * AUTHENTICATION FLOW
 * ============================================
 *
 * Initial Page Load:
 * 1. User opens app (/)
 * 2. AuthContext loads user from localStorage
 * 3. If user exists: User is authenticated
 * 4. If no user: User is not authenticated
 *
 * After Login:
 * 1. User submits login form
 * 2. Login component calls login() from AuthContext
 * 3. AuthContext saves user to localStorage
 * 4. AuthContext updates user state
 * 5. isAuthenticated() now returns true
 * 6. ProtectedRoute allows access
 * 7. User redirected to dashboard
 *
 * After Logout:
 * 1. User clicks logout
 * 2. logout() called from AuthContext
 * 3. AuthContext removes user from localStorage
 * 4. AuthContext sets user to null
 * 5. isAuthenticated() now returns false
 * 6. ProtectedRoute redirects to login
 *
 * ============================================
 * ROLE-BASED ACCESS CONTROL (RBAC)
 * ============================================
 *
 * Example: Admin-only route
 *
 * <ProtectedRoute requiredRole="admin">
 *   <AdminPanel />
 * </ProtectedRoute>
 *
 * Flow:
 * 1. User (john) tries to access /admin
 * 2. ProtectedRoute checks isAuthenticated()
 * 3. John is logged in → passes
 * 4. ProtectedRoute checks hasRole('admin')
 * 5. John has roles: ['user'] → fails
 * 6. Redirect to /unauthorized
 *
 * Flow for admin user:
 * 1. User (admin) tries to access /admin
 * 2. ProtectedRoute checks isAuthenticated()
 * 3. Admin is logged in → passes
 * 4. ProtectedRoute checks hasRole('admin')
 * 5. Admin has roles: ['user', 'admin'] → passes
 * 6. AdminPanel renders
 *
 * ============================================
 * MULTIPLE REQUIRED ROLES
 * ============================================
 *
 * Current implementation: Single role
 * Extension for multiple roles:
 *
 * const ProtectedRoute = ({ children, requiredRoles = [] }) => {
 *   const { isAuthenticated, hasAnyRole } = useAuth();
 *
 *   if (!isAuthenticated()) {
 *     return <Navigate to="/login" replace />;
 *   }
 *
 *   if (requiredRoles.length > 0 && !hasAnyRole(requiredRoles)) {
 *     return <Navigate to="/unauthorized" replace />;
 *   }
 *
 *   return children;
 * };
 *
 * Usage:
 * <ProtectedRoute requiredRoles={['admin', 'moderator']}>
 *   <ModeratorPanel />
 * </ProtectedRoute>
 *
 * ============================================
 * LOADING STATE HANDLING
 * ============================================
 *
 * Current implementation: Assumes auth state is ready
 *
 * Better approach with loading state:
 *
 * const ProtectedRoute = ({ children, requiredRole }) => {
 *   const { isAuthenticated, hasRole, loading } = useAuth();
 *
 *   if (loading) {
 *     return <div>Loading...</div>;
 *   }
 *
 *   if (!isAuthenticated()) {
 *     return <Navigate to="/login" replace />;
 *   }
 *
 *   if (requiredRole && !hasRole(requiredRole)) {
 *     return <Navigate to="/unauthorized" replace />;
 *   }
 *
 *   return children;
 * };
 *
 * ============================================
 * ALTERNATIVE APPROACHES
 * ============================================
 *
 * 1. Route-level protection (in App.js):
 *    - Check auth in parent Route
 *    - Simpler but less reusable
 *
 * 2. useEffect redirect:
 *    - Check auth in component
 *    - Call navigate() if unauthorized
 *    - Less clean, causes flash of content
 *
 * 3. Custom hook:
 *    - useRequireAuth() hook
 *    - Call in every protected component
 *    - More verbose
 *
 * 4. Higher-order component:
 *    - withAuth() HOC
 *    - Wraps component function
 *    - Older pattern, less common now
 *
 * ============================================
 * SECURITY CONSIDERATIONS
 * ============================================
 *
 * Frontend protection is NOT security:
 * - User can bypass by modifying localStorage
 * - User can modify JavaScript code
 * - User can call APIs directly
 *
 * Real security is on backend:
 * - JWT validation on every request
 * - Role checking in controllers
 * - Database-level permissions
 *
 * Frontend protection is for UX:
 * - Don't show features user can't use
 * - Provide clear login flow
 * - Better user experience
 *
 * Always validate on backend!
 *
 * ============================================
 * COMMON PITFALLS
 * ============================================
 *
 * 1. Forgetting to check auth on backend
 *    - Frontend can be bypassed
 *    - Always validate JWT
 *
 * 2. Not handling token expiration
 *    - Token expires, user still shows as logged in
 *    - API calls fail with 401
 *    - Should redirect to login
 *
 * 3. Race condition on page load
 *    - AuthContext still loading
 *    - ProtectedRoute redirects to login
 *    - Then auth loads and user is logged in
 *    - Solution: Show loading state
 *
 * 4. Infinite redirect loop
 *    - Protected route redirects to login
 *    - Login is also protected
 *    - Infinite loop
 *    - Solution: Make login public
 *
 * 5. Back button issues
 *    - User logs out
 *    - Presses back button
 *    - Sees protected page (cached)
 *    - Solution: Use replace=true
 */
