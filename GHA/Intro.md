**GitHub Actions (GHA) Tutorial**  

---

### **What is GitHub Actions?**

**GitHub Actions** is GitHub’s built-in **Continuous Integration and Continuous Deployment (CI/CD)** platform. It allows you to automate workflows directly inside your GitHub repository.

You can use it to:
- Automatically build and test code on every push or pull request
- Deploy applications to Azure, AWS, Vercel, etc.
- Run code quality checks, security scans, and notifications
- Automate releases, issue labeling, and more

**Key Advantages**:
- Free for public repositories and generous limits for private repos
- Runs on GitHub-hosted runners (Linux, Windows, macOS) or self-hosted runners
- Native integration with GitHub (Pull Requests, Issues, Secrets, etc.)

---

### **Core Concepts**

#### **1. Workflow**
A workflow is a configurable automated process.  
- Defined as a **YAML file** (`.yml` or `.yaml`)
- Stored in the repository at `.github/workflows/`
- One repository can have multiple workflows

#### **2. Event (Trigger)**
An event is something that starts a workflow. Common triggers include:
- `push` — when code is pushed to a branch
- `pull_request` — when a PR is opened, synchronized, or reopened
- `workflow_dispatch` — manual trigger from GitHub UI
- `schedule` — cron-based (e.g., nightly build)
- `release`, `issues`, `discussion`, etc.

#### **3. Job**
A job is a set of steps that run on the **same runner** (virtual machine).
- Jobs run **in parallel** by default
- You can make jobs run **sequentially** using `needs`
- Each job runs in its own fresh environment

#### **4. Step**
The smallest unit inside a job.
- Can be a shell command (`run`)
- Or an **Action** (reusable piece of code from GitHub Marketplace)

#### **5. Action**
A reusable, packaged piece of code.  
Examples:
- `actions/checkout@v4` — clones your repository
- `actions/setup-java@v4` — sets up Java environment
- `azure/login@v2` — logs into Azure

---

### **Workflow YAML Structure**

```yaml
name: CI/CD Pipeline          # Name of the workflow

on:                           # Triggers (Events)
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:          # Allows manual trigger

jobs:                         # Define jobs
  build:                      # Job name
    name: Build and Test
    runs-on: ubuntu-latest    # Runner (vm)

    steps:                    # Steps inside the job
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
```

---

### **Detailed Explanation of Triggers (Events)**

```yaml
on:
  push:
    branches: [ main, develop ]
    paths:                    # Trigger only if specific files change
      - 'backend/**'
      - 'pom.xml'

  pull_request:
    types: [opened, synchronize, reopened]

  schedule:
    - cron: '0 2 * * *'       # Every day at 2:00 AM UTC

  workflow_dispatch:
    inputs:
      environment:
        description: 'Environment to deploy'
        required: true
        default: 'staging'
```

**Best Practice**: Use `pull_request` + `push` for most projects.

---

### **Jobs – Parallel vs Sequential**

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Build Spring Boot
        run: ./mvnw clean package

  test:
    needs: build              # This job waits for 'build' to finish
    runs-on: ubuntu-latest
    steps:
      - name: Run tests
        run: ./mvnw test

  deploy:
    needs: [build, test]      # Waits for both
    if: github.ref == 'refs/heads/main'   # Conditional execution
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Azure
        run: echo "Deploying..."
```

---

### **Secrets Management (Very Important)**

**GitHub Secrets** securely store sensitive information (passwords, tokens, keys).

#### **Where to Add Secrets**
1. Go to your repository → **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**

Common secrets:
- `AZURE_CREDENTIALS` (for Azure login)
- `DB_PASSWORD`
- `DOCKER_PASSWORD`

#### **Using Secrets in Workflow**

```yaml
steps:
  - name: Login to Azure
    uses: azure/login@v2
    with:
      creds: ${{ secrets.AZURE_CREDENTIALS }}

  - name: Deploy to App Service
    uses: azure/webapps-deploy@v3
    with:
      app-name: fullstack-backend
      package: backend/target/*.jar

  - name: Use secret in command
    run: echo "Connecting to DB"
    env:
      DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
```

**Security Tips**:
- Never hardcode secrets in YAML
- Use `secrets.` context
- Repository secrets vs Organization secrets vs Environment secrets
- Use **Environment Protection Rules** for production deployments

---

### **Complete Example Workflow (React + Spring Boot + Azure)**

Create file: `.github/workflows/deploy.yml`

```yaml
name: Deploy Full-Stack App to Azure

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      # Backend - Spring Boot
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build Spring Boot with Maven
        working-directory: backend
        run: ./mvnw clean package -DskipTests

      # Frontend - React
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install & Build React
        working-directory: frontend
        run: |
          npm ci
          npm run build

      # Azure Deployment
      - name: Login to Azure
        uses: azure/login@v2
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}

      - name: Deploy Backend to App Service
        uses: azure/webapps-deploy@v3
        with:
          app-name: fullstack-backend
          package: backend/target/*.jar

      - name: Deploy Frontend to Static Web Apps
        uses: azure/static-web-apps-deploy@v1
        with:
          azure_static_web_apps_api_token: ${{ secrets.AZURE_STATIC_WEB_APPS_TOKEN }}
          repo_token: ${{ secrets.GITHUB_TOKEN }}
          action: "upload"
          app_location: "frontend"
          output_location: "build"
```

---

### **Useful Contexts & Expressions**

- `${{ github.sha }}` — Commit hash
- `${{ github.ref }}` — Branch or tag reference
- `${{ github.event_name }}` — Type of event
- `${{ secrets.SECRET_NAME }}`
- `${{ vars.VARIABLE_NAME }}` — For non-sensitive variables

**Conditional Example**:
```yaml
if: success() && github.ref == 'refs/heads/main'
```

---

### **Best Practices for Beginners**

1. Start simple — one workflow for CI (build + test)
2. Use official actions from GitHub Marketplace
3. Keep workflows in `.github/workflows/`
4. Name jobs and steps clearly
5. Use caching (`cache: 'maven'`, `cache: 'npm'`) to speed up runs
6. Always test workflows on a branch first
7. Monitor usage in **Actions** tab → **Usage**
8. Enable **Required Status Checks** in branch protection

---

