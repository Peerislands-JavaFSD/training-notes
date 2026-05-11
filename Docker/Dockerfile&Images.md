# Dockerfile

### 1.1 What is a Dockerfile?
A **Dockerfile** is a text file (no extension) containing a series of instructions that Docker follows sequentially to build an image. It's **declarative** (describe the end state) and **imperative** (step-by-step execution), producing immutable, reproducible images.

- **Layered Architecture Recap**: Each instruction creates a **layer**—a diff of file system changes. Layers are cached: unchanged instructions reuse prior layers, speeding up rebuilds. The final image is a stack of these read-only layers plus a thin writable layer at runtime.
    - **Analogy**: Like Git commits. `RUN apt install` is a commit adding packages; `COPY . /app` adds your code.
- **Build Context**: The directory (or Git URL) passed to `docker build` (e.g., `.` for current dir). Includes all files unless ignored via `.dockerignore` (like `.gitignore` for bloat).

### 1.2 Why Use Dockerfiles? (Theoretical Benefits)
- **Reproducibility**: Eliminates "it works on my machine" by codifying environments. One build command yields identical images across teams/CI pipelines.
- **Portability**: Images run anywhere with Docker, abstracting OS differences (e.g., Alpine Linux base for small size).
- **Efficiency**: Layer caching reduces build times; multi-stage builds discard build-time artifacts.
- **Security/Compliance**: Explicit instructions allow scanning (e.g., for vulnerabilities) and minimalism (no unnecessary tools).
- **DevOps Integration**: Fits into CI/CD: `git push` → build → push to registry → deploy.

| Aspect              | Without Dockerfile                  | With Dockerfile                     |
|---------------------|-------------------------------------|-------------------------------------|
| **Build Process**   | Manual scripts/setup                | Automated, versioned                |
| **Size/Overhead**   | Variable, bloated                   | Optimized layers, slim images       |
| **Debugging**       | Environment-specific                | Inspect layers (`docker history`)   |
| **Collaboration**   | Share VMs/scripts                   | Share images via registries         |

### 1.3 Build Lifecycle
1. **Parse**: Docker reads instructions top-to-bottom.
2. **Execute**: Each creates a layer (e.g., `FROM` sets base).
3. **Cache**: Skip if context unchanged.
4. **Tag/Output**: Final image tagged (e.g., `myapp:v1`).
- **Command**: `docker build -t myapp:v1 .` triggers this.

## 2. Syntax and Common Instructions

Dockerfiles use uppercase instructions, followed by arguments. Comments start with `#`. Indentation isn't required, but readability matters.

### 2.1 Essential Instructions
| Instruction | Purpose | Example | Theory Notes |
|-------------|---------|---------|--------------|
| **FROM**    | Base image (required first). | `FROM openjdk:17-jre-slim` | Starts the layer stack. Use official/minimal bases (e.g., `-alpine` for <50MB). Multi-stage: Multiple `FROM`s. |
| **LABEL**   | Metadata (key=value). | `LABEL maintainer="dev@example.com"` | Non-executing; for docs/search. |
| **RUN**     | Execute shell commands. | `RUN apt update && apt install -y curl` | Creates layers; chain with `&&` to minimize. Use `/bin/sh -c` for non-interactive. |
| **COPY**    | Copy files from context to image. | `COPY . /app` | Efficient for local files; preserves metadata. Prefer over `ADD` unless URLs/tars needed. |
| **ADD**     | Like COPY, but auto-extracts tars/URLs. | `ADD https://example.com/script.sh /app/` | Avoid for local copies; use for remote artifacts. |
| **WORKDIR** | Set working dir (creates if missing). | `WORKDIR /app` | Relative paths in later instructions; reduces `COPY` verbosity. |
| **ENV**     | Set environment variables. | `ENV JAVA_OPTS="-Xmx512m"` | Persistent in layers; runtime-injected. |
| **EXPOSE**  | Document ports (not binding). | `EXPOSE 8080` | Metadata for `docker run -p`; no auto-port mapping. |
| **VOLUME**  | Declare mount points for persistence. | `VOLUME /data` | Signals external storage; auto-creates host volumes. |
| **CMD**     | Default command (JSON array or shell). | `CMD ["java", "-jar", "app.jar"]` | Entrypoint override; last one wins. Use exec form (`[]`) to avoid shell overhead. |
| **ENTRYPOINT** | Immutable entrypoint (with optional CMD args). | `ENTRYPOINT ["java", "-jar"]` <br> `CMD ["app.jar"]` | Defines executable; combines with CMD for flexibility. |

- **Arg vs. Env**: `ARG` for build-time (e.g., `ARG VERSION=1.0`); `ENV` for runtime.
- **Shell Forms**: `RUN apt install curl` (uses `/bin/sh`) vs. exec: `RUN ["/bin/bash", "-c", "apt install curl"]` (avoids shell pitfalls).

### 2.2 Execution Order
Instructions run in sequence; order matters for caching:
- Put unchanging layers first (e.g., `FROM`, `RUN apt update`).
- Volatile layers last (e.g., `COPY . /app` for code changes).

## 3. Best Practices

- **Minimize Layers**: Combine `RUN` commands: `RUN apt update && apt install -y pkg1 pkg2 && rm -rf /var/lib/apt/lists/*` (cleans up in one layer).
- **Use .dockerignore**: Exclude `node_modules`, `.git`, tests to shrink context.
- **Multi-Stage Builds**: Build in one stage, copy artifacts to a slim runtime stage (reduces size by 50-90%).
- **Non-Root Users**: `RUN useradd -m appuser && chown -R appuser /app` then `USER appuser` (security).
- **Healthchecks**: `HEALTHCHECK CMD curl -f http://localhost || exit 1` for monitoring.
- **Scan Images**: Post-build, use `docker scout cves myimage` (Docker's vuln scanner).
- **Size Targets**: Aim <500MB; use distroless bases (e.g., `gcr.io/distroless/java17`—no shell, ultra-minimal).

## 4. Example: Dockerfile for a Spring Boot Application

Spring Boot apps typically produce a fat JAR via Maven/Gradle. We'll build a multi-stage Dockerfile: Stage 1 builds the JAR; Stage 2 runs it in a JRE-only image.

### 4.1 Assumptions
- Your project: Maven-based Spring Boot app in `./spring-app/` with `pom.xml`.
- Output: Executable `target/myapp.jar`.
- Ports: 8080 (default Spring).

### 4.2 Dockerfile
Create `Dockerfile` in project root:
```dockerfile
# syntax=docker/dockerfile:1  # Enables BuildKit features (optional, for 2025+)

# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
# Download deps first for caching
RUN mvn dependency:go-offline -B
COPY src ./src
# Build JAR (skip tests for speed; add -DskipTests=false in prod)
RUN mvn clean package -DskipTests

# Stage 2: Runtime image (slim, no build tools)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy only the JAR (discard build artifacts)
COPY --from=builder /app/target/*.jar app.jar
# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup && \
    chown -R appuser:appgroup /app
USER appuser

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]
```

### 4.3 Explanation
- **Multi-Stage**: Builder stage uses full Maven (heavy); runtime uses slim JRE (~100MB total image).
- **Caching**: `COPY pom.xml` + dep download first—rebuilds skip if deps unchanged.
- **Security**: Non-root user; healthcheck uses Spring Actuator (add dependency if needed).
- **Theory Tie-In**: Layers: Base (JRE) + JAR copy + user setup. `ENTRYPOINT/CMD` allows overrides (e.g., `docker run -e SPRING_PROFILES_ACTIVE=prod`).

### 4.4 Build and Run
```bash
$ cd spring-app
$ docker build -t spring-app:v1 .
# Output: ~150MB image

$ docker run -d -p 8080:8080 --name spring-container --env SPRING_PROFILES_ACTIVE=dev spring-app:v1
$ docker logs spring-container  # See startup
$ curl localhost:8080  # Test endpoint
$ docker stop spring-container && docker rm spring-container
```

- **Inspect Layers**: `docker history spring-app:v1` (shows layer sizes/commands).

## 5. Example: Dockerfile for a React Application

React apps build to static files via `npm run build`, served by Nginx (lightweight) or Node (if SSR). We'll use multi-stage: Build with Node, serve with Nginx.

### 5.1 Assumptions
- Your project: Create React App in `./react-app/` with `package.json`.
- Output: Static files in `build/`.
- Served on port 80.

### 5.2 Dockerfile
Create `Dockerfile` in project root:
```dockerfile
# Stage 1: Build the React app
FROM node:20-alpine AS builder
WORKDIR /app
# Copy package files first for caching
COPY package*.json ./
RUN npm ci --only=production=false  # Install all deps (dev included)
COPY . .
# Build optimized bundle
RUN npm run build

# Stage 2: Serve with Nginx (static files, no Node runtime)
FROM nginx:alpine
# Copy built assets to Nginx dir
COPY --from=builder /app/build /usr/share/nginx/html
# Copy custom Nginx config (optional; create nginx.conf for routing)
# COPY nginx.conf /etc/nginx/conf.d/default.conf
# Expose port
EXPOSE 80
# Nginx runs as non-root by default in Alpine
CMD ["nginx", "-g", "daemon off;"]
```

### 5.3 Explanation
- **Multi-Stage**: Builder (~1GB temp) → Nginx (~20MB final). Discards Node for tiny image.
- **Caching**: `COPY package*.json` + `npm ci` first—fast reinstalls.
- **Production Tweaks**: Use `--only=production` in final builds; add env for API URLs (e.g., `ARG REACT_APP_API_URL`).
- **Theory Tie-In**: Static serving leverages Nginx's efficiency; `CMD` uses exec form for signal handling.

### 5.4 Build and Run
```bash
$ cd react-app
$ docker build -t react-app:v1 .
# Output: ~25MB image

$ docker run -d -p 3000:80 --name react-container react-app:v1
$ open http://localhost:3000  # View app
$ docker stop react-container && docker rm react-container
```

- **Custom Config**: For SPA routing, add `nginx.conf` with `try_files $uri /index.html;` in location block.

## 6. Advanced Topics

### 6.1 Multi-Stage Builds (Deeper Dive)
As shown, use `--from=stage` to copy across stages. Benefits: Smaller images (e.g., Spring: 400MB → 150MB; React: 900MB → 25MB).
- **Debug**: `docker build --target builder -t debug .` (stop at stage).

### 6.2 Full-Stack: Spring Boot + React with Docker Compose
For a monorepo or combined deploy, use `docker-compose.yml`:
```yaml
version: '3.8'
services:
  backend:
    build: ./spring-app
    ports: ["8080:8080"]
    environment:
      - SPRING_PROFILES_ACTIVE=prod
  frontend:
    build: ./react-app
    ports: ["3000:80"]
    depends_on:
      - backend
```
Run: `docker compose up -d`. Theory: Compose orchestrates multi-container apps declaratively.

### 6.3 Build Args and Secrets
- `ARG BUILD_VERSION=1.0` in Dockerfile; pass with `--build-arg BUILD_VERSION=2.0`.
- Secrets: Use BuildKit (`DOCKER_BUILDKIT=1`) with `--secret id=mysecret,src=secret.txt`.

## 7. Troubleshooting and Next Steps

### 7.1 Common Issues
- **Large Builds**: Check `docker system df`; use `.dockerignore`.
- **Permission Errors**: Ensure `COPY` source exists; fix ownership in `RUN chown`.
- **Caching Misses**: `--no-cache` to force rebuild.
- **Layer Inspection**: `docker run -it myimage /bin/sh` (if shell present) or `docker image inspect`.
- **Vulns**: `docker scout cves myimage`—patch bases regularly.

### 7.2 Next Steps
- Experiment: Fork a GitHub repo (e.g., spring-petclinic) and Dockerize it.
- Advanced: Explore Distroless bases or Kaniko for remote builds.
- Resources: Official docs (docs.docker.com); practice with `docker buildx` for multi-platform.
