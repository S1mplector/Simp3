@echo off
echo Applying SiMP3 update...
echo.

:: Change to parent directory (where SiMP3.exe should be)
cd /d "%~dp0\.."

:: Wait a bit for the application to fully close
timeout /t 3 /nobreak > nul

:: Backup current executable
if exist "SiMP3.exe" (
    echo Backing up current version...
    move /Y "SiMP3.exe" "SiMP3.exe.backup" > nul 2>&1
)

:: Check if update file is a zip
set "updateFile=update\SiMP3.exe"
if /i "%~x1"==".zip" (
    echo Extracting update...
    powershell -Command "Expand-Archive -Path '%updateFile%' -DestinationPath '.' -Force"
) else (
    :: Copy new executable
    echo Installing new version...
    copy /Y "%updateFile%" "SiMP3.exe" > nul
)

:: Clean up update file
if exist "%updateFile%" del /Q "%updateFile%"

:: Start updated application
echo Starting SiMP3...
start "" "SiMP3.exe"

:: Clean up update directory
timeout /t 2 /nobreak > nul
rmdir /Q "update" 2>nul

:: Exit
exit
