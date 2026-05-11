#  Docker Images for Spring Boot Microservices and Setting Up Docker Compose

This tutorial assumes a **microservices setup**:
- **Service Discovery**: Eureka Server for registering and discovering services.
- **Config-Server**: Centralized configuration management (pulls from Git or local repo).
- **API-Gateway**: Routes requests (e.g., via Spring Cloud Gateway) and handles auth/load balancing.
- **Order-Service & Product-Service**: Business services (e.g., REST APIs for orders/products), registering with Eureka and fetching config from Config-Server.
- **MySQL**: Shared or per-service DB (we'll use one for simplicity; services connect via JDBC).
- **React App**: Frontend consuming the API-Gateway (e.g., fetches products/orders).

**Theory Recap**: Docker images are immutable blueprints (layered for efficiency). Multi-stage builds keep runtime images slim (~150MB for Spring JARs). Docker Compose defines declarative orchestration, handling networks (for service discovery), volumes (for DB persistence), and dependencies (e.g., Eureka before services).

## 1. Prerequisites

Before starting, ensure:
- **Docker & Docker Compose**: Installed (v27+ for Docker, v2.27+ for Compose). Verify: `docker --version` and `docker compose version`.
- **Java 17+ & Maven**: For building Spring JARs locally (or use Docker for builds).
- **Node.js 18+**: For React development.
- **Project Setup**: A monorepo with subdirectories. We'll create one.
- **Git**: For Config-Server repo (optional but recommended).
- **IDE**: VS Code or IntelliJ for editing.

**Hardware**: 8GB+ RAM recommended (multiple services).

## 2. Project Structure

Organize your project as a monorepo for easy orchestration. Create the root directory:

```bash
mkdir microservices-docker && cd microservices-docker
```

Final structure:
```
microservices-docker/
├── api-gateway/              # Spring Cloud Gateway
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/                  # Main Java files
├── eureka-server/            # Service Discovery
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── config-server/            # Config Management
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── order-service/            # Business Service
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── product-service/          # Business Service
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── react-frontend/           # React App
│   ├── Dockerfile
│   ├── package.json
│   └── src/                  # JS/TS files
├── mysql-db/                 # MySQL Scripts (optional)
│   └── init.sql              # Schema init
├── docker-compose.yml
├── .env                      # Secrets
└── .gitignore                # Exclude builds, node_modules
```

- **Spring Services**: Each is a Maven project. Download starters from [start.spring.io](https://start.spring.io) with dependencies:
    - Common: Spring Web, Spring Boot Actuator.
    - Eureka: `spring-cloud-starter-netflix-eureka-server` (for server); `spring-cloud-starter-netflix-eureka-client` (for clients).
    - Config-Server: `spring-cloud-config-server`.
    - API-Gateway: `spring-cloud-starter-gateway`.
    - DB: `spring-boot-starter-data-jpa`, `mysql-connector-j`.
- **React**: Create with `npx create-react-app react-frontend`. Add fetches to `http://localhost:8080/api/...` (proxied via Gateway).
- **MySQL Init** (`mysql-db/init.sql`): Basic schema, e.g., `CREATE DATABASE ordersdb; CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(255));`.

For brevity, assume you have basic `@RestController` endpoints in services (e.g., `/api/orders` in Order-Service). Full code skeletons available in GitHub repos like "spring-cloud-microservices-example".

## 3. Building Docker Images

We'll use **multi-stage Dockerfiles** for Spring services: Stage 1 builds the JAR with Maven; Stage 2 runs it in a slim JRE image. This reduces size from ~1GB to ~150MB.

### 3.1 Generic Dockerfile Template for Spring Boot Services

Copy this to each Spring service dir (`api-gateway/Dockerfile`, etc.). Customize `LABEL` and `EXPOSE` as needed.

```dockerfile
# syntax=docker/dockerfile:1  # For BuildKit (optional)

# Stage 1: Build JAR with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B  # Download deps (cache hit if unchanged)
COPY src ./src
# Build fat JAR (skip tests for speed; enable in CI)
RUN mvn clean package -DskipTests

# Stage 2: Runtime (slim JRE, no Maven)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Security: Non-root user
RUN addgroup -g 1001 -S springgroup && \
    adduser -S springuser -u 1001 -G springgroup && \
    chown -R springuser:springgroup /app
USER springuser

# Healthcheck (assumes Actuator endpoint)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port (customize per service)
EXPOSE 8761  # Example: 8761 for Eureka; 8888 for Config; 8080 for others

# Env vars (override in Compose)
ENV SPRING_PROFILES_ACTIVE=default \
    EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/ \
    SPRING_CLOUD_CONFIG_URI=http://config-server:8888

# Run JAR
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]
```

#### Customizations by Service:
| Service          | Port (EXPOSE) | Key Dependencies/Notes |
|------------------|---------------|------------------------|
| **Eureka Server** | 8761        | `@EnableEurekaServer`; No DB. |
| **Config Server** | 8888        | `@EnableConfigServer`; Git repo in `application.yml`: `spring.cloud.config.server.git.uri=https://github.com/your/config-repo`. |
| **API-Gateway**  | 8080        | Routes: e.g., `zuul.routes.orders.path=/api/orders/** url=http://order-service:8080`. |
| **Order-Service**| 8082        | Client of Eureka/Config; JDBC to MySQL. |
| **Product-Service** | 8081     | Similar to Order-Service. |

- **Theory**: Caching on `pom.xml` speeds rebuilds (only re-downloads deps if changed). Non-root reduces attack surface. Healthcheck waits for startup (~30-60s for Spring).

### 3.2 Dockerfile for React Frontend

In `react-frontend/Dockerfile`:

```dockerfile
# Stage 1: Build with Node
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production=false  # Install all (incl. dev for build)
COPY . .
ENV REACT_APP_API_URL=http://localhost:8080  # Proxy to Gateway
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
# Optional: Custom nginx.conf for SPA routing
# COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

- **Theory**: Discards Node (~900MB) for static serving (~25MB). Use `REACT_APP_API_URL` for env-specific API calls.

### 3.3 Dockerfile for MySQL (Not Needed)

Use official image in Compose. For init scripts, mount `mysql-db/init.sql`.

### 3.4 Building the Images

In root dir, build each:

```bash
# Build Spring services (generic)
docker build -t eureka-server:v1 ./eureka-server
docker build -t config-server:v1 ./config-server
docker build -t api-gateway:v1 ./api-gateway
docker build -t order-service:v1 ./order-service
docker build -t product-service:v1 ./product-service

# Build React
docker build -t react-frontend:v1 ./react-frontend

# Verify
docker images | grep -E "(eureka|config|api|order|product|react)"
```

- **Output**: Expect ~150MB per Spring image, ~25MB for React.
- **Tips**: Use `--no-cache` if changes; tag with `:latest` for dev. Push to registry (e.g., Docker Hub) for prod: `docker tag eureka-server:v1 youruser/eureka-server:v1 && docker push ...`.

## 4. Setting Up Docker Compose

Docker Compose YAML defines services, networks, and volumes. Create `docker-compose.yml` in root.

### 4.1 docker-compose.yml

```yaml
version: '3.8'

services:
  # Service Discovery (starts first)
  eureka-server:
    image: eureka-server:v1
    container_name: eureka-server
    ports:
      - "8761:8761"
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s  # Eureka bootstrap time
    restart: unless-stopped
    networks:
      - microservices-net

  # Config Server (depends on Eureka? Optional; often independent)
  config-server:
    image: config-server:v1
    container_name: config-server
    ports:
      - "8888:8888"
    environment:
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
    depends_on:
      eureka-server:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8888/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - microservices-net

  # MySQL DB (independent, but init on start)
  mysql-db:
    image: mysql:8.0
    container_name: mysql-db
    ports:
      - "3306:3306"  # For local tools like MySQL Workbench
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: microdb  # Shared DB; use schemas per service
    volumes:
      - mysql-data:/var/lib/mysql
      - ./mysql-db/init.sql:/docker-entrypoint-initdb.d/init.sql  # Auto-init
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${DB_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
    networks:
      - microservices-net

  # API Gateway (routes to services)
  api-gateway:
    image: api-gateway:v1
    container_name: api-gateway
    ports:
      - "8080:8080"
    environment:
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/microdb?createDatabaseIfNotExist=true
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
      mysql-db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - microservices-net

  # Order Service
  order-service:
    image: order-service:v1
    container_name: order-service
    ports:
      - "8082:8082"  # Optional expose
    environment:
      - SERVER_PORT=8082
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/microdb?createDatabaseIfNotExist=true
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
      mysql-db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - microservices-net

  # Product Service (similar to Order)
  product-service:
    image: product-service:v1
    container_name: product-service
    ports:
      - "8081:8081"
    environment:
      - SERVER_PORT=8081
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/microdb?createDatabaseIfNotExist=true
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
      mysql-db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - microservices-net

  # React Frontend
  react-frontend:
    image: react-frontend:v1
    container_name: react-frontend
    ports:
      - "3000:80"
    depends_on:
      - api-gateway
    restart: unless-stopped
    networks:
      - microservices-net

volumes:
  mysql-data:  # Persistent DB storage

networks:
  microservices-net:
    driver: bridge  # Internal communication (e.g., order-service calls mysql-db:3306)
```

### 4.2 .env File

Create `.env` in root (gitignore it!):
```
DB_PASSWORD=supersecretmysqlpass  # Change for prod
```

- **Theory**: Networks enable DNS resolution (e.g., `mysql-db` hostname). `depends_on + healthcheck` ensures sequential startup (DB → Config/Eureka → Services). Volumes persist MySQL data across restarts.

## 5. Running with Docker Compose

### 5.1 Startup Sequence

1. **Validate Config**:
   ```bash
   docker compose config  # Dumps expanded YAML; check for errors
   ```

2. **Build & Start (if not pre-built)**:
   ```bash
   docker compose up --build -d  # Builds images if missing, starts detached
   ```
    - **Time**: ~5-10 min first run (pulls bases, builds JARs, inits DB).
    - **Monitor**: `docker compose logs -f` (follow all) or `docker compose logs -f order-service` (specific).

3. **Verify Services**:
    - **Eureka Dashboard**: `http://localhost:8761` (see registered services).
    - **Config Test**: `curl http://localhost:8888/product-service/default` (fetches config).
    - **Gateway**: `curl http://localhost:8080/api/products` (routes to Product-Service).
    - **Services**: `curl http://localhost:8081/actuator/health` (Product); similar for others.
    - **React**: `http://localhost:3000` (loads UI, fetches via Gateway).
    - **DB**: `docker compose exec mysql-db mysql -u root -p${DB_PASSWORD} microdb -e "SHOW TABLES;"`.

   Use `docker compose ps` to check status (all "Up").

### 5.2 Scaling & Management

- **Scale Services**: `docker compose up -d --scale order-service=2` (multiple replicas; Eureka handles load).
- **Exec/Debug**: `docker compose exec order-service bash` (shell in container).
- **Logs**: `docker compose logs --timestamps order-service`.
- **Restart Single**: `docker compose restart api-gateway`.
- **Shutdown**: `docker compose down` (stops containers). Add `-v` to remove volumes (fresh DB).

## 6. Best Practices & Troubleshooting

### 6.1 Best Practices
- **Security**: Use Docker Secrets for `${DB_PASSWORD}` in prod; scan images (`docker scout cves eureka-server:v1`).
- **Optimization**: Pin versions (e.g., `mysql:8.0.35`); use multi-arch builds (`docker buildx`).
- **Dev Workflow**: Mount volumes for hot-reload (`volumes: - ./order-service/src:/app/src`—but rebuild JAR).
- **CI/CD**: In GitHub Actions: `docker compose build && docker compose push` (tag images).
- **Monitoring**: Add Prometheus in Compose; expose Actuator `/metrics`.
- **Separate DBs**: For isolation, add per-service MySQL with unique volumes/schemas.

### 6.2 Troubleshooting
- **Build Fails**: Check Maven logs (`docker compose build order-service --progress=plain`). Ensure `pom.xml` has `<packaging>jar</packaging>`.
- **Connection Errors**: Tail logs for "Cannot connect to Eureka" (wait for health); verify env vars.
- **Port Conflicts**: Change host ports (e.g., `"8083:8082"` for Order).
- **Slow Startup**: Increase `start_period` in healthchecks; use `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`.
- **Out of Memory**: Add `--memory=1g` to `docker compose run`; or in YAML: `deploy: { resources: { limits: { memory: 1G } } }`.
- **React Proxy Issues**: Ensure fetches use relative paths or proxy config in `package.json` ("proxy": "http://api-gateway:8080").
