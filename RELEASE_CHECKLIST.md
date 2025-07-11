# SiMP3 Release Checklist

## Pre-Release Steps

### 1. Version Update
- [ ] Update version in `pom.xml`
- [ ] Update version in `src/main/resources/application.properties`
- [ ] Update version in all packaging scripts:
  - [ ] `create-portable.bat`
  - [ ] `scripts/create-installer.bat`
  - [ ] `scripts/create-release.bat`
- [ ] Update version in application UI (if displayed)
- [ ] Commit version changes with message: `chore: bump version to X.Y.Z`

### 2. Code Quality
- [ ] Run all unit tests: `mvn test`
- [ ] Run integration tests if available
- [ ] Check for compiler warnings: `mvn clean compile`
- [ ] Review dependency updates: `mvn versions:display-dependency-updates`

### 3. Testing - Core Features
- [ ] Test all core features:
  - [ ] Music library scanning
  - [ ] Playback controls (play, pause, stop, next, previous)
  - [ ] Volume control and mute
  - [ ] Playlist management (create, edit, delete, reorder)
  - [ ] Search functionality (by title, artist, album)
  - [ ] Settings persistence
  - [ ] Audio visualizer
  - [ ] Mini player functionality
  - [ ] Keyboard shortcuts
- [ ] Test on clean Windows installation
- [ ] Test with various audio formats (MP3, FLAC, OGG, etc.)
- [ ] Test with large music libraries (1000+ songs)

### 4. Testing - Auto-Update System
- [ ] Test update detection:
  - [ ] Verify current version is correctly displayed
  - [ ] Test with mock GitHub release
  - [ ] Verify version comparison logic
- [ ] Test distribution type detection:
  - [ ] Run portable version and verify detection
  - [ ] Run installed version and verify detection
- [ ] Test update dialog:
  - [ ] Single distribution type scenario
  - [ ] Multiple distribution types scenario
  - [ ] "Remember my choice" functionality
- [ ] Test update download:
  - [ ] Progress indication
  - [ ] Checksum verification
  - [ ] Error handling (network issues, disk space)
- [ ] Test update installation:
  - [ ] Portable to portable update
  - [ ] Installer to installer update
  - [ ] Cross-distribution update warnings

### 5. Documentation
- [ ] Update README.md with latest features
- [ ] Update screenshots if UI changed
- [ ] Update AUTO_UPDATE.md if update process changed
- [ ] Update PACKAGING_GUIDE.md if build process changed
- [ ] Document any new dependencies or requirements
- [ ] Update CHANGELOG.md with all changes

## Build & Package

### 6. Clean Build Environment
```batch
mvn clean
rmdir /S /Q target
rmdir /S /Q releases
```

### 7. Build Application
```batch
mvn clean package
```
Verify:
- [ ] Build completes without errors
- [ ] JAR file created in `target/`
- [ ] All dependencies included

### 8. Create Distribution Packages

#### Portable Version
```batch
create-portable.bat
```
Enter version when prompted (e.g., `1.0.1`)

Verify portable package:
- [ ] ZIP file created: `releases/SiMP3-v1.0.1-portable.zip`
- [ ] Contains all necessary files:
  - [ ] `SiMP3.exe`
  - [ ] `app/` directory with JAR and dependencies
  - [ ] `runtime/` directory with JRE
- [ ] Runs without installation
- [ ] Settings saved locally

#### Installer Version
```batch
scripts\create-installer.bat
```
Choose option 1 (EXE) or 2 (MSI) when prompted

Verify installer package:
- [ ] Installer created: `releases/SiMP3-v1.0.1-installer.exe` (or `.msi`)
- [ ] Installer runs without errors
- [ ] Proper installation to Program Files
- [ ] Start Menu shortcuts created
- [ ] Uninstaller works correctly
- [ ] Registry entries correct

### 9. Generate Checksums

For each distribution file:
```powershell
# Portable version
$portableHash = Get-FileHash -Algorithm SHA256 .\releases\SiMP3-v1.0.1-portable.zip
$portableHash.Hash | Out-File -FilePath .\releases\SiMP3-v1.0.1-portable.zip.sha256 -NoNewline

# Installer version
$installerHash = Get-FileHash -Algorithm SHA256 .\releases\SiMP3-v1.0.1-installer.exe
$installerHash.Hash | Out-File -FilePath .\releases\SiMP3-v1.0.1-installer.exe.sha256 -NoNewline
```

Verify:
- [ ] SHA-256 files created for each distribution
- [ ] Checksum files contain only the hash (no extra formatting)

### 10. Test Distribution Packages

#### Test Portable Version
- [ ] Extract to different locations (Desktop, USB drive, etc.)
- [ ] Verify it runs without admin rights
- [ ] Test auto-update from previous portable version
- [ ] Verify data portability

#### Test Installer Version  
- [ ] Install on clean system
- [ ] Verify Start Menu entries
- [ ] Test auto-update from previous installer version
- [ ] Test uninstallation

### 11. Final Distribution Structure

Verify `releases/` folder contains:
```
releases/
├── SiMP3-v1.0.1-portable.zip          # Portable distribution
├── SiMP3-v1.0.1-portable.zip.sha256   # Checksum for portable
├── SiMP3-v1.0.1-installer.exe         # Installer distribution
├── SiMP3-v1.0.1-installer.exe.sha256  # Checksum for installer
└── checksums.txt                       # Combined checksums (optional)
```

## GitHub Release

### 12. Create Release Tag
```bash
git tag -a v1.0.1 -m "Release version 1.0.1"
git push origin v1.0.1
```

### 13. Create GitHub Release

1. Go to GitHub repository → Releases → "Draft a new release"
2. Select the tag: `v1.0.1`
3. Release title: `SiMP3 v1.0.1`
4. Upload all distribution files:
   - [ ] `SiMP3-v1.0.1-portable.zip`
   - [ ] `SiMP3-v1.0.1-portable.zip.sha256`
   - [ ] `SiMP3-v1.0.1-installer.exe`
   - [ ] `SiMP3-v1.0.1-installer.exe.sha256`

### 14. Write Release Notes

Use this template:

```markdown
# SiMP3 v1.0.1 Release

## 🎉 What's New
- Feature 1: Brief description
- Feature 2: Brief description
- Enhancement: Improved performance in X

## 🐛 Bug Fixes
- Fixed issue where...
- Resolved problem with...

## 📦 Downloads

Choose the version that best suits your needs:

### 🎒 Portable Version
**[SiMP3-v1.0.1-portable.zip](link)** (12.3 MB)
- ✅ No installation required
- ✅ Run from anywhere (USB drive, cloud storage)
- ✅ All settings stored locally
- ✅ Perfect for trying out or portable use

**SHA-256:** `abc123...`

### 📦 Installer Version  
**[SiMP3-v1.0.1-installer.exe](link)** (23.4 MB)
- ✅ Traditional Windows installation
- ✅ Start Menu integration
- ✅ Automatic updates
- ✅ Clean uninstaller included

**SHA-256:** `def456...`

## 💡 Which Version Should I Choose?

- **Portable**: If you want to run SiMP3 from a USB drive, don't have admin rights, or prefer keeping everything in one folder
- **Installer**: If you want a traditional installation with Start Menu shortcuts and system integration

## 🔄 Updating from Previous Versions

The auto-update system will automatically detect your current installation type and offer the appropriate update. You can also manually download and install your preferred version.

## 📋 System Requirements
- Windows 10/11 (64-bit)
- 4GB RAM minimum
- 200MB free disk space
- Audio output device

## 🙏 Acknowledgments
Thanks to all contributors and testers who made this release possible!

---
**Full Changelog**: [v1.0.0...v1.0.1](link)
```

### 15. Publish Release
- [ ] Review all information is correct
- [ ] Verify all files are uploaded
- [ ] Set as latest release (unless pre-release)
- [ ] Publish release

## Post-Release

### 16. Verify Auto-Update System
- [ ] Install previous version (portable)
- [ ] Check for updates - should find v1.0.1
- [ ] Download and install update
- [ ] Verify successful update

- [ ] Install previous version (installer)
- [ ] Check for updates - should find v1.0.1  
- [ ] Download and install update
- [ ] Verify successful update

### 17. Update Documentation
- [ ] Update website/wiki with new version
- [ ] Update any installation guides
- [ ] Post release announcement (if applicable)

### 18. Monitor Release
- [ ] Check GitHub issues for any immediate problems
- [ ] Monitor download statistics
- [ ] Respond to user feedback

## Quick Commands Reference

### Full rebuild and package both versions:
```batch
# Clean and build
mvn clean package

# Create portable version
create-portable.bat

# Create installer version  
scripts\create-installer.bat
```

### Test packages:
```batch
# Test portable
cd releases\SiMP3-v1.0.1-portable
SiMP3.exe

# Test installer
releases\SiMP3-v1.0.1-installer.exe
```

### Generate all checksums at once:
```powershell
Get-ChildItem releases\*.zip, releases\*.exe, releases\*.msi | ForEach-Object {
    $hash = Get-FileHash $_.FullName -Algorithm SHA256
    $hash.Hash | Out-File -FilePath "$($_.FullName).sha256" -NoNewline
    Write-Host "$($_.Name): $($hash.Hash)"
}
```

## Important Notes

1. **Always test both distribution types** before releasing
2. **Ensure file naming follows conventions** for auto-update detection:
   - Portable: `SiMP3-v{version}-portable.zip`
   - Installer: `SiMP3-v{version}-installer.exe` or `.msi`
3. **Include checksums** for security and integrity verification
4. **Test auto-update paths** for both distribution types
5. **Document distribution-specific issues** in release notes

## Troubleshooting Release Issues

### Build Failures
- Clean Maven cache: `mvn dependency:purge-local-repository`
- Check Java version: `java -version` (should be 17+)
- Verify all dependencies: `mvn dependency:tree`

### Packaging Failures
- Ensure WiX Toolset installed (for MSI)
- Check available disk space
- Run scripts as Administrator if needed
- Verify jpackage is available: `jpackage --version`

### Auto-Update Test Failures
- Check GitHub API rate limits
- Verify file naming conventions
- Test with local file server
- Check firewall/proxy settings

Remember: A successful release requires patience and attention to detail. Take your time and verify each step!