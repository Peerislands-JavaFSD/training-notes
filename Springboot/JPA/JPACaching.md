# Spring Boot Caching 

## 1. Introduction to Caching in Spring Boot

Caching is a performance optimization technique used to store frequently accessed data in memory so that repeated requests can be served faster without hitting the database every time.

In Spring Boot, caching is implemented using the **Spring Cache Abstraction**, which provides a consistent programming model independent of the underlying cache provider.

---

## 2. Advantages of Using Caching

* Reduces database load
* Improves API response time
* Enhances scalability
* Minimizes redundant data fetch operations

---

## 3. How Spring Boot Caching Works

Spring intercepts method calls annotated with caching annotations and performs the following:

1. Checks whether the requested data exists in the cache
2. If present, returns the cached value
3. If not present, executes the method and stores the result in the cache

---

## 4. Enabling Caching in Spring Boot

### 4.1 Dependency Configuration

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

Spring Boot uses an in-memory `ConcurrentHashMap` as the default cache implementation.

---

### 4.2 Enable Caching

```java
@SpringBootApplication
@EnableCaching
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 5. Sample Use Case – Product Management

### 5.1 Entity Class

```java
@Entity
public class Product {

    @Id
    private Long id;
    private String name;
    private Double price;

    // getters and setters
}
```

---

### 5.2 Repository Layer

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

---

## 6. Caching `getById` Method

### 6.1 Problem Statement

Each request to retrieve a product by ID triggers a database query, even when the data remains unchanged.

---

### 6.2 Solution Using `@Cacheable`

```java
@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}
```

---

### 6.3 Explanation

* `value = "products"` specifies the cache name
* `key = "#id"` ensures each product is cached separately
* On subsequent calls with the same ID, the method is not executed

---

## 7. Caching `getAll` Method

### 7.1 Use Case

Fetching all products can be expensive, especially when the dataset is large and frequently accessed.

---

### 7.2 Implementation

```java
@Cacheable(value = "productList")
public List<Product> getAllProducts() {
    return repository.findAll();
}
```

---

### 7.3 Important Note

Caching `getAll` requires strict cache eviction on any data modification to prevent stale data.

---

## 8. Cache Eviction on Data Modification

### 8.1 Evict Cache on Create Operation

```java
@CacheEvict(value = "productList", allEntries = true)
public Product saveProduct(Product product) {
    return repository.save(product);
}
```

---

### 8.2 Evict Cache on Update Operation Using `@Caching`

```java
@Caching(
    evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "productList", allEntries = true)
    }
)
public Product updateProduct(Product product) {
    return repository.save(product);
}
```

---

### 8.3 Evict Cache on Delete Operation

```java
@Caching(
    evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "productList", allEntries = true)
    }
)
public void deleteProduct(Long id) {
    repository.deleteById(id);
}
```

---

## 9. Explanation of `@Caching` Annotation

`@Caching` is a container annotation used when multiple caching operations need to be applied to a single method.

### Syntax

```java
@Caching(
    evict = {
        @CacheEvict(value = "products", allEntries = true)
    }
)
```

### Use Cases

* Evict multiple caches at once
* Combine `@Cacheable`, `@CachePut`, and `@CacheEvict`
* Maintain cache consistency across related APIs

---

## 10. Cache Annotations Overview

| Annotation       | Purpose                                 |
| ---------------- | --------------------------------------- |
| `@Cacheable`     | Fetch from cache or store method result |
| `@CacheEvict`    | Remove cache entries                    |
| `@CachePut`      | Update cache without skipping method    |
| `@Caching`       | Group multiple cache annotations        |
| `@EnableCaching` | Enable caching support                  |

---

## 11. Best Practices

* Apply caching only at the **service layer**
* Cache read-heavy and stable data
* Always evict caches on write operations
* Avoid caching paginated or search-based queries
* Use meaningful cache names

---

## 12. Limitations of Default Cache

The default in-memory cache:

* Is not distributed
* Does not survive application restart
* Is suitable only for development and small-scale applications

For production systems, external cache providers such as Redis or EhCache should be used.

---

## 13. Interview-Oriented Key Points

* Spring cache works via AOP proxies
* Cache annotations do not work on self-invocation
* `@Cacheable` skips method execution on cache hit
* `@CachePut` always executes the method
* Cache eviction is mandatory for consistency

---
