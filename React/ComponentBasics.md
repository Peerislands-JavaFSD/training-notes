# React Functional Components


Functional components are simple JavaScript functions that return UI elements. They replaced class components in most cases because they're easier to write, test, and understand.


## 1. Creating a React App with TypeScript

Start by setting up a new project using Vite, a fast build tool that supports React and TypeScript out of the box.

### Prerequisites
- Node.js (version 18 or higher) installed. Download from [nodejs.org](https://nodejs.org).
- A code editor like VS Code.

### Step-by-Step Setup
1. **Open your terminal** and create the project:
   ```
   npm create vite@latest my-functional-app -- --template react-ts
   ```
    - `my-functional-app` is your project name.
    - `--template react-ts` adds React with TypeScript.

2. **Navigate to the project**:
   ```
   cd my-functional-app
   ```

3. **Install dependencies**:
   ```
   npm install
   ```

4. **Run the development server**:
   ```
   npm run dev
   ```
    - Visit `http://localhost:5173` in your browser. You'll see a default React logo and counter—our starting point!

### Quick Tour of the Generated App
- `src/App.tsx`: The main component file. This is where we'll build.
- `src/main.tsx`: The entry point that renders `App` to the DOM.
- `index.html`: The single HTML file (React apps are Single Page Applications).
- `tsconfig.json`: TypeScript settings (e.g., handles JSX as `react-jsx`).

TypeScript is configured automatically, so your editor will provide autocompletion and error highlighting. Let's edit `src/App.tsx` to begin.

## 2. Components: The Building Blocks of React

A **component** is a reusable piece of UI, like a button or a card. Functional components are just functions that return what the UI should look like.

### Key Ideas
- Components start with a capital letter (e.g., `MyButton`).
- They can be nested: One component renders others.
- React renders the root component (`App`) and re-renders when data changes.

### Basic Functional Component
Replace the content in `src/App.tsx` with this:
```tsx
import React from 'react';

const App = (): JSX.Element => {
  return (
    <div>
      <h1>Welcome to My App!</h1>
      <p>This is a functional component.</p>
    </div>
  );
};

export default App;
```
- **Explanation**:
    - `const App = (): JSX.Element => { ... }`: Defines the component with explicit return type `JSX.Element` for TypeScript safety.
    - `return (...)`: Describes the UI. The curly braces `{}` group JSX (next section).
    - `export default App;`: Makes it importable in `main.tsx`.
- Save the file—Vite hot-reloads the browser automatically. You should see "Welcome to My App!"

### Creating a Child Component
Components can be composed. Create `src/Greeting.tsx`:
```tsx
import React from 'react';

const Greeting = (): JSX.Element => {
  return <h2>Hello from a child component!</h2>;
};

export default Greeting;
```
Now import and use it in `App.tsx`:
```tsx
import React from 'react';
import Greeting from './Greeting';

const App = (): JSX.Element => {
  return (
    <div>
      <h1>Welcome to My App!</h1>
      <Greeting /> {/* Renders the child */}
    </div>
  );
};

export default App;
```
- `<Greeting />` is like calling the function. Self-closing tags for components without children.

Components make code modular: `Greeting` could be reused anywhere.

## 3. JSX: JavaScript XML for UI

**JSX** is a syntax extension that lets you write HTML-like code inside JavaScript. It's not HTML—it's transformed into `React.createElement()` calls during build. JSX makes UI code readable.

### Key Rules
- **JSX in Parentheses**: Wrap returns in `()` for multi-line.
- **Expressions in Curly Braces**: Embed JS like `{2 + 2}` or `{userName}`.
- **Attributes as Props**: Use `className` (not `class`), `onClick` for events.
- **Fragments**: Use `<></>` or `<React.Fragment>` to wrap multiple elements without extra DOM nodes.
- **No Comments Inside**: Use `{/* comment */}`.

### Basic JSX Examples
Update `App.tsx`:
```tsx
import React from 'react';
import Greeting from './Greeting';

const App = (): JSX.Element => {
  const title = 'My React App'; // JS variable
  const isLoggedIn = true; // Conditional

  return (
    <div className="app"> {/* className for CSS */}
      <h1>{title}</h1> {/* JS expression */}
      <Greeting />
      {isLoggedIn ? <p>You are logged in!</p> : <p>Please log in.</p>} {/* Ternary conditional */}
      <ul>
        <li>Item 1</li>
        <li>Item 2</li>
      </ul>
    </div>
  );
};

export default App;
```
- **Explanation**:
    - `{title}`: Inserts the variable's value.
    - Ternary `? :`: Basic if-else for rendering (JSX can't use `if` directly).
    - Lists need keys in real apps (e.g., `<li key={id}>`), but skipped for basics.

Add some styles in `src/App.css` (imported automatically):
```css
.app {
  text-align: center;
  padding: 20px;
}
```
JSX blends markup and logic seamlessly.

## 4. Props: Passing Data Between Components

**Props** (properties) let parents pass data to children, like function arguments. Props are read-only—children can't change them.

In TypeScript, we use interfaces to type props for safety.

### Typing and Passing Props
Update `Greeting.tsx` to accept props:
```tsx
import React from 'react';

interface GreetingProps {
  name: string;      // Required string
  age?: number;      // Optional number (?)
}

const Greeting = ({ name, age }: GreetingProps): JSX.Element => {
  return (
    <h2>
      Hello, {name}! {age && `(Age: ${age})`} {/* age && prevents undefined */}
    </h2>
  );
};

export default Greeting;
```
- **Interface**: Defines prop shape. `?` makes optional.
- **Destructuring**: `{ name, age }` pulls values from props object, typed by `GreetingProps`.
- `: JSX.Element`: Explicit return type.

In `App.tsx`, pass props:
```tsx
import React from 'react';
import Greeting from './Greeting';

const App = (): JSX.Element => {
  const title = 'My React App';
  const isLoggedIn = true;

  return (
    <div className="app">
      <h1>{title}</h1>
      <Greeting name="Alice" age={25} /> {/* Pass props */}
      <Greeting name="Bob" /> {/* age optional */}
      {isLoggedIn ? <p>You are logged in!</p> : <p>Please log in.</p>}
    </div>
  );
};

export default App;
```
- **Explanation**:
    - `<Greeting name="Alice" age={25} />`: Props as attributes.
    - In `Greeting`, `{name}` displays "Alice".
    - TypeScript errors if you pass wrong types (e.g., `age="25"`—must be number).

### Default Props
For optionals, add defaults in destructuring:
```tsx
const Greeting = ({ name, age = 18 }: GreetingProps): JSX.Element => {
  // ...
};
```
Now `<Greeting name="Bob" />` uses age 18.

Props enable reusable components: `Greeting` works with any name/age.

## 5. State: Managing Dynamic Data

**State** holds data that changes over time (e.g., form input, counters). Use the `useState` hook in functional components—it returns `[value, setter]`.

In TypeScript, type the initial value.

### Basic State with useState
Update `App.tsx` for a counter:
```tsx
import React, { useState } from 'react'; // Import useState
import Greeting from './Greeting';

const App = (): JSX.Element => {
  const [count, setCount] = useState<number>(0); // Type: number, initial 0

  const increment = (): void => {
    setCount(count + 1); // Updater function
  };

  const title = 'My React App';
  const isLoggedIn = true;

  return (
    <div className="app">
      <h1>{title}</h1>
      <Greeting name="Alice" age={25} />
      <Greeting name="Bob" />
      {isLoggedIn ? <p>You are logged in!</p> : <p>Please log in.</p>}
      
      <div>
        <p>Count: {count}</p> {/* Displays state */}
        <button onClick={increment}>Increment</button> {/* Event handler */}
      </div>
    </div>
  );
};

export default App;
```
- **Explanation**:
    - `useState<number>(0)`: Creates state. `<number>` types it (infers if simple).
    - `[count, setCount]`: `count` is the value; `setCount` updates it, triggering re-render.
    - `onClick={increment}`: Calls function on click. Typed as `(): void`.
    - Updates are batched—`setCount` is async, so use callback for dependencies: `setCount(prev => prev + 1)`.

### State with Objects
For complex data, like a user form. Update `App.tsx`:
```tsx
import React, { useState } from 'react';
import Greeting from './Greeting';

interface User {
  name: string;
  age: number;
}

const App = (): JSX.Element => {
  const [user, setUser] = useState<User>({ name: '', age: 0 });

  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    setUser({ ...user, name: e.target.value }); // Spread to update immutably
  };

  const handleAgeChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    setUser({ ...user, age: parseInt(e.target.value) || 0 });
  };

  const title = 'My React App';
  const isLoggedIn = true;

  return (
    <div className="app">
      <h1>{title}</h1>
      <Greeting name={user.name || 'Guest'} age={user.age} />
      
      <div>
        <input 
          type="text" 
          value={user.name} 
          onChange={handleNameChange} 
          placeholder="Name" 
        />
        <input 
          type="number" 
          value={user.age} 
          onChange={handleAgeChange} 
          placeholder="Age" 
        />
        <p>User: {user.name}, Age: {user.age}</p>
      </div>
      
      {isLoggedIn ? <p>You are logged in!</p> : <p>Please log in.</p>}
    </div>
  );
};

export default App;
```
- **Explanation**:
    - `useState<User>(...)`: Types the object.
    - `{ ...user, name: newValue }`: Updates one field without mutating the old object.
    - `value={user.name}`: Controlled input—state drives the UI.
    - Event typing: `React.ChangeEvent<HTMLInputElement>` for inputs. Handlers typed as `(): void`.

State makes components interactive: Typing updates `Greeting` live.
