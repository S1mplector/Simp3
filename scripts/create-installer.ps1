# SiMP3 Installer Creation Script
# Creates a self-contained installer with bundled Java runtime using jpackage

param(
    [string]$Version = "1.0.0",
    [switch]$SkipBuild = $false,
    [string]$Type = "exe"  # exe or msi
)

Write-Host "=== SiMP3 Installer Builder ===" -ForegroundColor Cyan
Write-Host "Version: $Version" -ForegroundColor Yellow
Write-Host "Installer Type: $Type" -ForegroundColor Yellow

# Check if Java 17+ with jpackage is available
try {
    $javaVersion = java -version 2>&1
    $jpackageVersion = jpackage --version 2>&1
    Write-Host "Java and jpackage found" -ForegroundColor Green
} catch {
    Write-Host "ERROR: jpackage not found. Please ensure you have JDK 17+ installed (not just JRE)." -ForegroundColor Red
    Write-Host "Download from: https://adoptium.net/temurin/releases/" -ForegroundColor Yellow
    exit 1
}

# Build the project if not skipped
if (!$SkipBuild) {
    Write-Host "`nBuilding SiMP3..." -ForegroundColor Yellow
    $buildProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvn clean package -DskipTests" -NoNewWindow -PassThru -Wait
    if ($buildProcess.ExitCode -ne 0) {
        Write-Host "ERROR: Build failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "Build completed successfully!" -ForegroundColor Green
}

# Check if the JAR exists
$jarPath = "target\simp3-$Version.jar"
if (!(Test-Path $jarPath)) {
    # Try with shaded JAR name
    $jarPath = Get-ChildItem -Path "target" -Filter "*.jar" | Where-Object { $_.Name -notlike "*original*" } | Select-Object -First 1
    if (!$jarPath) {
        Write-Host "ERROR: JAR file not found in target directory!" -ForegroundColor Red
        exit 1
    }
    $jarPath = $jarPath.FullName
}

Write-Host "`nUsing JAR: $jarPath" -ForegroundColor Yellow

# Prepare directories
$buildDir = "target\jpackage"
$installerDir = "releases\installers"

if (Test-Path $buildDir) {
    Remove-Item -Path $buildDir -Recurse -Force
}
New-Item -ItemType Directory -Path $buildDir -Force | Out-Null

if (!(Test-Path $installerDir)) {
    New-Item -ItemType Directory -Path $installerDir -Force | Out-Null
}

# Extract dependencies for module path
Write-Host "`nPreparing dependencies..." -ForegroundColor Yellow
$libDir = "$buildDir\lib"
New-Item -ItemType Directory -Path $libDir -Force | Out-Null

# Copy the main JAR
Copy-Item $jarPath -Destination "$buildDir\simp3.jar" -Force

# Create input directory for jpackage
$inputDir = "$buildDir\input"
New-Item -ItemType Directory -Path $inputDir -Force | Out-Null
Copy-Item "$buildDir\simp3.jar" -Destination $inputDir -Force

# Run jpackage
Write-Host "`nCreating installer with jpackage..." -ForegroundColor Yellow

$jpackageArgs = @(
    "--type", $Type,
    "--name", "SiMP3",
    "--app-version", $Version,
    "--vendor", "SiMP3 Music Player",
    "--description", "A modern, modular music player",
    "--input", $inputDir,
    "--main-jar", "simp3.jar",
    "--main-class", "com.musicplayer.Launcher",
    "--icon", "src\main\resources\images\icons\app.ico",
    "--dest", $installerDir,
    "--win-dir-chooser",
    "--win-menu",
    "--win-shortcut",
    "--win-per-user-install",
    "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--java-options", "--add-opens=java.base/java.nio=ALL-UNNAMED",
    "--java-options", "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
    "--java-options", "--add-opens=java.base/java.io=ALL-UNNAMED"
)

# Add file associations
$jpackageArgs += @(
    "--file-associations", "scripts\file-associations.properties"
)

# Create file associations properties file
$fileAssocContent = @"
extension=mp3
mime-type=audio/mpeg
description=MP3 Audio File
icon=src\main\resources\images\icons\song.png
"@
Set-Content -Path "scripts\file-associations.properties" -Value $fileAssocContent

Write-Host "Running jpackage with arguments:" -ForegroundColor Gray
$jpackageArgs | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

try {
    & jpackage $jpackageArgs
    
    # Find the created installer
    $installerPattern = if ($Type -eq "msi") { "SiMP3-*.msi" } else { "SiMP3-*.exe" }
    $installer = Get-ChildItem -Path $installerDir -Filter $installerPattern | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    
    if ($installer) {
        # Rename to include version
        $newName = "SiMP3-v$Version-installer.$Type"
        $newPath = Join-Path $installerDir $newName
        Move-Item $installer.FullName -Destination $newPath -Force
        
        Write-Host "`n=== Installer created successfully! ===" -ForegroundColor Green
        Write-Host "Location: $newPath" -ForegroundColor Yellow
        Write-Host "`nThis installer includes Java runtime - no Java installation required!" -ForegroundColor Green
        
        # Create installer README
        $readmeContent = @"
SiMP3 Music Player v$Version - Installer
=====================================

This installer includes everything needed to run SiMP3:
- SiMP3 application
- Java runtime (bundled)
- All required dependencies

Installation:
1. Run the installer
2. Follow the installation wizard
3. Launch SiMP3 from the Start Menu or Desktop shortcut

No additional software installation required!

System Requirements:
- Windows 10 or later
- 200 MB free disk space
- 2 GB RAM recommended

Features:
- Self-contained - no Java installation needed
- Automatic file associations for music files
- Start menu and desktop shortcuts
- Clean uninstaller included
"@
        
        Set-Content -Path "$installerDir\README-installer.txt" -Value $readmeContent
        Write-Host "README created for installer" -ForegroundColor Green
    } else {
        Write-Host "WARNING: Installer file not found in $installerDir" -ForegroundColor Yellow
    }
} catch {
    Write-Host "ERROR: jpackage failed!" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

# Clean up
Remove-Item "scripts\file-associations.properties" -Force -ErrorAction SilentlyContinue