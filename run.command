#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "Building Java CLI project..."
mvn clean package

echo "Installing GUI dependencies..."
cd gui
npm install

echo "Launching Electron GUI..."
npm start
