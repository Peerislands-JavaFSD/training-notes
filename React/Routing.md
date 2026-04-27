# Routing in React 
### Prerequisites
- React 18+ project with TypeScript (e.g., via `npx create-react-app my-app --template typescript` or Vite).
- Install React Router: `npm install react-router-dom` (or `yarn add react-router-dom`).


---

## 1. Basic Routing Setup

**Purpose**: Define routes to render components based on URL paths. React Router handles client-side navigation without full page reloads.

### Key Components
- `<BrowserRouter>`: Wraps your app for HTML5 history API (clean URLs).
- `<Routes>`: Container for all routes (replaces `<Switch>` from v5).
- `<Route>`: Defines a path and what to render (component or element).

### Example: Simple App with Routes

#### `index.tsx` (Wrap the App)
```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);
```

#### `App.tsx` (Define Routes)
```tsx
import { Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import About from './pages/About';
import NotFound from './pages/NotFound';

const App: React.FC = () => {
  return (
    <div>
      <nav>
        <a href="/">Home</a> | <a href="/about">About</a>
      </nav>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="*" element={<NotFound />} />  {/* Catch-all for 404 */}
      </Routes>
    </div>
  );
};

export default App;
```

#### Page Components (e.g., `Home.tsx`)
```tsx
const Home: React.FC = () => <h1>Welcome to Home!</h1>;
export default Home;

// Similarly for About.tsx and NotFound.tsx
```

#### Explanation
- `<BrowserRouter>` enables routing.
- `<Routes>` evaluates routes in order; first match wins.
- `<Route path="..." element={<Component />}`: Renders the element when URL matches.
- `path="*"` : Wildcard for unmatched routes (404 page).
- Navigation: Use `<a href="/">` for now (but we'll improve with `<Link>`).
- TypeScript: No extra types needed here—React Router infers from React.FC.

**Pro Tip**: Use `<HashRouter>` for static hosts (e.g., GitHub Pages) where server config isn't possible—it uses `#` in URLs.

---

## 2. useParams: Dynamic Routes

**Purpose**: Extract dynamic segments from the URL (e.g., `/users/:id` where `:id` is a param).

`useParams()` returns an object with param keys/values. In TS, type it with generics for safety.

### Example: User Profile Page

#### Update `App.tsx`
```tsx
<Routes>
  {/* ... other routes */}
  <Route path="/users/:userId" element={<UserProfile />} />
</Routes>
```

#### `UserProfile.tsx`
```tsx
import { useParams } from 'react-router-dom';

// Type the params for safety
interface UserParams extends Record<string, string | undefined> {
  userId: string;
}

const UserProfile: React.FC = () => {
  const { userId } = useParams<UserParams>();  // Typed hook

  // Simulate fetching user data
  return <h1>User Profile for ID: {userId}</h1>;
};

export default UserProfile;
```

#### Explanation
- `path="/users/:userId"`: `:userId` is a dynamic param.
- `useParams<UserParams>()`: Returns `{ userId: string }` (e.g., for URL `/users/123`, `userId = "123"`).
- TS Generic: `UserParams` ensures `userId` is required and typed as string. Use `Record<string, string | undefined>` for flexibility.
- Usage: Navigate to `/users/123` → Renders "User Profile for ID: 123".
- Advanced: Multiple params like `/posts/:postId/comments/:commentId`.

**Common Pitfall**: Params are always strings—parse if needed (e.g., `parseInt(userId, 10)` for numbers).

---

## 3. useNavigate: Programmatic Navigation

**Purpose**: Navigate programmatically (e.g., after form submit or button click), replacing `history.push` from v5.

`useNavigate()` returns a function to navigate.

### Example: Login Redirect

#### `Login.tsx`
```tsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const navigate = useNavigate();  // Hook for navigation

  const handleLogin = () => {
    // Simulate auth
    if (username) {
      navigate('/dashboard');  // Redirect to dashboard
      // Or with state: navigate('/dashboard', { state: { from: 'login' } });
    }
  };

  return (
    <div>
      <input
        type="text"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="Username"
      />
      <button onClick={handleLogin}>Login</button>
    </div>
  );
};

export default Login;
```

#### Update `App.tsx`
```tsx
<Routes>
  {/* ... */}
  <Route path="/login" element={<Login />} />
  <Route path="/dashboard" element={<Dashboard />} />
</Routes>
```

#### `Dashboard.tsx`
```tsx
const Dashboard: React.FC = () => <h1>Welcome to Dashboard!</h1>;
```

#### Explanation
- `useNavigate()`: Returns `navigate` function.
- `navigate('/path')`: Imperative navigation (like redirect).
- Options: `navigate(-1)` (go back), `navigate('/path', { replace: true })` (replace history entry).
- TS: No extra typing needed—`navigate` infers paths from your routes (but not strictly enforced).
- Better than `<a>`: Prevents full reload, preserves state.

**Pro Tip**: Use `<Link to="/path">` for declarative links: `import { Link } from 'react-router-dom'; <Link to="/about">About</Link>;`.

---

## 4. Nested Routes

**Purpose**: Routes within routes (e.g., `/admin/users` and `/admin/settings` share a layout).

Use `<Outlet>` to render child routes inside the parent.

### Example: Admin Dashboard with Nested Routes

#### `App.tsx`
```tsx
<Routes>
  <Route path="/" element={<Home />} />
  <Route path="/admin" element={<AdminLayout />}>
    {/* Nested routes */}
    <Route index element={<AdminDashboard />} />  {/* Default child */}
    <Route path="users" element={<AdminUsers />} />
    <Route path="settings" element={<AdminSettings />} />
  </Route>
  <Route path="*" element={<NotFound />} />
</Routes>
```

#### `AdminLayout.tsx` (Parent with Outlet)
```tsx
import { Outlet } from 'react-router-dom';

const AdminLayout: React.FC = () => {
  return (
    <div>
      <header>Admin Header</header>
      <nav>
        <Link to="/admin">Dashboard</Link> | 
        <Link to="/admin/users">Users</Link> | 
        <Link to="/admin/settings">Settings</Link>
      </nav>
      <main>
        <Outlet />  {/* Renders child route here */}
      </main>
      <footer>Admin Footer</footer>
    </div>
  );
};

export default AdminLayout;
```

#### Child Components
```tsx
// AdminDashboard.tsx
const AdminDashboard: React.FC = () => <h2>Admin Dashboard</h2>;

// AdminUsers.tsx
const AdminUsers: React.FC = () => <h2>Manage Users</h2>;

// AdminSettings.tsx
const AdminSettings: React.FC = () => <h2>Settings Page</h2>;
```

#### Explanation
- Parent `<Route path="/admin" element={<AdminLayout />}>`: Wraps children.
- Nested `<Route>`: Paths are relative (e.g., `/admin/users`).
- `<Route index>`: Default for `/admin`.
- `<Outlet>`: Placeholder where child components render.
- Navigation: `<Link to="users">` (relative) or `<Link to="/admin/users">` (absolute).
- TS: No special types—Outlet is untyped, but components are React.FC.
- Benefits: Shared layout (header/nav) without duplication.

**Advanced**: Deep nesting (e.g., `/admin/users/:id/edit`) works recursively with multiple Outlets.

---

## 5. Route Guards (Protected Routes)

**Purpose**: Protect routes (e.g., require authentication). In v6/v7, use a wrapper component or loaders (for data fetching + auth).

We'll use a `<ProtectedRoute>` component for simplicity.

### Example: Auth-Protected Dashboard

#### Auth Context (Simulate Auth)
```tsx
// AuthContext.tsx
import { createContext, useState, ReactNode } from 'react';

interface AuthContextType {
  isAuthenticated: boolean;
  login: () => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const login = () => setIsAuthenticated(true);
  const logout = () => setIsAuthenticated(false);

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
```

#### Wrap App in `index.tsx`
```tsx
<BrowserRouter>
  <AuthProvider>
    <App />
  </AuthProvider>
</BrowserRouter>
```

#### `ProtectedRoute.tsx`
```tsx
import { useContext } from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { AuthContext } from './AuthContext';

const ProtectedRoute: React.FC = () => {
  const authContext = useContext(AuthContext);

  if (!authContext) {
    throw new Error('ProtectedRoute must be used within AuthProvider');
  }

  const { isAuthenticated } = authContext;

  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;
```

#### Update `App.tsx`
```tsx
<Routes>
  <Route path="/login" element={<Login />} />
  <Route element={<ProtectedRoute />}>
    {/* Protected nested routes */}
    <Route path="/dashboard" element={<Dashboard />} />
    <Route path="/profile" element={<Profile />} />
  </Route>
  {/* ... */}
</Routes>
```

#### `Login.tsx` (Update with Auth)
```tsx
// ... from earlier
const authContext = useContext(AuthContext);
// In handleLogin: authContext?.login(); navigate('/dashboard');
```

#### Explanation
- `AuthContext`: Manages auth state (real apps use JWT/cookies).
- `<ProtectedRoute>`: Wrapper that checks auth. Uses `<Outlet>` for nesting.
- If not authenticated: `<Navigate to="/login" replace />` redirects (replace: no back button entry).
- Usage: Wrap protected routes in `<Route element={<ProtectedRoute />}>`.
- TS: Context typed with `AuthContextType`; error if missing provider.
- Alternative (v6.4+ Loaders): Use `loader` functions for server-side auth checks, but for client-only, this works.

**Pro Tip**: For role-based guards, extend with `roles` in context.

---

## Best Practices & Tips (2025)
- **Error Handling**: Use `<Route errorElement={<ErrorPage />} />` for crashes.
- **Lazy Loading**: `const LazyHome = lazy(() => import('./Home')); <Route path="/" element={<Suspense fallback={<Loading />}><LazyHome /></Suspense>} />`.
- **Data Fetching**: v6.4+ loaders/actions for pre-fetching (e.g., in route defs).
- **TS Enhancements**: Use `react-router-dom`'s types (imported automatically).
- **Testing**: Use `@testing-library/react` with memory router.
- **Common Issues**: Relative vs absolute paths; always import hooks correctly.
- **Migration from v5**: No more `history`; use `useNavigate`.
