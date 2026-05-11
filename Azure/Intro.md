**Cloud Computing Introduction & Microsoft Azure Fundamentals**  

---

### **Introduction to Cloud Computing**

**What is Cloud Computing?**  
Cloud computing refers to the delivery of computing resources—including servers, storage, databases, networking, software applications, analytics tools, and artificial intelligence capabilities—over the internet on a pay-as-you-go basis. Rather than investing in and maintaining physical hardware and data centers on your own premises, organizations and individuals can access these resources from cloud service providers as and when needed. This model provides flexibility, scalability, and cost efficiency compared to traditional on-premises IT infrastructure.

**Key Characteristics of Cloud Computing**  
The National Institute of Standards and Technology (NIST) defines cloud computing through five essential characteristics. **On-demand self-service** allows users to provision computing resources automatically without requiring human interaction from the service provider. **Broad network access** ensures that capabilities are available over the network and accessible through standard mechanisms on various devices such as laptops, smartphones, and tablets. **Resource pooling** means the provider’s computing resources are pooled to serve multiple consumers using a multi-tenant model, with dynamic assignment according to demand. **Rapid elasticity** enables capabilities to scale out or in quickly and automatically, appearing unlimited to the consumer. Finally, **measured service** ensures that resource usage is monitored, controlled, and reported, providing transparency for both the provider and the consumer, similar to how utilities like electricity are billed.

**Cloud Service Models**  
Cloud services are broadly categorized into three main models, commonly remembered as the SPI model (Software, Platform, and Infrastructure).

- **Infrastructure as a Service (IaaS)** provides fundamental computing resources such as virtual machines, storage, and networking. In this model, the consumer has control over operating systems, applications, and data but does not manage the underlying physical infrastructure. Examples include Azure Virtual Machines and AWS EC2. IaaS is ideal when you need maximum control and customization.

- **Platform as a Service (PaaS)** offers a complete development and deployment environment in the cloud. The provider manages the underlying infrastructure, runtime, middleware, and operating systems, allowing developers to focus solely on building and managing applications and data. Examples include Azure App Service and Azure SQL Database. This model accelerates development cycles significantly.

- **Software as a Service (SaaS)** delivers fully functional applications over the internet. Users simply access the software through a web browser or client application without worrying about any underlying infrastructure, platform, or maintenance. Popular examples are Microsoft 365, Gmail, and Salesforce.

**Cloud Deployment Models**  
Organizations can deploy cloud solutions in different ways depending on their requirements for control, security, and compliance.

- **Public Cloud**: Services are owned and operated by third-party providers and made available to the general public over the internet. This is the most common and cost-effective option for beginners.
- **Private Cloud**: Infrastructure is dedicated exclusively to a single organization and can be hosted on-premises or by a third-party provider. It offers greater customization and control.
- **Hybrid Cloud**: A combination of public and private clouds that allows data and applications to move between the two environments. Most large enterprises adopt hybrid strategies for flexibility and compliance.
- **Multi-Cloud**: Using services from more than one public cloud provider (for example, Azure + AWS) to avoid vendor lock-in and leverage best-of-breed services.

**Benefits and Challenges of Cloud Computing**  
The primary benefits include significant cost savings by converting capital expenditure (CapEx) into operational expenditure (OpEx), rapid scalability to meet fluctuating demands, high reliability through built-in redundancy across global data centers, enhanced security features backed by massive investments from providers, and faster innovation through access to cutting-edge technologies like AI and machine learning.  

However, challenges also exist. These include concerns around data security and regulatory compliance, potential vendor lock-in, the need for new skill sets within teams, and the risk of unexpected costs if resources are not properly governed.

---

### **Microsoft Azure Fundamentals**

**Overview of Microsoft Azure**  
Microsoft Azure is a comprehensive cloud computing platform that provides over 200 services, including computing power, storage solutions, databases, networking, artificial intelligence, Internet of Things (IoT), and analytics. As of 2026, Azure remains one of the leading global cloud providers. It integrates seamlessly with Microsoft products such as Windows, Office 365, and Power Platform, making it particularly attractive for organizations already using Microsoft technologies.

#### **2.1 Core Architectural Components**

**Regions and Availability Zones**  
An Azure **region** is a specific geographic area that contains one or more data centers. When deploying resources, you select a region based on factors such as user proximity (to reduce latency), data residency and compliance requirements, service availability, and pricing variations. Examples of regions include East US, West Europe, and Southeast Asia.  

Within each region, **Availability Zones (AZs)** are physically separate data center facilities equipped with independent power, cooling, and networking. Most regions have at least three Availability Zones. Using Availability Zones helps protect applications from failures at the data center level through redundant deployments.

**Resource Groups**  
A resource group is a logical container that stores references to all related Azure resources for a specific solution. It acts as a management boundary, allowing you to manage the lifecycle of resources collectively. Deleting a resource group automatically deletes all resources inside it. Resource groups are essential for organizing, applying Role-Based Access Control (RBAC), tagging for cost tracking, and applying policies.

**Subscriptions and Management Groups**  
An **Azure subscription** serves as a billing and resource boundary. It is linked to a Microsoft Entra ID (formerly Azure Active Directory) tenant. Organizations often use multiple subscriptions to separate development, testing, and production environments or different departments.  

**Management Groups** provide a higher-level hierarchy for organizing multiple subscriptions. They enable centralized governance, policy application, and compliance enforcement across the entire organization.

**Azure Resource Manager (ARM)**  
Azure Resource Manager is the deployment and management service for Azure. It allows you to create, update, and delete resources in a consistent manner. Modern deployments frequently use **Bicep** (a declarative language) or ARM JSON templates for Infrastructure as Code (IaC) practices.

#### **2.2 Core Azure Services**

**Compute Services**  
Azure offers a wide range of compute options to suit different workloads. **Azure Virtual Machines (VMs)** provide Infrastructure as a Service with full control over the operating system and software. **Azure App Service** is a Platform as a Service offering for hosting web applications, APIs, and mobile backends with built-in scaling and CI/CD support. **Azure Functions** enable serverless computing where you run event-driven code without managing servers. Other options include Azure Kubernetes Service (AKS) for container orchestration, Azure Container Instances for simple container execution, and Azure Virtual Desktop for virtualized desktop environments.

**Networking Services**  
**Azure Virtual Network (VNet)** allows you to create isolated private networks in the cloud, similar to on-premises networks. Additional services such as Azure Load Balancer, Application Gateway, and Front Door provide traffic distribution and protection. Security features include Azure Firewall, while connectivity to on-premises environments is supported through VPN Gateway or the private ExpressRoute service.

**Storage Services**  
Azure Storage is highly durable and scalable. **Azure Blob Storage** handles unstructured data such as documents, images, and videos. **Azure Files** provides fully managed file shares accessible via SMB or NFS. Other services include Queue Storage for messaging, Table Storage for NoSQL key-value data, and managed disks for virtual machines. Storage accounts support multiple redundancy options (Locally Redundant Storage – LRS, Zone-Redundant – ZRS, Geo-Redundant – GRS, etc.) to balance cost and durability.

**Database Services**  
Azure provides both relational and non-relational databases. **Azure SQL Database** is a fully managed Platform as a Service version of SQL Server. **Azure Cosmos DB** offers globally distributed, multi-model NoSQL database capabilities with low latency. Additional options include managed databases for MySQL, PostgreSQL, and caching solutions like Azure Cache for Redis.

**Identity, Security, and Monitoring**  
**Microsoft Entra ID** manages identity and access in the cloud. **Role-Based Access Control (RBAC)** and Managed Identities enable secure, granular permission management. Security is further enhanced through Microsoft Defender for Cloud, Azure Key Vault for secrets management, and comprehensive monitoring via Azure Monitor and Application Insights.

#### **2.3 Pricing, Cost Management, SLAs, and Governance**

Azure follows a consumption-based pricing model. You can choose Pay-as-you-go, Reserved Instances, Savings Plans, or Spot VMs to optimize costs. The Azure Pricing Calculator and Cost Management tools help track and control spending through budgets, alerts, and detailed analysis.  

Service Level Agreements (SLAs) define Azure’s promised uptime percentages (often 99.9% or higher). Organizations design resilient architectures using Availability Zones and regions to meet composite SLAs. Governance is achieved through Azure Policy, Blueprints, and Microsoft Purview for compliance and data governance. Security follows a shared responsibility model where Microsoft secures the infrastructure and customers secure their data, identities, and applications.

---

### **Module 3: Getting Started and Next Steps**

**Practical Onboarding Steps**  
1. Sign up for a free Azure account at azure.microsoft.com.  
2. Explore the Azure Portal using the global search bar.  
3. Create your first Resource Group, then deploy simple resources like a Web App or Virtual Machine.  
4. Learn Azure CLI or PowerShell for automation.
