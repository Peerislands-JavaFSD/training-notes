**Azure CLI Tutorial: Managing App Service, Azure SQL & Storage**  
---

### **Introduction**

This guide shows you how to provision and manage core Azure resources using **Azure CLI** for a full-stack application:

- **React** → Frontend (Static Web Apps or App Service)
- **Spring Boot** → Backend (Java JAR on App Service)
- **Azure SQL Database** → Database (MySQL alternative is also noted)
- **Storage Account** → For file uploads, static assets, or backups

---

### **Prerequisites**

1. Install [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli).
2. Login: `az login`
3. Set subscription: `az account set --subscription "Your Subscription Name/ID"`
4. Have your applications ready:
   - Spring Boot JAR (`target/*.jar`)
   - React build folder (`build/`)

---

### **1. Resource Group (Starting Point)**

```bash
# Create resource group
az group create --name rg-fullstack-dev --location eastus

# List groups
az group list --output table
```

---

### **2. Storage Account**

Storage is useful for application files, images, logs, or backups.

```bash
# Create Storage Account
az storage account create \
  --name fullstackstorage2026 \
  --resource-group rg-fullstack-dev \
  --location eastus \
  --sku Standard_LRS \
  --kind StorageV2 \
  --allow-blob-public-access false

# Get connection string (store securely)
az storage account show-connection-string \
  --name fullstackstorage2026 \
  --resource-group rg-fullstack-dev

# Create a container (for blobs)
az storage container create \
  --account-name fullstackstorage2026 \
  --name uploads \
  --auth-mode login
```

**Common Uses**:
- Store React static assets or user uploads from Spring Boot.

---

### **3. Azure SQL Database**

```bash
# Create SQL Server
az sql server create \
  --name fullstack-sqlserver \
  --resource-group rg-fullstack-dev \
  --location eastus \
  --admin-user sqladmin \
  --admin-password "StrongPassword123!"

# Create Firewall Rule (allow Azure services + your IP)
az sql server firewall-rule create \
  --resource-group rg-fullstack-dev \
  --server fullstack-sqlserver \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

az sql server firewall-rule create \
  --resource-group rg-fullstack-dev \
  --server fullstack-sqlserver \
  --name AllowMyIP \
  --start-ip-address YOUR_PUBLIC_IP \
  --end-ip-address YOUR_PUBLIC_IP

# Create Database (General Purpose, Serverless - cost effective)
az sql db create \
  --resource-group rg-fullstack-dev \
  --server fullstack-sqlserver \
  --name appdb \
  --edition GeneralPurpose \
  --compute-model Serverless \
  --family Gen5 \
  --capacity 2
```

**Get Connection String**:
```bash
az sql db show-connection-string \
  --client java \
  --server fullstack-sqlserver \
  --name appdb
```

**Spring Boot `application.properties` example**:
```properties
spring.datasource.url=jdbc:sqlserver://fullstack-sqlserver.database.windows.net:1433;database=appdb;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
spring.datasource.username=sqladmin@fullstack-sqlserver
spring.datasource.password=StrongPassword123!
```

> **Note**: For **Azure Database for MySQL Flexible Server** (if you prefer MySQL), use `az mysql flexible-server create`.

---

### **4. App Service (for Spring Boot Backend)**

```bash
# Create App Service Plan (Linux, Basic tier for start)
az appservice plan create \
  --name fullstack-plan \
  --resource-group rg-fullstack-dev \
  --location eastus \
  --sku B1 \
  --is-linux

# Create Web App (Java 17)
az webapp create \
  --resource-group rg-fullstack-dev \
  --plan fullstack-plan \
  --name fullstack-backend \
  --runtime "JAVA|17-java17"

# Configure Application Settings (Connection Strings)
az webapp config appsettings set \
  --resource-group rg-fullstack-dev \
  --name fullstack-backend \
  --settings SPRING_PROFILES_ACTIVE=azure \
  --settings SPRING_DATASOURCE_URL="jdbc:sqlserver://..." \
  --settings SPRING_DATASOURCE_USERNAME="sqladmin@fullstack-sqlserver"

# (Optional) Use Key Vault for secrets in production
```

**Scale Up / Out**:
```bash
# Scale up (more CPU/memory)
az appservice plan update --name fullstack-plan --resource-group rg-fullstack-dev --sku S1

# Enable Autoscale (later)
```

---

### **5. Deploy Spring Boot Application**

**Option A: Using Azure CLI (Zip Deploy)**

```bash
# Zip your JAR (recommended name: app.jar)
cd backend
zip -r app.zip target/*.jar

# Deploy
az webapp deploy \
  --resource-group rg-fullstack-dev \
  --name fullstack-backend \
  --src-path app.zip \
  --type jar
```

**Option B: Maven Plugin (Recommended for Spring Boot)**

Add plugin in `pom.xml`, then:
```bash
mvn clean package azure-webapp:deploy
```

---

### **6. React Frontend Options**

#### **Option 1: Azure Static Web Apps (Recommended for React)**

```bash
# Create Static Web App (links to GitHub automatically)
az staticwebapp create \
  --name fullstack-frontend \
  --resource-group rg-fullstack-dev \
  --location eastus \
  --source https://github.com/yourusername/fullstack-app \
  --branch main \
  --app-location "/" \
  --output-location "build" \
  --api-location ""   # empty if no API in same repo
```

#### **Option 2: Deploy React on App Service**

```bash
# Create another App (or use same plan)
az webapp create \
  --resource-group rg-fullstack-dev \
  --plan fullstack-plan \
  --name fullstack-frontend \
  --runtime "NODE|20-lts"

# Zip and deploy build folder
cd frontend
zip -r frontend.zip build
az webapp deploy --resource-group rg-fullstack-dev --name fullstack-frontend --src-path frontend.zip --type static
```

---

### **7. Useful Management Commands**

```bash
# View logs (Streaming)
az webapp log tail --name fullstack-backend --resource-group rg-fullstack-dev

# Restart App
az webapp restart --name fullstack-backend --resource-group rg-fullstack-dev

# List all resources
az resource list --resource-group rg-fullstack-dev --output table

# Delete everything (careful!)
az group delete --name rg-fullstack-dev
```

**Configure CORS for React → Spring Boot**:
```bash
az webapp cors add \
  --resource-group rg-fullstack-dev \
  --name fullstack-backend \
  --allowed-origins "https://fullstack-frontend.azurewebsites.net"
```

---

### **Best Practices**

- Use **Managed Identities** instead of connection strings with passwords.
- Store secrets in **Azure Key Vault**.
- Enable **HTTPS Only**:
  ```bash
  az webapp update --name fullstack-backend --resource-group rg-fullstack-dev --https-only true
  ```
- Start with low-cost tiers (B1, Serverless) and monitor with Azure Advisor.
- Use **deployment slots** for zero-downtime (Standard tier+).

---

**Next Steps**:
1. Run the commands step-by-step in order.
2. Integrate your Spring Boot app with the database.
3. Deploy React and test API calls.
