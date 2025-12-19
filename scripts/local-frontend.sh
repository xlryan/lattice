#!/bin/bash

echo "========================================="
echo "   Starting Lattice Frontend (Local)     "
echo "========================================="

cd "$(dirname "$0")/../web-react" || exit

if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

echo "Starting Vite server..."
npm run dev
