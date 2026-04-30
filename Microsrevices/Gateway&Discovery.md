# Spring Cloud Microservices: Service Discovery, API Gateway & Load Balancer


---

# PART 1 — The Problem That Started It All

Imagine you have a simple online store. In the old days, you'd build **one giant application** that handles everything — products, orders, payments, users. This is called a **Monolith**.

```
┌─────────────────────────────────────┐
│         MONOLITH APPLICATION        │
│                                     │
│  Products | Orders | Users | Pay   │
└─────────────────────────────────────┘
```

### What's wrong with a Monolith?

- If the **payment** module crashes, the **entire app** goes down
- You can't scale just the **product** section during a sale — you scale everything
- One team can't deploy without affecting another team's work
- A bug in one module can bring down unrelated modules

### The Microservices Solution

Break the monolith into small, independent services:

```
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   Product    │   │    Order     │   │   Payment    │
│   Service    │   │   Service    │   │   Service    │
│  :8081       │   │  :8082       │   │  :8083       │
└──────────────┘   └──────────────┘   └──────────────┘
```

Each service:
- Runs independently
- Can be scaled on its own
- Can crash without taking others down
- Can be deployed separately

**But now a new problem appears...** How does Order Service know where Product Service is running? What if it moves to a different server? What if there are 5 copies of Product Service running for load balancing — which one do you call?

This is exactly the problem that **Service Discovery** solves.

---

# PART 2 — Service Discovery (Eureka)

## What is Service Discovery?

Think of it like a **phone directory** or a **GPS**. Instead of hardcoding addresses, services register themselves, and others look them up by name.

```
Without Service Discovery:
  Order Service → calls → http://192.168.1.45:8081/products  ← HARDCODED, FRAGILE

With Service Discovery:
  Order Service → asks Eureka → "Where is product-service?"
  Eureka replies → "It's at 192.168.1.45:8081"
  Order Service → calls → http://192.168.1.45:8081/products  ← DYNAMIC, FLEXIBLE
```

## How Eureka Works — Step by Step

```
Step 1: Services start up and REGISTER themselves with Eureka
┌─────────────────┐        ┌──────────────────┐
│ product-service │──────▶ │   Eureka Server  │
│ "I'm alive at   │        │                  │
│  port 8081"     │        │  Registry:       │
└─────────────────┘        │  product-service │
                           │    → :8081       │
┌─────────────────┐        │  order-service   │
│  order-service  │──────▶ │    → :8082       │
│ "I'm alive at   │        └──────────────────┘
│  port 8082"     │
└─────────────────┘

Step 2: A service wants to talk to another — asks Eureka
┌─────────────────┐        ┌──────────────────┐
│  order-service  │──────▶ │   Eureka Server  │
│ "Where is       │        │                  │
│  product-       │        │ "product-service │
│  service?"      │◀────── │  is at :8081"    │
└─────────────────┘        └──────────────────┘

Step 3: Order service directly calls product service
┌─────────────────┐        ┌──────────────────┐
│  order-service  │──────▶ │ product-service  │
│                 │        │   :8081          │
└─────────────────┘        └──────────────────┘
```

## Heartbeats — How Eureka Knows a Service is Alive

Every service sends a **heartbeat** to Eureka every 30 seconds saying "I'm still alive." If Eureka doesn't hear from a service for 90 seconds, it removes it from the registry. This way, dead services are never returned in lookups.

```
product-service ──ping──▶ Eureka  (every 30s)
product-service ──ping──▶ Eureka
product-service ──ping──▶ Eureka
product-service   💥CRASH💥
                           Eureka waits...90s...
                           Eureka removes product-service from registry
```

---

## Setting Up Eureka Server

### Step 1 — Create a new Spring Boot project with this dependency in `pom.xml`

```xml
<properties>
    <java.version>21</java.version>
    <spring-cloud.version>2025.1.1</spring-cloud.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Step 2 — Main Application class

```java
@SpringBootApplication
@EnableEurekaServer          // ← One annotation turns this into a registry
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### Step 3 — `application.properties`

```properties
spring.application.name=eureka-server
server.port=8761

# Don't register the Eureka server with itself
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

> **Why `register-with-eureka=false`?** Because Eureka itself is not a microservice — it's the registry. There's no reason for it to register with itself.

### Step 4 — Visit the Dashboard

Start the app and open `http://localhost:8761`

You'll see the Eureka dashboard showing all registered services. Right now it's empty — services register when they start up.

---

## Making a Service Register with Eureka

In your **product-service** and **order-service**, add this dependency:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

And these properties:

```properties
spring.application.name=product-service   ← This becomes the service name in Eureka
server.port=8081

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
```

That's literally it. Spring Boot auto-detects the Eureka client dependency and registers automatically on startup.

---

# PART 3 — Load Balancer

## What is a Load Balancer?

Suppose your Product Service is very busy. You run **3 copies** of it on different ports. Without a load balancer, you'd have to manually pick which one to call. A load balancer **automatically distributes** the calls across all running copies.

```
                     ┌─────────────────────┐
                     │    Load Balancer     │
                     └──────────┬──────────┘
              ┌─────────────────┼─────────────────┐
              ▼                 ▼                 ▼
   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
   │ product-service  │  │ product-service  │  │ product-service  │
   │   Instance 1     │  │   Instance 2     │  │   Instance 3     │
   │   :8081          │  │   :8082          │  │   :8083          │
   └──────────────────┘  └──────────────────┘  └──────────────────┘
```

## Client-Side Load Balancing with Spring Cloud

Spring Cloud uses **client-side load balancing**. This means the caller itself decides which instance to call — it asks Eureka for all available instances, then picks one using Round Robin (1st call → Instance 1, 2nd call → Instance 2, 3rd call → Instance 3, then back to 1).

```
order-service asks Eureka:
  "All instances of product-service?"

Eureka replies:
  → :8081
  → :8082
  → :8083

Spring Load Balancer in order-service picks :8081 (Round Robin)
Next call picks :8082, then :8083, then back to :8081...
```

## Setting Up Load Balancer

Add this dependency to any service that calls another service:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

Then configure `WebClient` with `@LoadBalanced` so it knows to use Eureka + Load Balancer:

```java
@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced   // ← Magic annotation: enables service-name resolution + load balancing
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

Now in your service, call by **name** not by hardcoded URL:

```java
// ❌ Hardcoded — breaks if product-service moves or scales
webClient.get().uri("http://localhost:8081/products/1")

// ✅ By name — load balanced, Eureka-resolved
webClient.get().uri("http://product-service/products/1")
//                        ↑ Spring sees this, asks Eureka, picks an instance
```

---

# PART 4 — API Gateway

## The Problem Without an API Gateway

With multiple microservices, your frontend would need to know about all of them:

```
Mobile App:
  Products → http://192.168.1.10:8081
  Orders   → http://192.168.1.11:8082
  Payment  → http://192.168.1.12:8083
  Users    → http://192.168.1.13:8084
```

Problems:
- Frontend knows internal service locations — a **security risk**
- If any service moves, you update the **frontend too**
- You have to handle **CORS, Auth, Rate Limiting** in every single service
- Mobile apps hitting 4 different servers — **no single entry point**

## The API Gateway Solution

One single entry point for everything. The gateway handles routing:

```
Mobile App → http://my-app.com (Gateway :8000)
                     │
          ┌──────────┼────────────┐
          ▼          ▼            ▼
    /products    /orders      /payments
          │          │            │
   product-     order-       payment-
   service      service      service
```

The gateway's job:
- **Route** `/products/**` → product-service
- **Route** `/orders/**` → order-service
- Handle **Authentication** in one place
- Handle **CORS** in one place
- Handle **Rate Limiting** in one place

## Understanding Your Setup

You are using `spring-cloud-starter-gateway-server-webmvc` with Spring Cloud **2025.x**. This is the newer **MVC-based** (blocking) gateway, as opposed to the older Reactive (WebFlux) gateway.

| Feature | Reactive Gateway | MVC Gateway (yours) |
|---|---|---|
| Dependency | `spring-cloud-starter-gateway` | `spring-cloud-starter-gateway-server-webmvc` |
| Stack | WebFlux (non-blocking) | Spring MVC (blocking) |
| Property prefix | `spring.cloud.gateway.routes` | `spring.cloud.gateway.server.webmvc.routes` |
| Spring Cloud | Up to 2024.x | **2025.x onwards** |

---

## Setting Up API Gateway

### `pom.xml`

```xml
<properties>
    <java.version>21</java.version>
    <spring-cloud.version>2025.1.1</spring-cloud.version>
</properties>

<dependencies>

    <!-- MVC Gateway — your version -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway-server-webmvc</artifactId>
    </dependency>

    <!-- So gateway can find services by name from Eureka -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <!-- Required for lb:// to work — CRITICAL -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>

</dependencies>
```

> **Why is loadbalancer needed in the gateway?** Because when you write `lb://product-service` in your gateway routes, the `lb://` prefix tells Spring to use the load balancer to resolve the service name. Without this dependency, `lb://` simply doesn't resolve — causing a silent 404.

### `application.properties` — The Correct Way for Spring Cloud 2025.x

```properties
spring.application.name=api-gateway
server.port=8000

# ─────────────────────────────────────────
# ROUTES — Spring Cloud 2025.x syntax
# ─────────────────────────────────────────

# Route 1: Product Service
spring.cloud.gateway.server.webmvc.routes[0].id=product-service-route
spring.cloud.gateway.server.webmvc.routes[0].uri=lb://product-service
spring.cloud.gateway.server.webmvc.routes[0].predicates[0]=Path=/products/**

# Route 2: Order Service
spring.cloud.gateway.server.webmvc.routes[1].id=order-service-route
spring.cloud.gateway.server.webmvc.routes[1].uri=lb://order-service
spring.cloud.gateway.server.webmvc.routes[1].predicates[0]=Path=/orders/**

# ─────────────────────────────────────────
# EUREKA
# ─────────────────────────────────────────
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# ─────────────────────────────────────────
# DEBUG — See what the gateway is doing
# ─────────────────────────────────────────
logging.level.org.springframework.cloud.gateway=DEBUG
```

### Main Application Class

```java
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

No extra annotations needed — the gateway auto-configures from your properties.

---

## Understanding Routes — Line by Line

```properties
spring.cloud.gateway.server.webmvc.routes[0].id=product-service-route
```
A unique name for this route. Used in logs and debugging. Can be anything.

```properties
spring.cloud.gateway.server.webmvc.routes[0].uri=lb://product-service
```
Where to forward the request. `lb://` means **use load balancer**. `product-service` must **exactly match** the `spring.application.name` in your product-service properties.

```properties
spring.cloud.gateway.server.webmvc.routes[0].predicates[0]=Path=/products/**
```
The **condition** for this route to activate. `/**` means "anything after `/products/`". So `/products`, `/products/1`, `/products/search` all match.

---

## What Happens When You Call the Gateway

Let's trace a real request:

```
YOU: GET http://localhost:8000/products

GATEWAY receives request at :8000
  → Checks routes: does /products match any predicate?
  → YES → routes[0] has Path=/products/**  ✅

GATEWAY looks at uri: lb://product-service
  → Asks Eureka: "Give me all instances of product-service"
  → Eureka returns: [localhost:8081]
  → Load balancer picks: localhost:8081

GATEWAY forwards request:
  → GET http://localhost:8081/products

PRODUCT SERVICE responds:
  → ["Asus A15", "Samsung S3", "Skoda Rapid"]

GATEWAY sends response back to YOU:
  → ["Asus A15", "Samsung S3", "Skoda Rapid"]
```

You never knew product-service was on port 8081. The gateway handled everything.

---

# PART 5 — Startup Order & Testing

## Always Start in This Order

```
1. Eureka Server     → Must be up first
        ↓
2. Product Service   → Registers with Eureka on startup
   Order Service     → Registers with Eureka on startup
        ↓
3. API Gateway       → Registers with Eureka + loads route config
```

If you start services before Eureka, they'll keep retrying to register (Spring Cloud has retry built-in), but it's cleaner to follow the order.

---

## Version Gotcha — Property Prefix History

This is the most common source of confusion for beginners using modern Spring Cloud:

```
Spring Cloud 2023.x   →  spring.cloud.gateway.routes[0]...
Spring Cloud 2024.x   →  spring.cloud.gateway.mvc.routes[0]...       (deprecated)
Spring Cloud 2025.x   →  spring.cloud.gateway.server.webmvc.routes[0]...  ✅ CORRECT
```

If you ever upgrade or downgrade Spring Cloud, **the first thing to check is this prefix**.

---

# PART 6 — Full Working Properties for Your Project

### `eureka-server/application.properties`
```properties
spring.application.name=eureka-server
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### `product-service/application.properties`
```properties
spring.application.name=product-service
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/springecomm
spring.datasource.username=springuser
spring.datasource.password=spring@123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

### `order-service/application.properties`
```properties
spring.application.name=order-service
server.port=8082

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

### `api-gateway/application.properties`
```properties
spring.application.name=api-gateway
server.port=8000

spring.cloud.gateway.server.webmvc.routes[0].id=product-service-route
spring.cloud.gateway.server.webmvc.routes[0].uri=lb://product-service
spring.cloud.gateway.server.webmvc.routes[0].predicates[0]=Path=/products/**

spring.cloud.gateway.server.webmvc.routes[1].id=order-service-route
spring.cloud.gateway.server.webmvc.routes[1].uri=lb://order-service
spring.cloud.gateway.server.webmvc.routes[1].predicates[0]=Path=/orders/**

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
logging.level.org.springframework.cloud.gateway=DEBUG
```

---

# PART 7 — Common Mistakes Cheatsheet

| Mistake | Symptom | Fix |
|---|---|---|
| Wrong property prefix for your Spring Cloud version | 404 from gateway | Use `spring.cloud.gateway.server.webmvc.routes` for 2025.x |
| Missing `spring-cloud-starter-loadbalancer` | 404 when using `lb://` | Add the dependency |
| `lb://` name doesn't match `spring.application.name` | 503 Service Unavailable | They must match exactly |
| Starting services before Eureka | Services don't appear in dashboard | Start Eureka first |
| `eureka.client.service-url.default-zone` (kebab) | Eureka not connected | Use `defaultZone` (camelCase) |
| Adding `spring-boot-starter-web` to gateway | Conflicts / startup failure | Gateway MVC already includes web |

---

# Summary — The Big Picture

```
  Client (Browser / Mobile / Postman)
           │
           ▼
  ┌─────────────────┐
  │   API Gateway   │  :8000  ← Single entry point
  │                 │         ← Routes requests
  │  /products/**──────────────────────┐
  │  /orders/**  ──────────────┐       │
  └─────────────────┘          │       │
           │                   │       │
           │ (registers)       │       │
           ▼                   ▼       ▼
  ┌─────────────────┐  ┌──────────┐  ┌────────────┐
  │  Eureka Server  │  │  Order   │  │  Product   │
  │  :8761          │  │  Service │  │  Service   │
  │                 │  │  :8082   │  │  :8081     │
  │  Registry:      │  └──────────┘  └────────────┘
  │  product→:8081  │       │               │
  │  order  →:8082  │◀──────┴───────────────┘
  │  gateway→:8000  │       (all register here)
  └─────────────────┘
```

- **Eureka** = the phone book (who is where)
- **Load Balancer** = the traffic distributor (which copy to call)
- **API Gateway** = the front door (one URL for everything)