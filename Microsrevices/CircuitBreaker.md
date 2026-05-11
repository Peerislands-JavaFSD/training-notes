# Circuit Breaker Pattern

You already have the perfect production-ready foundation:

```
Eureka Server (8761)  
└── customer-service (multiple instances)  
└── order-service  
└── api-gateway (8080) → single entry point + load balancing
```

Now we add **Resilience4j** — the **official, modern, and recommended** Circuit Breaker for Spring Boot 3 + Spring Cloud 2024–2025.

### What is a Circuit Breaker? (Real-World Analogy)

Imagine electricity in your house:
- Normal → Switch closed → Power flows
- Too many failures (short circuit) → Breaker trips → Switch opens → No more calls
- After cooldown → Half-open → Test one call → If OK → Closes again

Same in microservices:

| State    | Meaning                                   | Allows Calls? | Real Example |
|----------|-------------------------------------------|---------------|--------------|
| CLOSED   | Everything working normally               | Yes           | customer-service is fast & healthy |
| OPEN     | Too many errors → protect downstream       | No (fast fail)| customer-service is down/slow |
| HALF-OPEN| After waiting → test if it's recovered    | One test call | Try once after 30 seconds |

**Goal:** Prevent cascading failures. Don’t hammer a dying service.

### Resilience4j is the Winner in 2025

| Tool            | Status in 2025                     | Used in This Tutorial |
|------------------|------------------------------------|------------------------|
| Netflix Hystrix  | Dead (deprecated since 2018)       | Not used            |
| Resilience4j     | Official Spring recommendation     | Yes (light, fast, functional) |

### Step-by-Step: Add Circuit Breaker to Your Setup

We will protect the **call from order-service → customer-service** using Resilience4j.

#### Step 1: Add Resilience4j to order-service

**pom.xml** (in `order-service` only)
```xml
<dependencies>
    <!-- Existing dependencies... -->

    <!-- Resilience4j Core + Spring Boot Integration -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot3</artifactId>
        <version>2.2.0</version>
    </dependency>

    <!-- For nice /actuator endpoints (optional but recommended) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

#### Step 2: Configure Circuit Breaker in application.yml (order-service)

```yaml
server:
  port: 8082

spring:
  application:
    name: order-service

# Actuator endpoints (to see circuit breaker state live)
management:
  endpoints:
    web:
      exposure:
        include: health,info,circuitbreakers,circuitbreakerevents

# Resilience4j Configuration
resilience4j:
  circuitbreaker:
    instances:
      customerServiceCb:                 # Name of our circuit breaker
        register-health-indicator: true  # Shows in /actuator/health
        sliding-window-size: 10          # Evaluate last 10 calls
        minimum-number-of-calls: 5       # Need at least 5 calls to decide
        failure-rate-threshold: 50       # >50% failed → OPEN
        wait-duration-in-open-state: 30s # Stay open for 30 seconds
        automatic-transition-from-open-to-half-open-enabled: true
        permitted-number-of-calls-in-half-open-state: 3

  timelimiter:
    instances:
      customerServiceCb:
        timeout-duration: 2s            # If call takes >2s → timeout = failure

  retry:
    instances:
      customerServiceCb:
        max-attempts: 3
        wait-duration: 500ms
```

#### Step 3: Apply Circuit Breaker to Feign Client

Modify your **CustomerClient** in `order-service`:

```java
package com.example.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
@CircuitBreaker(name = "customerServiceCb", fallbackMethod = "getCustomerFallback")
@Retry(name = "customerServiceCb")  // Optional: retry before giving up
public interface CustomerClient {

    @GetMapping("/api/customers/{id}")
    CustomerDto getCustomerById(@PathVariable("id") Long id);

    // Fallback method (same signature + Throwable param)
    default CustomerDto getCustomerFallback(Long id, Throwable t) {
        return new CustomerDto(id, "Default Customer (Offline)", "offline@example.com");
    }
}

record CustomerDto(Long id, String name, String email) {}
```

#### Step 4: Update OrderController to Show Fallback Clearly

```java
@GetMapping("/{orderId}/customer")
public String getCustomerForOrder(@PathVariable Long orderId) {
    try {
        var customer = customerClient.getCustomerById(orderId);
        return String.format("Order %d → %s (%s)", orderId, customer.name(), customer.email());
    } catch (Exception e) {
        return "Order " + orderId + " → Customer service unavailable. Using fallback.";
    }
}
```

### Live Demo: See Circuit Breaker in Action

#### Scenario 1: Everything Healthy (CLOSED)

Start all services normally → Call 10 times:

```bash
curl http://localhost:8080/api/orders/1/customer
→ "Order 1 → John Doe 1 (john1@example.com)"
```

Circuit Breaker State: **CLOSED**  
Check: http://localhost:8082/actuator/circuitbreakers → shows `CLOSED`

#### Scenario 2: Simulate Failure → Trigger OPEN State

Stop **all** customer-service instances (or make one return 500)

Now spam the endpoint:

```bash
for i in {1..15}; do curl http://localhost:8080/api/orders/99/customer; echo; done
```

**You will see:**
- First few calls → 500 errors or timeouts
- After ~6–7 failed calls → Circuit Breaker **opens**
- Next calls → **Instant fallback** (no network call!)

Response becomes:
```
Order 99 → Default Customer (Offline) (offline@example.com)
```

**No delay! Fast fail!** This prevents cascading failure.

Check live state:
```
http://localhost:8082/actuator/health
→ "circuitBreakers": "CLOSED" → becomes "OPEN"
```

#### Scenario 3: Recovery → HALF-OPEN → CLOSED

Wait 30 seconds → Restart one customer-service instance

After 30s, the circuit breaker allows **3 test calls**:
- First test call → succeeds → Circuit closes automatically
- All future calls → back to normal

**Fully automatic recovery!**

### Visual Summary: Circuit Breaker States

```
CLOSED          → 90% success → stay CLOSED
   ↓ (too many errors)
OPEN            → 30 seconds → no calls, fast fallback
   ↓ (after wait)
HALF-OPEN       → Allow 3 test calls
   ↓ (success)           ↓ (still failing)
CLOSED                  → stay OPEN longer
```

### Bonus: Monitor Circuit Breakers in Real-Time

Open these endpoints (order-service):

- http://localhost:8082/actuator/health
- http://localhost:8082/actuator/circuitbreakers
- http://localhost:8082/actuator/circuitbreakerevents

You can even integrate with **Grafana + Prometheus + Micrometer** later.

### Final Architecture with Circuit Breaker

```
External Client
       ↓
API Gateway → Load Balancer → customer-service (3 instances)
                     ↑
               Circuit Breaker (Resilience4j)
               protects this call
       order-service → CustomerClient (Feign)
```

### Summary: What You Now Have (Full Resilience Pattern)

| Pattern                  | Tool Used                  | Where Applied                     | Benefit |
|--------------------------|----------------------------|-----------------------------------|---------|
| Service Discovery        | Eureka                     | All services                      | Dynamic IPs |
| API Gateway              | Spring Cloud Gateway       | Single entry point                | Clean URLs |
| Load Balancing           | Spring Cloud LoadBalancer  | Gateway + Feign                   | Scale out |
| Circuit Breaker          | Resilience4j               | order-service → customer-service  | Prevent cascading failures |
| Retry + Timeout          | Resilience4j               | Same call                         | Handle transient errors |
| Fallback                 | Custom method              | When circuit is OPEN              | Graceful degradation |

You now have the **complete resilience stack** used by Netflix, Alibaba, Uber, and all serious microservices teams in 2025.

Your system will **never completely crash** because one service is slow or down.
