# Spring Boot Microservices

---

# CHAPTER 1 — What is a Microservice?

Before understanding microservices, you need to understand what came before them.

## The Monolithic Architecture

A monolith is a single, unified application where every feature — user management, products, orders, payments, notifications — lives inside **one deployable unit**.

```
╔══════════════════════════════════════════════════════════╗
║              MONOLITHIC APPLICATION (.jar/.war)          ║
║                                                          ║
║   ┌────────────┐  ┌────────────┐  ┌────────────┐        ║
║   │   Users    │  │  Products  │  │   Orders   │        ║
║   │   Module   │  │   Module   │  │   Module   │        ║
║   └────────────┘  └────────────┘  └────────────┘        ║
║   ┌────────────┐  ┌────────────┐  ┌────────────┐        ║
║   │  Payments  │  │   Email    │  │  Reports   │        ║
║   │   Module   │  │   Module   │  │   Module   │        ║
║   └────────────┘  └────────────┘  └────────────┘        ║
║                                                          ║
║              ONE shared Database                         ║
║         ┌─────────────────────────┐                      ║
║         │         MySQL           │                      ║
║         └─────────────────────────┘                      ║
╚══════════════════════════════════════════════════════════╝
```

## The Microservices Architecture

Microservices breaks that monolith into **small, independent services**, each responsible for exactly one business capability.

```
                      ┌─────────────────┐
                      │   API Gateway   │
                      └────────┬────────┘
                               │
        ┌──────────┬───────────┼───────────┬──────────┐
        ▼          ▼           ▼           ▼          ▼
  ┌──────────┐ ┌─────────┐ ┌────────┐ ┌────────┐ ┌────────┐
  │  User    │ │Product  │ │ Order  │ │Payment │ │ Email  │
  │ Service  │ │Service  │ │Service │ │Service │ │Service │
  └────┬─────┘ └────┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
       │             │          │           │           │
  ┌────┴──┐    ┌─────┴─┐  ┌────┴──┐  ┌────┴──┐  ┌────┴──┐
  │MySQL  │    │MongoDB│  │MySQL  │  │Postgre│  │Redis  │
  └───────┘    └───────┘  └───────┘  └───────┘  └───────┘

  Each service has its OWN database — no sharing!
```

> Each service is a **separate Spring Boot application**, deployed independently, communicating over HTTP or messaging queues.

---

# CHAPTER 2 — Monolith vs Microservices

## Side-by-Side Comparison

```
┌─────────────────────────────────────────────────────────────────┐
│                    MONOLITH                                      │
│                                                                  │
│  Deploy:   One giant JAR file                                    │
│  Scale:    Must scale the whole app even if only orders are busy │
│  Failure:  Payment bug = entire app goes down                    │
│  Team:     All developers work on the same codebase             │
│  Tech:     One language, one framework for everything           │
│  DB:       One shared database for all features                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   MICROSERVICES                                  │
│                                                                  │
│  Deploy:   Each service deploys independently                    │
│  Scale:    Scale only the service that needs it                  │
│  Failure:  Payment service down ≠ Product service affected      │
│  Team:     Each team owns their own service                      │
│  Tech:     Each service can use different language/framework    │
│  DB:       Each service has its own private database             │
└─────────────────────────────────────────────────────────────────┘
```

## Scaling Difference — Visual

```
MONOLITH SCALING (Wasteful)
═══════════════════════════
During a sale, Orders module is busy.
But you must scale the ENTIRE app:

┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
│ Users+Products+   │  │ Users+Products+   │  │ Users+Products+   │
│ Orders+Payments   │  │ Orders+Payments   │  │ Orders+Payments   │
│ +Email+Reports    │  │ +Email+Reports    │  │ +Email+Reports    │
│   (3 copies)      │  │                   │  │                   │
└───────────────────┘  └───────────────────┘  └───────────────────┘
   ↑ you only needed Orders to scale, but scaled everything ↑


MICROSERVICES SCALING (Efficient)
══════════════════════════════════
Scale only what you need:

┌──────────┐  ┌──────────┐  ┌──────────┐  ← Orders scaled x3
│  Orders  │  │  Orders  │  │  Orders  │
└──────────┘  └──────────┘  └──────────┘
┌──────────┐                              ← Others untouched
│  Users   │
└──────────┘
┌──────────┐
│ Products │
└──────────┘
```

## When to Use What

```
Use MONOLITH when:                  Use MICROSERVICES when:
──────────────────                  ───────────────────────
• Small team (1-5 devs)             • Large teams, multiple squads
• Early-stage startup               • Product is mature & growing
• Simple domain                     • High scalability needs
• Fast prototype needed             • Different parts need different tech
• Low traffic expected              • Independent deployment needed
```

---

# CHAPTER 3 — API Gateway

## The Problem

Without a gateway, clients must know the address of **every single service**. This creates chaos:

```
WITHOUT API GATEWAY
═══════════════════

Mobile App must know:
  Product Service  → http://10.0.1.5:8081
  Order Service    → http://10.0.1.6:8082
  Payment Service  → http://10.0.1.7:8083
  User Service     → http://10.0.1.8:8084

Problems:
  ✗ Security risk — internal IPs exposed
  ✗ CORS handling needed in every service
  ✗ Auth logic repeated in every service
  ✗ If a service moves, update every client
  ✗ No single place to add rate limiting
```

## The Solution — API Gateway

```
WITH API GATEWAY
════════════════

Mobile App only knows ONE address:
  Everything → http://my-api.com  (Gateway)

                    ┌───────────────────────────────┐
                    │          API GATEWAY           │
                    │                               │
  Client ─────────▶ │  ┌─────────────────────────┐  │
                    │  │    Route Table           │  │
                    │  │  /products/** → :8081   │  │
                    │  │  /orders/**  → :8082   │  │
                    │  │  /payments/**→ :8083   │  │
                    │  │  /users/**   → :8084   │  │
                    │  └─────────────────────────┘  │
                    │                               │
                    │  ┌──────┐ ┌──────┐ ┌──────┐  │
                    │  │ Auth │ │ CORS │ │Rate  │  │
                    │  │      │ │      │ │Limit │  │
                    │  └──────┘ └──────┘ └──────┘  │
                    └───────────────────────────────┘
                               │
              ┌────────────────┼──────────────┐
              ▼                ▼              ▼
        Product-Service  Order-Service  Payment-Service
```

## What API Gateway Does

```
┌─────────────────────────────────────────────────┐
│              API GATEWAY RESPONSIBILITIES        │
│                                                  │
│  1. ROUTING          → Directs requests to the  │
│                         right service           │
│                                                  │
│  2. AUTHENTICATION   → Validates JWT tokens     │
│                         before forwarding       │
│                                                  │
│  3. RATE LIMITING    → Blocks users sending     │
│                         too many requests       │
│                                                  │
│  4. LOAD BALANCING   → Distributes requests     │
│                         across instances        │
│                                                  │
│  5. SSL TERMINATION  → Handles HTTPS in one     │
│                         place                   │
│                                                  │
│  6. REQUEST LOGGING  → Logs all traffic in      │
│                         one place               │
└─────────────────────────────────────────────────┘
```

## In Spring Boot

Spring provides **Spring Cloud Gateway** (two flavors):

```
spring-cloud-starter-gateway              → Reactive / WebFlux (older)
spring-cloud-starter-gateway-server-webmvc → MVC based (Spring Cloud 2025.x)
```

Routes are defined in properties — the gateway reads them and forwards matching requests automatically.

---

# CHAPTER 4 — Service Discovery (Eureka)

## The Problem — Services Don't Know Each Other

In a microservices world, services need to talk to each other. But:

- Services can run on **dynamic IPs** (especially in cloud/Docker)
- You can have **multiple instances** of the same service
- Instances can **start and stop** at any time

Hardcoding addresses breaks immediately in this environment.

## The Phone Book Analogy

```
Real World:
  You don't memorize everyone's phone number.
  You look up their name in the phone book.

Microservices:
  Services don't hardcode each other's addresses.
  They look up the service name in the Service Registry (Eureka).
```

## How Eureka Works — Full Lifecycle

```
PHASE 1: REGISTRATION (on startup)
════════════════════════════════════

  product-service starts on :8081
       │
       ▼
  Registers itself with Eureka:
  "Hi, I'm 'product-service' at host:8081, I'm healthy"
       │
       ▼
  ┌────────────────────────────┐
  │       EUREKA SERVER        │
  │                            │
  │  Registry:                 │
  │  ┌──────────────────────┐  │
  │  │ product-service:8081 │  │
  │  │ order-service:8082   │  │
  │  │ api-gateway:8000     │  │
  │  └──────────────────────┘  │
  └────────────────────────────┘


PHASE 2: HEARTBEAT (every 30 seconds)
══════════════════════════════════════

  product-service ──── ping ────▶ Eureka  ✅ alive
  product-service ──── ping ────▶ Eureka  ✅ alive
  product-service ──── ping ────▶ Eureka  ✅ alive
  product-service  💥 CRASH 💥
                                  Eureka waits 90s...
                                  No heartbeat received
                                  Removes product-service ❌


PHASE 3: DISCOVERY (when service needed)
══════════════════════════════════════════

  order-service needs to call product-service
       │
       ▼  "Where is product-service?"
  ┌────────────────────────────┐
  │       EUREKA SERVER        │
  │  "It's at localhost:8081"  │
  └────────────────────────────┘
       │
       ▼
  order-service calls http://localhost:8081/products
```

## Self-Preservation Mode

Eureka has a safety feature. If too many services go down at once, it suspects a **network partition** (not actual failures) and stops removing services from the registry. This prevents a mass deregistration that could cascade into failure.

```
Normal:    1-2 services miss heartbeat → remove them
Abnormal:  50% of services miss heartbeat → network problem?
           Eureka activates Self-Preservation Mode
           Keeps all entries intact until network recovers
```

---

# CHAPTER 5 — Load Balancer

## What is Load Balancing?

When you run **multiple instances** of a service, the load balancer decides which instance handles each incoming request.

```
WITHOUT Load Balancer:
═══════════════════════
All 1000 requests/sec hit Instance 1
Instance 1 dies under pressure 💥

WITH Load Balancer:
════════════════════
1000 requests/sec distributed evenly:

        ┌──────────────────┐
        │   Load Balancer  │
        └────────┬─────────┘
                 │
     ┌───────────┼───────────┐
     ▼           ▼           ▼
 Instance 1   Instance 2  Instance 3
 ~333 req/s   ~333 req/s  ~333 req/s
 ✅ Healthy   ✅ Healthy  ✅ Healthy
```

## Types of Load Balancing

```
SERVER-SIDE LOAD BALANCING
══════════════════════════
  Client ──▶ Load Balancer Server (Nginx/AWS ALB)
                    │
             picks an instance
                    │
                    ▼
             Service Instance

  A dedicated server sits between client and services.
  Client doesn't know about instances at all.


CLIENT-SIDE LOAD BALANCING  ← Spring Cloud uses this
══════════════════════════════
  Client has a list of all instances (from Eureka).
  Client itself picks which one to call.

  Order-Service (client)
       │
       ├── knows Instance 1 → :8081
       ├── knows Instance 2 → :8082
       └── knows Instance 3 → :8083
       │
       └── picks :8081 this time (Round Robin)
           picks :8082 next time
           picks :8083 next time
           picks :8081 again...
```

## Load Balancing Algorithms

```
┌──────────────────────────────────────────────────────────┐
│                 LOAD BALANCING STRATEGIES                 │
│                                                           │
│  ROUND ROBIN (Spring Cloud default)                       │
│  Request 1 → Instance A                                   │
│  Request 2 → Instance B                                   │
│  Request 3 → Instance C                                   │
│  Request 4 → Instance A  (cycles back)                    │
│                                                           │
│  WEIGHTED ROUND ROBIN                                     │
│  Instance A (powerful) gets 60% of requests               │
│  Instance B (weaker)   gets 40% of requests               │
│                                                           │
│  LEAST CONNECTIONS                                         │
│  Always routes to the instance with fewest active requests│
│                                                           │
│  RANDOM                                                   │
│  Picks a random instance each time                        │
└──────────────────────────────────────────────────────────┘
```

In Spring Cloud, `@LoadBalanced` on `WebClient` or `RestTemplate` + `spring-cloud-starter-loadbalancer` enables **client-side Round Robin** load balancing automatically via Eureka.

---

# CHAPTER 6 — Inter-Service Communication

## How Services Talk to Each Other

Microservices are separate processes — they communicate over the network. There are two styles:

```
┌─────────────────────────────────────────────────────────┐
│              SYNCHRONOUS COMMUNICATION                   │
│                                                          │
│  Caller WAITS for the response before continuing.        │
│                                                          │
│  Order-Service ──── HTTP GET /products/1 ────▶ Product  │
│  Order-Service ◀─── { id:1, price: 999 } ──── Service  │
│      (waits here until response arrives)                 │
│                                                          │
│  Tools: RestTemplate, WebClient, OpenFeign               │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│              ASYNCHRONOUS COMMUNICATION                  │
│                                                          │
│  Caller sends a message and MOVES ON immediately.        │
│  The other service processes it when it's ready.         │
│                                                          │
│  Order-Service ── "Order Placed" event ──▶ Message Queue │
│  Order-Service continues doing other work...             │
│                                           │              │
│                              Email-Service reads queue   │
│                              and sends confirmation      │
│                                                          │
│  Tools: RabbitMQ, Apache Kafka                           │
└─────────────────────────────────────────────────────────┘
```

## RestTemplate

The **oldest** way to make HTTP calls between services. Simple but verbose.

```
What it does:
  Wraps Java's HTTP client with Spring convenience methods.
  You manually build the URL and parse the response.

Order-Service uses RestTemplate:

  restTemplate.getForObject(
      "http://product-service/products/" + id,
      Product.class
  );

Drawbacks:
  ✗ Boilerplate code for every call
  ✗ URL strings are error-prone
  ✗ No built-in load balancing (need @LoadBalanced annotation)
  ✗ Being phased out in newer Spring versions
```

## WebClient

The **modern** replacement for RestTemplate. Supports both blocking and non-blocking calls.

```
What it does:
  Reactive HTTP client. More powerful and flexible than RestTemplate.
  Can be used in blocking mode too.

Order-Service uses WebClient:

  webClientBuilder.build()
      .get()
      .uri("http://product-service/products/" + id)
      .retrieve()
      .bodyToMono(Product.class)
      .block();  ← .block() makes it synchronous

Advantages over RestTemplate:
  ✓ Supports async/reactive programming
  ✓ Better error handling
  ✓ Cleaner fluent API
  ✓ Spring's recommended choice
```

## OpenFeign — The Cleanest Way

OpenFeign lets you write inter-service calls as **simple Java interfaces**. No URL building, no response parsing — it looks just like calling a local method.

```
WHAT YOU WRITE (just an interface):
════════════════════════════════════

  @FeignClient(name = "product-service")
  public interface ProductClient {

      @GetMapping("/products/{id}")
      Product getProductById(@PathVariable Long id);

      @GetMapping("/products")
      List<Product> getAllProducts();
  }

WHAT YOU USE (inject and call like any bean):
══════════════════════════════════════════════

  @Service
  public class OrderService {

      @Autowired
      private ProductClient productClient;  // inject the interface

      public void placeOrder(Long productId) {
          Product product = productClient.getProductById(productId);
          // ↑ looks like a local method call
          // ↑ actually making HTTP call to product-service
          // ↑ Eureka resolves the address
          // ↑ Load balancer picks the instance
      }
  }

COMPARISON:
════════════
  RestTemplate:  50 lines of boilerplate
  WebClient:     20 lines of fluent code
  OpenFeign:     3 lines — just call the interface method ✅
```

---

# CHAPTER 7 — Circuit Breaker

## The Cascading Failure Problem

In a chain of microservices, one slow or failing service can bring down the entire chain:

```
CASCADING FAILURE
══════════════════

User makes a request to API Gateway

Gateway → Order-Service → Product-Service → (CRASHED 💥)

Product-Service is down.
Order-Service waits... and waits... times out after 30 seconds.
During those 30 seconds, thousands of requests pile up in Order-Service.
Order-Service runs out of threads. Order-Service crashes. 💥

Now Gateway can't reach Order-Service either. Gateway crashes. 💥

One service failure brought down the entire system.
```

## The Circuit Breaker Pattern — Electrical Analogy

Just like an electrical circuit breaker that **trips** to protect your home when there's a power surge, a software circuit breaker **opens** to stop calls to a failing service.

```
CIRCUIT BREAKER STATES
═══════════════════════

  ┌─────────────┐
  │   CLOSED    │  ← Normal operation. Requests flow through.
  │  (working)  │    Failures are counted.
  └──────┬──────┘
         │  Failure rate exceeds threshold (e.g., 50% failures)
         ▼
  ┌─────────────┐
  │    OPEN     │  ← Circuit is broken. All requests IMMEDIATELY
  │  (failing)  │    return an error or fallback.
  │             │    No calls made to the failing service.
  └──────┬──────┘
         │  After a wait period (e.g., 30 seconds)
         ▼
  ┌─────────────┐
  │  HALF-OPEN  │  ← Allows a few test requests through.
  │  (testing)  │    If they succeed → back to CLOSED.
  │             │    If they fail → back to OPEN.
  └─────────────┘


WHAT HAPPENS WHEN CIRCUIT IS OPEN:
════════════════════════════════════

Without Circuit Breaker:
  Order → Product (DEAD) → waits 30s → timeout error

With Circuit Breaker OPEN:
  Order → Circuit Breaker → immediately returns FALLBACK response
  "Sorry, product info temporarily unavailable"
  Response in milliseconds, not 30 seconds.
```

## Fallback — Graceful Degradation

When a circuit opens, instead of crashing, you return a **fallback response**:

```
Normal:
  Order-Service calls Product-Service → gets real product details

Circuit Open (Product-Service down):
  Order-Service → Circuit Breaker returns fallback:
  "Product details temporarily unavailable, order still placed"

The system keeps working in a degraded but functional state.
This is called Graceful Degradation.
```

## In Spring Boot — Resilience4j

Spring Cloud Circuit Breaker uses **Resilience4j** as the implementation:

```
Key Resilience4j features:
┌──────────────────────────────────────────────────────────┐
│                                                           │
│  Circuit Breaker  → Opens/closes based on failure rate   │
│                                                           │
│  Retry            → Automatically retries failed calls   │
│                     (e.g., 3 times before giving up)     │
│                                                           │
│  Rate Limiter     → Limits calls per second to a service │
│                                                           │
│  Bulkhead         → Limits concurrent calls to a service │
│                     (prevents thread exhaustion)         │
│                                                           │
│  Time Limiter     → Sets max wait time for a response    │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

---

# CHAPTER 8 — Config Server

## The Problem — Config Chaos

With 10 microservices each having their own `application.properties`, managing configuration becomes a nightmare:

```
WITHOUT Config Server
══════════════════════

  product-service/application.properties  → db.url=jdbc:mysql://...
  order-service/application.properties    → db.url=jdbc:mysql://...
  payment-service/application.properties  → db.url=jdbc:mysql://...

  Change database password?
  → Update 10 files
  → Redeploy 10 services
  → Hope you didn't miss one
```

## The Solution — Centralized Configuration

```
WITH Config Server
═══════════════════

  One Git repository holds ALL configs:
  ┌─────────────────────────────────┐
  │        Git Repository           │
  │  product-service.properties     │
  │  order-service.properties       │
  │  payment-service.properties     │
  │  application.properties (global)│
  └────────────────┬────────────────┘
                   │
                   ▼
          ┌─────────────────┐
          │  Config Server  │  ← Spring Cloud Config Server
          │  :8888          │    reads from Git
          └────────┬────────┘
                   │ serves config on request
        ┌──────────┼───────────┐
        ▼          ▼           ▼
   product-    order-      payment-
   service     service     service

  Change a config?
  → Update ONE file in Git
  → Services refresh automatically (with Spring Actuator /refresh)
  → No redeploy needed!
```

## Config Server Features

```
┌────────────────────────────────────────────────────────┐
│               CONFIG SERVER CAPABILITIES               │
│                                                        │
│  ✓ Centralized config for all services                 │
│                                                        │
│  ✓ Environment-specific configs:                       │
│     product-service-dev.properties  (development)      │
│     product-service-prod.properties (production)       │
│                                                        │
│  ✓ Encrypted sensitive values (passwords, API keys)    │
│     password={cipher}AQA3ksd9f2k...  ← encrypted      │
│                                                        │
│  ✓ Git-backed — full change history / rollback         │
│                                                        │
│  ✓ Live refresh — services pick up changes             │
│     without restart via /actuator/refresh              │
└────────────────────────────────────────────────────────┘
```

---

# CHAPTER 9 — Distributed Logging & Monitoring

## The Problem — Logs Are Scattered Everywhere

```
WITHOUT Centralized Logging
═════════════════════════════

User reports: "My order failed at 3:42 PM"

You need to check:
  → SSH into API Gateway server    → grep logs → nothing useful
  → SSH into Order-Service server  → grep logs → partial info
  → SSH into Payment server        → grep logs → found an error!

But how do you connect these logs?
Each service has its own log with no shared request ID.
Finding the root cause is like solving a puzzle blindfolded.
```

## Distributed Tracing — The Solution

Every request gets a unique **Trace ID** that travels through all services:

```
Request enters API Gateway:
  Trace ID: abc-123 assigned ◀────── same ID travels everywhere

  Gateway    [abc-123] Received GET /orders/1
      │
      ▼
  Order-Service  [abc-123] Processing order 1
      │
      ▼
  Product-Service [abc-123] Looking up product
      │
      ▼
  Payment-Service [abc-123] ERROR: Card declined ❌

Now you search for "abc-123" in one place → complete picture!
```

## The Logging & Monitoring Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    OBSERVABILITY TOOLS                       │
│                                                             │
│  DISTRIBUTED TRACING                                        │
│  ┌──────────┐  ┌──────────┐                                 │
│  │  Zipkin  │  │  Jaeger  │  ← Trace request across services│
│  └──────────┘  └──────────┘                                 │
│                                                             │
│  LOG AGGREGATION (ELK Stack)                                │
│  ┌──────────────────────────────────────┐                   │
│  │ Elasticsearch + Logstash + Kibana    │                   │
│  │ All service logs in one searchable   │                   │
│  │ dashboard with filters & charts      │                   │
│  └──────────────────────────────────────┘                   │
│                                                             │
│  METRICS & ALERTING                                         │
│  ┌────────────┐  ┌────────────┐                             │
│  │ Prometheus │  │  Grafana   │  ← CPU, memory, error rate │
│  │ (collects) │  │(visualizes)│     dashboards & alerts    │
│  └────────────┘  └────────────┘                             │
│                                                             │
│  HEALTH CHECKS                                              │
│  ┌─────────────────────────────┐                            │
│  │  Spring Boot Actuator       │  ← /actuator/health       │
│  │  Exposes metrics endpoints  │    /actuator/metrics       │
│  └─────────────────────────────┘    /actuator/info         │
└─────────────────────────────────────────────────────────────┘
```

## What Gets Monitored

```
Application Metrics:
  • Request rate (requests per second)
  • Error rate (% of failed requests)
  • Response time (p50, p95, p99 latencies)
  • Active connections

Infrastructure Metrics:
  • CPU usage per service
  • Memory consumption
  • Disk I/O
  • Network traffic

Business Metrics:
  • Orders placed per minute
  • Payment success rate
  • Products viewed most
```

---

# CHAPTER 10 — The Complete Architecture

Putting it all together:

```
╔══════════════════════════════════════════════════════════════════╗
║                  COMPLETE MICROSERVICES ARCHITECTURE             ║
╚══════════════════════════════════════════════════════════════════╝

  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
  │   Web App   │    │ Mobile App  │    │  3rd Party  │
  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
         └─────────────┬────┘─────────────┘
                       ▼
            ┌─────────────────────┐
            │     API GATEWAY     │  Auth, Rate Limit,
            │      :8000          │  Routing, Logging
            └──────────┬──────────┘
                       │
         ┌─────────────┼──────────────┐
         ▼             ▼              ▼
  ┌────────────┐ ┌───────────┐ ┌───────────┐
  │  Product   │ │   Order   │ │  Payment  │
  │  Service   │ │  Service  │ │  Service  │
  │   :8081    │ │   :8082   │ │   :8083   │
  └─────┬──────┘ └─────┬─────┘ └─────┬─────┘
        │ OpenFeign/   │ Circuit      │
        │ WebClient    │ Breaker      │
        └─────────┬────┘─────────────┘
                  │  (service-to-service calls)
                  │
   ┌──────────────▼─────────────────────────┐
   │           INFRASTRUCTURE               │
   │                                        │
   │  ┌──────────────┐  ┌───────────────┐   │
   │  │ Eureka Server│  │ Config Server │   │
   │  │  :8761       │  │  :8888        │   │
   │  │ (Registry)   │  │ (Git-backed)  │   │
   │  └──────────────┘  └───────────────┘   │
   │                                        │
   │  ┌──────────────┐  ┌───────────────┐   │
   │  │   Zipkin     │  │   Grafana +   │   │
   │  │  (Tracing)   │  │  Prometheus   │   │
   │  └──────────────┘  └───────────────┘   │
   └────────────────────────────────────────┘
```

---

# CHAPTER 11 — Component Summary Card

```
╔══════════════════════════════════════════════════════════════╗
║                  QUICK REFERENCE CARD                        ║
╠═══════════════════╦══════════════════════════════════════════╣
║ Component         ║ Purpose                                  ║
╠═══════════════════╬══════════════════════════════════════════╣
║ Eureka Server     ║ Service registry — phone book for        ║
║                   ║ services                                 ║
╠═══════════════════╬══════════════════════════════════════════╣
║ API Gateway       ║ Single entry point — routes, auth,       ║
║                   ║ rate limiting                            ║
╠═══════════════════╬══════════════════════════════════════════╣
║ Load Balancer     ║ Distributes requests across multiple     ║
║                   ║ instances of a service                   ║
╠═══════════════════╬══════════════════════════════════════════╣
║ Circuit Breaker   ║ Stops calls to failing services,         ║
║                   ║ returns fallback                         ║
╠═══════════════════╬══════════════════════════════════════════╣
║ Config Server     ║ Centralized config for all services      ║
║                   ║ from one Git repo                        ║
╠═══════════════════╬══════════════════════════════════════════╣
║ RestTemplate      ║ Old way to call other services over HTTP ║
╠═══════════════════╬══════════════════════════════════════════╣
║ WebClient         ║ Modern HTTP client, async capable        ║
╠═══════════════════╬══════════════════════════════════════════╣
║ OpenFeign         ║ Cleanest HTTP client — just an interface ║
╠═══════════════════╬══════════════════════════════════════════╣
║ Zipkin / Jaeger   ║ Trace a request across all services      ║
╠═══════════════════╬══════════════════════════════════════════╣
║ Prometheus        ║ Collect metrics from all services        ║
╠═══════════════════╬══════════════════════════════════════════╣
║ Grafana           ║ Visualize metrics in dashboards          ║
╠═══════════════════╬══════════════════════════════════════════╣
║ ELK Stack         ║ Aggregate, search and view all logs      ║
╠═══════════════════╬══════════════════════════════════════════╣
║ Actuator          ║ Expose health, metrics endpoints for     ║
║                   ║ monitoring tools                         ║
╚═══════════════════╩══════════════════════════════════════════╝
```

---

# CHAPTER 12 — Learning Roadmap

```
BEGINNER
════════
  □ Understand Monolith vs Microservices
  □ Build Eureka Server
  □ Build 2 services that register to Eureka
  □ Set up API Gateway with basic routes
  □ Call one service from another using WebClient

INTERMEDIATE
════════════
  □ Add OpenFeign for cleaner inter-service calls
  □ Add Circuit Breaker with fallback using Resilience4j
  □ Set up Spring Cloud Config Server
  □ Add Spring Boot Actuator for health checks
  □ Understand load balancing with multiple instances

ADVANCED
════════
  □ Add distributed tracing with Zipkin
  □ Set up Prometheus + Grafana dashboards
  □ Centralize logs with ELK Stack
  □ Add JWT authentication in the Gateway
  □ Containerize everything with Docker
  □ Orchestrate with Kubernetes
```

---

> **The golden rule of microservices:** Each service should do **one thing** and do it well. If you're unsure whether something should be its own service, ask — "Can I deploy this independently without touching anything else?" If yes, it's a good microservice candidate.