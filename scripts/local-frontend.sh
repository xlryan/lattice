#!/bin/bash

echo "========================================="
echo "   Starting Lattice Frontend (Local)     "
echo "========================================="

# Kill existing process on port 3000
PID=$(lsof -ti :3000)
if [ -n "$PID" ]; then
  echo "Killing existing process on port 3000 (PID: $PID)..."
  kill -9 $PID
fi

cd "$(dirname "$0")/../web-react" || exit

if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

echo "Starting Vite server..."
npm run dev
