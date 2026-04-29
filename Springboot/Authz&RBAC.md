# Spring Security + JWT + RBAC in Spring Boot 

---

## 🧠 Core Concepts First

| Term | What it means |
|------|--------------|
| **Authentication** | *Who are you?* (login with username/password) |
| **Authorization** | *What can you do?* (check your role/permissions) |
| **JWT** | A signed token the server gives you after login — you send it with every request |
| **RBAC** | Different users have different roles (e.g., `ADMIN`, `USER`) with different access |

---

## 📦 Step 1 — Project Setup

Go to [start.spring.io](https://start.spring.io) and add these dependencies:

- `Spring Web`
- `Spring Security`
- `Spring Data JPA`
- `H2 Database` (for simplicity; swap with MySQL/Postgres later)
- `Lombok`

Then manually add the **JWT library** to your `pom.xml`:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## 🗂️ Step 2 — Project Structure

```
src/main/java/com/example/demo/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── DemoController.java
├── entity/
│   └── User.java
├── repository/
│   └── UserRepository.java
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
├── dto/
│   ├── LoginRequest.java
│   └── AuthResponse.java
└── DemoApplication.java
```

---

## 👤 Step 3 — User Entity

```java
// entity/User.java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role; // e.g., "ROLE_ADMIN" or "ROLE_USER"
}
```

> ⚠️ Spring Security expects roles to be prefixed with `ROLE_`. So store them as `ROLE_ADMIN`, `ROLE_USER`, etc.

---

## 🗄️ Step 4 — Repository

```java
// repository/UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

---

## 🔑 Step 5 — JWT Utility Class

This class handles **creating**, **parsing**, and **validating** JWTs.

```java
// security/JwtUtil.java
@Component
public class JwtUtil {

    // Secret key — in production, store this in application.properties or env vars!
    private final String SECRET_KEY = "mySecretKey12345mySecretKey12345mySecretKey12345"; // must be 256+ bits

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // 1. Generate a JWT token for a user
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)                            // who the token belongs to
                .claim("role", role)                             // add role as a custom claim
                .setIssuedAt(new Date())                         // when token was created
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // expires in 1 hour
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Extract username from token
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 3. Extract role from token
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 4. Validate token — check signature + expiry
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // --- Helpers ---
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
}
```

---

## 👥 Step 6 — UserDetailsService Implementation

Spring Security needs a `UserDetailsService` to load user info from your database during authentication.

```java
// security/UserDetailsServiceImpl.java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole()) // e.g., "ROLE_ADMIN"
                .build();
    }
}
```

---

## 🛡️ Step 7 — JWT Auth Filter

This filter intercepts **every HTTP request**, checks for a JWT in the `Authorization` header, validates it, and sets the security context.

```java
// security/JwtAuthFilter.java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get the Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. Check if it starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // pass along — no token found
            return;
        }

        // 3. Extract the token (remove "Bearer " prefix)
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        // 4. If username found and not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Validate token
            if (jwtUtil.isTokenValid(token, userDetails)) {

                // 6. Create auth object and set it in the security context
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // roles go here
                    );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response); // continue the filter chain
    }
}
```

> 💡 **Key idea:** Once `SecurityContextHolder` has the authentication object, Spring Security knows *who* the user is and *what roles* they have — for this entire request.

---

## ⚙️ Step 8 — Security Configuration

This is the heart of Spring Security setup.

```java
// config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize on methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // disable CSRF for stateless REST APIs

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // login/register is public
                .anyRequest().authenticated()                // everything else needs auth
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no sessions — JWT only
            )

            // Add our JWT filter BEFORE the standard username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // always hash passwords!
    }
}
```

---

## 📬 Step 9 — DTOs (Data Transfer Objects)

```java
// dto/LoginRequest.java
@Data
public class LoginRequest {
    private String username;
    private String password;
}

// dto/AuthResponse.java
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
```

---

## 🚪 Step 10 — Auth Controller (Login + Register)

```java
// controller/AuthController.java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // hash the password
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // This throws an exception if credentials are wrong
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // If we reach here, credentials are valid — generate token
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
```

---

## 🎯 Step 11 — Protected Endpoints with RBAC

Now let's create endpoints that only certain roles can access.

```java
// controller/DemoController.java
@RestController
@RequestMapping("/api")
public class DemoController {

    // Any authenticated user can access this
    @GetMapping("/hello")
    public String hello(Principal principal) {
        return "Hello, " + principal.getName() + "!";
    }

    // Only ADMIN role can access this
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnly() {
        return "Welcome, Admin! This is a restricted area.";
    }

    // Only USER role can access this
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userOnly() {
        return "Welcome, User!";
    }

    // Both ADMIN and USER can access this
    @GetMapping("/shared")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String shared() {
        return "Both roles can see this.";
    }
}
```

> `@PreAuthorize` works because we added `@EnableMethodSecurity` in `SecurityConfig`.

---

## 🧪 Step 12 — Seed Data for Testing

Add this to `DemoApplication.java` or a `CommandLineRunner` bean:

```java
@Bean
CommandLineRunner seedData(UserRepository repo, PasswordEncoder encoder) {
    return args -> {
        repo.save(new User(null, "admin", encoder.encode("admin123"), "ROLE_ADMIN"));
        repo.save(new User(null, "user",  encoder.encode("user123"),  "ROLE_USER"));
    };
}
```

---

## 🔁 How It All Flows Together

```
CLIENT                        SERVER
  |                              |
  |-- POST /api/auth/login ----> |
  |   { username, password }     |  1. AuthenticationManager checks credentials
  |                              |  2. JwtUtil generates token with role claim
  |<-- { token: "eyJ..." } ----- |
  |                              |
  |-- GET /api/admin ----------> |
  |   Header: Bearer eyJ...      |  3. JwtAuthFilter extracts & validates token
  |                              |  4. Sets Authentication in SecurityContext
  |                              |  5. @PreAuthorize("hasRole('ADMIN')") checks role
  |<-- 200 OK / 403 Forbidden -- |
```

---

## 🧪 Step 13 — Testing with cURL

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pass123","role":"ROLE_USER"}'
```

**Login (get token):**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# Response: { "token": "eyJhbGci..." }
```

**Access protected endpoint:**
```bash
curl http://localhost:8080/api/admin \
  -H "Authorization: Bearer eyJhbGci..."
```

---

## ⚡ Common Mistakes to Avoid

| Mistake | Fix |
|--------|-----|
| Storing plain-text passwords | Always use `BCryptPasswordEncoder` |
| Hardcoding secret key in code | Move to `application.properties` and use env vars in production |
| Forgetting `ROLE_` prefix | Store roles as `ROLE_ADMIN`, not just `ADMIN` |
| Not setting `STATELESS` session | Without it, Spring creates sessions, defeating JWT's purpose |
| Putting `@EnableMethodSecurity` in wrong class | It must be on a `@Configuration` class |

---

## 🔐 Production Checklist

- [ ] Move JWT secret to `application.properties` or an env variable
- [ ] Use HTTPS in production (never send tokens over HTTP)
- [ ] Set a reasonable token expiry (1 hour is common)
- [ ] Add a **refresh token** mechanism for long sessions
- [ ] Store secret as Base64-encoded 256-bit key
- [ ] Add proper exception handling (`@ControllerAdvice`)
- [ ] Implement token blacklisting/revocation if needed

---