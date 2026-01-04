# React Frontend Complete Learning Guide

This guide teaches you how to build a React frontend that connects to the Spring Boot JWT backend.

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [Key Concepts](#key-concepts)
- [Component Breakdown](#component-breakdown)
- [API Integration](#api-integration)
- [Authentication Flow](#authentication-flow)
- [Todo CRUD Operations](#todo-crud-operations)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)

---

## Overview

This frontend demonstrates:
- **React fundamentals** - Components, hooks, state
- **API integration** - HTTP requests with Axios
- **Authentication** - JWT token management
- **Form handling** - Validation, error display
- **CRUD operations** - Create, Read, Update, Delete todos
- **Protected routes** - Role-based access control
- **Error handling** - User-friendly error messages

## Project Structure

```
frontend/
├── public/
│   └── index.html              # HTML template
├── src/
│   ├── components/             # Reusable components
│   │   ├── Login.js           # Login form
│   │   ├── Signup.js          # Registration form
│   │   ├── TodoList.js        # Display todos
│   │   ├── TodoForm.js        # Create/Edit todo
│   │   ├── TodoItem.js        # Single todo display
│   │   └── ProtectedRoute.js  # Route guard
│   ├── pages/                  # Page components
│   │   ├── Home.js            # Landing page
│   │   ├── Dashboard.js       # User dashboard
│   │   └── NotFound.js        # 404 page
│   ├── context/
│   │   └── AuthContext.js     # Auth state management
│   ├── services/
│   │   └── api.js             # API client (Axios)
│   ├── hooks/
│   │   └── useTodos.js        # Custom hook for todos
│   ├── utils/
│   │   └── validation.js      # Form validation helpers
│   ├── App.js                  # Main app component
│   ├── App.css                 # Styles
│   └── index.js                # Entry point
├── package.json                # Dependencies
└── README.md                   # Frontend docs
```

## Setup Instructions

### 1. Install Dependencies

```bash
cd frontend
npm install
```

This installs:
- **react** - UI library
- **react-dom** - React rendering
- **react-router-dom** - Routing
- **axios** - HTTP client

### 2. Start Development Server

```bash
npm start
```

- Runs on http://localhost:3000
- Auto-reloads on file changes
- Proxies API requests to backend (localhost:8080)

### 3. Verify Backend is Running

Before starting frontend, ensure Spring Boot backend is running:

```bash
# In backend directory
mvn spring-boot:run
```

Backend should be on http://localhost:8080

---

## Key Concepts

### 1. React Components

Components are reusable pieces of UI.

**Function Component:**
```jsx
function Welcome({ name }) {
  return <h1>Hello, {name}!</h1>;
}
```

**Using Component:**
```jsx
<Welcome name="John" />
```

### 2. React Hooks

Hooks let you use state and other React features in function components.

**useState** - State management:
```jsx
const [count, setCount] = useState(0);

// Update state
setCount(count + 1);
```

**useEffect** - Side effects (API calls, timers):
```jsx
useEffect(() => {
  // Runs after component renders
  fetchData();
}, [dependency]); // Re-run when dependency changes
```

**useContext** - Access context:
```jsx
const { user, login } = useAuth();
```

### 3. Event Handling

**Form Submit:**
```jsx
const handleSubmit = (e) => {
  e.preventDefault(); // Prevent page reload
  // Handle form data
};

<form onSubmit={handleSubmit}>...</form>
```

**Button Click:**
```jsx
const handleClick = () => {
  console.log('Clicked!');
};

<button onClick={handleClick}>Click Me</button>
```

**Input Change:**
```jsx
const [value, setValue] = useState('');

<input
  value={value}
  onChange={(e) => setValue(e.target.value)}
/>
```

### 4. Conditional Rendering

**If/Else:**
```jsx
{isLoggedIn ? (
  <Dashboard />
) : (
  <Login />
)}
```

**Show/Hide:**
```jsx
{error && <div className="error">{error}</div>}
```

### 5. Lists and Keys

**Map Array to Components:**
```jsx
{todos.map(todo => (
  <TodoItem key={todo.id} todo={todo} />
))}
```

**Key Prop:**
- Must be unique
- Helps React identify which items changed
- Don't use array index as key if order can change

---

## Component Breakdown

### Login Component

**Purpose:** User login form

**Key Features:**
- Form validation
- API call to /api/auth/signin
- JWT token storage
- Error handling
- Loading state

**Code Structure:**
```jsx
function Login() {
  // State
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});

  // Context
  const { login, loading, error } = useAuth();
  const navigate = useNavigate();

  // Validation
  const validate = () => {
    const newErrors = {};
    if (!username) newErrors.username = 'Username is required';
    if (!password) newErrors.password = 'Password is required';
    return newErrors;
  };

  // Submit
  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    const success = await login(username, password);
    if (success) {
      navigate('/dashboard');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="error">{error}</div>}

      <input
        type="text"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="Username"
      />
      {errors.username && <span>{errors.username}</span>}

      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
      />
      {errors.password && <span>{errors.password}</span>}

      <button type="submit" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}
```

**Learning Points:**
1. **Controlled inputs** - React state controls input values
2. **Form validation** - Client-side validation before API call
3. **Loading state** - Disable button during request
4. **Error display** - Show validation and API errors
5. **Navigation** - Redirect after successful login

### TodoList Component

**Purpose:** Display all todos

**Key Features:**
- Fetch todos from API
- Display loading state
- Handle errors
- Conditional rendering (empty state)

**Code Structure:**
```jsx
function TodoList() {
  const [todos, setTodos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch todos on mount
  useEffect(() => {
    fetchTodos();
  }, []);

  const fetchTodos = async () => {
    try {
      setLoading(true);
      const response = await todoAPI.getAll();
      setTodos(response.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  if (todos.length === 0) return <div>No todos yet!</div>;

  return (
    <div>
      {todos.map(todo => (
        <TodoItem
          key={todo.id}
          todo={todo}
          onUpdate={fetchTodos}
          onDelete={fetchTodos}
        />
      ))}
    </div>
  );
}
```

**Learning Points:**
1. **useEffect** - Fetch data on component mount
2. **Loading states** - Show spinner while loading
3. **Error handling** - Display errors to user
4. **Empty state** - Handle case when no data
5. **Parent-child communication** - Pass callbacks to children

### TodoForm Component

**Purpose:** Create or edit todo

**Key Features:**
- Form validation
- Create new todo (POST)
- Edit existing todo (PUT)
- Clear form after submit

**Code Structure:**
```jsx
function TodoForm({ todo, onSave, onCancel }) {
  const [title, setTitle] = useState(todo?.title || '');
  const [description, setDescription] = useState(todo?.description || '');
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const validate = () => {
    const newErrors = {};
    if (!title.trim()) {
      newErrors.title = 'Title is required';
    }
    if (title.length > 100) {
      newErrors.title = 'Title must be less than 100 characters';
    }
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    try {
      setSubmitting(true);
      const todoData = {
        title,
        description,
        completed: todo?.completed || false
      };

      if (todo) {
        // Update existing
        await todoAPI.update(todo.id, todoData);
      } else {
        // Create new
        await todoAPI.create(todoData);
      }

      onSave(); // Notify parent
      setTitle(''); // Clear form
      setDescription('');
    } catch (err) {
      setErrors({ submit: getErrorMessage(err) });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder="Todo title"
      />
      {errors.title && <span>{errors.title}</span>}

      <textarea
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        placeholder="Description (optional)"
      />

      {errors.submit && <div className="error">{errors.submit}</div>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Saving...' : (todo ? 'Update' : 'Create')}
      </button>

      {onCancel && (
        <button type="button" onClick={onCancel}>
          Cancel
        </button>
      )}
    </form>
  );
}
```

**Learning Points:**
1. **Props** - Receive data from parent
2. **Conditional initial state** - Edit vs create mode
3. **Validation** - Client-side validation
4. **API calls** - POST for create, PUT for update
5. **Callbacks** - Notify parent of success

### ProtectedRoute Component

**Purpose:** Restrict access to authenticated users

**Code Structure:**
```jsx
function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, hasRole } = useAuth();

  if (!isAuthenticated()) {
    // Not logged in, redirect to login
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    // Logged in but insufficient permissions
    return <div>Access Denied</div>;
  }

  // Authorized, render children
  return children;
}

// Usage
<Route path="/dashboard" element={
  <ProtectedRoute>
    <Dashboard />
  </ProtectedRoute>
} />

<Route path="/admin" element={
  <ProtectedRoute requiredRole="ROLE_ADMIN">
    <AdminPanel />
  </ProtectedRoute>
} />
```

---

## API Integration

### Making API Calls

**1. Import API:**
```jsx
import { todoAPI, getErrorMessage } from '../services/api';
```

**2. Make Request:**
```jsx
try {
  const response = await todoAPI.getAll();
  const todos = response.data;
} catch (error) {
  const message = getErrorMessage(error);
  console.error(message);
}
```

**3. Handle Loading:**
```jsx
const [loading, setLoading] = useState(false);

const fetchData = async () => {
  setLoading(true);
  try {
    const response = await todoAPI.getAll();
    setData(response.data);
  } finally {
    setLoading(false);
  }
};
```

### Request Types

**GET - Fetch data:**
```jsx
const response = await todoAPI.getAll();
```

**POST - Create new:**
```jsx
const data = { title: 'Learn React', completed: false };
const response = await todoAPI.create(data);
```

**PUT - Update existing:**
```jsx
const data = { title: 'Updated title', completed: true };
const response = await todoAPI.update(id, data);
```

**DELETE - Remove:**
```jsx
await todoAPI.delete(id);
```

---

## Authentication Flow

### Login Flow

```
1. User fills login form
   ↓
2. Submit form → login(username, password)
   ↓
3. API POST /api/auth/signin
   ↓
4. Backend validates credentials
   ↓
5. Backend returns JWT + user data
   ↓
6. Store token in localStorage
   ↓
7. Store user in context state
   ↓
8. Redirect to dashboard
```

### Protected Route Access

```
1. User navigates to /dashboard
   ↓
2. ProtectedRoute checks authentication
   ↓
3. isAuthenticated() checks if user exists
   ↓
4. If not authenticated → Redirect to /login
   ↓
5. If authenticated → Render dashboard
```

### Logout Flow

```
1. User clicks logout button
   ↓
2. logout() function called
   ↓
3. Remove token from localStorage
   ↓
4. Remove user from localStorage
   ↓
5. Set user state to null
   ↓
6. Redirect to home page
```

---

## Todo CRUD Operations

### Create Todo

```jsx
const createTodo = async (title, description) => {
  const todoData = {
    title,
    description,
    completed: false
  };

  const response = await todoAPI.create(todoData);
  return response.data;
};
```

### Read Todos

```jsx
const fetchTodos = async () => {
  const response = await todoAPI.getAll();
  return response.data;
};
```

### Update Todo

```jsx
const updateTodo = async (id, updates) => {
  const response = await todoAPI.update(id, updates);
  return response.data;
};
```

### Delete Todo

```jsx
const deleteTodo = async (id) => {
  await todoAPI.delete(id);
};
```

### Toggle Complete

```jsx
const toggleTodo = async (id) => {
  await todoAPI.toggleComplete(id);
};
```

---

## Error Handling

### API Error Handling

```jsx
try {
  await todoAPI.create(data);
} catch (error) {
  if (error.response) {
    // Server responded with error
    if (error.response.status === 401) {
      // Unauthorized - redirect to login
      navigate('/login');
    } else if (error.response.status === 400) {
      // Validation error
      const errors = error.response.data.errors;
      setErrors(errors);
    }
  } else if (error.request) {
    // No response - server down
    setError('Server not responding');
  } else {
    // Request setup error
    setError('Request failed');
  }
}
```

### Validation Errors

```jsx
// Frontend validation
const validate = (data) => {
  const errors = {};

  if (!data.title) {
    errors.title = 'Title is required';
  }

  if (data.title.length > 100) {
    errors.title = 'Title too long';
  }

  return errors;
};

// Display errors
{errors.title && (
  <span className="error">{errors.title}</span>
)}
```

---

## Best Practices

### 1. Always Handle Loading States

```jsx
if (loading) return <div>Loading...</div>;
```

### 2. Always Handle Errors

```jsx
if (error) return <div>Error: {error}</div>;
```

### 3. Validate Forms Before Submit

```jsx
const errors = validate(formData);
if (Object.keys(errors).length > 0) {
  setErrors(errors);
  return;
}
```

### 4. Clear Forms After Success

```jsx
const handleSubmit = async () => {
  await createTodo(data);
  setTitle(''); // Clear form
  setDescription('');
};
```

### 5. Use Keys in Lists

```jsx
{todos.map(todo => (
  <TodoItem key={todo.id} todo={todo} />
))}
```

### 6. Disable Buttons During Submit

```jsx
<button disabled={loading}>
  {loading ? 'Submitting...' : 'Submit'}
</button>
```

### 7. Clean Up Effects

```jsx
useEffect(() => {
  const timer = setTimeout(() => {...}, 1000);
  return () => clearTimeout(timer); // Cleanup
}, []);
```

---

## Next Steps

1. **Read the code files** in `frontend/src/`
2. **Run the application** with `npm start`
3. **Test all features**:
   - Sign up new account
   - Login
   - Create todos
   - Edit todos
   - Delete todos
   - Logout
4. **Experiment**:
   - Add new fields to todos
   - Add filtering/sorting
   - Add search functionality
   - Improve styling

---

Happy Learning! 🚀

Every component includes detailed comments explaining React concepts,
API integration, state management, and best practices!
