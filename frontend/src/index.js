import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

/**
 * INDEX.JS - APPLICATION ENTRY POINT
 *
 * This is the first JavaScript file that runs in a React application
 *
 * Demonstrates:
 * 1. React 18 root API
 * 2. Mounting React app to DOM
 * 3. Import order (CSS before components)
 * 4. StrictMode for development checks
 *
 * KEY LEARNING CONCEPTS:
 * - ReactDOM.createRoot: New React 18 API
 * - root.render: Renders React component tree
 * - StrictMode: Development mode helper
 * - DOM mounting: Connecting React to HTML
 */

/**
 * CREATE ROOT
 *
 * React 18 introduces createRoot for concurrent features
 *
 * Old API (React 17):
 * ReactDOM.render(<App />, document.getElementById('root'));
 *
 * New API (React 18):
 * const root = ReactDOM.createRoot(document.getElementById('root'));
 * root.render(<App />);
 *
 * Benefits of new API:
 * - Enables concurrent rendering
 * - Better performance for updates
 * - Automatic batching of state updates
 * - Improved Suspense support
 *
 * document.getElementById('root'):
 * - Finds the <div id="root"></div> in index.html
 * - React will render inside this element
 * - Everything in this div will be controlled by React
 */
const root = ReactDOM.createRoot(document.getElementById('root'));

/**
 * RENDER APP
 *
 * Renders the entire React application
 *
 * <React.StrictMode>:
 * - Development mode helper (removed in production build)
 * - Performs additional checks and warnings:
 *   * Identifies unsafe lifecycle methods
 *   * Warns about legacy string ref API
 *   * Warns about deprecated findDOMNode
 *   * Detects unexpected side effects
 *   * Detects legacy context API
 *
 * Note: StrictMode renders components twice in development
 * - Helps find bugs caused by side effects
 * - Console logs may appear twice
 * - Only in development mode
 * - Doesn't affect production build
 *
 * <App />:
 * - Root component of the application
 * - Contains BrowserRouter, AuthProvider, Routes
 * - Everything starts here
 */
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

/*
 * ============================================
 * HOW IT WORKS
 * ============================================
 *
 * 1. Browser loads index.html
 * 2. index.html includes <div id="root"></div>
 * 3. index.html loads compiled JavaScript bundle
 * 4. index.js (this file) executes
 * 5. createRoot finds <div id="root">
 * 6. render mounts <App /> inside root div
 * 7. App component renders BrowserRouter
 * 8. BrowserRouter renders AuthProvider
 * 9. AuthProvider renders Routes
 * 10. Routes renders first matching route component
 * 11. React virtual DOM syncs with real DOM
 * 12. User sees rendered page
 *
 * ============================================
 * IMPORT ORDER IMPORTANCE
 * ============================================
 *
 * Order matters for CSS:
 *
 * import './index.css';  // Global styles (reset, fonts)
 * import App from './App';  // App component
 * import './App.css';  // Would override App component styles
 *
 * Best practice:
 * 1. Global CSS first (index.css)
 * 2. Component-specific CSS in component files
 * 3. This prevents specificity issues
 *
 * ============================================
 * DEVELOPMENT VS PRODUCTION
 * ============================================
 *
 * Development (npm start):
 * - React runs in development mode
 * - StrictMode enabled (double rendering)
 * - Detailed error messages
 * - React DevTools enabled
 * - Source maps for debugging
 * - Hot module replacement
 * - Slower performance (more checks)
 *
 * Production (npm run build):
 * - React runs in production mode
 * - StrictMode removed from bundle
 * - Minified code
 * - No source maps (unless configured)
 * - No development warnings
 * - Optimized performance
 * - Smaller bundle size
 *
 * ============================================
 * STRICT MODE BEHAVIOR
 * ============================================
 *
 * Example component:
 *
 * function Counter() {
 *   const [count, setCount] = useState(0);
 *   console.log('Rendering Counter');
 *   return <button onClick={() => setCount(count + 1)}>{count}</button>;
 * }
 *
 * In StrictMode development:
 * - "Rendering Counter" logs TWICE on mount
 * - Component actually renders once for user
 * - Second render helps detect side effects
 *
 * In production:
 * - "Rendering Counter" logs once
 * - StrictMode is completely removed
 *
 * ============================================
 * ALTERNATIVE: NO STRICT MODE
 * ============================================
 *
 * You can remove StrictMode if needed:
 *
 * root.render(<App />);
 *
 * When to remove:
 * - Third-party libraries causing issues
 * - Console logs confusing during debugging
 * - Testing specific behaviors
 *
 * When to keep:
 * - New projects (recommended)
 * - Learning React (catches mistakes)
 * - Following best practices
 *
 * ============================================
 * ERROR HANDLING
 * ============================================
 *
 * If root element not found:
 *
 * Error: "Target container is not a DOM element"
 *
 * Cause: index.html doesn't have <div id="root">
 *
 * Fix: Ensure index.html has:
 * <div id="root"></div>
 *
 * If React not imported:
 *
 * Error: "React is not defined"
 *
 * Cause: Missing import React from 'react';
 *
 * Fix: Add import (though not always needed in newer React)
 *
 * ============================================
 * REACT 18 FEATURES
 * ============================================
 *
 * Using createRoot enables:
 *
 * 1. Automatic Batching:
 *    Before: Only batched updates in event handlers
 *    Now: Batches all updates (including in promises, timeouts)
 *
 *    setCount(1);
 *    setName('John');
 *    // Only one re-render (batched)
 *
 * 2. Transitions:
 *    Mark updates as non-urgent
 *
 *    import { useTransition } from 'react';
 *    const [isPending, startTransition] = useTransition();
 *    startTransition(() => {
 *      setSearchQuery(value); // Non-urgent update
 *    });
 *
 * 3. Suspense for Data Fetching:
 *    <Suspense fallback={<Loading />}>
 *      <UserProfile />
 *    </Suspense>
 *
 * 4. Concurrent Rendering:
 *    React can pause and resume rendering
 *    Keeps UI responsive during heavy updates
 *
 * ============================================
 * WEB VITALS (OPTIONAL)
 * ============================================
 *
 * Measure performance:
 *
 * import reportWebVitals from './reportWebVitals';
 *
 * root.render(
 *   <React.StrictMode>
 *     <App />
 *   </React.StrictMode>
 * );
 *
 * // Log performance metrics
 * reportWebVitals(console.log);
 *
 * // Or send to analytics
 * reportWebVitals(sendToAnalytics);
 *
 * Metrics measured:
 * - CLS: Cumulative Layout Shift
 * - FID: First Input Delay
 * - FCP: First Contentful Paint
 * - LCP: Largest Contentful Paint
 * - TTFB: Time to First Byte
 *
 * ============================================
 * SERVICE WORKER (OPTIONAL)
 * ============================================
 *
 * Enable offline functionality:
 *
 * import * as serviceWorkerRegistration from './serviceWorkerRegistration';
 *
 * root.render(<App />);
 *
 * // Register service worker for PWA
 * serviceWorkerRegistration.register();
 *
 * Benefits:
 * - Offline support
 * - Faster loading (caching)
 * - Background sync
 * - Push notifications
 *
 * ============================================
 * DEBUGGING TIPS
 * ============================================
 *
 * 1. Blank page after build:
 *    - Check browser console for errors
 *    - Ensure index.html is in public folder
 *    - Check if bundle loaded correctly
 *
 * 2. "root is null" error:
 *    - index.html missing root div
 *    - ID typo (root vs Root)
 *
 * 3. CSS not loading:
 *    - Check import path
 *    - Ensure CSS file exists
 *    - Check build output
 *
 * 4. App not updating:
 *    - Hard refresh (Ctrl+F5)
 *    - Clear browser cache
 *    - Check if dev server running
 *
 * 5. Console showing duplicate logs:
 *    - Normal in StrictMode development
 *    - Remove StrictMode to test
 *    - Won't happen in production
 */
