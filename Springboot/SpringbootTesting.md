### Unit Testing and Integration Testing in Spring Boot

Welcome to this detailed tutorial on unit testing and integration testing in Spring Boot! As of December 2025, Spring Boot 3.x (specifically 3.3.x and later) emphasizes fast, reliable testing through built-in support for JUnit 5, Mockito, and sliced testing annotations. Testing is crucial in microservices and enterprise apps to ensure code quality, catch regressions early, and facilitate CI/CD pipelines.

We'll cover:
- **Fundamentals**: Why test, unit vs. integration.
- **Setup**: Project configuration.
- **Unit Testing**: Isolated tests for components like services and controllers.
- **Integration Testing**: End-to-end tests involving multiple layers (e.g., DB, web).
- **Best Practices**: Tips for maintainable tests.
- **Examples**: A simple e-commerce app with User entity, UserService, and UserController.

This tutorial assumes basic Java/Spring knowledge. We'll use Maven for builds, but Gradle equivalents are similar.

#### 1. Why Testing in Spring Boot?
- **Unit Tests**: Focus on a single component (e.g., a service method) in isolation. Fast (~ms), no external dependencies. Use mocks for collaborators.
- **Integration Tests**: Verify interactions between components (e.g., controller → service → repo → DB). Slower but ensure real-world behavior.
- Benefits: 80%+ code coverage, TDD support, resilience in distributed systems.
- Spring Boot's `spring-boot-starter-test` includes JUnit 5, AssertJ, Mockito, and JSON libraries out-of-the-box.

#### 2. Project Setup
Create a Spring Boot project via [Spring Initializr](https://start.spring.io):
- **Dependencies**: Spring Web, Spring Data JPA, H2 Database (for in-memory testing), Spring Boot Starter Test.
- **Java**: 17+.
- **Maven `pom.xml` Snippet** (key parts):
  ```xml
  <dependencies>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-data-jpa</artifactId>
      </dependency>
      <dependency>
          <groupId>com.h2database</groupId>
          <artifactId>h2</artifactId>
          <scope>runtime</scope>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-test</artifactId>
          <scope>test</scope>
      </dependency>
  </dependencies>
  ```
- **Sample App Structure**:
    - `User.java` (Entity): `@Entity public class User { private Long id; private String name; /* getters/setters */ }`
    - `UserRepository.java`: `public interface UserRepository extends JpaRepository<User, Long> {}`
    - `UserService.java`: `@Service public class UserService { @Autowired private UserRepository repo; public User save(User user) { return repo.save(user); } public List<User> findAll() { return repo.findAll(); } }`
    - `UserController.java`: `@RestController @RequestMapping("/users") public class UserController { @Autowired private UserService service; @PostMapping public User create(@RequestBody User user) { return service.save(user); } @GetMapping public List<User> list() { return service.findAll(); } }`
    - `Application.java`: `@SpringBootApplication public class Application { public static void main(String[] args) { SpringApplication.run(Application.class, args); } }`

Run `mvn test` to verify setup.

#### 3. Unit Testing: Isolating Components
Unit tests mock dependencies to focus on logic. Use JUnit 5 (`@Test`, `@BeforeEach`) and Mockito (`@Mock`, `@InjectMocks`).

##### 3.1 Testing a Service (Business Logic)
Test `UserService` without hitting the DB—mock the repository.

- **Test Class**: `UserServiceTest.java`
  ```java
  import org.junit.jupiter.api.BeforeEach;
  import org.junit.jupiter.api.Test;
  import org.mockito.InjectMocks;
  import org.mockito.Mock;
  import org.mockito.MockitoAnnotations;
  import java.util.Arrays;
  import java.util.List;
  import static org.junit.jupiter.api.Assertions.assertEquals;
  import static org.mockito.ArgumentMatchers.any;
  import static org.mockito.Mockito.when;

  class UserServiceTest {

      @Mock
      private UserRepository userRepository;

      @InjectMocks
      private UserService userService;

      @BeforeEach
      void setUp() {
          MockitoAnnotations.openMocks(this);
      }

      @Test
      void saveUser_ShouldReturnSavedUser() {
          // Arrange: Prepare mock response
          User mockUser = new User();
          mockUser.setName("John Doe");
          when(userRepository.save(any(User.class))).thenReturn(mockUser);

          // Act: Call the method
          User savedUser = userService.save(mockUser);

          // Assert: Verify result and interactions
          assertEquals("John Doe", savedUser.getName());
          // Verify repository was called once
      }

      @Test
      void findAllUsers_ShouldReturnAllUsers() {
          // Arrange
          User user1 = new User(); user1.setName("Alice");
          User user2 = new User(); user2.setName("Bob");
          List<User> mockUsers = Arrays.asList(user1, user2);
          when(userRepository.findAll()).thenReturn(mockUsers);

          // Act
          List<User> users = userService.findAll();

          // Assert
          assertEquals(2, users.size());
          assertEquals("Alice", users.get(0).getName());
      }
  }
  ```
- **Explanation**:
    - `@Mock`: Creates a mock for `UserRepository`.
    - `@InjectMocks`: Injects the mock into `UserService`.
    - `when(...).thenReturn(...)`: Stubs mock behavior.
    - `any(User.class)`: Matches any `User` argument (flexible matcher).
    - Run: `mvn test`—executes in ~50ms, no DB needed.
    - **Why Unit?** Tests service logic (e.g., validation) without I/O.

##### 3.2 Testing a Controller (Web Layer)
Use `@WebMvcTest` for sliced testing—loads only MVC components (controllers, filters). Auto-configures `MockMvc` for HTTP simulation.

- **Test Class**: `UserControllerTest.java`
  ```java
  import org.junit.jupiter.api.Test;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
  import org.springframework.boot.test.mock.mockito.MockBean;
  import org.springframework.http.MediaType;
  import org.springframework.test.web.servlet.MockMvc;
  import static org.mockito.ArgumentMatchers.any;
  import static org.mockito.Mockito.when;
  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

  @WebMvcTest(UserController.class)
  class UserControllerTest {

      @Autowired
      private MockMvc mockMvc;  // Auto-configured for HTTP mocks

      @MockBean
      private UserService userService;  // Mock service in the slice

      @Test
      void createUser_ShouldReturnCreatedUser() throws Exception {
          // Arrange
          User user = new User();
          user.setName("Jane Doe");
          when(userService.save(any(User.class))).thenReturn(user);

          // Act & Assert
          mockMvc.perform(post("/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"Jane Doe\"}"))
                  .andExpect(status().isOk())
                  .andExpect(jsonPath("$.name").value("Jane Doe"));
      }

      @Test
      void listUsers_ShouldReturnUserList() throws Exception {
          // Arrange
          User user1 = new User(); user1.setName("Alice");
          when(userService.findAll()).thenReturn(Arrays.asList(user1));

          // Act & Assert
          mockMvc.perform(get("/users"))
                  .andExpect(status().isOk())
                  .andExpect(jsonPath("$[0].name").value("Alice"));
      }
  }
  ```
- **Explanation**:
    - `@WebMvcTest`: Loads only web layer; scans `@Controller` beans.
    - `@MockBean`: Overrides real `UserService` with a mock (in context).
    - `MockMvc.perform(...)`: Simulates HTTP requests.
    - `jsonPath`: AssertJ-like JSON assertions (via JsonPath).
    - **Advantages**: Fast (no server start), focuses on HTTP handling, status codes, JSON serialization.
    - **Edge Case**: Add `@AutoConfigureMockMvc(addFilters = false)` to exclude security filters if needed.

#### 4. Integration Testing: Verifying Interactions
Integration tests load parts or all of the context, including real DBs (e.g., H2 in-memory).

##### 4.1 Data Layer: Testing Repository
Use `@DataJpaTest`—sliced for JPA, auto-configures H2 and `TestEntityManager`.

- **Test Class**: `UserRepositoryTest.java`
  ```java
  import org.junit.jupiter.api.Test;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
  import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
  import static org.assertj.core.api.Assertions.assertThat;

  @DataJpaTest  // Auto: H2 DB, JPA repos, rollback transactions
  class UserRepositoryTest {

      @Autowired
      private TestEntityManager entityManager;  // For manual persist/flush

      @Autowired
      private UserRepository userRepository;

      @Test
      void saveUser_ShouldPersistAndReturnUser() {
          // Arrange & Act
          User user = new User();
          user.setName("Integrated User");
          User saved = userRepository.save(user);

          // Assert (with flush to trigger persistence)
          entityManager.flush();
          assertThat(saved.getId()).isGreaterThan(0L);
          assertThat(userRepository.findById(saved.getId())).isPresent();
      }
  }
  ```
- **Explanation**:
    - `@DataJpaTest`: Embedded H2 (`spring.datasource.url=jdbc:h2:mem:testdb`), rolls back txns automatically.
    - `TestEntityManager`: Persist entities and flush to DB.
    - **Customization**: `@AutoConfigureTestDatabase(replace = ANY)` for real DB in CI.
    - **Why Integration?** Tests real JPA queries, constraints.

##### 4.2 Full Application: End-to-End Testing
Use `@SpringBootTest` for complete context. Test with `TestRestTemplate` (real HTTP) or `MockMvc` (mocked).

- **Test Class**: `UserIntegrationTest.java`
  ```java
  import org.junit.jupiter.api.Test;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.context.SpringBootTest;
  import org.springframework.boot.test.web.client.TestRestTemplate;
  import org.springframework.boot.test.web.server.LocalServerPort;
  import org.springframework.http.HttpStatus;
  import org.springframework.http.ResponseEntity;
  import static org.assertj.core.api.Assertions.assertThat;

  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)  // Starts real server on random port
  class UserIntegrationTest {

      @LocalServerPort  // Injects the random port
      private int port;

      @Autowired
      private TestRestTemplate restTemplate;  // For real HTTP calls

      @Test
      void createAndListUsers_ShouldWorkEndToEnd() {
          // Arrange & Act: Create user
          User user = new User();
          user.setName("Integration Jane");
          ResponseEntity<User> createResponse = restTemplate.postForEntity(
                  "http://localhost:" + port + "/users", user, User.class);

          // Assert create
          assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
          User createdUser = createResponse.getBody();
          assertThat(createdUser.getName()).isEqualTo("Integration Jane");

          // Act: List users
          ResponseEntity<String> listResponse = restTemplate.getForEntity(
                  "http://localhost:" + port + "/users", String.class);

          // Assert list
          assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
          assertThat(listResponse.getBody()).contains("Integration Jane");
      }
  }
  ```
- **Explanation**:
    - `@SpringBootTest(webEnvironment = RANDOM_PORT)`: Full context + embedded Tomcat on random port.
    - `TestRestTemplate`: Real HTTP client (auto-configured).
    - Transactions: Rollback only in mocked env; use `@Transactional` for explicit control.
    - **Alternative (Faster)**: Add `@AutoConfigureMockMvc` and use `MockMvc` instead of real server.
    - **Pro Tip**: For reactive apps, use `@WebFluxTest` + `WebTestClient`.

#### 5. Best Practices and Advanced Tips
- **Layered Testing Pyramid**: 70% unit, 20% integration slices, 10% full E2E.
- **Mocking**: Prefer `@MockBean` in slices; verify interactions with `verify(mock, times(1))`.
- **Assertions**: Use AssertJ (`assertThat`) for fluent, readable checks.
- **Profiles**: `@ActiveProfiles("test")` for test-specific `application-test.properties` (e.g., H2 config).
- **Coverage**: Integrate JaCoCo (`mvn jacoco:report`)—aim for 80%+.
- **External Dependencies**: Use Testcontainers for Dockerized DBs (add `testImplementation 'org.testcontainers:junit-jupiter'`).
    - Example: `@Testcontainers @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");`
- **Security**: Test with `@WithMockUser` in `@WebMvcTest`.
- **Performance**: Slices run 10x faster than full `@SpringBootTest`.
- **Common Pitfalls**: Avoid `@SpringBootApplication` scans in tests; use `@DirtiesContext` for stateful tests.
- **Tools**: IDEs like IntelliJ auto-generate tests; GitHub Actions for CI.

| Test Type | Annotation | Speed | Scope | Example Use |
|-----------|------------|-------|-------|-------------|
| Unit (Service) | Plain JUnit + Mockito | Very Fast | Single Class | Logic validation |
| Unit (Controller) | `@WebMvcTest` | Fast | Web Layer | HTTP endpoints |
| Integration (Repo) | `@DataJpaTest` | Medium | Data Layer | JPA queries |
| Full Integration | `@SpringBootTest` | Slow | Entire App | E2E flows |

#### 6. Running and Debugging
- Command: `mvn clean test` (or `mvn test -Dtest=UserServiceTest` for specific).
- Debug: Set breakpoints; use `@TestMethodOrder` for execution order.
- Coverage Report: `mvn jacoco:report` → open `target/site/jacoco/index.html`.
