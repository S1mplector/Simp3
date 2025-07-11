# SiMP3 Packaging and Distribution Guide

## Overview

SiMP3 can be packaged in several ways, each with different requirements for the end user:

1. **Basic EXE** - Requires Java 17+ installed on target machine
2. **Self-contained Installer** - Includes Java runtime, no prerequisites needed
3. **Portable Package** - Includes runtime, can run from USB/folder

## Current Issue

The executable created by simply copying from the `target` folder **requires Java to be installed** on the target machine. This is why it works on your development machine but fails on other computers without Java.

## Solution: Proper Build Process

### Option 1: Basic Release (Requires Java)

```powershell
# This creates an EXE that requires Java 17+ on target machine
.\scripts\create-release.ps1 -Version "1.0.0"
```

**Pros:**
- Small file size (~10 MB)
- Quick to build

**Cons:**
- Requires Java 17+ installed on target machine
- Will fail silently if Java is not found

### Option 2: Self-Contained Installer (Recommended)

```powershell
# First, ensure you have JDK 17+ (not just JRE)
java -version
jpackage --version

# Create installer with bundled Java runtime
.\scripts\create-installer.ps1 -Version "1.0.0" -Type exe
```

**Pros:**
- No Java required on target machine
- Professional installer experience
- Includes uninstaller
- File associations for music files

**Cons:**
- Larger file size (~80-100 MB)
- Requires JDK with jpackage tool

### Option 3: Maven JavaFX Plugin

```powershell
# Use Maven's JavaFX plugin for packaging
.\scripts\package-with-runtime.ps1 -Version "1.0.0"
```

## Step-by-Step: Creating a Proper Release

### For GitHub Releases (Recommended Approach)

1. **Build the project properly:**
   ```powershell
   mvn clean package
   ```

2. **Create self-contained installer:**
   ```powershell
   .\scripts\create-installer.ps1 -Version "1.0.0"
   ```

3. **Upload to GitHub:**
   - Go to your GitHub repository
   - Click "Releases" → "Create a new release"
   - Tag version: `v1.0.0`
   - Release title: `SiMP3 v1.0.0`
   - Upload files:
     - `releases\installers\SiMP3-v1.0.0-installer.exe` (self-contained)
     - `releases\SiMP3-v1.0.0\SiMP3.exe` (requires Java)
   - Add release notes explaining requirements

### Release Notes Template

```markdown
## SiMP3 v1.0.0

### Downloads

#### 🎯 Recommended: Windows Installer
- **File:** `SiMP3-v1.0.0-installer.exe`
- **Size:** ~90 MB
- **Requirements:** Windows 10 or later
- **Includes:** Java runtime bundled - no installation required!

#### 💾 Standalone Executable
- **File:** `SiMP3.exe`
- **Size:** ~10 MB
- **Requirements:** Java 17+ must be installed
- **Note:** For advanced users who already have Java

### What's New
- Auto-update functionality
- Update checking on startup
- Comprehensive logging
- Settings integration

### Installation Instructions

**For Installer (Recommended):**
1. Download `SiMP3-v1.0.0-installer.exe`
2. Run the installer
3. Follow the setup wizard
4. Launch from Start Menu or Desktop

**For Standalone EXE:**
1. Ensure Java 17+ is installed: `java -version`
2. Download `SiMP3.exe`
3. Double-click to run
```

## Troubleshooting

### "The application won't start" (No error message)
- **Cause:** Java is not installed or not in PATH
- **Solution:** Use the installer version or install Java 17+

### "jpackage: command not found"
- **Cause:** Using JRE instead of JDK
- **Solution:** Install full JDK from [Adoptium](https://adoptium.net/)

### Build Failures
1. Clean everything: `mvn clean`
2. Delete `target` folder manually
3. Rebuild: `mvn package`

## File Structure After Proper Build

```
simp3/
├── target/
│   ├── SiMP3.exe              # Launch4j wrapper (needs Java)
│   ├── simp3-1.0.0.jar        # Shaded JAR with dependencies
│   └── jpackage/              # If using jpackage
├── releases/
│   ├── SiMP3-v1.0.0/          # Basic release
│   │   ├── SiMP3.exe
│   │   └── README.txt
│   └── installers/            # Self-contained installers
│       ├── SiMP3-v1.0.0-installer.exe
│       └── README-installer.txt
```

## Best Practices

1. **Always test on a clean machine** without Java installed
2. **Provide both versions** - installer and standalone
3. **Clear documentation** about requirements
4. **Use semantic versioning** (1.0.0, 1.0.1, etc.)
5. **Sign your executables** (optional but recommended)

## Quick Commands Reference

```powershell
# Full build and package
mvn clean package

# Create basic release (needs Java)
.\scripts\create-release.ps1

# Create installer (self-contained)
.\scripts\create-installer.ps1

# Test without building
.\scripts\create-installer.ps1 -SkipBuild

# Create MSI instead of EXE
.\scripts\create-installer.ps1 -Type msi