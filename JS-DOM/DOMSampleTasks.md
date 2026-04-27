### Task 1: Highlight Paragraphs

Create a script that highlights all paragraphs (`<p>`) on a webpage by changing their background color.

#### Example HTML:
```html
<!DOCTYPE html>
<html>
<head>
  <title>Highlight Paragraphs</title>
  <style>
    .highlight {
      background-color: yellow;
    }
  </style>
</head>
<body>
  <p>Paragraph 1</p>
  <p>Paragraph 2</p>
  <p>Paragraph 3</p>
  <script src="highlight.js"></script>
</body>
</html>
```

#### highlight.js:
```javascript
let paragraphs = document.getElementsByTagName('p');
for (let i = 0; i < paragraphs.length; i++) {
  paragraphs[i].classList.add('highlight');
}
```

### Task 2: Create a Navigation Menu

Create a script that dynamically generates a navigation menu from an array of links. Each link should be an item in an unordered list (`<ul>`).

#### Example HTML:
```html
<!DOCTYPE html>
<html>
<head>
  <title>Dynamic Navigation Menu</title>
</head>
<body>
  <div id="navMenu"></div>
  <script src="menu.js"></script>
</body>
</html>
```

#### menu.js:
```javascript
let links = [
  { href: '#home', text: 'Home' },
  { href: '#about', text: 'About' },
  { href: '#services', text: 'Services' },
  { href: '#contact', text: 'Contact' }
];

let navMenu = document.getElementById('navMenu');
let ul = document.createElement('ul');

links.forEach(link => {
  let li = document.createElement('li');
  let a = document.createElement('a');
  a.href = link.href;
  a.textContent = link.text;
  li.appendChild(a);
  ul.appendChild(li);
});

navMenu.appendChild(ul);
```

### Task 3: Update Table Content

Create a script that updates the content of a table with new data.

#### Example HTML:
```html
<!DOCTYPE html>
<html>
<head>
  <title>Update Table</title>
</head>
<body>
  <table id="dataTable" border="1">
    <thead>
      <tr>
        <th>Name</th>
        <th>Age</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>John</td>
        <td>30</td>
      </tr>
      <tr>
        <td>Jane</td>
        <td>25</td>
      </tr>
    </tbody>
  </table>
  <script src="updateTable.js"></script>
</body>
</html>
```

#### updateTable.js:
```javascript
let newData = [
  { name: 'Alice', age: 28 },
  { name: 'Bob', age: 32 },
  { name: 'Charlie', age: 22 }
];

let tbody = document.querySelector('#dataTable tbody');
tbody.innerHTML = ''; // Clear existing rows

newData.forEach(data => {
  let tr = document.createElement('tr');
  
  let tdName = document.createElement('td');
  tdName.textContent = data.name;
  tr.appendChild(tdName);
  
  let tdAge = document.createElement('td');
  tdAge.textContent = data.age;
  tr.appendChild(tdAge);
  
  tbody.appendChild(tr);
});
```

### Task 4: Add Class to Specific Elements

Create a script that adds a specific class to all elements with a given tag name, for example, adding a class to all `<li>` elements.

#### Example HTML:
```html
<!DOCTYPE html>
<html>
<head>
  <title>Add Class to Elements</title>
  <style>
    .highlight {
      background-color: lightblue;
    }
  </style>
</head>
<body>
  <ul>
    <li>Item 1</li>
    <li>Item 2</li>
    <li>Item 3</li>
  </ul>
  <script src="addClass.js"></script>
</body>
</html>
```

#### addClass.js:
```javascript
let listItems = document.getElementsByTagName('li');
for (let i = 0; i < listItems.length; i++) {
  listItems[i].classList.add('highlight');
}
```

### Task 5: Create and Append Elements

Create a script that creates a series of `<div>` elements with a specific class and appends them to a container.

#### Example HTML:
```html
<!DOCTYPE html>
<html>
<head>
  <title>Create and Append Elements</title>
  <style>
    .box {
      width: 100px;
      height: 100px;
      background-color: lightgray;
      margin: 5px;
    }
  </style>
</head>
<body>
  <div id="container"></div>
  <script src="createElements.js"></script>
</body>
</html>
```

#### createElements.js:
```javascript
let container = document.getElementById('container');

for (let i = 0; i < 5; i++) {
  let newDiv = document.createElement('div');
  newDiv.classList.add('box');
  container.appendChild(newDiv);
}
```
