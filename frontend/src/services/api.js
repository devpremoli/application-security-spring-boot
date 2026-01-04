/**
 * API SERVICE - Central HTTP Client
 *
 * This file handles ALL communication with the Spring Boot backend
 *
 * KEY LEARNING CONCEPTS:
 * 1. Axios for HTTP requests
 * 2. Request/Response interceptors
 * 3. JWT token management
 * 4. Error handling
 * 5. API base URL configuration
 *
 * ============================================
 * WHAT IS AXIOS?
 * ============================================
 *
 * Axios is a promise-based HTTP client for JavaScript
 * - Makes HTTP requests from browser
 * - Supports async/await
 * - Automatic JSON transformation
 * - Request/Response interceptors
 *
 * Alternative: fetch() API (built-in)
 * Why Axios?: Better error handling, interceptors, easier syntax
 */

import axios from 'axios';

/**
 * API BASE URL
 *
 * Points to our Spring Boot backend
 * Default port: 8080
 *
 * In production:
 * - Use environment variables
 * - Example: process.env.REACT_APP_API_URL
 *
 * Why localhost:8080?
 * - Spring Boot default port
 * - Backend runs on http://localhost:8080
 * - Frontend runs on http://localhost:3000 (React default)
 */
const API_BASE_URL = 'http://localhost:8080/api';

/**
 * CREATE AXIOS INSTANCE
 *
 * Creates a customized axios instance with default config
 *
 * Benefits:
 * - Centralized configuration
 * - Don't repeat baseURL in every request
 * - Can add default headers
 * - Can add interceptors
 */
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * REQUEST INTERCEPTOR
 *
 * Runs BEFORE every request is sent
 *
 * Use cases:
 * - Add authentication token to headers
 * - Log requests for debugging
 * - Modify request data
 * - Add timestamps
 *
 * This interceptor adds JWT token to Authorization header
 *
 * Flow:
 * 1. Request about to be sent
 * 2. Interceptor checks for token in localStorage
 * 3. If token exists, adds to Authorization header
 * 4. Request sent with token
 *
 * Format: "Authorization: Bearer <token>"
 */
api.interceptors.request.use(
  (config) => {
    /*
     * GET TOKEN FROM LOCAL STORAGE
     *
     * localStorage.getItem('token'):
     * - Browser API for storing data
     * - Data persists even after browser closes
     * - Data is stored as strings
     *
     * Alternative storage options:
     * - sessionStorage: Cleared when tab closes
     * - Cookies: Can be httpOnly for security
     * - Memory: Lost on page refresh
     *
     * Security note:
     * - localStorage is vulnerable to XSS attacks
     * - In production, consider httpOnly cookies
     */
    const token = localStorage.getItem('token');

    /*
     * ADD TOKEN TO HEADERS
     *
     * If token exists, add to Authorization header
     *
     * Format: "Bearer <token>"
     * - "Bearer" is the authentication scheme
     * - Space is required between "Bearer" and token
     *
     * Spring Security expects this format:
     * AuthTokenFilter extracts token after "Bearer "
     */
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    /*
     * LOGGING FOR DEBUGGING
     *
     * Console logs help you learn and debug
     *
     * Shows:
     * - Request method (GET, POST, etc.)
     * - Request URL
     * - Request data (for POST/PUT)
     *
     * In production, remove or use proper logging library
     */
    console.log('📤 Request:', config.method.toUpperCase(), config.url);
    if (config.data) {
      console.log('   Data:', config.data);
    }

    // Return modified config
    return config;
  },
  (error) => {
    // Handle request errors
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

/**
 * RESPONSE INTERCEPTOR
 *
 * Runs AFTER receiving response from server
 *
 * Use cases:
 * - Log responses for debugging
 * - Transform response data
 * - Handle errors globally
 * - Refresh expired tokens
 *
 * This interceptor logs responses and handles errors
 */
api.interceptors.response.use(
  (response) => {
    /*
     * SUCCESS RESPONSE
     *
     * HTTP status codes 2xx (200-299)
     * - 200: OK (successful request)
     * - 201: Created (resource created)
     * - 204: No Content (successful, no response body)
     */
    console.log('✅ Response:', response.status, response.config.url);
    return response;
  },
  (error) => {
    /*
     * ERROR RESPONSE
     *
     * HTTP status codes 4xx and 5xx
     * - 400: Bad Request (validation error)
     * - 401: Unauthorized (not authenticated)
     * - 403: Forbidden (not authorized)
     * - 404: Not Found
     * - 500: Internal Server Error
     */

    if (error.response) {
      /*
       * SERVER RESPONDED WITH ERROR STATUS
       *
       * error.response contains:
       * - status: HTTP status code
       * - data: Error response body
       * - headers: Response headers
       */
      const status = error.response.status;
      const message = error.response.data?.message || 'An error occurred';

      console.error(`❌ Response Error [${status}]:`, message);

      /*
       * HANDLE SPECIFIC ERROR CODES
       */

      // 401 Unauthorized - Token expired or invalid
      if (status === 401) {
        console.log('🔒 Unauthorized - Please login again');
        // Clear invalid token
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        // Redirect to login (handled by component)
      }

      // 403 Forbidden - Insufficient permissions
      if (status === 403) {
        console.log('⛔ Forbidden - Insufficient permissions');
      }

      // 400 Bad Request - Validation errors
      if (status === 400) {
        console.log('⚠️ Validation Error:', error.response.data);
      }

    } else if (error.request) {
      /*
       * REQUEST MADE BUT NO RESPONSE
       *
       * Possible causes:
       * - Server is down
       * - Network error
       * - CORS issue
       * - Timeout
       */
      console.error('❌ No Response:', error.message);
      console.error('💡 Is the backend server running on http://localhost:8080?');

    } else {
      /*
       * REQUEST SETUP ERROR
       *
       * Error occurred before request was sent
       * - Invalid URL
       * - Invalid config
       */
      console.error('❌ Request Setup Error:', error.message);
    }

    // Reject promise so component can handle error
    return Promise.reject(error);
  }
);

/**
 * ============================================
 * AUTHENTICATION API METHODS
 * ============================================
 */

/**
 * LOGIN USER
 *
 * POST /api/auth/signin
 *
 * Request body:
 * {
 *   "username": "john",
 *   "password": "password123"
 * }
 *
 * Response:
 * {
 *   "token": "eyJhbGc...",
 *   "type": "Bearer",
 *   "id": 1,
 *   "username": "john",
 *   "email": "john@example.com",
 *   "roles": ["ROLE_USER"]
 * }
 *
 * Usage in component:
 * const response = await authAPI.login(username, password);
 * const { token, user } = response.data;
 */
export const authAPI = {
  login: (username, password) => {
    return api.post('/auth/signin', { username, password });
  },

  /**
   * REGISTER NEW USER
   *
   * POST /api/auth/signup
   *
   * Request body:
   * {
   *   "username": "john",
   *   "email": "john@example.com",
   *   "password": "password123"
   * }
   *
   * Response:
   * {
   *   "message": "User registered successfully!"
   * }
   */
  signup: (username, email, password) => {
    return api.post('/auth/signup', { username, email, password });
  },
};

/**
 * ============================================
 * TODO API METHODS
 * ============================================
 */

/**
 * TODO CRUD OPERATIONS
 *
 * All these endpoints require authentication
 * JWT token is automatically added by request interceptor
 */
export const todoAPI = {
  /**
   * GET ALL TODOS
   *
   * GET /api/todos
   * Headers: Authorization: Bearer <token>
   *
   * Response:
   * [
   *   {
   *     "id": 1,
   *     "title": "Learn React",
   *     "description": "Complete React tutorial",
   *     "completed": false,
   *     "createdAt": "2024-01-15T10:30:00"
   *   },
   *   ...
   * ]
   */
  getAll: () => {
    return api.get('/todos');
  },

  /**
   * GET SINGLE TODO
   *
   * GET /api/todos/{id}
   *
   * Example: /api/todos/1
   */
  getById: (id) => {
    return api.get(`/todos/${id}`);
  },

  /**
   * CREATE NEW TODO
   *
   * POST /api/todos
   *
   * Request body:
   * {
   *   "title": "Learn Spring Boot",
   *   "description": "Complete Spring Boot tutorial",
   *   "completed": false
   * }
   */
  create: (todoData) => {
    return api.post('/todos', todoData);
  },

  /**
   * UPDATE TODO
   *
   * PUT /api/todos/{id}
   *
   * Request body:
   * {
   *   "title": "Updated title",
   *   "description": "Updated description",
   *   "completed": true
   * }
   */
  update: (id, todoData) => {
    return api.put(`/todos/${id}`, todoData);
  },

  /**
   * DELETE TODO
   *
   * DELETE /api/todos/{id}
   */
  delete: (id) => {
    return api.delete(`/todos/${id}`);
  },

  /**
   * TOGGLE TODO COMPLETION
   *
   * PATCH /api/todos/{id}/toggle
   *
   * Convenience endpoint to toggle completed status
   */
  toggleComplete: (id) => {
    return api.patch(`/todos/${id}/toggle`);
  },
};

/**
 * ============================================
 * TEST API METHODS
 * ============================================
 *
 * These match the TestController endpoints
 * Use to verify authentication and authorization
 */
export const testAPI = {
  /**
   * PUBLIC ENDPOINT
   * No authentication required
   */
  getPublicContent: () => {
    return api.get('/test/all');
  },

  /**
   * USER ENDPOINT
   * Requires ROLE_USER
   */
  getUserContent: () => {
    return api.get('/test/user');
  },

  /**
   * MODERATOR ENDPOINT
   * Requires ROLE_MODERATOR
   */
  getModContent: () => {
    return api.get('/test/mod');
  },

  /**
   * ADMIN ENDPOINT
   * Requires ROLE_ADMIN
   */
  getAdminContent: () => {
    return api.get('/test/admin');
  },
};

/**
 * ============================================
 * ERROR HANDLING HELPER
 * ============================================
 *
 * Extract error message from various error formats
 */
export const getErrorMessage = (error) => {
  if (error.response) {
    // Server responded with error
    const data = error.response.data;

    // Validation errors (from GlobalExceptionHandler)
    if (data.errors) {
      // Multiple field errors
      return Object.values(data.errors).join(', ');
    }

    // Single error message
    if (data.message) {
      return data.message;
    }

    // Default message
    return `Error: ${error.response.status}`;
  }

  if (error.request) {
    // No response received
    return 'Server not responding. Is it running on http://localhost:8080?';
  }

  // Request setup error
  return error.message || 'An unexpected error occurred';
};

/**
 * ============================================
 * USAGE EXAMPLES
 * ============================================
 *
 * Example 1: Login
 *
 * try {
 *   const response = await authAPI.login('john', 'password123');
 *   const { token, id, username, email, roles } = response.data;
 *   localStorage.setItem('token', token);
 *   localStorage.setItem('user', JSON.stringify({ id, username, email, roles }));
 *   console.log('Logged in successfully!');
 * } catch (error) {
 *   const message = getErrorMessage(error);
 *   console.error('Login failed:', message);
 * }
 *
 * ---
 *
 * Example 2: Create Todo
 *
 * try {
 *   const todoData = {
 *     title: 'Learn React',
 *     description: 'Complete React tutorial',
 *     completed: false
 *   };
 *   const response = await todoAPI.create(todoData);
 *   console.log('Todo created:', response.data);
 * } catch (error) {
 *   const message = getErrorMessage(error);
 *   console.error('Failed to create todo:', message);
 * }
 *
 * ---
 *
 * Example 3: Get All Todos
 *
 * try {
 *   const response = await todoAPI.getAll();
 *   const todos = response.data;
 *   console.log('Todos:', todos);
 * } catch (error) {
 *   if (error.response?.status === 401) {
 *     console.log('Please login first');
 *   } else {
 *     console.error('Failed to fetch todos:', getErrorMessage(error));
 *   }
 * }
 */

export default api;
