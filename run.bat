@echo off
echo Building Java CLI project...
mvn clean package

echo Installing GUI dependencies...
cd gui
call npm install

echo Launching Electron GUI...
call npm start
