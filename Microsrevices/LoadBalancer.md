# Load Balancing in Microservices


You already have this stack:

```
Eureka Server (8761)  
└── customer-service (many instances)  
└── order-service  
└── api-gateway (8080) → single entry point
```

Now we will **deeply understand Load Balancing** and see **exactly where and how it works** in your setup — with real examples you can run and test.

### What is Load Balancing? (Simple Explanation)

**Goal:** When you have 3 running instances of `customer-service`, which one should handle the next request?

| Type                    | Where it Runs                  | Who Does It in Your Project?               | Example in Your Setup                                  |
|-------------------------|--------------------------------|--------------------------------------------|--------------------------------------------------------|
| Server-side LB          | External (Nginx, AWS ALB, etc.)| Not used here                                      | —                                                      |
| Client-side LB          | Inside the caller application  | 2 Places do it automatically:                     |                                                        |
| 1. API Gateway LB       | Inside Spring Cloud Gateway    | Spring Cloud LoadBalancer (automatic)             | Gateway → customer-service (multiple instances)        |
| 2. Feign/WebClient LB   | Inside order-service           | Spring Cloud LoadBalancer (automatic)             | order-service → customer-service (multiple instances)  |

**Good News:** In Spring Boot 3 + Spring Cloud 2024+, **you get client-side load balancing for FREE** — no extra code!

### Let’s Prove It with a Real Running Example

We will start **3 instances** of `customer-service` on different ports and watch load balancing in action from **both directions**.

#### Step 1: Modify Customer Service to Show Which Instance Responded

Add this to `CustomerController.java` (in customer-service project):

```java
import org.springframework.beans.factory.annotation.Value;  // ← Add this

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Value("${server.port}")  // ← Shows which port (instance) handled the request
    private String port;

    @GetMapping("/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        return new Customer(id, "John Doe " + id, "john" + id + "@example.com", "Served by instance on port: " + port);
    }
}

record Customer(Long id, String name, String email, String servedBy) {}
```

#### Step 2: Start 3 Instances of customer-service

Open 3 terminals and run:

```bash
# Instance 1
java -jar customer-service-1.0.0.jar --server.port=8081

# Instance 2
java -jar customer-service-1.0.0.jar --server.port=8082

# Instance 3
java -jar customer-service-1.0.0.jar --server.port=8083
```

All 3 will register in Eureka → You’ll see in http://localhost:8761:

```
CUSTOMER-SERVICE  → 3 instances UP (8081, 8082, 8083)
```

#### Test 1: Load Balancing from API Gateway (Most Important in Production)

Call the **gateway** 10 times rapidly:

```bash
for i in {1..10}; do
  curl http://localhost:8080/api/customers/99
  echo "\n---"
done
```

**You will see output rotating between ports:**

```json
{"id":99,"name":"John Doe 99","email":"john99@example.com","servedBy":"Served by instance on port: 8082"}
---
{"id":99,"name":"John Doe 99","email":"john99@example.com","servedBy":"Served by instance on port: 8081"}
---
{"id":99,"name":"John Doe 99","email":"john99@example.com","servedBy":"Served by instance on port: 8083"}
---
{"id":99,"name":"John Doe 99","email":"john99@example.com","servedBy":"Served by instance on port: 8081"}
```

**This is API Gateway doing client-side load balancing automatically!**  
No Nginx, no AWS ALB needed.

#### Test 2: Load Balancing from Order Service (Internal Call)

Now call the order-service endpoint (which uses Feign to call customer-service):

```bash
for i in {1..10}; do
  curl http://localhost:8080/api/orders/88/customer
  echo "\n---"
done
```

**Expected Output (rotating ports):**

```
Order 88 belongs to John Doe 88 (john88@example.com) → Served by port: 8083
---
Order 88 belongs to John Doe 88 (john88@example.com) → Served by port: 8081
---
Order 88 belongs to John Doe 88 (john88@example.com) → Served by port: 8082
```

**Order-service also load-balances automatically using the same Spring Cloud LoadBalancer!**

### How Does This Magic Work? (Under the Hood)

| Component                        | What It Does                                                                 |
|----------------------------------|------------------------------------------------------------------------------|
| `lb://customer-service`          | Special URI scheme → “load balance this serviceId from Eureka”               |
| Spring Cloud LoadBalancer        | Replaces old Ribbon (2024 default)                                           |
| RoundRobinLoadBalancer (default) | Default strategy → cycles through healthy instances                          |
| Health checks                    | Eureka heartbeats every 30s → unhealthy instances removed automatically     |

### Optional: Change Load Balancing Strategy (Just for Fun)

Add to **api-gateway** or **order-service** `application.yml`:

```yaml
spring:
  cloud:
    loadbalancer:
      configurations: random   # or round-robin (default), weighted, etc.
```

Or use **Round Robin with Sticky Session** (advanced):

```yaml
spring:
  cloud:
    loadbalancer:
      configurations: sticky-session
```

Or even **weighted** (if some instances are stronger):

```yaml
# In customer-service instance with more power
eureka:
  instance:
    metadata-map:
      weight: 90   # higher = more requests
```

### Final Architecture with Load Balancing Visualized

```
External Clients
        ↓
http://localhost:8080 (API Gateway)
        ↓  ← Spring Cloud LoadBalancer (Round Robin)
┌───────────────────────────────────────┐
│     customer-service instances          │
│   ┌─────────────┐  ┌─────────────┐     │
│   │   port 8081 │  │   port 8082 │     │
│   └─────────────┘  └─────────────┘     │
│           │               │           │
│           └──────┬────────┘           │
│                  ▼                    │
│             port 8083                 │
└───────────────────────────────────────┘
        ↑
Eureka knows all 3 are healthy
```

### Summary: Load Balancing in Your Project

| Caller               | Calls Service         | Load Balancer Used           | Where It Happens          | Strategy     |
|----------------------|-----------------------|------------------------------|---------------------------|--------------|
| API Gateway          | customer-service      | Spring Cloud LoadBalancer    | Inside Gateway            | Round-robin  |
| Order Service        | customer-service      | Spring Cloud LoadBalancer    | Inside Order Service      | Round-robin  |
| External Clients     | —                     | None needed                  | They only see port 8080   | —            |

**You didn’t write a single line of load balancing code — it just works!**

This is the **exact pattern** used by Netflix, Uber, Alibaba, and thousands of companies in production.

You now have the complete trifecta:

Service Discovery (Eureka)  
API Gateway (Spring Cloud Gateway)  
Client-Side Load Balancing (Spring Cloud LoadBalancer)
