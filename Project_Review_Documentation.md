# Smart Home Energy Management System

---

## 1. Abstract
The **Smart Home Energy Management System (SHEMS)** is a comprehensive, full-stack enterprise platform designed to revolutionize how households and facility managers monitor, control, and optimize electricity consumption. In an era of increasing energy costs and grid instability, this system provides real-time analytics, automated load shedding, and secure, tamper-proof event logging. Built on a robust Java Spring Boot backend and an elegant, iOS-inspired Thymeleaf frontend, SHEMS bridges the gap between complex IoT architecture and intuitive user control, ensuring optimal energy utilization without sacrificing user comfort.

---

## 2. Problem Statement
Modern residential and commercial environments face several critical energy challenges:
- **Increasing Energy Costs**: Lack of visibility into real-time consumption makes it difficult for users to reduce waste.
- **Grid Instability**: Simultaneous high-power draw from multiple devices can lead to circuit overloads and equipment damage.
- **Lack of Accountability**: Standard smart home systems often lack tamper-proof auditing for critical system changes or administrative overrides.
- **Fragmented Control**: Managing diverse IoT devices often requires multiple incompatible platforms.

---

## 3. Proposed Solution
SHEMS addresses these issues through a centralized, intelligent hub that combines advanced monitoring with automated safety measures:
- **Real-Time Visibility**: High-fidelity charts and dashboard metrics provide immediate insights into power usage.
- **Intelligent Load Balancing**: An automated load-shedding algorithm that prioritizes essential devices during peak demand.
- **Enterprise-Grade Security**: Blockchain-based logging and multi-factor authentication (2FA) for secure operations.
- **Unified Interface**: A single platform for web, CLI, and IoT device management.

---

## 4. System Architecture

### 4.1 Backend (Spring Boot)
The core of the system is a **Java Spring Boot** application that manages the business logic, data persistence, and security.
- **Framework**: Spring Boot 3.x
- **Security**: Spring Security with Role-Based Access Control (RBAC) and JWT-based authentication.
- **Data Layer**: Spring Data JPA for seamless interaction with relational databases.
- **IoT Integration**: Built-in support for MQTT protocol (`tcp://test.mosquitto.org:1883`) to communicate with physical hardware.

### 4.2 Frontend (Web Interface)
The user interface is designed with a premium, iOS-inspired **Glassmorphism** aesthetic.
- **Technologies**: HTML5, CSS3, Vanilla JavaScript.
- **Templating**: Thymeleaf for Server-Side Rendering (SSR).
- **Visualization**: Chart.js for real-time and historical energy consumption analytics.

### 4.3 CLI Component
A standalone Java-based Command Line Interface (CLI) allows technicians and power users to interact with the system via a terminal.
- **Capabilities**: Device listing, status toggling, and remote management through the REST API.

---

## 5. Core Features

### 5.1 Device Management & Control
Users can manage a variety of smart devices (Lights, AC, Fans, etc.) through both the web dashboard and CLI.
- **Manual Control**: Real-time toggling of device status (ON/OFF).
- **Scheduling**: Automated scheduling for device operations to optimize energy use.
- **Device Pairing**: Easy integration for new smart home hardware.

### 5.2 Intelligent Load Shedding Logic
The system features a sophisticated load shedding algorithm to prevent grid overloads.
- **Global Load Monitoring**: Continuous calculation of total system wattage.
- **Priority-Based Shedding**: Automatically shuts down low-priority devices when the load exceeds safe thresholds, ensuring critical devices remain powered.

### 5.3 Immutable Event Ledger (Blockchain)
For high-security environments, SHEMS implements a simulated blockchain-based logging system.
- **Data Integrity**: Every critical action (firmware updates, admin overrides) is logged in a cryptographically chained ledger.
- **Auditability**: Provides a tamper-proof record for security audits.

### 5.4 Advanced Analytics & Data Export
Comprehensive data tools for monitoring and reporting.
- **Live Analytics**: Real-time wattage usage vs. historical patterns.
- **CSV Export**: Admins can export detailed operational logs for financial and technical analysis.

---

## 6. Technical Implementation Details

### 6.1 Security & Authentication
- **RBAC**: Three distinct roles — `USER`, `ADMIN`, and `TECHNICIAN`.
- **2FA/TOTP**: Enhanced security with Time-based One-Time Passwords for multi-factor authentication.
- **JWT**: Secure session management for both web and CLI clients.

### 6.2 Data Persistence & Seeding
- **Database Support**: Built for compatibility with H2 (development) and enterprise solutions like PostgreSQL or MySQL.
- **Automatic Seeding**: The system automatically populates sample data on the first run to allow immediate demonstration of features.

---

## 7. Deployment Guide

### 7.1 Prerequisites
- **Java**: JDK 17 or higher.
- **Maven**: For dependency management and building the project.

### 7.2 Startup Instructions
The project includes a robust startup script (`run.bat`) that handles the following:
1. Environment verification (Java/Maven checks).
2. Automated build (Maven compilation/packaging).
3. Port management (Clearing port 8080).
4. Application launch and browser automation.

---

## 8. Conclusion
The Smart Home Energy Management System represents a robust, enterprise-ready solution for modern IoT management. With its combination of intelligent load shedding, secure logging, and premium UI, it is well-positioned for both residential and commercial deployment.

**Status**: Final Review Ready
**Target Environment**: Enterprise Smart Home Hubs
