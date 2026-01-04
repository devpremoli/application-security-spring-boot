import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import Home from './pages/Home';
import Login from './components/auth/Login';
import Signup from './components/auth/Signup';
import Dashboard from './pages/Dashboard';
import './App.css';

/**
 * APP COMPONENT
 *
 * Root component of the application
 * Sets up routing and global providers
 *
 * Demonstrates:
 * 1. React Router setup
 * 2. Context Provider pattern
 * 3. Route configuration
 * 4. Protected routes
 * 5. Route redirects
 * 6. 404 handling
 *
 * KEY LEARNING CONCEPTS:
 * - BrowserRouter: Enables client-side routing
 * - Routes & Route: Define route-component mapping
 * - Navigate: Programmatic redirects
 * - Context Provider: Wraps entire app for global state
 *
 * ROUTING STRUCTURE:
 * / → Home (public)
 * /login → Login (public)
 * /signup → Signup (public)
 * /dashboard → Dashboard (protected)
 * /* → Redirect to home (404 handler)
 */

function App() {
  return (
    /**
     * BROWSER ROUTER
     *
     * Enables client-side routing using HTML5 History API
     * Wraps entire application
     *
     * How it works:
     * - Uses browser URL bar for navigation
     * - No page reloads on route changes
     * - Back/forward buttons work
     * - URLs look normal: /login, /dashboard (not /#/login)
     *
     * Alternative: HashRouter
     * - Uses URL hash: /#/login, /#/dashboard
     * - Works without server configuration
     * - Less clean URLs
     */
    <BrowserRouter>
      {/*
        AUTH PROVIDER

        Wraps entire app to provide authentication context
        All components can access user state and auth functions

        Provides:
        - user: Current user object or null
        - login(username, password): Login function
        - signup(data): Signup function
        - logout(): Logout function
        - isAuthenticated(): Check if logged in
        - hasRole(role): Check user role

        Why wrap entire app?
        - Any component can check auth status
        - No prop drilling needed
        - Global state for user data
      */}
      <AuthProvider>
        {/*
          ROUTES CONTAINER

          Contains all route definitions
          Only ONE route will match and render at a time

          Route matching:
          - First match wins (order matters)
          - Exact match by default in v6
          - Wildcard route (*) catches all unmatched
        */}
        <Routes>
          {/*
            HOME ROUTE

            Path: /
            Component: Home
            Access: Public (everyone)

            The landing page of the application
            Shows different content for logged-in vs guests
          */}
          <Route path="/" element={<Home />} />

          {/*
            LOGIN ROUTE

            Path: /login
            Component: Login
            Access: Public (everyone)

            Users can login with username and password
            On success, redirects to /dashboard
          */}
          <Route path="/login" element={<Login />} />

          {/*
            SIGNUP ROUTE

            Path: /signup
            Component: Signup
            Access: Public (everyone)

            New users can create an account
            On success, redirects to /login
          */}
          <Route path="/signup" element={<Signup />} />

          {/*
            DASHBOARD ROUTE (PROTECTED)

            Path: /dashboard
            Component: Dashboard (wrapped in ProtectedRoute)
            Access: Authenticated users only

            ProtectedRoute wrapper:
            - Checks if user is authenticated
            - If yes: Renders Dashboard
            - If no: Redirects to /login

            Dashboard contains:
            - User info display
            - TodoList component
            - Logout button
          */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          {/*
            404 CATCH-ALL ROUTE

            Path: * (wildcard - matches anything)
            Component: Navigate (redirect)
            Behavior: Redirects to home page

            Handles:
            - Typos in URL: /dashbaord → /
            - Deleted routes: /old-page → /
            - Random URLs: /xyz123 → /

            Alternative: Show 404 page
            <Route path="*" element={<NotFound />} />
          */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

/*
 * ============================================
 * ROUTING FLOW EXAMPLES
 * ============================================
 *
 * Example 1: User navigates to /dashboard (not logged in)
 * 1. BrowserRouter detects URL change
 * 2. Routes matches /dashboard route
 * 3. ProtectedRoute component renders
 * 4. ProtectedRoute checks isAuthenticated()
 * 5. User not logged in → returns false
 * 6. ProtectedRoute returns <Navigate to="/login" />
 * 7. React Router navigates to /login
 * 8. Routes matches /login route
 * 9. Login component renders
 *
 * Example 2: User logs in successfully
 * 1. User submits login form
 * 2. Login component calls login(username, password)
 * 3. AuthContext sends API request
 * 4. Backend returns JWT token
 * 5. AuthContext stores token and user in localStorage
 * 6. AuthContext updates user state
 * 7. Login component calls navigate('/dashboard')
 * 8. Routes matches /dashboard
 * 9. ProtectedRoute checks isAuthenticated()
 * 10. User is logged in → returns true
 * 11. ProtectedRoute returns <Dashboard />
 * 12. Dashboard renders
 *
 * Example 3: User types invalid URL
 * 1. User navigates to /invalid-page
 * 2. Routes tries to match /invalid-page
 * 3. No exact match found
 * 4. Wildcard route (*) matches
 * 5. <Navigate to="/" replace /> executes
 * 6. Routes matches / route
 * 7. Home component renders
 *
 * ============================================
 * COMPONENT HIERARCHY
 * ============================================
 *
 * App
 * └── BrowserRouter
 *     └── AuthProvider
 *         └── Routes
 *             ├── Route (/) → Home
 *             ├── Route (/login) → Login
 *             ├── Route (/signup) → Signup
 *             ├── Route (/dashboard) → ProtectedRoute → Dashboard
 *             └── Route (*) → Navigate to /
 *
 * ============================================
 * REACT ROUTER V6 CHANGES
 * ============================================
 *
 * If you're coming from v5, note these changes:
 *
 * 1. <Switch> → <Routes>
 *    v5: <Switch><Route ... /></Switch>
 *    v6: <Routes><Route ... /></Routes>
 *
 * 2. component prop → element prop
 *    v5: <Route path="/" component={Home} />
 *    v6: <Route path="/" element={<Home />} />
 *
 * 3. exact prop removed (exact by default)
 *    v5: <Route exact path="/" ... />
 *    v6: <Route path="/" ... />
 *
 * 4. <Redirect> → <Navigate>
 *    v5: <Redirect to="/login" />
 *    v6: <Navigate to="/login" />
 *
 * 5. useHistory → useNavigate
 *    v5: const history = useHistory(); history.push('/dashboard')
 *    v6: const navigate = useNavigate(); navigate('/dashboard')
 *
 * 6. Nested routes work differently
 *    v5: Routes defined in multiple places
 *    v6: Outlet component for nested routes
 *
 * ============================================
 * ADDING MORE ROUTES
 * ============================================
 *
 * Example: Add admin-only route
 *
 * import AdminPanel from './pages/AdminPanel';
 *
 * <Route
 *   path="/admin"
 *   element={
 *     <ProtectedRoute requiredRole="admin">
 *       <AdminPanel />
 *     </ProtectedRoute>
 *   }
 * />
 *
 * Example: Add profile page
 *
 * import Profile from './pages/Profile';
 *
 * <Route
 *   path="/profile"
 *   element={
 *     <ProtectedRoute>
 *       <Profile />
 *     </ProtectedRoute>
 *   }
 * />
 *
 * Example: Add settings page with nested routes
 *
 * import Settings from './pages/Settings';
 * import AccountSettings from './pages/AccountSettings';
 * import SecuritySettings from './pages/SecuritySettings';
 *
 * <Route
 *   path="/settings"
 *   element={
 *     <ProtectedRoute>
 *       <Settings />
 *     </ProtectedRoute>
 *   }
 * >
 *   <Route path="account" element={<AccountSettings />} />
 *   <Route path="security" element={<SecuritySettings />} />
 * </Route>
 *
 * Then in Settings.js:
 * import { Outlet } from 'react-router-dom';
 *
 * function Settings() {
 *   return (
 *     <div>
 *       <h1>Settings</h1>
 *       <Outlet /> {/* Nested routes render here */}
 *     </div>
 *   );
 * }
 *
 * ============================================
 * ROUTE PARAMETERS
 * ============================================
 *
 * Example: Todo detail page
 *
 * <Route path="/todos/:id" element={<TodoDetail />} />
 *
 * In TodoDetail.js:
 * import { useParams } from 'react-router-dom';
 *
 * function TodoDetail() {
 *   const { id } = useParams(); // Extract :id from URL
 *   // Fetch todo with this ID
 *   return <div>Todo {id}</div>;
 * }
 *
 * ============================================
 * QUERY PARAMETERS
 * ============================================
 *
 * Example: Search page with query params
 *
 * URL: /search?q=react&sort=date
 *
 * import { useSearchParams } from 'react-router-dom';
 *
 * function Search() {
 *   const [searchParams, setSearchParams] = useSearchParams();
 *   const query = searchParams.get('q'); // "react"
 *   const sort = searchParams.get('sort'); // "date"
 *
 *   return <div>Search results for: {query}</div>;
 * }
 *
 * ============================================
 * PROGRAMMATIC NAVIGATION
 * ============================================
 *
 * Navigate from component code:
 *
 * import { useNavigate } from 'react-router-dom';
 *
 * function MyComponent() {
 *   const navigate = useNavigate();
 *
 *   const handleClick = () => {
 *     navigate('/dashboard'); // Go to dashboard
 *     navigate(-1); // Go back
 *     navigate(1); // Go forward
 *     navigate('/login', { replace: true }); // Replace history entry
 *     navigate('/profile', { state: { from: 'dashboard' } }); // Pass state
 *   };
 *
 *   return <button onClick={handleClick}>Navigate</button>;
 * }
 *
 * ============================================
 * LAZY LOADING ROUTES
 * ============================================
 *
 * Load route components only when needed:
 *
 * import { lazy, Suspense } from 'react';
 *
 * const Dashboard = lazy(() => import('./pages/Dashboard'));
 * const AdminPanel = lazy(() => import('./pages/AdminPanel'));
 *
 * function App() {
 *   return (
 *     <BrowserRouter>
 *       <AuthProvider>
 *         <Suspense fallback={<div>Loading...</div>}>
 *           <Routes>
 *             <Route path="/" element={<Home />} />
 *             <Route path="/dashboard" element={
 *               <ProtectedRoute>
 *                 <Dashboard />
 *               </ProtectedRoute>
 *             } />
 *             <Route path="/admin" element={
 *               <ProtectedRoute requiredRole="admin">
 *                 <AdminPanel />
 *               </ProtectedRoute>
 *             } />
 *           </Routes>
 *         </Suspense>
 *       </AuthProvider>
 *     </BrowserRouter>
 *   );
 * }
 *
 * Benefits:
 * - Smaller initial bundle
 * - Faster page load
 * - Load admin code only when admin visits
 *
 * ============================================
 * SERVER CONFIGURATION
 * ============================================
 *
 * BrowserRouter requires server configuration!
 *
 * Problem:
 * - User navigates to /dashboard
 * - Refreshes page
 * - Browser requests /dashboard from server
 * - Server doesn't have /dashboard route
 * - Returns 404
 *
 * Solution:
 * Configure server to return index.html for all routes
 *
 * Development (React Scripts):
 * - Already configured
 * - No action needed
 *
 * Production (Apache):
 * .htaccess:
 * RewriteEngine On
 * RewriteBase /
 * RewriteRule ^index\.html$ - [L]
 * RewriteCond %{REQUEST_FILENAME} !-f
 * RewriteCond %{REQUEST_FILENAME} !-d
 * RewriteRule . /index.html [L]
 *
 * Production (Nginx):
 * location / {
 *   try_files $uri /index.html;
 * }
 *
 * Spring Boot:
 * @Controller
 * public class WebController {
 *   @GetMapping(value = "/{path:[^\\.]*}")
 *   public String forward() {
 *     return "forward:/";
 *   }
 * }
 */
