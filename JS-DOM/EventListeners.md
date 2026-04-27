Event listeners in JavaScript allow you to respond to user interactions, such as clicks, mouse movements, key presses, and more. Understanding how events propagate through the DOM (Document Object Model) is crucial for managing these interactions effectively. There are two main phases of event propagation: capturing and bubbling.

### Adding Event Listeners

Event listeners are added to DOM elements using the `addEventListener` method:

```javascript
element.addEventListener(event, handler, options);
```

- `event`: A string representing the event type (e.g., 'click', 'mouseover').
- `handler`: A function to be executed when the event occurs.
- `options` (optional): An object or boolean indicating characteristics about the event listener.

### Event Propagation

Event propagation defines the order in which events are handled when they occur on nested elements. There are three phases of event propagation:

1. **Capturing Phase**: The event travels from the root of the document down to the target element.
2. **Target Phase**: The event reaches the target element.
3. **Bubbling Phase**: The event bubbles back up from the target element to the root of the document.

### Event Capturing and Bubbling

- **Capturing**: Also known as "trickling," the event is first captured by the outermost element and propagated inward to the target element.
- **Bubbling**: The event is first captured and handled by the target element and then propagated outward to the outermost element.

### Example: Capturing and Bubbling

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Event Propagation</title>
  <style>
    .outer { padding: 50px; background-color: lightblue; }
    .inner { padding: 50px; background-color: lightcoral; }
  </style>
</head>
<body>
  <div class="outer">
    Outer
    <div class="inner">
      Inner
    </div>
  </div>

  <script>
    const outer = document.querySelector('.outer');
    const inner = document.querySelector('.inner');

    // Capturing phase
    outer.addEventListener('click', (e) => {
      console.log('Outer Capturing');
    }, true);

    inner.addEventListener('click', (e) => {
      console.log('Inner Capturing');
    }, true);

    // Bubbling phase
    outer.addEventListener('click', (e) => {
      console.log('Outer Bubbling');
    });

    inner.addEventListener('click', (e) => {
      console.log('Inner Bubbling');
    });
  </script>
</body>
</html>
```

### Output

If you click on the inner div:

1. **Capturing Phase**:
   - "Outer Capturing"
   - "Inner Capturing"

2. **Target Phase**:
   - "Inner Bubbling"

3. **Bubbling Phase**:
   - "Outer Bubbling"

### Event Options

When adding an event listener, you can specify options to control its behavior:

- `capture`: A boolean indicating whether the event should be captured during the capturing phase.
- `once`: If true, the listener will be automatically removed after it is invoked.
- `passive`: If true, indicates that the function will never call `preventDefault`. If a passive listener does call `preventDefault`, the user agent will ignore it and generate a console warning.

Example:

```javascript
element.addEventListener('click', handler, { capture: true, once: true, passive: true });
```

### Preventing Default Behavior and Stopping Propagation

- **Prevent Default**: Stops the default action of the event (e.g., a link redirecting to a new page).

  ```javascript
  element.addEventListener('click', (e) => {
    e.preventDefault();
  });
  ```

- **Stop Propagation**: Stops the event from propagating further.

  ```javascript
  element.addEventListener('click', (e) => {
    e.stopPropagation();
  });
  ```

- **Stop Immediate Propagation**: Stops the event from propagating and prevents other listeners on the same element from being called.

  ```javascript
  element.addEventListener('click', (e) => {
    e.stopImmediatePropagation();
  });
  ```

### Summary

Event listeners are a fundamental part of interacting with the DOM in JavaScript. Understanding the phases of event propagation—capturing and bubbling—helps you control the flow of events and manage complex interactions. Using options and methods like `preventDefault`, `stopPropagation`, and `stopImmediatePropagation` provides finer control over event handling.