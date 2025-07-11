# SiMP3 Packaging Guide

This guide explains how to package and distribute SiMP3 for Windows users.

## Prerequisites

Before packaging SiMP3, ensure you have:

1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://adoptium.net/
   - Verify: `java -version` (should show 17 or higher)
   - Ensure you have JDK, not just JRE (jpackage requires JDK)

2. **Apache Maven**
   - Download from: https://maven.apache.org/download.cgi
   - Add to PATH
   - Verify: `mvn -version`

3. **WiX Toolset v3.14** (for creating MSI installers)
   - Run: `install-wix.bat`
   - Or download manually from: https://github.com/wixtoolset/wix3/releases
   - Verify: `candle.exe -?`

## Packaging Options

SiMP3 provides three packaging options, each with different use cases:

### 1. Windows Installer (.msi/.exe)
**Best for:** General distribution to end users

Creates a professional Windows installer that:
- Bundles Java runtime (no Java required on target PC)
- Creates Start Menu shortcuts
- Handles proper installation/uninstallation
- Registers with Windows Programs & Features

**To create:**
```batch
scripts\create-installer.bat
```

Choose option 1 (exe) or 2 (msi) when prompted. The installer will be created in:
- `releases\SiMP3-v{version}-installer.exe` or
- `releases\SiMP3-v{version}-installer.msi`

### 2. Portable Version
**Best for:** USB drives, no-installation scenarios

Creates a self-contained folder that:
- Includes bundled Java runtime
- Requires no installation
- Can run from any location
- Leaves no registry entries

**To create:**
```batch
create-portable.bat
```

The portable version will be created in:
- `releases\SiMP3-v{version}-portable\`

To use: Copy the entire folder and run `SiMP3.exe` from within it.

### 3. Basic Executable (Not Recommended)
**Best for:** Development/testing only

Creates a basic .exe that:
- Requires Java 17+ installed on target PC
- Will not work without Java
- Smaller file size but less portable

**To create:**
```batch
scripts\create-release.bat
```

The basic executable will be created in:
- `releases\SiMP3-v{version}\SiMP3.exe`

## Script Features

All packaging scripts now include:

### Error Checking
- Verifies all prerequisites are installed
- Checks Java version compatibility
- Validates script locations
- Confirms successful operations

### User Guidance
- Clear error messages with solutions
- Interactive prompts for options
- Progress indicators
- Success confirmations

### Safety Features
- Confirmation prompts before operations
- Directory existence checks
- Clean error handling
- Detailed logging

## Step-by-Step Guide

### First Time Setup

1. **Install Prerequisites**
   ```batch
   # Install WiX Toolset (if not already installed)
   install-wix.bat
   ```

2. **Build the Project**
   ```batch
   mvn clean package
   ```

### Creating a Release

1. **For Distribution (Recommended)**
   ```batch
   scripts\create-installer.bat
   ```
   - Choose installer type (exe or msi)
   - Enter version number
   - Wait for completion

2. **For Portable Use**
   ```batch
   create-portable.bat
   ```
   - Enter version number
   - Wait for completion

### Distribution Checklist

Before distributing:

1. **Test on Clean System**
   - Test on PC without Java installed
   - Verify all features work
   - Check auto-update functionality

2. **Version Management**
   - Update version in `pom.xml`
   - Create Git tag
   - Build with correct version

3. **GitHub Release**
   - Create new release on GitHub
   - Upload installer/portable version
   - Update release notes

## Troubleshooting

### Common Issues

**"Maven not found"**
- Install Maven from https://maven.apache.org/
- Add Maven bin directory to PATH
- Restart terminal

**"jpackage not found"**
- Ensure you have JDK 17+, not just JRE
- Download JDK from https://adoptium.net/
- Add JDK bin directory to PATH

**"WiX not found" when creating MSI**
- Run `install-wix.bat`
- Restart terminal after installation
- Check PATH includes WiX bin directory

**"Build failed"**
- Run `mvn clean` first
- Check for compilation errors
- Ensure all dependencies are downloaded

### Build Requirements

- Minimum 4GB RAM recommended
- ~500MB free disk space
- Internet connection (first build)
- Windows 10/11

## Advanced Options

### Custom Icons
Place custom icons in `src/main/resources/`:
- `icon.ico` - Windows icon
- `icon.png` - General icon

### JVM Options
Edit `scripts\create-installer.ps1` to modify:
- Memory settings
- System properties
- Runtime options

### Version Numbering
Follow semantic versioning:
- MAJOR.MINOR.PATCH
- Example: 1.2.3

## File Structure

After packaging:
```
releases/
├── SiMP3-v1.0.0-installer.exe    # Windows installer
├── SiMP3-v1.0.0-installer.msi    # MSI installer
├── SiMP3-v1.0.0-portable/        # Portable version
│   ├── SiMP3.exe
│   ├── app/
│   └── runtime/
└── SiMP3-v1.0.0/                 # Basic version
    └── SiMP3.exe
```

## Best Practices

1. **Always Test**
   - Test each package type
   - Verify on different Windows versions
   - Check with/without admin rights

2. **Version Consistently**
   - Update pom.xml version
   - Tag Git commits
   - Document changes

3. **Provide Clear Instructions**
   - Include README in releases
   - Document system requirements
   - Provide troubleshooting guide

## Support

For issues with packaging:
1. Check error messages carefully
2. Verify all prerequisites
3. Run scripts from project root
4. Check GitHub issues
5. Review build logs

Remember: The installer and portable versions are self-contained and don't require Java on the target system, making them the best choice for distribution.