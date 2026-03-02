# Smart Home Energy Management System
## Final Review Documentation

---

### 1. Abstract
The **Smart Home Energy Management System (SHEMS)** is a comprehensive, full-stack enterprise platform designed to revolutionize how households and facility managers monitor, control, and optimize electricity consumption. In an era of increasing energy costs and grid instability, this system provides real-time analytics, automated load shedding, and secure, tamper-proof event logging. Built on a robust Java Spring Boot backend and an elegant, iOS-inspired Thymeleaf frontend, SHEMS bridges the gap between complex IoT architecture and intuitive user control, ensuring optimal energy utilization without sacrificing user comfort.

---

### 2. Purpose and Usage
**Purpose:**
The primary goal of SHEMS is to provide a centralized hub for managing smart devices in a residential or commercial setting. It aims to prevent circuit overloads, reduce energy waste, and provide actionable insights into power consumption trends. By utilizing intelligent scheduling and prioritization (load shedding), the system ensures that critical devices remain operational during high-demand periods while non-essential devices are safely throttled.

**Core Usage Scenarios:**
*   **Homeowners (Users):** Log in to view live power draw, check weekly energy costs, and manually override device statuses (e.g., turning off the AC remotely). Users can schedule devices to run during off-peak hours to save money.
*   **Administrators:** Access a dedicated Command Center to monitor global system load, track active sessions, verify the integrity of the blockchain event ledger, and perform data exports for deep analysis.
*   **Field Technicians:** Utilize a streamlined portal to view assigned maintenance tasks, check device health statuses (e.g., failing firmware, high error rates), and log visit resolutions.

---

### 3. System Architecture
The application employs a standard Model-View-Controller (MVC) architectural pattern, emphasizing separation of concerns, security, and scalability.

#### 3.1 Technology Stack
*   **Backend:** Java 17, Spring Boot, Spring Security, Spring Data JPA, Maven.
*   **Frontend:** HTML5, CSS3 (Glassmorphism design), Vanilla JavaScript, Chart.js for data visualization, Thymeleaf for SSR (Server-Side Rendering).
*   **Database:** Configured for seamless integration with lightweight relational databases (e.g., H2 for dev, PostgreSQL/MySQL for production).

#### 3.2 Core Components
*   **Controllers (`/com/smarthome/backend/controller`):** RESTful endpoints and web route handlers orchestrating the flow of data between the frontend and operational services.
    *   *Examples:* `AdminController`, `DeviceController`, `EnergyController`, `WebController`.
*   **Services (`/com/smarthome/backend/service`):** The business logic layer.
    *   *DeviceService:* Handles status toggling and intelligent load shedding based on global capacity against device priorities.
    *   *BlockchainService:* A mock blockchain implementation ensuring that all critical system changes (like firmware updates or administrative overrides) are cryptographically chained and tamper-evident.
    *   *UsageLogService:* Aggregates hourly/daily power data for frontend charts.
*   **Security (`/com/smarthome/backend/security`):** Implements Role-Based Access Control (RBAC) separating `USER`, `ADMIN`, and `TECHNICIAN` roles. It also features Time-based One-Time Password (TOTP) implementations for 2FA.

---

### 4. Key Features

#### 4.1 Intelligent Load Shedding
The system continuously calculates the `Global System Load`. If the total wattage drawn by active devices exceeds the configured safe threshold, the system automatically intervenes. It evaluates the "Priority" attribute of active devices (High, Medium, Low) and intelligently shuts down low-priority devices (like ornamental lights) to protect the grid and maintain high-priority devices (like refrigerators).

#### 4.2 Immutable Event Ledger (Blockchain Sync)
To ensure accountability, especially in an enterprise or multi-tenant deployment, critical actions are logged into a simulated blockchain. Each block contains a hash of the previous block, a timestamp, and the specific event details. This ledger can be audited in the Admin Control Panel.

#### 4.3 Advanced Energy Analytics & Export
Through the `AdminController` and frontend `Chart.js` integrations, administrators can view live wattage usage versus historical weekly patterns. Additionally, `DeviceExportController` allows admins to filter raw operational data by device type and time range, exporting it directly to comprehensive CSV reports for financial accounting.

#### 4.4 Simulation and Dev Modes
The application features a unique "PRO Diagnostic Mode" built directly into the UI. When enabled, it overlays backend system health metrics (Database load, API Gateway latency, MQTT Broker status) and live server logs directly onto the frontend interface, functioning as a built-in development and monitoring tool.

---

### 5. Deployment and Operations
The system is designed for ease of deployment on Windows environments via a simplified `run.bat` script.
*   **Build Pipeline:** The script auto-detects Java/Maven requirements, cleans previous builds, compiles the Spring Boot `.jar`, securely vacates default ports (8080), and boots the application.
*   **Scalability:** Because it is built on Spring Boot, the backend can be easily containerized via Docker and deployed to cloud environments like AWS or Azure, scaling horizontally behind a load balancer.

---
**Document Status:** Final Release
**Project Module:** Smart Home Applications
**Framework:** Java Spring Boot / Web MVC
