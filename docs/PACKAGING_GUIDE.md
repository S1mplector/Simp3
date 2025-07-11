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

## Distribution Types Explained

SiMP3 supports two distribution types, each designed for different use cases:

### Portable Version

**What is it?**
A self-contained package that requires no installation. All files, including the Java runtime, are bundled together in a single directory.

**Characteristics:**
- ✅ No installation required
- ✅ Can run from any location (USB drive, network share, cloud folder)
- ✅ No admin rights needed
- ✅ All settings and data stored locally
- ✅ Easy to backup - just copy the folder
- ✅ Multiple versions can coexist
- ✅ Leaves no traces on the system

**Best for:**
- Users without admin rights
- Running from USB drives
- Trying out the application
- Portable workstations
- Keeping multiple versions

**File Structure:**
```
SiMP3-portable/
├── SiMP3.exe           # Main executable
├── app/                # Application files
│   └── SiMP3.jar      # Main JAR file
├── runtime/            # Bundled Java runtime
├── data/              # User data (created on first run)
└── logs/              # Application logs
```

### Installer Version

**What is it?**
A traditional Windows installer that installs SiMP3 system-wide with proper integration.

**Characteristics:**
- ✅ Professional installation experience
- ✅ Start Menu shortcuts
- ✅ Desktop shortcut (optional)
- ✅ File associations (optional)
- ✅ Registered in Programs & Features
- ✅ Clean uninstaller included
- ✅ Automatic PATH configuration
- ✅ System-wide availability

**Best for:**
- Permanent installations
- Corporate environments
- Users who prefer traditional software
- System-wide deployment
- Managed IT environments

**Installation Locations:**
- Default: `C:\Program Files\SiMP3\`
- User data: `%APPDATA%\SiMP3\`
- Logs: `%LOCALAPPDATA%\SiMP3\logs\`

### Comparison Table

| Feature | Portable | Installer |
|---------|----------|-----------|
| Installation required | ❌ No | ✅ Yes |
| Admin rights needed | ❌ No | ✅ Yes |
| Start Menu integration | ❌ No | ✅ Yes |
| Can run from USB | ✅ Yes | ❌ No |
| System registry entries | ❌ No | ✅ Yes |
| Uninstaller | ❌ No | ✅ Yes |
| Multiple instances | ✅ Yes | ❌ No |
| Auto-update support | ✅ Yes | ✅ Yes |
| Settings location | Local folder | AppData |
| Typical size | ~150 MB | ~50 MB |

## Packaging Options

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

To distribute: ZIP the folder to create `SiMP3-v{version}-portable.zip`

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

## Building for Auto-Update Compatibility

To ensure your releases work properly with the auto-update system:

### File Naming Requirements

**Critical:** File names must follow these exact patterns for auto-update detection:

#### Portable Version
- Primary: `SiMP3-v{version}-portable.zip`
- Example: `SiMP3-v1.0.1-portable.zip`
- The word "portable" MUST be in the filename

#### Installer Version
- EXE: `SiMP3-v{version}-installer.exe` or `SiMP3-v{version}-setup.exe`
- MSI: `SiMP3-v{version}-installer.msi`
- Examples: 
  - `SiMP3-v1.0.1-installer.exe`
  - `SiMP3-v1.0.1-setup.exe`
  - `SiMP3-v1.0.1-installer.msi`
- Must contain "installer" or "setup" in the filename

#### Checksums
- Format: `{filename}.sha256`
- Examples:
  - `SiMP3-v1.0.1-portable.zip.sha256`
  - `SiMP3-v1.0.1-installer.exe.sha256`

### Directory Structure Expectations

The auto-update system uses directory structure to detect distribution type:

**Portable indicators:**
- Presence of `update/` directory
- Presence of `data/` directory
- Running from user-writable locations

**Installer indicators:**
- Running from `Program Files`
- Running from `AppData\Local`
- Standard Windows installation paths

### Update Script Considerations

Different update scripts are generated based on distribution type:

**Portable Updates:**
- In-place file replacement
- Backup of current executable
- Extraction of ZIP archives
- Rollback on failure

**Installer Updates:**
- Silent installation flags
- Proper Windows Installer handling
- Clean uninstallation of old version
- Registry updates

## GitHub Release Best Practices

### Release Structure

A proper GitHub release for auto-update should include:

```
Release: v1.0.1
├── SiMP3-v1.0.1-portable.zip
├── SiMP3-v1.0.1-portable.zip.sha256
├── SiMP3-v1.0.1-installer.exe
├── SiMP3-v1.0.1-installer.exe.sha256
└── Release Notes (in description)
```

### Asset Naming Examples

✅ **Correct naming:**
- `SiMP3-v1.0.1-portable.zip`
- `SiMP3-v1.0.1-installer.exe`
- `SiMP3-v2.0.0-beta-portable.zip`
- `SiMP3-v2.0.0-beta-setup.exe`

❌ **Incorrect naming:**
- `SiMP3-1.0.1.zip` (missing 'v' prefix and distribution type)
- `SiMP3-portable-v1.0.1.zip` (wrong order)
- `simp3_v1.0.1_portable.zip` (wrong separators)
- `SiMP3.exe` (no version or type)

### Release Notes Format

Include clear download sections:

```markdown
## Downloads

### 🎒 Portable Version
`SiMP3-v1.0.1-portable.zip` (150 MB)
- No installation required
- Run from anywhere
- SHA-256: `abc123...`

### 📦 Installer Version
`SiMP3-v1.0.1-installer.exe` (50 MB)
- Traditional installation
- Start Menu integration
- SHA-256: `def456...`
```

### Checksum Generation

Always include SHA-256 checksums:

```powershell
# Generate checksum
$hash = Get-FileHash -Algorithm SHA256 "SiMP3-v1.0.1-portable.zip"

# Save to file (just the hash, no formatting)
$hash.Hash | Out-File -FilePath "SiMP3-v1.0.1-portable.zip.sha256" -NoNewline

# Display for release notes
Write-Host "SHA-256: $($hash.Hash)"
```

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
   # Create installer version
   scripts\create-installer.bat
   
   # Create portable version
   create-portable.bat
   ```

2. **Generate Checksums**
   ```powershell
   # For all release files
   Get-ChildItem releases\*.zip, releases\*.exe, releases\*.msi | ForEach-Object {
       $hash = Get-FileHash $_.FullName -Algorithm SHA256
       $hash.Hash | Out-File -FilePath "$($_.FullName).sha256" -NoNewline
   }
   ```

3. **Prepare for GitHub**
   - Rename files to include version number
   - Verify naming conventions
   - Test auto-update detection

### Distribution Checklist

Before distributing:

1. **Test on Clean System**
   - Test on PC without Java installed
   - Verify all features work
   - Check auto-update functionality

2. **Version Management**
   - Update version in `pom.xml`
   - Update version in `application.properties`
   - Create Git tag
   - Build with correct version

3. **GitHub Release**
   - Create new release on GitHub
   - Upload both distribution types
   - Include checksums
   - Write clear release notes

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

4. **Auto-Update Compatibility**
   - Follow naming conventions exactly
   - Include both distribution types
   - Test update paths thoroughly

## Support

For issues with packaging:
1. Check error messages carefully
2. Verify all prerequisites
3. Run scripts from project root
4. Check GitHub issues
5. Review build logs

Remember: The installer and portable versions are self-contained and don't require Java on the target system, making them the best choice for distribution.