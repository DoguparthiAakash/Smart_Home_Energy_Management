# Smart Home Energy Management System

A Spring Boot based backend for managing smart home devices and energy analytics.

## How to Run

### Quick Start (Linux/Mac)
You can start the server using the provided helper script:

```bash
./run.sh
```

### Manual Start
1.  Navigate to the backend directory:
    ```bash
    cd backend
    ```
2.  Run the application using Maven:
    ```bash
    mvn spring-boot:run
    ```

## Web Interface
Once running, access the dashboard at:
[http://localhost:8080/dashboard](http://localhost:8080/dashboard)

## Features
- **Real-Time Analytics**: View usage and cost.
- **Device Control**: Toggle devices ON/OFF.
- **Device Management**: Add (Manual or Pair) and Delete devices.
- **Data Seeding**: Automatically populates sample data on first run.
- **MQTT Support**: Supports real device integration via `tcp://test.mosquitto.org:1883`.
