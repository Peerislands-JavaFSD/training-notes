# Basic React Hooks 

### 1. useState – Quick Recap (for context)
```tsx
const [count, setCount] = useState<number>(0);
const [user, setUser] = useState<{ name: string; age: number } | null>(null);
```

Now let’s dive into the advanced ones.

### 2. useEffect – Side Effects & Lifecycle

**Purpose**: Run code after render (fetch data, subscriptions, manual DOM changes, etc.).

#### Basic Syntax & Dependency Array
```tsx
useEffect(() => {
  // runs after every render
}, []); // dependency array
```

#### Three Main Patterns

| Dependency Array | When it runs                          |
|------------------|----------------------------------------|
| `[]`             | Only once on mount (like componentDidMount) |
| `[a, b]`         | On mount + when a or b changes         |
| omitted          | After every render (rarely needed)     |

#### Example 1: Fetch Data on Mount
```tsx
import { useState, useEffect } from 'react';

interface User {
  id: number;
  name: string;
  email: string;
}

function UserProfile({ userId }: { userId: number }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    
    fetch(`https://jsonplaceholder.typicode.com/users/${userId}`)
      .then(res => res.json())
      .then(data => {
        setUser(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
      
    // Cleanup (optional): cancel request, clear timers, etc.
    return () => {
      console.log('Cleanup before next effect or unmount');
    };
  }, [userId]); // re-run when userId changes

  if (loading) return <p>Loading...</p>;
  if (!user) return <p>User not found</p>;

  return (
    <div>
      <h2>{user.name}</h2>
      <p>{user.email}</p>
    </div>
  );
}
```

#### Example 2: Subscribe to Window Resize
```tsx
function WindowSize() {
  const [size, setSize] = useState({ width: window.innerWidth, height: window.innerHeight });

  useEffect(() => {
    const handleResize = () => {
      setSize({ width: window.innerWidth, height: window.innerHeight });
    };

    window.addEventListener('resize', handleResize);

    // Cleanup: remove listener on unmount
    return () => window.removeEventListener('resize', handleResize);
  }, []); // empty → only on mount/unmount

  return <p>Window: {size.width} × {size.height}</p>;
}
```

### 3. useRef – Mutable Values & DOM Access

**Purpose**:
- Store mutable values that don’t trigger re-renders
- Access DOM elements directly

#### Example 1: Focus an Input on Mount
```tsx
function TextInputWithFocus() {
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    inputRef.current?.focus(); // safely focus
  }, []);

  return <input ref={inputRef} type="text" placeholder="I'm focused!" />;
}
```

#### Example 2: Track Previous Value (without re-render on change)
```tsx
function CounterWithPrev() {
  const [count, setCount] = useState(0);
  const prevCountRef = useRef<number>();

  useEffect(() => {
    prevCountRef.current = count; // update ref, no re-render
  }, [count]);

  const prevCount = prevCountRef.current;

  return (
    <div>
      <p>Current: {count}</p>
      <p>Previous: {prevCount ?? 'None'}</p>
      <button onClick={() => setCount(c => c + 1)}>Increment</button>
    </div>
  );
}
```

#### Example 3: Store Interval ID
```tsx
const intervalRef = useRef<NodeJS.Timeout | null>(null);

// Start interval
intervalRef.current = setInterval(() => { ... }, 1000);

// Cleanup
return () => {
  if (intervalRef.current) clearInterval(intervalRef.current);
};
```

### 4. useMemo – Memoize Expensive Calculations

**Purpose**: Prevent expensive computations on every render.

#### Only recompute when dependencies change.

#### Example: Expensive Fibonacci (don’t do this on every render!)
```tsx
function FibDisplay({ n }: { n: number }) {
  const fib = useMemo(() => {
    console.log('Calculating fib...');
    let a = 0, b = 1;
    for (let i = 2; i <= n; i++) {
      [a, b] = [b, a + b];
    }
    return n <= 1 ? n : b;
  }, [n]); // only recalc when n changes

  return <p>Fib({n}) = {fib}</p>;
}
```

#### Bad useMemo (anti-pattern)
```tsx
// Don't memoize primitive values or simple ops
const doubled = useMemo(() => count * 2, [count]); // overkill
```

### 5. useCallback – Memoize Functions

**Purpose**: Prevent unnecessary re-creation of functions (critical when passing callbacks to child components).

#### Without useCallback → Child re-renders unnecessarily
```tsx
function Parent() {
  const [count, setCount] = useState(0);

  // New function on every render → Child re-renders even if props didn't change
  const handleClick = () => {
    console.log('Clicked!', count);
  };

  return <Child onClick={handleClick} count={count} />;
}
```

#### With useCallback → Stable reference
```tsx
function Parent() {
  const [count, setCount] = useState(0);

  const handleClick = useCallback(() => {
    console.log('Clicked!', count);
  }, [count]); // only recreate when count changes

  return (
    <>
      <button onClick={() => setCount(c => c + 1)}>+1</button>
      <Child onClick={handleClick} count={count} />
    </>
  );
}

// Child can now be wrapped in React.memo
const Child = React.memo(({ onClick, count }: { onClick: () => void; count: number }) => {
  console.log('Child rendered');
  return <button onClick={onClick}>Click me (count: {count})</button>;
});
```

### Bonus: When to Use Which Hook?

| Need                              | Hook          | Example                              |
|-----------------------------------|---------------|--------------------------------------|
| Fetch data / subscriptions        | useEffect     | API calls, event listeners           |
| Access DOM element                | useRef        | focus, scroll, canvas                |
| Store value without re-render     | useRef        | prev value, timer IDs                |
| Expensive calculation             | useMemo       | filtering large lists, math          |
| Pass stable callback to child     | useCallback   | event handlers in memoized children  |
| Local component state             | useState      | form inputs, toggles                 |

### Final Tips & Best Practices (2025)

1. **Always list dependencies** correctly – use ESLint rule `react-hooks/exhaustive-deps`.
2. **Prefer useCallback only when passing functions to memoized children or as effect dependencies**.
3. **useMemo is rarely needed** – profile first! Most apps don’t need it.
4. **Never put objects/arrays in dependency array without memoization**:
   ```tsx
   // Bad
   useEffect(() => {}, [user]);

   // Good
   const userId = user.id;
   useEffect(() => {}, [userId]);
   ```
