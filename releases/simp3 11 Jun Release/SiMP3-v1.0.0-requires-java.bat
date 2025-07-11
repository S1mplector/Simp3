@echo off
echo Starting SiMP3 Music Player...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Run the application
echo Launching SiMP3...
java -jar "%SCRIPT_DIR%simp3-1.0.0.jar"

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start SiMP3
    echo Make sure simp3-1.0.0.jar is in the same directory as this script
    pause
)