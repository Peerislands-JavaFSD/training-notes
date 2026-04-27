# Event Handling, Controlled & Uncontrolled Components

## 1. Event Handling in React + TypeScript

React uses **Synthetic Events** — a cross-browser wrapper around native DOM events.

### Correctly Typing Event Handlers (Most Important!)

| HTML Element       | Event Type in React + TS                              | Example Handler Signature                          |
|---------------------|--------------------------------------------------------|-----------------------------------------------------|
| button, div, span   | `React.MouseEvent<HTMLButtonElement>`                 | `(e: React.MouseEvent<HTMLButtonElement>) => void` |
| input, textarea     | `React.ChangeEvent<HTMLInputElement>`                 | `(e: React.ChangeEvent<HTMLInputElement>) => void` |
| select              | `React.ChangeEvent<HTMLSelectElement>`                | same as above                                       |
| form                | `React.FormEvent<HTMLFormElement>`                    | `(e: React.FormEvent) => void`                      |
| keyboard            | `React.KeyboardEvent<HTMLInputElement>`               | `(e: React.KeyboardEvent) => void`                  |

### Best Practice: Create Reusable Typed Handlers

```tsx
import React, { useState } from 'react';

function EventDemo() {
  const [text, setText] = useState('');

  // Input change — most common
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setText(e.target.value);
  };

  // Button click
  const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    console.log('Clicked!', e.currentTarget); // always use currentTarget in React
  };

  // Form submit
  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault(); // Always prevent default on forms!
    alert('Submitted: ' + text);
  };

  // Key press (e.g., Enter to submit)
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      alert('Enter pressed: ' + text);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        value={text}
        onChange={handleInputChange}
        onKeyDown={handleKeyDown}
        placeholder="Type something..."
      />
      <button type="button" onClick={handleClick}>
        Log Click
      </button>
      <button type="submit">Submit Form</button>
    </form>
  );
}
```

**Pro Tip**: Use `e.currentTarget` instead of `e.target` — it's safer in React.

## 2. Controlled vs Uncontrolled Components

| Feature                  | Controlled Components                     | Uncontrolled Components                  |
|--------------------------|-------------------------------------------|------------------------------------------|
| Value managed by          | React state                               | DOM itself                               |
| Default in modern React   | YES (Recommended)                         | Only when necessary                      |
| Easy to validate/reset   | Yes                                       | Hard                                     |
| Real-time access to value | Yes                                       | Need ref to read                         |
| Performance              | Slightly more renders                     | Slightly faster                          |

### Example 1: Controlled Input (The React Way)

```tsx
function ControlledForm() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [age, setAge] = useState<number | ''>('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log({ name, email, age });
  };

  return (
    <form onSubmit={handleSubmit}>
      <h3>Controlled Form</h3>
      <input
        type="text"
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="Name"
      />
      <br />

      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
      />
      <br />

      <input
        type="number"
        value={age}
        onChange={(e) => setAge(e.target.value ? Number(e.target.value) : '')}
        placeholder="Age"
      />
      <br />

      <button type="submit">Submit</button>
      <button type="button" onClick={() => { setName(''); setEmail(''); setAge(''); }}>
        Reset
      </button>
    </form>
  );
}
```

**Benefits**:
- Instant access to form data
- Easy validation
- Easy to reset or pre-fill
- Works perfectly with libraries like React Hook Form, Zod, etc.

### Example 2: Uncontrolled Input (Using useRef)

Use only when:
- Integrating with non-React code
- Simple forms with no validation
- File inputs (always uncontrolled!)

```tsx
import { useRef } from 'react';

function UncontrolledForm() {
  const nameRef = useRef<HTMLInputElement>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Read values manually from DOM
    const name = nameRef.current?.value;
    const file = fileRef.current?.files?.[0];

    console.log('Name:', name);
    console.log('File:', file?.name);
  };

  return (
    <form onSubmit={handleSubmit}>
      <h3>Uncontrolled Form</h3>
      <input type="text" ref={nameRef} placeholder="Name (uncontrolled)" />
      <br />
      <input type="file" ref={fileRef} />
      <br />
      <button type="submit">Submit</button>
    </form>
  );
}
```

**File inputs are always uncontrolled** — you cannot set `value`.

### Real-World: Mixed Form (Best of Both Worlds)

```tsx
function RegistrationForm() {
  const [email, setEmail] = useState('');
  const passwordRef = useRef<HTMLInputElement>(null);
  const termsRef = useRef<HTMLInputElement>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!termsRef.current?.checked) {
      alert('Accept terms!');
      return;
    }

    console.log({
      email,
      password: passwordRef.current?.value,
      termsAccepted: termsRef.current?.checked,
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email (controlled)"
        required
      />
      <br />

      <input
        type="password"
        ref={passwordRef}
        placeholder="Password (uncontrolled)"
        required
      />
      <br />

      <label>
        <input type="checkbox" ref={termsRef} />
        I accept terms
      </label>
      <br />

      <button type="submit">Register</button>
    </form>
  );
}
```

## Summary Table

| Use Case                            | Recommended Approach       |
|-------------------------------------|----------------------------|
| Forms with validation               | Controlled         |
| Instant form value access           | Controlled         |
| Resetting or pre-filling forms      | Controlled         |
| File input                          | Uncontrolled (must) |
| Integrating with legacy JS          | Uncontrolled       |
| Performance-critical large forms    | Controlled + React Hook Form) |

## Final Best Practices (2025)

1. Default to **controlled components** — they’re predictable and testable.
2. Always type your event handlers properly in TypeScript.
3. Use `e.preventDefault()` on forms.
4. Use `currentTarget`, not `target`.
5. For large forms → use React Hook Form + Zod (controlled under the hood).
6. Never do this:
   ```tsx
   // Bad — loses type safety
   onChange={(e) => setValue(e.target.value)}
   // Good
   onChange={(e: React.ChangeEvent<HTMLInputElement>) => setValue(e.target.value)}
   ```
