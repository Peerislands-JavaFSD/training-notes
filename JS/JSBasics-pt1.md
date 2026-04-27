

# **JavaScript Operators**

JavaScript provides a wide range of operators used for performing operations on variables and values.

---

## **1. Arithmetic Operators**

Used to perform mathematical operations.

| Operator | Description         | Example  |
| -------- | ------------------- | -------- |
| `+`      | Addition            | `a + b`  |
| `-`      | Subtraction         | `a - b`  |
| `*`      | Multiplication      | `a * b`  |
| `/`      | Division            | `a / b`  |
| `%`      | Modulus (remainder) | `a % b`  |
| `**`     | Exponentiation      | `a ** b` |
| `++`     | Increment           | `a++`    |
| `--`     | Decrement           | `a--`    |

---

## **2. Assignment Operators**

Used to assign values to variables.

| Operator | Meaning             | Example  |
| -------- | ------------------- | -------- |
| `=`      | Assign              | `x = 5`  |
| `+=`     | Add and assign      | `x += 2` |
| `-=`     | Subtract and assign | `x -= 2` |
| `*=`     | Multiply and assign | `x *= 2` |
| `/=`     | Divide and assign   | `x /= 2` |
| `%=`     | Modulo and assign   | `x %= 2` |

---

## **3. Comparison Operators**

Used to compare two values.

| Operator | Description                 | Example                   |
| -------- | --------------------------- | ------------------------- |
| `==`     | Equal to (loose)            | `2 == "2"` returns true   |
| `===`    | Strict equal (type + value) | `2 === "2"` returns false |
| `!=`     | Not equal (loose)           | `2 != "2"` returns false  |
| `!==`    | Strict not equal            | `2 !== "2"` returns true  |
| `>`      | Greater than                | `a > b`                   |
| `<`      | Less than                   | `a < b`                   |
| `>=`     | Greater or equal            | `a >= b`                  |
| `<=`     | Less or equal               | `a <= b`                  |

---

## **4. Logical Operators**

Used to combine multiple conditions.

| Operator | Meaning     | Example           |            |        |   |         |
| -------- | ----------- | ----------------- | ---------- | ------ | - | ------- |
| `&&`     | Logical AND | `a > 3 && b < 10` |            |        |   |         |
| `        |             | `                 | Logical OR | `a > 3 |   | b < 10` |
| `!`      | Logical NOT | `!true`           |            |        |   |         |

---

## **5. Ternary Operator**

Short form of if-else.

```js
let result = age >= 18 ? "Adult" : "Minor";
```

---

## **6. Type Operators**

| Operator     | Description        | Example               |
| ------------ | ------------------ | --------------------- |
| `typeof`     | Returns data type  | `typeof "hello"`      |
| `instanceof` | Checks object type | `obj instanceof Date` |

---

# **Basic JavaScript Inbuilt Functions**

JavaScript provides built-in functions for strings, numbers, arrays, and general utilities.

---

# **1. String Functions**

Frequently used functions for string manipulation.

| Function            | Description               |
| ------------------- | ------------------------- |
| `length`            | Returns string length     |
| `toUpperCase()`     | Converts to upper case    |
| `toLowerCase()`     | Converts to lower case    |
| `trim()`            | Removes whitespace        |
| `slice(start, end)` | Extracts part of string   |
| `includes(value)`   | Checks substring presence |
| `replace(a, b)`     | Replaces first match      |
| `split(separator)`  | Splits string into array  |

Example:

```js
"Hello World".toUpperCase();
```

---

# **2. Number Functions**

| Function       | Description                  |
| -------------- | ---------------------------- |
| `Number()`     | Converts to number           |
| `parseInt()`   | Parses integer from string   |
| `parseFloat()` | Parses float                 |
| `toFixed(n)`   | Formats number to n decimals |
| `isNaN(value)` | Checks if value is NaN       |

Example:

```js
parseInt("42");
```

---

# **3. Array Functions**

| Function          | Description              |
| ----------------- | ------------------------ |
| `push()`          | Add element to end       |
| `pop()`           | Remove last element      |
| `shift()`         | Remove first element     |
| `unshift()`       | Add element to beginning |
| `indexOf(value)`  | Find index               |
| `includes(value)` | Check presence           |
| `slice()`         | Copy part of array       |
| `splice()`        | Add/remove elements      |
| `join()`          | Convert array to string  |
| `map()`           | Transform array          |
| `filter()`        | Filter values            |
| `reduce()`        | Combine values           |
| `forEach()`       | Loop through array       |

Example:

```js
[1,2,3].map(x => x * 2);
```

---

# **4. Math Functions**

| Function            | Description                      |
| ------------------- | -------------------------------- |
| `Math.random()`     | Generates random number (0 to 1) |
| `Math.floor()`      | Rounds down                      |
| `Math.ceil()`       | Rounds up                        |
| `Math.round()`      | Normal rounding                  |
| `Math.max(a,b,...)` | Maximum value                    |
| `Math.min(a,b,...)` | Minimum value                    |
| `Math.pow(a,b)`     | a to the power b                 |

Example:

```js
Math.floor(Math.random() * 10);
```

---

# **5. Date Functions**

| Function        | Description          |
| --------------- | -------------------- |
| `new Date()`    | Creates current date |
| `getFullYear()` | Year                 |
| `getMonth()`    | Month index (0-11)   |
| `getDate()`     | Day of month         |
| `getHours()`    | Hours                |
| `getMinutes()`  | Minutes              |

Example:

```js
const now = new Date();
```

---

# **6. Utility Functions**

| Function           | Description            |
| ------------------ | ---------------------- |
| `JSON.stringify()` | Convert object to JSON |
| `JSON.parse()`     | Convert JSON to object |
| `setTimeout()`     | Run after delay        |
| `setInterval()`    | Repeat function        |

Example:

```js
setTimeout(() => console.log("Done"), 1000);
```

---

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
