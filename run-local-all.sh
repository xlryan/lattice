#!/bin/bash

# Function to kill all background jobs on exit
cleanup() {
    echo "Shutting down all services..."
    kill $(jobs -p) 2>/dev/null
}
trap cleanup EXIT

echo "========================================="
echo "   Lattice: Starting ALL Local Services  "
echo "========================================="

# Global cleanup
echo "Ensuring ports are free..."
for PORT in 8080 8000 3000; do
    PID=$(lsof -ti :$PORT)
    if [ -n "$PID" ]; then
        echo "Killing lingering process on port $PORT (PID: $PID)..."
        kill -9 $PID
    fi
done

SCRIPT_DIR="$(dirname "$0")"

# 1. Backend
"$SCRIPT_DIR/scripts/local-backend.sh" &
BACKEND_PID=$!
echo "Backend started (PID: $BACKEND_PID)"

# 2. Python Engine
cd "lattice-python-engine" && ./run_local.sh &
PYTHON_PID=$!
cd ..
echo "Python Engine started (PID: $PYTHON_PID)"

# 3. Frontend
# Give backend a moment to initialize? optional.
sleep 5
"$SCRIPT_DIR/scripts/local-frontend.sh" &
FRONTEND_PID=$!
echo "Frontend started (PID: $FRONTEND_PID)"

echo "All services running. Press Ctrl+C to stop."
wait
