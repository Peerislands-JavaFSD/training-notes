In JavaScript, non-primitive data types are complex data structures that can hold multiple values and have dynamic properties and methods. Unlike primitive data types (like numbers, strings, booleans, null, and undefined), which are immutable and have a fixed size, non-primitive data types are mutable and can grow dynamically. Here are the main non-primitive data types in JavaScript:

### 1. Objects

JavaScript objects are unordered collections of key-value pairs. They are used to store various data and more complex entities.

#### Example:

```javascript
const person = {
  firstName: 'John',
  lastName: 'Doe',
  age: 30,
  hobbies: ['reading', 'music'],
  address: {
    street: '123 Main St',
    city: 'Anytown'
  },
  greet: function() {
    return `Hello, my name is ${this.firstName} ${this.lastName}.`;
  }
};
```

- **Properties**: Key-value pairs where keys are strings (or symbols) and values can be any JavaScript data type, including other objects.
- **Methods**: Functions stored as object properties.

### 2. Arrays

Arrays are ordered collections of data stored using a numerical index. They can hold multiple values of any data type, including other arrays and objects.

#### Example:

```javascript
const numbers = [1, 2, 3, 4, 5];
const fruits = ['Apple', 'Banana', 'Cherry'];
const mixedArray = [1, 'two', { key: 'value' }, [4, 5, 6]];
```

- **Indexed Access**: Elements are accessed using zero-based indices (`numbers[0]` returns `1`).
- **Methods**: Arrays have built-in methods like `push`, `pop`, `splice`, `forEach`, etc., for manipulation and iteration.

### 3. Functions

Functions in JavaScript are objects and can be assigned to variables, passed as arguments, and returned from other functions. They encapsulate a block of code that can be called and executed.

#### Example:

```javascript
function greet(name) {
  return `Hello, ${name}!`;
}

const result = greet('John');
console.log(result); // Output: Hello, John!
```

- **First-Class Citizens**: Functions can be treated like any other data type.
- **Anonymous Functions**: Functions without a name (`const func = function() { ... };`).
- **Arrow Functions (ES6+)**: Concise syntax for defining functions (`const add = (a, b) => a + b;`).

### 4. Dates

JavaScript has a built-in `Date` object for working with dates and times. It encapsulates a specific date and time, allowing you to perform operations like formatting, comparison, and arithmetic.

#### Example:

```javascript
const now = new Date();
console.log(now); // Output: Current date and time

const tomorrow = new Date();
tomorrow.setDate(now.getDate() + 1);
console.log(tomorrow); // Output: Date object representing tomorrow
```

- **Constructor Methods**: `new Date()` creates a `Date` object representing the current date and time.
- **Methods**: `getDate()`, `setDate()`, `getMonth()`, `getHours()`, etc., for manipulating and retrieving date components.

### 5. Regular Expressions (RegExp)

Regular expressions are objects used for matching patterns in strings. They provide a powerful way to search, replace, and extract data based on patterns.

#### Example:

```javascript
const pattern = /[a-zA-Z]+/;
const text = 'Hello, World!';
console.log(pattern.test(text)); // Output: true (matches alphabetical characters)
```

- **Pattern Matching**: Regular expressions define patterns using meta-characters (`^`, `$`, `[]`, `+`, `*`, etc.).
- **Methods**: `test()`, `exec()`, `match()`, `replace()`, etc., for working with strings.

### 6. Objects with Special Behaviors

JavaScript also has other non-primitive types like `Map`, `Set`, `WeakMap`, and `WeakSet`, which provide specialized data structures for storing collections of values with additional functionalities and behaviors.

#### Example:

```javascript
// Using Map
const map = new Map();
map.set('name', 'John');
map.set('age', 30);
console.log(map.get('name')); // Output: John
```

- **Map**: Key-value pairs where keys can be any value (including objects or primitive values).
- **Set**: Collection of unique values.
- **WeakMap** and **WeakSet**: Variants that allow for garbage collection of keys that are no longer referenced.

### Summary

JavaScript's non-primitive data types are versatile and essential for building complex applications. They enable you to store and manipulate structured data, work with dates and regular expressions, and leverage specialized data structures like maps and sets. Understanding these data types is crucial for effective JavaScript programming and developing robust applications.