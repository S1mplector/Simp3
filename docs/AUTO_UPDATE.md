# Auto-Update Feature Documentation

## Overview

The SiMP3 music player includes an advanced automatic update system that checks for new releases on GitHub and allows users to download and install updates seamlessly. The system now supports multiple distribution types (portable and installer versions) with intelligent detection and user preference management.

## Features

- **Automatic Update Checks**: Checks for updates on application startup (configurable)
- **Manual Update Checks**: Users can manually check for updates via Help → Check for Updates
- **Distribution Type Support**: Handles both portable and installer versions intelligently
- **Distribution Type Detection**: Automatically detects whether running portable or installed version
- **User Preference Management**: Remembers user's preferred distribution type
- **Background Downloads**: Updates download in the background with progress indication
- **Version Comparison**: Smart semantic versioning comparison (major.minor.patch)
- **Skip Version**: Users can skip specific versions they don't want to install
- **Checksum Verification**: SHA-256 checksum verification for download integrity
- **Staged Updates**: Updates are staged and applied on next application restart
- **Cross-Distribution Awareness**: Handles updates between different distribution types appropriately

## Distribution Types

### Portable Version
- **Characteristics**:
  - No installation required
  - Can run from any location (USB drive, Desktop, etc.)
  - Self-contained with all dependencies
  - Settings and data stored locally
  - Easy to move between computers

- **File Naming**: `SiMP3-v{version}-portable.zip`
- **Update Process**: Extracts and replaces files in-place

### Installer Version
- **Characteristics**:
  - System-wide installation
  - Start Menu integration
  - Proper uninstaller included
  - Registered with Windows Programs & Features
  - Standard installation paths

- **File Naming**: 
  - `SiMP3-v{version}-installer.exe`
  - `SiMP3-v{version}-setup.exe`
  - `SiMP3-v{version}-installer.msi`
- **Update Process**: Runs installer with silent flags

## Distribution Type Detection

The system automatically detects the current distribution type using multiple methods:

1. **Path-Based Detection**:
   - Installer paths: `Program Files`, `ProgramData`, `AppData\Local`, `AppData\Roaming`
   - Portable paths: `Desktop`, `Downloads`, `Documents`, paths containing "portable"

2. **Directory Structure Detection**:
   - Presence of `update/` or `data/` directories indicates portable version
   - Standard Windows installation paths indicate installer version

3. **Fallback**: If detection fails, the system prompts the user to choose

## User Experience Flow

### When Single Distribution Type Available
1. Update notification appears with version information
2. User clicks "Update Now"
3. Update downloads with progress indication
4. Application prompts for restart
5. Update applies automatically on restart

### When Multiple Distribution Types Available
1. Update notification shows distribution choice
2. User selects preferred type (Portable or Installer)
3. Option to "Remember my choice" for future updates
4. Download proceeds with selected distribution
5. Appropriate update script runs on restart

## Configuration

### For Developers

1. Update the GitHub repository configuration in `UpdateConfig.java`:
```java
public static final String GITHUB_OWNER = "your-github-username";
public static final String GITHUB_REPO = "your-repository-name";
```

2. Create GitHub releases with multiple distribution types:
   - Proper version tags (e.g., `v1.0.1`, `v2.0.0`)
   - Multiple release assets with proper naming:
     - `SiMP3-v1.0.1-portable.zip`
     - `SiMP3-v1.0.1-installer.exe`
   - Release notes in the description
   - SHA-256 checksums for each asset

### For Users

Update settings are stored in the application settings:
- **Auto-check for updates**: Enable/disable automatic update checks
- **Check interval**: How often to check (default: 24 hours)
- **Skipped versions**: Versions the user chose to skip
- **Preferred distribution type**: User's preferred distribution (Portable/Installer)
- **Remember distribution choice**: Whether to remember the preference

## File Naming Conventions

Release assets must follow these naming patterns for proper detection:

### Portable Versions
- Primary: `SiMP3-v{version}-portable.zip`
- Alternative: `SiMP3-v{version}-portable.exe` (self-extracting)
- Must contain "portable" in the filename

### Installer Versions
- EXE Installer: `SiMP3-v{version}-installer.exe` or `SiMP3-v{version}-setup.exe`
- MSI Installer: `SiMP3-v{version}-installer.msi`
- Must contain "installer" or "setup" in the filename

### Checksums
- Format: `{filename}.sha256`
- Example: `SiMP3-v1.0.1-portable.zip.sha256`

## Update Process Details

### 1. Check Phase
- Application queries GitHub Releases API
- Parses all available assets for the latest release
- Identifies distribution types from filenames
- Compares current version with latest release
- Shows update dialog if newer version available

### 2. Download Phase
- User selects distribution type (if multiple available)
- Update downloads with progress bar
- SHA-256 checksum verified after download
- Download stored in temporary directory

### 3. Installation Phase
- Update staged in `update/` directory
- Appropriate update script created based on distribution type
- User prompted to restart application
- On restart, update script applies the update

## Technical Implementation

### Core Components

- **DistributionType**: Enum defining distribution types and detection logic
  ```java
  public enum DistributionType {
      PORTABLE("Portable", "portable"),
      INSTALLER("Installer", "installer", "setup"),
      UNKNOWN("Unknown");
  }
  ```

- **UpdateService**: Enhanced with distribution type detection and handling
  - `detectDistributionType()`: Detects current installation type
  - `parseReleaseInfo()`: Parses multiple assets and their types
  - `createUpdateScript()`: Creates appropriate script for update type

- **UpdateDialog**: Enhanced UI for distribution selection
  - Shows distribution options when multiple types available
  - Displays file sizes for each option
  - Remembers user preferences

- **UpdateInfo**: Model enhanced with distribution type information
  ```java
  private DistributionType distributionType = DistributionType.UNKNOWN;
  ```

- **Settings**: Stores distribution preferences
  ```java
  private DistributionType preferredDistributionType = DistributionType.UNKNOWN;
  private boolean rememberDistributionChoice = false;
  ```

### Update Scripts

The system generates different update scripts based on the distribution type:

#### Portable Update Script
- Backs up current executable
- Extracts ZIP files using PowerShell
- Handles self-extracting archives
- Replaces files in-place
- Includes rollback on failure

#### Installer Update Script
- Detects installer type (MSI vs EXE)
- Uses appropriate silent installation flags
- Handles different installer frameworks
- Cleans up after installation
- Provides user feedback for portable-to-installer transitions

## API Integration

The update system uses GitHub's REST API:
```
GET https://api.github.com/repos/{owner}/{repo}/releases/latest
```

Response parsing handles multiple assets:
```json
{
  "assets": [
    {
      "name": "SiMP3-v1.0.1-portable.zip",
      "browser_download_url": "...",
      "size": 12345678
    },
    {
      "name": "SiMP3-v1.0.1-installer.exe",
      "browser_download_url": "...",
      "size": 23456789
    }
  ]
}
```

## Security

- HTTPS connections for all downloads
- SHA-256 checksum verification for each distribution type
- No automatic installation without user consent
- Update scripts run with appropriate permissions

## Creating a Release

### 1. Build Both Distribution Types

```bash
# Build portable version
create-portable.bat

# Build installer version
scripts\create-installer.bat
```

### 2. Calculate SHA-256 Checksums

```powershell
# For portable version
Get-FileHash -Algorithm SHA256 .\releases\SiMP3-v1.0.1-portable.zip

# For installer version
Get-FileHash -Algorithm SHA256 .\releases\SiMP3-v1.0.1-installer.exe
```

### 3. Create Checksum Files

```powershell
# Create checksum files
"CHECKSUM_HERE" | Out-File -FilePath .\releases\SiMP3-v1.0.1-portable.zip.sha256
"CHECKSUM_HERE" | Out-File -FilePath .\releases\SiMP3-v1.0.1-installer.exe.sha256
```

### 4. Create GitHub Release

- Tag: `v1.0.1` (semantic versioning)
- Title: `SiMP3 v1.0.1`
- Upload all assets:
  - `SiMP3-v1.0.1-portable.zip`
  - `SiMP3-v1.0.1-portable.zip.sha256`
  - `SiMP3-v1.0.1-installer.exe`
  - `SiMP3-v1.0.1-installer.exe.sha256`

Example release description:
```markdown
## What's New
- Added distribution type support to auto-update
- Fixed playlist sorting bug
- Improved performance

## Downloads
Choose the version that suits your needs:

### 🎒 Portable Version
`SiMP3-v1.0.1-portable.zip` (12.3 MB)
- No installation required
- Run from anywhere
- Perfect for USB drives

### 📦 Installer Version
`SiMP3-v1.0.1-installer.exe` (23.4 MB)
- Traditional Windows installation
- Start Menu integration
- Automatic uninstaller

## Checksums
Verify your download integrity:
- Portable: `ABC123...` (SHA-256)
- Installer: `DEF456...` (SHA-256)
```

## Troubleshooting

### Distribution Type Detection Issues
- **Problem**: Wrong distribution type detected
- **Solution**: Check installation path and directory structure
- **Manual Override**: Delete settings file to force re-detection

### Update Check Fails
- Check internet connection
- Verify GitHub repository is public
- Check UpdateConfig has correct owner/repo
- Ensure release assets follow naming conventions

### Download Fails
- Check available disk space
- Verify firewall settings
- Try manual download from GitHub
- Check if specific distribution type is available

### Installation Fails
- **Portable**: Ensure write permissions in application directory
- **Installer**: Run as administrator if needed
- Check Windows Defender or antivirus interference
- Manually apply update from `update/` directory

### Cross-Distribution Updates
- **Portable to Installer**: Update will install to Program Files
- **Installer to Portable**: User will be notified of location change
- Consider backing up settings before cross-distribution updates

## Best Practices

### For Developers
1. **Always provide both distribution types** for each release
2. **Use consistent naming conventions** for assets
3. **Include checksums** for all downloadable files
4. **Test both update paths** before releasing
5. **Document any distribution-specific features** in release notes

### For Users
1. **Choose the right distribution**:
   - Portable: For USB drives, no admin rights, or temporary use
   - Installer: For permanent installation with system integration
2. **Stick to one distribution type** for smoother updates
3. **Back up your data** before major updates
4. **Enable "Remember my choice"** for consistent updates

## Future Enhancements

- [ ] Delta updates (only download changed files)
- [ ] Rollback functionality with version history
- [ ] Update channels (stable/beta) per distribution type
- [ ] Automatic backup before update
- [ ] Linux/macOS distribution support
- [ ] Custom distribution type plugins
- [ ] P2P update distribution for faster downloads