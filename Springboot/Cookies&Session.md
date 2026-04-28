# Spring Boot Session Handling and Cookies

Spring Boot applications often need to remember a user between requests. Since HTTP is stateless, the server does not naturally “remember” who the user is after one request ends. That is where **sessions** and **cookies** come in.

In simple words:

* **Cookies** are small pieces of data stored in the browser.
* **Sessions** are data stored on the server, usually linked to the user through a session ID.
* A **cookie** often carries the **session ID**.
* The server uses that session ID to find the matching session data.

---

# 1. Why do we need sessions and cookies?

Suppose a user logs in to your application.

Without session handling, every request would look like a new user request.

With sessions:

* user logs in once
* server creates session data
* browser stores a session cookie
* future requests send that cookie automatically
* server recognizes the user

This is useful for:

* login/logout systems
* shopping carts
* user preferences
* temporary request data
* keeping track of user state

---

# 2. HTTP is stateless

HTTP itself does not remember previous requests.

For example:

1. User sends `POST /login`
2. Server checks credentials
3. Server sends response

Next request:

4. User sends `GET /profile`

Without sessions, server would not know this is the same user unless you send credentials again.

So we use:

* cookies to store a small identifier in the browser
* sessions to store user-related data on the server

---

# 3. What is a cookie?

A cookie is a key-value pair stored in the browser.

Example:

```text
username=Arun
theme=dark
JSESSIONID=ABCD1234XYZ
```

Cookies are sent by the browser to the server automatically with each request to the same domain.

## Cookie characteristics

A cookie can have:

* name
* value
* max age
* path
* domain
* secure flag
* HttpOnly flag
* SameSite attribute

## Example use cases

* remember language preference
* keep user logged in
* store session ID
* remember theme choice

---

# 4. What is a session?

A session is temporary data stored on the server for a specific user.

Example session data:

```text
sessionId = ABCD1234XYZ
userId = 101
username = Arun
role = ADMIN
```

The browser does not directly store this server-side data. Instead, it usually stores only the session ID in a cookie like `JSESSIONID`.

When the browser sends the cookie back, the server looks up the corresponding session.

---

# 5. Cookie vs Session

| Feature   | Cookie                         | Session                                            |
| --------- | ------------------------------ | -------------------------------------------------- |
| Stored in | Browser                        | Server                                             |
| Size      | Small                          | Can be larger                                      |
| Security  | Less secure for sensitive data | More secure than cookies                           |
| Lifetime  | Can be long or short           | Usually expires after inactivity or server timeout |
| Used for  | Preferences, identifiers       | Login state, temporary user data                   |

Important rule:

Do not store passwords, OTPs, or sensitive personal data in cookies.

---

# 6. Session handling in Spring Boot

Spring Boot supports session handling through the servlet API and Spring MVC.

You can use:

* `HttpSession`
* `HttpServletRequest`
* `HttpServletResponse`
* cookies directly

For beginners, the easiest way is to start with `HttpSession`.

---

# 7. Basic Spring Boot project setup

## Dependencies

Use Spring Web.

If you want to build a login example, you can also add:

* Spring Boot DevTools
* Lombok
* Spring Security later, if needed

## Maven dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

---

# 8. Understanding `HttpSession`

`HttpSession` is a server-side object provided by Java/Spring to store data for a user session.

Common operations:

* `setAttribute(String name, Object value)`
* `getAttribute(String name)`
* `removeAttribute(String name)`
* `invalidate()`
* `getId()`

---

# 9. Simple session example

Let us create a beginner-friendly example.

## Controller

```java
package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/session")
public class SessionController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, HttpSession session) {
        session.setAttribute("username", username);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("sessionId", session.getId());
        response.put("username", username);
        return response;
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Object username = session.getAttribute("username");

        if (username == null) {
            response.put("message", "No active session. Please login.");
        } else {
            response.put("message", "Session found");
            response.put("username", username);
            response.put("sessionId", session.getId());
        }

        return response;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return response;
    }
}
```

---

# 10. How this works

## `/session/login`

When you call:

```http
POST /session/login?username=Arun
```

Spring creates or uses an existing session.

Then:

```java
session.setAttribute("username", username);
```

stores `"Arun"` in the session.

The response also returns the session ID.

## `/session/profile`

When you call this later, the browser sends the session cookie automatically.

Spring finds the session and reads:

```java
session.getAttribute("username");
```

If the value exists, the server knows the user is logged in.

## `/session/logout`

This clears the session completely:

```java
session.invalidate();
```

After this, the session is no longer valid.

---

# 11. How the browser and server communicate

This is the most important concept.

## First request: login

1. Browser sends login request
2. Server creates session
3. Server sends back `Set-Cookie: JSESSIONID=...`
4. Browser stores cookie

## Next request: profile

1. Browser automatically sends cookie:

   ```text
   Cookie: JSESSIONID=...
   ```
2. Server reads the cookie
3. Server finds the session
4. Server restores the stored data

This is why the user stays “logged in”.

---

# 12. Viewing cookies in the browser

When you inspect browser developer tools:

* Chrome DevTools → Application → Cookies
* You can see cookie name, value, expiry, path, domain

For session cookies, the cookie may disappear when the browser closes, depending on configuration.

---

# 13. Creating and reading cookies manually in Spring Boot

You can also create your own cookies.

## Example: set a cookie

```java
package com.example.demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cookie")
public class CookieController {

    @GetMapping("/set")
    public String setCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("userName", "Arun");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1 hour

        response.addCookie(cookie);
        return "Cookie created successfully";
    }
}
```

---

## Example: read cookies

```java
package com.example.demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cookie")
public class CookieReadController {

    @GetMapping("/read")
    public String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return "No cookies found";
        }

        for (Cookie cookie : cookies) {
            if ("userName".equals(cookie.getName())) {
                return "Found cookie value: " + cookie.getValue();
            }
        }

        return "userName cookie not found";
    }
}
```

---

# 14. Common cookie attributes

## 1. `HttpOnly`

If set to `true`, JavaScript in the browser cannot access the cookie.

This helps protect against XSS attacks.

```java
cookie.setHttpOnly(true);
```

## 2. `Secure`

Cookie is sent only over HTTPS.

Spring’s classic `Cookie` class does not directly expose a simple setter in all cases, but this is usually configured via response headers or framework settings.

## 3. `Path`

Defines where the cookie is sent.

```java
cookie.setPath("/");
```

If you set path to `/app`, the cookie is sent only for URLs under `/app`.

## 4. `Max-Age`

How long the cookie lives in seconds.

```java
cookie.setMaxAge(3600);
```

* `-1` means session cookie
* `0` deletes the cookie
* positive value means persistent cookie

## 5. `Domain`

Defines which domain can access the cookie.

---

# 15. Session timeout

Sessions do not live forever. They expire after inactivity.

You can configure timeout in Spring Boot.

## `application.properties`

```properties
server.servlet.session.timeout=30m
```

This means the session expires after 30 minutes of inactivity.

You can also use:

```properties
server.servlet.session.timeout=10m
```

or

```properties
server.servlet.session.timeout=1h
```

---

# 16. Session timeout behavior

If a user logs in and then stays inactive for 30 minutes, the server may remove the session.

Then the next request will behave like a fresh request.

This is important for security and memory usage.

---

# 17. Get session only if it already exists

By default, `request.getSession()` creates a session if none exists.

Sometimes you do not want that.

Use:

```java
request.getSession(false);
```

This returns:

* existing session if present
* `null` if no session exists

## Example

```java
@GetMapping("/check")
public String checkSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);

    if (session == null) {
        return "No session exists";
    }

    return "Session exists with ID: " + session.getId();
}
```

---

# 18. Useful session methods

## Set data

```java
session.setAttribute("userId", 1001);
```

## Read data

```java
Object userId = session.getAttribute("userId");
```

## Remove one attribute

```java
session.removeAttribute("userId");
```

## Destroy entire session

```java
session.invalidate();
```

## Get session ID

```java
String id = session.getId();
```

---

# 19. Example: shopping cart using session

A shopping cart is a classic session use case.

## Controller

```java
package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @PostMapping("/add")
    public List<String> addItem(@RequestParam String item, HttpSession session) {
        List<String> cart = (List<String>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        cart.add(item);
        session.setAttribute("cart", cart);

        return cart;
    }

    @GetMapping("/view")
    public Object viewCart(HttpSession session) {
        List<String> cart = (List<String>) session.getAttribute("cart");

        if (cart == null) {
            return "Cart is empty";
        }

        return cart;
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute("cart");
        return "Cart cleared";
    }
}
```

---

# 20. What happens here

* `add` stores a list in session
* `view` fetches the list
* `clear` removes it

This makes the cart survive multiple requests without storing it in a database yet.

---

# 21. Cookies for remembering preferences

Cookies are better than sessions for small client-side preferences.

Example:

* theme = dark
* language = en
* fontSize = medium

These are not sensitive, so cookies are fine.

## Example

```java
@GetMapping("/theme")
public String setTheme(HttpServletResponse response) {
    Cookie cookie = new Cookie("theme", "dark");
    cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
    cookie.setPath("/");
    response.addCookie(cookie);
    return "Theme cookie set";
}
```

---

# 22. Session vs token-based authentication

Many modern apps use tokens like JWT instead of server sessions, especially for APIs.

But sessions are still very important for:

* traditional web apps
* Spring MVC applications
* server-rendered applications
* beginner understanding of state management

Sessions are also simpler to understand before moving to JWT.

---

# 23. Secure session handling tips

## 1. Never store sensitive secrets in cookies

Avoid storing:

* passwords
* OTPs
* bank details
* tokens in plain text

## 2. Use HttpOnly cookies

This blocks JavaScript access.

## 3. Use HTTPS in production

This protects cookies during transmission.

## 4. Invalidate session on logout

Always call:

```java
session.invalidate();
```

## 5. Keep timeout reasonable

Do not keep sessions alive forever.

---

# 24. Common mistakes beginners make

## Mistake 1: Thinking cookies and sessions are the same

They are not.

* cookie = browser-side
* session = server-side

## Mistake 2: Storing large data in cookies

Cookies should be small.

## Mistake 3: Forgetting to invalidate session on logout

This can create security issues.

## Mistake 4: Using `getSession()` when you only want to check existence

This may create a new session accidentally.

## Mistake 5: Storing everything in session forever

Sessions are temporary, not a database.

---

# 25. Using Spring Session

For simple apps, `HttpSession` is enough.

For distributed systems or multiple servers, you may need **Spring Session**.

Spring Session allows session data to be stored in:

* Redis
* JDBC database
* Hazelcast
* other external stores

This helps when:

* you have multiple application servers
* you want session sharing across instances
* you need session persistence after app restart

For beginners, learn `HttpSession` first, then move to Spring Session.

---

# 26. Mini project example: login flow

Here is a simple flow you can implement.

## Step 1: login

```java
@PostMapping("/login")
public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
    if ("admin".equals(username) && "1234".equals(password)) {
        session.setAttribute("loggedInUser", username);
        return "Login success";
    }
    return "Invalid credentials";
}
```

## Step 2: access protected page

```java
@GetMapping("/dashboard")
public String dashboard(HttpSession session) {
    String user = (String) session.getAttribute("loggedInUser");

    if (user == null) {
        return "Please login first";
    }

    return "Welcome to dashboard, " + user;
}
```

## Step 3: logout

```java
@PostMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate();
    return "Logged out";
}
```

---

# 27. Testing with Postman

You can test session behavior using Postman.

## Example requests

### Login

`POST /session/login?username=Arun`

Postman stores cookies automatically if cookie handling is enabled.

### Profile

`GET /session/profile`

If the cookie is preserved, you should get session data.

### Logout

`POST /session/logout`

After logout, profile should no longer work.

---

# 28. How to check session cookie in response

When you call a session endpoint for the first time, the response headers may include:

```text
Set-Cookie: JSESSIONID=ABC123...; Path=/; HttpOnly
```

That means the server told the browser to store the session ID.

---

# 29. Best practice for beginners

When learning, remember this mental model:

* **Session** = memory on server
* **Cookie** = note in browser that points to that memory

That is the easiest way to understand it.

---

# 30. Summary

## Cookies

* stored in browser
* sent with every request
* used for small client-side data
* often used to store session ID

## Sessions

* stored on server
* used to remember user state
* ideal for login, cart, temporary data

## In Spring Boot

* use `HttpSession` for server-side session handling
* use `Cookie` and `HttpServletResponse` for manual cookie handling
* use `session.invalidate()` to log out
* configure timeout using `server.servlet.session.timeout`

---
