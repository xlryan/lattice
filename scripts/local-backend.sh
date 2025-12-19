#!/bin/bash

# Configuration
JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home"
DB_HOST="localhost"
DB_PORT="25432"

echo "========================================="
echo "   Starting Lattice Backend (Local)      "
echo "========================================="

# Check Java
if [ ! -d "$JAVA_HOME" ]; then
  echo "Error: JAVA_HOME not found at $JAVA_HOME"
  exit 1
fi
export JAVA_HOME

# Check DB Port (Simple check)
if ! nc -z $DB_HOST $DB_PORT 2>/dev/null; then
    echo "WARNING: Database port $DB_PORT on $DB_HOST is not accessible."
    echo "Ensure your Postgres Docker container is running: 'docker-compose up -d postgres'"
    read -p "Press Enter to continue anyway, or Ctrl+C to abort..."
fi

# Set Environment Variables
export LATTICE_DB_HOST=$DB_HOST
export LATTICE_DB_PORT=$DB_PORT
export LATTICE_DB_NAME=grain_log
export LATTICE_DB_USER=grain_user
export LATTICE_DB_PASSWORD=grain_password

# Run
cd "$(dirname "$0")/../lattice-oss" || exit
echo "Build and Run..."
./gradlew :lattice-application:bootRun
