**Azure Infrastructure as Code (IaC) Tutorial**  

---

### **Architecture Overview**

This tutorial deploys a modern full-stack application:

- **Frontend**: React (Single Page Application) → Hosted on **Azure Static Web Apps** (best for React/SPAs with built-in CI/CD, custom domains, and free tier).
- **Backend**: Spring Boot (Java) → Hosted on **Azure App Service** (Web App for Containers or direct JAR deployment).
- **Database**: **Azure Database for MySQL - Flexible Server** (fully managed, scalable MySQL).
- **Additional Resources**: Resource Group, App Service Plan, networking basics, and secure connection strings.

**High-Level Flow**: React frontend calls Spring Boot APIs → Spring Boot connects to MySQL.

**Why Bicep?**  
Bicep is a declarative, human-readable language that compiles to ARM templates. It is cleaner than raw JSON, supports modules, and is the Microsoft-recommended IaC tool for Azure.

---

### **Prerequisites**

1. **Azure Account** — Free tier is sufficient to start.
2. **Tools**:
   - Azure CLI (installed and logged in: `az login`)
   - Bicep CLI (comes with Azure CLI; update with `az bicep upgrade`)
   - Git & GitHub account (for deployments)
   - Java 17+ & Maven (for Spring Boot)
   - Node.js (for React)
3. **Code Structure** (Recommended):
   ```
   my-fullstack-app/
   ├── frontend/          # React app
   ├── backend/           # Spring Boot app
   ├── infra/             # Bicep files
   │   ├── main.bicep
   │   ├── parameters.bicepparam (optional)
   │   └── modules/
   └── README.md
   ```

---

### **Step 1: Prepare Your Applications**

#### **Backend (Spring Boot)**
- Use Spring Boot 3.x with Spring Data JPA and MySQL driver.
- In `application.properties` (use profiles for Azure):
  ```properties
  spring.datasource.url=jdbc:mysql://${DB_HOST}:3306/${DB_NAME}?useSSL=true&requireSSL=true
  spring.datasource.username=${DB_USER}
  spring.datasource.password=${DB_PASSWORD}
  spring.jpa.hibernate.ddl-auto=update
  ```
- Build a JAR: `mvn clean package`

#### **Frontend (React)**
- Create a standard Create React App or Vite project.
- Use environment variables or Axios to call backend API (e.g., `https://your-backend.azurewebsites.net/api/...`).
- Build with `npm run build`.

---

### **Step 2: Infrastructure with Bicep**

Create the `infra` folder and these files.

#### **main.bicep** (Core File)

```bicep
@description('Resource Group Location')
param location string = resourceGroup().location

@description('Application Name Prefix')
param appNamePrefix string = 'fullstack'

@description('MySQL Admin Username')
param mysqlAdminLogin string

@secure()
@description('MySQL Admin Password')
param mysqlAdminPassword string

// Resource Group is created outside or referenced

// 1. App Service Plan (for Backend)
resource appServicePlan 'Microsoft.Web/serverfarms@2024-04-01' = {
  name: '${appNamePrefix}-plan'
  location: location
  sku: {
    name: 'B1'  // Start with Basic; scale to P1v3 later
    tier: 'Basic'
  }
  properties: {
    reserved: true  // For Linux
  }
  kind: 'linux'
}

// 2. Azure App Service Web App for Spring Boot
resource backendApp 'Microsoft.Web/sites@2024-04-01' = {
  name: '${appNamePrefix}-backend'
  location: location
  kind: 'app'
  properties: {
    serverFarmId: appServicePlan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'JAVA|17-java17'  // Or use container
      alwaysOn: true
      appSettings: [
        { name: 'SPRING_PROFILES_ACTIVE', value: 'azure' }
        // Connection string will be added via outputs or Key Vault
      ]
    }
  }
}

// 3. Azure Database for MySQL Flexible Server
resource mysqlServer 'Microsoft.DBforMySQL/flexibleServers@2024-01-01' = {
  name: '${appNamePrefix}-mysql'
  location: location
  sku: {
    name: 'Standard_B1ms'  // Burstable for cost; change for production
    tier: 'Burstable'
  }
  properties: {
    administratorLogin: mysqlAdminLogin
    administratorLoginPassword: mysqlAdminPassword
    version: '8.0.32'
    storage: {
      storageSizeGB: 32
    }
    highAvailability: {
      mode: 'Disabled'  // Enable ZoneRedundant for prod
    }
  }
}

resource mysqlDatabase 'Microsoft.DBforMySQL/flexibleServers/databases@2024-01-01' = {
  parent: mysqlServer
  name: 'appdb'
}

// Firewall rule example (for initial public access - restrict in prod)
resource firewallRule 'Microsoft.DBforMySQL/flexibleServers/firewallRules@2024-01-01' = {
  parent: mysqlServer
  name: 'AllowAllAzureIps'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

// 4. Azure Static Web App for React Frontend
resource staticWebApp 'Microsoft.Web/staticSites@2023-12-01' = {
  name: '${appNamePrefix}-frontend'
  location: location
  sku: {
    name: 'Free'
  }
  properties: {
    // Build settings can be configured in GitHub Actions
  }
}

// Outputs
output backendUrl string = backendApp.properties.hostNames[0]
output frontendUrl string = staticWebApp.properties.defaultHostname
output mysqlHost string = mysqlServer.properties.fullyQualifiedDomainName
```

---

### **Step 3: Deploy the Infrastructure**

1. Create a Resource Group:
   ```bash
   az group create --name rg-fullstack-dev --location eastus
   ```

2. Deploy Bicep:
   ```bash
   az deployment group create \
     --resource-group rg-fullstack-dev \
     --template-file infra/main.bicep \
     --parameters mysqlAdminLogin=adminuser \
     --parameters mysqlAdminPassword=StrongPassword123!
   ```

Use a `parameters.bicepparam` file for better management in production.

---

### **Step 4: Deploy Application Code**

#### **Backend Deployment Options**
- Use Azure CLI / Maven Plugin.
- Or enable GitHub Actions / Zip Deploy.
- Set Application Settings in App Service for DB connection (use Managed Identity for production security).

#### **Frontend (Static Web Apps)**
- Push code to GitHub.
- In Azure Portal or via Bicep outputs, link your repo.
- Azure automatically builds and deploys on push (React build command: `npm run build`, output folder: `build`).

---

### **Step 5: Connect Everything**

1. Update Spring Boot connection string using App Settings in Azure.
2. Allow the App Service outbound IP in MySQL firewall (or use Private Endpoint + VNet for production).
3. Use CORS configuration in Spring Boot for React frontend.
4. (Recommended) Store secrets in **Azure Key Vault** and reference them.

---

### **Best Practices & Production Enhancements**

- **Security**: Use Private Endpoints, Microsoft Entra ID authentication, Managed Identities.
- **Scaling**: Auto-scale App Service Plan; use Premium tier for MySQL.
- **CI/CD**: GitHub Actions workflows for backend + Static Web Apps built-in CI/CD.
- **Monitoring**: Application Insights + Azure Monitor.
- **Modules**: Split Bicep into reusable modules (networking.bicep, database.bicep, etc.).
- **Cost Control**: Start with Burstable SKU and Free tiers.

**Next-Level Tools**:
- Azure Developer CLI (`azd`) for full-stack templates.
- Terraform (alternative to Bicep).

---
