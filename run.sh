#!/bin/bash

# Navigate to the backend directory
cd "$(dirname "$0")/backend"

# Run the Spring Boot application
echo "Starting Smart Home Energy Management System..."
mvn spring-boot:run
