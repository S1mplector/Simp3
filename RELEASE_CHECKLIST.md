# SiMP3 Release Checklist

## Pre-Release Steps

### 1. Version Update
- [ ] Update version in `pom.xml`
- [ ] Update version in `package-simp3.bat` 
- [ ] Update version in application UI (if displayed)

### 2. Testing
- [ ] Test all core features:
  - [ ] Music library scanning
  - [ ] Playback controls (play, pause, stop, next, previous)
  - [ ] Volume control
  - [ ] Playlist management
  - [ ] Search functionality
  - [ ] Settings persistence
  - [ ] Audio visualizer
- [ ] Test on clean Windows installation
- [ ] Test with various audio formats (MP3, FLAC, etc.)

### 3. Documentation
- [ ] Update README.md with latest features
- [ ] Update screenshots if UI changed
- [ ] Document any new dependencies or requirements

## Build & Package

### 4. Run Packaging Script
```batch
package-simp3.bat
```

This will:
- Clean and build the project
- Create self-contained executable with jpackage
- Generate portable ZIP package
- Copy standalone executable
- Create distribution README
- Generate checksums

### 5. Verify Distribution Files

The `dist/` folder should contain:
- `SiMP3-v1.0.0-portable-win64.zip` - Main distribution file
- `SiMP3-v1.0.0-requires-java.exe` - Alternative for Java users
- `README.txt` - User instructions
- `checksums.txt` - File integrity verification

## What to Ship to Users

### Recommended Distribution Package

**Primary Package: `SiMP3-v1.0.0-portable-win64.zip`**

This is what most users should download. It contains:
- Self-contained application with bundled Java runtime
- No installation required
- No Java prerequisites
- All dependencies included

### Alternative Package

**For Advanced Users: `SiMP3-v1.0.0-requires-java.exe`**

Only provide this if specifically requested or for users who:
- Already have Java 17+ installed
- Prefer smaller download size
- Are comfortable with Java requirements

## Post-Release

### 6. Distribution Platforms

Consider uploading to:
- [ ] GitHub Releases
- [ ] Your website
- [ ] SourceForge
- [ ] Other distribution platforms

### 7. Release Notes Template

```markdown
# SiMP3 v1.0.0 Release

## What's New
- Feature 1
- Feature 2
- Bug fixes

## Downloads
- **Recommended**: SiMP3-v1.0.0-portable-win64.zip (XX MB)
  - Self-contained, no Java required
- **Alternative**: SiMP3-v1.0.0-requires-java.exe (XX MB)
  - Requires Java 17+

## Installation
1. Download the portable ZIP file
2. Extract to your desired location
3. Run SiMP3.exe

## System Requirements
- Windows 10/11 64-bit
- 4GB RAM
- 200MB free disk space
```

## Quick Commands

### Full rebuild and package:
```batch
package-simp3.bat
```

### Test the packaged application:
```batch
dist\SiMP3-v1.0.0-portable-win64\SiMP3.exe
```

### Verify checksums:
```powershell
Get-FileHash dist\*.zip -Algorithm SHA256