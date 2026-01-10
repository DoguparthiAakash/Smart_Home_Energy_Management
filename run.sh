#!/bin/bash

# Navigate to the backend directory
cd "$(dirname "$0")/backend"

# Kill any process running on port 8080
PID=$(lsof -t -i:8080)
if [ -n "$PID" ]; then
  echo "Killing process on port 8080 (PID: $PID)..."
  kill -9 $PID
fi

# Run the Spring Boot application
echo "Starting Smart Home Energy Management System..."
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:h2:file:./data/smarthome_db;AUTO_SERVER=TRUE"
