import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Home.css';

/**
 * HOME PAGE
 *
 * Landing page for the application
 * Public route - accessible to everyone
 *
 * Demonstrates:
 * 1. Conditional rendering based on auth state
 * 2. Navigation with Link component
 * 3. Different views for authenticated vs unauthenticated users
 * 4. Marketing/landing page structure
 *
 * KEY CONCEPTS:
 * - Public route (no authentication required)
 * - Different CTAs based on auth state
 * - Link component for SPA navigation
 */

const Home = () => {
    // ============================================
    // HOOKS
    // ============================================

    /**
     * AUTH CONTEXT
     *
     * Check if user is logged in
     * Shows different content accordingly
     */
    const { isAuthenticated } = useAuth();

    // ============================================
    // RENDER
    // ============================================

    return (
        <div className="home">
            {/* Hero Section */}
            <section className="hero">
                <div className="hero-content">
                    {/* Title */}
                    <h1 className="hero-title">
                        🚀 Spring Boot JWT Security
                    </h1>

                    {/* Subtitle */}
                    <p className="hero-subtitle">
                        A comprehensive learning application demonstrating
                        Spring Boot Security with JWT authentication,
                        React frontend integration, and full-stack todo management.
                    </p>

                    {/* Call-to-Action Buttons */}
                    {/*
                        CONDITIONAL RENDERING

                        Shows different buttons based on authentication status:

                        If logged in:
                        - "Go to Dashboard" button
                        - User can access their todos

                        If not logged in:
                        - "Get Started" (signup) button
                        - "Login" button
                        - User needs to create account or login
                    */}
                    <div className="hero-actions">
                        {isAuthenticated() ? (
                            // Logged in: Show dashboard link
                            <Link to="/dashboard" className="btn btn-primary btn-large">
                                Go to Dashboard →
                            </Link>
                        ) : (
                            // Not logged in: Show signup and login
                            <>
                                <Link to="/signup" className="btn btn-primary btn-large">
                                    Get Started
                                </Link>
                                <Link to="/login" className="btn btn-secondary btn-large">
                                    Login
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            </section>

            {/* Features Section */}
            <section className="features">
                <h2 className="features-title">What You'll Learn</h2>

                <div className="features-grid">
                    {/* Feature 1: Backend Security */}
                    <div className="feature-card">
                        <div className="feature-icon">🔐</div>
                        <h3 className="feature-title">Spring Security & JWT</h3>
                        <p className="feature-description">
                            Learn how to implement JWT authentication,
                            secure REST APIs, and manage user sessions
                            with Spring Boot Security.
                        </p>
                        <ul className="feature-list">
                            <li>JWT token generation & validation</li>
                            <li>Password hashing with BCrypt</li>
                            <li>Role-based access control</li>
                            <li>Custom security filters</li>
                        </ul>
                    </div>

                    {/* Feature 2: React Frontend */}
                    <div className="feature-card">
                        <div className="feature-icon">⚛️</div>
                        <h3 className="feature-title">React Integration</h3>
                        <p className="feature-description">
                            Build a modern React frontend with authentication,
                            protected routes, and seamless API integration.
                        </p>
                        <ul className="feature-list">
                            <li>React hooks (useState, useEffect, useContext)</li>
                            <li>Protected routes with React Router</li>
                            <li>Axios interceptors for auth</li>
                            <li>Global state management</li>
                        </ul>
                    </div>

                    {/* Feature 3: Validation */}
                    <div className="feature-card">
                        <div className="feature-icon">✅</div>
                        <h3 className="feature-title">Data Validation</h3>
                        <p className="feature-description">
                            Master Bean Validation on the backend and
                            client-side validation on the frontend.
                        </p>
                        <ul className="feature-list">
                            <li>Bean Validation (JSR-380)</li>
                            <li>Custom validators</li>
                            <li>Frontend form validation</li>
                            <li>Error handling & display</li>
                        </ul>
                    </div>

                    {/* Feature 4: Full-Stack CRUD */}
                    <div className="feature-card">
                        <div className="feature-icon">📝</div>
                        <h3 className="feature-title">Complete CRUD App</h3>
                        <p className="feature-description">
                            Build a full-featured todo application with
                            Create, Read, Update, and Delete operations.
                        </p>
                        <ul className="feature-list">
                            <li>RESTful API design</li>
                            <li>User-specific data filtering</li>
                            <li>Optimistic UI updates</li>
                            <li>Real-time form validation</li>
                        </ul>
                    </div>
                </div>
            </section>

            {/* Technology Stack */}
            <section className="tech-stack">
                <h2 className="tech-stack-title">Built With</h2>

                <div className="tech-grid">
                    <div className="tech-item">
                        <div className="tech-name">Spring Boot 3.2</div>
                        <div className="tech-desc">Backend Framework</div>
                    </div>
                    <div className="tech-item">
                        <div className="tech-name">Spring Security</div>
                        <div className="tech-desc">Authentication & Authorization</div>
                    </div>
                    <div className="tech-item">
                        <div className="tech-name">JWT</div>
                        <div className="tech-desc">Token-based Auth</div>
                    </div>
                    <div className="tech-item">
                        <div className="tech-name">React 18</div>
                        <div className="tech-desc">Frontend Library</div>
                    </div>
                    <div className="tech-item">
                        <div className="tech-name">React Router 6</div>
                        <div className="tech-desc">Routing & Navigation</div>
                    </div>
                    <div className="tech-item">
                        <div className="tech-name">Axios</div>
                        <div className="tech-desc">HTTP Client</div>
                    </div>
                    <div className="tech-item">
                        <div className="tech-name">H2 Database</div>
                        <div className="tech-desc">In-Memory Database</div>
                    </div>
                    <div className="tech-item">
                        <div className="tech-name">Maven</div>
                        <div className="tech-desc">Build Tool</div>
                    </div>
                </div>
            </section>

            {/* Call-to-Action */}
            {!isAuthenticated() && (
                <section className="cta">
                    <h2 className="cta-title">Ready to Get Started?</h2>
                    <p className="cta-subtitle">
                        Create an account and start learning Spring Boot Security today!
                    </p>
                    <div className="cta-actions">
                        <Link to="/signup" className="btn btn-primary btn-large">
                            Sign Up Now
                        </Link>
                        <Link to="/login" className="btn btn-secondary btn-large">
                            Already have an account? Login
                        </Link>
                    </div>
                </section>
            )}

            {/* Footer */}
            <footer className="home-footer">
                <p>
                    Spring Boot JWT Security Demo • Educational Project
                </p>
                <p className="footer-links">
                    <a href="https://spring.io/projects/spring-boot" target="_blank" rel="noopener noreferrer">
                        Spring Boot Docs
                    </a>
                    {' • '}
                    <a href="https://react.dev" target="_blank" rel="noopener noreferrer">
                        React Docs
                    </a>
                    {' • '}
                    <a href="https://jwt.io" target="_blank" rel="noopener noreferrer">
                        JWT.io
                    </a>
                </p>
            </footer>
        </div>
    );
};

export default Home;

/*
 * ============================================
 * COMPONENT PURPOSE
 * ============================================
 *
 * Home page serves as:
 * 1. Landing page for new visitors
 * 2. Marketing page explaining features
 * 3. Entry point to signup/login
 * 4. Quick access to dashboard for logged-in users
 *
 * ============================================
 * CONDITIONAL RENDERING
 * ============================================
 *
 * isAuthenticated() check determines UI:
 *
 * For logged-in users:
 * - Shows "Go to Dashboard" button
 * - Hides signup/login buttons
 * - Hides bottom CTA section
 *
 * For visitors:
 * - Shows "Get Started" and "Login" buttons
 * - Shows full features section
 * - Shows bottom CTA section
 *
 * ============================================
 * LINK VS NAVIGATE
 * ============================================
 *
 * <Link> Component:
 * - Declarative navigation
 * - Renders as <a> tag
 * - SPA navigation (no page reload)
 * - SEO friendly
 * - Accessible (keyboard, screen readers)
 *
 * Usage:
 * <Link to="/signup">Sign Up</Link>
 *
 * navigate() Function:
 * - Imperative navigation
 * - Called from event handlers
 * - Used after form submission, logout, etc.
 *
 * Usage:
 * const navigate = useNavigate();
 * navigate('/dashboard');
 *
 * ============================================
 * NAVIGATION FLOW
 * ============================================
 *
 * New User Journey:
 * 1. Arrives at Home (/)
 * 2. Sees features and benefits
 * 3. Clicks "Get Started"
 * 4. Navigates to Signup (/signup)
 * 5. Creates account
 * 6. Redirects to Login (/login)
 * 7. Logs in
 * 8. Redirects to Dashboard (/dashboard)
 *
 * Returning User Journey:
 * 1. Arrives at Home (/)
 * 2. Clicks "Login"
 * 3. Navigates to Login (/login)
 * 4. Logs in
 * 5. Redirects to Dashboard (/dashboard)
 *
 * Logged-in User Journey:
 * 1. Arrives at Home (/)
 * 2. Sees "Go to Dashboard" button
 * 3. Clicks button
 * 4. Navigates to Dashboard (/dashboard)
 *
 * ============================================
 * STYLING APPROACH
 * ============================================
 *
 * Home.css uses:
 * - Flexbox for layout
 * - Grid for features section
 * - Gradients for visual appeal
 * - Responsive design (mobile-first)
 * - CSS variables for colors
 * - Smooth transitions
 *
 * ============================================
 * MARKETING VS APPLICATION
 * ============================================
 *
 * This page is hybrid:
 * - Marketing: Features, benefits, CTA
 * - Application: Quick access for logged-in users
 *
 * Pure marketing site would:
 * - Have separate domain
 * - More elaborate design
 * - No application functionality
 *
 * Pure application would:
 * - Skip marketing content
 * - Redirect immediately to login/dashboard
 * - Minimal landing page
 *
 * ============================================
 * ACCESSIBILITY
 * ============================================
 *
 * Current features:
 * - Semantic HTML (section, header, footer)
 * - Clear heading hierarchy
 * - Link text describes destination
 * - Good color contrast
 *
 * Improvements:
 * - ARIA landmarks
 * - Skip to content link
 * - Focus indicators
 * - Reduced motion support
 *
 * ============================================
 * SEO CONSIDERATIONS
 * ============================================
 *
 * For better SEO:
 * - Add meta tags (title, description)
 * - Use semantic HTML
 * - Include keywords naturally
 * - Add alt text to images
 * - Create sitemap
 * - Server-side rendering (Next.js)
 *
 * Current limitation:
 * - React is client-side rendered
 * - Content not in initial HTML
 * - Search engines might not index well
 *
 * ============================================
 * PERFORMANCE
 * ============================================
 *
 * Optimization techniques:
 * - Lazy load sections below fold
 * - Optimize images (WebP format)
 * - Minimize CSS/JS bundles
 * - Use CDN for static assets
 * - Implement caching
 *
 * ============================================
 * ANALYTICS
 * ============================================
 *
 * Track user behavior:
 * - Page views
 * - Button clicks (signup, login)
 * - Time on page
 * - Bounce rate
 *
 * Tools:
 * - Google Analytics
 * - Mixpanel
 * - Segment
 *
 * Implementation:
 * useEffect(() => {
 *   analytics.track('Page Viewed', { page: 'Home' });
 * }, []);
 */
