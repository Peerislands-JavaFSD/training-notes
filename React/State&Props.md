# State and Props 

## 1. What are Props?

**Props** (short for "properties") are inputs passed to a React component from its parent. They allow data to flow down the component tree in a unidirectional manner. Props are **read-only**—a component cannot modify its own props.

In TypeScript, we define props using interfaces or types to ensure type safety. This helps catch errors during development.

### Key Characteristics of Props:
- Immutable within the child component.
- Used for customization and reusability.
- Passed as attributes in JSX, like HTML attributes.

### Example: Passing Props to a Child Component

Let's create a simple `Greeting` component that receives a `name` prop.

#### Parent Component (`App.tsx`):
```tsx
import React from 'react';
import Greeting from './Greeting';

const App: React.FC = () => {
  return (
    <div>
      <Greeting name="Alice" />
    </div>
  );
};

export default App;
```

#### Child Component (`Greeting.tsx`):
```tsx
import React from 'react';

// Define the props interface
interface GreetingProps {
  name: string;  // Required prop
}

const Greeting: React.FC<GreetingProps> = (props) => {
  return <h1>Hello, {props.name}!</h1>;
};

export default Greeting;
```

#### Explanation:
- We define an interface `GreetingProps` to type the props object.
- The component uses `React.FC<GreetingProps>` to specify the functional component type.
- In JSX, `name` is passed as a prop from the parent.
- Output: Renders "Hello, Alice!".

#### Optional Props and Default Values:
You can make props optional with `?` and provide defaults.

```tsx
interface GreetingProps {
  name: string;
  age?: number;  // Optional prop
}

const Greeting: React.FC<GreetingProps> = ({ name, age = 30 }) => {  // Default value for age
  return <h1>Hello, {name}! You are {age} years old.</h1>;
};
```

Usage: `<Greeting name="Bob" />` (renders age as 30) or `<Greeting name="Bob" age={25} />`.

## 2. What is State?

**State** represents internal data that a component manages and can change over time. Unlike props, state is mutable and triggers re-renders when updated. It's used for dynamic UI elements like forms, counters, or toggles.

In functional components, we use the `useState` hook to manage state. TypeScript infers types or allows explicit generics for complex state.

### Key Characteristics of State:
- Private to the component (not passed from parent).
- Updates via setter functions (e.g., `setCount`).
- Causes the component to re-render on change.

### Example: A Simple Counter Using State

#### Component (`Counter.tsx`):
```tsx
import React, { useState } from 'react';

const Counter: React.FC = () => {
  // Initialize state with type inference (number)
  const [count, setCount] = useState(0);

  const increment = () => {
    setCount(count + 1);  // Update state
  };

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={increment}>Increment</button>
    </div>
  );
};

export default Counter;
```

#### Explanation:
- `useState(0)` initializes `count` to 0 and provides `setCount` to update it.
- Clicking the button calls `increment`, which updates state and re-renders the component.
- TypeScript infers `count` as `number`. For complex types, use generics: `useState<{ name: string }>({ name: '' })`.

#### State with Objects:
For more complex state, like an object:

```tsx
interface User {
  name: string;
  age: number;
}

const [user, setUser] = useState<User>({ name: '', age: 0 });

const updateName = (newName: string) => {
  setUser((prevUser) => ({ ...prevUser, name: newName }));  // Spread to avoid mutating directly
};
```

Always use the functional update form (`setUser(prev => ...}`) for state depending on previous values to avoid stale state issues.

## 3. Differences Between Props and State

| Aspect          | Props                          | State                          |
|-----------------|--------------------------------|--------------------------------|
| **Ownership**   | Owned by parent component     | Owned by the component itself |
| **Mutability**  | Read-only (immutable)         | Mutable (can be updated)      |
| **Purpose**     | Pass data down the tree       | Manage internal component data|
| **Re-rendering**| Doesn't trigger re-render on change (unless parent re-renders) | Triggers re-render on update |
| **Example Use** | Configuration from parent     | User input, toggles, counters |
| **TypeScript**  | Defined via interface in child| Inferred or generic in `useState` |

- **When to Use Props**: For static or parent-controlled data.
- **When to Use State**: For data that changes within the component (e.g., due to user interactions).
- **Props Drilling**: If props need to go deep, consider context or state management libraries like Redux.
- **Lifting State Up**: If child components need to share state, move it to a common parent.

## 4. Combining Props and State: A Practical Example

Let's build a `TodoList` where the parent passes initial todos (props), and the child manages additions (state).

#### Parent (`App.tsx`):
```tsx
import React from 'react';
import TodoList from './TodoList';

const initialTodos = ['Buy milk', 'Walk the dog'];

const App: React.FC = () => {
  return <TodoList initialTodos={initialTodos} />;
};

export default App;
```

#### Child (`TodoList.tsx`):
```tsx
import React, { useState } from 'react';

interface TodoListProps {
  initialTodos: string[];
}

const TodoList: React.FC<TodoListProps> = ({ initialTodos }) => {
  const [todos, setTodos] = useState(initialTodos);
  const [newTodo, setNewTodo] = useState('');

  const addTodo = () => {
    if (newTodo) {
      setTodos([...todos, newTodo]);
      setNewTodo('');
    }
  };

  return (
    <div>
      <ul>
        {todos.map((todo, index) => <li key={index}>{todo}</li>)}
      </ul>
      <input
        type="text"
        value={newTodo}
        onChange={(e) => setNewTodo(e.target.value)}
      />
      <button onClick={addTodo}>Add Todo</button>
    </div>
  );
};

export default TodoList;
```

#### Explanation:
- Props (`initialTodos`) initialize the state.
- State (`todos` and `newTodo`) handles dynamic updates.
- This demonstrates how props seed state, but state takes over for mutations.

### Best Practices
- Avoid mutating props or state directly—use setters.
- Use TypeScript generics for complex state types.
- For performance, memoize components with `React.memo` if props don't change often.
- Debug with React DevTools to inspect props and state.

