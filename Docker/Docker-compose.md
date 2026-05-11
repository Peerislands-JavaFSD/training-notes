# Docker Compose 


This tutorial is structured progressively:
- **Theory**: Foundational concepts and principles.
- **YAML Syntax and Key Elements**: Building blocks.
- **CLI Commands**: Practical usage.
- **Best Practices**: Optimization and pitfalls.
- **Full-Stack Example**: Integrating Spring Boot (backend), React (frontend), and MySQL (database), with theory ties, Dockerfile recaps, and step-by-step setup.
- **Troubleshooting and Next Steps**.

## 1. Theory of Docker Compose

### 1.1 What is Docker Compose?
Docker Compose is a tool for **defining and running multi-container Docker applications**. It abstracts the complexity of manually running `docker run` for interconnected services (e.g., app + DB + cache) into a declarative YAML file (`docker-compose.yml` or `compose.yaml`).

- **Core Philosophy**: "Define once, run anywhere." Like a Dockerfile for single images, Compose files describe the *orchestration* of services, making environments reproducible across dev, staging, and prod.
- **Key Benefits (Theoretical)**:
    - **Simplicity**: Replaces shell scripts or Kubernetes YAML for local/multi-service setups.
    - **Isolation**: Each project gets its own network/volumes, preventing conflicts.
    - **Dependency Management**: Services start in order (e.g., DB before app).
    - **Scalability**: Scale services independently (e.g., 3 backend replicas).
    - **Portability**: YAML is human-readable and version-controlled; works with CI/CD.

### 1.2 Historical and Architectural Context
Introduced in 2013 (same era as Docker), Compose evolved from Fig (acquired by Docker). It runs on the Docker Engine, leveraging:
- **Networks**: Auto-created bridge networks for service discovery (e.g., `backend` service reachable as `db:3306`).
- **Volumes**: Shared persistence across containers.
- **Theoretical Model**: Think of it as a "mini-orchestrator." Services are like Kubernetes pods (groups of containers), but simpler for single-host use. For clusters, it pairs with Swarm/Kubernetes.

| Aspect             | Manual `docker run` Chains         | Docker Compose                    |
|--------------------|------------------------------------|-----------------------------------|
| **Definition**     | Imperative scripts                 | Declarative YAML                  |
| **Dependencies**   | Manual `--link` or waits           | Built-in `depends_on`             |
| **Environments**   | Fragile env vars                   | `.env` files + interpolation      |
| **Scaling**        | Repeated `docker run`              | `docker compose up --scale`       |
| **Tear-Down**      | Manual `docker rm/stop`            | `docker compose down`             |

### 1.3 Orchestration Lifecycle
1. **Parse YAML**: Compose validates and expands (e.g., env vars).
2. **Build/Pull**: Images from Dockerfiles or registries.
3. **Create Resources**: Networks, volumes.
4. **Start Services**: In dependency order; health checks for readiness.
5. **Monitor/Scale**: Logs, exec, restarts.
6. **Cleanup**: Optional removal of volumes/images.

Theory Tie-In: Builds on Docker's immutability—services use fixed images, but volumes persist state.

## 2. YAML Syntax and Key Elements

Compose files use YAML (v3.8+ recommended). Root keys: `services`, `networks`, `volumes`.

### 2.1 Core Structure
```yaml
version: '3.8'  # Specifies Compose file format (3.8 for modern features)

services:  # Define containers/services
  app:     # Service name (used for DNS, e.g., app:8080)
    build: ./app  # Path to Dockerfile/context
    # Or image: nginx:alpine  # Pre-built image
    ports:
      - "8080:8080"  # Host:container
    environment:
      - DB_HOST=db
    depends_on:
      - db
    volumes:
      - app-data:/app/data

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: secret
    volumes:
      - db-data:/var/lib/mysql

volumes:  # Named volumes (persistent)
  app-data:
  db-data:

networks:  # Custom networks (default: projectname_default)
  default:  # Or named: mynet
    driver: bridge
```

### 2.2 Essential Service Keys
| Key          | Purpose | Example | Notes |
|--------------|---------|---------|-------|
| **build**    | Local build context. | `build: ./backend` or `{ context: ., dockerfile: Dockerfile.dev }` | Ties to Dockerfile; supports args (`build: { args: { VERSION: 1.0 } }`). |
| **image**    | Remote/local image. | `image: mysql:8.0` | Pulled if missing. |
| **ports**    | Expose mappings. | `ports: ["3000:80"]` | Short syntax; use quotes for ranges (`"8000-8010:8000"`). |
| **environment** | Env vars. | `environment: { DB_URL: "jdbc:mysql://db:3306/mydb" }` or file: `env_file: .env` | Interpolates `${VAR}` from host/.env. |
| **depends_on** | Startup order. | `depends_on: [db]` | v3+: Use `condition: service_healthy` for health waits. |
| **volumes**  | Mounts. | `volumes: ["./host/dir:/container/dir", "named-vol:/data"]` | Binds or named (persistent). |
| **restart**  | Policy. | `restart: unless-stopped` | Auto-restart on crash. |
| **healthcheck** | Readiness probe. | `healthcheck: { test: ["CMD", "curl", "-f", "http://localhost"], interval: 30s }` | Integrates with depends_on. |
| **deploy**   | Scaling/replicas (Swarm mode). | `deploy: { replicas: 2 }` | Local: Use `--scale` CLI. |

- **Extensions**: `profiles` for conditional services; `configs/secrets` for prod secrets.
- **Validation**: `docker compose config` to preview expanded YAML.

## 3. CLI Commands

Use `docker compose` (no hyphen in v2+). Prefix with `docker compose -f custom.yml`.

- **Start/Stop**:
    - `docker compose up [service...]` : Builds/pulls, creates, starts. `-d`: Detached.
    - `docker compose down` : Stops/removes containers/networks. `--volumes`: Remove volumes; `-v` alias.
    - Example: `docker compose up -d backend frontend db`

- **Build/Logs**:
    - `docker compose build [service]` : Rebuild images. `--no-cache`: Fresh.
    - `docker compose logs [service]` : Tail logs. `-f`: Follow; `--timestamps`.

- **Scale/Exec**:
    - `docker compose up --scale backend=3 -d` : Run 3 replicas.
    - `docker compose exec backend bash` : Shell into service.

- **Inspect**:
    - `docker compose ps` : List services/status.
    - `docker compose config` : Validate/dump YAML.

- **Full Cycle Example**:
  ```
  $ docker compose up --build -d  # Build and start detached
  $ docker compose logs -f backend  # Monitor
  $ curl localhost:8080  # Test
  $ docker compose down -v  # Clean up
  ```

## 4. Best Practices

- **Version Pinning**: Use specific tags (e.g., `mysql:8.0.35`) for reproducibility.
- **Environment Separation**: Use `.env` for secrets; multiple compose files (`-f docker-compose.prod.yml -f docker-compose.dev.yml`).
- **Health Checks**: Always add for DBs/apps to ensure readiness.
- **Networks**: Explicitly define for security (e.g., isolate frontend from backend).
- **Volumes**: Named over anonymous for portability; back up DB volumes.
- **Security**: Avoid root in services; use `user: "1000:1000"`; scan with `docker scout`.
- **Optimization**: Separate build/prod compose files; use multi-stage Dockerfiles.
- **Pitfalls**: Circular depends_on loops; large images slowing startup.

## 5. Full-Stack Example: Spring Boot + React + MySQL

We'll containerize a simple full-stack app:
- **Spring Boot**: REST API (e.g., `/api/users`) connecting to MySQL.
- **React**: Frontend consuming the API (e.g., fetches users on load).
- **MySQL**: Persistent DB for users table.

**Theory**: This exemplifies microservices orchestration—services communicate via internal network (e.g., React → Spring → MySQL). Volumes ensure DB state survives restarts; ports expose only UI/API. Compose enforces "DB first" startup, mimicking prod topologies.

### 5.1 Project Structure
Create a monorepo:
```
my-fullstack-app/
├── backend/          # Spring Boot
│   ├── Dockerfile    # From previous tutorial
│   ├── src/          # Java code
│   └── pom.xml
├── frontend/         # React
│   ├── Dockerfile    # From previous
│   ├── src/          # JS/TS code
│   └── package.json
├── db/               # MySQL init (optional)
│   └── init.sql      # CREATE TABLE users...
├── docker-compose.yml
└── .env              # Secrets
```

### 5.2 Dockerfiles Recap (Minimal)
- **backend/Dockerfile** (Spring Boot, from prior; assumes JAR build):
  ```dockerfile
  FROM maven:3.9.6-eclipse-temurin-17 AS builder
  WORKDIR /app
  COPY pom.xml .
  RUN mvn dependency:go-offline -B
  COPY src ./src
  RUN mvn clean package -DskipTests

  FROM eclipse-temurin:17-jre-alpine
  WORKDIR /app
  COPY --from=builder /app/target/*.jar app.jar
  RUN addgroup -g 1001 appgroup && adduser -S appuser -u 1001 -G appgroup && chown -R appuser:appgroup /app
  USER appuser
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```
    - Spring Config: Use `application.yml` with `spring.datasource.url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:usersdb}`.

- **frontend/Dockerfile** (React, from prior):
  ```dockerfile
  FROM node:20-alpine AS builder
  WORKDIR /app
  COPY package*.json ./
  RUN npm ci
  COPY . .
  RUN npm run build

  FROM nginx:alpine
  COPY --from=builder /app/build /usr/share/nginx/html
  EXPOSE 80
  CMD ["nginx", "-g", "daemon off;"]
  ```
    - React: In `src/App.js`, fetch `http://localhost:8080/api/users` (proxied via nginx.conf if needed).

- **MySQL Init** (`db/init.sql`):
  ```sql
  CREATE DATABASE IF NOT EXISTS usersdb;
  USE usersdb;
  CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255));
  INSERT INTO users (name) VALUES ('Alice'), ('Bob');
  ```

### 5.3 docker-compose.yml
```yaml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=db
      - DB_PORT=3306
      - DB_NAME=usersdb
      - DB_USER=root
      - DB_PASSWORD=${DB_PASSWORD}  # From .env
    depends_on:
      db:
        condition: service_healthy
    volumes:
      - backend-logs:/app/logs  # Optional app logs
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    restart: unless-stopped
    networks:
      - app-network

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - app-network

  db:
    image: mysql:8.0
    command: --default-authentication-plugin=mysql_native_password  # For older clients
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: usersdb
    ports:
      - "3306:3306"  # Optional: Expose for local tools
    volumes:
      - db-data:/var/lib/mysql
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql  # Auto-run on first start
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
    networks:
      - app-network

volumes:
  db-data:  # Persistent MySQL data
  backend-logs:

networks:
  app-network:
    driver: bridge
```

### 5.4 .env File
```
DB_PASSWORD=supersecret123  # Change in prod!
```

### 5.5 Build and Run
1. **Prep**: Ensure Spring Boot connects via env vars; React fetches from `http://backend:8080/api/users` (internal DNS).
2. **Commands**:
   ```bash
   $ cd my-fullstack-app
   $ docker compose up --build -d  # Builds all, starts detached
   # Wait ~1-2 min for health checks

   $ docker compose ps  # Verify: 3/3 running
   $ docker compose logs db  # Check init.sql ran
   $ curl localhost:8080/api/users  # Test backend (returns JSON)
   $ open http://localhost:3000  # View React UI (displays users)

   $ docker compose exec db mysql -u root -p${DB_PASSWORD} usersdb -e "SELECT * FROM users;"  # Query DB
   $ docker compose down -v  # Stop; -v removes volumes (fresh restart)
   ```
3. **Scale Example**: `docker compose up -d --scale backend=2` (2 Spring instances sharing DB).

### 5.6 Theory Ties in Example
- **Dependency Chain**: `depends_on + healthcheck` ensures DB is queryable before backend starts—prevents connection errors.
- **Networking**: `app-network` enables seamless inter-service calls (e.g., backend uses `db:3306` hostname).
- **Persistence**: `db-data` volume survives `down/up`, modeling prod DBs.
- **Immutability**: Services rebuild from Dockerfiles; only data (volumes) changes.
- **Dev vs. Prod**: Local exposes all ports; prod might use internal-only networks + ingress.

## 6. Troubleshooting and Next Steps

### 6.1 Common Issues
- **Build Fails**: Check Dockerfile paths; use `docker compose build --no-cache backend`.
- **Connection Refused**: Verify health checks; tail logs (`docker compose logs -f`).
- **Port Conflicts**: Change host ports (e.g., `"8081:8080"`).
- **Volume Permissions**: MySQL may need `user: root` temp for init; chown in Dockerfile.
- **Env Interpolation**: Ensure `.env` is loaded; debug with `docker compose config`.

### 6.2 Next Steps
- **Extend**: Add Redis for caching (`image: redis:alpine`); use profiles for test/prod.
- **CI/CD**: Integrate with GitHub Actions: `docker compose build && docker compose push`.
- **Advanced**: Migrate to Kubernetes (compose-to-k8s converters exist); explore Docker Compose Watch for hot-reloads.
- **Practice**: Clone a sample repo (e.g., spring-boot-react-mysql on GitHub) and tweak.
