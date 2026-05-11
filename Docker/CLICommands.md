# Docker CLI Commands

## 1. Prerequisites and Setup

### 1.1 Verify Installation
Before diving in, confirm Docker is running.

- **Command**: `docker version`
    - **Explanation**: Displays the Docker CLI and Engine versions, plus Go runtime info. Ensures client-server communication.
    - **Example**:
      ```
      $ docker version
      Client:
       Version:           27.1.1
       API version:       1.46
       Go version:        go1.22.5
       Git commit:        6312585
       Built:             Thu Dec 12 2025 10:00:00 UTC  # Hypothetical future build
       OS/Arch:           darwin/arm64
      Server:
       Engine:
        Version:          27.1.1
       ...
      ```
    - **Common Flags**: None typically needed.
    - **Troubleshoot**: If "Cannot connect to the Docker daemon," start Docker (e.g., `sudo systemctl start docker` on Linux).

- **Command**: `docker info`
    - **Explanation**: Shows detailed Engine info: nodes, plugins, storage drivers (e.g., overlay2), and resource usage. Useful for diagnostics.
    - **Example Output Snippet**:
      ```
      Containers: 2
      Running: 1
      Paused: 0
      Stopped: 1
      Images: 5
      ...
      Storage Driver: overlay2
      ```
    - **Flags**: `--format '{{.ServerVersion}}'` for JSON-like output.

### 1.2 Run as Non-Root (Optional but Recommended)
Add your user to the `docker` group: `sudo usermod -aG docker $USER`, then log out/in. Avoid `sudo` for every command.

## 2. Basic Commands

Start with essentials: pulling/running images and listing resources.

### 2.1 Pull an Image
- **Command**: `docker pull <image>[:<tag>]`
    - **Explanation**: Downloads an image from a registry (default: Docker Hub) to your local store. Layers are fetched incrementally if partially present. Ties to theory: Prepares blueprints for instantiation.
    - **Example**:
      ```
      $ docker pull nginx:alpine
      alpine: Pulling from library/nginx
      Digest: sha256:abc123...
      Status: Downloaded newer image for nginx:alpine
      ```
    - **Flags**:
        - `-q`: Quiet mode (no progress bars).
        - `--platform linux/arm64`: For multi-arch pulls.
    - **Use Case**: Always pull before running in production for latest security patches.

### 2.2 Run a Container
- **Command**: `docker run [OPTIONS] <image>[:<tag>] [COMMAND] [ARG...]`
    - **Explanation**: Creates and starts a container from an image. If the image isn't local, it pulls first. The container runs the image's entrypoint (default command) and exits when done (unless detached). Core to runtime lifecycle.
    - **Example** (Simple echo):
      ```
      $ docker run hello-world
      Hello from Docker!
      This message shows that your installation appears to be working correctly.
      ...
      ```
    - **Example** (Interactive web server):
      ```
      $ docker run -d -p 8080:80 --name my-nginx nginx:alpine
      # -d: Detached mode (background)
      # -p: Port mapping (host:container)
      # --name: Assign a name
      ```
      Access at `http://localhost:8080`.
    - **Key Flags**:
      | Flag       | Description                          | Example Usage                  |
      |------------|--------------------------------------|--------------------------------|
      | `-d`       | Run in detached mode                 | `docker run -d ...`            |
      | `-it`      | Interactive mode with TTY            | `docker run -it ubuntu bash`   |
      | `-p`       | Publish ports (host:container)       | `-p 80:80`                     |
      | `--rm`     | Auto-remove container on exit        | `docker run --rm ...`          |
      | `-e`       | Set environment variables            | `-e MY_VAR=value`              |
      | `--name`   | Name the container                   | `--name myapp`                 |
    - **Theory Tie-In**: `--rm` enforces ephemerality; `-p` leverages bridge networking.

### 2.3 List Images and Containers
- **Command**: `docker images` (or `docker image ls`)
    - **Explanation**: Lists local images with repo, tag, ID, creation date, and size.
    - **Example**:
      ```
      $ docker images
      REPOSITORY   TAG       IMAGE ID       CREATED        SIZE
      nginx        alpine    123abc456def   2 weeks ago    23MB
      hello-world  latest    fedcba987654   1 month ago    13kB
      ```
    - **Flags**: `-q`: IDs only; `-a`: All (including intermediates).

- **Command**: `docker ps` (or `docker container ls`)
    - **Explanation**: Lists running containers (like `ps` in Unix). Shows ID, image, status, ports.
    - **Example**:
      ```
      $ docker ps
      CONTAINER ID   IMAGE          COMMAND                  CREATED         STATUS         PORTS                 NAMES
      abc123def456   nginx:alpine   "/docker-entrypoint.…"   5 minutes ago   Up 5 minutes   0.0.0.0:8080->80/tcp  my-nginx
      ```
    - **Flags**: `-a`: All containers (running + stopped); `-q`: IDs only; `--format`: Custom output.

## 3. Image Management

Focus on building, tagging, and distributing images.

### 3.1 Build an Image
- **Command**: `docker build [OPTIONS] PATH | URL | -`
    - **Explanation**: Creates a new image from a Dockerfile in the specified context (current dir by default). Layers are built sequentially; cache is used for unchanged instructions.
    - **Example** (Assuming a simple `Dockerfile` with `FROM alpine` and `RUN echo "Hello"`):
      ```
      $ docker build -t myapp:v1 .
      Sending build context to Docker daemon  2.048kB
      Step 1/2 : FROM alpine
      ...
      Successfully tagged myapp:v1
      ```
    - **Flags**:
      | Flag      | Description                          | Example                     |
      |-----------|--------------------------------------|-----------------------------|
      | `-t`      | Tag the image (name:tag)             | `-t myapp:v1`               |
      | `-f`      | Specify Dockerfile path              | `-f Dockerfile.dev`         |
      | `--no-cache` | Ignore build cache                | For fresh builds            |
      | `--build-arg` | Pass build-time variables        | `--build-arg VERSION=1.0`   |
    - **Theory Tie-In**: Builds enforce immutability—rerun for reproducibility.

### 3.2 Tag and Push Images
- **Command**: `docker tag <source_image> <target_image>`
    - **Explanation**: Creates a new tag for an existing image (no new layers). Essential for versioning.
    - **Example**:
      ```
      $ docker tag myapp:v1 myregistry.com/myapp:v1
      $ docker images  # Now shows both tags
      ```

- **Command**: `docker push <image>[:<tag>]`
    - **Explanation**: Uploads image layers to a registry. Only changed layers are sent.
    - **Example**:
      ```
      $ docker push myregistry.com/myapp:v1
      The push refers to repository [myregistry.com/myapp]
      ...
      v1: digest: sha256:def456... size: 1.2kB
      ```
    - **Flags**: `--all-tags`: Push all tags.
    - **Auth**: Login first with `docker login` (e.g., `docker login myregistry.com`).

- **Command**: `docker rmi <image>[:<tag>]`
    - **Explanation**: Removes images. Fails if in use by containers.
    - **Example**: `docker rmi myapp:v1`
    - **Flags**: `-f`: Force removal.

## 4. Container Management

Interact with running/stopped containers.

### 4.1 Inspect and Logs
- **Command**: `docker logs <container>`
    - **Explanation**: Streams stdout/stderr from the container. Crucial for debugging.
    - **Example**:
      ```
      $ docker logs my-nginx
      /docker-entrypoint.sh: nginx -g 'daemon off;'
      172.17.0.1 - - [12/Dec/2025:10:00:00 +0000] "GET / HTTP/1.1" 200 ...
      ```
    - **Flags**: `-f`: Follow (tail -f); `--tail 10`: Last N lines; `-t`: Timestamps.

- **Command**: `docker inspect <container> | <image>`
    - **Explanation**: JSON output of full config (e.g., env vars, mounts, network).
    - **Example Snippet**:
      ```
      $ docker inspect my-nginx | jq '.[] | {Name, NetworkSettings}'
      {
        "Name": "/my-nginx",
        "NetworkSettings": {
          "Ports": {
            "80/tcp": [{"HostIp": "0.0.0.0", "HostPort": "8080"}]
          }
        }
      }
      ```
    - **Flags**: `--format '{{.Config.Entrypoint}}'`: Extract specific fields.

### 4.2 Exec into a Container
- **Command**: `docker exec [OPTIONS] <container> <command>`
    - **Explanation**: Runs a command inside a running container (e.g., shell access). Leverages namespaces for isolation.
    - **Example**:
      ```
      $ docker exec -it my-nginx sh
      / # ls /etc/nginx
      conf.d  fastcgi.conf  ...
      / # exit
      ```
    - **Flags**: `-it`: Interactive TTY; `-u user`: Run as specific user.

### 4.3 Stop, Start, Restart, Remove
- **Command**: `docker stop <container>`
    - **Explanation**: Gracefully stops (sends SIGTERM, then SIGKILL after timeout).
    - **Example**: `docker stop my-nginx`

- **Command**: `docker start <container>`
    - **Explanation**: Restarts a stopped container with original config.
    - **Example**: `docker start my-nginx`

- **Command**: `docker restart <container>`
    - **Explanation**: Stops then starts.
    - **Example**: `docker restart my-nginx`

- **Command**: `docker rm <container>`
    - **Explanation**: Removes stopped containers.
    - **Example**: `docker rm my-nginx`
    - **Flags**: `-f`: Force (stops if running); `-v`: Remove volumes too.

## 5. Networking and Storage

### 5.1 Networking
- **Command**: `docker network ls`
    - **Explanation**: Lists networks (bridge, host, none, overlay).
    - **Example**:
      ```
      $ docker network ls
      NETWORK ID     NAME      DRIVER    SCOPE
      123abc456def   bridge    bridge    local
      ...
      ```

- **Command**: `docker network create <name>`
    - **Example**: `docker network create mynet`
    - **Run with Network**: `docker run --network mynet ...`

- **Inspect**: `docker network inspect <name>` (shows attached containers).

### 5.2 Volumes
- **Command**: `docker volume create <name>`
    - **Explanation**: Creates a persistent volume.
    - **Example**: `docker volume create mydata`

- **Run with Volume**: `docker run -v mydata:/app ...` (host vol to container path).
- **List/Remove**: `docker volume ls` / `docker volume rm <name>`.
- **Bind Mount**: `docker run -v /host/path:/container/path ...` (less portable).

## 6. Advanced Commands

### 6.1 System Prune
- **Command**: `docker system prune [OPTIONS]`
    - **Explanation**: Removes unused resources (dangling images, stopped containers, unused networks/volumes). Frees space.
    - **Example**: `docker system prune -a -f` (`-a`: All unused; `-f`: Force no prompt).
    - **Warning**: Destructive—review with `--dry-run` equivalent via flags.

### 6.2 Export/Import (Legacy, but Useful)
- **Command**: `docker export <container> > file.tar`
    - **Explanation**: Exports container FS as tar (for backups).
- **Import**: `docker import file.tar <image>`.

### 6.3 Stats
- **Command**: `docker stats [OPTIONS] [CONTAINER...]`
    - **Explanation**: Real-time resource usage (CPU, mem, net I/O).
    - **Example**: `docker stats my-nginx` (streams until Ctrl+C).

## 7. Troubleshooting and Best Practices

### 7.1 Common Issues
- **"No space left"**: Run `docker system df` to check usage; prune as needed.
- **Port Conflicts**: Use `docker ps` to check bindings; change `-p`.
- **Permission Denied**: Ensure non-root access; check SELinux/AppArmor.
- **Debug Builds**: Add `--progress=plain` to `build` for verbose output.

### 7.2 Best Practices
- **Use Tags Over `latest`**: `docker run myapp:1.0` for reproducibility.
- **Combine Flags**: `docker run -d --rm -p 80:80 --name app -v data:/app myimage`.
- **Scripting**: Pipe to `jq` for JSON (e.g., `docker inspect | jq`); use `--format` for tables.
- **Security**: Scan with `docker scout` (newer CLI integration); run as non-root (`--user`).
- **Aliases**: Add to `~/.bashrc`: `alias d='docker'`; `alias dps='docker ps -a'`.
- **Compose for Multi-Container**: Intro to `docker compose up -d` (YAML-driven; separate tutorial if needed).
- **Performance**: Limit resources with `--cpus=1 --memory=512m`.
