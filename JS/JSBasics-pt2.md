
### Type Coercion

JavaScript performs type coercion implicitly when operators expect one type but receive another. This can lead to unexpected results if not understood correctly.

#### Example of Type Coercion:

```javascript
let x = 10 + '5'; // '105' (number 10 coerced into string)
let y = '20' - 5; // 15 (string '20' coerced into number)
```

#### Explicit Type Conversion:

You can explicitly convert types using JavaScript functions like `parseInt`, `parseFloat`, or using the unary `+` operator:

```javascript
let numString = '123';
let num = parseInt(numString); // converts '123' to 123
let floatString = '3.14';
let float = parseFloat(floatString); // converts '3.14' to 3.14
let number = +'42'; // converts '42' to 42 using the unary + operator
```

### `let` and `const` Declarations

#### `let` Declaration:

- **Purpose**: Declares a block-scoped variable, optionally initializing it to a value.
- **Mutable**: Variables declared with `let` can be reassigned.

Example:

```javascript
let name = 'Alice';
name = 'Bob'; // valid
```

#### `const` Declaration:

- **Purpose**: Declares a block-scoped constant, whose value cannot be re-assigned.
- **Immutable**: Once assigned, the value of a `const` cannot change.

Example:

```javascript
const pi = 3.14;
pi = 3.14159; // Error: Assignment to constant variable
```

#### Best Practices:

- Use `const` by default for variables that should not be reassigned.
- Use `let` for variables that will be reassigned.

### Template Literals

Template literals (introduced in ES6) provide an easy way to embed expressions and multi-line strings in JavaScript.

#### Syntax:

```javascript
let name = 'Alice';
let greeting = `Hello, ${name}!`;

console.log(greeting); // Outputs: Hello, Alice!
```

#### Features:

- **Expression interpolation**: Embed variables and expressions using `${...}` inside backticks (`).
- **Multi-line strings**: Write multi-line strings without concatenation or special characters.

#### Example:

```javascript
let firstName = 'John';
let lastName = 'Doe';
let fullName = `${firstName} ${lastName}`;

let message = `
    Hello ${fullName},

    Welcome to our website!
`;

console.log(message);
```

