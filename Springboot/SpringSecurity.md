# Spring Security 
## 🧠 What is Spring Security?

Spring Security is a **powerful, highly customizable authentication and access-control framework** for Java applications. It is the de-facto standard for securing Spring-based applications.

At its core, it does two things:

> **Authentication** — Verify *who* you are
> **Authorization** — Decide *what* you're allowed to do

---

## 🏗️ The Big Picture — Spring Security's Architecture

Before diving into parts, understand the big picture:

```
Incoming HTTP Request
        │
        ▼
┌───────────────────────────────┐
│      Filter Chain             │  ← Spring Security lives HERE
│  (a series of filters)        │
│                               │
│  Filter 1 → Filter 2 → ...   │
│  → UsernamePasswordAuthFilter │
│  → BasicAuthFilter            │
│  → ExceptionTranslationFilter │
│  → AuthorizationFilter        │
└───────────────┬───────────────┘
                │
                ▼
        Your Controller
```

Spring Security inserts itself as a **chain of filters** between the raw HTTP request and your application code. Every request passes through this chain. Each filter has a specific job.

---

## 🔗 1. The Filter Chain — The Backbone

A **Filter** in Java/Servlet terms is a component that intercepts HTTP requests before they reach a servlet (your controller). Spring Security registers a special filter called `DelegatingFilterProxy` with the servlet container.

```
Servlet Container (Tomcat)
    └── DelegatingFilterProxy          ← registered with Tomcat
            └── FilterChainProxy       ← Spring Security's main entry point
                    └── SecurityFilterChain
                            ├── DisableEncodeUrlFilter
                            ├── WebAsyncManagerIntegrationFilter
                            ├── SecurityContextHolderFilter
                            ├── HeaderWriterFilter
                            ├── CsrfFilter
                            ├── LogoutFilter
                            ├── UsernamePasswordAuthenticationFilter
                            ├── ExceptionTranslationFilter
                            └── AuthorizationFilter           ← last one
```

### Key filters to know:

| Filter | Job |
|--------|-----|
| `SecurityContextHolderFilter` | Loads/saves `SecurityContext` for each request |
| `UsernamePasswordAuthenticationFilter` | Handles form login (POST to `/login`) |
| `BearerTokenAuthenticationFilter` | Handles JWT / OAuth2 bearer tokens |
| `ExceptionTranslationFilter` | Catches auth exceptions, sends 401 / 403 |
| `AuthorizationFilter` | Final gate — checks if user has permission |

> In Spring Boot, the filter chain is **auto-configured** for you. You customize it via `SecurityFilterChain` bean in your `SecurityConfig`.

---

## 🔐 2. Authentication — The Process

Authentication answers: **"Who is this person?"**

Here's the full flow when a user logs in:

```
User sends credentials
        │
        ▼
AuthenticationFilter          (e.g., UsernamePasswordAuthenticationFilter)
        │  creates
        ▼
Authentication object          (e.g., UsernamePasswordAuthenticationToken)
  - principal: "john"          (who)
  - credentials: "password"    (proof)
  - authenticated: false       (not yet!)
        │  passes to
        ▼
AuthenticationManager          (the coordinator)
  └── ProviderManager          (default implementation)
          │  delegates to
          ▼
  AuthenticationProvider       (the actual verifier)
  └── DaoAuthenticationProvider (default — uses UserDetailsService)
          │  calls
          ▼
  UserDetailsService
  └── loadUserByUsername()     (YOU implement this)
          │  returns
          ▼
  UserDetails object
  (username, hashed password, roles)
          │
          ▼
  PasswordEncoder.matches()    (compares raw vs hashed password)
          │  if match
          ▼
  Fully authenticated token
  (authenticated: true, authorities populated)
          │
          ▼
  SecurityContextHolder        (stores it for the rest of the request)
```

### The key objects:

**`Authentication`** — represents a user's identity at any point:
```java
public interface Authentication {
    Object getPrincipal();       // the user (UserDetails)
    Object getCredentials();     // password (cleared after auth)
    Collection<GrantedAuthority> getAuthorities(); // roles
    boolean isAuthenticated();
}
```

**`AuthenticationManager`** — the coordinator. Takes an unverified `Authentication`, returns a verified one (or throws `AuthenticationException`):
```java
public interface AuthenticationManager {
    Authentication authenticate(Authentication authentication)
        throws AuthenticationException;
}
```

**`AuthenticationProvider`** — the actual verifier. One provider handles one type of auth (username+password, JWT, OAuth2, LDAP, etc.):
```java
public interface AuthenticationProvider {
    Authentication authenticate(Authentication authentication);
    boolean supports(Class<?> authentication); // "can I handle this type?"
}
```

**`UserDetailsService`** — YOUR bridge to the database:
```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

**`UserDetails`** — the user object Spring Security understands:
```java
public interface UserDetails {
    String getUsername();
    String getPassword();
    Collection<? extends GrantedAuthority> getAuthorities(); // roles
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
}
```

---

## 🗂️ 3. SecurityContext & SecurityContextHolder

Once authenticated, Spring needs to **remember who you are** for the rest of the request.

```
SecurityContextHolder
    └── SecurityContext
            └── Authentication   ← your authenticated user lives here
```

- **`SecurityContextHolder`** is a thread-local store. It holds one `SecurityContext` per thread.
- **`SecurityContext`** holds the current `Authentication` object.
- After the request ends, it is cleared automatically.

```java
// How to read the current user anywhere in your code:
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Collection<? extends GrantedAuthority> roles = auth.getAuthorities();
```

> In a stateless JWT setup, the `JwtAuthFilter` re-populates the `SecurityContext` on **every request** from the token, since there's no session.

---

## 🚦 4. Authorization — The Process

Authorization answers: **"Is this user allowed to do this?"**

This happens **after** authentication, at the `AuthorizationFilter` (the last filter in the chain).

Spring Security has two levels of authorization:

### Level 1 — URL-based (in SecurityConfig)
```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")   // URL rule
    .requestMatchers("/user/**").hasRole("USER")
    .requestMatchers("/public/**").permitAll()
    .anyRequest().authenticated()
);
```
These rules are evaluated by `AuthorizationFilter` on every request.

### Level 2 — Method-based (on your controllers)
```java
@PreAuthorize("hasRole('ADMIN')")     // checked BEFORE method runs
public String deleteUser(Long id) { ... }

@PostAuthorize("returnObject.owner == authentication.name") // checked AFTER
public Document getDocument(Long id) { ... }
```
These are evaluated by **AOP proxies** (Spring's aspect-oriented programming), not filters. This is why `@EnableMethodSecurity` must be present — it activates the AOP interceptors.

### The `GrantedAuthority` interface

Roles are represented as `GrantedAuthority` objects:
```java
public interface GrantedAuthority {
    String getAuthority(); // returns e.g., "ROLE_ADMIN"
}
```
The `SimpleGrantedAuthority` class is the standard implementation:
```java
new SimpleGrantedAuthority("ROLE_ADMIN")
```

---

## 🏠 5. How Spring Boot Auto-Configures All of This

When you add `spring-boot-starter-security` to your project, Spring Boot's **auto-configuration** kicks in (`SpringBootWebSecurityConfiguration`). Without any code from you, it:

1. Creates a default `SecurityFilterChain`
2. Protects **all URLs** — requires login for everything
3. Provides a generated password in the console on startup
4. Enables **HTTP Basic auth** and a default login form
5. Registers a default in-memory user (`user` / generated password)

The moment you define **your own `SecurityFilterChain` bean**, Spring Boot **backs off** and lets you take full control. This is the Spring Boot convention: *auto-configure if absent, back off if present.*

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // Your custom config — Spring Boot's default is now gone
    ...
    return http.build();
}
```

---

## 🔄 6. The Full Request Lifecycle (Putting It All Together)

```
① HTTP Request arrives at Tomcat

② DelegatingFilterProxy hands it to FilterChainProxy

③ FilterChainProxy walks through SecurityFilterChain filters:

    ┌─ SecurityContextHolderFilter
    │   └─ Loads SecurityContext (empty for stateless JWT)
    │
    ├─ YourJwtAuthFilter (custom)
    │   └─ Reads "Authorization: Bearer <token>"
    │   └─ Validates token
    │   └─ Calls UserDetailsService.loadUserByUsername()
    │   └─ Creates UsernamePasswordAuthenticationToken (with authorities)
    │   └─ Sets it in SecurityContextHolder ✅
    │
    ├─ ExceptionTranslationFilter
    │   └─ Watches for AuthenticationException → 401
    │   └─ Watches for AccessDeniedException  → 403
    │
    └─ AuthorizationFilter
        └─ Reads Authentication from SecurityContextHolder
        └─ Checks URL rules from authorizeHttpRequests()
        └─ GRANTS or DENIES access

④ Request reaches DispatcherServlet → Your Controller

⑤ If @PreAuthorize present — AOP proxy checks role BEFORE method runs

⑥ Response travels back through the filter chain in reverse

⑦ SecurityContextHolderFilter clears the SecurityContext
```

---

## 🔑 7. Password Encoding — Why It Matters

Spring Security enforces password hashing through `PasswordEncoder`:

```java
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);         // hash it
    boolean matches(CharSequence raw, String encoded); // compare
}
```

`BCryptPasswordEncoder` is the standard — it's slow by design (makes brute-force hard) and adds a random salt automatically:

```
raw:     "mypassword"
encoded: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

> **Never store plain-text passwords.** Spring Security will actually throw an error if your `UserDetails` returns a password that doesn't start with an encoding prefix like `{bcrypt}`.

---

## 🌐 8. Stateful vs Stateless Security

| | Stateful (Session) | Stateless (JWT) |
|---|---|---|
| **How it works** | Server creates a session, gives client a session ID cookie | Server gives client a signed JWT token |
| **Server stores** | Session data in memory/DB | Nothing — token is self-contained |
| **Client sends** | `Cookie: JSESSIONID=abc` | `Authorization: Bearer eyJ...` |
| **Scales?** | Harder (sticky sessions needed) | Easily (any server can verify the token) |
| **Logout** | Easy — delete session | Hard — token valid until expiry |
| **Spring config** | Default (`IF_REQUIRED`) | `STATELESS` |

For JWT you must explicitly tell Spring not to create sessions:
```java
.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

---

## 📦 9. Key Spring Security Classes — Cheat Sheet

```
Authentication Flow:
  AuthenticationFilter
    → AuthenticationManager (ProviderManager)
      → AuthenticationProvider (DaoAuthenticationProvider)
        → UserDetailsService (YOU implement)
          → UserDetails (returned from your DB)
        → PasswordEncoder (BCrypt comparison)
      → SecurityContextHolder (stores result)

Authorization Flow:
  AuthorizationFilter (URL rules)
    → SecurityExpressionHandler
      → hasRole(), hasAuthority(), permitAll(), etc.
  AOP Proxy (method rules — @PreAuthorize)
    → MethodSecurityInterceptor
```

---

## 💡 Mental Model to Remember

Think of Spring Security like a **nightclub**:

- The **Filter Chain** is the entrance path everyone must walk through
- The **AuthenticationFilter** is the bouncer checking your ID
- **UserDetailsService** is the VIP list the bouncer consults
- **PasswordEncoder** is the ID verification machine
- **SecurityContextHolder** is the wristband you get once you're inside
- **AuthorizationFilter** is the staff checking wristbands at each area (VIP room, bar, etc.)
- **`@PreAuthorize`** is a staff member at a specific door checking your wristband again just before you enter

---

## Summary — The 6 Pillars

| Pillar | Interface / Class | Your Job |
|--------|------------------|----------|
| Filter Chain | `SecurityFilterChain` | Configure via `HttpSecurity` |
| Authentication | `AuthenticationManager` | Spring provides; you configure providers |
| User Loading | `UserDetailsService` | **You implement** — load from DB |
| Password Check | `PasswordEncoder` | Declare a `BCryptPasswordEncoder` bean |
| Context Storage | `SecurityContextHolder` | Automatic; read from it anywhere |
| Authorization | `AuthorizationFilter` + AOP | Configure URL rules + `@PreAuthorize` |
