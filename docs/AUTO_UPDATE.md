# Auto-Update Feature Documentation

## Overview

The SiMP3 music player includes an automatic update system that checks for new releases on GitHub and allows users to download and install updates seamlessly.

## Features

- **Automatic Update Checks**: Checks for updates on application startup (configurable)
- **Manual Update Checks**: Users can manually check for updates via Help → Check for Updates
- **Background Downloads**: Updates download in the background with progress indication
- **Version Comparison**: Smart semantic versioning comparison (major.minor.patch)
- **Skip Version**: Users can skip specific versions they don't want to install
- **Checksum Verification**: SHA-256 checksum verification for download integrity
- **Staged Updates**: Updates are staged and applied on next application restart

## Configuration

### For Developers

1. Update the GitHub repository configuration in `UpdateConfig.java`:
```java
public static final String GITHUB_OWNER = "your-github-username";
public static final String GITHUB_REPO = "your-repository-name";
```

2. Create GitHub releases with:
   - Proper version tags (e.g., `v1.0.1`, `v2.0.0`)
   - Windows executable as a release asset
   - Release notes in the description
   - SHA-256 checksum in the release body (format: `SHA-256: <checksum>`)

### For Users

Update settings are stored in the application settings:
- **Auto-check for updates**: Enable/disable automatic update checks
- **Check interval**: How often to check (default: 24 hours)
- **Skipped versions**: Versions the user chose to skip

## Update Process

1. **Check Phase**:
   - Application queries GitHub Releases API
   - Compares current version with latest release
   - Shows update dialog if newer version available

2. **Download Phase**:
   - User clicks "Download & Install"
   - Update downloads with progress bar
   - SHA-256 checksum verified after download

3. **Installation Phase**:
   - Update staged in `updates/` directory
   - User prompted to restart application
   - On restart, batch script applies the update

## File Structure

```
SiMP3/
├── updates/              # Staged updates directory
│   └── pending/          # Pending update files
├── update-wrapper.bat    # Windows update script
└── SiMP3.exe            # Main application
```

## Technical Details

### Components

- **UpdateService**: Core service handling update logic
- **UpdateDialog**: User interface for update notifications
- **UpdateInfo**: Model for update information
- **VersionComparator**: Semantic version comparison utility
- **UpdateConfig**: Configuration constants

### API Integration

The update system uses GitHub's REST API:
```
GET https://api.github.com/repos/{owner}/{repo}/releases/latest
```

### Security

- HTTPS connections for all downloads
- SHA-256 checksum verification
- No automatic installation without user consent

## Creating a Release

1. Build the application:
   ```bash
   mvn clean package
   ```

2. Calculate SHA-256 checksum:
   ```powershell
   Get-FileHash -Algorithm SHA256 .\target\SiMP3.exe
   ```

3. Create GitHub release:
   - Tag: `v1.0.1` (semantic versioning)
   - Title: `SiMP3 v1.0.1`
   - Description: Include release notes and checksum
   - Asset: Upload `SiMP3.exe`

Example release description:
```markdown
## What's New
- Added auto-update functionality
- Fixed playlist sorting bug
- Improved performance

## Checksum
SHA-256: ABC123DEF456...
```

## Troubleshooting

### Update Check Fails
- Check internet connection
- Verify GitHub repository is public
- Check UpdateConfig has correct owner/repo

### Download Fails
- Check available disk space
- Verify firewall settings
- Try manual download from GitHub

### Installation Fails
- Run application as administrator
- Check file permissions
- Manually apply update from `updates/` directory

## Future Enhancements

- [ ] Delta updates (only download changed files)
- [ ] Rollback functionality
- [ ] Update channels (stable/beta)
- [ ] Automatic backup before update
- [ ] Linux/macOS update support