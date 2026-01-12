#!/bin/bash
# Stop any existing process on 8080
fuser -k 8080/tcp 2>/dev/null || true

# Check if mosquitto is running (optional but recommended)
if ! pgrep -x "mosquitto" > /dev/null; then
    echo "Warning: Mosquitto MQTT broker is not running. Live updates might fail."
    echo "Try: sudo service mosquitto start"
fi

# Build and Run with robust memory settings
echo "Starting Smart Home Energy Management System..."
# Compile first to ensure latest changes are included
MAVEN_OPTS="-XX:+UseSerialGC -Xmx1024m" mvn -f backend/pom.xml clean package -DskipTests
# Run the jar with stability flags
java -XX:+UseSerialGC -Xmx1024m -jar backend/target/backend-0.0.1-SNAPSHOT.jar
