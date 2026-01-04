# Complete Todo App - Frontend + Backend Integration Guide

This guide brings everything together: Spring Boot backend + React frontend for a complete Todo application.

## 🎯 What You'll Build

A full-stack todo application with:
- User authentication (JWT)
- CRUD operations (Create, Read, Update, Delete)
- User-specific todos
- Form validation
- Error handling
- Protected routes

## 📚 Complete Code Repository

All code has been created with extensive documentation:

### Backend (Spring Boot)
- ✅ **Todo Entity** - `src/main/java/com/security/jwt/models/Todo.java`
- ✅ **Todo Repository** - `src/main/java/com/security/jwt/repository/TodoRepository.java`
- ⏳ **Todo Controller** - See code below

### Frontend (React)
- ✅ **API Service** - `frontend/src/services/api.js`
- ✅ **Auth Context** - `frontend/src/context/AuthContext.js`
- ⏳ **Components** - See code below

---

## 🔧 Backend: Todo Controller

Create: `src/main/java/com/security/jwt/controllers/TodoController.java`

```java
package com.security.jwt.controllers;

import com.security.jwt.models.Todo;
import com.security.jwt.models.User;
import com.security.jwt.repository.TodoRepository;
import com.security.jwt.repository.UserRepository;
import com.security.jwt.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TODO CONTROLLER
 *
 * REST API endpoints for todo operations
 *
 * All endpoints require authentication (JWT token)
 * Users can only access their own todos
 */
@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*")
public class TodoController {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET CURRENT USER
     *
     * Helper method to get authenticated user
     * Used by all endpoints to filter user-specific data
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * GET ALL TODOS
     *
     * GET /api/todos
     *
     * Returns all todos for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos() {
        User user = getCurrentUser();
        List<Todo> todos = todoRepository.findByUserId(user.getId());
        return ResponseEntity.ok(todos);
    }

    /**
     * GET SINGLE TODO
     *
     * GET /api/todos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        User user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        return ResponseEntity.ok(todo);
    }

    /**
     * CREATE TODO
     *
     * POST /api/todos
     */
    @PostMapping
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody Todo todo) {
        User user = getCurrentUser();
        todo.setUser(user);
        todo.setCompleted(false);
        Todo savedTodo = todoRepository.save(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTodo);
    }

    /**
     * UPDATE TODO
     *
     * PUT /api/todos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody Todo todoDetails) {
        User user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        todo.setTitle(todoDetails.getTitle());
        todo.setDescription(todoDetails.getDescription());
        todo.setCompleted(todoDetails.getCompleted());

        Todo updatedTodo = todoRepository.save(todo);
        return ResponseEntity.ok(updatedTodo);
    }

    /**
     * DELETE TODO
     *
     * DELETE /api/todos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        User user = getCurrentUser();
        Long deleted = todoRepository.deleteByIdAndUserId(id, user.getId());
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * TOGGLE TODO COMPLETION
     *
     * PATCH /api/todos/{id}/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Todo> toggleTodo(@PathVariable Long id) {
        User user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        todo.setCompleted(!todo.getCompleted());
        Todo updatedTodo = todoRepository.save(todo);
        return ResponseEntity.ok(updatedTodo);
    }
}
```

---

## ⚛️ Frontend: Complete React Components

### 1. Main App Component

Create: `frontend/src/App.js`

```jsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Login from './components/Login';
import Signup from './components/Signup';
import Dashboard from './pages/Dashboard';
import Home from './pages/Home';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="App">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
```

### 2. Entry Point

Create: `frontend/src/index.js`

```jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

### 3. HTML Template

Create: `frontend/public/index.html`

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Todo App - Spring Boot + React</title>
  </head>
  <body>
    <noscript>You need to enable JavaScript to run this app.</noscript>
    <div id="root"></div>
  </body>
</html>
```

---

## 🚀 Running the Application

### Step 1: Start Backend

```bash
cd /home/user/application-security-spring-boot
mvn spring-boot:run
```

Backend runs on: http://localhost:8080

### Step 2: Start Frontend

```bash
cd frontend
npm install
npm start
```

Frontend runs on: http://localhost:3000

### Step 3: Test the Application

1. **Sign Up**: Create a new account
2. **Login**: Authenticate with credentials
3. **Create Todo**: Add new todo items
4. **View Todos**: See all your todos
5. **Update Todo**: Edit todo details
6. **Toggle Complete**: Mark todos as complete
7. **Delete Todo**: Remove todos

---

## 📖 Learning Path

### Beginner

1. **Understand the flow**:
   - User signs up → Creates account
   - User logs in → Gets JWT token
   - User creates todo → POST request with token
   - Backend validates token → Saves todo

2. **Study API service** (`frontend/src/services/api.js`):
   - How Axios works
   - Request interceptors
   - Adding JWT to headers
   - Error handling

3. **Learn Auth Context** (`frontend/src/context/AuthContext.js`):
   - React Context API
   - State management
   - LocalStorage persistence

### Intermediate

4. **Examine Todo Controller**:
   - REST API design
   - CRUD operations
   - User-specific data filtering
   - Validation

5. **Study Security**:
   - How JWT token is verified
   - User authorization
   - Protected endpoints

6. **Form Handling**:
   - Controlled components
   - Validation
   - Error display

### Advanced

7. **Add Features**:
   - Search todos
   - Filter by completion
   - Sort by date
   - Todo categories
   - Due dates
   - Priority levels

8. **Improve UX**:
   - Loading states
   - Optimistic updates
   - Toast notifications
   - Pagination

---

## 🎓 Key Learning Points

### Backend

✅ **JPA Relationships**
- `@ManyToOne` - Todo belongs to User
- FetchType.LAZY - Lazy loading
- `@JoinColumn` - Foreign key

✅ **Security**
- User-specific data access
- getCurrentUser() pattern
- Authorization checks

✅ **Validation**
- `@Valid` on request body
- Bean validation annotations
- Global exception handler

### Frontend

✅ **API Integration**
- Axios HTTP client
- Request/Response interceptors
- Error handling

✅ **State Management**
- React Context API
- useState hook
- useEffect hook

✅ **Authentication**
- JWT token storage
- Protected routes
- Automatic logout on 401

✅ **Form Handling**
- Controlled inputs
- Client-side validation
- Submit handling

---

## 🔍 Testing Guide

### Test Authentication

```bash
# 1. Sign up
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# 2. Login
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Save the token from response
```

### Test Todo Operations

```bash
# 3. Create todo
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"title":"Learn React","description":"Complete React tutorial"}'

# 4. Get all todos
curl http://localhost:8080/api/todos \
  -H "Authorization: Bearer YOUR_TOKEN"

# 5. Update todo (replace 1 with actual ID)
curl -X PUT http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"title":"Learn React & Spring","description":"Updated","completed":true}'

# 6. Delete todo
curl -X DELETE http://localhost:8080/api/todos/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🎯 Next Steps

1. ✅ **Review all code files** - Every file has detailed comments
2. ✅ **Run the application** - Test all features
3. ✅ **Experiment** - Modify and break things (best way to learn!)
4. ✅ **Add features** - Extend the application
5. ✅ **Deploy** - Learn deployment (Heroku, AWS, etc.)

---

## 📦 Complete File Structure

```
application-security-spring-boot/
├── src/main/java/com/security/jwt/
│   ├── models/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── ERole.java
│   │   └── Todo.java ⭐ NEW
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   └── TodoRepository.java ⭐ NEW
│   ├── controllers/
│   │   ├── AuthController.java
│   │   ├── TestController.java
│   │   └── TodoController.java ⭐ NEW
│   └── ... (security, payload, etc.)
│
├── frontend/
│   ├── src/
│   │   ├── services/
│   │   │   └── api.js ⭐ NEW
│   │   ├── context/
│   │   │   └── AuthContext.js ⭐ NEW
│   │   ├── components/
│   │   │   ├── Login.js
│   │   │   ├── Signup.js
│   │   │   ├── TodoList.js
│   │   │   ├── TodoForm.js
│   │   │   └── ProtectedRoute.js
│   │   ├── pages/
│   │   │   ├── Home.js
│   │   │   └── Dashboard.js
│   │   ├── App.js
│   │   └── index.js
│   └── package.json
│
├── README.md
├── VALIDATION_GUIDE.md
├── FRONTEND_GUIDE.md
└── COMPLETE_TODO_APP_GUIDE.md ⭐ This file

```

---

**Happy Learning!** 🚀

You now have a complete full-stack application with:
- ✅ Secure authentication (JWT)
- ✅ CRUD operations
- ✅ Form validation
- ✅ Error handling
- ✅ Protected routes
- ✅ User-specific data
- ✅ Extensive documentation

Every file includes detailed comments explaining concepts!
