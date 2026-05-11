**GitHub Actions – Advanced Concepts Tutorial**  

### **1. Dependency Caching**

Dependency caching significantly reduces workflow execution time by storing downloaded packages (Maven, npm, Gradle, etc.) across runs.

#### **Why Caching Matters**
On a fresh runner, building a Spring Boot project or React app can take several minutes just to download dependencies. Caching restores these packages in seconds on subsequent runs.

#### **Practical Examples**

**Maven (Spring Boot)**

```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'maven'                    # Built-in Maven caching

- name: Build with Maven
  working-directory: backend
  run: ./mvnw clean package -DskipTests
```

**npm / Yarn (React)**

```yaml
- name: Set up Node.js
  uses: actions/setup-node@v4
  with:
    node-version: 20
    cache: 'npm'                      # Automatic npm cache
    cache-dependency-path: frontend/package-lock.json

- name: Install dependencies
  working-directory: frontend
  run: npm ci
```

**Manual Cache (Advanced Control)**

```yaml
- name: Cache Maven packages
  uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('backend/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-maven-
```

**Best Practice**: Combine built-in caching (`cache: 'maven'`) with manual cache for complex monorepos.

---

### **2. Committing Build Artifacts to Repository**

While GitHub Artifacts (temporary download links) are preferred for most build outputs, there are valid scenarios for committing artifacts directly to the repository — such as releasing versioned builds, documentation sites, or static assets.

#### **When to Commit Artifacts**
- Versioned release bundles
- Generated documentation
- Static website builds (sometimes)

#### **Implementation Example**

```yaml
name: Build and Commit Artifacts

on:
  push:
    branches: [ main ]

jobs:
  build-and-commit:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Build React App
        working-directory: frontend
        run: |
          npm ci
          npm run build

      - name: Build Spring Boot JAR
        working-directory: backend
        run: ./mvnw clean package -DskipTests

      - name: Commit and Push Artifacts
        run: |
          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
          
          mkdir -p releases/${{ github.sha }}
          cp backend/target/*.jar releases/${{ github.sha }}/
          cp -r frontend/build releases/${{ github.sha }}/frontend/
          
          git add releases/
          git commit -m "Add build artifacts for commit ${{ github.sha }}" || echo "No changes to commit"
          git push
```

**Important Considerations**:
- Avoid committing large binaries (use Git LFS if necessary)
- Use a dedicated `releases/` or `dist/` folder
- Consider using GitHub Releases + Assets instead for production distributions
- This approach increases repository size over time

---

### **3. Conditional Execution**

Conditional logic allows you to control when jobs or steps run.

#### **Common Condition Expressions**

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' && success()     # Only on main branch

    steps:
      - name: Always run step
        run: echo "This always runs"

      - name: Run only on success
        if: success()
        run: echo "Previous steps succeeded"

      - name: Run on failure
        if: failure()
        run: echo "Something went wrong"

      - name: Run only for tagged releases
        if: startsWith(github.ref, 'refs/tags/v')
        run: echo "This is a release"
```

**Complex Condition Example**:

```yaml
if: |
  github.event_name == 'push' && 
  contains(github.event.head_commit.message, '[deploy]') &&
  needs.build.result == 'success'
```

---

### **4. Targeting Different Environments**

GitHub Environments provide deployment safety, environment-specific secrets, and protection rules.

#### **Setting Up Environments**
Go to repository **Settings → Environments** → New environment (`development`, `staging`, `production`).

#### **Workflow Example with Environments**

```yaml
name: Deploy to Environments

on:
  workflow_dispatch:
    inputs:
      environment:
        type: environment
        required: true

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: ${{ inputs.environment }}     # Applies protection rules & secrets

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Login to Azure
        uses: azure/login@v2
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}   # Environment-specific secret

      - name: Deploy to Azure App Service
        uses: azure/webapps-deploy@v3
        with:
          app-name: fullstack-${{ inputs.environment }}
          package: backend/target/*.jar
```

**Environment Protection Rules** (highly recommended):
- Require approval from specific team members
- Limit deployments to certain branches
- Set wait timers (e.g., 30 minutes for production)

---

### **5. Reusing Workflows**

Reusable workflows allow you to define common logic once and call it from multiple repositories or workflows.

#### **Creating a Reusable Workflow**

File: `.github/workflows/reusable-build.yml`

```yaml
name: Reusable Build Workflow

on:
  workflow_call:
    inputs:
      java_version:
        required: false
        type: string
        default: '17'
      working_directory:
        required: true
        type: string
    secrets:
      sonar_token:
        required: false

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ inputs.java_version }}
          distribution: 'temurin'
          cache: 'maven'

      - name: Build Project
        working-directory: ${{ inputs.working_directory }}
        run: ./mvnw clean package
```

#### **Calling the Reusable Workflow**

```yaml
name: Main CI Pipeline

on: [push, pull_request]

jobs:
  backend-build:
    uses: ./.github/workflows/reusable-build.yml
    with:
      working_directory: backend
      java_version: '17'
    secrets:
      sonar_token: ${{ secrets.SONAR_TOKEN }}

  frontend-build:
    uses: ./.github/workflows/reusable-build-frontend.yml   # Another reusable workflow
    with:
      working_directory: frontend
```

**Benefits**:
- Single source of truth for build logic
- Easier maintenance across repositories
- Supports inputs, outputs, and secrets

---

### **Best Practices for Advanced GitHub Actions**

- Always use `concurrency` to prevent overlapping runs on the same branch.
- Leverage caching aggressively for faster pipelines.
- Prefer GitHub Environments over manual conditionals for production deployments.
- Keep reusable workflows modular and well-documented.
- Monitor workflow usage and billing in repository settings.
- Use `actions/cache@v4` for custom caching needs.
