**GitHub Actions (GHA) – Comprehensive Tutorial**  

---

### **Understanding Workflow Orchestration**

GitHub Actions allows you to define automated processes that respond to events in your repository. A **workflow** is the complete automated process, written in YAML format and stored in the `.github/workflows/` directory of your repository. Each workflow can contain one or more jobs that execute on virtual machines called **runners**.

The power of GitHub Actions lies in its ability to create reliable, repeatable pipelines that handle building, testing, analyzing, and deploying code with minimal manual intervention. This ensures consistent quality and faster delivery cycles for applications, such as a full-stack setup with React frontend, Spring Boot backend, and a database.

---

### **Sequential Multi-Job Workflows**

Most real-world pipelines require steps to happen in a specific order. For example, you should only run tests after a successful build, and only deploy after tests and code analysis pass.

#### **How Job Dependencies Work**
Use the `needs` keyword to define dependencies between jobs. This creates a directed acyclic graph where downstream jobs wait for upstream jobs to complete successfully.

Here is a detailed example:

```yaml
name: Full CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    name: Build Application
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build Spring Boot project
        working-directory: backend
        run: ./mvnw clean package -DskipTests
```

**Explanation**:  
This `build` job checks out the code, sets up the Java environment with caching for faster runs, and compiles the Spring Boot application without running tests to keep the build stage fast.

```yaml
  unit-test:
    name: Run Unit Tests
    needs: build                    # This job executes only after 'build' succeeds
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Execute unit tests
        working-directory: backend
        run: ./mvnw test
```

**Explanation**:  
The `needs: build` line ensures this job does not start until the build job completes successfully. If the build fails, this job is automatically skipped. This prevents wasting resources on testing broken code.

You can make a job depend on multiple previous jobs:

```yaml
  deploy:
    name: Deploy to Azure
    needs: [unit-test, sonar-analysis]   # Waits for both jobs
    if: github.ref == 'refs/heads/main'  # Conditional execution
    runs-on: ubuntu-latest
    steps:
      # Deployment steps here
```

This structure creates a clear, professional pipeline flow: Build → Test → Analyze → Deploy.

---

### **Repository Events and Advanced Triggers**

Workflows are activated by **events**. Choosing the right triggers is essential for efficient automation.

#### **Common Event Types**

- **`push`**: Triggers when code is pushed to specified branches.
- **`pull_request`**: Triggers on pull request activity, useful for validating changes before merging.
- **`workflow_dispatch`**: Enables manual triggering from the GitHub interface with optional input parameters.
- **`schedule`**: Runs workflows on a cron schedule for periodic tasks like nightly builds.
- Other events include `release`, `issues`, `workflow_run`, etc.

**Detailed Trigger Example**:

```yaml
on:
  push:
    branches: 
      - main
      - develop
    paths:                          # Trigger only when relevant files change
      - 'backend/**'
      - 'frontend/**'
      - 'pom.xml'
      - 'package.json'

  pull_request:
    branches: [ main ]
    types: [opened, synchronize, reopened, ready_for_review]

  workflow_dispatch:
    inputs:
      deploy_environment:
        description: 'Target environment'
        required: true
        type: choice
        options:
        - dev
        - staging
        - production
```

**Explanation**:  
The `paths` filter prevents unnecessary workflow runs when only documentation or unrelated files are changed. The `workflow_dispatch` input allows teams to manually choose the deployment target when needed.

---

### **Unit Testing in Workflows**

Integrating tests ensures code quality at every stage.

#### **Spring Boot Unit Testing**

```yaml
- name: Run Spring Boot Tests
  working-directory: backend
  run: ./mvnw test --no-transfer-progress

- name: Publish Test Results
  if: always()                          # Runs even if previous steps fail
  uses: mikepenz/action-junit-report@v4
  with:
    report_paths: 'backend/target/surefire-reports/TEST-*.xml'
    check_name: 'Unit Test Report'
```

#### **React Frontend Testing**

```yaml
- name: Install Node dependencies
  working-directory: frontend
  run: npm ci

- name: Run React tests with coverage
  working-directory: frontend
  run: npm test -- --ci --coverage

- name: Upload coverage report
  uses: actions/upload-artifact@v4
  with:
    name: frontend-coverage
    path: frontend/coverage/
```

**Explanation**:  
The `if: always()` ensures reports are generated for review even when tests fail. Artifacts allow developers to download detailed reports directly from the workflow run.

---

### **Working with Build Artifacts**

Artifacts are files produced during a workflow that can be downloaded or passed between jobs.

```yaml
- name: Upload Spring Boot JAR artifact
  uses: actions/upload-artifact@v4
  with:
    name: backend-jar
    path: backend/target/*.jar
    retention-days: 7                     # How long to keep the artifact

- name: Upload React production build
  uses: actions/upload-artifact@v4
  with:
    name: frontend-build
    path: frontend/build/
    retention-days: 30
```

**Explanation**:  
Artifacts are useful for debugging failed builds or manually deploying a specific version. You can download them from the Actions tab in your repository. In multi-job setups, use `actions/download-artifact@v4` to pass files between jobs.

---

### **Static Code Analysis with SonarQube**

SonarQube helps maintain code quality by detecting bugs, vulnerabilities, code smells, and technical debt.

#### **SonarQube Cloud Setup in Workflow**

```yaml
  sonar-analysis:
    name: SonarQube Code Analysis
    needs: unit-test
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0                    # Important for accurate history

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: SonarQube Scan
        uses: sonarsource/sonarqube-scan-action@v2
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: https://sonarcloud.io
        with:
          projectKey: your_project_key
```

**Explanation**:  
Store your SonarQube token as a repository secret. The analysis runs after tests pass, providing a quality gate that can block deployments if standards are not met. This step is critical for maintaining long-term code health in team projects.

---

### **Additional Professional Automations**

- **Concurrency Control** (prevents overlapping runs):
  ```yaml
  concurrency:
    group: ${{ github.workflow }}-${{ github.ref }}
    cancel-in-progress: true
  ```

- **Conditional Execution and Failure Notifications**:
  Use `if: failure()` to send alerts only when something goes wrong.

---

### **Complete Professional Workflow Example**

The following combines all concepts into one cohesive pipeline suitable for a React + Spring Boot application:

```yaml
name: FullStack CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    # Build job definition...

  unit-test:
    needs: build
    # Test job definition...

  sonar-analysis:
    needs: unit-test
    # SonarQube job definition...

  deploy:
    needs: [sonar-analysis]
    if: github.ref == 'refs/heads/main'
    # Deployment steps to Azure App Service, etc.
```
